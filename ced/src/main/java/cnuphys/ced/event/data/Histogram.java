package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;

public class Histogram extends PlotDialog {

	// the column being binned (unless it is binning an expression)
	private ColumnData _columnData;

	// the expression being binned (unless it is binning a column)
	private String  _namedExpressionName;
    private NamedExpression _namedExpression;

	// the histgram data
	private HistoData _histoData;

	/**
	 * Create an on-the-fly histogram
	 * 
	 * @param histoData
	 */
	public Histogram(HistoData histoData) {
		super(histoData.getName());
		_histoData = histoData;

		// are we dealing with an expression or a column?
		String name = _histoData.getName();
		boolean isColumn = ColumnData.validColumnName(name);

		if (isColumn) {
			_columnData = ColumnData.getColumnData(histoData.getName());
		} else {
			_namedExpressionName = name;
		}

		_plotPanel = createPlotPanel(histoData);
		add(_plotPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Get the NamedExpression which might be null
	 * @return the named expression
	 */
	public NamedExpression getNamedExpression() {
		if (_namedExpression != null) {
			return _namedExpression;
		}
		
		_namedExpression = DefinitionManager.getInstance()
				.getNamedExpression(_namedExpressionName);
		return _namedExpression;
	}


	//create the plot panel
	private PlotPanel createPlotPanel(HistoData h1) {
		DataSet data;
		try {
			data = new DataSet(h1);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}

		int numXticks = Math.min(5, h1.getNumberBins() - 1);

		PlotCanvas canvas = new PlotCanvas(data, h1.getName(), h1.getName(),
				"counts");

		canvas.getParameters().setNumDecimalX(1);
		canvas.getParameters().setNumDecimalY(0);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(5);
		canvas.getParameters().setMinExponentX(4);
		// System.err.println("MIN X: " + h1.getMinX());
		// System.err.println("MAX X: " + h1.getMaxX());
		canvas.getParameters().setXRange(h1.getMinX(), h1.getMaxX());
		canvas.getParameters().setTextFont(Fonts.smallFont);

		canvas.getPlotTicks().setDrawBinValue(false);
		canvas.getPlotTicks().setNumMajorTickX(numXticks);
		canvas.getPlotTicks().setNumMajorTickY(2);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);

		data.getCurveStyle(0).setFillColor(X11Colors.getX11Color("dark red"));
		data.getCurveStyle(0).setLineColor(new Color(0, 0, 0, 32));
		data.getCurveStyle(0).setFitColor(X11Colors.getX11Color("dodger blue"));
		data.getCurve(0).getFit().setFitType(FitType.NOLINE);

		PlotPanel ppanel = new PlotPanel(canvas, PlotPanel.STANDARD);
		ppanel.setColor(X11Colors.getX11Color("alice blue"));

		ppanel.setBorder(BorderFactory.createEtchedBorder());

		return ppanel;
	}

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {

			NamedExpression namedExpression = getNamedExpression();

			int len = getMinLength(_columnData, namedExpression);
			for (int index = 0; index < len; index++) {
				double val = getValue(index, _columnData, namedExpression);
				if (!Double.isNaN(val)) {
					_histoData.add(val);
				}
			}
		}
	}

	@Override
	protected void clear() {
		_histoData.clear();
		_plotPanel.getCanvas().needsRedraw(true);
		_errorCount = 0;
	}

	/**
	 * Get the plot type for properties
	 * 
	 * @return the plot type
	 */
	@Override
	public String getPlotType() {
		return PlotDialog.HISTOGRAM;
	}

	
	@Override
	public void customXml(XmlPrintStreamWriter xmlPrintStreamWriter) {
		writeHistoData(xmlPrintStreamWriter, _histoData);
	}

}
