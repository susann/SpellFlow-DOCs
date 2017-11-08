package SpellFlowLanguage.diagram.helperClasses.actions;

import java.util.Calendar;
import java.util.Timer;

import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;

import SpellFlowLanguage.diagram.edit.parts.GotoActivityEditPart;
import SpellFlowLanguage.diagram.edit.parts.GotoActivityEditPart.GotoActivityFigure;

/**
 * Defines the behaviour when clicking the goto element:<br>
 * - scroll viewport to the corresponding target element of 
 *   this goto element and<br>
 * - show a fade out-animation in order to highlight the target
 *   node.
 * 
 * @author susann.gottmann
 *
 */
public class GotoAction implements IAction {

	private Point target;
	private String text;
	private boolean enabled = true;
	private GotoActivityEditPart gotoEP;
	
	
	/**
	 * Constructor. Disables newly created Action.
	 */
	public GotoAction() {
		// Do not call this constructor. It's useless.
		setEnabled(false);
	}

	/**
	 * Better to use this constructor.
	 * 
	 * @param text
	 * @param p
	 */
	public GotoAction(String text, Point p, GotoActivityEditPart g) {
		this.target = p;
		this.text = text;
		this.gotoEP = g;
	}

	
	
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAccelerator() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getActionDefinitionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HelpListener getHelpListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMenuCreator getMenuCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStyle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChecked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * If context menu entry clicked, scroll viewport to the 
	 * corresponding shape using the method scrollto.
	 */
	@Override
	public void run() {
		//System.out.println("run " + this.getText());
		//System.out.println(this.target.toString());
		scrollto(this.target.x, this.target.y);
	}

	 /**
	  * Scroll view to the given position.
	  * 
	  * @param x
	  * @param y
	  */
	private void scrollto(int x, int y) {
		IFigure viewport = this.gotoEP.getFigure().getParent(); 
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
		GotoActivityFigure f = this.gotoEP.getPrimaryShape();
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
	
	@Override
	public void runWithEvent(Event event) {
		this.run();
	}

	@Override
	public void setActionDefinitionId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChecked(boolean checked) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled  = enabled;
	}

	@Override
	public void setHelpListener(HelpListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHoverImageDescriptor(ImageDescriptor newImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setImageDescriptor(ImageDescriptor newImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMenuCreator(IMenuCreator creator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAccelerator(int keycode) {
		// TODO Auto-generated method stub

	}

}
