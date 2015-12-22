package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Font;
import java.util.Properties;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;

import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;
import cnuphys.splot.xml.XmlSupport;

public class PlotParameters implements XmlPrintStreamWritable {

	/** The XML root element name */
	public static final String XmlRootElementName = "PlotParameters";

	// force include zero on plots?
	public static final String XmlIncZeroElementName = "IncludeZero";
	public static final String XmlXZeroAttName = "xzero";
	public static final String XmlYZeroAttName = "yzero";
	private boolean _includeYzero = false;
	private boolean _includeXzero = false;

	// use data limits as plot limits
	public static final String XmlUseDataLimitElementName = "UseDataLimits";
	public static final String XmlXUseDataLimitAttName = "xdatalimit";
	public static final String XmlYUseDataLimitAttName = "ydatalimit";
	private boolean _useYDataLimits = false;
	private boolean _useXDataLimits = false;

	// ticks
	public static final String XmlTickValueDisplayElementName = "TickValueDisplay";
	public static final String XmlNumDecXAttName = "xnumdec";
	public static final String XmlMixExpXAttName = "xminexp";
	public static final String XmlNumDecYAttName = "ynumdec";
	public static final String XmlMixExpYAttName = "yminexp";

	// names for manual ranges XmlManualRangeElementName
	public static final String XmlManualRangeElementName = "ManualRanges";
	public static final String XmlUseManualXAttName = "xmanual";
	public static final String XmlUseManualYAttName = "ymanual";
	public static final String XmlManualMinXAttName = "xminmanual";
	public static final String XmlManualMaxXAttName = "xmaxmanual";
	public static final String XmlManualMinYAttName = "yminmanual";
	public static final String XmlManualMaxYAttName = "ymaxmanual";

	// legend
	public static final String XmlLegendElementName = "LegendDrawing";
	public static final String XmlDrawLegendAttName = "draw";
	public static final String XmlLegendFontAttName = "legendfont";
	public static final String XmlLegendLineLengthAttName = "legendlinelength";
	public static final String XmlLegendBorderAttName = "legendborder";
	public static final String XmlLegendFillColorAttName = "legendbackground";
	public static final String XmlLegendTextColorAttName = "legendforeground";
	public static final String XmlLegendBorderColorAttName = "legendbordercolor";

	private int _legendLineLength = 70;
	private Font _legendFont = Environment.getInstance().getCommonFont(12);
	private boolean _legendBorder = true;
	private Color _legendFillColor = new Color(255, 255, 255, 164);
	private Color _legendTextColor = Color.black;
	private Color _legendBorderColor = Color.black;
	private boolean _drawLegend = true;

	// labels
	public static final String XmlMainLabelElementName = "MainLabels";
	public static final String XmlTitleAttName = "title";
	public static final String XmlXLabAttName = "xlab";
	public static final String XmlYLabAttName = "ylab";
	public static final String XmlTitleFontAttName = "titlefont";
	public static final String XmlAxesFontAttName = "axesfont";
	public static final String XmlStatusFontAttName = "statusfont";

	private Font _titleFont = Environment.getInstance().getCommonFont(20);
	private Font _axesLabelFont = Environment.getInstance().getCommonFont(14);
	private String _plotTitle = "No Plot";
	private String _xLabel = "X Data";
	private String _yLabel = "Y Data";
	private Font _statusFont = Environment.getInstance().getCommonFont(10);

	// annotating lines
	private Vector<PlotLine> _lines = new Vector<PlotLine>();

	// the canvas
	private PlotCanvas _canvas;

	// for tick values
	private int _numDecimalX = 2;
	private int _minExponentX = 2;
	private int _numDecimalY = 2;
	private int _minExponentY = 2;

	// set ranges manually
	private boolean _manualXRange;
	private double _manualXmin;
	private double _manualXmax;

	private boolean _manualYRange;
	private double _manualYmin;
	private double _manualYmax;

	/**
	 * Create plot parameters for a canvas
	 * 
	 * @param canvas the canvas
	 */
	public PlotParameters(PlotCanvas canvas) {
		_canvas = canvas;
	}

	/**
	 * Force the plot to include x = 0
	 * 
	 * @param incZero the flag
	 */
	public void mustIncludeXZero(boolean incZero) {
		_includeXzero = incZero;
	}

	/**
	 * Force the plot to include y = 0
	 * 
	 * @param incZero the flag
	 */
	public void mustIncludeYZero(boolean incZero) {
		_includeYzero = incZero;
	}

