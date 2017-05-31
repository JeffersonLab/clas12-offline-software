package cnuphys.bCNU.graphics.toolbar;

import java.awt.Point;
import java.awt.event.MouseEvent;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.rubberband.IRubberbanded;
import cnuphys.bCNU.graphics.rubberband.Rubberband;
import cnuphys.bCNU.item.AItem;

/**
 * @author heddle
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("serial")
public class PolygonButton extends ToolBarToggleButton implements IRubberbanded {

	/**
	 * Create a button for creating a polygon.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public PolygonButton(IContainer container) {
		super(container, "images/polygon.gif", "Create a polygon");
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
					Rubberband.Policy.POLYGON);
			rubberband.setActive(true);
			rubberband.startRubberbanding(mouseEvent.getPoint());
		}
	}

	/**
	 * Notification that rubber banding is finished.
	 */
	@Override
	public void doneRubberbanding() {
		Point pp[] = rubberband.getRubberbandVertices();
		rubberband = null;

		if ((pp == null) || (pp.length < 2)) {
			return;
		}
		AItem item = container.createPolygonItem(
				container.getAnnotationLayer(), pp);
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
