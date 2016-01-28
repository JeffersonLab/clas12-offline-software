package cnuphys.splot.style;

import java.awt.Color;

public class Styled implements IStyled {

	// default fill color half-alpha blue
	private Color _fillColor = new Color(0, 0, 255, 128);

	// default line color is black
	private Color _lineColor = Color.black;

	// default fit color is null--will use line color
	private Color _fitColor = null;

	// default line style is solid
	private LineStyle _lineStyle = LineStyle.SOLID;

	// default line width is 1
	private float _lineWidth = 1.0f;

	// default symbol type = square
	private SymbolType _symbolType = SymbolType.SQUARE;

	// default full width symbol size
	private int _symbolSize = 8;

	// for random combinations
	private static final Color _colors[] = { Color.black, Color.red, Color.blue,
			Color.gray };
	private static final SymbolType _symbols[] = { SymbolType.SQUARE,
			SymbolType.CIRCLE, SymbolType.DIAMOND, SymbolType.CROSS,
			SymbolType.DOWNTRIANGLE, SymbolType.UPTRIANGLE };

	/**
	 * Create with all defaults.
	 */
	public Styled() {
	}

	/**
	 * Generate a sort of random style
	 * 
	 * @param index determines the style. Can be any integer
	 */
	public Styled(int index) {
		_fillColor = _colors[index % _colors.length];
		_lineColor = _fillColor.darker();
		_symbolType = _symbols[index % _symbols.length];
	}

	/**
	 * Create a Styled object
	 * 
	 * @param fillColor the fill color
	 * @param lineColor the line color
	 * @param lineStyle the line style
	 * @param lineWidth the line width
	 * @param symbolType the symbol type
	 * @param symbolSize the symbol size
	 */
	public Styled(Color fillColor, Color lineColor, LineStyle lineStyle,
			float lineWidth, SymbolType symbolType, int symbolSize) {
		_fillColor = fillColor;
		_lineColor = lineColor;
		_lineStyle = lineStyle;
		_lineWidth = lineWidth;
		_symbolType = symbolType;
		_symbolSize = symbolSize;

	}

	/**
	 * Create with all defaults but the given fill color.
	 * 
	 * @param fillColor the fill color to user.
	 */
	public Styled(Color fillColor) {
		_fillColor = fillColor;
	}

	/**
	 * Create with all defaults but the given fill color. The link color is set
	 * a little darker or lighter.
	 * 
	 * @param fillColor the fill color to user.
	 * @param darkerLineColor if true/false make line color darker/lighter than
	 *            fill color.
	 */
	public Styled(Color fillColor, boolean darkerLineColor) {
		_fillColor = fillColor;
		if (darkerLineColor) {
			_lineColor = fillColor.darker();
		}
		else {
			_lineColor = fillColor.brighter();
		}

	}

	/**
	 * Get the fill color for the symbols
	 * 
	 * @return the fill color for the symbols
	 */
	@Override
	public Color getFillColor() {
		return _fillColor;
	}

	@Override
	public Color getLineColor() {
		return _lineColor;
	}
	
	@Override
	public Color getFitColor() {
		if (_fitColor == null) {
			return _lineColor;
		}
		return _fitColor;
	}


	@Override
	public LineStyle getLineStyle() {
		return _lineStyle;
	}

	@Override
	public float getLineWidth() {
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
	public void setFitColor(Color fitColor) {
		_fitColor = fitColor;
	}
	
	/**
	 * Set the line style
	 * 
	 * @param lineStyle the new line style
	 */
	@Override
	public void setLineStyle(LineStyle lineStyle) {
		_lineStyle = lineStyle;
	}

	/**
	 * Set the line width
	 * 
	 * @param lineWidth the new line width
	 */
	@Override
	public void setLineWidth(float lineWidth) {
		_lineWidth = lineWidth;
	}

	/**
	 * Set the symbol type
	 * 
	 * @param lineWidth the new symbol type
	 */
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
	 * @param symbolSize symbol size (full width) in pixels.
	 */
	@Override
	public void setSymbolSize(int symbolSize) {
		_symbolSize = symbolSize;
	}
}