package cnuphys.splot.example;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.plot.GraphicsUtilities;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.SplotMenus;

public abstract class APlotDialog extends JDialog {

	// the plot canvas
	protected PlotCanvas _canvas;

	// the menus and items
	protected SplotMenus _menus;

	protected Properties _properties;

	public APlotDialog(JFrame owner, String title, boolean modal, Properties properties) {
		super(owner, title, modal);
		_properties = properties;
		setLayout(new BorderLayout(4, 4));

		// Initialize look and feel
		GraphicsUtilities.initializeLookAndFeel();

		// System.out.println("Environment: " + Environment.getInstance());

		try {

			DataSet dataSet = createDataSet();
			String plotTitle = getPlotTitle();
			String xLabel = getXAxisLabel();
			String yLabel = getYAxisLabel();
			_canvas = new PlotCanvas(dataSet, plotTitle, xLabel, yLabel);
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

		// ppanel.setPreferredSize(new Dimension(750, 700));

		add(ppanel, BorderLayout.CENTER);

		// add user compoenents
		addNorth();
		addSouth();
		addEast();
		addWest();

		pack();
		GraphicsUtilities.centerComponent(this);
	}

	/**
	 * Add a north component
	 */
	protected void addNorth() {
	}

	/**
	 * Add a south component
	 */
	protected void addSouth() {
	}

	/**
	 * Add an east component
	 */
	protected void addEast() {
	}

	/**
	 * Add a west component
	 */
	protected void addWest() {
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	/**
	 * Get the plot canvas
	 * 
	 * @return the plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _canvas;
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