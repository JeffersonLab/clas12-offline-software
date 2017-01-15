package cnuphys.ced.alldata.graphics;

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

		_histoPanelX = new HistoPanel("Select X");
		_histoPanelY = new HistoPanel("Select Y");
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
	 * Get the Histo2DData object based on the selections.
	 * @return the Histo2DData object
	 */
	public Histo2DData getHisto2DData() {
		String xname = getSelectPanelX().getResolvedName();
		String yname = getSelectPanelY().getResolvedName();
		String name = xname + " - " + yname;
		
		int xnumBins = _histoPanelX.getNumBins();
		double xminVal = _histoPanelX.getMinVal();
		double xmaxVal = _histoPanelX.getMaxVal();

		
		int ynumBins = _histoPanelY.getNumBins();
		double yminVal = _histoPanelY.getMinVal();
		double ymaxVal = _histoPanelY.getMaxVal();
		
		return new Histo2DData(name, xname, yname, xminVal, xmaxVal, xnumBins, yminVal, ymaxVal, ynumBins);
	}
}
