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
public class RadArcButton extends ToolBarToggleButton implements IRubberbanded {

	/**
	 * Create a button for creating rectangles by rubberbanding.
	 * 
	 * @param container
	 *            the container using this button.
	 */
	public RadArcButton(IContainer container) {
		super(container, "images/radarc.gif", "Create a radius-arc");
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
					Rubberband.Policy.RADARC);
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

		// this is tricky. There should be three points in the poly. The 0th is
		// the center. The first determines the start angle and the radius. The
		// 2nd,
		// relative to the first, determines the opening angle.

		if ((pp == null) || (pp.length != 3)) {
			return;
		}

		double xc = pp[0].x;
		double yc = pp[0].y;
		double x1 = pp[1].x;
		double y1 = pp[1].y;
		double x2 = pp[2].x;
		double y2 = pp[2].y;
		double dx1 = x1 - xc;
		double dy1 = y1 - yc;
		double dx2 = x2 - xc;
		double dy2 = y2 - yc;
		double r1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
		double r2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

		if (r1 < 0.99) {
			return;
		}
		if (r2 < 0.99) {
			return;
		}

		double aAngle = Math.acos((dx1 * dx2 + dy1 * dy2) / (r1 * r2));
		if ((dx1 * dy2 - dx2 * dy1) > 0.0) {
			aAngle = -aAngle;
		}

		// scale to have save radius
		double scale = r1 / r2;
		dx2 *= scale;
		dy2 *= scale;
		x2 = xc + dx2;
		y2 = yc + dy2;

		double arcAngle = (int) Math.toDegrees(aAngle);

		int pixrad = (int) r1;

		if (pixrad < 3) {
			return;
		}

		// create a rad arc item
		AItem item = container.createRadArcItem(container.getAnnotationLayer(),
				pp[0], pp[1], arcAngle);

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
