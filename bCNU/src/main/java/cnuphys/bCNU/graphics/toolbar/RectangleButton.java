package cnuphys.bCNU.graphics.toolbar;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;

@SuppressWarnings("serial")
public class RectangleButton extends ToolBarToggleButton implements
		IRubberbanded {

	/**
	 * Create a button for creating rectangles by rubberbanding.
	 * 
	 * @param container
	 *            the container using this button.
	 */
	public RectangleButton(IContainer container) {
		super(container, "images/rectangle.gif", "Create a rectangle");
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
			rubberband = new Rubberband(container, this,
					Rubberband.Policy.RECTANGLE);
			rubberband.setActive(true);
			rubberband.startRubberbanding(mouseEvent.getPoint());
		}
	}

	/**
	 * Notification that rubber banding is finished.
	 */
	@Override
	public void doneRubberbanding() {
		Rectangle b = rubberband.getRubberbandBounds();
		rubberband = null;
		// create a rectangle item

		if ((b.width < 3) || (b.height < 3)) {
			return;
		}
		AItem item = container.createRectangleItem(
				container.getAnnotationLayer(), b);
		if (item != null) {
			item.setRightClickable(true);
			item.setDraggable(true);
			item.setRotatable(true);
			item.setResizable(true);
			item.setDeletable(true);
			item.setLocked(false);
		}

		container.selectAllItems(false);
		container.getToolBar().resetDefaultSelection();
		container.refresh();
	}
}