	/**
	 * Check whether the legend border is drawn
	 * 
	 * @return <code>true</code> if the legend border is drawn.
	 */
	public boolean isLegendBorder() {
		return _legendBorder;
	}

	/**
	 * Set whether the legend border is drawn
	 * 
	 * @param legBorder <code>true</code> if the legend border is drawn.
	 */
	public void setLegendBorder(boolean legBorder) {
		_legendBorder = legBorder;
	}

	/**
	 * Set the legend fill color
	 * 
	 * @param color the legend fill color
	 */
	public void setLegendBackground(Color color) {
		_legendFillColor = color;
	}

	/**
	 * Get the legend fill color
	 * 
	 * @return the legend fill coor
	 */
	public Color getLegendBackground() {
		return _legendFillColor;
	}

	/**
	 * Set the legend text color
	 * 
	 * @param color the legend text color
	 */
	public void setLegendForeground(Color color) {
		_legendTextColor = color;
	}

	/**
	 * Get the legend text color
	 * 
	 * @return the legend text color
	 */
	public Color getLegendForeground() {
		return _legendTextColor;
	}

	/**
	 * Get the legend border color
	 * 
	 * @return the legend border color
	 */
	public Color getLegendBorderColor() {
		return _legendBorderColor;
	}

	/**
	 * Set the legend border color
	 * 
	 * @param color the legend border color
	 */
	public void setLegendBorderColor(Color color) {
		_legendBorderColor = color;
	}

	/**
	 * Set the title font
	 * 
	 * @param font the new title font
	 */
	public void setTitleFont(Font font) {
		_titleFont = font;
	}

	/**
	 * Get the title font
	 * 
	 * @return the title font
	 */
	public Font getTitleFont() {
		return _titleFont;
	}

	/**
	 * Set the axes font
	 * 
	 * @param font the new axes font
	 */
	public void setAxesFont(Font font) {
		_axesLabelFont = font;
	}

	/**
	 * Get the axes font
	 * 
	 * @return the axes font
	 */
	public Font getAxesFont() {
		return _axesLabelFont;
	}

	/**
	 * Set the status font
	 * 
	 * @param font the new status font
	 */
	public void setStatusFont(Font font) {
		_statusFont = font;
	}

	/**
	 * Get the status font
	 * 
	 * @return the status font
	 */
	public Font getStatusFont() {
		return _statusFont;
	}

	/**
	 * Set the legend font
	 * 
	 * @param font the new legend font
	 */
	public void setLegendFont(Font font) {
		_legendFont = font;
	}

	/**
	 * Get the legend font
	 * 
	 * @return the legend font
	 */
	public Font getLegendFont() {
		return _legendFont;
	}

	/**
	 * Set the legend line length
	 * 
	 * @param legLineLen the line length in pixels
	 */
	public void setLegendLineLength(int legLineLen) {
		_legendLineLength = legLineLen;
	}

	/**
	 * Get the legend line length
	 * 
	 * @return legLineLen the line length in pixels
	 */
	public int getLegendLineLength() {
		return _legendLineLength;
	}

	/**
	 * Check whether we should use x data limits rather than "nice values"
	 * 
	 * @return <code>true</code> we should use x data limits rather than
	 *         "nice values"
	 */
	public boolean useXDataLimits() {
		return _useXDataLimits;
	}

	/**
	 * Set whether we should use x data limits rather than "nice values"
	 * 
	 * @param useDataLim the flag value
	 */
	public void setUseXDataLimits(boolean useDataLim) {
		_useXDataLimits = useDataLim;
	}

	/**
	 * Check whether we should use y data limits rather than "nice values"
	 * 
	 * @return <code>true</code> we should use y data limits rather than
	 *         "nice values"
	 */
	public boolean useYDataLimits() {
		return _useYDataLimits;
	}

	/**
	 * Set whether we should use y data limits rather than "nice values"
	 * 
	 * @param useDataLim the flag value
	 */
	public void setUseYDataLimits(boolean useDataLim) {
		_useYDataLimits = useDataLim;
	}

	/**
	 * Check whether to include x = 0
	 * 
	 * @return <code>true</code> if we should include x = 0
	 */
	public boolean includeXZero() {
		return _includeXzero;
	}

	/**
	 * Check whether to include y = 0
	 * 
	 * @return <code>true</code> if we should include y = 0
	 */
	public boolean includeYZero() {
		return _includeYzero;
	}

	/**
	 * Add a plot line to the plot
	 * 
	 * @param line the line to add
	 */
	public void addPlotLine(PlotLine line) {
		_lines.remove(line);
		_lines.add(line);
	}

