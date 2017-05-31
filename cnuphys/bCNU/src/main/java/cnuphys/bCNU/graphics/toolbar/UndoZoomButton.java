package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class UndoZoomButton extends ToolBarButton {

	/**
	 * Create the button used for undoing the previous zoom.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public UndoZoomButton(IContainer container) {
		super(container, "images/undo_zoom.gif", "Undo previous zoom");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.undoLastZoom();
		container.getToolBar().resetDefaultSelection();
	}
}
