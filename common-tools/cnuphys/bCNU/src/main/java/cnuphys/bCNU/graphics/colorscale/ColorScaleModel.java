package cnuphys.bCNU.graphics.colorscale;

import java.awt.Color;
import java.util.Arrays;

import cnuphys.lund.X11Colors;

/**
 * Used for color scales, such as on magnetic field plots
 * 
 * @author heddle
 * 
 */
public class ColorScaleModel {

	/**
	 * The array of colors
	 */
	protected Color colors[] = null;

	/**
	 * The array of values
	 */
	protected double values[] = null;

	/**
	 * Constant used for "smallness" check
	 */
	private static final double TINY = 1.0e-8;

	// null color
	private static final Color NULLCOLOR = new Color(0, 0, 0, 16);

	// Color returned for a too-small value
	private Color _tooSmallColor = new Color(96, 96, 96, 16);

	// Color returned for a too-big value (default: null)
	private Color _tooBigColor = X11Colors.getX11Color("brown");

	// An extra comment.
	private String _comment = "";

	// Number of decimal points displayed.
	private int _precision = 1;

	// Number of rows of display.
	private int _numberOfRows = 1;

	private Color HOTCOLOR = Color.red;

	/**
	 * This creates a ColorScaleModel for converting a value into a color. The
	 * values array should be sorted in ascending order. In any rate, it will be.
	 * Ideally, there is one more entry in the values array, so that each color fits
	 * in the space between 2 values. However, this is not a requirement. It will
	 * still give sensible results. Also, the value array is NOT assumed to be
	 * equally spaced.
	 * 
	 * @param values the array of values.
	 * @param colors the array of colors.
	 */
	public ColorScaleModel(double values[], Color[] colors) {
		this("", values, colors);
	}

	/**
	 * This creates a ColorScaleModel for converting a value into a color. The
	 * values array should be sorted in ascending order. In any rate, it will be.
	 * Ideally, there is one more entry in the values array, so that each color fits
	 * in the space between 2 values. However, this is not a requirement. It will
	 * still give sensible results. Also, the value array is NOT assumed to be
	 * equally spaced.
	 * 
	 * @param comment an extra comment.
	 * @param values  the array of values.
	 * @param colors  the array of colors.
	 */
	public ColorScaleModel(String comment, double values[], Color[] colors) {
		this(comment, values, colors, 1, 1);
	}

