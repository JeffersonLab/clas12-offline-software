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
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.SymbolType;

public class Scatter extends AExample {

	@Override
	protected DataSet createDataSet() throws DataSetException {
		return new DataSet(DataSetType.XYXY, getColumnNames());
	}

	@Override
	protected String[] getColumnNames() {
		String names[] = { "X", "Y" };
		return names;
	}

	@Override
	protected String getYAxisLabel() {
		return "Y Data";
	}

	@Override
	protected String getPlotTitle() {
		return "Scatter Plot";
	}

	@Override
	protected String getXAxisLabel() {
		return "X Data";
	}

	@Override
	public void fillData() {
		DataSet ds = _canvas.getDataSet();
		for (int i = 0; i < 1000; i++) {
			// demo that the data can be added out of order
			double x = -0.5 + Math.random();
			double y = x + 0.2 * (Math.random() - 0.5);

			try {
				ds.add(x, y);
			}
			catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setPreferences() {
		Color fillColor = new Color(255, 0, 0, 96);
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);

		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.LINE);
			dc.getStyle().setSymbolType(SymbolType.CIRCLE);
			dc.getStyle().setSymbolSize(4);
			dc.getStyle().setFillColor(fillColor);
			dc.getStyle().setBorderColor(null);
			dc.getStyle().setFitLineColor(Color.black);
			dc.getStyle().setFitLineWidth(2.0f);
		}

		// many options controlled via plot parameters
		PlotParameters params = _canvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);
		params.addPlotLine(new HorizontalLine(_canvas, 0));
		params.addPlotLine(new VerticalLine(_canvas, 0));
		params.setLegendDrawing(true);
	}

	public static void main(String arg[]) {
		final Scatter example = new Scatter();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});

	}

}
