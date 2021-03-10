package cnuphys.bCNU.view;

import cnuphys.splot.plot.PlotPanel;

public interface IPlotMaker {

	/**
	 * Add a plot. Return null if you do not want a plot in that cell.
	 * 
	 * @param w
	 * @param h
	 * @return the PlotPanel, or <code>null</code>
	 */
	public PlotPanel addPlot(int row, int col, int w, int h);

}
