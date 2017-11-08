package spell_2_spell_flow_button.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import spell_2_spell_flow_button.parser.ModifyXmiFile;
import de.tub.tfs.henshin.tgg.TGG;

/**
 * After clicking button "S2F", the selected files will be translated based on the
 * following steps:<br>
 * <b>(1)</b> all TGGs will be applied (REMARK: currently it's: SPELL2FlowFlat.henshin)<br>
 * <b>(2)</b> all flat graph grammars will be applied (REMARK: currently it's: Refactor_SPELL-Flow.henshin)<br>
 * <b>(3)</b> the Java script will be applied for extending the resulting file with design elements 
 * (REMARK: Maybe later, this last step will be performed using a TGG file, too.)<br>
 * As a result, we get a *.sfld file which can be imported into the Spell-Flow Editor.
 * 
 * @author susann.gottmann
 */
public class TranslateButton extends AbstractHandler implements IHandler {
	/**
	 * Debug flag: if true, then additional printouts on console activated
	 */
	private boolean _debug = true;
	/**
	 * All TGGs that will be applied after each other.
	 */
	protected static List<String> tggFiles = Arrays.asList("SPELL2FlowFlat.henshin");
	/**
	 * All flat graph grammars that will be applied (i.e., refactorings).
	 */
	protected static List<String> flatGrammarFiles = Arrays.asList("Refactor_SPELL-Flow.henshin");
	/** 
	 * List containing full paths to all henshin files (only flat graphs) 
	 * that will be used for the translation.
	 */
	protected static Queue<File> loadQueue = new LinkedList<File>();
	/** 
	 * List containing full paths to all henshin files (only TGG) 
	 * that will be used for the translation.
	 */
	protected static Queue<File> loadQueueTGG = new LinkedList<File>();
	/**
	 * List that contains all files that should be translated.
	 */
	private Queue<IFile> transQueue = new LinkedList<IFile>();
	/**
	 * 
	 */
	private List<String> newInputFiles = new ArrayList<String>();
	/**
	 * 
	 */
	private List<File> inputFilesForHenshin = new ArrayList<File>();
	/**
	 * List of project directories.
	 */
	private ArrayList<File> dirs = new ArrayList<File>();
	
	

	
	// TODO - Was wird gebraucht, was kann weg?
	protected static List<String> trFileNames = new Vector<String>();
	protected static IFile trFile;
	protected static List<TGG> trSystems = new ArrayList<TGG>();
	protected static Job loadGrammarJob = null;

	
	
