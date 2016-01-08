package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BorderFactory;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
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
	
	//the data set
	private DataSet _dataSet;
	
	//the x and y column data
	private ColumnData _colDatX;
	private ColumnData _colDatY;

	public ScatterPlot(DataSet dataSet) {
		super(ScatterPanel.getTitle(dataSet));
		_dataSet = dataSet;
		
		_colDatX = ColumnData.getColumnData(dataSet.getColumnName(0));
		_colDatY = ColumnData.getColumnData(dataSet.getColumnName(1));
		
		_plotPanel = createPlotPanel(_dataSet);
		add(_plotPanel, BorderLayout.CENTER);

	}
	

	private PlotPanel createPlotPanel(DataSet data) {
		
		String xn = data.getColumnName(0);
		String yn = data.getColumnName(1);
		PlotCanvas canvas = new PlotCanvas(data, ScatterPanel.getTitle(data), xn, yn);

		canvas.getParameters().setNumDecimalX(1);
		canvas.getParameters().setNumDecimalY(0);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(5);
		canvas.getParameters().setMinExponentX(4);
		canvas.getParameters().setLegendFont(Fonts.smallFont);
		
		canvas.getPlotTicks().setDrawBinValue(false);
		canvas.getPlotTicks().setNumMajorTickX(4);
		canvas.getPlotTicks().setNumMajorTickY(4);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);
		Collection<DataColumn> ycols = data.getAllColumnsByType(DataColumnType.Y);

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
	public void newClasIoEvent(EvioDataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {

			double valsX[] = _colDatX.getAsDoubleArray();
			double valsY[] = _colDatY.getAsDoubleArray();
			if (valsX == null) {
				warning("null Data Array (X) in ScatterPlot.newClasIoEvent");
				return;
			}
			if (valsY == null) {
				warning("null Data Array (Y) in ScatterPlot.newClasIoEvent");
				return;
			}
			
			int len = valsX.length;
			int lenY = valsY.length;
			
			if (len != lenY) {
				warning("Unequal lenght data arrays in ScatterPlot.newClasIoEvent");
			}
			
			// cuts?
			Vector<ICut> cuts = getCuts();
			for (int i = 0; i < len; i++) {
				
				boolean pass = true;
				
				if (cuts != null) {
					for (ICut cut : cuts) {
						pass = cut.pass(i);
						if (!pass) {
							break;
						}
					}
				}

				if (pass) {
					try {
						_dataSet.add(valsX[i], valsY[i]);
					} catch (DataSetException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	@Override
	protected void clear() {
		_dataSet.clear();
		_plotPanel.getCanvas().needsRedraw(true);
		_errorCount = 0;
	}

	@Override
	protected void saveDefinition(File file) {
	}

}
