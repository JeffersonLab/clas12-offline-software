package cnuphys.bCNU.graphics.style;

import java.awt.Color;

public interface IStyled {

	/**
	 * Get the color used for fill the interior area.
	 * 
	 * @return the fill color.
	 */
	public Color getFillColor();

	/**
	 * Set the color used for fill the interior area.
	 * 
	 * @param fillColor
	 *            the fill color.
	 */
	public void setFillColor(Color fillColor);

	/**
	 * Get the color used for line drawing.
	 * 
	 * @return the line color.
	 */
	public Color getLineColor();

	/**
	 * Set the color used for fill the line drawing.
	 * 
	 * @param lineColor
	 *            the fill color.
	 */
	public void setLineColor(Color lineColor);

	/**
	 * Get the style used for drawing lines.
	 * 
	 * @return the line style.
	 */
	public LineStyle getLineStyle();

	/**
	 * Set the style used for drawing lines.
	 * 
	 * @param lineStyle
	 *            the line style.
	 */
	public void setLineStyle(LineStyle lineStyle);

	/**
	 * Get the symbol used for drawing points.
	 * 
	 * @return the symbol used for drawing points.
	 */
	public SymbolType getSymbolType();

	/**
	 * Set the symbol used for drawing points.
	 * 
	 * @param symbolType
	 *            the symbol used for drawing points.
	 */
	public void setSymbolType(SymbolType symbolType);

	/**
	 * Get the line width for drawing lines.
	 * 
	 * @return the line width in pixels.
	 */
	public int getLineWidth();

	/**
	 * Set the line width for drawing lines.
	 * 
	 * @param lineWidth
	 *            the line width in pixels.
	 */
	public void setLineWidth(int lineWidth);

	/**
	 * Get the symbol size (full width) in pixels.
	 * 
	 * @return the symbol size (full width) in pixels.
	 */
	public int getSymbolSize();

	/**
	 * Set symbol size (full width) in pixels.
	 * 
	 * @param symbolSize
	 *            symbol size (full width) in pixels.
	 */
	public void setSymbolSize(int symbolSize);
}
