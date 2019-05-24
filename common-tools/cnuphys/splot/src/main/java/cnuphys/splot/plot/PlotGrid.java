package cnuphys.splot.plot;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JPanel;

import cnuphys.splot.pdata.DataSet;

public class PlotGrid extends JPanel {

	private int _numRow;
	private int _numCol;
	private Vector<PlotPanel> _panels = new Vector<PlotPanel>();

	/**
	 * Create a plot grid of independent canvases
	 * 
	 * @param numRow the number of rows
	 * @param numCol the number of columns
	 */
	public PlotGrid(int numRow, int numCol) {
		_numRow = numRow;
		_numCol = numCol;
		setLayout(new GridLayout(numRow, numCol, 2, 2));
	}

	/**
	 * Add a plot panel to the grid
	 * 
	 * @param panel the plot panel
	 */
	public void addPlotPanel(PlotPanel panel) {
		if (panel != null) {
			add(panel);
			_panels.add(panel);
		}
	}

	/**
	 * Add a plot canvas to the grid
	 * 
	 * @param canvas the plot canvas
	 */
	public void addPlotCanvas(PlotCanvas canvas) {
		if (canvas != null) {
			PlotPanel panel = new PlotPanel(canvas);
			addPlotPanel(panel);
		}
	}

	// convert row and col to index
	private int rowColToIndex(int row, int col) {
		return row * _numCol + col;
	}

	/**
	 * Obtain the plot panel from the 0-based row and column
	 * 
	 * @param row the row index
	 * @param col the column index
	 * @return the plot panel or <code>null</code>.
	 */
	public PlotPanel getPlotPanel(int row, int col) {
		int index = rowColToIndex(row, col);

		if ((index < 0) || (index >= _panels.size())) {
			return null;
		}
		return _panels.get(index);
	}

	/**
	 * Obtain the plot canvas from the 0-based row and column
	 * 
	 * @param row the row index
	 * @param col the column index
	 * @return the plot canvas or <code>null</code>.
	 */
	public PlotCanvas getPlotCanvas(int row, int col) {
		PlotPanel panel = getPlotPanel(row, col);
		return (panel == null) ? null : panel.getCanvas();
	}

	/**
	 * Obtain the data from the 0-based row and column
	 * 
	 * @param row the row index
	 * @param col the column index
	 * @return the dataset or <code>null</code>.
	 */
	public DataSet getDataSet(int row, int col) {
		PlotCanvas canvas = getPlotCanvas(row, col);
		return (canvas == null) ? null : canvas.getDataSet();
	}

}
