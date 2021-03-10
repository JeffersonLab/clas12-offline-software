package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.ECGeometry;

public class SectorECItem extends PolygonItem {

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	// should be ECGeometry.EC_INNER or ECGeometry.EC_OUTER
	private int _plane;

	// should be ECGeometry.EC_U, EC_V, or EC_W
	private int _stripType;

	private static final String _ecNames[] = { "EC (inner)", "EC (outer)" };
	private static final String _ecStripNames[] = { "U", "V", "W" };
	private static final Color _ecFill[] = { new Color(225, 215, 215), new Color(215, 215, 225) };
	private static final Color _ecLine[] = { Color.gray, Color.gray };

	/**
	 * Create a world polygon item
	 * 
	 * @param layer      the Layer this item is on.
	 * @param planeIndex should be EC_INNER or EC_OUTER
	 * @param stripIndex should be EC_U, EC_V, or EC_W
	 * @param sec        tor the 1-based sector
	 */
	public SectorECItem(LogicalLayer layer, int planeIndex, int stripIndex, int sector) {
		super(layer, getShell((SectorView) layer.getContainer().getView(), planeIndex, stripIndex, sector));

		setRightClickable(false);
		_sector = sector;
		_plane = planeIndex;
		_stripType = stripIndex;

		_name = _ecNames[_plane] + " " + _ecStripNames[stripIndex] + " sector " + _sector;

		_style.setFillColor(_ecFill[planeIndex]);
		_style.setLineColor(_ecLine[planeIndex]);
		_style.setLineWidth(0);
		_view = (SectorView) getLayer().getContainer().getView();

	}
	
	@Override
	public boolean shouldDraw(Graphics g, IContainer container) {
		return true;
	}

	/**
	 * Custom drawer for the item.
	 * 
	 * @param g         the graphics context.
	 * @param container the graphical container being rendered.
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
			System.err.println("StripType: " + _stripType + " path is null");
			return;
		}

		setPath(path);
		// super.drawItem(g, container);

		for (int stripIndex = 0; stripIndex < ECGeometry.EC_NUMSTRIP; stripIndex++) {
			Point2D.Double wp[] = getStrip(stripIndex);

			if (wp != null) {
				Path2D.Double path2d = WorldGraphicsUtilities.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path2d, _style.getFillColor(), _style.getLineColor(), 0,
						LineStyle.SOLID, true);
			}
		}

		// hits
		drawHits(g, container);

	}

	/**
	 * Get a strip outline
	 * 
	 * @param stripIndex the 0-based index
	 * @return
	 */
	private Point2D.Double[] getStrip(int stripId) {

		if (!ECGeometry.doesProjectedPolyFullyIntersect(_plane, _stripType, stripId, _view.getProjectionPlane())) {
			return null;
		}

		Point2D.Double wp[] = ECGeometry.getIntersections(_plane, _stripType, stripId, _view.getProjectionPlane(),
				true);

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
		} else {
			drawAccumulatedHits(g, container);
		}
	}

	// single event drawer
	private void drawSingleEventHits(Graphics g, IContainer container) {
		TdcAdcHitList hits = AllEC.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (TdcAdcHit hit : hits) {
				if (hit != null) {
					try {
						if (hit.sector == _sector) {
							int layer = hit.layer - 4; // 0..5
							int stack0 = layer / 3; // 000,111
							int view0 = layer % 3; // 012012
							if ((stack0 == _plane) && (view0 == _stripType)) {
								int strip0 = hit.component - 1;

								Point2D.Double wp[] = getStrip(strip0);

								if (wp != null) {
									Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);

									Color fc = hits.adcColor(hit, AllEC.getInstance().getMaxECALAdc());
									WorldGraphicsUtilities.drawPath2D(g, container, path, fc, fc, 0, LineStyle.SOLID,
											true);
								}
							}

						}
					} catch (Exception e) {
						Log.getInstance().exception(e);
					}
				} // hit not null
				else {
					Log.getInstance().warning("[SectorECItem] null hit in ECAll hit list");
				}
			}
		}
	}

	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int medianHit = AccumulationManager.getInstance().getMedianECALCount(_plane);

		int hits[][][][] = AccumulationManager.getInstance().getAccumulatedECALData();
		for (int strip0 = 0; strip0 < 36; strip0++) {
			int hitCount = hits[_sector - 1][_plane][_stripType][strip0];
			double fract = _view.getMedianSetting() * (((double) hitCount) / (1 + medianHit));

			Point2D.Double wp[] = getStrip(strip0);

			if (wp != null) {
				Color color = AccumulationManager.getInstance().getColor(_view.getColorScaleModel(), fract);
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
				WorldGraphicsUtilities.drawPath2D(g, container, path, color, _style.getLineColor(), 0, LineStyle.SOLID,
						true);
			}

		}
	}

	/**
	 * Get the shell of the ec.
	 * 
	 * @param view       the view being rendered.
	 * @param planeIndex the index (0: inner, 1:outer)
	 * @param stripType  the strip index (0:U, 1:V, 2:W)
	 * @param sector     the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, int planeIndex, int stripType, int sector) {

		Point2D.Double wp[] = ECGeometry.getShell(planeIndex, stripType, view.getProjectionPlane());

		if (wp == null) {
			Log.getInstance().warning("null shell in SectorECItem planeIndex = " + planeIndex + " stripType = "
					+ stripType + "  sector = " + sector);
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
	 * Add any appropriate feedback strings panel. Default implementation returns
	 * the item's name.
	 * 
	 * @param container       the Base container.
	 * @param screenPoint     the mouse location.
	 * @param worldPoint      the corresponding world point.
	 * @param feedbackStrings the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		if (contains(container, screenPoint)) {
			feedbackStrings.add(getName());
		}

		// which strip?

		for (int stripId = 0; stripId < ECGeometry.EC_NUMSTRIP; stripId++) {
			Point2D.Double wp[] = getStrip(stripId);
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {
					feedbackStrings.add("$white$plane " + _ecNames[_plane] + " type " + _ecStripNames[_stripType]
							+ " strip " + (stripId + 1));

					// on a hit?
					TdcAdcHitList hits = AllEC.getInstance().getHits();
					if ((hits != null) && !hits.isEmpty()) {

						int layer = 4 + 3 * _plane + _stripType;

						TdcAdcHit hit = hits.get(_sector, layer, stripId + 1);
						if (hit != null) {
							hit.tdcAdcFeedback(AllEC.layerNames[hit.layer], "strip", feedbackStrings);
						}

					}

//					// on a hit?
//					// the data container
//					Vector<HitRecord> hits = EC.matchingHits(_sector,
//							_plane + 1, _stripType + 1, stripId + 1);
//
//					if (hits != null) {
//						for (HitRecord hit : hits) {
//							EC.preliminaryFeedback(hit.hitIndex, feedbackStrings);
//							DataSupport.truePidFeedback(EC.pid(), hit.hitIndex, feedbackStrings);
//							EC.dgtzFeedback(hit.hitIndex, feedbackStrings);
//						}
//					}

					return;
				}
			} // wp != null
		}
	}
}
