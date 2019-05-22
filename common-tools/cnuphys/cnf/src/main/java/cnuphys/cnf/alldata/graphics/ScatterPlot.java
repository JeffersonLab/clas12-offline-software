package cnuphys.cnf.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import javax.swing.BorderFactory;

import org.jlab.io.base.DataEvent;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.alldata.DataManager;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.style.SymbolType;

@SuppressWarnings("serial")
public class ScatterPlot extends PlotDialog {

	private static Color fillColor = new Color(255, 0, 0, 96);

	// the data set
	private DataSet _dataSet;
	
	//the plot canvas 
	private PlotCanvas _canvas;

	// the x and y column data
	private ColumnData _colDatX;
	private ColumnData _colDatY;

	// the (alternative) x and y expressions
	private String _namedExpressionNameX;
	private String _namedExpressionNameY;
	private NamedExpression _expressionX;
	private NamedExpression _expressionY;

	/**
	 * Create a Scatter Plot
	 * 
	 * @param dataSet the underlying data set
	 */
	public ScatterPlot(DataSet dataSet) {
		super(ScatterPanel.getTitle(dataSet));
		_dataSet = dataSet;
		
		//create the canvas
		String title = ScatterPanel.getTitle(_dataSet);
		String xname = _dataSet.getColumnName(0);
		String yname = _dataSet.getColumnName(1);

		_canvas = new PlotCanvas(_dataSet, title, xname, yname);

		boolean isColumnX = DataManager.getInstance().validColumnName(xname);
		boolean isColumnY = DataManager.getInstance().validColumnName(yname);

		if (isColumnX) {
			_colDatX = DataManager.getInstance().getColumnData(xname);
		} else {
			_namedExpressionNameX = xname;
		}
		if (isColumnY) {
			_colDatY = DataManager.getInstance().getColumnData(yname);
		} else {
			_namedExpressionNameY = yname;
		}

		_plotPanel = createPlotPanel();
		add(_plotPanel, BorderLayout.CENTER);

	}

	/**
	 * Get the NamedExpression (for X) which might be null
	 * 
	 * @return the named expression
	 */
	public NamedExpression getNamedExpressionX() {
		if (_expressionX != null) {
			return _expressionX;
		}

		_expressionX = DefinitionManager.getInstance().getNamedExpression(_namedExpressionNameX);
		return _expressionX;
	}

	/**
	 * Get the NamedExpression (for Y) which might be null
	 * 
	 * @return the named expression
	 */
	public NamedExpression getNamedExpressionY() {
		if (_expressionY != null) {
			return _expressionY;
		}

		_expressionY = DefinitionManager.getInstance().getNamedExpression(_namedExpressionNameY);
		return _expressionY;
	}

	private PlotPanel createPlotPanel() {

		_canvas.getParameters().setNumDecimalX(1);
		_canvas.getParameters().setNumDecimalY(0);
		_canvas.getParameters().setTitleFont(Fonts.mediumFont);
		_canvas.getParameters().setAxesFont(Fonts.smallFont);
		_canvas.getParameters().setMinExponentY(5);
		_canvas.getParameters().setMinExponentX(4);
		_canvas.getParameters().setTextFont(Fonts.smallFont);

		_canvas.getPlotTicks().setDrawBinValue(false);
		_canvas.getPlotTicks().setNumMajorTickX(4);
		_canvas.getPlotTicks().setNumMajorTickY(4);
		_canvas.getPlotTicks().setNumMinorTickX(0);
		_canvas.getPlotTicks().setNumMinorTickY(0);
		_canvas.getPlotTicks().setTickFont(Fonts.smallFont);
		Collection<DataColumn> ycols = _canvas.getDataSet().getAllColumnsByType(DataColumnType.Y);

		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.NOLINE);
			dc.getStyle().setSymbolType(SymbolType.CIRCLE);
			dc.getStyle().setSymbolSize(4);
			dc.getStyle().setFillColor(fillColor);
			dc.getStyle().setBorderColor(null);
		}

		PlotPanel ppanel = new PlotPanel(_canvas, PlotPanel.STANDARD);
		ppanel.setColor(X11Colors.getX11Color("alice blue"));

		ppanel.setBorder(BorderFactory.createEtchedBorder());

		return ppanel;
	}

	@Override
	public void processEvent(final DataEvent event, boolean isStreaming) {
		if (isStreaming) {

			NamedExpression expX = getNamedExpressionX();
			NamedExpression expY = getNamedExpressionY();

			int lenx = getMinLength(event, _colDatX, expX);
			int leny = getMinLength(event, _colDatY, expY);
			int len = Math.min(lenx, leny);

			for (int index = 0; index < len; index++) {
				double valx = getValue(event, index, _colDatX, expX);
				double valy = getValue(event, index, _colDatY, expY);
				if (!Double.isNaN(valx) && !Double.isNaN(valy)) {
					try {
						_dataSet.add(valx, valy);
					} catch (DataSetException e) {
						e.printStackTrace();
					}
				}

			}
		} // isAccumulating
	}

	@Override
	protected void clear() {
		
				
		_dataSet.clear();
		_plotPanel.getCanvas().needsRedraw(true);
		_errorCount = 0;
	}

	@Override
	public String getPlotType() {
		return PlotDialog.SCATTERPLOT;
	}


}
