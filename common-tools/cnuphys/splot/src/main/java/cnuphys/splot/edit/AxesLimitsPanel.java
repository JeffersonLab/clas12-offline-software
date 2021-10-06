package cnuphys.splot.edit;

import java.awt.GridLayout;

import javax.swing.JPanel;

import cnuphys.splot.plot.CommonBorder;
import cnuphys.splot.plot.PlotCanvas;

public class AxesLimitsPanel extends JPanel {
	
	//the plot canvas
	private PlotCanvas _canvas;
	
	//the two panels for t x and y axes
	private OneAxisLimitsPanel  _xPanel;
	private OneAxisLimitsPanel  _yPanel;

	/**
	 * Create the panel for editing the axes limits
	 * @param canvas
	 */
	public AxesLimitsPanel(PlotCanvas canvas) {
		_canvas = canvas;
		
		setLayout(new GridLayout(2, 1, 4, 4));
		
		_xPanel = new OneAxisLimitsPanel(canvas, OneAxisLimitsPanel.Axis.X);
		_yPanel = new OneAxisLimitsPanel(canvas, OneAxisLimitsPanel.Axis.Y);
		
		setBorder(new CommonBorder("Axes Limits"));
		
		add(_xPanel);
		add(_yPanel);
	}
	
	/** Apply any changes */
	public void apply() {
		_xPanel.apply();
		_yPanel.apply();
	}
}
