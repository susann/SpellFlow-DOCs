package spell_2_spell_flow_button.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import lu.uni.snt.spell.SPELLStandaloneSetup;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.ApplicationMonitor;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.interpreter.util.HenshinEGraph;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.MultiUnit;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.impl.HenshinPackageImpl;

import SpellFlowLanguage.Root;
import SpellFlowLanguage.SpellFlowLanguagePackage;

/**
 * Some code is taken from and inspired by the following projects:<br>
 * - {@link lu.uni.snt.spell.symexec/ExecHandler.java}<br>
 * - {@link de.tub.tfs.henshin.tgg.interpreter.gui.LoadHandler} and {@link de.tub.tfs.henshin.tgg.interpreter.gui.TransHandler}<br>
 * But the source code is modified significantly.
 * 
 * This class is performing the translation of an input file in executing the following steps:<br>
 * - First, perform triple graph transformation of a set of triple graph grammars. 
 *   Each TGG is applied after each other. Each intermediate result is saved in a separate output file.<br>
 * - Afterwards, a set of flat graph grammars is applied after each other (for refactorings). 
 *   Each intermediate result is saved in a separate output file. The last output file is provided
 *   to the calling class for further operations. <br><br>
 *  
 *  Remark: Currently, we apply one TGG and one flat graph grammar.
 * 
 * @author susann.gottmann
 *
 */
public class ExecutionJobFlat extends Job {
	/**
	 * Debug flag: if true, then additional printouts on console activated
	 */
	private boolean _debug = true;
	/** List containing full paths to all henshin files (only flat graphs) 
	 * that will be used for the translation.
	 */
	protected Queue<File> loadQueue = new LinkedList<File>();
	/**
	 * URI of SPELL eProc to be translated
	 */
	private URI inputURI;
	/**
	 * Path to input file
	 */
	private File inputFile;
	/**
	 * The ResourceSet used for file system operations (e.g. access input files, save output files, etc.)
	 */
	private ResourceSet resSet = null;
	/**
	 * Semaphore so that processes with HenshinTGG do not run in parallel
	 */
	static Semaphore semaphore = new Semaphore(1);
	/**
	 * Flat graph grammar
	 */
	private Module henshinGrammar;
	/**
	 * Engine used for flat graph transformation
	 */
	private EngineImpl emfEngineFlat;
	/**
	 * A helper flag
	 */
	private boolean init = false;
	/**
	 * For flat graph grammars, the main transformation unit which will be executed, 
	 * shall always be named "Main" (i.e., take care when writing the grammar that 
	 * this always holds).
	 */
	private static final String transformationUnitToExecute = "Main";
	/**
	 * List of applied rules as HashMap
	 */
	private HashMap<String, Integer> ruleApps = new HashMap<String, Integer>();
	/**
	 * Save the last filename in order to reuse it in {@link spell_2_spell_flow_button.command.TranslateButton}
	 * which provides the name to @link spell_2_spell_flow_button.parser.DOMRead which does the final enrichment
	 * of design xmi tags to this file.
	 */
	String output;

	
	
	// ##################################################################################################################
	
	
	
	/**
	 * Constructor
	 * 
	 * @param inputFile
	 */
	public ExecutionJobFlat(File inputFile) {
		super("Transformation of " + inputFile.getName());
		this.setUser(true); // ?
		System.out.println("Transformation of " + inputFile);
		
		this.resSet = new ResourceSetImpl();
		this.inputFile = inputFile;

		this.loadQueue.clear();
	}
	
	
	
