package SpellFlowLanguage.diagram.helperClasses.edit.parts;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

//import SpellFlowLanguage.diagram.helperClasses.actions.HistoryNavigator;

/**
 * Class in between EditPart and ShapeNodeEditPart which defines new sub-figures
 * as anchor.
 * 
 * @author susann.gottmann
 *
 */
public class ChangedAnchorEditPart extends ShapeNodeEditPart {

	/**
	 * Default constructor.
	 * @param view
	 */
	public ChangedAnchorEditPart(View view) {
		super(view);
	}

	/**
	 * Anchor is set to small circle on top of each figure.
	 * 
	 * @author susann.gottmann
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		for(Object o : this.getFigure().getChildren() )  {
			if(o instanceof Shape) {
				for(Object o1 : ((Shape) o).getChildren()) {
					if(o1 instanceof Ellipse) {
						// return small ellipse as anchor
						return new SlidableAnchor((IFigure) o1);
					}
				}
			}
		}
		// ellipse not found, return main figure as anchor
		return new SlidableAnchor(this.getFigure());
	}

	/**
	 * Anchor is set to small circle on top of each figure
	 * 
	 * @author susann.gottmann
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
//		Rectangle r = this.getFigure().getBounds();
//		PrecisionPoint p = new PrecisionPoint(r.getCenter().x, r.getTop().y); 
//		return new SlidableAnchor(this.getFigure(), p);
		for(Object o : this.getFigure().getChildren() )  {
			if(o instanceof Shape) {
				for(Object o1 : ((Shape) o).getChildren()) {
					if(o1 instanceof Ellipse) {
						// return small ellipse as anchor
						return new SlidableAnchor((IFigure) o1);
					}
				}
			}
		}
		// ellipse not found, return main figure as anchor
		return new SlidableAnchor(this.getFigure());
	}
	
	
	/**
	 * Anchor is set to last child shape.
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		for(Object o : this.getFigure().getChildren() )  {
			if(o instanceof Shape) {
				@SuppressWarnings("unchecked")
				List<Object> children = ((Shape) o).getChildren();
				// The last child is usually the shape where the source anchor
				// should be attached to.
				Object lastChild = children.get(children.size()-1);
				if (lastChild instanceof Shape) {
					return new SlidableAnchor((IFigure) lastChild);
				}
				/*
				for(Object o1 : ((Shape) o).getChildren().get()) {
					if(!(o1 instanceof Ellipse) ) {
						if( (o1 instanceof Shape) && ((Shape) o1).getPreferredSize().width > 2) { 
							// return shape which is not the ellipse and 
							// also not the tiny line as anchor
							return new SlidableAnchor((IFigure) o1);
						}
					}
				}
				*/
			}
		}
		// Something went wrong: return main figure as anchor
		// This should not happen.
		return new SlidableAnchor(this.getFigure());
	}

	/**
	 * Anchor is set to last child shape.
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		for(Object o : this.getFigure().getChildren() )  {
			if(o instanceof Shape) {
				@SuppressWarnings("unchecked")
				List<Object> children = ((Shape) o).getChildren();
				// The last child is usually the shape where the source anchor
				// should be attached to.
				Object lastChild = children.get(children.size()-1);
				if (lastChild instanceof Shape) {
					return new SlidableAnchor((IFigure) lastChild);
				}
				/*
				for(Object o1 : ((Shape) o).getChildren()) {
					if(!(o1 instanceof Ellipse) ) {
						if( (o1 instanceof Shape) && (((Shape) o1).getPreferredSize().width > 2) ) {
							// return shape which is not the ellipse and 
							// also not the tiny line as anchor
							return new SlidableAnchor((IFigure) o1);
						}
					}
				}
				*/
			}
		}
		// Something went wrong: return main figure as anchor
		// This should not happen.
		return new SlidableAnchor(this.getFigure());
	}

	/**
	 * This method must be defined here. Anyway, 
	 * it will be overridden by derived class(es).
	 * 
	 * @return null
	 */
	@Override
	protected NodeFigure createNodeFigure() {
		return null;
	}

	/**
	 * TODO Nida: 
	 *      Change this request, so that the underlying 
	 *      layer will open in the same tab.
	 *      
	 *      Currently, it just calls the method from 
	 *      GraphicalEditPart.
	 */
	@Override
	public void performRequest(Request request) {
		// TODO Auto-generated method stub
		
		
        
	
		super.performRequest(request);
	}
}
