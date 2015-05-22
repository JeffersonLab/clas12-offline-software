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
import cnuphys.ced.event.data.ECDataContainer;
import cnuphys.ced.event.data.HitRecord;
import cnuphys.ced.geometry.PCALGeometry;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.splot.plot.X11Colors;

public class SectorPCALItem extends PolygonItem {

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

    // should be PCLAGeometry.PCAL_U, PCAL_V, or PCAL_W
    private int _stripType;

    private static final String _stripNames[] = { "U", "V", "W" };
    private static final Color _ecFill = X11Colors.getX11Color("alice blue");

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

	_style.setFillColor(_ecFill);
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

	Point2D.Double path[] = getShell(_view, _stripType, _sector);

	if (path == null) {
	    return;
	}

	Graphics2D g2 = (Graphics2D) g;
	g2.addRenderingHints(renderHints);
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

	// the data container
	ECDataContainer ecData = _eventManager.getECData();

	Color default_fc = Color.red;
	int pid[] = ecData.pcal_true_pid;

	boolean drew = false;

	for (int hitIndex = 0; hitIndex < ecData
		.getHitCount(ECDataContainer.PCAL_OPTION); hitIndex++) {
	    if ((ecData.pcal_dgtz_sector[hitIndex] == _sector)
		    && (ecData.pcal_dgtz_view[hitIndex] == (_stripType + 1))) {
		Color fc = default_fc;
		if (pid != null) {
		    LundId lid = LundSupport.getInstance().get(pid[hitIndex]);
		    if (lid != null) {
			fc = lid.getStyle().getFillColor();
		    }
		}

		int strip0 = ecData.pcal_dgtz_strip[hitIndex] - 1;

		Point2D.Double wp[] = getStrip(strip0);

		if (wp != null) {
		    Path2D.Double path = WorldGraphicsUtilities
			    .worldPolygonToPath(wp);
		    WorldGraphicsUtilities.drawPath2D(g, container, path, fc,
			    _style.getLineColor(), 0, LineStyle.SOLID, true);
		    drew = true;
		}

	    }
	} // loop hits
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
		view.getTransformation3D());

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
		    ECDataContainer ecData = _eventManager.getECData();
		    Vector<HitRecord> hits = ecData.getMatchingHits(_sector, 1,
			    _stripType + 1, stripId + 1,
			    ECDataContainer.PCAL_OPTION);

		    if (hits != null) {
			for (HitRecord hit : hits) {
			    ecData.onHitFeedbackStrings(hit.hitIndex,
				    ECDataContainer.PCAL_OPTION,
				    ecData.pcal_true_pid,
				    ecData.pcal_true_mpid,
				    ecData.pcal_true_tid,
				    ecData.pcal_true_mtid,
				    ecData.pcal_true_otid, feedbackStrings);
			}
		    }

		    return;
		}
	    } // wp != null
	}

    }
}