	/**
	 * This creates a ColorScaleModel for converting a value into a color. The
	 * values array should be sorted in ascending order. In any rate, it will be.
	 * Ideally, there is one more entry in the values array, so that each color fits
	 * in the space between 2 values. However, this is not a requirement. It will
	 * still give sensible results. Also, the value array is NOT assumed to be
	 * equally spaced.
	 * 
	 * @param comment      an extra comment.
	 * @param values       the array of values.
	 * @param colors       the array of colors.
	 * @param precision    the number of decimals to display. Use 0 for integers.
	 * @param numberOfRows the number of rows to us displaying the colors.
	 */
	public ColorScaleModel(String comment, double values[], Color[] colors, int precision, int numberOfRows) {
		_comment = comment;
		this.colors = colors;
		Arrays.sort(values);
		this.values = values;
		_precision = precision;
		_numberOfRows = numberOfRows;
		// set the too big color to the last color, by default
		_tooBigColor = colors[colors.length - 1];
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
		return getColor(value, false);
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
	 * @param value                 the for which we want the color.
	 * @param useColorInterpolation if <code>true</code>, use color interpolation.
	 * @return the color corresponding to the value.
	 */
	public Color getColor(double value, boolean useColorInterpolation) {

		if (Double.isNaN(value)) {
			// System.err.println("NULL COLOR");
			return NULLCOLOR;
		}

		double minv = getMinValue();
		double maxv = getMaxValue();

		if (relativeDifference(value, minv) < TINY) {
			return colors[0];
		} else if (relativeDifference(value, maxv) < TINY) {
			return colors[colors.length - 1];
		} else if (value < minv) {
			return _tooSmallColor;
		} else if (value > maxv) {
			return _tooBigColor;
		}

		int index = Arrays.binarySearch(values, value);
		// unlikely, but maybe we are exactly on a value
		if (index >= 0) {
			return colors[index];
		}
		index = -(index + 1); // now the insertion point.

		// fix to round down instead of up
		// index--;

		index = Math.max(0, Math.min(colors.length - 1, index));

		if (index < 0) {
			return _tooSmallColor;
		}
		if (index >= colors.length) {
			return _tooBigColor;
		}

		if (useColorInterpolation) {
			Color c1 = colors[index];
			if (index == (colors.length - 1)) {
				return c1;
			}
			Color c2 = colors[index + 1];
			double v1 = values[index];
			double v2 = values[index + 1];
			double fract = (value - v1) / (v2 - v1);
			return getInterpretedColor(c1, c2, fract);
		} else {
			return colors[index];
		}
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

		double minv = getMinValue();
		double maxv = getMaxValue();

		if (minv == maxv) {
			return 1.0;
		} else {
			return Math.abs((v1 - v2) / (maxv - minv));
		}
	}

	/**
	 * Get the minimum value of the values array.
	 * 
	 * @return the minimum value of the values array.
	 */
	public double getMinValue() {
		return values[0];
	}

	/**
	 * Get the maximum value of the values array.
	 * 
	 * @return the maximum value of the values array.
	 */
	public double getMaxValue() {
		return values[values.length - 1];
	}

	/**
	 * Get the color array.
	 * 
	 * @return the color array.
	 */
	public Color[] getColors() {
		return colors;
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
	 * Get the values array.
	 * 
	 * @return the values array.
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Get the comment of the color scale.
	 * 
	 * @return the color scale comment.
	 */
	public String getComment() {
		return _comment;
	}

	/**
	 * Get the precision used for display.
	 * 
	 * @return the precision used for display.
	 */
	public int getPrecision() {
		return _precision;
	}

	/**
	 * @return the numberOfRows
	 */
	public int getNumberOfRows() {
		return _numberOfRows;
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

		// int r3 = r1 + (int) (fract * (r2 - r1));
		// int g3 = g1 + (int) (fract * (g2 - g1));
		// int b3 = b1 + (int) (fract * (b2 - b1));
		// int a3 = a1 + (int) (fract * (a2 - a1));

		double f = 1.0 / fract;
		double h = 1.0 / (1.0 - fract);

		f = f * f;
		h = h * h;

		double sum = f + h;
		int r3 = (int) ((f * r1 + h * r2) / sum);
		int g3 = (int) ((f * g1 + h * g2) / sum);
		int b3 = (int) ((f * b1 + h * b2) / sum);
		int a3 = (int) ((f * a1 + h * a2) / sum);

		return new Color(r3, g3, b3, a3);
	}

	/**
	 * Get a scale such as you might find on a weather map.
	 * 
	 * @param n the number of "in between colors"
	 * @return a set of colors for a weather map
	 */
	public static Color[] getWeatherMapColors(int n) {

		int r[] = { 151, 89, 45, 63, 64, 43, 39, 116, 241, 238, 234 };
		int g[] = { 0, 36, 61, 97, 179, 131, 144, 185, 177, 125, 42 };
		int b[] = { 163, 176, 170, 255, 205, 116, 10, 45, 0, 0, 15 };

		n = Math.max(2, Math.min(20, n));

		double f = 1.0 / n;

		int len = r.length;
		int colorlen = (len - 1) * n + 1;
		Color colors[] = new Color[colorlen];

		int k = 0;
		for (int i = 0; i < (len - 1); i++) {
			for (int j = 0; j < n; j++) {
				int rr = r[i] + (int) (j * f * (r[i + 1] - r[i]));
				int gg = g[i] + (int) (j * f * (g[i + 1] - g[i]));
				int bb = b[i] + (int) (j * f * (b[i + 1] - b[i]));
				colors[k] = new Color(rr, gg, bb);
				k++;
			}
		}

		colors[(len - 1) * n] = new Color(r[len - 1], g[len - 1], b[len - 1]);
		return colors;
	}

	/**
	 * Get the off scale color
	 * 
	 * @return the hot color
	 */
	public Color getHotColor() {
		return HOTCOLOR;
	}
	
	public static ColorScaleModel createColorModel(double minVal, double maxVal, int numVal, Color start, Color end) {
		int numColor = numVal-1;
		double values[] = new double[numVal];
		Color[] colors = new Color[numColor];
		
	
		double dv = (maxVal - minVal)/(numVal-1);
		for (int i = 0; i < numVal; i++) {
			values[i] = minVal + i*dv;
		}
		
		int r1 = start.getRed();
		int r2 = end.getRed();
		int g1 = start.getGreen();
		int g2 = end.getGreen();
		int b1 = start.getBlue();
		int b2 = end.getBlue();

		double dr = ((double)(r2-r1))/(numColor-1);
		double dg = ((double)(g2-g1))/(numColor-1);
		double db = ((double)(b2-b1))/(numColor-1);
		
		for (int i = 0; i < numColor; i++) {
			int r = Math.min(255, (int)(r1 + i*dr));
			int g = Math.min(255, (int)(g1 + i*dg));
			int b = Math.min(255, (int)(b1 + i*db));
			
			colors[i] = new Color(r, g, b);
		}
		
		return new ColorScaleModel(values, colors);
	}

	/**
	 * Get a monochrome model companion to a color model.
	 * 
	 * @param model the color model.
	 * @return a monochrome model companion
	 */
	public static ColorScaleModel getMonochromeModel(ColorScaleModel model) {
		double values[] = model.values;

		Color[] colors = model.colors;

		int len = colors.length;
		Color mono[] = new Color[len];

		mono[0] = new Color(0f, 0f, 0f);
		mono[len - 1] = new Color(1f, 1f, 1f);
		float del = 1.0f / (len - 1);

		for (int i = 1; i < len - 1; i++) {
			float c = i * del;
			mono[i] = new Color(c, c, c);
		}


		ColorScaleModel mcm = new ColorScaleModel(values, mono);

		mcm.HOTCOLOR = Color.cyan;
		return mcm;
	}

	/**
	 * Create a uniform array of values with one more entry than are in a colors
	 * array.
	 * 
	 * @param colors a colors array
	 * @param min    the min value
	 * @param max    the max value
	 * @return the values array
	 */
	public static double[] uniformValueArray(Color colors[], double min, double max) {
		int len = colors.length;
		double vals[] = new double[len + 1];
		double del = (max - min) / len;

		vals[0] = min;
		vals[len] = max;

		for (int i = 1; i < len; i++) {
			vals[i] = min + i * del;
		}

		return vals;
	}

	public boolean isTooSmall(Color color) {
		return (_tooSmallColor.equals(color));
	}

}
