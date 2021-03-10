package cnuphys.cnf.export;


import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.io.base.DataEvent;

import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;

public class ExportManager implements IEventListener {

	//singleton
	private static ExportManager _instance;
	
	//the export menu
	private static JMenu _exportMenu;
	
	//which exporter is active;
	private AExporter _activeExporter;
	
	//the exporters
	private Hashtable<JMenuItem, AExporter> _exporters;
	
	//singleton constructor
	private ExportManager() {
		EventManager.getInstance().addEventListener(this, 2);
		_exporters = new Hashtable<>();
		_exportMenu = new JMenu("Export");
		
		//create a CSV exporter
		addExporter(new CSVExporter());
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
	
	//export using a given exporter
	private final void export(JMenuItem mitem) {
		System.out.println("Telling to export: [" + mitem.getActionCommand() + "]");
		
		AExporter exporter = _exporters.get(mitem);
		
		
		//the filter for "OUTSIDE" or "INSIDE" assuming the ordering is
		//x, y, z, bx, by, bz
//		IExportFilter filter = new IExportFilter() {
//
//			@Override
//			public boolean pass(double[] a) {
//				
//				
//				double dot = 0;
//				for (int i = 0; i < 3; i++) {
//					dot += a[i] *a[i+3];
//				}
//				return (dot <= 0); //outside
//		//		return dot > 0; //inside
//		//		return true;
//			}
//			
//		};

		if (exporter != null) {
			_activeExporter = exporter;

			boolean okToExport = _activeExporter.prepareToExport(null);

			if (okToExport) {
				EventManager.getInstance().rewindFile();
				
				//hack to get first
				EventManager.getInstance().reloadCurrentEvent();
				
				EventManager.getInstance().streamToEndOfFile();
			}
			else {
				System.out.println("export cancelled");
			}
		}
		
	}
	
	/**
	 * Add an exporter to the collection
	 * @param exporter the exporter to add
	 */
	public void addExporter(AExporter exporter) {
		JMenuItem mitem = new JMenuItem(exporter.getMenuName());
		
		mitem.setEnabled(EventManager.getInstance().haveOpenFile());
		
		//use lambda for action
		mitem.addActionListener(e -> export(mitem));
		_exportMenu.add(mitem);
		
		_exporters.put(mitem, exporter);
				
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

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
		
		if (_activeExporter != null) {
			_activeExporter.nextEvent(event);
		}
	}

	@Override
	public void openedNewEventFile(File file) {
		enableExporters(true);
	}

	@Override
	public void rewoundFile(File file) {
	}

	@Override
	public void streamingStarted(File file, int numToStream) {
	}

	@Override
	public void streamingEnded(File file, int reason) {
		if (_activeExporter != null) {
			_activeExporter.done();
			_activeExporter = null;
		}
	}
	
	private void enableExporters(boolean enabled) {
		
		
		boolean b = enabled && EventManager.getInstance().haveOpenFile();
		
		Enumeration<JMenuItem> e = _exporters.keys();
		while (e.hasMoreElements()) {
			JMenuItem mitem = e.nextElement();
			mitem.setEnabled(b);
		}
	}
}