	/**
	 * Remove a plot line from the plot
	 * 
	 * @param line the line to remove
	 */
	public void removePlotLine(PlotLine line) {
		_lines.remove(line);
	}

	/**
	 * Get all the plot lines
	 * 
	 * @return all the plot lines
	 */
	public Vector<PlotLine> getPlotLines() {
		return _lines;
	}

	/**
	 * Get the plot title
	 * 
	 * @return the plot title
	 */
	public String getPlotTitle() {
		return _plotTitle;
	}

	/**
	 * Set the plot title
	 * 
	 * @param title the plot title
	 */
	public void setPlotTitle(String title) {
		_canvas.remoteFirePropertyChange(PlotCanvas.TITLECHANGEPROP,
				getPlotTitle(), title);
		_plotTitle = title;
	}

	/**
	 * Get the label for the x axis
	 * 
	 * @return the label for the x axis
	 */
	public String getXLabel() {
		return _xLabel;
	}

	/**
	 * Get the label for the y axis
	 * 
	 * @return the label for the y axis
	 */
	public String getYLabel() {
		return _yLabel;
	}

	/**
	 * Set the x axis label
	 * 
	 * @param label the plot x axis label
	 */
	public void setXLabel(String label) {
		_canvas.remoteFirePropertyChange(PlotCanvas.XLABELCHANGEPROP,
				getXLabel(), label);
		_xLabel = label;
	}

	/**
	 * Set the y axis label
	 * 
	 * @param label the plot y axis label
	 */
	public void setYLabel(String label) {
		_canvas.remoteFirePropertyChange(PlotCanvas.YLABELCHANGEPROP,
				getYLabel(), label);
		_yLabel = label;
	}

	/**
	 * Check whether we should draw a legend
	 * 
	 * @return whether we should draw a legend
	 */
	public boolean legendDrawing() {
		return _drawLegend;
	}

	/**
	 * Set whether we should draw a legend
	 * 
	 * @param draw the new drawing flag
	 */
	public void setLegendDrawing(boolean draw) {
		_drawLegend = draw;
	}

	/**
	 * The number of decimals for tick values on x axis
	 * 
	 * @return the numDecimalX
	 */
	public int getNumDecimalX() {
		return _numDecimalX;
	}

	/**
	 * Set the number of decimals for tick values on x axis
	 * 
	 * @param numDecimalX the numDecimalX to set
	 */
	public void setNumDecimalX(int numDecimalX) {
		_numDecimalX = numDecimalX;
	}

	/**
	 * The exponent where we switch to scientific notation on the x axis
	 * 
	 * @return the minExponentX
	 */
	public int getMinExponentX() {
		return _minExponentX;
	}

	/**
	 * Set the exponent where we switch to scientific notation on the x axis
	 * 
	 * @param minExponentX the minExponentX to set
	 */
	public void setMinExponentX(int minExponentX) {
		_minExponentX = minExponentX;
	}

	/**
	 * The number of decimals for tick values on y axis
	 * 
	 * @return the numDecimalY
	 */
	public int getNumDecimalY() {
		return _numDecimalY;
	}

	/**
	 * Set the number of decimals for tick values on y axis
	 * 
	 * @param numDecimalY the numDecimalY to set
	 */
	public void setNumDecimalY(int numDecimalY) {
		_numDecimalY = numDecimalY;
	}

	/**
	 * The exponent where we switch to scientific notation on the y axis
	 * 
	 * @return the minExponentY
	 */
	public int getMinExponentY() {
		return _minExponentY;
	}

	/**
	 * Set the exponent where we switch to scientific notation on the y axis
	 * 
	 * @param minExponentY the minExponentY to set
	 */
	public void setMinExponentY(int minExponentY) {
		_minExponentY = minExponentY;
	}

	/**
	 * Manually set the x range
	 * 
	 * @param xmin the minimum x
	 * @param xmax the maximum x
	 */
	public void setXRange(double xmin, double xmax) {
		_manualXRange = true;
		_manualXmin = xmin;
		_manualXmax = xmax;
		_canvas.setWorldSystem();
	}

	/**
	 * Manually set the y range
	 * 
	 * @param xmin the minimum y
	 * @param xmax the maximum y
	 */
	public void setYRange(double ymin, double ymax) {
		_manualYRange = true;
		_manualYmin = ymin;
		_manualYmax = ymax;
		_canvas.setWorldSystem();
	}

	/**
	 * Is the X plot range using manual limits
	 * 
	 * @return <code>true</code> if the X plot range using manual limits
	 */
	public boolean manualRangeX() {
		return _manualXRange;
	}

