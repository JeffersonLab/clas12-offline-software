package cnuphys.ced.event.data;

import java.awt.Color;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;

public class AdcColorScale extends ColorScaleModel {

	private static AdcColorScale _instance;
	
	private AdcColorScale() {
		super(getScaleValues(), getScaleColors());
	}
	
	public static AdcColorScale getInstance() {
		if (_instance == null) {
			_instance = new AdcColorScale();
		}
		return _instance;
	}
	

	/**
	 * Get the values array for the color scale. Note the range is 0..1 so use
	 * fraction of max value to get color
	 * 
	 * @return the values array.
	 */
	private static double[] getScaleValues() {

		int len = getScaleColors().length + 1;

		double values[] = new double[len];

		double min = 0.0;
		double max = 1.001;
		double del = (max - min) / (values.length - 1);
		for (int i = 0; i < values.length; i++) {
			values[i] = i * del;
		}
		return values;
	}

	/**
	 * Get the color array for the plot.
	 * 
	 * @return the color array for the plot.
	 */
	private static Color[] getScaleColors() {
		int r[] = { 196, 173, 255, 255, 255, 255, 173 };
		int g[] = { 255, 255, 255, 165, 69, 0, 0 };
		int b[] = { 255, 128, 0, 0, 0, 0, 0 };

		int n = 8;

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
}
