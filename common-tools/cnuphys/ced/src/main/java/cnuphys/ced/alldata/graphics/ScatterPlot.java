package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import javax.swing.BorderFactory;

import org.jlab.io.base.DataEvent;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.style.SymbolType;

public class ScatterPlot extends PlotDialog {

	private Color fillColor = new Color(255, 0, 0, 96);

	// the data set
	private DataSet _dataSet;

	// the x and y column data
	private ColumnData _colDatX;
	private ColumnData _colDatY;
	
	//the (alternative) x and y expressions
	private String  _namedExpressionNameX;
	private String  _namedExpressionNameY;
	private NamedExpression _expressionX;
	private NamedExpression _expressionY;

	/**
	 * Create a Scatter Plot
	 * @param dataSet the underlying data set
	 */
	public ScatterPlot(DataSet dataSet) {
		super(ScatterPanel.getTitle(dataSet));
		_dataSet = dataSet;
		
		String xname = dataSet.getColumnName(0);
		String yname = dataSet.getColumnName(1);
		
		boolean isColumnX = DataManager.getInstance().validColumnName(xname);
		boolean isColumnY = DataManager.getInstance().validColumnName(yname);

		if (isColumnX) {
			_colDatX = DataManager.getInstance().getColumnData(xname);
		} else {
			_namedExpressionNameX = xname;
		}
		if (isColumnY) {
			_colDatY =  DataManager.getInstance().getColumnData(yname);
		} else {
			_namedExpressionNameY = yname;
		}

		_plotPanel = createPlotPanel(_dataSet);
		add(_plotPanel, BorderLayout.CENTER);

	}
	
	
	/**
	 * Get the NamedExpression (for X) which might be null
	 * @return the named expression
	 */
	public NamedExpression getNamedExpressionX() {
		if (_expressionX != null) {
			return _expressionX;
		}
		
		_expressionX =  DefinitionManager.getInstance()
				.getNamedExpression(_namedExpressionNameX);
		return _expressionX;
	}
	
	
	/**
	 * Get the NamedExpression (for Y) which might be null
	 * @return the named expression
	 */
	public NamedExpression getNamedExpressionY() {
		if (_expressionY != null) {
			return _expressionY;
		}
		
		_expressionY =  DefinitionManager.getInstance()
				.getNamedExpression(_namedExpressionNameY);
		return _expressionY;
	}



	private PlotPanel createPlotPanel(DataSet data) {

		String xn = data.getColumnName(0);
		String yn = data.getColumnName(1);
		PlotCanvas canvas = new PlotCanvas(data, ScatterPanel.getTitle(data),
				xn, yn);

		canvas.getParameters().setNumDecimalX(1);
		canvas.getParameters().setNumDecimalY(0);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(5);
		canvas.getParameters().setMinExponentX(4);
		canvas.getParameters().setTextFont(Fonts.smallFont);

		canvas.getPlotTicks().setDrawBinValue(false);
		canvas.getPlotTicks().setNumMajorTickX(4);
		canvas.getPlotTicks().setNumMajorTickY(4);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);
		Collection<DataColumn> ycols = data
				.getAllColumnsByType(DataColumnType.Y);

		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.NOLINE);
			dc.getStyle().setSymbolType(SymbolType.CIRCLE);
			dc.getStyle().setSymbolSize(4);
			dc.getStyle().setFillColor(fillColor);
			dc.getStyle().setLineColor(null);
		}

		PlotPanel ppanel = new PlotPanel(canvas, PlotPanel.STANDARD);
		ppanel.setColor(X11Colors.getX11Color("alice blue"));

		ppanel.setBorder(BorderFactory.createEtchedBorder());

		return ppanel;
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			
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
		} //isAccumulating
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

	@Override
	public void customXml(XmlPrintStreamWriter writer) {
		writeDataSetXYXY(writer, _dataSet);
	}

	
	/**
	 * Tests whether this listener is interested in events while accumulating
	 * @return <code>true</code> if this listener is NOT interested in  events while accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return false;
	}

}
