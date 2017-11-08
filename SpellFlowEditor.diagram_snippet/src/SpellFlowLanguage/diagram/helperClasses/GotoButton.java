package SpellFlowLanguage.diagram.helperClasses;

import java.util.Calendar;
import java.util.Timer;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import SpellFlowLanguage.diagram.edit.parts.GotoActivityEditPart;
import SpellFlowLanguage.diagram.edit.parts.StepActivityEditPart;
import SpellFlowLanguage.diagram.edit.parts.StepActivityEditPart.StepActivityFigure;
import SpellFlowLanguage.diagram.helperClasses.actions.Task_;



/**
 * This class defines the behavior of the goto button (scroll viewport 
 * to the position of the corresponding step node and execute fade 
 * out-animation for highlighting the corresponding Step node).
 * Furthermore, it establishes a link between the underlying GotoEditPart 
 * and the StepActivityEditPart which is the target of this goto button.
 * 
 * @author susann.gottmann
 *
 */
public class GotoButton extends Button {
	
	
	private StepActivityEditPart targetStep = null;
	private String targetStepNumber = "";
	private GotoActivityEditPart correspondingGotoEP = null;
	
	/**
	 * Default constructor
	 */
	public GotoButton() {
		super();
		this.setOpaque(false);
		this.setBorder(null);
		this.setSize(20, 20);
	}

	/**
	 * Constructor with parameter for button text,
	 * and set button to invisible
	 * 
	 * @param text
	 */
	public GotoButton(String text) {
		super(text);
		this.setOpaque(false);
		this.setBorder(null);
		this.setSize(100, 100);
	}

	/**
	 * Setter
	 * 
	 * @param targetStepNumber
	 */
	public void setTargetStepNumber(String targetStepNumber) {
		// if the step number is enclosed in brackets "[ ]", remove them
		if (targetStepNumber.startsWith("[ ") )
			targetStepNumber = targetStepNumber.substring(2);
		if (targetStepNumber.endsWith("]") )
			targetStepNumber = targetStepNumber.substring(0,targetStepNumber.indexOf("]"));
		this.targetStepNumber = targetStepNumber;
	}
	
	/**
	 * Setter
	 * 
	 * @param gotoActivityEditPart
	 */
	public void setGotoActivityEditPart(
			GotoActivityEditPart gotoActivityEditPart) {
		this.correspondingGotoEP = gotoActivityEditPart;
		
	}

	/**
	 * When the button is clicked, the view shall scroll 
	 * to the corresponding StepActivity element.
	 */
	 @Override
	protected void fireActionPerformed() {
		// Find corresponding step
		if(!targetStepNumber.isEmpty()) {
			this.targetStep = findStepEditPart(targetStepNumber);

			// Get upper left corner of the IFigure and scroll to that position
			if(this.targetStep != null) {
				Rectangle r = this.targetStep.getContentPane().getBounds();
				scrollto(r.getTopLeft().x(), r.getTopLeft().y());
			}
		}

		super.fireActionPerformed();
	}

	 /**
	  * Scroll view to the given position.
	  * 
	  * @param x
	  * @param y
	  */
	private void scrollto(int x, int y) {
		IFigure viewport = this.correspondingGotoEP.getFigure().getParent();
		while(!(viewport instanceof FreeformViewport)) {
			viewport = viewport.getParent();
		}
		((FreeformViewport)viewport).setViewLocation(x-100, y-10);	
		// Highlight the target figure.
		doFadeOut();
	}

	/**
	 * Do a fade-out animation for highlighting the target node.
	 */
	private void doFadeOut() {
		StepActivityFigure f = this.targetStep.getPrimaryShape();
		Color oldColor = f.getBackgroundColor();
		Color newColor = new Color(null, new RGB(120,155,255));
		f.setBackgroundColor(newColor);
		
		Timer timer = new Timer();
		Task_ task = new Task_();
		task.b = oldColor;
		task.s = f;
		task.t = timer;
		timer.schedule(task, Calendar.getInstance().getTime(), 10);
	}
	
	/**
	 * Searches for the corresponding StepEditPart with the label containing the given text
	 *
	 * @param targetStepNumber2
	 * @return the target StepActivityEditPart object or null
	 */
	private StepActivityEditPart findStepEditPart(String targetStepNumber2) {

		// Take all children of root and search for the corresponding step
		if (this.correspondingGotoEP != null) {
			// TODO: Currently, parent is RootEditPart -> What happens when we branch?
			for(Object o : this.correspondingGotoEP.getParent().getChildren()) {
				if(o instanceof StepActivityEditPart) {
					String n = ((StepActivityEditPart) o).getStepNumber();
					n = n.substring(0, n.indexOf(":"));
					// Remove spaces, just in case...
					n = n.replace(" ", "");
					targetStepNumber2 = targetStepNumber2.replace(" ", "");
					// The corresponding StepActivitypart is found
					if(n.equals(targetStepNumber2)) {
						//System.out.println("Targetnode found " + o);
						((StepActivityEditPart) o).setCallingGoto(this.correspondingGotoEP);
						return (StepActivityEditPart) o;
					}
				}
			}
		}
		
		return null;
	}

}
