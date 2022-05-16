package cnuphys.splot.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cnuphys.splot.fit.IValueGetter;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.StripData;
import cnuphys.splot.style.SymbolType;

public class StripChartPanel extends JPanel {

	protected PlotCanvas _canvas;
	
	private IValueGetter _getter;
	private int _capacity;
	private long _interval;
	
	// title related
	private JLabel _titleLabel;

	// axes labels
	private JLabel _xLabel;
	private JLabel _yLabel;

	
	/**
	 * Create a plot panel for a single xy dataset and a toolbar
	 * 
	 * @param dataSet   the data set
	 * @param plotTitle the title of the plot
	 */
	public StripChartPanel(String title, String xLabel, String yLabel, IValueGetter getter, int capacity, long interval) {
		
		_getter = getter;
		_capacity = capacity;
		_interval = interval;
		
		try {
			_canvas = new PlotCanvas(createDataSet(), title, xLabel, yLabel);
		}
		catch (DataSetException e) {
			e.printStackTrace();
			return;
		}
		
		_canvas.setParent(this);
		
		Environment.getInstance().commonize(this, null);
		setLayout(new BorderLayout(0, 0));

		add(_canvas, BorderLayout.CENTER);

		addSouth();
		addNorth();
		addWest();

		
		setPreferences();
	}
	
	private void addSouth() {
		// south panel for x axis and status
		JPanel spanel = new JPanel();
		Environment.getInstance().commonize(spanel, null);

		spanel.setLayout(new BorderLayout());
		spanel.setOpaque(true);
		spanel.setBackground(Color.white);
		PlotParameters parameters = _canvas.getParameters();

		// axes labels
		_xLabel = makeJLabel(parameters.getXLabel(), parameters.getAxesFont(), SwingConstants.CENTER, Color.white, null,
				false);
		_xLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		spanel.add(_xLabel, BorderLayout.CENTER);

		add(spanel, BorderLayout.SOUTH);
	}

	// add the north component
	private void addNorth() {
		PlotParameters parameters = _canvas.getParameters();


		JPanel npanel = new JPanel();
		npanel.setOpaque(true);
		npanel.setBackground(Color.white);
		Environment.getInstance().commonize(npanel, null);
		npanel.setLayout(new BorderLayout());

		// title label
		_titleLabel = makeJLabel(_canvas.getParameters().getPlotTitle(), parameters.getTitleFont(),
				SwingConstants.CENTER, Color.white, null, false);
		_titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		npanel.add(_titleLabel, BorderLayout.CENTER);
		add(npanel, BorderLayout.NORTH);
	}

	// add the west component
	private void addWest() {
		PlotParameters parameters = _canvas.getParameters();
		_yLabel = makeRotatedLabel(_canvas.getParameters().getYLabel(), parameters.getAxesFont(), Color.white, null);
		add(_yLabel, BorderLayout.WEST);
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	
	
	// convenience function for making a label
	private JLabel makeJLabel(String text, Font font, int alignment, Color bg, Color fg, boolean excludeFromPrint) {

		JLabel lab = null;
		if (excludeFromPrint) {
			lab = new JLabel(text != null ? text : " ") {
				@Override
				public void paint(Graphics g) {
					// exclude from print
					if (!PrintUtilities.isPrinting()) {
						super.paint(g);
					}
				}
			};
		}
		else {
			lab = new JLabel(text != null ? text : " ");
		}
		lab.setFont(font);
		lab.setOpaque(true);
		if (bg != null) {
			lab.setBackground(bg);
		}
		if (fg != null) {
			lab.setForeground(fg);
		}
		lab.setHorizontalAlignment(alignment);
		lab.setVerticalAlignment(SwingConstants.CENTER);
		return lab;
	}
	
	// convenience function for making a rotated label for y axis label
	private JLabel makeRotatedLabel(String text, Font font, Color bg, Color fg) {
		JLabel lab = new JLabel(text);
		lab.setFont(font);
		lab.setOpaque(true);
		lab.setUI(new VerticalLabelUI());
		lab.setHorizontalAlignment(SwingConstants.CENTER);

		if (bg != null) {
			lab.setBackground(bg);
		}
		if (fg != null) {
			lab.setForeground(bg);
		}
		return lab;
	}
	
	/**
	 * Get the underlying plot canvas.
	 * @return the underlying plot canvas.
	 */
	public PlotCanvas getPlotCanvas() {
		return _canvas;
	}
	
	/**
	 * Create the data set
	 * @return the data set
	 * @throws DataSetException
	 */
	protected DataSet createDataSet() throws DataSetException {
		StripData sd = new StripData("Data", _capacity, _getter, _interval);
		return new DataSet(sd, "x", "y");
	}



	public void setPreferences() {
		_canvas.getPlotTicks().setNumMajorTickY(3);
		_canvas.getPlotTicks().setNumMinorTickY(0);
		
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setFitLineColor(Color.red);
		ds.getCurveStyle(0).setFillColor(new Color(128, 0, 0, 48));
		ds.getCurveStyle(0).setSymbolType(SymbolType.NOSYMBOL);
		PlotParameters params = _canvas.getParameters();
		
		params.setMinExponentY(6);
		params.setNumDecimalY(2);
		params.setXLimitsMethod(LimitsMethod.USEDATALIMITS);
		params.mustIncludeYZero(true);
	}


}
