package cnuphys.splot.style;

import java.awt.Color;

public class Styled implements IStyled {

	private static Color _defFillColor = new Color(0, 0, 255, 128);
	private static Color _defBorderColor = new Color(192, 192, 192, 128);
	private static Color _defAuxLineColor = new Color(160, 160, 160, 144);

	// default fill color half-alpha blue
	private Color _fillColor = _defFillColor;

	// default line color is black
	private Color _borderColor = _defBorderColor;

	// default fit color is null--will use line color
	private Color _fitColor = Color.black;

	// default fit line style is solid
	private LineStyle _fitLineStyle = LineStyle.SOLID;

	// auxiliary lines
	// default fit color is null--will use line color
	private Color _auxLineColor = _defAuxLineColor;

	// default auxiliary line style is solid
	private LineStyle _auxLineStyle = LineStyle.DASH;

	// default fit line width is 1
	private float _fitLineWidth = 0.75f;

	// default auxiliary line width is 1
	private float _auxLineWidth = 0.75f;

	// default symbol type = square
	private SymbolType _symbolType = SymbolType.SQUARE;

	// default full width symbol size
	private int _symbolSize = 8;

	// for random combinations
	private static final Color _colors[] = { Color.black, Color.red, Color.blue, Color.gray };
	private static final SymbolType _symbols[] = { SymbolType.SQUARE, SymbolType.CIRCLE, SymbolType.DIAMOND,
			SymbolType.CROSS, SymbolType.DOWNTRIANGLE, SymbolType.UPTRIANGLE };

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
		_borderColor = _fillColor.darker();
		_symbolType = _symbols[index % _symbols.length];
	}

	/**
	 * Create a Styled object
	 * 
	 * @param fillColor    the fill color
	 * @param borderColor  the symbol border color
	 * @param fitLineColor the fit line color
	 * @param auxLineColor the auxiliary line color
	 * @param fitLineStyle the fit line style
	 * @param auxLineStyle the auxiliary line style
	 * @param fitLineWidth the line width for fits
	 * @param fitLineWidth the line width for auxiliary lines
	 * @param symbolType   the symbol type
	 * @param symbolSize   the symbol size
	 */
	public Styled(Color fillColor, Color borderColor, Color fitLineColor, Color auxLineColor, LineStyle fitLineStyle,
			LineStyle auxLineStyle, float fitLineWidth, float auxLineWidth, SymbolType symbolType, int symbolSize) {
		_fillColor = fillColor;
		_borderColor = borderColor;
		_fitLineStyle = fitLineStyle;
		_fitLineWidth = fitLineWidth;
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
	 * Create with all defaults but the given fill color. The link color is set a
	 * little darker or lighter.
	 * 
	 * @param fillColor       the fill color to user.
	 * @param darkerLineColor if true/false make line color darker/lighter than fill
	 *                        color.
	 */
	public Styled(Color fillColor, boolean darkerLineColor) {
		_fillColor = fillColor;
		if (darkerLineColor) {
			_borderColor = fillColor.darker();
		}
		else {
			_borderColor = fillColor.brighter();
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
	public Color getBorderColor() {
		return _borderColor;
	}

	@Override
	public Color getFitLineColor() {
		if (_fitColor == null) {
			return _borderColor;
		}
		return _fitColor;
	}

	@Override
	public LineStyle getFitLineStyle() {
		return _fitLineStyle;
	}

	@Override
	public float getFitLineWidth() {
		return _fitLineWidth;
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
	public void setBorderColor(Color lineColor) {
		_borderColor = lineColor;
	}

	@Override
	public void setFitLineColor(Color fitColor) {
		_fitColor = fitColor;
	}

	/**
	 * Set the line style
	 * 
	 * @param lineStyle the new line style
	 */
	@Override
	public void setFitLineStyle(LineStyle lineStyle) {
		_fitLineStyle = lineStyle;
	}

	/**
	 * Set the line width
	 * 
	 * @param lineWidth the new line width
	 */
	@Override
	public void setFitLineWidth(float lineWidth) {
		_fitLineWidth = lineWidth;
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

	@Override
	public Color getAuxLineColor() {
		return _auxLineColor;
	}

	@Override
	public void setAuxLineColor(Color auxColor) {
		_auxLineColor = auxColor;
	}

	@Override
	public LineStyle getAuxLineStyle() {
		return _auxLineStyle;
	}

	@Override
	public void setAuxLineStyle(LineStyle lineStyle) {
		_auxLineStyle = lineStyle;
	}

	@Override
	public float getAuxLineWidth() {
		return _auxLineWidth;
	}

	@Override
	public void setAuxLineWidth(float lineWidth) {
		_auxLineWidth = lineWidth;

	}
}