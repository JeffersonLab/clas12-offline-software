package cnuphys.cnf.plot;

import java.io.File;

import javax.swing.JMenu;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;

public class PlotManager {

	//singleton
	private static PlotManager _instance;
	
	//the plot menu
	private static JMenu _plotMenu;
	
	//singleton constructor
	private PlotManager() {
	}
	
	/**
	 * Access to the PlotManager singleton
	 * @return the PlotManager singleton
	 */
	public static PlotManager getInstance() {
		if (_instance == null) {
			_instance = new PlotManager();
		}
		return _instance;
	}
	
	/**
	 * Reset all the plots to empty
	 */
	public void resetPlots() {
		
	}
	
	/**
	 * Get the plot menu
	 * @return
	 */
	public static JMenu getPlotMenu() {
		if (_plotMenu == null) {
			_plotMenu = new JMenu("Plot Wizard");
		}
		
		return _plotMenu;
	}
	
	/**
	 * Create a sample plot for testing
	 */
	public static void createSamplePlot() {
		
	}
}
