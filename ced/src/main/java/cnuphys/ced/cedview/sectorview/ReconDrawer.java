package cnuphys.ced.cedview.sectorview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DCDataContainer;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.ced.geometry.GeometryManager;

public class ReconDrawer extends SectorViewDrawer {

    // cached rectangles for feedback
    private Vector<FeedbackRect> _fbRects = new Vector<FeedbackRect>();

    // feedback string color
    private static String fbcolors[] = { "$wheat$", "$misty rose$" };
    private static String prefix[] = { "HB ", "TB " };

    public ReconDrawer(SectorView view) {
	super(view);
    }

    @Override
    public void draw(Graphics g, IContainer container) {

	if (ClasIoEventManager.getInstance().isAccumulating()) {
	    return;
	}

	// reconstructed drawing
	DCDataContainer dcData = ClasIoEventManager.getInstance().getDCData();
	_fbRects.clear();

	// drift chamber hit based
	if (_view.showDChbHits()) {
	    drawDCReconHits(g, container, dcData, DataDrawSupport.DC_HB_COLOR,
		    0);
	}

	// drift chamber time based based
	if (_view.showDCtbHits()) {
	    drawDCReconHits(g, container, dcData, DataDrawSupport.DC_TB_COLOR,
		    1);
	}

	// FTOF
	if (_view.showFTOFReconHits()) {
	    drawFTOFReconHits(g, container);
	}
    }

    // draw FTOF reconstructed hits

    // draw reconstructed hits
    private void drawFTOFReconHits(Graphics g, IContainer container) {
	FTOFDataContainer ftofData = _eventManager.getFTOFData();

	// arggh this sector array is zero based
	int sector[] = ftofData.ftofrec_ftofhits_sector;
	float recX[] = ftofData.ftofrec_ftofhits_x;
	float recY[] = ftofData.ftofrec_ftofhits_y;
	float recZ[] = ftofData.ftofrec_ftofhits_z;
	int panel[] = ftofData.ftofrec_ftofhits_panel_id;
	int paddle[] = ftofData.ftofrec_ftofhits_paddle_id;

	// _view.getWorldFromDetectorXYZ(100 * v3d[0], 100 *v3d[1],
	// 100 * v3d[2], wp);

	if ((recX != null) && (recY != null) && (recZ != null)) {
	    double sectXYZ[] = new double[3];
	    double labXYZ[] = new double[3];
	    Point2D.Double wp = new Point2D.Double();
	    Point pp = new Point();
	    int hitCount = recX.length;
	    // System.err.println("Drawing " + hitCount + " ftof recons hits");
	    for (int hitIndex = 0; hitIndex < hitCount; hitIndex++) {
		int sect = sector[hitIndex] + 1; // 1-based
		if (_view.isSectorOnView(sect)) {
		    sectXYZ[0] = recX[hitIndex];
		    sectXYZ[1] = recY[hitIndex];
		    sectXYZ[2] = recZ[hitIndex];
		    GeometryManager.sectorXYZToLabXYZ(sect, labXYZ, sectXYZ);
		    _view.getWorldFromLabXYZ(labXYZ[0], labXYZ[1], labXYZ[2],
			    wp);

		    // _view.sectorToWorld(wp, sectXYZ, sect);
		    container.worldToLocal(pp, wp);

		    String s1 = "$Orange Red$"
			    + vecStr("FTOF hit (lab)", labXYZ[0], labXYZ[1],
				    labXYZ[2]);
		    String s2 = "$Orange Red$FTOF panel: "
			    + FTOFDataContainer.panelNames[panel[hitIndex] - 1]
			    + " paddle: " + (paddle[hitIndex] + 1);

		    container.worldToLocal(pp, wp);
		    FeedbackRect fbr = new FeedbackRect(pp.x - 4, pp.y - 4, 8,
			    8, hitIndex, ftofData, 0, s1, s2);
		    _fbRects.add(fbr);

		    DataDrawSupport.drawReconHit(g, pp);
		}
	    }
	}
    }

