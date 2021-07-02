package cnuphys.splot.example;

import java.awt.Color;

import cnuphys.splot.fit.IValueGetter;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.StripData;
import cnuphys.splot.plot.LimitsMethod;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.style.SymbolType;

public class StripChart extends AExample implements IValueGetter {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		StripData sd = new StripData("Memory", 25, this, 2000);
		return new DataSet(sd, "time", "Memory Usage (MB)");
	}

	@Override
	protected String[] getColumnNames() {
		return null;
	}

	@Override
	protected String getXAxisLabel() {
		return "Time (s)";
	}

	@Override
	protected String getYAxisLabel() {
		return "Heap Memory (MB)";
	}

	@Override
	protected String getPlotTitle() {
		return "Sample Strip Chart";
	}

	@Override
	public void fillData() {
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setFitLineColor(Color.red);
		ds.getCurveStyle(0).setFillColor(new Color(128, 0, 0, 48));
		ds.getCurveStyle(0).setSymbolType(SymbolType.NOSYMBOL);
		PlotParameters params = _canvas.getParameters();
		params.setMinExponentY(6);
		params.setNumDecimalY(2);
		params.setXLimitsMethod(LimitsMethod.USEDATALIMITS);
		params.mustIncludeYZero(true);
	}

	@Override
	public double value(double x) {
		return 10000 * Math.random();
	}

	public static void main(String arg[]) {
		final StripChart example = new StripChart();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
