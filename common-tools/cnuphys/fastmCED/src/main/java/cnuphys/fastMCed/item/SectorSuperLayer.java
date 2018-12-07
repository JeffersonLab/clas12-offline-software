package cnuphys.fastMCed.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jlab.geom.prim.Plane3D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.geometry.DCGeometry;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.view.sector.ISuperLayer;
import cnuphys.fastMCed.view.sector.SectorView;
import cnuphys.fastMCed.view.sector.SuperLayerDrawing;

/**
 * Used in SectorView views.
 * 
 * @author heddle
 *
 */
public class SectorSuperLayer extends PolygonItem implements ISuperLayer {

	//base superlayer drawer
	private SuperLayerDrawing _superlayerDrawer;

	// convenient access to the event manager
	private PhysicsEventManager _eventManager = PhysicsEventManager.getInstance();

	// sector 1-based 1..6
	private int _sector;

	private int _superLayer;

	// cache the outline
	private Point2D.Double[] _cachedWorldPolygon = GeometryManager.allocate(34);

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
	public SectorSuperLayer(LogicalLayer logLayer, SectorView view, int sector, int superLayer) {
		super(logLayer);
		_view = view;
		_sector = sector;
		_superLayer = superLayer;
		_superlayerDrawer = new SuperLayerDrawing(_view, this);
	}
	
	public SuperLayerDrawing getSuperLayerDrawer() {
		return _superlayerDrawer;
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

		//don't draw if streaming
		if (StreamManager.getInstance().isStarted()) {
			return;
		}

		getStyle().setFillColor(Color.white);
		super.drawItem(g, container); // draws shell

		_superlayerDrawer.drawItem(g, container, _lastDrawnPolygon);
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
		
		//don't bother if streaming
		if (StreamManager.getInstance().isStarted()) {
			return;
		}

		_superlayerDrawer.getFeedbackStrings(container, screenPoint, worldPoint, feedbackStrings);
	}

	/**
	 * The wires are dirty, probably because of a phi rotation
	 * 
	 * @param wires
	 *            the new wire projections.
	 */
	public void dirtyWires() {
		setDirty(true);
		setPath(getWorldPolygon());
	}

	// get the world polygon corresponding to the boundary of the superlayer
	private Point2D.Double[] getWorldPolygon() {

		if (_dirty) {
			DCGeometry.getSuperLayerPolygon(_superLayer, projectionPlane(), _cachedWorldPolygon);
			if (isLowerSector()) {
				SuperLayerDrawing.flipPolyToLowerSector(_cachedWorldPolygon);
			}
		} // end dirty
		return _cachedWorldPolygon;
	}


	/**
	 * Get the plane perpendicular to the wires
	 * @return the plane perpendicular to the wires
	 */
	@Override
	public Plane3D projectionPlane() {
		return _view.getProjectionPlane();
	}
	/**
	 * Test whether this is a lower sector
	 * 
	 * @return <code>true</code> if this is a lower sector
	 */
	@Override
	public boolean isLowerSector() {
		return (_sector > 3);
	}
	/**
	 * Get the 1-based sector
	 * @return the 1-based sector
	 */
	@Override
	public int sector() {
		return _sector;
	}
	
	/**
	 * Get the one based superlayer
	 * @return the one based superlayyer
	 */
	@Override
	public int superlayer() {
		return _superLayer;
	}
	
	/**
	 * return the underlying polygon item
	 * @return the underlying polygon item
	 */
	@Override
	public PolygonItem item() {
		return this;
	}

}