	/**
	 * Prepare all lists for the execution, i.e., retrieve TGGs first, then the
	 * flat graph grammars that should be applied and retrieve all SPELL files
	 * that are selected by the user for the translation. Finally, start the
	 * translation job.
	 * 
	 * @author susann.gottmann
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// #####################################################################################
		// Pre-Processing
		// #####################################################################################

		// clean up all queues / lists / etc.
		loadQueue.clear();
		loadQueueTGG.clear();
		transQueue.clear();
		trFileNames.clear();
		
		// Get all underlying directories
		refreshDirs();
		
		// The TGG grammars (translations)
		for (String string : tggFiles) {
			for(File f : dirs) {
				File newfile = new File(f + "\\" + string);
				// Check if location contains file
				if(newfile.exists()) {
					loadQueueTGG.add(newfile);
					break;
				}
			}
		}
		// The flat graph grammars (refactorings)
		for (String string : flatGrammarFiles) {
			for(File f : dirs) {
				File newfile = new File(f + "\\" + string);
				// Check if location contains file
				if(newfile.exists()) {
					loadQueue.add(newfile);
					break;
				}
			}
		}

// #DEBUG START
		if(_debug) {
			// Test output
			System.out.println("--------------GRAMMARS-------------------------");
			System.out.println("--------------flat-----------------------------");
			for (File file : loadQueue) {
				System.out.println(file.toString());
			}
			System.out.println("--------------tgg------------------------------");
			for (File file : loadQueueTGG) {
				System.out.println(file.toString());
			}
		}
// #DEBUG END

		// Find files to convert
		transQueue = retrieveFilesForTranslation(event);

// #DEBUG START
		if(_debug) {
			// Test output
			System.out.println("-------------INSTANCES-------------------------");
			for (IFile file : transQueue) {
				System.out.println(file.toString());
			}
		}
// #DEBUG END
		
		// #####################################################################################
		// 1st step: conversion with TGGs 
		// (currently only SPELL2FlowFlat.henshin)
		// #####################################################################################

		for (IFile inputFile: transQueue) {
			ExecutionJobTGG job = new ExecutionJobTGG(inputFile);
			job.setTGG(loadQueueTGG);
			job.schedule();
			
			try {
				job.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Find new input file, i.e., output of job
			String s = job.getOutput();
			
			if(_debug)
				System.out.println("OUTPUT: " + s);
			
			refreshDirs();
			for(File f : dirs) {
				File newfile = new File(f + "\\" + s);
				// Check if location contains file
				if(newfile.exists()) {
					inputFilesForHenshin.add(newfile);
					break;
				}
			}
			
			if(_debug)
				System.out.println("inputFilesForHenshin: " + inputFilesForHenshin);
		}
		
		// #####################################################################################
		// 2nd step: conversion with flat graph grammars
		// (currently only Refactor_SPELL-Flow.henshin)
		// #####################################################################################
		
		for (File inputFile: inputFilesForHenshin) {
			ExecutionJobFlat job = new ExecutionJobFlat(inputFile);
			job.setGrammar(loadQueue);
			job.schedule();
			
			try {
				job.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			newInputFiles.add(job.getOutput());

			if(_debug)
				System.out.println("OUTPUT: " + job.getOutput());
		}
		
		// #####################################################################################
		// 3rd step: enrich with design elements using Brunos Java script.
		// #####################################################################################

		// Copy List<String> into an array String[] in order to use DOMRead.main method.
		int len = newInputFiles.size();
		String[] inputs = new String[len];
		
		refreshDirs();
		for(File f : dirs) {
			for(int i = 0; i < len; i++) {
				File newfile = new File(f + "\\" + newInputFiles.get(i));
				// Check if location contains file
				if(newfile.exists()) {
					inputs[i] = newfile.toString();
					break;
				}
    		}
		}
		
		// Brunos Java code for enrichment of target file
		ModifyXmiFile.main(inputs);
		System.out.println("--------------Enrichment finished!-------------");
		
		return null;
	}
	
	

	/**
	 * Gets all underlying directories of the current workspace projects.
	 */
	private void refreshDirs() {
		// Clear list
		dirs.clear();
		
		// Get object which represents the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		for (IProject project : workspace.getRoot().getProjects()) {
			File[] d = new File(project.getLocationURI()).listFiles(File::isDirectory);
			for (File file : d) {
				dirs.add(file);
				dirs.addAll(getMoreDirs(file));
			}
		}
	}


	private List<File> getMoreDirs(File file) {
		ArrayList<File> l = new ArrayList<File>();
		File[] d = file.listFiles(File::isDirectory);
		for (File f : d) {
			l.add(f);
			l.addAll(getMoreDirs(f));
		}
		return l;
	}



	/**
	 * Taken from {@link de.tub.tfs.henshin.tgg.interpreter.gui/TransHandler.java}
	 * But slightly modified.
	 * 
	 * It gets the current selection and adds the corresponding files to
	 * the transQueue list.
	 * 
	 * @author frank.hermann, susann.gottmann
	 * 
	 * @param event
	 * @return transQueue is filled with all selected objects
	 */
	@SuppressWarnings("unchecked")
	protected Queue<IFile> retrieveFilesForTranslation(ExecutionEvent event) {
		// Find files to translate:
		Queue<IFile> transQueue = new LinkedList<IFile>();
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel != null && sel instanceof IStructuredSelection) {
			IStructuredSelection structSel = (IStructuredSelection) sel;
			for (Iterator<Object> it = structSel.iterator(); it.hasNext();) {
				Object obj = it.next();
				if (obj instanceof IFile) {
					IFile file = (IFile) obj;
					transQueue.add(file);
				}
				if (obj instanceof IContainer) {
					IResource[] resArr;
					try {
						resArr = ((IContainer) obj).members();
						for (int i=0; i<resArr.length; i++) {
							if (resArr[i] instanceof IFile) {
								IFile file = (IFile) resArr[i];
								transQueue.add(file);
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return transQueue;
	}
}


/* OLD using ExecutionJob instead of ExecutionJobTGG and ExecutionJobFlat
// Start translation job for each input file
for (IFile inputFile: transQueue) {
	ExecutionJob job = new ExecutionJob(inputFile);
	job.setTGG(loadQueueTGG);
	job.setGrammar(loadQueue);
	job.schedule();
	
	try {
		job.join();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	newInputFiles.add(job.getOutput());
}
 */



