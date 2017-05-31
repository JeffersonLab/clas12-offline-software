package cnuphys.bCNU.graphics.toolbar;

/**
 * The center button recenters the view on a given location.
 */
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;

/**
 * @author heddle
 */

@SuppressWarnings("serial")
public class CenterButton extends ToolBarToggleButton {

	/**
	 * Create the button used for recentering a container.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public CenterButton(IContainer container) {
		super(container, "images/center.gif", "recenter the view");
		customCursorImageFile = "images/centercursor.png";
	}

	/**
	 * The container has been clicked with this as the active button.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		container.prepareToZoom();
		container.recenter(mouseEvent.getPoint());
	}
}
