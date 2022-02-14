package cnuphys.splot.example;

import java.awt.Color;

import com.nr.ran.Normaldev;

import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotParameters;

public class Histo extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		HistoData h1 = new HistoData("Histo 1", 0.0, 100.0, 50);
		return new DataSet(h1);
	}

	@Override
	protected String[] getColumnNames() {
		return null;
	}

	@Override
	protected String getXAxisLabel() {
		return "some measured value";
	}

	@Override
	protected String getYAxisLabel() {
		return "Counts";
	}

	@Override
	protected String getPlotTitle() {
		return "Sample 1D Histogram";
	}

	@Override
	public void fillData() {
		int n = 10000;
		Normaldev normDev;
		double mu = 50.0;
		double sig = 10.0;
		int seed = 33557799;
		normDev = new Normaldev(mu, sig, seed);

		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < n; i++) {
			double y = normDev.dev();
			try {
				ds.add(y);
			}
			catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
		ds.getCurveStyle(0).setBorderColor(Color.black);
		ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);
		PlotParameters params = _canvas.getParameters();
		params.setMinExponentY(6);
		params.setNumDecimalY(0);

		// test gradient
		params.setGradientDrawing(true);
	}

	public static void main(String arg[]) {
		final Histo example = new Histo();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}
}
