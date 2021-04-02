package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

public class PlotParameters {
	
	// for legend
	private Font _textFont = Environment.getInstance().getCommonFont(10);
	private Color _textFillColor = new Color(248, 248, 248, 224);
	private Color _textTextColor = Color.black;
	private Color _textBorderColor = Color.black;

	// for extra text
	private Font _extraFont = Environment.getInstance().getCommonFont(10);
	private Color _extraFillColor = new Color(248, 248, 248, 224);
	private Color _extraTextColor = Color.black;
	private Color _extraBorderColor = Color.black;

	// force include zero on plots?
	private boolean _includeYzero = false;
	private boolean _includeXzero = false;
	
	//how axis limits are determined
	private LimitsMethod _xLimitsMethod = LimitsMethod.ALGORITHMICLIMITS;
	private LimitsMethod _yLimitsMethod = LimitsMethod.ALGORITHMICLIMITS;
	

	// legend related
	private int _legendLineLength = 70;
	private boolean _legendBorder = true;
	private boolean _drawLegend = true;

	// extra text
	private boolean _extraBorder = true;
	private boolean _drawExtra = true;
	private String[] _extraStrings;

	// color scale
	private boolean _drawGradient = false;

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

	// if we set ranges manually
	private double _manualXmin = Double.NaN;
	private double _manualXmax = Double.NaN;

	private double _manualYmin = Double.NaN;
	private double _manualYmax = Double.NaN;

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
	 * Get the extra strings for a second legend like display
	 * 
	 * @return the extra strings
	 */
	public String[] getExtraStrings() {
		return _extraStrings;
	}

	/**
	 * Set the extra strings for a second legend like display
	 * 
	 * @param extraStrings the new extra strings array
	 */
	public void setExtraStrings(String... extraStrings) {
		_extraStrings = extraStrings;
	}

	/**
	 * Check whether the extra text border is drawn
	 * 
	 * @return <code>true</code> if the extra text border is drawn.
	 */
	public boolean isExtraBorder() {
		return _extraBorder;
	}

	/**
	 * Set whether the extra text border is drawn
	 * 
	 * @param extraBorder <code>true</code> if the extra text border is drawn.
	 */
	public void setExtraBorder(boolean extraBorder) {
		_extraBorder = extraBorder;
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
	 * @param color the legendfill color
	 */
	public void setTextBackground(Color color) {
		_textFillColor = color;
	}

	/**
	 * Get the legend fill color
	 * 
	 * @return the legend fill color
	 */
	public Color getTextBackground() {
		return _textFillColor;
	}

	/**
	 * Set the legend text color
	 * 
	 * @param color the legend text color
	 */
	public void setTextForeground(Color color) {
		_textTextColor = color;
	}

	/**
	 * Get the legend text color
	 * 
	 * @return the legend text color
	 */
	public Color getTextForeground() {
		return _textTextColor;
	}

	/**
	 * Get the legend border color
	 * 
	 * @return the legend border color
	 */
	public Color getTextBorderColor() {
		return _textBorderColor;
	}

	/**
	 * Set the legend border color
	 * 
	 * @param color the legend border color
	 */
	public void setTextBorderColor(Color color) {
		_textBorderColor = color;
	}

	/**
	 * Set the legend font
	 * 
	 * @param font the legend font
	 */
	public void setTextFont(Font font) {
		_textFont = font;
	}

	/**
	 * Get the legend font
	 * 
	 * @return the legend font
	 */
	public Font getTextFont() {
		return _textFont;
	}

	/**
	 * Set the extra text fill color
	 * 
	 * @param color the extra text fill color
	 */
	public void setExtraBackground(Color color) {
		_extraFillColor = color;
	}

	/**
	 * Get the extra text fill color
	 * 
	 * @return the extra text fill color
	 */
	public Color getExtraBackground() {
		return _extraFillColor;
	}

	/**
	 * Set the extra text color
	 * 
	 * @param color the extra text color
	 */
	public void setExtraForeground(Color color) {
		_extraTextColor = color;
	}

	/**
	 * Get the extra text color
	 * 
	 * @return the extra text color
	 */
	public Color getExtraForeground() {
		return _extraTextColor;
	}

	/**
	 * Get the extra text border color
	 * 
	 * @return the extra text border color
	 */
	public Color getExtraBorderColor() {
		return _extraBorderColor;
	}

	/**
	 * Set the extra text border color
	 * 
	 * @param color the extra text border color
	 */
	public void setExtraBorderColor(Color color) {
		_extraBorderColor = color;
	}

	/**
	 * Set the extra text font
	 * 
	 * @param font the new extra text font
	 */
	public void setExtraFont(Font font) {
		_extraFont = font;
	}

	/**
	 * Get the extra text font
	 * 
	 * @return the extra text font
	 */
	public Font getExtraFont() {
		return _extraFont;
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
		_canvas.remoteFirePropertyChange(PlotCanvas.TITLECHANGEPROP, getPlotTitle(), title);
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
		_canvas.remoteFirePropertyChange(PlotCanvas.XLABELCHANGEPROP, getXLabel(), label);
		_xLabel = label;
	}

	/**
	 * Set the y axis label
	 * 
	 * @param label the plot y axis label
	 */
	public void setYLabel(String label) {
		_canvas.remoteFirePropertyChange(PlotCanvas.YLABELCHANGEPROP, getYLabel(), label);
		_yLabel = label;
	}

	/**
	 * Check whether we should draw extra text
	 * 
	 * @return whether we should draw the extra text
	 */
	public boolean extraDrawing() {
		return _drawExtra;
	}

	/**
	 * Set whether we should draw the extra text
	 * 
	 * @param draw the new drawing flag
	 */
	public void setExtraDrawing(boolean draw) {
		_drawExtra = draw;
	}

	/**
	 * Check whether we should draw a legend
	 * 
	 * @return whether we should draw a legend
	 */
	public boolean isLegendDrawn() {
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
	 * Check whether we should draw a gradient
	 * 
	 * @return whether we should draw a legend
	 */
	public boolean gradientDrawing() {
		return _drawGradient;
	}

	/**
	 * Set whether we should draw a gradient
	 * 
	 * @param draw the new drawing flag
	 */
	public void setGradientDrawing(boolean draw) {
		_drawGradient = draw;
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
		setXLimitsMethod(LimitsMethod.MANUALLIMITS);
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
		setYLimitsMethod(LimitsMethod.MANUALLIMITS);
		_manualYmin = ymin;
		_manualYmax = ymax;
		_canvas.setWorldSystem();
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
	 * Get the limit method for the x axis
	 * @return the limit method for the x axis
	 */
	public LimitsMethod getXLimitsMethod() {
		return _xLimitsMethod;
	}
	
	/**
	 * Set the limits method for the x axis
	 * 
	 * @param method the method
	 */
	public void setXLimitsMethod(LimitsMethod method) {
		if (_xLimitsMethod != method) {
			_xLimitsMethod = method;
		}
	}
	
	/**
	 * Get the limit method for the y axis
	 * @return the limit method for the y axis
	 */
	public LimitsMethod getYLimitsMethod() {
		return _yLimitsMethod;
	}
	
	/**
	 * Set the limits method for the y axis
	 * 
	 * @param method the method
	 */
	public void setYLimitsMethod(LimitsMethod method) {
		if (_yLimitsMethod != method) {
			_yLimitsMethod = method;
		}
	}


}
