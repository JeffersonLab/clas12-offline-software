package cnuphys.splot.example;

import java.awt.Color;

import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotParameters;

import com.nr.ran.Normaldev;

public class GrowingHisto extends AExample {

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
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
		ds.getCurveStyle(0).setLineColor(Color.black);
		ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);
		PlotParameters params = _canvas.getParameters();
		params.setMinExponentY(6);
		params.setNumDecimalY(0);
	}

	public static void main(String arg[]) {
		final GrowingHisto example = new GrowingHisto();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

		System.err.println("Ready...");

		int n = 10000;
		Normaldev normDev;
		double mu = 50.0;
		double sig = 10.0;
		int seed = 33557799;
		normDev = new Normaldev(mu, sig, seed);

		final DataSet ds = example.getPlotCanvas().getDataSet();

		while (true) {
			try {
				double y = normDev.dev();
				try {
					ds.add(y);
					example.getPlotCanvas().needsRedraw(true);
				} catch (DataSetException e) {
					e.printStackTrace();
				}
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
