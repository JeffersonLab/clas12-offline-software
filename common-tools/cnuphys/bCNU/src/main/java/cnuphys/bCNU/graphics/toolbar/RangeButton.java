package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.YouAreHereItem;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class RangeButton extends ToolBarToggleButton {

	/**
	 * Create the button for placing down a reference point (anchor).
	 * 
	 * @param container
	 *            the owner container.
	 */
	public RangeButton(IContainer container) {
		super(container, "images/range.gif", "Range");
	}

	/**
	 * Handle a mouse clicked (into the map canvas) event (if this tool is
	 * active)
	 * 
	 * @param e
	 *            the MouseEvent
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point2D.Double wp = new Point2D.Double();
		container.localToWorld(e.getPoint(), wp);

		YouAreHereItem item = container.getYouAreHereItem();
		if (item != null) {
			item.setFocus(wp);
		} else {
			YouAreHereItem.createYouAreHereItem(container, wp);
		}

		container.getToolBar().resetDefaultSelection();
		container.refresh();
	}

}
