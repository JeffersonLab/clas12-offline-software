package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.HitRecord;
import cnuphys.ced.event.data.PCAL;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public class SectorPCALItem extends PolygonItem {

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	// the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// should be PCLAGeometry.PCAL_U, PCAL_V, or PCAL_W
	private int _stripType;

	private static final String _stripNames[] = { "U", "V", "W" };
	private static final Color _pcalFill = new Color(220, 220, 220);
	private static final Color _pcalLine = new Color(140, 140, 140);
	
	private static int[] _stripCounts = {68, 62, 62}; //u,v,w

	/**
	 * Create a sector view pcal item
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param stripIndex
	 *            should be PCAL_U, PCAL_V, or PCAL_W
	 * @param sector
	 *            the 1-based sector
	 */
	public SectorPCALItem(LogicalLayer layer, int stripIndex, int sector) {
		super(layer, getShell((SectorView) layer.getContainer().getView(),
				stripIndex, sector));

		setRightClickable(false);
		_sector = sector;
		_stripType = stripIndex;

		_name = "PCAL " + _stripNames[stripIndex] + " sector " + _sector;

		_style.setFillColor(_pcalFill);
		_style.setLineColor(_pcalLine);
		_style.setLineWidth(0);
		_view = (SectorView) getLayer().getContainer().getView();

	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		// TODO use dirty. If the item is not dirty, should be able to draw
		// the _lastDrawnPolygon directly;
		if (ClasIoEventManager.getInstance().isAccumulating() || FastMCManager.getInstance().isStreaming()) {
			return;
		}

		Point2D.Double path[] = getShell(_view, _stripType, _sector);

		if (path == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		setPath(path);
		// super.drawItem(g, container);

		for (int stripIndex = 0; stripIndex < PCALGeometry.PCAL_NUMSTRIP[_stripType]; stripIndex++) {
			Point2D.Double wp[] = getStrip(stripIndex);

			if (wp != null) {
				Path2D.Double path2d = WorldGraphicsUtilities
						.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path2d,
						_style.getFillColor(), _style.getLineColor(), 0,
						LineStyle.SOLID, true);
			}
		}

		// hits
		drawHits(g, container);

	}

	/**
	 * Get a strip outline
	 * 
	 * @param stripIndex
	 *            the 0-based index
	 * @return
	 */
	private Point2D.Double[] getStrip(int stripId) {
		Point2D.Double wp[] = PCALGeometry.getIntersections(_stripType,
				stripId, _view.getProjectionPlane(), true);

		if (wp == null) {
			return null;
		}

		// lower sectors (4, 5, 6) (need sign flip
		if (_sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	// draw any hits
	private void drawHits(Graphics g, IContainer container) {

		if (_view.isSingleEventMode()) {
			drawSingleEventHits(g, container);
		}
		else {
			drawAccumulatedHits(g, container);
		}
	}
	
	
	//single event drawer
	private void drawSingleEventHits(Graphics g, IContainer container) {
		
		int hitCount = PCAL.hitCount();
		if (hitCount > 0) {
			Color default_fc = Color.red;

			int pid[] = PCAL.pid();
			int sector[] = PCAL.sector();
			int view[] = PCAL.view();
			int strip[] = PCAL.strip();
			
			for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
				if ((sector[hitIndex] == _sector)
						&& (view[hitIndex] == (_stripType + 1))) {
					Color fc = default_fc;
					
					if (_view.showMcTruth()) {
						if (pid != null) {
							LundId lid = LundSupport.getInstance()
									.get(pid[hitIndex]);
							if (lid != null) {
								fc = lid.getStyle().getFillColor();
							}
						}
					}

					int strip0 = strip[hitIndex] - 1;

					Point2D.Double wp[] = getStrip(strip0);

					if (wp != null) {
						Path2D.Double path = WorldGraphicsUtilities
								.worldPolygonToPath(wp);
						WorldGraphicsUtilities.drawPath2D(g, container, path, fc,
								fc, 0, LineStyle.SOLID, true);
					}

				}
			} // loop hits
		} //hitcount > 0
	}
	
	//accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int maxHit = AccumulationManager.getInstance().getMaxDgtzPcalCount();
		if (maxHit < 1) {
			return;
		}

		int hits[][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzPcalData();
		for (int strip0 = 0; strip0 < _stripCounts[_stripType]; strip0++) {
			int hit = hits[_sector - 1][_stripType][strip0];
			double fract;
			if (_view.isSimpleAccumulatedMode()) {
				fract = ((double) hit) / maxHit;
			}
			else {
				fract = Math.log(hit+1.)/Math.log(maxHit+1.);
			}
			
			Point2D.Double wp[] = getStrip(strip0);

			if (wp != null) {
				Color color = AccumulationManager.getInstance()
						.getColor(fract);
				Path2D.Double path = WorldGraphicsUtilities
						.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path, color,
						_style.getLineColor(), 0, LineStyle.SOLID, true);
			}

		}
	}


	/**
	 * Get the shell of the ec.
	 * 
	 * @param view
	 *            the view being rendered.
	 * @param stripType
	 *            the strip index (0:U, 1:V, 2:W)
	 * @param sector
	 *            the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, int stripType,
			int sector) {

		Point2D.Double wp[] = PCALGeometry.getShell(stripType,
				view.getProjectionPlane());

		if (wp == null) {
			Log.getInstance().warning(
					"null shell in SectorPCALItem stripType = " + stripType
							+ "  sector = " + sector);
			return null;
		}

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	/**
	 * Add any appropriate feedback strings for the headsup display or feedback
	 * panel. Default implementation returns the item's name.
	 * 
	 * @param container
	 *            the Base container.
	 * @param screenPoint
	 *            the mouse location.
	 * @param worldPoint
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		if (contains(container, screenPoint)) {
			feedbackStrings.add(getName());
		}

		// which strip?

		for (int stripId = 0; stripId < PCALGeometry.PCAL_NUMSTRIP[_stripType]; stripId++) {
			Point2D.Double wp[] = getStrip(stripId);
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities
						.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {
					feedbackStrings.add("$white$type "
							+ _stripNames[_stripType] + " strip "
							+ (stripId + 1));

					// on a hit?
					// the data container
					Vector<HitRecord> hits = PCAL.matchingHits(_sector,
							_stripType + 1, stripId + 1);

					if (hits != null) {
						for (HitRecord hit : hits) {
							PCAL.preliminaryFeedback(hit.hitIndex, feedbackStrings);
							DataSupport.truePidFeedback(PCAL.pid(), hit.hitIndex, feedbackStrings);
							PCAL.dgtzFeedback(hit.hitIndex, feedbackStrings);
						}
					}

					return;
				}
			} // wp != null
		}

	}
}