package cnuphys.lund;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Hashtable;

public class LundStyle {

	private static final float _LINEWIDTH = 1.5f;

	// strokes for line drawing
	private static BasicStroke _solid = new BasicStroke(_LINEWIDTH,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private static BasicStroke _dashed = new BasicStroke(_LINEWIDTH,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 8.0f, new float[] {
					10.0f, 10.0f }, 0.0f);
	/**
	 * Hashtable of styles.
	 */
	protected static Hashtable<LundId, LundStyle> styles = new Hashtable<LundId, LundStyle>(
			143);

	private Stroke _stroke;
	private Color _lineColor;
	private Color _fillColor;

	private static final LundStyle _unknownStyle = new LundStyle(Color.darkGray);

	/**
	 * A drawing style for the trajectory
	 * 
	 * @param lundId
	 *            the lund particle id
	 * @param color
	 *            the id color
	 */
	private LundStyle(LundId lundId, Color color) {

		_fillColor = color;
		_lineColor = _fillColor.darker();

		// neutrals are dashed
		if (lundId.getChargeX3() == 0) {
			_stroke = _dashed;
		} else {
			_stroke = _solid;
		}
	}

	private LundStyle(Color lineColor) {
		_stroke = _dashed;
		_lineColor = lineColor;

		int r = _lineColor.getRed();
		int g = _lineColor.getGreen();
		int b = _lineColor.getBlue();

		_fillColor = new Color(r, g, b, 190);
	}

	/**
	 * Get the line color
	 * 
	 * @return the line color
	 */
	public Color getLineColor() {
		return _lineColor;
	}

	/**
	 * Get the fill color
	 * 
	 * @return the fill color
	 */
	public Color getFillColor() {
		return _fillColor;
	}

	/**
	 * Get the stroke
	 * 
	 * @return the drawing stroke
	 */
	public Stroke getStroke() {
		return _stroke;
	}

	/**
	 * Add a lund style into the database
	 * 
	 * @param lundId
	 *            the lund particle id
	 * @param lineColor
	 *            the line color
	 * @return the added style (or if it already existed, taken from the
	 *         database)
	 */
	public static LundStyle addLundStyle(LundId lundId, Color lineColor) {
		LundStyle style = styles.get(lundId);
		if (style == null) {
			style = new LundStyle(lundId, lineColor);
			styles.put(lundId, style);
		}
		return style;
	}

	/**
	 * Get a style for a given lund Id. Returns _unknownStyle if the style
	 * hasn't been added yet or lundId is null.
	 * 
	 * @param lundId
	 *            the Id to match
	 * @return the style
	 */
	public static LundStyle getStyle(LundId lundId) {

		if ((lundId == null) || (styles.get(lundId) == null)) {
			return _unknownStyle;
		}

		return styles.get(lundId);
	}

}
