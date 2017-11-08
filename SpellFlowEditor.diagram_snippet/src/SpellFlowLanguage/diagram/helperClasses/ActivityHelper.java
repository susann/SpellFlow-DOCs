package SpellFlowLanguage.diagram.helperClasses;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;

import SpellFlowLanguage.Activity;

/**
 * Each EditPart which will get a tooltip showing the corresponding 
 * source code & source code line numbers will use this class for 
 * fetching all information for the tooltip.
 * 
 * @author susann.gottmann
 */
public class ActivityHelper extends CompartmentEditPart {

	/**
	 * Constructor.
	 * @param model
	 */
	public ActivityHelper(EObject model) {
		super(model);
	}

	/**
	 * Taken from OtherActivityDescriptionEditPart
	 */
	protected EObject getParserElement() {
		return resolveSemanticElement();
	}
	
	/**
	 * NOT GENERATED
	 * 
	 * @author susann.gottmann
	 * @return null or comment + description
	 */
	public String getCommentText() {
		EObject parserElement = getParserElement();
		if (parserElement instanceof Activity) {
			return ((Activity)parserElement).getSourceCode();
			
			/*
			// Both are empty -> return null
			if ( (((Activity)parserElement).getComments() == null) && (((Activity)parserElement).getDescription() == null) )
				return null;
			
			
			// description is empty -> return only comments
			if ( ((Activity)parserElement).getDescription() == null) 
				return ((Activity)parserElement).getComments();
			
			// comments are empty -> return only description
			if ( ((Activity)parserElement).getComments() == null) 
				return ((Activity)parserElement).getDescription();

			// else -> return both
			return ((Activity)parserElement).getComments() + "\n" + ((Activity)parserElement).getDescription();
		*/
		}
		
		// It is no Activity -> return null
		return null;
	}
	
	/**
	 * NOT GENERATED
	 * 
	 * @author susann.gottmann
	 * @return
	 */
	public String getStartLine() {
		EObject parserElement = getParserElement();
		if (parserElement instanceof Activity) {
			return Integer.toString(((Activity)parserElement).getStartLineNumber());
		}
		return null;
	}
	/**
	 * NOT GENERATED
	 * 
	 * @author susann.gottmann
	 * @return
	 */
	public String getEndLine() {
		EObject parserElement = getParserElement();
		if (parserElement instanceof Activity) {
			return Integer.toString(((Activity)parserElement).getEndLineNumber());
		}
		return null;
	}
	
}
