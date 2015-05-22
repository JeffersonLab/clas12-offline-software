package cnuphys.splot.plot;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.xml.XmlPrintStreamWriter;
import cnuphys.splot.xml.XmlSupport;

public class VerticalLine extends PlotLine {

    /** The XML root element name */
    public static final String XmlRootElementName = "VLINE";

    // the x value of the vertical line
    private double _x;

    public VerticalLine(PlotCanvas canvas, double x) {
	super(canvas);
	_x = x;
    }

    @Override
    public double getX0() {
	return _x;
    }

    @Override
    public double getX1() {
	return _x;
    }

    @Override
    public double getY0() {
	return _canvas.getWorld().getMinY();
    }

    @Override
    public double getY1() {
	return _canvas.getWorld().getMaxY();
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
	props.put(XmlSupport.XmlValAttName, _x);
	try {
	    writer.writeElementWithProps(XmlRootElementName, props);
	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }

}
