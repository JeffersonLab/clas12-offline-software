package cnuphys.splot.example;

import java.util.Collection;

import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;

public class OneXThreeY extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYY, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X", "Y1", "Y2", "Y3" };
		return names;
	}

	@Override
	protected String getXAxisLabel() {
		return "Shared X Data";
	}

	@Override
	protected String getYAxisLabel() {
		return "Three Y's Sharing X";
	}

	@Override
	protected String getPlotTitle() {
		return "Shared X Data";
	}

	@Override
	public void fillData() {
		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < 100; i++) {
			// demo that the data can be added out of order
			double x = Math.random();

			try {
				ds.add(x, x, x * x, x * x * x);
			}
			catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);

		int order = 1;
		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.POLYNOMIAL);
			dc.getFit().setPolynomialOrder(order++);
		}

		// many options controlled via plot parameters
		PlotParameters params = _canvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);
	}

	public static void main(String arg[]) {
		final OneXThreeY example = new OneXThreeY();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