	/**
	 * Main method - executes the transformation of file inputURI,
	 * i.e., applies all given TGGs first, then all given flat grammars.
	 * 
	 * @param monitor
	 * @return Status.OK_STATUS or Exception
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		boolean grammarLoaded = false;
		try {
			// Sequentialise processing of several files
			semaphore.acquire(); 
			
			ruleApps.clear();
				
			monitor.beginTask("Loading Execution Grammar", 2);
			System.out.println("Loading Grammar");

			if (!init){
				SPELLStandaloneSetup.doSetup();
				monitor.worked(1);

				this.initHenshin();
				monitor.worked(1);

				init = true;
			} else {
				// Increase the status of monitor
				monitor.worked(2);
			}

			// ---------------------- Initialisation ----------------------------
			URI outputURI = null;
			EGraph graph = null;
			grammarLoaded = false;
			int round = 0;
			
			System.out.println("--------------START FLAT GRAPH TRANSFORMATION--");

			// ---------------------- Flat Graph Transformation ------------------------
			
			for(File grammar : loadQueue) {
				if(grammarLoaded && henshinGrammar != null) {
					cleanGrammar(henshinGrammar);
					grammarLoaded = false;
				}
				
				String s = grammar.getName();
				// Retrieve grammar name without ending
				if(s.endsWith(".henshin"))
				{
					s = s.substring(0, s.length() - 8);
				}
				
				// Load grammar and do the transformation with Henshin
				this.loadGrammar(s);
				grammarLoaded = true;

				monitor.worked(1);

				monitor.beginTask("Flat Graph Transformation (i.e., Refactoring) of: " + inputFile.getName(), 3);
				
				
				// Init graph (which will be used during whole transformation steps) with null
				
				// ############################ TODO ########################################
				
				graph = this.parseSPELLeProc(inputFile);
				if(graph == null) {
					throw new NullPointerException("Graph is null");
				}

				
				// ############################ TODO ########################################

				monitor.worked(1);

				// Graph
				Unit unit = henshinGrammar.getUnit(transformationUnitToExecute);
				
				System.out.println("--------------GRAPHS BEFORE TRANS--------------");
				System.out.println(graph.toString());
				
				emfEngineFlat = prepareEMFEngine(emfEngineFlat);
				applyUnitToGraph(unit, graph, new HenshinApplicationMonitor(monitor));
				monitor.worked(1);

				System.out.println("--------------GRAPHS AFTER TRANS---------------");
				System.out.println(graph.toString());

				
				Integer c_initial = new Integer(0);
				for (Integer c : ruleApps.values()) {
					c_initial += c;
				}
				System.out.println(ruleApps.toString() + " (#" + c_initial + ")");
				
				monitor.beginTask("Save generated SPELL-Flow", 1);

				// Serialise flowchart xmi file
				String spellFlowRootNodeName = SpellFlowLanguagePackage.eINSTANCE.getRoot().getName();
				List<EObject> roots = graph.getDomain((EClass)SpellFlowLanguagePackage.eINSTANCE.getEClassifier(spellFlowRootNodeName), true);

				Iterator<EObject> it = roots.iterator();

				while (it.hasNext()) {
					Root node=(Root)it.next();
					URI trimmedURI = this.inputURI.trimFileExtension();
					trimmedURI = trimmedURI.trimSegments(1);
					String nameOfURI = trimmedURI.lastSegment();
					
					outputURI = trimmedURI.appendSegment(nameOfURI + "_out_ref" + round).appendFileExtension("sfl");
					
					resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("sfl", new XMLResourceFactoryImpl());
					Resource res1 = resSet.createResource(outputURI);
					res1.getContents().add(node);
					
					try {
						Map<String, Object> saveOptions = new HashMap<String, Object>();						
						res1.save(saveOptions);
						round++;
						
						// Remember output file with parent folder (NOT whole path)
						String outputWithLastFolder = outputURI.segment(outputURI.segmentCount() - 2) + "\\" + outputURI.lastSegment();
						setOutput(outputWithLastFolder);
//#DEBUG START
						if(_debug) {
							System.out.println("--------------TRANSLATION RESULT SAVED---------");
							System.out.println(outputURI.toString());
							// Unfortunately, this does NOT result in a reusable path, 
							// e.g., on Windows: "/" are generated, whereas Windows needs "\"
							System.out.println(outputURI.device() + outputURI.path());
							System.out.println(outputWithLastFolder);
						}
//#DEBUG END				
					} catch (IOException e) {
						e.printStackTrace();
					}
					unloadModel(resSet, outputURI);
				}
				monitor.worked(1);
			}
			
			unloadModel(resSet, this.inputURI);
			cleanGrammar(henshinGrammar);
			semaphore.release();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}

		// Say: "Job is done." explicitly.
		this.done(Status.OK_STATUS);
		return Status.OK_STATUS;
	}

	

	/**
	 * Executes flat graph transformation in applying a given transformation unit to a
	 * given EGraph.
	 * 
	 * @param unit
	 * @param graph
	 * @param monitor
	 * @return boolean that indicates, if the transformation was successful or not
	 */
	private boolean applyUnitToGraph(Unit unit, EGraph graph, ApplicationMonitor monitor) {
		UnitApplication unitApplication = new UnitApplicationImpl(emfEngineFlat, graph, unit, null);
		return unitApplication.execute(monitor);
	}

	
	
