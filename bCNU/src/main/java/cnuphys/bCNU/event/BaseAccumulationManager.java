package cnuphys.bCNU.event;

import java.awt.Color;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;

public abstract class BaseAccumulationManager implements IAccumulator,
		IPhysicsEventListener {

	// common colorscale
	public static ColorScaleModel colorScaleModel = new ColorScaleModel(
			getAccumulationValues(), getAccumulationColors());

	/**
	 * Get the values array for the plot.
	 * 
	 * @return the values array.
	 */
	private static double getAccumulationValues()[] {

		int len = getAccumulationColors().length + 1;

		double values[] = new double[len];

		double min = 0.0;
		double max = 1.0;
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
	private static Color getAccumulationColors()[] {

		int r[] = { 255, 176, 255, 255, 255, 255, 255, 200, 150 };
		int g[] = { 255, 224, 255, 255, 165, 100, 0, 0, 0 };
		int b[] = { 255, 230, 128, 0, 0, 0, 0, 0, 0 };

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
