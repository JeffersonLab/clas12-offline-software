package cnuphys.bCNU.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.toolbar.CommonToolBar;

public class HistoGridView extends ScrollableGridView implements MouseListener, MouseMotionListener {

	// the plot items
	protected PlotPanel _plotPanel[][];

	// which 1-based cell is selected
	protected PlotCanvas _hotCanvas;

	private static final Color selectedColor = Color.red;
	private static final Color unselectedColor = X11Colors.getX11Color("wheat");

	/**
	 * Create a histo grid
	 * 
	 * @param numRow
	 * @param numCol
	 * @param cellWidth
	 * @param cellHeight
	 * @param keyVals
	 */

	protected HistoGridView(int numRow, int numCol, int cellWidth, int cellHeight, Object... keyVals) {
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
	public static HistoGridView createHistoGridView(String title, int numRow, int numCol, int cellWidth, int cellHeight,
			double screenFraction, IPlotMaker histomaker) {

		int width = numCol * cellWidth;
		int height = numRow * cellHeight;

		int tbarbits = BaseToolBar.NODRAWING & ~BaseToolBar.TEXTFIELD & ~BaseToolBar.CONTROLPANELBUTTON
				& ~BaseToolBar.RECTGRIDBUTTON & ~BaseToolBar.MAGNIFYBUTTON & ~BaseToolBar.PANBUTTON
				& ~BaseToolBar.RANGEBUTTON & ~BaseToolBar.CENTERBUTTON & ~BaseToolBar.UNDOZOOMBUTTON
				& ~BaseToolBar.TEXTBUTTON & ~BaseToolBar.DELETEBUTTON;

		BaseContainer container = new BaseContainer(new Rectangle2D.Double(0, 0, 1, 1)) {
			@Override
			public void scale(double scaleFactor) {
				PlotCanvas canvas = ((HistoGridView) (getView()))._hotCanvas;
				if (canvas != null) {
					canvas.scale(scaleFactor);
				}
			}

			@Override
			public void restoreDefaultWorld() {
				PlotCanvas canvas = ((HistoGridView) (getView()))._hotCanvas;
				if (canvas != null) {
					canvas.setWorldSystem();
					canvas.repaint();
				}
			}

			/**
			 * The active toolbar button changed.
			 * 
			 * @param activeButton the new active button.
			 */
			@Override
			public void activeToolBarButtonChanged(ToolBarToggleButton activeButton) {
				((HistoGridView) (getView())).resetPlotToolbars(activeButton == getToolBar().getBoxZoomButton());
			}

			/**
			 * Have you handled the print button so the default action is ignored.
			 * 
			 * @return <code>true</code> if the printer button was handled.
			 */
			@Override
			public boolean handledPrint() {
				PlotCanvas canvas = ((HistoGridView) (getView()))._hotCanvas;
				if (canvas != null) {
					canvas.print();
				}

				return true;
			}

			/**
			 * Have you handled the camera button so the default action is ignored.
			 * 
			 * @return <code>true</code> if the camera button was handled.
			 */
			@Override
			public boolean handledCamera() {
				PlotCanvas canvas = ((HistoGridView) (getView()))._hotCanvas;
				if (canvas != null) {
					canvas.takePicture();
				}

				return true;
			}

		};

		final HistoGridView view = new HistoGridView(numRow, numCol, cellWidth, cellHeight, PropertySupport.WIDTH,
				width, PropertySupport.CONTAINER, container, PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR,
				true, PropertySupport.TOOLBARBITS, tbarbits, PropertySupport.VISIBLE, true, PropertySupport.TITLE,
				title, PropertySupport.SCROLLABLE, true, PropertySupport.STANDARDVIEWDECORATIONS, true);

		screenFraction = Math.max(0.25, Math.min(1.0, screenFraction));
		view.setSize(GraphicsUtilities.screenFraction(screenFraction));

		// the histograms
		if (histomaker != null) {
			for (int row = 1; row <= view._numRow; row++) {
				for (int col = 1; col <= view._numCol; col++) {
					PlotPanel histo = histomaker.addPlot(row, col, view._cellWidth, view._cellHeight);
					if (histo != null) {
						view.addComponent(histo);
						histo.getCanvas().addMouseMotionListener(view);
						histo.getCanvas().addMouseListener(view);
						histo.getCanvas().getParent().setBackground(unselectedColor);
					} else {
						view.addComponent(new JLabel("empty cell"));
					}
					view._plotPanel[row - 1][col - 1] = histo;
				}
			}
		}

		return view;
	}

	public static PlotPanel createHistogram(HistoGridView view, int width, int height, String title, String xLabel,
			String yLabel, double minValue, double maxValue, int numBin) {

		// no rebinning
		HistoData h1 = new HistoData(null, minValue, maxValue, numBin);

		DataSet data;
		try {
			data = new DataSet(h1);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}

		int numXticks = Math.min(5, numBin - 1);

		PlotCanvas canvas = new PlotCanvas(data, title, xLabel, yLabel);

		canvas.getParameters().setNumDecimalX(0);
		canvas.getParameters().setNumDecimalY(0);
		canvas.getParameters().setTitleFont(Fonts.mediumFont);
		canvas.getParameters().setAxesFont(Fonts.smallFont);
		canvas.getParameters().setMinExponentY(5);
		canvas.getParameters().setMinExponentX(4);
		canvas.getParameters().setXRange(minValue, maxValue);
		canvas.getParameters().setTextFont(Fonts.smallFont);

		canvas.getPlotTicks().setDrawBinValue(true);
		canvas.getPlotTicks().setNumMajorTickX(numXticks);
		canvas.getPlotTicks().setNumMajorTickY(2);
		canvas.getPlotTicks().setNumMinorTickX(0);
		canvas.getPlotTicks().setNumMinorTickY(0);
		canvas.getPlotTicks().setTickFont(Fonts.smallFont);

		DataSet ds = canvas.getDataSet();
		ds.getCurveStyle(0).setFillColor(X11Colors.getX11Color("dark red"));
		ds.getCurveStyle(0).setBorderColor(new Color(0, 0, 0, 32));
		ds.getCurveStyle(0).setFitLineColor(X11Colors.getX11Color("dodger blue"));
		ds.getCurve(0).getFit().setFitType(FitType.NOLINE);

		PlotPanel ppanel = new PlotPanel(canvas, PlotPanel.VERYBARE);
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
			PlotCanvas canvas = (PlotCanvas) (e.getSource());

//			String s = canvas.getLocationString()  + "  [" + canvas.getParameters().getPlotTitle() + " ]  ";
			String s = "<html>[" + canvas.getParameters().getPlotTitle() + " ]  ";

			DataSet ds = canvas.getDataSet();
			HistoData hd = ds.getColumn(0).getHistoData();
			s += hd.maxBinString();

			Point2D.Double wp = new Point2D.Double();
			canvas.localToWorld(e.getPoint(), wp);
			int bin = hd.getBin(wp.x);
			if (bin >= 0) {
				long count = hd.getCount(bin);
				s += "  1-based bin: " + (bin + 1) + " count: " + count;
			}

			setStatus(s);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if (!isPointerButtonActive()) {
			return;
		}

		if (e.getClickCount() == 1) {

			if (_hotCanvas != null) {
				_hotCanvas.getParent().setBackground(unselectedColor);
			}
			PlotCanvas canvas = (PlotCanvas) (e.getSource());
			if (canvas != _hotCanvas) {
				canvas.getParent().setBackground(selectedColor);
				_hotCanvas = canvas;
			} else {
				_hotCanvas = null;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Check whether the pointer bar is active on the tool bar
	 * 
	 * @return <code>true</code> if the Pointer button is active.
	 */
	protected boolean isPointerButtonActive() {
		ToolBarToggleButton mtb = getContainer().getActiveButton();
		return (mtb == getContainer().getToolBar().getPointerButton());
	}

	protected void resetPlotToolbars(boolean boxZoom) {
		String s = (boxZoom ? CommonToolBar.BOXZOOM : null);
		for (int row = 0; row < _numRow; row++) {
			for (int col = 0; col < _numCol; col++) {
				PlotPanel ppan = _plotPanel[row][col];
				if (ppan != null) {
					ppan.setSelectedToggle(s);
				}
			}
		}
	}

}