    // draw reconstructed dc hits
    // here option is used to choose between time based and hit based
    private void drawDCReconHits(Graphics g, IContainer container,
	    DCDataContainer dcData, Color color, int option) {

	double hX[] = null;
	double hZ[] = null;
	int sector[] = null;
	int superlayer[] = null;
	int layer[] = null;
	int wire[] = null;

	// option specifies whether hit based (0) or time based (1)
	if (option == 0) {
	    hX = dcData.hitbasedtrkg_hbhits_X;
	    hZ = dcData.hitbasedtrkg_hbhits_Z;
	    sector = dcData.hitbasedtrkg_hbhits_sector;
	    superlayer = dcData.hitbasedtrkg_hbhits_superlayer;
	    layer = dcData.hitbasedtrkg_hbhits_layer;
	    wire = dcData.hitbasedtrkg_hbhits_wire;
	} else if (option == 1) { // time based
	    hX = dcData.timebasedtrkg_tbhits_X;
	    hZ = dcData.timebasedtrkg_tbhits_Z;
	    sector = dcData.timebasedtrkg_tbhits_sector;
	    superlayer = dcData.timebasedtrkg_tbhits_superlayer;
	    layer = dcData.timebasedtrkg_tbhits_layer;
	    wire = dcData.timebasedtrkg_tbhits_wire;
	} else {
	    return;
	}

	int hbHitCount = (sector == null) ? 0 : sector.length;

	if ((hbHitCount > 0) && (wire != null) && (layer != null)
		&& (superlayer != null)) {

	    double tiltedXYZ[] = new double[3];
	    double sectorXYZ[] = new double[3];
	    double labXYZ[] = new double[3];
	    Point2D.Double wp = new Point2D.Double();
	    Point pp = new Point();

	    for (int hitIndex = 0; hitIndex < hbHitCount; hitIndex++) {

		if (_view.isSectorOnView(sector[hitIndex])) {
		    int upperOrLower;
		    if (sector[hitIndex] < 4) {
			upperOrLower = SectorView.UPPER_SECTOR;
		    } else {
			upperOrLower = SectorView.LOWER_SECTOR;
		    }
		    Polygon poly = _view.getHexagon(container, upperOrLower,
			    superlayer[hitIndex], layer[hitIndex],
			    wire[hitIndex]);
		    Rectangle rr = poly.getBounds();
		    drawDiag(g, rr, color, option, poly);

		    // the data in the arrays are in tilted coordinates
		    tiltedXYZ[0] = hX[hitIndex];
		    tiltedXYZ[1] = 0;
		    tiltedXYZ[2] = hZ[hitIndex];

		    _view.tiltedToSector(tiltedXYZ, sectorXYZ);
		    // _view.sectorToWorld(wp, sectorXYZ, sector[hitIndex]);

		    GeometryManager.sectorXYZToLabXYZ(sector[hitIndex], labXYZ,
			    sectorXYZ);
		    _view.getWorldFromLabXYZ(labXYZ[0], labXYZ[1], labXYZ[2],
			    wp);

		    String s = fbcolors[option]
			    + prefix[option]
			    + vecStr(" hit location", sectorXYZ[0],
				    sectorXYZ[1], sectorXYZ[2]);

		    container.worldToLocal(pp, wp);
		    FeedbackRect fbr = new FeedbackRect(pp.x - 4, pp.y - 4, 8,
			    8, hitIndex, dcData, option, s);
		    _fbRects.add(fbr);

		    DataDrawSupport.drawCircleCross(g, fbr, Color.gray, color);
		} // sector on view
	    } // end hit based for
	} // end arrays not null
    }

    private void drawDiag(Graphics g, Rectangle rect, Color color, int opt,
	    Polygon hex) {

	Graphics2D g2 = (Graphics2D) g;
	Shape oldClip = g2.getClip();
	g2.setClip(hex);

	int l = rect.x + 2;
	int t = rect.y + 2;
	int r = l + rect.width - 4;
	int b = t + rect.height - 4;

	g.setColor(color);

	if (opt == 0) {
	    g.drawLine(l, b, r, t);
	} else {
	    g.drawLine(l, t, r, b);
	}

	g2.setClip(oldClip);
    }

    /**
     * See if we are on a feedback rect
     * 
     * @param container
     *            the drawing container
     * @param screenPoint
     *            the mouse location
     * @param option
     *            0 for hit based, 1 for time based
     * @return the FeedbackRect, or <code>null</code>
     */
    public FeedbackRect getFeedbackRect(IContainer container,
	    Point screenPoint, int option) {
	if (_fbRects.isEmpty()) {
	    return null;
	}

	for (FeedbackRect rr : _fbRects) {
	    if ((rr.option == option) && rr.contains(screenPoint)) {
		return rr;
	    }
	}
	return null;
    }

    /**
     * Use what was drawn to generate feedback strings
     * 
     * @param container
     *            the drawing container
     * @param screenPoint
     *            the mouse location
     * @param worldPoint
     *            the corresponding world location
     * @param feedbackStrings
     *            add strings to this collection
     * @param option
     *            0 for hit based, 1 for time based
     */
    @Override
    public void vdrawFeedback(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint, List<String> feedbackStrings, int option) {

	if (_fbRects.isEmpty()) {
	    return;
	}

	for (FeedbackRect rr : _fbRects) {
	    if ((rr.option == option)
		    && rr.contains(screenPoint, feedbackStrings)) {
		return;
	    }
	}

    }

    // for writing out a vector
    private String vecStr(String prompt, double vx, double vy, double vz) {
	return vecStr(prompt, vx, vy, vz, 2);
    }

    // for writing out a vector
    private String vecStr(String prompt, double vx, double vy, double vz,
	    int ndig) {
	return prompt + " (" + DoubleFormat.doubleFormat(vx, ndig) + ", "
		+ DoubleFormat.doubleFormat(vy, ndig) + ", "
		+ DoubleFormat.doubleFormat(vz, ndig) + ")";
    }

}
