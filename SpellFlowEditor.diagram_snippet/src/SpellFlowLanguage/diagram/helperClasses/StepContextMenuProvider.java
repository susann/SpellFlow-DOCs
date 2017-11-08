package SpellFlowLanguage.diagram.helperClasses;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.IMenuManager;

import SpellFlowLanguage.diagram.edit.parts.GotoActivityEditPart;
import SpellFlowLanguage.diagram.edit.parts.StepActivityEditPart;
import SpellFlowLanguage.diagram.helperClasses.actions.GotoAction;
import SpellFlowLanguage.diagram.helperClasses.actions.GotoReferences;

/**
 * This class builds up the context menu for the Step nodes, i.e.,<br>
 * - if underlying nodes are available, add "go deeper entry";<br>
 * - if Goto buttons are available which have this Step node as target, 
 *   then add entry "GOTO in line [add corresponding line]". Note, each
 *   Step may be the target of more than one Goto.
 * 
 * @author susann.gottmann
 *
 */
public class StepContextMenuProvider extends GoDeeperContextMenuProvider {

	private GotoReferences gotoReferences = null;
	
	public StepContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
		gotoReferences = new GotoReferences();
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		@SuppressWarnings("unchecked")
		List<EditPart> selectedEditParts = this.getViewer().getSelectedEditParts();
		if (selectedEditParts.size() == 1) {
			EditPart editPart = selectedEditParts.get(0);
			if (editPart instanceof StepActivityEditPart) {
				// Clean all entries first
				menu.removeAll();
				gotoReferences.removeAll();
				
				// Find items
				buildUpReferences((StepActivityEditPart) editPart);
				menu.add(gotoReferences);
			}
		}
		
		// Call super in order to build "Go deeper" entry & action
		super.buildContextMenu(menu);
	}


	/**
	 * For each goto with the same label, add an entry
	 * @param editPart
	 */
	private void buildUpReferences(StepActivityEditPart editPart) {
		// Find the "back" goto, if it exists
		GotoActivityEditPart gotoEP = editPart.getCallingGoto();
		if(gotoEP != null) {
			Point p = gotoEP.getLocation();
			String s = "GOTO in line " + gotoEP.getStartLine() + " (back)";

			gotoReferences.add(new GotoAction(s,p, gotoEP));
		}
		
		// Find all other Gotos
		for (Object o : editPart.getParent().getChildren()) {
			if( (o instanceof GotoActivityEditPart) ) {
				if (!((gotoEP != null) && (gotoEP == (GotoActivityEditPart) o) ) ) {
					String gt = ((GotoActivityEditPart) o).getTargetID();
					String st = editPart.getStepNumber();
					st = st.substring(0, st.indexOf(":"));
					// Remove spaces, just in case...
					st = st.replace(" ", "");					
					gt = gt.replace(" ", "");					
					if( st.equals(gt)) {
						Point p = ((GotoActivityEditPart) o).getLocation();
						String s = "GOTO in line " + ((GotoActivityEditPart) o).getStartLine();
						
						gotoReferences.add(new GotoAction(s, p, (GotoActivityEditPart) o));
					}
				}
			}
		}
	}



}
