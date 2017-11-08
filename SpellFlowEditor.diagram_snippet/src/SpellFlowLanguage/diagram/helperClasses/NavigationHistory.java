package SpellFlowLanguage.diagram.helperClasses;

import java.util.ArrayList;

import org.eclipse.ui.part.FileEditorInput;

/**
 * Class contains list of navigation history entries and 
 * operations for it.<br><br>
 * 
 * It is used in classes SpellFlowModelDiagramEditor and
 * OpenDiagramEditPolicy.
 * 
 * @author susann
 *
 */
public class NavigationHistory {
	/**
	 * The list that shall contain all navigation history entries.
	 */
	private ArrayList<FileEditorInput> history = null;
	
	/**
	 * Default constructor.
	 */
	public NavigationHistory() {
		this.history = new ArrayList<FileEditorInput>();
	}
	
	/**
	 * Appends the given element to the list.
	 * @param element
	 */
	public void addElementToHistory(FileEditorInput element) {
		this.history.add(element);
	}

	/**
	 * Returns the last element of the list.
	 * @return null, if empty, otherwise returns the last element.
	 */
	public FileEditorInput getLastElementFromHistory() {
		if (this.history.isEmpty()) {
			return null;
		}
		
		int lastElementIndex = this.history.size() - 1;
		FileEditorInput d = this.history.get(lastElementIndex);
		this.history.remove(lastElementIndex);
		
		return d;
	}
	
	/**
	 * Returns if the list is empty, or not.
	 * @return true or false
	 */
	public Boolean isEmpty() {
		return this.history.isEmpty();
	}
	
	/**
	 * Returns the content of the list.
	 * @return
	 */
	public ArrayList<FileEditorInput> getAllElements() {
		return this.history;
	}
	
}
