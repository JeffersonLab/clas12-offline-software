package cnuphys.splot.example;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.GraphicsUtilities;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.SplotMenus;

/**
 * A template class for plot examples
 * 
 * @author heddle
 * 
 */
public abstract class AExample extends JFrame {

	// the plot canvas
	protected PlotCanvas _canvas;

	// the menus and items
	protected SplotMenus _menus;

	public AExample() {
		super("sPlot");

		// Initialize look and feel
		GraphicsUtilities.initializeLookAndFeel();

		System.out.println("Environment: " + Environment.getInstance());

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);

		try {
			_canvas = new PlotCanvas(createDataSet(), getPlotTitle(), getXAxisLabel(), getYAxisLabel());
		}
		catch (DataSetException e) {
			e.printStackTrace();
			return;
		}

		// add the menu bar
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);
		_menus = new SplotMenus(_canvas, mb, true);
		fillData();
		setPreferences();
		final PlotPanel ppanel = new PlotPanel(_canvas);

		ppanel.setPreferredSize(new Dimension(750, 700));

		add(ppanel, BorderLayout.CENTER);

		// add a curve panel
		addEast();
		pack();
		GraphicsUtilities.centerComponent(this);
	}

	/**
	 * Get the plot canvas
	 * 
	 * @return the plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _canvas;
	}

	// add the east component
	private void addEast() {
		// _curveEditor = new CurveEditorPanel(_canvas);
		// add(_curveEditor, BorderLayout.EAST);
	}

	protected abstract DataSet createDataSet() throws DataSetException;

	protected abstract String[] getColumnNames();

	protected abstract String getXAxisLabel();

	protected abstract String getYAxisLabel();

	protected abstract String getPlotTitle();

	// fill the plot data
	public abstract void fillData();

	// set the preferences
	public abstract void setPreferences();

}
