package cnuphys.splot.example;

import java.awt.Color;
import java.util.Collection;

import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.HorizontalLine;
import cnuphys.splot.plot.PlotParameters;

public class ErfcTest extends AExample {

	static double _rawdata[] = { 25.0, 1.000900900900901, 0.01733697900764091, 26.0, 1.000900900900901,
			0.01733697900764091, 27.0, 1.000900900900901, 0.01733697900764091, 28.0, 1.000900900900901,
			0.01733697900764091, 29.0, 1.000900900900901, 0.01733697900764091, 30.0, 1.000900900900901,
			0.01733697900764091, 31.0, 1.000900900900901, 0.01733697900764091, 32.0, 1.000900900900901,
			0.01733697900764091, 33.0, 1.000900900900901, 0.01733697900764091, 34.0, 1.000900900900901,
			0.01733697900764091, 35.0, 1.000900900900901, 0.01733697900764091, 36.0, 1.000900900900901,
			0.01733697900764091, 37.0, 1.000900900900901, 0.01733697900764091, 38.0, 1.000900900900901,
			0.01733697900764091, 39.0, 1.000900900900901, 0.01733697900764091, 40.0, 1.000900900900901,
			0.01733697900764091, 41.0, 1.000900900900901, 0.01733697900764091, 42.0, 0.9996996996996997,
			0.017326572656758746, 43.0, 1.0003003003003004, 0.017331776613222914, 44.0, 1.0006006006006005,
			0.017334378005599775, 45.0, 0.9990990990990991, 0.017321367136840526, 46.0, 0.9972972972972973,
			0.017305741182250943, 47.0, 0.9927927927927928, 0.017266614428186246, 48.0, 0.9927927927927928,
			0.017266614428186246, 49.0, 0.9888888888888889, 0.017232632715199488, 50.0, 0.9774774774774775,
			0.017132915105820917, 51.0, 0.9621621621621622, 0.016998164201903792, 52.0, 0.9597597597597598,
			0.016976929759882967, 53.0, 0.9414414414414415, 0.016814135350353657, 54.0, 0.8996996996996997,
			0.016437155775860673, 55.0, 0.8687687687687687, 0.016152136767399645, 56.0, 0.8369369369369369,
			0.015853466923502115, 57.0, 0.7672672672672672, 0.015179281628949083, 58.0, 0.7018018018018019,
			0.014517275633960144, 59.0, 0.6336336336336337, 0.013794215108535558, 60.0, 0.578978978978979,
			0.013185884924985075, 61.0, 0.4921921921921922, 0.01215752701501331, 62.0, 0.4297297297297297,
			0.01135992811974639, 63.0, 0.3483483483483483, 0.010227859679235077, 64.0, 0.26036036036036037,
			0.008842301420021296, 65.0, 0.1990990990990991, 0.007732368282032272, 66.0, 0.15195195195195196,
			0.006755088215736933, 67.0, 0.12252252252252252, 0.006065768731748996, 68.0, 0.09759759759759759,
			0.005413740653849834, 69.0, 0.05795795795795796, 0.004171905101936878, 70.0, 0.04924924924924925,
			0.0038457202627224314, 71.0, 0.030930930930930932, 0.003047715184712378, 72.0, 0.02132132132132132,
			0.002530375307260168, 73.0, 0.014414414414414415, 0.002080541510593246, 74.0, 0.0075075075075075074,
			0.0015015015015015015, 75.0, 0.0045045045045045045, 0.0011630580619241492, 76.0, 0.0036036036036036037,
			0.001040270755296623, 77.0, 0.0015015015015015015, 6.71491885135072E-4, 78.0, 0.0015015015015015015,
			6.71491885135072E-4, 79.0, 9.009009009009009E-4, 5.201353776483115E-4, 80.0, 0, 0, 81.0, 0, 0, 82.0, 0,
			0, };

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
		return "<html>DAC Threshold";
	}

	@Override
	protected String getYAxisLabel() {
		return "<html>Occupancy";
	}

	@Override
	protected String getPlotTitle() {
		return "<html>p4 U1, BCO 128ns, BLR on, low gain, 125 ns, chan 0";
	}

	@Override
	public void fillData() {
		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < _rawdata.length - 2; i += 3) {
			try {
				ds.add(_rawdata[i], _rawdata[i + 1], _rawdata[i + 2]);
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
			dc.getFit().setFitType(FitType.ERFC);
		}
		PlotParameters params = _canvas.getParameters();
		params.addPlotLine(new HorizontalLine(_canvas, 0));
		params.addPlotLine(new HorizontalLine(_canvas, 1));
		params.setLegendDrawing(false);
		_canvas.getDataSet().getCurveStyle(0).setFillColor(new Color(0, 0, 240, 128));
	}

	public static void main(String arg[]) {
		final ErfcTest example = new ErfcTest();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}
}