	/**
	 * Is the Y plot range using manual limits
	 * 
	 * @return <code>true</code> if the Y plot range using manual limits
	 */
	public boolean manualRangeY() {
		return _manualYRange;
	}

	/**
	 * Get the minimum value for a manual X range
	 * 
	 * @return the value for a manual X range
	 */
	public double getManualXMin() {
		return _manualXmin;
	}

	/**
	 * Get the maximum value for a manual X range
	 * 
	 * @return the value for a manual X range
	 */
	public double getManualXMax() {
		return _manualXmax;
	}

	/**
	 * Get the minimum value for a manual Y range
	 * 
	 * @return the value for a manual Y range
	 */
	public double getManualYMin() {
		return _manualYmin;
	}

	/**
	 * Get the maximum value for a manual Y range
	 * 
	 * @return the value for a manual Y range
	 */
	public double getManualYMax() {
		return _manualYmax;
	}

	/**
	 * This is called as a result of a save. The object needs to write itself
	 * out in xml.
	 * 
	 * @param write the xml writer
	 */
	@Override
	public void writeXml(XmlPrintStreamWriter writer) {
		try {
			writer.writeStartElement(XmlRootElementName);
			writeXmlMainLabels(writer);
			writeXmlIncludeZero(writer);
			writeLegend(writer);
			writeTickValueDisplay(writer);
			writeManualRangeData(writer);
			writeDataLimitData(writer);

			// tell any plotlines to write themselves
			for (PlotLine plotline : _lines) {
				plotline.writeXml(writer);
			}

			writer.writeEndElement();

		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	// write the info about the plot title and axis labels
	private void writeXmlMainLabels(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlTitleAttName, XmlSupport.validXML(_plotTitle));
		props.put(XmlXLabAttName, XmlSupport.validXML(_xLabel));
		props.put(XmlYLabAttName, XmlSupport.validXML(_yLabel));
		props.put(XmlTitleFontAttName, _titleFont);
		props.put(XmlAxesFontAttName, _axesLabelFont);
		props.put(XmlStatusFontAttName, _statusFont);
		writer.writeElementWithProps(XmlMainLabelElementName, props);
	}

	// write the info about whether we include zeros
	private void writeXmlIncludeZero(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlXZeroAttName, _includeXzero);
		props.put(XmlYZeroAttName, _includeYzero);
		writer.writeElementWithProps(XmlIncZeroElementName, props);
	}

	// write info about the legend
	private void writeLegend(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();

		String fgHex = GraphicsUtilities.colorToHex(_legendFillColor);
		String bgHex = GraphicsUtilities.colorToHex(_legendTextColor);
		String bcHex = GraphicsUtilities.colorToHex(_legendBorderColor);

		props.put(XmlDrawLegendAttName, _drawLegend);
		props.put(XmlLegendFontAttName, _legendFont);
		props.put(XmlLegendLineLengthAttName, _legendLineLength);
		props.put(XmlLegendBorderAttName, _legendBorder);
		props.put(XmlLegendFillColorAttName, fgHex);
		props.put(XmlLegendTextColorAttName, bgHex);
		props.put(XmlLegendBorderColorAttName, bcHex);
		writer.writeElementWithProps(XmlLegendElementName, props);
	}

	// write info about how tick values are displayed
	private void writeTickValueDisplay(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlNumDecXAttName, _numDecimalX);
		props.put(XmlMixExpXAttName, _minExponentX);
		props.put(XmlNumDecYAttName, _numDecimalY);
		props.put(XmlMixExpYAttName, _minExponentY);
		writer.writeElementWithProps(XmlTickValueDisplayElementName, props);
	}

	// write info about data limits as plot limits
	private void writeDataLimitData(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlXUseDataLimitAttName, _useXDataLimits);
		props.put(XmlYUseDataLimitAttName, _useYDataLimits);
		writer.writeElementWithProps(XmlUseDataLimitElementName, props);
	}

	// write info about manual ranges if used
	private void writeManualRangeData(XmlPrintStreamWriter writer)
			throws XMLStreamException {
		Properties props = new Properties();
		props.put(XmlUseManualXAttName, _manualXRange);
		props.put(XmlManualMinXAttName, _manualXmin);
		props.put(XmlManualMaxXAttName, _manualXmax);
		props.put(XmlUseManualYAttName, _manualYRange);
		props.put(XmlManualMinYAttName, _manualYmin);
		props.put(XmlManualMaxYAttName, _manualYmax);
		writer.writeElementWithProps(XmlManualRangeElementName, props);
	}

}
