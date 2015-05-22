package cnuphys.splot.fit;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;

/**
 * This class is used to place holds on fit parameters
 * 
 * @author heddle
 *
 */
public class FitHold implements XmlPrintStreamWritable {

    /** The XML root element name */
    public static final String XmlRootElementName = "FitHold";
    public static final String XmlFitHoldIndexAttName = "fitholdindex";
    public static final String XmlFitHoldValueAttName = "fitholdvalue";

    // the index of the fit parameter to be held
    protected int index;

    // the value it is held at
    protected double value;

    /**
     * Create a hold on a fitting parameter. When the fit is performed, any
     * parameters that are "held" will be set to a fixed value and no changed in
     * the optimization
     * 
     * @param index
     *            the parameter index. All fits have a parameter array (usually
     *            called <code>a[]</code>. This is an index into that array.
     * @param value
     *            the value that should be held.
     */
    protected FitHold(int index, double value) {
	this.index = index;
	this.value = value;
    }

    @Override
    public void writeXml(XmlPrintStreamWriter writer) {
	try {
	    Properties props = new Properties();
	    props.put(XmlFitHoldIndexAttName, index);
	    props.put(XmlFitHoldValueAttName, value);
	    writer.writeElementWithProps(XmlRootElementName, props);

	} catch (XMLStreamException e) {
	    e.printStackTrace();
	}
    }
}
