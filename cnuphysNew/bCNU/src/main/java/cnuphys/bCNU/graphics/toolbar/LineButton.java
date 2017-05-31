/**
 * 
 */
package cnuphys.bCNU.graphics.toolbar;

import java.awt.Point;
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;

@SuppressWarnings("serial")
public class LineButton extends ToolBarToggleButton implements IRubberbanded {

	/**
	 * Create a button for creating rectangles by rubberbanding.
	 * 
	 * @param container
	 *            the container using this button.
	 */
	public LineButton(IContainer container) {
		super(container, "images/line.gif", "Create a line");
	}

	/**
	 * The mouse has been pressed, start rubber banding.
	 * 
	 * @param mouseEvent
	 *            the causal mouse event.
	 */
	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if (rubberband == null) {
			rubberband = new Rubberband(container, this, Rubberband.Policy.LINE);
			rubberband.setActive(true);
			rubberband.startRubberbanding(mouseEvent.getPoint());
		}
	}

	/**
	 * Notification that rubber banding is finished.
	 */
	@Override
	public void doneRubberbanding() {
		Point p0 = rubberband.getStartPt();
		Point p1 = rubberband.getCurrentPt();
		rubberband = null;

		// create a line item

		if ((Math.abs(p0.x - p1.x) > 2) || (Math.abs(p0.y - p1.y) > 2)) {
			AItem item = container.createLineItem(
					container.getAnnotationLayer(), p0, p1);
			if (item != null) {
				item.setRightClickable(true);
				item.setDraggable(true);
				item.setRotatable(false);
				item.setResizable(true);
				item.setDeletable(true);
				item.setLocked(false);
			}
		}

		container.selectAllItems(false);
		container.getToolBar().resetDefaultSelection();
		container.refresh();
	}
}
