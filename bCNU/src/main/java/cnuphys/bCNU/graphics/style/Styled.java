package cnuphys.bCNU.graphics.style;

import java.awt.Color;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.attributes.Attributes;

/**
 * An adapter for the IStyled interface.
 * 
 * @author heddle
 * 
 */
public class Styled implements IStyled {

	// default fill color half-alpha blue
	private Color _fillColor = new Color(0, 0, 255, 128);

	// default line color is blacl
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
		} else if (darkerLineColor) {
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

	/**
	 * Place clones of the style parameters into the attributes for editing.
	 * 
	 * @param attributes
	 *            the attributes object to stuff.
	 */
	@Override
	public void toAttributes(Attributes attributes) {
		if (attributes == null) {
			return;
		}

		Color fc = clone(_fillColor);
		Color lc = clone(_lineColor);

		fc = (fc != null) ? fc : Attributes.NULLCOLOR;
		lc = (lc != null) ? lc : Attributes.NULLCOLOR;

		// enums and primitives do not have to be cloned
		attributes.add(AttributeType.SYMBOLTYPE, _symbolType);
		attributes.add(AttributeType.LINESTYLE, _lineStyle);
		attributes.add(AttributeType.FILLCOLOR, fc);
		attributes.add(AttributeType.LINECOLOR, lc);
		attributes.add(AttributeType.SYMBOLSIZE, _symbolSize);
		attributes.add(AttributeType.LINEWIDTH, _lineWidth);
	}

	/**
	 * Extract style parameters from an attributes object
	 * 
	 * @param attributes
	 *            the attribute object in question.
	 */
	@Override
	public void fromAttributes(Attributes attributes) {
		if (attributes == null) {
			return;
		}

		_symbolType = attributes.symbolTypeValue(AttributeType.SYMBOLTYPE);
		_lineStyle = attributes.lineStyleValue(AttributeType.LINESTYLE);

		_fillColor = clone(attributes.colorValue(AttributeType.FILLCOLOR));
		_lineColor = clone(attributes.colorValue(AttributeType.LINECOLOR));

		_symbolSize = Math
				.max(3, attributes.intValue(AttributeType.SYMBOLSIZE));
		_lineWidth = Math.max(0,
				Math.min(10, attributes.intValue(AttributeType.LINEWIDTH)));
	}

	// clone a color
	private Color clone(Color c) {
		if (c == null) {
			return null;
		}
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
}
