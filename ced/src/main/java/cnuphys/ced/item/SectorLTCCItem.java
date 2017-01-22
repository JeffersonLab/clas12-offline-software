package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.common.SuperLayerDrawing;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.LTCCGeometry;

public class SectorLTCCItem extends PolygonItem {

	private static Color _fillColors[] = {X11Colors.getX11Color("mint cream"), 
			X11Colors.getX11Color("alice blue")};

	// convenient access to the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// sector 1-based 1..6
	private int _sector;

	// 1-based ring 1..4
	private int _ring;
	
	// 1-based half, 1..2
	private int _half;

	// cache the outline
	private Point2D.Double[] _cachedWorldPolygon = GeometryManager.allocate(4);

	// the view this item lives on.
	private SectorView _view;

	
	/**
	 * Create a super layer item for the sector view. Note, no points are added
	 * in the constructor. The points will always be supplied by the setPoints
	 * method, which will send projected wire positions (with a border of guard
	 * wires)
	 * 
	 * @param logLayer
	 *            the Layer this item is on.
	 * @param view
	 *            the view this item lives on.
	 * @param sector
	 *            the 1-based sector [1..6]
	 * @param superLayer
	 *            the 1-based superlayer [1..6]
	 */
	public SectorLTCCItem(LogicalLayer logLayer, SectorView view, int sector, int ring, int half) {
		super(logLayer);
		_view = view;
		_sector = sector;
		_ring = ring;
		_half = half;
		setPath(getWorldPolygon());
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

		if (_eventManager.isAccumulating() || FastMCManager.getInstance().isStreaming()) {
			return;
		}
		
		setPath(getWorldPolygon());


		getStyle().setFillColor(_fillColors[_ring % 2]);
		super.drawItem(g, container); // draws shell
		
		// hits
		drawHits(g, container);
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
//		
//		int hitCount = HTCC.hitCount();
//		if (hitCount > 0) {
//			Color default_fc = Color.red;
//
//			int pid[] = HTCC.pid();
//			int sector[] = HTCC.sector();
//			int ring[] = HTCC.ring();
//			int half[] = HTCC.half();
//			
//			for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
//				if ((sector[hitIndex] == _sector)
//						&& (ring[hitIndex] == _ring)
//						&& (half[hitIndex] == _half)) {
//					Color fc = default_fc;
//					
//					if (_view.showMcTruth()) {
//						if (pid != null) {
//							LundId lid = LundSupport.getInstance()
//									.get(pid[hitIndex]);
//							if (lid != null) {
//								fc = lid.getStyle().getFillColor();
//							}
//						}
//					}
//					else {
//						fc = hitFillColor(hitIndex);
//					}
//
//					g.setColor(fc);
//					g.fillPolygon(_lastDrawnPolygon);
//					g.setColor(Color.black);
//					g.drawPolygon(_lastDrawnPolygon);
//				}
//			} //end for loop
//		} // hitCount > 0
	}
	
	private Color hitFillColor(int hitIndex) {
		Color color = Color.red;
//		if (hitIndex >= 0) {
//			int nphe[] = HTCC.nphe();
//			if ((nphe != null) && (hitIndex < nphe.length)) {
//				double numphe = nphe[hitIndex];
//				color = HTCC.colorScaleModel.getColor(numphe);
//			}
//		}
		
		return color;
	}
		
	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		
//		int maxHit = AccumulationManager.getInstance().getMaxDgtzHTCCCount();
//		if (maxHit < 1) {
//			return;
//		}
//
//		int hits[][][] = AccumulationManager.getInstance().getAccumulatedDgtzHTCCData();
//
//		int hit = hits[_sector - 1][_ring - 1][_half - 1];
//
//		double fract;
//		if (_view.isSimpleAccumulatedMode()) {
//			fract = ((double) hit) / maxHit;
//		} else {
//			fract = Math.log(hit + 1.) / Math.log(maxHit + 1.);
//		}
//
//		Color color = AccumulationManager.getInstance().getColor(fract);
//
//		g.setColor(color);
//		g.fillPolygon(_lastDrawnPolygon);
//		g.setColor(Color.black);
//		g.drawPolygon(_lastDrawnPolygon);
		
	}

	/**
	 * Add any appropriate feedback strings for the headsup display or feedback
	 * panel.
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
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		if (contains(container, screenPoint)) {
			if (contains(container, screenPoint)) {
				feedbackStrings.add(DataSupport.prelimColor + "LTCC sect " + _sector + 
						" ring " + _ring + " half " + _half);
				
				// on a hit?
				// the data container
//				Vector<HitRecord> hits = HTCC.matchingHits(_sector,
//						_ring, _half);
//
//				if (hits != null) {
//					for (HitRecord hit : hits) {
//						HTCC.preliminaryFeedback(hit.hitIndex, feedbackStrings);
//						DataSupport.truePidFeedback(EC.pid(), hit.hitIndex, feedbackStrings);
//						HTCC.dgtzFeedback(hit.hitIndex, feedbackStrings);
//					}
//				}

			}
		}
		
	}

	// get the world polygon corresponding to the boundary of the superlayer
	private Point2D.Double[] getWorldPolygon() {

		if (_dirty) {
			LTCCGeometry.getSimpleWorldPoly(_ring, _half, _view.getSliderPhi(), _cachedWorldPolygon);
			if (isLowerSector()) {
				SuperLayerDrawing.flipPolyToLowerSector(_cachedWorldPolygon);
			}
		} // end dirty
		return _cachedWorldPolygon;
	}
	

	/**
	 * Test whether this is a lower sector
	 * 
	 * @return <code>true</code> if this is a lower sector
	 */
	public boolean isLowerSector() {
		return (_sector > 3);
	}
	
	/**
	 * Get the 1-based sector
	 * @return the 1-based sector
	 */
	public int sector() {
		return _sector;
	}
	
	/**
	 * Get the 1-based ring
	 * @return the 1-based ring
	 */
	public int ring() {
		return _ring;
	}
	
	/**
	 * Get the 1-based half
	 * @return the 1-based half
	 */
	public int half() {
		return _half;
	}


}