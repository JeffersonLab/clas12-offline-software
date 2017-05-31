package cnuphys.bCNU.graphics.style;

import java.awt.Color;

/**
 * An adapter for the IStyled interface.
 * 
 * @author heddle
 * 
 */
public class Styled implements IStyled {

	// default fill color half-alpha blue
	private Color _fillColor = new Color(0, 0, 255, 128);

	// default line color is black
	private Color _lineColor = Color.black;

	// default line style is solid
	private LineStyle _lineStyle = LineStyle.SOLID;

	// default line width is 1
	private int _lineWidth = 1;

	// default symbol type = square
	private SymbolType _symbolType = SymbolType.SQUARE;

	// default full width symbol size
	private int _symbolSize = 10;

	/**
	 * Create with all defaults.
	 */
	public Styled() {
	}

	/**
	 * Create with all defaults but the given fill color.
	 * 
	 * @param fillColor
	 *            the fill color to user.
	 */
	public Styled(Color fillColor) {
		_fillColor = fillColor;
	}

	/**
	 * Create with all defaults but the given fill color. The link color is set
	 * a little darker or lighter.
	 * 
	 * @param fillColor
	 *            the fill color to user.
	 * @param darkerLineColor
	 *            if true/false make line color darker/lighter than fill color.
	 */
	public Styled(Color fillColor, boolean darkerLineColor) {
		_fillColor = fillColor;
		if (darkerLineColor) {
			_lineColor = fillColor.darker();
		} else  {
			_lineColor = fillColor.brighter();
		}

	}

	@Override
	public Color getFillColor() {
		return _fillColor;
	}

	@Override
	public Color getLineColor() {
		return _lineColor;
	}

	@Override
	public LineStyle getLineStyle() {
		return _lineStyle;
	}

	@Override
	public int getLineWidth() {
		return _lineWidth;
	}

	@Override
	public SymbolType getSymbolType() {
		return _symbolType;
	}

	@Override
	public void setFillColor(Color fillColor) {
		_fillColor = fillColor;
	}

	@Override
	public void setLineColor(Color lineColor) {
		_lineColor = lineColor;
	}

	@Override
	public void setLineStyle(LineStyle lineStyle) {
		_lineStyle = lineStyle;
	}

	@Override
	public void setLineWidth(int lineWidth) {
		_lineWidth = lineWidth;
	}

	@Override
	public void setSymbolType(SymbolType symbolType) {
		_symbolType = symbolType;
	}

	/**
	 * Get the symbol size (full width) in pixels.
	 * 
	 * @return the symbol size (full width) in pixels.
	 */
	@Override
	public int getSymbolSize() {
		return _symbolSize;
	}

	/**
	 * Set symbol size (full width) in pixels.
	 * 
	 * @param symbolSize
	 *            symbol size (full width) in pixels.
	 */
	@Override
	public void setSymbolSize(int symbolSize) {
		_symbolSize = symbolSize;
	}

}
