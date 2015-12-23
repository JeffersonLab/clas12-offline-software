package cnuphys.bCNU.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;

public class HistoGridView extends ScrollableGridView implements MouseMotionListener {

	// the plot items
	protected PlotPanel _plotPanel[][];

	/**
	 * Create a histo grid
	 * 
	 * @param numRow
	 * @param numCol
	 * @param cellWidth
	 * @param cellHeight
	 * @param keyVals
	 */

	protected HistoGridView(int numRow, int numCol, int cellWidth,
			int cellHeight, Object... keyVals) {
		super(numRow, numCol, cellWidth, cellHeight, keyVals);
		_plotPanel = new PlotPanel[numRow][numCol];
	}

	public void clear() {
		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numCol; col++) {
				PlotPanel pp = _plotPanel[row][col];
				if (pp != null) {
					pp.getCanvas().getDataSet().clear();
				}
			}
		}
	}

	/**
	 * Get the plot panel in the given cell
	 * 
	 * @param row the 1-based row
	 * @param col the 1-based column
	 * @return the plot item (might be <code>null</code>);
	 */
	public PlotPanel getPlotPanel(int row, int col) {
		return _plotPanel[row - 1][col - 1];
	}

	/**
	 * Get the histo data for the histogram given cell
	 * 
	 * @param row the 1-based row
	 * @param col the 1-based column
	 * @return the histo data (might be <code>null</code>);
	 */
	public HistoData getHistoData(int row, int col) {
		PlotPanel ppan = getPlotPanel(row, col);
		HistoData hd = null;
		if (ppan != null) {
			DataSet ds = ppan.getCanvas().getDataSet();
			if (ds.is1DHistoSet()) {
				hd = ds.getColumn(0).getHistoData();
			}
		}

		return hd;
	}
	/**
	 * Create a histo grid
	 * 
	 * @param title
	 * @param numRow
	 * @param numCol
	 * @param cellWidth
	 * @param cellHeight
	 * @param screenFraction
	 * @return
	 */
	public static HistoGridView createHistoGridView(String title, int numRow,
			int numCol, int cellWidth, int cellHeight, double screenFraction,
			IHistogramMaker histomaker) {

		int width = numCol * cellWidth;
		int height = numRow * cellHeight;

		final HistoGridView view = new HistoGridView(numRow, numCol, cellWidth,
				cellHeight, PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, false,
				PropertySupport.VISIBLE, true, PropertySupport.HEADSUP, false,
				PropertySupport.TITLE, title, PropertySupport.SCROLLABLE, true,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		screenFraction = Math.max(0.25, Math.min(1.0, screenFraction));
		view.setSize(GraphicsUtilities.screenFraction(screenFraction));

		// the histograms
		if (histomaker != null) {
			for (int row = 1; row <= view._numRow; row++) {
				for (int col = 1; col <= view._numCol; col++) {
					PlotPanel histo = histomaker.addHistogram(row, col,
							view._cellWidth, view._cellHeight);
					if (histo != null) {
						view.addComponent(histo);
						histo.getCanvas().addMouseMotionListener(view);
					}
					view._plotPanel[row - 1][col - 1] = histo;
				}
			}
		}

		return view;
	}

	public static PlotPanel createHistogram(HistoGridView view, int width, int height, String title,
			String xLabel, String yLabel, double minValue, double maxValue,
			int numBin) {

		// no rebinning
		HistoData h1 = new HistoData(null, minValue, maxValue, numBin);

		DataSet data;
		try {
			data = new DataSet(h1);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}
		
		int numXticks = numBin-1;
		while (numXticks > 9) {
			numXticks /= 2;
		}

		PlotCanvas canvas = new PlotCanvas(data, title, xLabel, yLabel);

		canvas.getParameters().setNumDecimalX(0);
		canvas.getParameters().setNumDecimalY(0);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(5);
		canvas.getParameters().setMinExponentX(4);
		canvas.getParameters().setXRange(minValue, maxValue);
		canvas.getParameters().setLegendFont(Fonts.smallFont);
		
		canvas.getPlotTicks().setDrawBinValue(true);
		canvas.getPlotTicks().setNumMajorTickX(numXticks);
		canvas.getPlotTicks().setNumMajorTickY(2);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);
		
		DataSet ds = canvas.getDataSet();
		ds.getCurveStyle(0).setFillColor(X11Colors.getX11Color("dark red"));
		ds.getCurveStyle(0).setLineColor(new Color(0,0,0,32));
		ds.getCurveStyle(0).setFitColor(X11Colors.getX11Color("dodger blue"));
		ds.getCurve(0).getFit().setFitType(FitType.NOLINE);


		PlotPanel ppanel = new PlotPanel(canvas, true);
		ppanel.setColor(X11Colors.getX11Color("alice blue"));
		ppanel.setPreferredSize(new Dimension(width, height));
		return ppanel;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof PlotCanvas) {
			PlotCanvas canvas = (PlotCanvas)(e.getSource());
			
			
//			String s = canvas.getLocationString()  + "  [" + canvas.getParameters().getPlotTitle() + " ]  ";
			String s =  "<html>[" + canvas.getParameters().getPlotTitle() + " ]  ";
			
			DataSet ds = canvas.getDataSet();
			HistoData hd = ds.getColumn(0).getHistoData();
			s += hd.maxBinString();
			
			Point2D.Double wp = new Point2D.Double();
			canvas.localToWorld(e.getPoint(), wp);
			int bin = hd.getBin(wp.x);
			if (bin >= 0) {
				long count = hd.getCount(bin);
				s += "  1-based bin: " + (bin+1) + " count: " + count;
			}

			setStatus(s);
		}
	}

}
