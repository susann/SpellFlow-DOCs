package SpellFlowLanguage.diagram.helperClasses;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import SpellFlowLanguage.diagram.helperClasses.actions.GoUpAction;
import SpellFlowLanguage.impl.ElementImpl;

/**
 * This class sets the context menu entry "go up". It's copied from the class
 * GoDeeperContextMenuProvider.

 *
 */
public class GoUpContextMenuProvider extends ContextMenuProvider {

	GraphicalViewer graphicalViewer;
	IDiagramWorkbenchPart part;
	
	// -----------------------------------------------------------------------
	
	public GoUpContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
	}

	public void setGraphicalViewer(GraphicalViewer g) {
		this.graphicalViewer = g;
	}

	public void setIDiagramWorkbenchPart(IDiagramWorkbenchPart p) {
		this.part = p;
	}
	
	/**
	 * Build context menu. 

	 * 
	 * 
	 * 
	 *Shows entry only, if we are in a sub-layer, not in the main layer.
	 *
	 */
	 
	@Override
	public void buildContextMenu(IMenuManager menu){
		@SuppressWarnings("unchecked")
		// Gets the selection, i.e., the element that is below the mouse pointer
		// after the left key of the mouse was clicked. If it is an EditPart,
		// then shows "go up". 
		
		List<EditPart> selectedEditParts = this.getViewer().getSelectedEditParts();
		if (selectedEditParts.size() == 1) {
			EditPart editPart = selectedEditParts.get(0);
			Boolean isNotEmpty = false;
		   
			Object o = null;
			// getModel() might return ShapeImpl or NodeImpl
			if (editPart.getModel() instanceof ShapeImpl) {
				o = ((ShapeImpl)editPart.getModel()).getElement();
			}
			if (editPart.getModel() instanceof NodeImpl) {
				o = ((NodeImpl)editPart.getModel()).getElement();
			}
			// Checks for any Element of SpellFlowModel 
			// (ElementImpl has the relevant method getTo_Element)
			if ((o != null) && (o instanceof ElementImpl)) {
				if ( ((ElementImpl)o).getTo_Element().isEmpty() ) {
					isNotEmpty = true;
				}
			}
	       
			// shows "go up" entry, if previous ifs found out
	    	// that a parent is available.
        	 
	        if(isNotEmpty) {	
		    GoUpAction gua = new GoUpAction();
			gua.setEditPart(editPart);
			menu.add(gua);
			
	    	// calls buildContextMenu from GoUpContextMenuProvider
			
			//super.buildContextMenu(menu);
			//super.add(gua);
          // menu.setVisible(true);
			//createContextMenu(null);
	        }
	     
		}
			
	}

	private void showContextMenu(IMenuManager menu) {
		// TODO Auto-generated method stub
		
	}
	
}
