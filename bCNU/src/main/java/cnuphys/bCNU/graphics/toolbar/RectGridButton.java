package cnuphys.bCNU.graphics.toolbar;

import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;

@SuppressWarnings("serial")
public class RectGridButton extends ToolBarToggleButton implements
		IRubberbanded {

	/**
	 * Create a button for creating rectangles by rubberbanding.
	 * 
	 * @param container
	 *            the container using this button.
	 */
	public RectGridButton(IContainer container) {
		super(container, "images/rectgrid.gif", "Create a rectangular grid");
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
//		Rectangle b = rubberband.getRubberbandBounds();
//		rubberband = null;
//		// create a rectangle item
//
//		if ((b.width < 3) || (b.height < 3)) {
//			return;
//		}
//		AItem item = container.createRectangleItem(
//				container.getAnnotationLayer(), b);
//		if (item != null) {
//			item.setRightClickable(true);
//			item.setDraggable(true);
//			item.setRotatable(true);
//			item.setResizable(true);
//			item.setDeletable(true);
//			item.setLocked(false);
//		}
//
//		container.selectAllItems(false);
		container.getToolBar().resetDefaultSelection();
		container.refresh();
	}
}