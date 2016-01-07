package cnuphys.ced.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.ced.event.plot.ReconstructionPlotGrid;
import cnuphys.ced.frame.Ced;

public class PlotManager {

	// the plot menu
	private JMenu _plotMenu;

	// singleton
	private static PlotManager instance;

	// plot grid dialog
	private static ReconstructionPlotGrid _reconGrid = new ReconstructionPlotGrid();

	private PlotManager() {
		_plotMenu = new JMenu("Plots");
		Ced.getFrame().getJMenuBar().add(_plotMenu);

		final JMenuItem gridPlot = new JMenuItem("Reconstruction Plots");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();

				if (source == gridPlot) {
					_reconGrid.setVisible(true);
				}

			}

		};

		gridPlot.addActionListener(al);
		_plotMenu.add(gridPlot);
	}

	/**
	 * Access to the PlotManager singleton
	 * 
	 * @return the PlotManager singleton
	 */
	public static PlotManager getInstance() {
		if (instance == null) {
			instance = new PlotManager();
		}
		return instance;
	}

}
