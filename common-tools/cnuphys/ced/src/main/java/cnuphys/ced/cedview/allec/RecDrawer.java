package cnuphys.ced.cedview.allec;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.view.FBData;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.RECCalorimeter;
import cnuphys.ced.frame.CedColors;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

/**
 * Rec drawer for the AllECView
 * 
 * @author davidheddle
 *
 */
public class RecDrawer extends ECViewDrawer {

	// the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// the current event
	private DataEvent _currentEvent;

	// cached for feedback
	private ArrayList<FBData> _fbData = new ArrayList<>();

	public RecDrawer(ECView view) {
		super(view);
	}

	// ignore drawing or feedback
	private boolean ignore() {
		if (!_view.showRecCal()) {
			return true;
		}

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return true;
		}

		_currentEvent = _eventManager.getCurrentEvent();
		if (_currentEvent == null) {
			return true;
		}

		if (!_currentEvent.hasBank("REC::Calorimeter")) {
			return true;
		}

		return false;
	}

	@Override
	public void draw(Graphics g, IContainer container) {
		_fbData.clear();

		if (ignore()) {
			return;
		}

		RECCalorimeter recCal = RECCalorimeter.getInstance();
		if (recCal.isEmpty()) {
			return;
		}

		Point pp = new Point();
		Rectangle2D.Double wr = new Rectangle2D.Double();
		Point2D.Double wp = new Point2D.Double();

		for (int i = 0; i < recCal.count; i++) {

			if (recCal.layer[i] < 4) { // is it pcal?
				continue;
			}

			int plane = (recCal.layer[i] < 7) ? ECGeometry.EC_INNER : ECGeometry.EC_OUTER;

			if ((plane == ECGeometry.EC_INNER) && !_view.displayInner()) {
				continue;
			}

			if ((plane == ECGeometry.EC_OUTER) && _view.displayInner()) {
				continue;
			}

			Point3D clasP = new Point3D(recCal.x[i], recCal.y[i], recCal.z[i]);
			Point3D localP = new Point3D();
			ECGeometry.getTransformations(plane).clasToLocal(localP, clasP);

			localP.setZ(0);

			// get the right item
			_view.getHexSectorItem(recCal.sector[i]).ijkToScreen(container, localP, pp);

			SymbolDraw.drawDavid(g, pp.x, pp.y, 4, Color.black, Color.red);
			
			
			float radius = recCal.getRadius(recCal.energy[i]);
			if (radius > 0) {
				container.localToWorld(pp, wp);
				wr.setRect(wp.x - radius, wp.y - radius, 2 * radius, 2 * radius);
				
				LundId lid = recCal.getLundId(i);
				Color color = (lid == null) ? CedColors.RECEcalFill : lid.getStyle().getTransparentFillColor();
				
				
				WorldGraphicsUtilities.drawWorldOval(g, container, wr, color, null);
			}


			_fbData.add(new FBData(pp,
					String.format("$magenta$REC xyz (%-6.3f, %-6.3f, %-6.3f) cm", recCal.x[i], recCal.y[i],
							recCal.z[i]),
					String.format("$magenta$REC layer %d", recCal.layer[i]),
					String.format("$magenta$%s", recCal.getPIDStr(i)),
					String.format("$magenta$REC Energy %-7.4f GeV", recCal.energy[i])));

		} //for i

	}

	/**
	 * Use what was drawn to generate feedback strings
	 * 
	 * @param container       the drawing container
	 * @param screenPoint     the mouse location
	 * @param worldPoint      the corresponding world location
	 * @param feedbackStrings add strings to this collection
	 */
	@Override
	public void feedback(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (ignore()) {
			return;
		}

		for (FBData fbdata : _fbData) {
			boolean added = fbdata.addFeedback(screenPoint, feedbackStrings);
			if (added) {
				break;
			}
		}

	}

}