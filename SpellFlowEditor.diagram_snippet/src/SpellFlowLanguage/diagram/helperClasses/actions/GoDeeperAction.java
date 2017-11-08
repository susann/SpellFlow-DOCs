package SpellFlowLanguage.diagram.helperClasses.actions;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import SpellFlowLanguage.diagram.edit.parts.StepActivityEditPart;

/**
 * This class performs the "go deeper" action, i.e., it opens the diagram 
 * containing all children of the editPart of interest. This EditPart element
 * is stored in the variable editPart.<br><br>
 * 
 * This class if inspired by OpenDiagramAction in SpellFlowModelNavigatorActionProvider 
 * (from package: SpellFlowLanguage.diagram.navigator).
 * 
 * @author susann.gottmann
 */
public class GoDeeperAction extends Action {

	private EditPart editPart;
	
	IWorkbenchPage page;

	GraphicalViewer graphicalViewer;

	IDiagramWorkbenchPart part;
	
	// -----------------------------------------------------------------------
	
	@Override
	public String getDescription() {
		return "Go to underlying layer";
	}

	@Override
	public String getText() {
		return "Go deeper";
	}

	@Override
	public String getToolTipText() {
		return "Go to underlying layer";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Open underlying layer.
	 * Currently it's only working for StepActivityEditParts.
	 * TODO Susann: 
	 *      Activate this also for other (relevant!)
	 *      EditParts, e.g., IfCondition, OtherActivity, etc.
	 */
	@Override
	public void run() {
		// Creates and executes a request for opening the underlying layer.
		SelectionRequest request = new SelectionRequest();
		if(this.editPart instanceof StepActivityEditPart) {
			request.setLocation(((StepActivityEditPart) this.editPart).getLocation());
		}
		request.setType(RequestConstants.REQ_OPEN);
		
		this.editPart.performRequest(request);
		
		// TODO Nida: 
		// Open the whole new layer in the same tab, not in a new tab.
		 //<- reusing, not opening new
		//page.openEditor(editorInput, getEditorID());
		
		
		
		
		
		
		// CODE FROM OTHER CLASSES in SpellFlowLanguage.diagram package
		// THAT MIGHT HELP:

		
/*
		EditPart rootEditPart;
		Element newRoot;
		
		Object element = ((ShapeImpl)this.editPart.getModel()).getElement();
		System.out.println(element);
		if(element instanceof StepActivityImpl) {
			for(Element e : ((StepActivityImpl) element).getTo_Element()) {
				if (isFirst(e, (StepActivityImpl) element)) {
					newRoot = e;
					//rootEditPart = (EditPart) this.graphicalViewer.getEditPartRegistry().entrySet().toArray()[0];
					
				}
				// Or we find a new equals-solution= -> check attributes...
 			}
		}
		
	*/	
		/*
		 * 
		 * 
		 * 			if (myDiagram == null || myDiagram.eResource() == null) {
				return;
			}

			IEditorInput editorInput = getEditorInput(myDiagram);
			IWorkbenchPage page = myViewerSite.getPage();
			try {
				page.openEditor(
						editorInput,
						SpellFlowLanguage.diagram.part.SpellFlowModelDiagramEditor.ID);
			} catch (PartInitException e) {
				SpellFlowLanguage.diagram.part.SpellFlowModelDiagramEditorPlugin
						.getInstance().logError(
								"Exception while openning diagram", e); //$NON-NLS-1$
			}



		private static IEditorInput getEditorInput(Diagram diagram) {
			Resource diagramResource = diagram.eResource();
			for (EObject nextEObject : diagramResource.getContents()) {
				if (nextEObject == diagram) {
					return new FileEditorInput(
							WorkspaceSynchronizer.getFile(diagramResource));
				}
				if (nextEObject instanceof Diagram) {
					break;
				}
			}
			URI uri = EcoreUtil.getURI(diagram);
			String editorName = uri.lastSegment() + '#'
					+ diagram.eResource().getContents().indexOf(diagram);
			IEditorInput editorInput = new URIEditorInput(uri, editorName);
			return editorInput;
		}


					Diagram diagram = (Diagram) selectedElement;
					if (SpellFlowLanguage.diagram.edit.parts.RootEditPart.MODEL_ID
							.equals(SpellFlowLanguage.diagram.part.SpellFlowModelVisualIDRegistry
									.getModelID(diagram))) {
						myDiagram = diagram;
					}
		 */
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
		//System.out.println(event);
		this.run();
	}

	/**
	 * Setter for GraphicalViewer
	 * Attention: this.graphicalViewer is unused.
	 * 
	 * @author susann
	 * @param g
	 */
	public void setGraphicalViewer(GraphicalViewer g) {
		this.graphicalViewer = g;
	}

	/**
	 * Setter for IDiagramWorkbenchPart
	 * Attention: this.part is unused.
	 * 
	 * @author susann
	 * @param p
	 */
	public void setIDiagramWorkbenchPart(IDiagramWorkbenchPart p) {
		this.part = p;
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
