package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * Zoom out by a fixed amount.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ZoomOutButton extends ToolBarButton {

	/**
	 * Create the zoom-out button, which zooms out by a fixed amount.
	 * 
	 * @param container
	 *            the container this button acts upon.
	 */
	public ZoomOutButton(IContainer container) {
		super(container, "images/zoom_out.gif", "Zoom out");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            the causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.scale(1.0 / IContainer.FIXED_ZOOM_FACTOR);
		container.getToolBar().resetDefaultSelection();
	}
}
