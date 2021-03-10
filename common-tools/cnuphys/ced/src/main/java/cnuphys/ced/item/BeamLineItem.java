package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.magfieldview.MagfieldView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.BaseBeamLineItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.view.BaseView;

public class BeamLineItem extends BaseBeamLineItem {

	// target icon
	// private static final ImageIcon _targetIcon = ImageManager.getInstance()
	// .loadImageIcon("images/target.gif");

	/**
	 * Create a beamline item which is a glorified line.
	 * 
	 * @param layer the Layer this item is on.
	 */
	public BeamLineItem(LogicalLayer layer) {
		super(layer);
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		super.drawItem(g, container);

		// draw target
		BaseView view = container.getView();
		double targetZ = Double.NaN;
		if (view instanceof SectorView) {
			targetZ = ((SectorView) view).getTargetZ();
		} else if (view instanceof CentralZView) {
			targetZ = ((CentralZView) view).getTargetZ();
		}
		else if (view instanceof MagfieldView) {
			targetZ =((MagfieldView) view).getTargetZ();
		}

		// draw it?
		if (!Double.isNaN(targetZ)) {
			Point targetPP = new Point();
			container.worldToLocal(targetPP, targetZ, 0.0);
			int xc = targetPP.x;
			int yc = targetPP.y;

			g.setColor(Color.white);
			g.fillOval(xc - 3, yc - 3, 6, 6);

			g.setColor(Color.black);
			g.drawOval(xc - 3, yc - 3, 6, 6);

			g.drawLine(xc, yc - 3, xc, yc + 3);
			g.drawLine(xc - 3, yc, xc + 3, yc);
		}

	}

	// no feedback for beamline
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {
	}
}
