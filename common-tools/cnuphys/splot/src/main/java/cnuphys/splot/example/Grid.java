package cnuphys.splot.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.nr.ran.Normaldev;

import cnuphys.splot.fit.FGaussian;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.fit.IValueGetter;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.pdata.StripData;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.GraphicsUtilities;
import cnuphys.splot.plot.HorizontalLine;
import cnuphys.splot.plot.LimitsMethod;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotGrid;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.SymbolType;

@SuppressWarnings("serial")
public class Grid extends JFrame implements IValueGetter {

	private static int NUMPLOTS = 6;
	
	private PlotGrid _plotGrid;
	
	private PlotCanvas[] _canvases = new PlotCanvas[NUMPLOTS];

	private Font _titleFont = Environment.getInstance().getCommonFont(12);
	private Font _statusFont = Environment.getInstance().getCommonFont(9);
	private Font _axesFont = Environment.getInstance().getCommonFont(10);
	private Font _legendFont = Environment.getInstance().getCommonFont(10);

	public Grid() {
		
		super("sPlot");

		// Initialize look and feel
		GraphicsUtilities.initializeLookAndFeel();

		System.out.println("Environment: " + Environment.getInstance());

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		};
		addWindowListener(windowAdapter);

		// add the menu bar
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);
		JMenu fileMenu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		};
		quitItem.addActionListener(al);
		fileMenu.add(quitItem);
		mb.add(fileMenu);

		_plotGrid = new PlotGrid(2, 3);
		add(_plotGrid, BorderLayout.CENTER);

		// add some plots
		for (int index = 0; index < NUMPLOTS; index++) {
			try {
				_canvases[index] = new PlotCanvas(createDataSet(index), getPlotTitle(index), getXAxisLabel(index),
						getYAxisLabel(index));

				fillData(_canvases[index], index);
				setPreferences(_canvases[index], index);

				_plotGrid.addPlotCanvas(_canvases[index]);
			}
			catch (DataSetException e) {
				e.printStackTrace();
				return;
			}
			
			_canvases[index].setWorldSystem();
		}

		// test
