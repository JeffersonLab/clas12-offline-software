package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
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
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.HitRecord;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public class SectorECItem extends PolygonItem {

	protected static RenderingHints renderHints = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	static {
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
	};

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	// the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// should be ECGeometry.EC_INNER or ECGeometry.EC_OUTER
	private int _plane;

	// should be ECGeometry.EC_U, EC_V, or EC_W
	private int _stripType;

	private static final String _ecNames[] = { "EC (inner)", "EC (outer)" };
	private static final String _ecStripNames[] = { "U", "V", "W" };
	private static final Color _ecFill[] = { new Color(220, 220, 220),
			Color.white };

	/**
	 * Create a world polygon item
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param planeIndex
	 *            should be EC_INNER or EC_OUTER
	 * @param stripIndex
	 *            should be EC_U, EC_V, or EC_W
	 * @param sec
	 *            tor the 1-based sector
	 */
	public SectorECItem(LogicalLayer layer, int planeIndex, int stripIndex,
			int sector) {
		super(layer, getShell((SectorView) layer.getContainer().getView(),
				planeIndex, stripIndex, sector));

		setRightClickable(false);
		_sector = sector;
		_plane = planeIndex;
		_stripType = stripIndex;

		_name = _ecNames[_plane] + " " + _ecStripNames[stripIndex] + " sector "
				+ _sector;

		_style.setFillColor(_ecFill[planeIndex]);
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
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Point2D.Double path[] = getShell(_view, _plane, _stripType, _sector);

		if (path == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.addRenderingHints(renderHints);
		setPath(path);
		// super.drawItem(g, container);

		for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {
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
		Point2D.Double wp[] = ECGeometry.getIntersections(_plane, _stripType,
				stripId, _view.getTransformation3D(), true);

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

		// the data container
		ECDataContainer ecData = _eventManager.getECData();

		Color default_fc = Color.red;
		int pid[] = ecData.ec_true_pid;


		for (int hitIndex = 0; hitIndex < ecData
				.getHitCount(ECDataContainer.EC_OPTION); hitIndex++) {
			if ((ecData.ec_dgtz_sector[hitIndex] == _sector)
					&& (ecData.ec_dgtz_stack[hitIndex] == (_plane + 1))
					&& (ecData.ec_dgtz_view[hitIndex] == (_stripType + 1))) {
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

				int strip0 = ecData.ec_dgtz_strip[hitIndex] - 1;

				Point2D.Double wp[] = getStrip(strip0);

				if (wp != null) {
					Path2D.Double path = WorldGraphicsUtilities
							.worldPolygonToPath(wp);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc,
							_style.getLineColor(), 0, LineStyle.SOLID, true);
				}

			}
		}
	}
	
	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int maxHit = AccumulationManager.getInstance().getMaxDgtzEcCount();
		if (maxHit < 1) {
			return;
		}

		int hits[][][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzEcData();
		for (int strip0 = 0; strip0 < 36; strip0++) {
			int hit = hits[_sector - 1][_plane][_stripType][strip0];
			double fract = ((double) (hit)) / maxHit;
			
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
	 * @param planeIndex
	 *            the index (0: inner, 1:outer)
	 * @param stripType
	 *            the strip index (0:U, 1:V, 2:W)
	 * @param sector
	 *            the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, int planeIndex,
			int stripType, int sector) {

		Point2D.Double wp[] = ECGeometry.getShell(planeIndex, stripType,
				view.getTransformation3D());

		if (wp == null) {
			Log.getInstance().warning(
					"null shell in SectorECItem planeIndex = " + planeIndex
							+ " stripType = " + stripType + "  sector = "
							+ sector);
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

		for (int stripId = 0; stripId < ECGeometry.EC_NUMSTRIP; stripId++) {
			Point2D.Double wp[] = getStrip(stripId);
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities
						.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {
					feedbackStrings.add("$white$plane " + _ecNames[_plane]
							+ " type " + _ecStripNames[_stripType] + " strip "
							+ (stripId + 1));

					// on a hit?
					// the data container
					ECDataContainer ecData = _eventManager.getECData();
					Vector<HitRecord> hits = ecData.getMatchingHits(_sector,
							_plane + 1, _stripType + 1, stripId + 1,
							ECDataContainer.EC_OPTION);

					if (hits != null) {
						for (HitRecord hit : hits) {
							ecData.onHitFeedbackStrings(hit.hitIndex,
									ECDataContainer.EC_OPTION,
									ecData.ec_true_pid, ecData.ec_true_mpid,
									ecData.ec_true_tid, ecData.ec_true_mtid,
									ecData.ec_true_otid, feedbackStrings);
						}
					}

					return;
				}
			} // wp != null
		}
	}
}
