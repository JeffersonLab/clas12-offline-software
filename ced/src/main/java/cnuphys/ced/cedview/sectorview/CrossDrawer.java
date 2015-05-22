package cnuphys.ced.cedview.sectorview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.DCDataContainer;

public class CrossDrawer extends SectorViewDrawer {

    public static final int HB = 0;
    public static final int TB = 1;

    private static final Color TRANSYELLOW = new Color(255, 255, 0, 240);
    private static final Color TRANSORANGE = new Color(255, 128, 0, 240);

    private static Color transColors[] = { TRANSYELLOW, TRANSORANGE };
    private static String prefix[] = { "HB ", "TB " };

    private static final int CROSSHALF = 6; // pixels
    private static final int ARROWLEN = 40; // pixels
    private static final Stroke THICKLINE = new BasicStroke(1.5f);

    // feedback string color
    private static String fbcolors[] = { "$wheat$", "$misty rose$" };

    private int _mode = HB;

    private double tiltedx[];
    private double tiltedy[];
    private double tiltedz[];
    private int sector[];
    private double unitx[];
    private double unity[];
    private double unitz[];

    // cached rectangles for feedback
    private FeedbackRects[] _fbRects = new FeedbackRects[2];

    public CrossDrawer(SectorView view) {
	super(view);
	for (int i = 0; i < _fbRects.length; i++) {
	    _fbRects[i] = new FeedbackRects();
	}
    }

    /**
     * Set the mode. 0 for hit based, 1 for time based
     * 
     * @param mode
     *            the new mode
     */
    public void setMode(int mode) {
	_mode = mode;
    }

