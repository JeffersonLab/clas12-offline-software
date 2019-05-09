package cnuphys.splot.example;

import java.util.Collection;

import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;

public class TwoLinesWithErrors extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYEXYE, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X1", "Y1", "E1", "X2", "Y2", "E2" };
		return names;
	}

	@Override
	protected String getXAxisLabel() {
		return "<html>x data  X<SUB>M</SUB><SUP>2</SUP>";
	}

	@Override
	protected String getYAxisLabel() {
		return "<html>y data  Y<SUB>Q</SUB><SUP>2</SUP>";
	}

	@Override
	protected String getPlotTitle() {
		return "<html>Sample Plot X<SUP>2</SUP> vs. Q<SUP>2</SUP>";
	}

	@Override
	public void fillData() {
		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < 15; i++) {
			try {
				if (i < 10) {
					ds.add(i, i + 2 * Math.random(), 2.0 * Math.random(), i + 0.5, 10 - i + 2 * Math.random(),
							2.0 * Math.random());
				}
				else {
					ds.add(i, i + 2 * Math.random(), 2.0 * Math.random());
				}
			}
			catch (DataSetException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// ds.add(i, i);
		}
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.POLYNOMIAL);
		}

		// many options controlled via plot parameters
		PlotParameters params = _canvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);
	}

	public static void main(String arg[]) {
		final TwoLinesWithErrors example = new TwoLinesWithErrors();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
