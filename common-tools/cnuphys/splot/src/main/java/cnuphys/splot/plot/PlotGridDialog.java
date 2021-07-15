package cnuphys.splot.plot;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class PlotGridDialog extends JDialog {

	protected Font _titleFont = Environment.getInstance().getCommonFont(12);
	protected Font _statusFont = Environment.getInstance().getCommonFont(9);
	protected Font _axesFont = Environment.getInstance().getCommonFont(10);
	protected Font _legendFont = Environment.getInstance().getCommonFont(10);

	protected PlotGrid _plotGrid;

	/**
	 * 
	 * @param owner
	 * @param title
	 * @param modal
	 * @param numRow the number of rows
	 * @param numCol the number of columns
	 */
	public PlotGridDialog(JFrame owner, String title, boolean modal, int numRow, int numCol, int width, int height) {
		super(owner, title, modal);

		_plotGrid = new PlotGrid(numRow, numCol);
		add(_plotGrid, BorderLayout.CENTER);

		setSize(width, height);
	}

}
