package SpellFlowLanguage.diagram.helperClasses.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

//import SpellFlowLanguage.diagram.helperClasses.CheckParent;
import SpellFlowLanguage.diagram.helperClasses.edit.parts.ChangedAnchorEditPart;

/**
 * This class performs the "go up" action, i.e., it opens the diagram 
 * containing overlying layer of the editPart of interest. This EditPart element
 * is stored in the variable editPart. This action is created in the 
 * GoUpContextMenuProvider class.<br><br>
 * 
 * This class is copied from GoDeeperAction.
 * 
 * @author susann.gottmann
 */
public class GoUpAction extends Action {

	private EditPart editPart;
	
	// -----------------------------------------------------------------------
	
	@Override
	public String getDescription() {
		return "Go to overlying layer";
	}

	@Override
	public String getText() {
		return "Go up";
	}

	@Override
	public String getToolTipText() {
		return "Go to overlying layer";
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	/**
	 * Open overlying layer.
	 * 
	 * TODO Nida: 
	 *      Implement the performRequest method also for this "go up" 
	 *      action.
	 *      Add history / navigation here
	 *      Open the new graph in the same tab.
	 */
	@Override
	public void run() {
		// Creates and executes a request for opening the underlying layer.
		SelectionRequest request = new SelectionRequest();
		if(this.editPart instanceof ChangedAnchorEditPart) {
			request.setLocation(((ChangedAnchorEditPart) this.editPart).getLocation());
		}
		request.setType(RequestConstants.REQ_OPEN);
		this.editPart.performRequest(request);
		// TODO Nida
				//opening the overlying layer
				//author @Nida
		
		// TODO Nida
	}

	/**
	 * We do not want to check and handle the event, 
	 * because we always run it in the same way.
	 * -> So just call this.run() for that.
	 * 
	 * @author susann
	 */
	@Override
	public void runWithEvent(Event event) {
		this.run();
	}

	/**
	 * Setter for the corresponding EditPart. It is used in this.run() method.
	 *  
	 * @author susann
	 * @param e
	 */
	public void setEditPart(EditPart e) {
		this.editPart = e;
	}
	
}
