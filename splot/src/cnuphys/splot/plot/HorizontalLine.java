package cnuphys.splot.plot;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.xml.XmlPrintStreamWriter;
import cnuphys.splot.xml.XmlSupport;

public class HorizontalLine extends PlotLine {

    /** The XML root element name */
    public static final String XmlRootElementName = "HLINE";

    // the y value of the horizontal line
    private double _y;

    public HorizontalLine(PlotCanvas canvas, double y) {
	super(canvas);
	_y = y;
    }

    @Override
    public double getX0() {
	return _canvas.getWorld().getMinX();
    }

    @Override
    public double getX1() {
	return _canvas.getWorld().getMaxX();
    }

    @Override
    public double getY0() {
	return _y;
    }

    @Override
    public double getY1() {
	return _y;
    }

    /**
     * This is called as a result of a save. The object needs to write itself
     * out in xml.
     * 
     * @param write
     *            the xml writer
     */
    @Override
    public void writeXml(XmlPrintStreamWriter writer) {
	Properties props = new Properties();
	props.put(XmlSupport.XmlValAttName, _y);
	try {
	    writer.writeElementWithProps(XmlRootElementName, props);
	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }

}