//		for (int row = 0; row < 2; row++) {
//			for (int col = 0; col < 3; col++) {
//				System.out.println("Plot at row: " + row + " col: " + col + "  has title: "
//						+ _plotGrid.getPlotCanvas(row, col).getTitle());
//			}
//		}
		sizeToScreen(0.85);
	}

	/**
	 * Size and center a JFrame relative to the screen.
	 *
	 * @param frame          the frame to size.
	 * @param fractionalSize the fraction desired of the screen--e.g., 0.85 for 85%.
	 */
	private void sizeToScreen(double fractionalSize) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		d.width = (int) (fractionalSize * d.width);
		d.height = (int) (fractionalSize * d.height);
		setSize(d);
	}

	//get the dataset based in the plot index
	protected DataSet createDataSet(int index) throws DataSetException {
		switch (index) {
		case 0:
			return new DataSet(DataSetType.XYEXYE, getColumnNames(index));

		case 1:
			return new DataSet(DataSetType.XYEEXYEE, getColumnNames(index));

		case 2:
			StripData sd = new StripData("Memory", 25, this, 2000);
			return new DataSet(sd, "time", "Memory Usage (MB)");

		case 3:
			return new DataSet(DataSetType.XYEXYE, getColumnNames(index));

		case 4:
			HistoData h1 = new HistoData("Histo 1", 0.0, 100.0, 50);
			HistoData h2 = new HistoData("Histo 2", 0.0, 150.0, 50);
			return new DataSet(h1, h2);

		case 5:
			return new DataSet(DataSetType.XYXY, getColumnNames(index));

		}

		return null;
	}

	//get the column names based on plot index
	protected String[] getColumnNames(int index) {
		switch (index) {
		case 0:
			String names0[] = { "X", "Y", "E" };
			return names0;

		case 1:
			String names1[] = { "X", "Y", "Xerr", "Yerr" };
			return names1;

		case 2:
			break;

		case 3:
			String names3[] = { "X1", "Y1", "E1", "X2", "Y2", "E2", "X3", "Y3", "E3" };
			return names3;

		case 4:
			break;

		case 5:
			String names5[] = { "X", "Y" };
			return names5;

		}

		return null;
	}

	//get the x axis label based on plot index
	protected String getXAxisLabel(int index) {
		switch (index) {
		case 0:
			return "<html>DAC Threshold";

		case 1:
			return "<html>x data  X<SUB>M</SUB><SUP>2</SUP>";

		case 2:
			return "Time (s)";

		case 3:
			return "<html>x <b>data</b>";

		case 4:
			return "some measured value";

		case 5:
			return "X Data";

		}

		return null;
	}

	
	//get the y axis label based on index
	protected String getYAxisLabel(int index) {
		switch (index) {
		case 0:
			return "<html>Occupancy";

		case 1:
			return "<html>y data  Y<SUB>Q</SUB><SUP>2</SUP>";

		case 2:
			return "Heap Memory (MB)";

		case 3:
			return "<html>y <b>data</b>";

		case 4:
			return "Counts";

		case 5:
			return "Y Data";

		}

		return null;
	}

	//get the plot title based on index
	protected String getPlotTitle(int index) {
		switch (index) {
		case 0:
			return "<html>p4 U1, BCO 128ns, BLR on, low gain, 125 ns, chan 0";

		case 1:
			return "<html>Line with X and Y errors";

		case 2:
			return "Sample Strip Chart";

		case 3:
			return "<html>Fit to Gaussians";

		case 4:
			return "Sample 1D Histograms";

		case 5:
			return "Scatter Plot";

		}

		return null;
	}

	// fill the plot data based on index
	public void fillData(PlotCanvas canvas, int index) {

		DataSet ds = canvas.getDataSet();

		switch (index) {
		case 0:
			for (int i = 0; i < ErfTest._rawdata.length - 2; i += 3) {
				try {
					double x = ErfTest._rawdata[i];
					double y = ErfTest._rawdata[i+1];
					double e = ErfTest._rawdata[i+2];
	//				System.out.println("Plot [" + index + "]  (x, y, e) = (" + x + ", " + y + ", " + e + ")");
					ds.add(x, y, e);
				}
				catch (DataSetException e) {
					e.printStackTrace();
					System.exit(1);
				}
				// ds.add(i, i);
			}
			break;

		case 1:
			for (int i = 0; i < LineWithXAndYErrors.x.length; i++) {
				try {
					ds.add(LineWithXAndYErrors.x[i], LineWithXAndYErrors.y[i], LineWithXAndYErrors.xSig[i],
							LineWithXAndYErrors.ySig[i]);
				}
				catch (DataSetException e) {
					e.printStackTrace();
				}
			}
			break;

		case 2:
			break;

		case 3:

			int numCurve = 3;

			double y[] = new double[numCurve];
			double sig[] = new double[numCurve];
			double x[] = new double[numCurve];

			double a1[] = { 1.0, 1.0, 1.5 };
			double a2[] = { 0.8, 0.9, 1.2, 1.5, 4.0, 0.8 };
			double a3[] = { 0.6, 1.1, 1.5, 1.6, 3.9, 0.7, 1.1, 7.0, 0.6 };

			FGaussian gauss[] = new FGaussian[numCurve];
			gauss[0] = new FGaussian(a1);
			gauss[1] = new FGaussian(a2);
			gauss[2] = new FGaussian(a3);

			int num = 50;
			double dx = 10.0 / num;

			for (int i = 0; i < 40; i++) {

				for (int j = 0; j < numCurve; j++) {
					x[j] = i * dx + 0.25 * j * dx;
					y[j] = gauss[j].value(x[j]);
					sig[j] = 0.25 * Math.random();

					// add a linear background
					if (j == 2) {
						y[j] = y[j] + (0.2 + 0.00833 * x[j]);
					}
				}

				try {
					ds.add(x[0], spreadFactor() * y[0], sig[0], x[1], spreadFactor() * y[1], sig[1], x[2],
							spreadFactor() * y[2], sig[2]);
				}
				catch (DataSetException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			break;

		case 4:
			int n = 10000;
			Normaldev normDev1;
			double mu = 50.0;
			double ssig = 10.0;
			int seed = 33557799;
			normDev1 = new Normaldev(mu, ssig, seed);

			Normaldev normDev2;
			mu = 100.0;
			ssig = 20.0;
			normDev2 = new Normaldev(mu, ssig, seed);

			for (int i = 0; i < n; i++) {
				double y1 = normDev1.dev();
				double y2 = normDev2.dev();
				try {
					ds.add(y1, y2);
				}
				catch (DataSetException e) {
					e.printStackTrace();
				}
			}
			break;

		case 5:
			for (int i = 0; i < 1000; i++) {
				// demo that the data can be added out of order
				double xx = -0.5 + Math.random();
				double yy = xx + 0.2 * (Math.random() - 0.5);

				try {
					ds.add(xx, yy);
				}
				catch (DataSetException e) {
					e.printStackTrace();
				}
			}
			break;

		}

	}

	// set the preferences
	public void setPreferences(PlotCanvas canvas, int index) {

		DataSet ds = canvas.getDataSet();
		PlotParameters params = canvas.getParameters();

		params.setTitleFont(_titleFont);
		params.setAxesFont(_axesFont);
		params.setStatusFont(_statusFont);
		params.setStatusFont(_legendFont);
		params.setLegendLineLength(40);

		switch (index) {
		case 0:
			Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);
			for (DataColumn dc : ycols) {
				dc.getFit().setFitType(FitType.ERF);
			}
			params.addPlotLine(new HorizontalLine(canvas, 0));
			params.addPlotLine(new HorizontalLine(canvas, 1));
			params.setLegendDrawing(false);
			canvas.getDataSet().getCurveStyle(0).setFillColor(new Color(0, 0, 240, 128));
			break;

		case 1:
			break;

		case 2:
			ds.getCurveStyle(0).setBorderColor(Color.red);
			ds.getCurveStyle(0).setFitLineColor(Color.red);
			ds.getCurveStyle(0).setFillColor(new Color(128, 0, 0, 48));
			ds.getCurveStyle(0).setSymbolType(SymbolType.NOSYMBOL);
			params.setMinExponentY(6);
			params.setNumDecimalY(0);
			params.setXLimitsMethod(LimitsMethod.USEDATALIMITS);
			params.mustIncludeYZero(true);
			break;

		case 3:
			ycols = ds.getAllColumnsByType(DataColumnType.Y);
			for (DataColumn dc : ycols) {
				dc.getFit().setFitType(FitType.GAUSSIANS);
			}
			params.mustIncludeXZero(true);
			params.mustIncludeYZero(true);
			break;

		case 4:
			ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
			ds.getCurveStyle(0).setBorderColor(Color.black);
			ds.getCurveStyle(0).setBorderColor(Color.black);
			ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);

			ds.getCurveStyle(1).setFillColor(new Color(196, 196, 196, 64));
			ds.getCurveStyle(1).setBorderColor(Color.red);
			ds.getCurve(1).getFit().setFitType(FitType.GAUSSIANS);

			params.setMinExponentY(6);
			params.setNumDecimalY(0);
			break;

		case 5:
			Color fillColor = new Color(255, 0, 0, 96);
			ycols = ds.getAllColumnsByType(DataColumnType.Y);

			for (DataColumn dc : ycols) {
				dc.getFit().setFitType(FitType.NOLINE);
				dc.getStyle().setSymbolType(SymbolType.CIRCLE);
				dc.getStyle().setSymbolSize(4);
				dc.getStyle().setFillColor(fillColor);
				dc.getStyle().setBorderColor(null);
			}

			// many options controlled via plot parameters
			params.mustIncludeXZero(true);
			params.mustIncludeYZero(true);
			params.addPlotLine(new HorizontalLine(canvas, 0));
			params.addPlotLine(new VerticalLine(canvas, 0));
			break;

		}
	}

	// introduce some jitter
	private double spreadFactor() {
		return (1.0 + 0.05 * Math.random());
	}

	@Override
	public double value(double x) {
		return 10000 * Math.random();
	}

	public static void main(String arg[]) {
		final Grid example = new Grid();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				example.setVisible(true);
			}
		});
	}

}
