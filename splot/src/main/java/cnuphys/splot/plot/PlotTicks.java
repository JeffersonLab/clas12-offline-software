package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;

public class PlotTicks implements XmlPrintStreamWritable {

    /** The XML root element name */
    public static final String XmlRootElementName = "TickParameters";

    // ticks
    public static final String XmlMajTickLenAttName = "majticklen";
    public static final String XmlMinTickLenAttName = "minticklen";
    public static final String XmlNumMajTickXAttName = "xnummajtick";
    public static final String XmlNumMinTickXAttName = "xnummintick";
    public static final String XmlNumMajTickYAttName = "ynummajtick";
    public static final String XmlNumMinTickYAttName = "ynummintick";

    private int majorTickLen = 6;
    private int minorTickLen = 2;
    private int numMajorTickX = 4; // interior ticks
    private int numMinorTickX = 4; // interior ticks
    private int numMajorTickY = 4; // interior ticks
    private int numMinorTickY = 4; // interior ticks

    private Color _tickColor = Color.black;

    private Font _tickFont = Environment.getInstance().getCommonFont(11);

    // plot owner
    private PlotCanvas _plotCanvas;

    // work points
    private Point _pp = new Point();
    private Point2D.Double _wp = new Point2D.Double();

    public PlotTicks(PlotCanvas plotCanvas) {
	_plotCanvas = plotCanvas;
    }

    /**
     * Draw the plot ticks
     * 
     * @param g
     *            the Graphics context
     */
    public void draw(Graphics g) {
	Rectangle ab = _plotCanvas.getActiveBounds();
	if (ab == null) {
	    return;
	}

	Rectangle.Double world = _plotCanvas.getWorld();
	if (world == null) {
	    return;
	}

	g.setColor(_tickColor);
	g.setFont(_tickFont);

	double xmin = world.x;
	double ymin = world.y;
	double xmax = world.getMaxX();
	double ymax = world.getMaxY();

	double delx = (xmax - xmin) / (numMajorTickX + 1);
	double dely = (ymax - ymin) / (numMajorTickY + 1);

	// major ticks
	drawXTicks(g, xmin, xmax, world.getCenterY(), majorTickLen,
		numMajorTickX, ab, true);
	drawYTicks(g, ymin, ymax, world.getCenterX(), majorTickLen,
		numMajorTickY, ab, true);

	// minor ticks
	for (int i = 0; i <= numMajorTickX; i++) {
	    double xxmin = xmin + i * delx;
	    drawXTicks(g, xxmin, xxmin + delx, world.getCenterY(),
		    minorTickLen, numMinorTickX, ab, false);
	}

	// minor ticks
	for (int i = 0; i <= numMajorTickY; i++) {
	    double yymin = ymin + i * dely;
	    drawYTicks(g, yymin, yymin + dely, world.getCenterX(),
		    minorTickLen, numMinorTickY, ab, false);
	}

    }

    // draw x tick marks
    private void drawXTicks(Graphics g, double xmin, double xmax, double yc,
	    int ticklen, int numtick, Rectangle ab, boolean drawVal) {
	if (numtick < 1) {
	    return;
	}

	PlotParameters params = _plotCanvas.getParameters();
	FontMetrics fm = _plotCanvas.getFontMetrics(_tickFont);
	double delx = (xmax - xmin) / (numtick + 1);

	int t = ab.y;
	int b = t + ab.height;
	int sb = b + fm.getHeight();

	for (int i = 0; i <= (numtick + 1); i++) {
	    if ((i > 0) && (i < numtick + 1)) {
		double value = xmin + i * delx;
		_wp.setLocation(value, yc);
		_plotCanvas.worldToLocal(_pp, _wp);
		g.drawLine(_pp.x, b, _pp.x, b - ticklen);
		g.drawLine(_pp.x, t, _pp.x, t + ticklen);

		if (drawVal) {
		    String valstr = DoubleFormat.doubleFormat(value,
			    params.getNumDecimalX(), params.getMinExponentX());
		    int sw = fm.stringWidth(valstr);
		    g.drawString(valstr, _pp.x - sw / 2, sb);
		}
	    }
	}

    }

    // draw y tick marks
    private void drawYTicks(Graphics g, double ymin, double ymax, double xc,
	    int ticklen, int numtick, Rectangle ab, boolean drawVal) {
	if (numtick < 1) {
	    return;
	}

	PlotParameters params = _plotCanvas.getParameters();
	FontMetrics fm = _plotCanvas.getFontMetrics(_tickFont);
	double dely = (ymax - ymin) / (numtick + 1);

	int l = ab.x;
	int r = l + ab.width;
	int sb = l - 4;

	for (int i = 0; i <= (numtick + 1); i++) {
	    if ((i > 0) && (i < numtick + 1)) {
		double value = ymin + i * dely;
		_wp.setLocation(xc, value);
		_plotCanvas.worldToLocal(_pp, _wp);
		g.drawLine(l, _pp.y, l + ticklen, _pp.y);
		g.drawLine(r, _pp.y, r - ticklen, _pp.y);
		if (drawVal) {
		    String valstr = DoubleFormat.doubleFormat(value,
			    params.getNumDecimalY(), params.getMinExponentY());
		    int sw = fm.stringWidth(valstr);
		    GraphicsUtilities.drawRotatedText((Graphics2D) g, valstr,
			    _tickFont, sb, _pp.y + sw / 2, 0, 0, -90);
		}
	    }
	}

    }

    public int getNumMajorTickX() {
	return numMajorTickX;
    }

    public void setNumMajorTickX(int numMajorTickX) {
	this.numMajorTickX = numMajorTickX;
    }

    public int getNumMinorTickX() {
	return numMinorTickX;
    }

    public void setNumMinorTickX(int numMinorTickX) {
	this.numMinorTickX = numMinorTickX;
    }

    public int getNumMajorTickY() {
	return numMajorTickY;
    }

    public void setNumMajorTickY(int numMajorTickY) {
	this.numMajorTickY = numMajorTickY;
    }

    public int getNumMinorTickY() {
	return numMinorTickY;
    }

    public void setNumMinorTickY(int numMinorTickY) {
	this.numMinorTickY = numMinorTickY;
    }

    /**
     * This is called as a result of a save. The PlotTicks needs to write itself
     * out in xml.
     * 
     * @param write
     *            the xml writer
     */
    @Override
    public void writeXml(XmlPrintStreamWriter writer) {
	try {
	    Properties props = new Properties();
	    props.put(XmlMajTickLenAttName, majorTickLen);
	    props.put(XmlMinTickLenAttName, minorTickLen);
	    props.put(XmlNumMajTickXAttName, numMajorTickX);
	    props.put(XmlNumMinTickXAttName, numMinorTickX);
	    props.put(XmlNumMajTickYAttName, numMajorTickY);
	    props.put(XmlNumMinTickYAttName, numMinorTickY);
	    writer.writeElementWithProps(XmlRootElementName, props);

	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }

}
