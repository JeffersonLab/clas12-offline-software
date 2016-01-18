package cnuphys.ced.event.data;

import java.awt.GridLayout;

import javax.swing.JPanel;

import cnuphys.splot.pdata.Histo2DData;

public class Histo2DPanel extends JPanel {

	// panel for 1st variable
	private HistoPanel _histoPanelX;

	// panel for 2nd variable
	private HistoPanel _histoPanelY;

	public Histo2DPanel() {
		
		setLayout(new GridLayout(2, 1, 0, 8));

		_histoPanelX = new HistoPanel();
		_histoPanelY = new HistoPanel();
		add(_histoPanelX);
		add(_histoPanelY);
	}
	
	public SelectPanel getSelectPanelX() {
		return _histoPanelX.getSelectPanel();
	}

	
	public SelectPanel getSelectPanelY() {
		return _histoPanelY.getSelectPanel();
	}

	/**
	 * Create the 2D histogram data
	 * @return the 2D histogram data
	 */

	public Histo2DData getHisto2DData() {
		return null;
	}
}
