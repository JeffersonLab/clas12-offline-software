package cnuphys.ced.event.plot;

import java.awt.Color;

import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.frame.Ced;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.GrowableArray;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotGridDialog;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.LineStyle;

public class ReconstructionPlotGrid extends PlotGridDialog implements
		IAccumulationListener {

	private static int _numRow = 2;
	private static int _numCol = 3;
	private static int _width = 400;
	private static int _height = 400;

	public ReconstructionPlotGrid() {
		super(Ced.getFrame(), "Reconstruction Plots", false, _numRow,
				_numCol, _numCol * _width, _numRow * _height);

		AccumulationManager.getInstance().addAccumulationListener(this);

		// add the plots

		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numCol; col++) {
				try {
					PlotCanvas canvas = new PlotCanvas(createDataSet(row, col),
							getPlotTitle(row, col), getXAxisLabel(row, col),
							getYAxisLabel(row, col));

					setPreferences(canvas, row, col);

					_plotGrid.addPlotCanvas(canvas);
				} catch (DataSetException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	// set the preferences
	public void setPreferences(PlotCanvas canvas, int row, int col) {

		DataSet ds = canvas.getDataSet();
		PlotParameters params = canvas.getParameters();

		params.setTitleFont(_titleFont);
		params.setAxesFont(_axesFont);
		params.setStatusFont(_statusFont);
		params.setStatusFont(_legendFont);
		params.setLegendLineLength(40);

		VerticalLine vline = (new VerticalLine(canvas, 0));
		vline.getStyle().setLineColor(Color.red);
		vline.getStyle().setLineWidth(1.5f);
		vline.getStyle().setLineStyle(LineStyle.DOT);
		params.addPlotLine(vline);

		ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
		ds.getCurveStyle(0).setLineColor(Color.black);
		ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);

		params.setMinExponentY(6);
		params.setNumDecimalY(0);

	}

	protected DataSet createDataSet(int row, int col) throws DataSetException {

		if (row == 0) {
			if (col == 0) {
				HistoData hd = new HistoData("", -0.25, 0.25, 50);
				return new DataSet(hd);
			} else if (col == 1) {
				HistoData hd = new HistoData("", -2.0, 2.0, 50);
				return new DataSet(hd);
			} else if (col == 2) {
				HistoData hd = new HistoData("", -3, 3, 50);
				return new DataSet(hd);
			}
		} else if (row == 1) {
			if (col == 0) {
				HistoData hd = new HistoData("", -0.25, 0.25, 50);
				return new DataSet(hd);
			} else if (col == 1) {
				HistoData hd = new HistoData("", -5.0, 5.0, 50);
				return new DataSet(hd);
			} else if (col == 2) {
				HistoData hd = new HistoData("", -3, 3, 50);
				return new DataSet(hd);
			}
		}

		return null;
	}

	protected String getPlotTitle(int row, int col) {

		if (row == 0) {
			if (col == 0) {
				return "Time Based Momentum Resolution";
			} else if (col == 1) {
				return "<html>Time Based &theta; Resolution";
			} else if (col == 2) {
				return "<html>Time Based &phi; Resolution";
			}
		} else if (row == 1) {
			if (col == 0) {
				return "Hit Based Momentum Resolution";
			} else if (col == 1) {
				return "<html>Hit Based &theta; Resolution";
			} else if (col == 2) {
				return "<html>Hit Based &phi; Resolution";
			}
		}

		return null;
	}

	protected String getXAxisLabel(int row, int col) {
		return "Fractional Resolution";
	}

	protected String getYAxisLabel(int row, int col) {
		return "Counts";
	}

	@Override
	public void accumulationEvent(int reason) {

		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
//			update();
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
//			update();
			break;

		case AccumulationManager.ACCUMULATION_CLEAR:
			System.err.println("CLEAR ");
			for (int row = 0; row < _numRow; row++) {
				for (int col = 0; col < _numCol; col++) {
					DataSet ds = _plotGrid.getDataSet(row, col);
					if (ds != null) {
						ds.clear();
					}
				}
			}
			break;

		}

	}

//	private void update() {
//
//		DataSet dataSet;
//		GrowableArray data;
//
//		// time based momentum resolution
//		dataSet = _plotGrid.getDataSet(0, 0);
//		data = AccumulationManager.getInstance().getTBMomentumResolutionData();
//		add(dataSet, data);
//
//		// time based theta resolution
//		dataSet = _plotGrid.getDataSet(0, 1);
//		data = AccumulationManager.getInstance().getTBThetaResolutionData();
//		add(dataSet, data);
//
//		// time based phi resolution
//		dataSet = _plotGrid.getDataSet(0, 2);
//		data = AccumulationManager.getInstance().getTBPhiResolutionData();
//		add(dataSet, data);
//
//		// hit based momentum resolution
//		dataSet = _plotGrid.getDataSet(1, 0);
//		data = AccumulationManager.getInstance().getHBMomentumResolutionData();
//		add(dataSet, data);
//
//		// hit based theta resolution
//		dataSet = _plotGrid.getDataSet(1, 1);
//		data = AccumulationManager.getInstance().getHBThetaResolutionData();
//		add(dataSet, data);
//
//		// hit based phi resolution
//		dataSet = _plotGrid.getDataSet(1, 2);
//		data = AccumulationManager.getInstance().getHBPhiResolutionData();
//		add(dataSet, data);
//
//	}

	private void add(DataSet ds, GrowableArray ga) {
		if ((ds == null) || (ga == null)) {
			return;
		}

		for (int i = 0; i < ga.size(); i++) {
			try {
				ds.add(ga.get(i));
			} catch (DataSetException e) {
				e.printStackTrace();
			}
		}

	}
}
