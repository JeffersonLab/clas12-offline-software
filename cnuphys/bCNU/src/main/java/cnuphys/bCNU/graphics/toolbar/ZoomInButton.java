package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.ActionEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * A a zoom-in button.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ZoomInButton extends ToolBarButton {

	/**
	 * Create the zoom-in button, which zooms in by a fixed amount.
	 * 
	 * @param container
	 *            the container this button acts upon.
	 */
	public ZoomInButton(IContainer container) {
		super(container, "images/zoom_in.gif", "Zoom in");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param e
	 *            The causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		container.scale(IContainer.FIXED_ZOOM_FACTOR);
		container.getToolBar().resetDefaultSelection();
	}
}
