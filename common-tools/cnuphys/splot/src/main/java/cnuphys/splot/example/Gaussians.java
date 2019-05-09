package cnuphys.splot.example;

import java.util.Collection;

import cnuphys.splot.fit.FGaussian;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;

public class Gaussians extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYEXYE, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X1", "Y1", "E1", "X2", "Y2", "E2", "X3", "Y3", "E3" };
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

		int numCurve = 3;

		double y[] = new double[numCurve];
		double sig[] = new double[numCurve];
		double x[] = new double[numCurve];

		double a1[] = { 1.0, 1.0, 1.5 };
		double a2[] = { 0.8, 0.9, 1.2, 1.5, 4.0, 0.8 };
		double a3[] = { 0.6, 1.1, 1.5, 1.6, 3.9, 0.7, 1.1, 7.0, 0.6 };

		FGaussian gauss[] = new FGaussian[numCurve];
		gauss[0] = new FGaussian(a1);
		gauss[1] = new FGaussian(a2);
		gauss[2] = new FGaussian(a3);

		int num = 50;
		double dx = 10.0 / num;
		DataSet ds = _canvas.getDataSet();

		for (int i = 0; i < 40; i++) {

			for (int j = 0; j < numCurve; j++) {
				x[j] = i * dx + 0.25 * j * dx;
				y[j] = gauss[j].value(x[j]);
				sig[j] = 0.25 * Math.random();

				// add a linear background
				if (j == 2) {
					y[j] = y[j] + (0.2 + 0.00833 * x[j]);
				}
			}

			try {
				ds.add(x[0], spreadFactor() * y[0], sig[0], x[1], spreadFactor() * y[1], sig[1], x[2],
						spreadFactor() * y[2], sig[2]);
			}
			catch (DataSetException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// ds.add(i, i);
		}
	}

	// introduce some jitter
	private double spreadFactor() {
		return (1.0 + 0.05 * Math.random());
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.GAUSSIANS);
		}
		PlotParameters params = _canvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);

		String extra[] = { "This is an extra string", "This is a longer extra string",
				"This is an even longer extra string" };
		params.setExtraStrings(extra);

	}

	public static void main(String arg[]) {
		final Gaussians example = new Gaussians();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
