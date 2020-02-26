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
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.LTCC;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.geometry.LTCCGeometry;

public class SectorLTCCItem extends PolygonItem {

	private static Color _fillColors[] = {X11Colors.getX11Color("mint cream"), 
			X11Colors.getX11Color("alice blue")};

	// convenient access to the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// sector 1-based 1..6
	private int _sector;

	// 1-based ring 1..18
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

		if (_eventManager.isAccumulating()) {
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
		AdcHitList hits = LTCC.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (AdcHit hit : hits) {
				if ((hit != null) && (hit.sector == _sector) && (hit.layer == _half) && (hit.component == _ring)) {
					g.setColor(hits.adcColor(hit));
					g.fillPolygon(_lastDrawnPolygon);
					g.setColor(Color.black);
					g.drawPolygon(_lastDrawnPolygon);
				}
			}
		}

	}

		
	// accumulated drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int medianHit = AccumulationManager.getInstance().getMedianLTCCCount();

		int hits[][][] = AccumulationManager.getInstance().getAccumulatedLTCCData();

		int hitCount = hits[_sector - 1][_half - 1][_ring - 1];

		double fract = _view.getMedianSetting() * (((double) hitCount) / (1+medianHit));

		Color color = AccumulationManager.getInstance().getColor(fract);

		g.setColor(color);
		g.fillPolygon(_lastDrawnPolygon);
		g.setColor(Color.black);
		g.drawPolygon(_lastDrawnPolygon);
	}

	/**
	 * Add any appropriate feedback strings
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
			
			AdcHitList hits = LTCC.getInstance().getHits();
			AdcHit hit = null;
			
			if ((hits != null) && !hits.isEmpty()) {
				//arggh opposite of htcc
				hit = hits.get(_sector, _half, _ring);
			}
			
			if (hit == null) {
				feedbackStrings.add(DataSupport.prelimColor + "LTCC sect " + _sector + 
						" ring " + _ring + " half " + _half);
			}
			else {
				hit.tdcAdcFeedback("half " + _half, "ring", feedbackStrings);
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