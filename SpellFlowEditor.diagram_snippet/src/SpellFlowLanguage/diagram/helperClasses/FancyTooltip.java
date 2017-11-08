package SpellFlowLanguage.diagram.helperClasses;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

/**
 * This class defines a tooltip that is used within our EditParts.
 * Currently, the tooltip shows:<br>
 * - the corrsponding source code<br>
 * - the corresponding source code line numbers 
 * 
 * 
 * @author susann.gottmann
 *
 */
public class FancyTooltip extends Figure {

	private String code;
	private String startLine;
	private String endLine;
	
	public FancyTooltip(String commentText, String startLine, String endLine) {
		this.code = commentText;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	/**
	 * Defines the tooltip which can get any IFigure.
	 * 
	 * @return a RectangleFigure which contains all tooltip elements.
	 */
	public RectangleFigure getFancyTooltip() {

		// Main figure (RectangleFigure)
		RectangleFigure rect = new RectangleFigure();
		rect.setSize(200, 70);
		//rect.setPreferredSize(200, 70);
		rect.setBorder(new MarginBorder(3));
		
		// Headline label (in bigger & bold)
		Label l1 = new Label("Corresponding Source Code:");
		l1.setFont(new Font(null, "SansSerif", 10, SWT.BOLD));

		// Source code line numbers (normal font)
		Label l2 = new Label("In Lines: " + startLine + " - " + endLine);

		// Source code snippet, 
		// with blue dashed border, 
		// text is blue and font is courier
		ParseSPELL2SyntaxHighlightingLabel lcode = new ParseSPELL2SyntaxHighlightingLabel(code, 150, 20);

		
		// RectangleFigure needs a layout in order to show children elements
		// We use FlowLayout; parameter false means: no horizontal layout
		rect.setLayoutManager(new FlowLayout(false));
		rect.add(l1);
		rect.add(l2);
		rect.add(lcode);
		
		return rect;
	}

}
