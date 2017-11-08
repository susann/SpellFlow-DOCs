package SpellFlowLanguage.diagram.helperClasses;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import SpellFlowLanguage.diagram.helperClasses.actions.GoDeeperAction;
import SpellFlowLanguage.impl.ElementImpl;

/**
 * This class sets the context menu entry "go deeper", according to the following rules:<br>
 * - Is the element the mouse points to, when the right mouse button was clicked an
 *   EditPart?<br>
 * - Is the corresponding model node of this EditPart derived from Element (in SpellFlowLanguge)?<br>
 * - Does this model element has any children, i.e., does method getTo_Element() returns a 
 *   non-empty list? <br>
 * -> If all these conditions hold, then show the "go deeper" entry in the context menu and 
 *    set the underlying GoDeeperAction which processes the click on the "go deeper" entry.
 * 
 * @author susann.gottmann
 *
 */

public class GoDeeperContextMenuProvider extends GoUpContextMenuProvider {

	GraphicalViewer graphicalViewer;
	IDiagramWorkbenchPart part;
	
	// -----------------------------------------------------------------------
	
	public GoDeeperContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
	}

	public void setGraphicalViewer(GraphicalViewer g) {
		this.graphicalViewer = g;
	}

	public void setIDiagramWorkbenchPart(IDiagramWorkbenchPart p) {
		this.part = p;
	}

	/**
	 * Build context menu, i.e., if the underlying EditPart is a StepActivityEditPart and
	 * if the corresponding StepActivity has children elements, then enable the "Go deeper"
	 * action.
	 * 
	 * @author susann
	 */
	@Override
	public void buildContextMenu(IMenuManager menu) {
		@SuppressWarnings("unchecked")
		// Get the selection, i.e., the element that is below the mouse pointer
		// after the left key of the mouse was clicked.
		List<EditPart> selectedEditParts = this.getViewer().getSelectedEditParts();
		if (selectedEditParts.size() == 1) {
			EditPart editPart = selectedEditParts.get(0);
			// Checks, if the model element (StepActivity) that
			// corresponds to this StepActivityEditPart has
			// children: If yes, show the "go deeper" entry in 
			// the context menu; if not: don't show this context
			// menu entry. 
			Boolean isNotEmpty = true;
			Object o = null;
			// getModel() might return ShapeImpl or NodeImpl
			if (editPart.getModel() instanceof ShapeImpl) {
				o = ((ShapeImpl)editPart.getModel()).getElement();
			}
			if (editPart.getModel() instanceof NodeImpl) {
				o = ((NodeImpl)editPart.getModel()).getElement();
			}
			// Check for any Element of SpellFlowModel 
			// (ElementImpl has the relevant method getTo_Element)
			if ((o != null) && (o instanceof ElementImpl)) {
				if ( ((ElementImpl)o).getTo_Element().isEmpty() ) {
					isNotEmpty = false;
				}
			}
			
 
			// Now, show "go deeper" entry, if prevoius ifs found out
			// that children are available.
			if(isNotEmpty) {
				
				
	
				GoDeeperAction gda = new GoDeeperAction();
				gda.setEditPart(editPart);
				gda.setGraphicalViewer(this.graphicalViewer);
				gda.setIDiagramWorkbenchPart(this.part);
				menu.add(gda);
			}
		}
		// Also call buildContextMenu from GoUpContextMenuProvider
		super.buildContextMenu(menu);
	}
	


}