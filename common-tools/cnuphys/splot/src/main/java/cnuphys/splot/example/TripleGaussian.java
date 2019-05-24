package cnuphys.splot.example;

import java.util.Collection;

import cnuphys.splot.fit.FGaussian;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.HorizontalLine;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.VerticalLine;

public class TripleGaussian extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYEXYE, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X", "Y", "E" };
		return names;
	}

	@Override
	protected String getXAxisLabel() {
		return "<html>x <b>data</b>";
	}

	@Override
	protected String getYAxisLabel() {
		return "<html>y <b>data</b>";
	}

	@Override
	protected String getPlotTitle() {
		return "<html>Fit to Gaussians";
	}

	@Override
	public void fillData() {

		double aa[] = { 0.6, 1.1, 1.5, 1.6, 3.9, 0.7, 1.1, 7.0, 0.6 };

		FGaussian gauss = new FGaussian(aa);

		int num = 50;
		double dx = 10.0 / num;
		DataSet ds = _canvas.getDataSet();

		for (int i = 0; i < 50; i++) {

			double x = i * dx;
			double y = gauss.value(x);
			double sig = 0.25 * Math.random();

			try {
				ds.add(x, -spreadFactor() * y, sig);
			}
			catch (DataSetException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// ds.add(i, i);
		}
	}

	private double spreadFactor() {
		return (1.0 + 0.05 * Math.random());
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.GAUSSIANS);
			dc.getFit().setNumGaussian(3);
		}

		// many options controlled via plot parameters
		PlotParameters params = _canvas.getParameters();
		params.addPlotLine(new HorizontalLine(_canvas, 0));
		params.addPlotLine(new VerticalLine(_canvas, 0));
	}

	public static void main(String arg[]) {
		final TripleGaussian example = new TripleGaussian();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
