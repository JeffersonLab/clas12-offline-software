package cnuphys.bCNU.graphics.toolbar;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;

@SuppressWarnings("serial")
public class EllipseButton extends ToolBarToggleButton implements IRubberbanded {

	/**
	 * Create a button for creating ellipses by rubberbanding.
	 * 
	 * @param container
	 *            the container using this button.
	 */
	public EllipseButton(IContainer container) {
		super(container, "images/ellipse.gif", "Create an ellipse");
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if (rubberband == null) {
			rubberband = new Rubberband(container, this, Rubberband.Policy.OVAL);
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
		// create an ellipse item

		if ((b.width < 3) || (b.height < 3)) {
			return;
		}
		AItem item = container.createEllipseItem(
				container.getAnnotationLayer(), b);
		if (item != null) {
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