    @Override
    public void draw(Graphics g, IContainer container) {

	if (ClasIoEventManager.getInstance().isAccumulating()) {
	    return;
	}

	Graphics2D g2 = (Graphics2D) g;

	Stroke oldStroke = g2.getStroke();
	g2.setStroke(THICKLINE);

	// dc crosses?
	DCDataContainer dcData = ClasIoEventManager.getInstance().getDCData();
	int crossCount = 0;
	if (_mode == HB) {
	    crossCount = dcData.getHitBasedCrossCount();
	} else if (_mode == TB) {
	    crossCount = dcData.getTimeBasedCrossCount();
	}

	_fbRects[_mode].rects = null;

	if (crossCount > 0) {
	    double result[] = new double[3];
	    Point pp = new Point();
	    Point2D.Double wp = new Point2D.Double();
	    if (_mode == HB) {
		tiltedx = dcData.hitbasedtrkg_hbcrosses_x;
		tiltedy = dcData.hitbasedtrkg_hbcrosses_y;
		tiltedz = dcData.hitbasedtrkg_hbcrosses_z;
		sector = dcData.hitbasedtrkg_hbcrosses_sector;
		unitx = dcData.hitbasedtrkg_hbcrosses_ux;
		unity = dcData.hitbasedtrkg_hbcrosses_uy;
		unitz = dcData.hitbasedtrkg_hbcrosses_uz;
	    } else if (_mode == TB) {
		tiltedx = dcData.timebasedtrkg_tbcrosses_x;
		tiltedy = dcData.timebasedtrkg_tbcrosses_y;
		tiltedz = dcData.timebasedtrkg_tbcrosses_z;
		sector = dcData.timebasedtrkg_tbcrosses_sector;
		unitx = dcData.timebasedtrkg_tbcrosses_ux;
		unity = dcData.timebasedtrkg_tbcrosses_uy;
		unitz = dcData.timebasedtrkg_tbcrosses_uz;
	    }

	    _fbRects[_mode].rects = new Rectangle[crossCount];

	    for (int i = 0; i < crossCount; i++) {
		result[0] = tiltedx[i];
		result[1] = tiltedy[i];
		result[2] = tiltedz[i];
		_view.tiltedToSector(result, result);
		_view.sectorToWorld(wp, result, sector[i]);

		// right sector?
		int mySector = _view.getSector(container, null, wp);
		if (mySector == sector[i]) {

		    container.worldToLocal(pp, wp);

		    // arrows
		    Point2D.Double wp2 = new Point2D.Double();
		    Point pp2 = new Point();

		    int pixlen = ARROWLEN;
		    double r = pixlen
			    / WorldGraphicsUtilities
				    .getMeanPixelDensity(container);

		    result[0] = tiltedx[i] + r * unitx[i];
		    result[1] = tiltedy[i] + r * unity[i];
		    result[2] = tiltedz[i] + r * unitz[i];
		    _view.tiltedToSector(result, result);

		    _view.sectorToWorld(wp2, result, sector[i]);
		    container.worldToLocal(pp2, wp2);

		    g.setColor(Color.orange);
		    g.drawLine(pp.x + 1, pp.y, pp2.x + 1, pp2.y);
		    g.drawLine(pp.x, pp.y + 1, pp2.x, pp2.y + 1);
		    g.setColor(Color.darkGray);
		    g.drawLine(pp.x, pp.y, pp2.x, pp2.y);

		    // the circles and crosses
		    SymbolDraw.drawOval(g, pp.x, pp.y, CROSSHALF, CROSSHALF,
			    Color.black, transColors[_mode]);
		    SymbolDraw.drawCross(g, pp.x, pp.y, CROSSHALF, Color.black);

		    // fbrects for quick feedback
		    _fbRects[_mode].rects[i] = new Rectangle(pp.x - CROSSHALF,
			    pp.y - CROSSHALF, 2 * CROSSHALF, 2 * CROSSHALF);
		}

	    } // end for
	} // hbcrosscount > 0

	g2.setStroke(oldStroke);
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
     */
    @Override
    public void vdrawFeedback(IContainer container, Point screenPoint,
	    Point2D.Double worldPoint, List<String> feedbackStrings, int option) {

	if (_fbRects[_mode].rects == null) {
	    return;
	}

	// hit based dc crosses?
	DCDataContainer dcData = ClasIoEventManager.getInstance().getDCData();
	int hbcrosscount = dcData.getHitBasedCrossCount();

	if (hbcrosscount < 1) {
	    return;
	}

	boolean hitBased = (_mode == HB);
	double cross_x[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_x
		: dcData.timebasedtrkg_tbcrosses_x;
	double cross_y[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_y
		: dcData.timebasedtrkg_tbcrosses_y;
	double cross_z[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_z
		: dcData.timebasedtrkg_tbcrosses_z;
	int idarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_ID
		: dcData.timebasedtrkg_tbcrosses_ID;
	int sectarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_sector
		: dcData.timebasedtrkg_tbcrosses_sector;
	int regarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_region
		: dcData.timebasedtrkg_tbcrosses_region;
	// int trackarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_trackID :
	// dcData.timebasedtrkg_tbcrosses_trackID;
	double errxarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_err_x
		: dcData.timebasedtrkg_tbcrosses_err_x;
	double erryarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_err_y
		: dcData.timebasedtrkg_tbcrosses_err_y;
	double errzarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_err_z
		: dcData.timebasedtrkg_tbcrosses_err_z;
	double uxarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_ux
		: dcData.timebasedtrkg_tbcrosses_ux;
	double uyarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_uy
		: dcData.timebasedtrkg_tbcrosses_uy;
	double uzarray[] = hitBased ? dcData.hitbasedtrkg_hbcrosses_uz
		: dcData.timebasedtrkg_tbcrosses_uz;

	for (int i = 0; i < _fbRects[_mode].rects.length; i++) {
	    if ((_fbRects[_mode].rects[i] != null)
		    && _fbRects[_mode].rects[i].contains(screenPoint)) {

		double tiltedx = cross_x[i];
		double tiltedy = cross_y[i];
		double tiltedz = cross_z[i];

		int id = idarray[i];
		int sect = sectarray[i];
		int reg = regarray[i];
		// int track = trackarray[i];

		double xerr = errxarray[i];
		double yerr = erryarray[i];
		double zerr = errzarray[i];

		double ux = uxarray[i];
		double uy = uyarray[i];
		double uz = uzarray[i];

		// feedbackStrings.add(fbcolors[_mode] + prefix[_mode] +
		// "cross ID: " + id
		// + "  sect: " + sect + "  reg: " + reg
		// + "  track: " + track);
		feedbackStrings.add(fbcolors[_mode] + prefix[_mode]
			+ "cross ID: " + id + "  sect: " + sect + "  reg: "
			+ reg);

		feedbackStrings.add(vecStr("cross loc tilted", tiltedx,
			tiltedy, tiltedz));
		feedbackStrings.add(vecStr("cross error", xerr, yerr, zerr));
		feedbackStrings.add(vecStr("cross direc tilted", ux, uy, uz));

		double result[] = new double[3];
		result[0] = tiltedx;
		result[1] = tiltedy;
		result[2] = tiltedz;
		_view.tiltedToSector(result, result);
		feedbackStrings.add(vecStr("cross loc sector", result[0],
			result[1], result[2]));

		result[0] = ux;
		result[1] = uy;
		result[2] = uz;
		_view.tiltedToSector(result, result);
		feedbackStrings.add(vecStr("cross direc sector", result[0],
			result[1], result[2]));

		break;
	    }
	}
    }

    // for writing out a vector
    private String vecStr(String prompt, double vx, double vy, double vz) {
	return fbcolors[_mode] + prefix[_mode] + prompt + " ("
		+ DoubleFormat.doubleFormat(vx, 2) + ", "
		+ DoubleFormat.doubleFormat(vy, 2) + ", "
		+ DoubleFormat.doubleFormat(vz, 2) + ")";
    }

    class FeedbackRects {
	public Rectangle rects[];
    }
}
