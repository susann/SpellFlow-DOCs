package SpellFlowLanguage.diagram.helperClasses.requests;

import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gmf.runtime.notation.Diagram;

/**
 * OpenDiagramSeletionRequest is derived from SelectionRequest and
 * extended by the boolean value goUp that indicates, if the 
 * request will initiate a "open parent diagram (i.e., go up
 * to the layer above)" or a "open child diagram (i.e., go down
 * to the layer below)". Furthermore, we remember the parent diagram 
 * in the variable "parent".
 * 
 * @author susann
 *
 */
public class OpenDiagramSelectionRequest extends SelectionRequest {
	
	private Boolean goUp = false;

	private Diagram parent = null;

	/**
	 * Default constructor.
	 * 
	 * @param b
	 */
	public OpenDiagramSelectionRequest(boolean b) {
		this.goUp = b;
	}

	/**
	 * Getter
	 * @return
	 */
	public Boolean getGoUp() {
		return goUp;
	}

	/**
	 * Setter
	 * @param goUp
	 */
	public void setGoUp(Boolean goUp) {
		this.goUp = goUp;
	}

	/**
	 * Setter
	 * @param diagramView
	 */
	public void setParent(Diagram diagramView) {
		this.parent = diagramView;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public Diagram getParent() {
		return this.parent;
	}

}
