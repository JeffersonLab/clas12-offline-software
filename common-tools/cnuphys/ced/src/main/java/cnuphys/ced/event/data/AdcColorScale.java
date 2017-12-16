package cnuphys.ced.event.data;

import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;

public class AdcColorScale extends ColorScaleModel {

	private static AdcColorScale _instance;
	
	private AdcColorScale() {
		super(getScaleValues(), ColorScaleModel.getWeatherMapColors(8));
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

		int len = ColorScaleModel.getWeatherMapColors(8).length + 1;

		double values[] = new double[len];

		double min = 0.0;
		double max = 1.001;
		double del = (max - min) / (values.length - 1);
		for (int i = 0; i < values.length; i++) {
			values[i] = i * del;
		}
		return values;
	}


}
