package spell_2_spell_flow_button.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import lu.uni.snt.spell.SPELLStandaloneSetup;
import lu.uni.snt.spell.sPELL.SPELLPackage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.model.HenshinPackage;
import org.eclipse.emf.henshin.model.IndependentUnit;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.MultiUnit;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.SequentialUnit;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.impl.HenshinPackageImpl;

import Correspondence.CorrespondencePackage;
import SpellFlowLanguage.Root;
import SpellFlowLanguage.SpellFlowLanguagePackage;
import de.tub.tfs.henshin.tgg.TGG;
import de.tub.tfs.henshin.tgg.TggPackage;
import de.tub.tfs.henshin.tgg.interpreter.impl.TggTransformationImpl;
import de.tub.tfs.muvitor.ui.utils.EMFModelManager;

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
 * @todo Apply more than one TGG, especially in the order: TGG, flat grammar, TGG is impossible.
 *
 */
public class ExecutionJobTGG extends Job {
	/**
	 * Debug flag: if true, then additional printouts on console activated
	 */
	private boolean _debug = true;
	/** List containing full paths to all henshin files (only TGG) 
	 * that will be used for the translation.
	 */
	protected Queue<File> loadQueueTGG = new LinkedList<File>();
	/**
	 * URI of SPELL eProc to be translated
	 */
	private URI inputURI;
	/**
	 * Path to input file
	 */
	private IFile inputFile;
	/**
	 * The ResourceSet used for file system operations (e.g. access input files, save output files, etc.)
	 */
	private ResourceSet resSet = null;
	/**
	 * Semaphore so that processes with HenshinTGG do not run in parallel
	 */
	static Semaphore semaphore = new Semaphore(1);
	/**
	 * Triple graph grammar
	 */
	private TGG henshinTGG;
	/**
	 * Engine used for triple graph transformation
	 */
	//private TggEngineImpl emfEngineTGG;
	/**
	 * A helper flag
	 */
	private boolean init = false;
	/**
	 * For TGGs, the transformation units which will be executed, shall always
	 * be named "FTRuleFolder" (due to forward translations).
	 */
	private static final String transformationUnitToExecuteTGG = "FTRuleFolder";
	/**
	 * List of applied rules as HashMap
	 */
	private HashMap<String, Integer> ruleApps = new HashMap<String, Integer>();
	/**
	 * List of applied rules as String
	 */
	//private String appliedRules = "";
	/**
	 * The EMFModelManager
	 */
	private EMFModelManager manager;
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
	public ExecutionJobTGG(IFile inputFile) {
		super("Transformation of " + inputFile.getName());
		this.setUser(true); // ?
		System.out.println("Transformation of " + inputFile);
		
		this.resSet = new ResourceSetImpl();
		this.inputFile = inputFile;

		this.loadQueueTGG.clear();
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
			
			//appliedRules = "";
			ruleApps.clear();
				
			monitor.beginTask("Loading Execution TGG", 2);
			System.out.println("Loading TGG");

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
			List<EObject> inputEObjects = null;

			// JUST ANOTHER TRY TO LOAD TGGs
			// Register conversion from flat henshin to tgg -> in order to load henshin file as tgg
			manager = EMFModelManager.createModelManager("henshin");
			manager.cleanUp();
			initClassConversions();
			setEclipsePrefs();
			// Temp value counting the TGG transformations. Used for distinguishing each 
			// intermediate file that will be saved. (The counter is used as index.) 
			int round = 0;
			
			
			// ---------------------- TGG Transformation ------------------------
			// Foreach TGG in loadQueueTGG list do a transformation...
			for(File grammar : loadQueueTGG) {
				if(grammarLoaded && henshinTGG != null) {
					cleanGrammar(henshinTGG);
					grammarLoaded = false;
				}
				
				String s = grammar.getName();
				// Retrieve grammar name without ending
				if(s.endsWith(".henshin"))
				{
					s = s.substring(0, s.length() - 8);
				}
				
				// Load TGG and set henshinTGG
				this.loadTGG(s);
				grammarLoaded = true;
				monitor.worked(1);

				// ##################################################################################################################
				/*
				// In case we are not on the first round anymore, then we can reset the inputFile to
				// the last saved output file.
				if(outputURI != null) {
					String b = outputURI.lastSegment();
					String c = outputURI.segment(outputURI.segments().length -2);
					IPath p = Path.fromOSString(inputFile.toString());
					p = p.removeLastSegments(1).append(c).append(b);
					
					// WIRD NICHT GEFUNDEN
					IFile i = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(p);
					if(i != null) {
						inputFile = i;
					}
				}
				System.out.println("--------------FILE USED FOR TRANSLATION--------");
				System.out.println(inputFile.toString());
				*/
				
				monitor.beginTask("Triple Graph Transformation (i.e., Translation) of: " + inputFile.getName(), 3);
				if(graph == null) {
					// Load file that will be translated
					inputEObjects = parseSPELLeProcForTGG(inputFile); // add all of root
//#DEBUG START
					if(_debug) {
						System.out.println("--------------GRAPHS BEFORE TRANS--------------");
						System.out.println(inputEObjects.toString());
					}
//#DEBUG END
				}

				
				// Do the transformation with HenshinTGG
				TggTransformationImpl tggTransformation = new TggTransformationImpl();
				// ####################################################################################################
				// TODO DO IT LATER -> WHEN I NEED TO APPLY A SECOND TGG (with source model = target model)
				if(graph != null) {
					tggTransformation.setInput(graph.getRoots());
				} else {
					tggTransformation.setInput(inputEObjects);
				}
				tggTransformation.opRulesList.clear();
				
				//emfEngineTGG = tggTransformation.getEmfEngine();

				// Retrieve list of operational rules
				// ATTENTION: 
				// If we have a sequential unit directly below of FTRuleFolder, 
				// then we need a list of the rule list where each "block" will
				// be applied after each other.
				List<List<Rule>> opRulesList = getOpRulesSequential(henshinTGG);
				if(opRulesList.isEmpty()) {
					opRulesList.add(getOpRules(henshinTGG));
				}
//#DEBUG START
				if(_debug) {
					System.out.println(opRulesList);
				}
//#DEBUG END
				
				// Counter: For system.out.println information
				int counter = 1;
				for (List<Rule> opRules : opRulesList) {
					boolean foundApplicationForRound = false;
					boolean foundApplicationForModule = false;

					boolean newMatchesArePossible = true;
					boolean initialRound = true;

					// Print counter to console
					System.out.println("###" + counter + ". round of rule application. ###");
					counter++;
					
					// Execute each list as long as there are matches
					while (newMatchesArePossible) {
						foundApplicationForRound = false;
						tggTransformation.setOpRuleList(opRules);
						tggTransformation.setNullValueMatching(henshinTGG.isNullValueMatching());
	
						foundApplicationForModule = tggTransformation.applyRules(_debug);
						monitor.worked(1);
						if (monitor.isCanceled()) {
							monitor.done();
							return Status.CANCEL_STATUS;
						}
						foundApplicationForRound = foundApplicationForRound || foundApplicationForModule;
	
						if (!initialRound && foundApplicationForRound)
							System.out.println(
									"Warning: some ruleapplications depend on rules that are "
									+ "applied in a subsequent module. This can cause inefficient executions. "
									+ "Try to reorder the modules or rules.");
	
						initialRound = false;
						newMatchesArePossible = foundApplicationForRound;
					}
				}
				
				monitor.worked(1);

				graph = tggTransformation.getGraph();
				
//#DEBUG START
				if(_debug) {
					System.out.println("--------------GRAPHS AFTER TRANS---------------");
					System.out.println(graph.toString());
				}
//#DEBUG END				

				removeNodesAndEdgesFromGraph(graph);
				
//#DEBUG START
				if(_debug) {
					System.out.println("--------------GRAPHS AFTER DELETION------------");
					System.out.println(graph.toString());
				}
//#DEBUG END				
				
				// Save intermediate result
				String spellFlowRootNodeName = SpellFlowLanguagePackage.eINSTANCE.getRoot().getName();
				List<EObject> roots = graph.getDomain((EClass)SpellFlowLanguagePackage.eINSTANCE.getEClassifier(spellFlowRootNodeName), true);

				Iterator<EObject> it = roots.iterator();

				while (it.hasNext()) {
					Root node=(Root)it.next();
					URI trimmedURI = this.inputURI.trimFileExtension();
					outputURI = trimmedURI.appendSegment(trimmedURI.segment(trimmedURI.segmentCount() - 1) + "_out_tgg" + round).appendFileExtension("sfl");
					
					resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("sfl", new XMLResourceFactoryImpl());
					Resource res1 = resSet.createResource(outputURI);
					res1.getContents().add(node);
					
					try {
						Map<String, Object> saveOptions = new HashMap<String, Object>();						
						res1.save(saveOptions);
						round++;
						setOutput(outputURI.lastSegment());
						
//#DEBUG START
						if(_debug) {
							System.out.println("--------------INTERMEDIATE RESULT SAVED--------");
							System.out.println(outputURI.toString());
						}
//#DEBUG END				
					} catch (IOException e) {
						e.printStackTrace();
					}
					unloadModel(resSet, outputURI);
				}
			} // End for loop of TG transformation

			cleanGrammar(henshinTGG);
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
	 * I don't know, why we need this - if we don't have this piece of code, then the mapping from
	 * NODE to TNODE (EDGE to TEDGE, etc.) is not working...
	 */
	private void setEclipsePrefs() {
		try {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("lu.uni.snt.spellflow.gui"); // does all the above behind the scenes
			
			prefs.putInt("CachedQueueEntries",loadQueueTGG.size());

			int idx = 0;
			for (File iFile : loadQueueTGG) {
				prefs.put("CachedLoadQueue_" + idx,iFile.getPath().toString());
				idx++;
			}

			// prefs are automatically flushed during a plugin's "super.stop()".
			prefs.flush();
		} catch(Exception e) {
			//TODO write a real exception handler.
			e.printStackTrace();
		}
	}



	/**
	 * Set class conversions from default elements to TGG elements.
	 * {@link spell_2_spell_flow_button.command.ExecutionJob.undoClassConversions()}
	 */
	private void initClassConversions() {
		if (EMFModelManager.hasClassConversion(HenshinPackage.eINSTANCE, "Node", TggPackage.Literals.TNODE))
			return;
		
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.NODE, TggPackage.Literals.TNODE);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.EDGE, TggPackage.Literals.TEDGE);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.ATTRIBUTE, TggPackage.Literals.TATTRIBUTE);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.RULE, TggPackage.Literals.TGG_RULE);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.GRAPH, TggPackage.Literals.TRIPLE_GRAPH);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,HenshinPackage.Literals.MODULE, TggPackage.Literals.TGG);
		EMFModelManager.registerClassConversion(HenshinPackage.eINSTANCE,null, TggPackage.Literals.IMPORTED_PACKAGE);
	}



	/**
	 * Deletes:
	 * - all edges from correspondence nodes into source and target
	 * - all correspondence nodes (CORR, CORRCMT, CORRCMT2TGT, CORRNL2TGT)
	 * - the source graph
	 * in order to receive a graph containing only the target model.
	 *   
	 * @param graph
	 */
	private void removeNodesAndEdgesFromGraph(EGraph graph) {
		List<EObject> roots = graph.getDomain((EClass)SpellFlowLanguagePackage.eINSTANCE.getEClassifier(SpellFlowLanguagePackage.eINSTANCE.getRoot().getName()), true);
		
		for (EObject root : roots) {
			// Remove all backreferences
			TreeIterator<EObject> nodesIt = root.eAllContents();
			EObject targetObject=root;
			removeCorrEdges(targetObject);
			while(nodesIt.hasNext()){
				targetObject=nodesIt.next();
				removeCorrEdges(targetObject);
			}
		}

		// Delete correspondence nodes from graph
		for (EObject o : graph.getDomain(CorrespondencePackage.eINSTANCE.getCorr(), true)) {
			graph.removeTree(o);
		}
		for (EObject o : graph.getDomain(CorrespondencePackage.eINSTANCE.getCorrcmt(), true)) {
			graph.removeTree(o);
		}
		for (EObject o : graph.getDomain(CorrespondencePackage.eINSTANCE.getCorrCmt2Tgt(), true)) {
			graph.removeTree(o);
		}
		for (EObject o : graph.getDomain(CorrespondencePackage.eINSTANCE.getCorrNL2Tgt(), true)) {
			graph.removeTree(o);
		}

		// TODO: Currently, it only deletes the source graph, if it is typed over SPELL meta model.
		// Delete source model from graph
		for (EObject o : graph.getDomain(SPELLPackage.eINSTANCE.getfile_input(), true)) {
			graph.removeTree(o);
		}
	}

	
	
	/**
	 * Remove correspondence edges from EGraph in iterating over all eCRossReferences.
	 * This method uses {@link spell_2_spell_flow_button.command.ExecutionJob.isCorrEdge(EReference)}
	 * for checking, if an eReference is a correspondence edge.
	 * 
	 * @param targetObject
	 * 
	 * @author frank.hermann, susann.gottmann
	 */
	@SuppressWarnings("rawtypes")
	private void removeCorrEdges(EObject targetObject) {
		EContentsEList.FeatureIterator featureIterator = (EContentsEList.FeatureIterator) targetObject.eCrossReferences().iterator();
		EReference eReference = null;
		EReference t2cEReference = null;
		while (featureIterator.hasNext()) {
			featureIterator.next();
			if (featureIterator.feature() instanceof EReference) {
				eReference = (EReference) featureIterator.feature();
				if (isCorrEdge(eReference))
					t2cEReference = eReference;
			}
		}
		if (t2cEReference != null)
			targetObject.eUnset(t2cEReference);
	}
	
	

	/**
	 * Check if eReference is an edge between a correspondence node and the source model or
	 * an edge between a correspondence node and the target model (i.e., we also check 
	 * eOpposite edges, if they exist). 
	 * Correspondence nodes are: CORR; CORRCMT, CORRCMT2TGT, CORRNL2TGT
	 * 
	 * This method is used in: {@link spell_2_spell_flow_button.command.ExecutionJob.removeCorrEdges(EObject)}
	 * 
	 * @param eReference
	 * @return true, if it is an edge from/to a correspondence node,
	 *         false, otherwise.
	 *         
	 * @author susann.gottmann
	 */
	private Boolean isCorrEdge(EReference eReference) {
		String name = eReference.getName();
		
		// Find names of all possible corr edges
		for (EReference r : CorrespondencePackage.eINSTANCE.getCorr().getEAllReferences()) {
			if(name.equals(r.getName()))
				return true;
			if( (r.getEOpposite() != null) && (name.equals(r.getEOpposite().getName())) )
					return true;
		}
		for (EReference r : CorrespondencePackage.eINSTANCE.getCorrcmt().getEAllReferences()) {
			if(name.equals(r.getName()))
				return true;
			if( (r.getEOpposite() != null) && (name.equals(r.getEOpposite().getName())) )
					return true;
		}
		for (EReference r : CorrespondencePackage.eINSTANCE.getCorrCmt2Tgt().getEAllReferences()) {
			if(name.equals(r.getName()))
				return true;
			if( (r.getEOpposite() != null) && (name.equals(r.getEOpposite().getName())) )
					return true;
		}
		for (EReference r : CorrespondencePackage.eINSTANCE.getCorrNL2Tgt().getEAllReferences()) {
			if(name.equals(r.getName()))
				return true;
			if( (r.getEOpposite() != null) && (name.equals(r.getEOpposite().getName())) )
					return true;
		}
		return false;
	}
	
	

	/**
	 * Takes an IFile as input and loads and parses it in order 
	 * to use it in triple graph transformation.
	 * 
	 * This method is very similar to {@link spell_2_spell_flow_button.command.ExecutionJob.parseSPELLeProc(IFile)}
	 * 
	 * @param ifile
	 * @return List of EObjects representing a triple graph parsed from the file
	 */
	private List<EObject> parseSPELLeProcForTGG(IFile ifile) {
		this.inputURI = URI.createPlatformResourceURI(ifile.getFullPath().toString(), true);
		Resource res = resSet.getResource(inputURI, true);
		List<EObject> inputEObjects = res.getContents();
		return inputEObjects;
	}
	
	
	
	/**
	 * Initialise Henshin and HenshinTGG.
	 */
	private void initHenshin() {
		HenshinPackageImpl.init();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new XMIResourceFactoryImpl());	
	}
	
	
	
	/**
	 * Loads the triple graph grammar which has the same name as indicated in parameter "tgg". 
	 * 
	 * This method is very similar to {@link spell_2_spell_flow_button.command.ExecutionJob.loadGrammar(String)}
	 * 
	 * @param tgg
	 * @throws IOException
	 */
	private void loadTGG(String tgg) throws IOException {
		URL url = FileLocator.toFileURL(Platform.getBundle("SPELL-2-SPELL-Flow-Button").getEntry("/"));
		URI tggFileName = URI.createFileURI(tgg);
		String fileExtension = "henshin";
		URI uri = URI.createFileURI(url.getFile() + tggFileName.appendFileExtension(fileExtension).toFileString());
		Resource res = resSet.getResource(uri, true);
		EList<EObject> modules = res.getContents();
		henshinTGG = (TGG) modules.get(0);
//#DEBUG START			
		if(_debug) {
			System.out.println("Load TGG " + tggFileName + "." + fileExtension + " was successful.");
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
	 * Setter for loadQueueTGG.
	 * 
	 * @param loadQueueTGG
	 */
	public void setTGG(Queue<File> loadQueueTGG) {
		this.loadQueueTGG = loadQueueTGG;
	}

	
	
	/**
	 * 
	 * @param module
	 * @return list of operational rules
	 */
	protected static List<Rule> getOpRules(Module module){
		if (module == null)
			return null;
		String name_OP_RULE_FOLDER = transformationUnitToExecuteTGG;
		IndependentUnit opRuleFolder = (IndependentUnit) module.getUnit(name_OP_RULE_FOLDER);
		List<Rule> opRules = new Vector<Rule>();
		getAllRules(opRules, opRuleFolder);
		
		return opRules;
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
	 * Sort all rules into a list of lists. Why do we need a list of lists? Because we
	 * might have sequential units, where each "block" of the sequential unit have to be 
	 * executed after each other. Any mixture of sequential rules is forbidden!
	 * 
	 * @param module
	 * @return List of list of rules
	 */
	protected static List<List<Rule>> getOpRulesSequential(Module module){
		if (module == null)
			return null;

		List<List<Rule>> opRules = new ArrayList<List<Rule>>();
		
		String name_OP_RULE_FOLDER = transformationUnitToExecuteTGG;
		IndependentUnit opRuleFolder = (IndependentUnit) module.getUnit(name_OP_RULE_FOLDER);
		EList<Unit> subunits = opRuleFolder.getSubUnits();
		
		for (Unit unit : subunits) {
			if(unit instanceof SequentialUnit) {
				for (Unit u : unit.getSubUnits(true)) {
					if(u instanceof MultiUnit) {
						List<Rule> l = new ArrayList<Rule>();
						getAllRules(l,(MultiUnit)u);
						opRules.add(l);
					}
				}
			} 
		}
		
		return opRules;
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

