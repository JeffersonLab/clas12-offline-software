package cnuphys.bCNU.view;

import cnuphys.splot.plot.PlotPanel;

public interface IHistogramMaker {

	/**
	 * Add a histogram. Return null if you do not want a histogram in that cell.
	 * @param w
	 * @param h
	 * @return the PlotPanel, or <code>null</code> 
	 */
	public PlotPanel addHistogram(int row, int col, int w, int h);

}
