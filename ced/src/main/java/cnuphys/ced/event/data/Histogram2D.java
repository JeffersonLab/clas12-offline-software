package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
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

	
	public Histogram2D(Histo2DData histoData) {
		super(histoData.getName());
		_histoData = histoData;
		
		_colDatX = ColumnData.getColumnData(histoData.getXName());
		_colDatY = ColumnData.getColumnData(histoData.getYName());

		_plotPanel = createPlotPanel(histoData);
		add(_plotPanel, BorderLayout.CENTER);
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
		
//		System.err.println("MIN X: " + h1.getMinX());
//		System.err.println("MAX X: " + h1.getMaxX());
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


	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		if (ClasIoEventManager.getInstance().isAccumulating()) {

			double valsX[] = _colDatX.getAsDoubleArray();
			double valsY[] = _colDatY.getAsDoubleArray();
			if (valsX == null) {
				warning("null Data Array (X) in Histogram2D.newClasIoEvent");
				return;
			}
			if (valsY == null) {
				warning("null Data Array (Y) in Histogram2D.newClasIoEvent");
				return;
			}

			int lenX = valsX.length;
			int lenY = valsY.length;

			if (lenX != lenY) {
				warning("Unequal lenght data arrays in Histogram2D.newClasIoEvent");
			}
			
			int len = Math.min(lenX, lenY);

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
					_histoData.add(valsX[i], valsY[i]);
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

	@Override
	protected void customWrite(BufferedWriter out) {
		String name = _histoData.getName();
		String xnumBin = "" + _histoData.getNumberBinsX();
		String xMin = "" + _histoData.getMinX();
		String xMax = "" + _histoData.getMaxX();
		String ynumBin = "" + _histoData.getNumberBinsY();
		String yMin = "" + _histoData.getMinY();
		String yMax = "" + _histoData.getMaxY();
		
		String xname = _histoData.getXName();
		String yname = _histoData.getYName();
		
		try {
			writeDelimitted(out, HISTO2DDATA, name, xname, yname, xnumBin, xMin, xMax, ynumBin, yMin, yMax);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the plot type for properties
	 * @return the plot type
	 */
	@Override
	public String getPlotType() {
		return PlotDialog.HISTOGRAM2D;
	}
	

}
