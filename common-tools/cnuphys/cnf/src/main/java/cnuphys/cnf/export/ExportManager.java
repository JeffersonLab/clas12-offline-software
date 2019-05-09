package cnuphys.cnf.export;


import java.io.File;

import javax.swing.JMenu;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;

public class ExportManager {

	//singleton
	private static ExportManager _instance;
	
	//the plot menu
	private static JMenu _exportMenu;
	
	//singleton constructor
	private ExportManager() {
	}
	
	/**
	 * Access to the PlotManager singleton
	 * @return the PlotManager singleton
	 */
	public static ExportManager getInstance() {
		if (_instance == null) {
			_instance = new ExportManager();
		}
		return _instance;
	}
	
	
	/**
	 * Get the plot menu
	 * @return
	 */
	public static JMenu getExportMenu() {
		if (_exportMenu == null) {
			_exportMenu = new JMenu("Export");
		}
		
		return _exportMenu;
	}
}