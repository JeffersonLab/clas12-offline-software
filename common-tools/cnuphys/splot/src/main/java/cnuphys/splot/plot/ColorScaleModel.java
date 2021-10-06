package cnuphys.splot.plot;

import java.awt.Color;

/**
 * Used for color scales, such as on magnetic field plots
 * 
 * @author heddle
 * 
 */
public class ColorScaleModel {

	// a standard color scale
	private static ColorScaleModel _blueToRed;

	/**
	 * The array of colors
	 */
	protected Color _colors[] = null;

	/**
	 * Constant used for "smallness" check
	 */
	private static final double TINY = 1.0e-6;

	// Color returned for a too-small
	private Color _tooSmallColor = new Color(240, 240, 240, 64);

	// Color returned for a too-big value
	private Color _tooBigColor = new Color(64, 64, 64, 64);

	private Color _zeroColor = Color.white;

	private double _minVal;

	private double _maxVal;

	/**
	 * This creates a ColorScaleModel for converting a value into a color.
	 * 
	 * @param minVal the minimum value;
	 * @param maxVal the maximum value the array of colors.
	 */
	public ColorScaleModel(double minVal, double maxVal, Color[] colors) {
		_colors = colors;
		_minVal = minVal;
		_maxVal = maxVal;
	}

	/**
	 * Get a standard blue to red colors scale
	 * 
	 * @return a standard blue to red color scale
	 */
	public static ColorScaleModel blueToRed() {
		if (_blueToRed == null) {
			Color colors[] = {
					// new Color(0, 0, 139),
					// new Color(0, 0, 255),
					// new Color(0, 128, 255),
					new Color(240, 240, 255), new Color(128, 255, 255), new Color(0, 255, 255), new Color(86, 255, 86), //
					new Color(173, 255, 47), new Color(255, 255, 0), new Color(255, 165, 0), new Color(255, 69, 0),
					new Color(255, 0, 0),
					// new Color(139, 0, 0),
					new Color(127, 0, 127) };
			_blueToRed = new ColorScaleModel(0.0, 1.0, colors);
		}

		return _blueToRed;
	}

	/**
	 * Get a color via getColor but add an alpha value
	 * 
	 * @param value the value
	 * @param alpha the alpha value [0..255]
	 * @return the color corresponding to the value.
	 */
	public Color getAlphaColor(double value, int alpha) {
		Color c = getColor(value);
		Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		return color;
	}

	/**
	 * Given a value, returns the corresponding color. Example:
	 * <p>
	 * values: {0, 2, 4, 6, 8}
	 * <p>
	 * colors: {R, G, B, Y}
	 * <p>
	 * then
	 * <p>
	 * value = 1.77 -> R
	 * <p>
	 * value = -1 -> tooSmallColor
	 * <p>
	 * value = 8.01 -> tooBigColor
	 * <p>
	 * value = 4 (exactly) -> G
	 * 
	 * @param value the for which we want the color.
	 * @return the color corresponding to the value.
	 */
	public Color getColor(double value) {

		if (Double.isNaN(value)) {
			// System.err.println("NULL COLOR");
			return null;
		}

		int collen = _colors.length;

		if (relativeDifference(value, _minVal) < TINY) {
			return _zeroColor;
		}
		else if (relativeDifference(value, _maxVal) < TINY) {
			return _colors[collen - 1];
		}
		else if (value < _minVal) {
			return _tooSmallColor;
		}
		else if (value > _maxVal) {
			return _tooBigColor;
		}

		double fract = (value - _minVal) / (_maxVal - _minVal);

		int index = (int) ((collen - 1) * fract);

		if (index < 0) {
			return _tooSmallColor;
		}
		if (index >= collen) {
			return _tooBigColor;
		}

		double partFract = (collen - 1) * fract - index;

		Color c1 = _colors[index];
		Color c2 = _colors[index + 1];

		Color interpColor = getInterpretedColor(c1, c2, partFract);
//		System.err.println(DoubleFormat.doubleFormat(partFract, 3) + 
//				"  red: " + c1.getRed() + ", " + c2.getRed() + ", " + interpColor.getRed() +
//				"  green: " + c1.getGreen() + ", " + c2.getGreen() + ", " + interpColor.getGreen() +
//				"  blue: " + c1.getBlue() + ", " + c2.getBlue() + ", " + interpColor.getBlue());

		return interpColor;
	}

	/**
	 * Compute the fraction difference between two points, normalized to the full
	 * scale.
	 * 
	 * @param v1 one value.
	 * @param v2 the other value.
	 * @return the fractional difference.
	 */
	private double relativeDifference(double v1, double v2) {

		double minv = _minVal;
		double maxv = _maxVal;

		if (minv == maxv) {
			return 1.0;
		}
		else {
			return Math.abs((v1 - v2) / (maxv - minv));
		}
	}

	/**
	 * Get the color array.
	 * 
	 * @return the color array.
	 */
	public Color[] getColors() {
		return _colors;
	}

	/**
	 * Get the too-big color.
	 * 
	 * @return the color used for a "too big" value.
	 */
	public Color getTooBigColor() {
		return _tooBigColor;
	}

	/**
	 * Get the too-small color.
	 * 
	 * @return the color used for a "too small" value.
	 */
	public Color getTooSmallColor() {
		return _tooSmallColor;
	}

	/**
	 * @param tooSmallColor the tooSmallColor to set
	 */
	public void setTooSmallColor(Color tooSmallColor) {
		_tooSmallColor = tooSmallColor;
	}

	/**
	 * @param tooBigColor the tooBigColor to set
	 */
	public void setTooBigColor(Color tooBigColor) {
		_tooBigColor = tooBigColor;
	}

	/**
	 * Get the color based on the fract.
	 * 
	 * @param c1
	 * @param c2
	 * @param fract
	 * @return
	 */
	private Color getInterpretedColor(Color c1, Color c2, double fract) {
		if (fract < 0.02) {
			return c1;
		}
		if (fract > 0.98) {
			return c2;
		}

		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		int a1 = c1.getAlpha();

		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();
		int a2 = c2.getAlpha();

		int r3 = r1 + (int) (fract * (r2 - r1));
		int g3 = g1 + (int) (fract * (g2 - g1));
		int b3 = b1 + (int) (fract * (b2 - b1));
		int a3 = a1 + (int) (fract * (a2 - a1));

		return new Color(r3, g3, b3, a3);
	}

}