	/**
	 * Creates a new EngineImpl object or cleans the eisting one.
	 * 
	 * @param emfEngine
	 * @return the new or cleaned EmfEngine object
	 */
	private EngineImpl prepareEMFEngine(EngineImpl emfEngine) {
		if (emfEngine != null) {
			emfEngine.clearCache();
			return emfEngine;
		} else {
			return new EngineImpl();
		}
	}

	
	
	/**
	 * Takes an IFile as input and loads and parses it in order 
	 * to use it in flat graph transformation.
	 * 
	 * This method is very similar to {@link spell_2_spell_flow_button.command.ExecutionJob.parseSPELLeProcForTGG(IFile)}
	 * 
	 * @param file
	 * @return flat EGraph parsed from the file
	 */
	private EGraph parseSPELLeProc(File file) {
		//this.inputURI = URI.createPlatformResourceURI(ifile.getPath(), true);
		this.inputURI = URI.createFileURI(file.getPath());
		Resource res = resSet.getResource(inputURI, true);
		EObject spellRootNode = (EObject)res.getContents().get(0);
		Graph g = HenshinFactory.eINSTANCE.createGraph();
		EGraph graph = new HenshinEGraph(g); 
		graph.addGraph(spellRootNode);
		return graph;
	}

	
	
	/**
	 * Initialise Henshin and HenshinTGG.
	 */
	private void initHenshin() {
		HenshinPackageImpl.init();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new XMIResourceFactoryImpl());	
	}
	
	
	
	/**
	 * Loads the flat graph grammar which has the same name as indicated in parameter "grammar". 
	 * 
	 * This method is very similar to {@link spell_2_spell_flow_button.command.ExecutionJob.loadTGG(String)}
	 * 
	 * @param grammar
	 * @throws IOException
	 */
	private void loadGrammar(String grammar) throws IOException {
		URL url = FileLocator.toFileURL(Platform.getBundle("SPELL-2-SPELL-Flow-Button").getEntry("/"));
		URI grammarFileName = URI.createFileURI(grammar);
		String fileExtension = "henshin";
		URI uri = URI.createFileURI(url.getFile() + grammarFileName.appendFileExtension(fileExtension).toFileString());
		Resource res = resSet.getResource(uri, true);
		EList<EObject> modules = res.getContents();
		henshinGrammar = (Module) modules.get(0);
//#DEBUG START			
		if(_debug) {
			System.out.println("Load grammar " + grammarFileName + "." + fileExtension + " was successful.");
		}
//#DEBUG END
	}

	
	
	/**
	 * Unloads the resource provided by its URI.
	 * 
	 * @param resSet
	 * @param uri
	 */
	private static void unloadModel(ResourceSet resSet, URI uri) {
		Resource res = resSet.getResource(uri, false);
		
		try {
			if (res != null)
				res.unload();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * ...
	 * 
	 * @param m
	 */
	private static void cleanGrammar(Module m){
		TreeIterator<EObject> treeIterator = m.eAllContents();
		while (treeIterator.hasNext()){
			EObject eObject = treeIterator.next();
			if (!eObject.eAdapters().isEmpty())
				for (Iterator<Adapter> itr = eObject.eAdapters().iterator();itr.hasNext();){
					Adapter next = itr.next();
					try {
						Field field = next.getClass().getDeclaredField("this$0");
						field.setAccessible(true);
						Object object = field.get(next);
						if (object instanceof Engine){
							itr.remove();
						}
					} catch (Exception ex){
						
					}
				}
		}
	}

	
	
	/**
	 * Setter for loadQueue.
	 * 
	 * @param loadQueue
	 */
	public void setGrammar(Queue<File> loadQueue) {
		this.loadQueue = loadQueue;
	}

	
	
	/**
	 * 
	 * @param units
	 * @param folder
	 */
	protected static void getAllRules(List<Rule> units, MultiUnit folder){
		for (Unit unit : folder.getSubUnits()) {
			if (unit instanceof MultiUnit){
			} else {
				units.add((Rule) unit);
			}
		}
	}

	
	
	/**
	 * Setter for output.
	 * 
	 * @param s
	 */
	private void setOutput(String s) {
		output = s;
	}
	
	
	
	/**
	 * Getter for output.
	 * 
	 * @return
	 */
	public String getOutput() {
		return output;
	}
	
}

