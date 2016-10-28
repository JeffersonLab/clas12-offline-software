package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.evio.EvioDataEvent;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.Histo2DData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;

public class Histogram2D extends PlotDialog {
	
	//the histgram data
	private Histo2DData _histoData;
	
	// the x and y column data
	private ColumnData _colDatX;
	private ColumnData _colDatY;

	//the (alternative) x and y expressions
	private String  _namedExpressionNameX;
	private String  _namedExpressionNameY;
	private NamedExpression _expressionX;
	private NamedExpression _expressionY;
	
	public Histogram2D(Histo2DData histoData) {
		super(histoData.getName());
		_histoData = histoData;
		
		String xname = histoData.getXName();
		String yname = histoData.getYName();
		
		boolean isColumnX = ColumnData.validColumnName(xname);
		boolean isColumnY = ColumnData.validColumnName(yname);

		if (isColumnX) {
			_colDatX = ColumnData.getColumnData(xname);
		} else {
			_namedExpressionNameX = xname;
		}
		if (isColumnY) {
			_colDatY = ColumnData.getColumnData(yname);
		} else {
			_namedExpressionNameY = yname;
		}

		_plotPanel = createPlotPanel(histoData);
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


	
	private PlotPanel createPlotPanel(Histo2DData h2) {
		DataSet data;
		try {
			data = new DataSet(h2);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}
		
		PlotCanvas canvas = new PlotCanvas(data, h2.getName(), h2.getXName(), h2.getYName());

		canvas.getParameters().setNumDecimalX(1);
		canvas.getParameters().setNumDecimalY(1);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(4);
		canvas.getParameters().setMinExponentX(4);
		
		canvas.getParameters().setXRange(h2.getMinX(), h2.getMaxX());
		canvas.getParameters().setYRange(h2.getMinY(), h2.getMaxY());
		canvas.getParameters().setTextFont(Fonts.smallFont);
		
		canvas.getParameters().setGradientDrawing(true);
		
		canvas.getPlotTicks().setDrawBinValue(false);
		canvas.getPlotTicks().setNumMajorTickX(5);
		canvas.getPlotTicks().setNumMajorTickY(5);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);
		
		data.getCurve(0).getFit().setFitType(FitType.NOLINE);
		
		PlotPanel ppanel = new PlotPanel(canvas, PlotPanel.STANDARD);
		ppanel.setColor(X11Colors.getX11Color("alice blue"));

		ppanel.setBorder(BorderFactory.createEtchedBorder());
		
		return ppanel;
	}

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event) {
	}
	

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			
			
			NamedExpression expX = getNamedExpressionX();
			NamedExpression expY = getNamedExpressionY();

			
			int lenx = getMinLength(_colDatX, expX);
			int leny = getMinLength(_colDatY, expY);
			int len = Math.min(lenx, leny);
			
			for (int index = 0; index < len; index++) {
				double valx = getValue(index, _colDatX, expX);
				double valy = getValue(index, _colDatY, expY);
				if (!Double.isNaN(valx) && !Double.isNaN(valy)) {
					_histoData.add(valx, valy);
				}
			}
		} //isAccumulating
	}

	@Override
	protected void clear() {
		_histoData.clear();
		_plotPanel.getCanvas().needsRedraw(true);
		_errorCount = 0;
	}

	/**
	 * Get the plot type for properties
	 * @return the plot type
	 */
	@Override
	public String getPlotType() {
		return PlotDialog.HISTOGRAM2D;
	}
	
	@Override
	public void customXml(XmlPrintStreamWriter writer) {
		writeHisto2DData(writer, _histoData);
	}

}
