package cnuphys.splot.example;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;

public class LineWithXAndYErrors extends AExample {

	public static double x[] = { 3, 4, 5 };
	public static double y[] = { 228.380525, 303.564738, 379.971954 };
	public static double xSig[] = { 0.07, 0.08, 0.09 };
	public static double ySig[] = { 0.218365, 0.227791, 0.222510 };

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYEEXYEE, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X", "Y", "Xerr", "Yerr" };
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
		return "<html>Line with X and Y errors";
	}

	@Override
	public void fillData() {
		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < x.length; i++) {
			try {
				ds.add(x[i], y[i], xSig[i], ySig[i]);
			}
			catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setPreferences() {
	}

	public static void main(String arg[]) {
		final LineWithXAndYErrors example = new LineWithXAndYErrors();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
