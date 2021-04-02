package cnuphys.adaptiveSwim.test;

import java.awt.Color;

import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotGridDialog;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.UnicodeSupport;
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.LineStyle;


public class LineTestPlotGrid extends PlotGridDialog  {

	private static int _numRow = 3;
	private static int _numCol = 3;
	private static int _width = 400;
	private static int _height = 400;

	private PlotCanvas[][] _canvases;
	private DataSet[][] _dataSets;
	
	private double _accuracy;
	private double _epsilon;
	private double _zTarg;

	public LineTestPlotGrid(double zTarg, double accuracy, double epsilon) {
		super(null, "Swimmer Z Test Plots", false, _numRow, _numCol, _numCol * _width, _numRow * _height);

		_zTarg = zTarg;
		_accuracy = accuracy;
		_epsilon = epsilon;
		
		_canvases = new PlotCanvas[_numRow][_numCol];
		_dataSets = new DataSet[_numRow][_numCol];

		// add the plots

		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numCol; col++) {
				try {
					_dataSets[row][col] = createDataSet(row, col);
					_canvases[row][col] = new PlotCanvas(_dataSets[row][col], getPlotTitle(row, col),
							getXAxisLabel(row, col), getYAxisLabel(row, col));

					setPreferences(_canvases[row][col], row, col);

					_plotGrid.addPlotCanvas(_canvases[row][col]);
				} catch (DataSetException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * Update the plots
	 * 
	 * @param diff
	 */
	public void update(AdaptiveResultDiff diff) {
		try {
			_dataSets[0][0].add(diff.getFinalXDiff());
			_dataSets[0][1].add(diff.getFinalYDiff());
			_dataSets[0][2].add(diff.getFinalZDiff());
			_dataSets[1][0].add(diff.getFinalSDiff());
			_dataSets[1][1].add(diff.getFinalThetaDiff());
			_dataSets[1][2].add(diff.getFinalPhiDiff());
			
			AdaptiveSwimResult  oldSwimRes = diff.result1;
			AdaptiveSwimResult  newSwimRes = diff.result2;
			
			_dataSets[2][0].add(oldSwimRes.finalDeltaZ(_zTarg));
			_dataSets[2][1].add(newSwimRes.finalDeltaZ(_zTarg));
			_dataSets[2][2].add(100.0*diff.getBDLDiff()/(diff.result1.getTrajectory().getComputedBDL()));
		} catch (DataSetException e) {
			e.printStackTrace();
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
		
		params.setExtraStrings(String.format(
				"Accuracy %-6.2gm", _accuracy),
				String.format("Epsilon %-6.2gm", _epsilon));

		params.addPlotLine(new VerticalLine(canvas, 0));

		VerticalLine vline = (new VerticalLine(canvas, 0));
		vline.getStyle().setBorderColor(Color.red);
		vline.getStyle().setFitLineWidth(1.5f);
		vline.getStyle().setFitLineStyle(LineStyle.DOT);
		params.addPlotLine(vline);

		ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
		ds.getCurveStyle(0).setFitLineColor(Color.red);
		ds.getCurveStyle(0).setFitLineWidth(2);
		ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);

		params.setMinExponentY(6);
		params.setNumDecimalY(0);

		params.setMinExponentX(4);
		params.setNumDecimalX(3);

	}

	// create the datasets
	private DataSet createDataSet(int row, int col) throws DataSetException {

		if (row == 0) {
			if (col == 0) {
				HistoData hd = new HistoData("", -0.02, 0.02, 50);
				return new DataSet(hd);
			} else if (col == 1) {
				HistoData hd = new HistoData("", -0.02, 0.02, 50);
				return new DataSet(hd);
			} else if (col == 2) {
				HistoData hd = new HistoData("", -0.02, 0.02, 50);
				return new DataSet(hd);
			} 
		} else if (row == 1) {
			if (col == 0) {
				HistoData hd = new HistoData("", -0.02, 0.02, 50);
				return new DataSet(hd);
			}
			else if (col == 1) {
				HistoData hd = new HistoData("", -0.01, 0.01, 50);
				return new DataSet(hd);
			} else if (col == 2) {
				HistoData hd = new HistoData("", -0.02, 0.02, 50);
				return new DataSet(hd);
			} 
		} else if (row == 2) {
			if (col == 0) {
				HistoData hd = new HistoData("", -0.01, 0.01, 50);
				return new DataSet(hd);
			} else if (col == 1) {
				HistoData hd = new HistoData("", -0.01, 0.01, 50);
				return new DataSet(hd);
			} else if (col == 2) {
				HistoData hd = new HistoData("", -2, 2, 50);
				return new DataSet(hd);
			}
		}

		return null;
	}

	protected String getPlotTitle(int row, int col) {

		if (row == 0) {
			if (col == 0) {
				return "Final X Difference";
			} else if (col == 1) {
				return "Final Y Difference";
			} else if (col == 2) {
				return "Final Z Difference";
			} 
		} else if (row == 1) {
			if (col == 0) {
				return "Final S Difference";
			}
			else if (col == 1) {
				return "Final " + UnicodeSupport.SMALL_THETA + " Difference (deg)";
			} else if (col == 2) {
				return "Final " + UnicodeSupport.SMALL_PHI + " Difference (deg)";
			} 
		} else if (row == 2) {
			if (col == 0) {
				return "Z - Ztarg (Old Swimmer)";
			} else if (col == 1) {
				return "Z - Ztarg (New Swimmer)";
			}
			else if (col == 2) {
				return "BDL Difference";
			}
		}

		return null;
	}

	protected String getXAxisLabel(int row, int col) {
		if (row == 0) {
			if (col == 0) {
				return "Final X Difference (m)";
			} else if (col == 1) {
				return "Final Y Difference (m)";
			} else if (col == 2) {
				return "Final Z Difference (m)";
			} 
		} else if (row == 1) {
			if (col == 0) {
				return "Final S Difference (m)";
			}
			else if (col == 1) {
				return "Final " + UnicodeSupport.SMALL_THETA + " Difference (deg)";
			} else if (col == 2) {
				return "Final " + UnicodeSupport.SMALL_PHI + " Difference (deg)";
			} 
		} else if (row == 2) {
			if (col == 0) {
				return "Z - Ztarg (Old Swimmer) m";
			} else if (col == 1) {
				return "Z - Ztarg (New Swimmer) m";
			} else if (col == 2) {
				return "BDL % Difference";
			}
		}

		return "???";
	}

	protected String getYAxisLabel(int row, int col) {
		return "Counts";
	}

}
