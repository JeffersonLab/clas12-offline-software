package cnuphys.cnf.export;

import java.io.File;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.FileUtilities;
import cnuphys.cnf.event.EventManager;

public abstract class AExporter {
	
	//default data folder
	protected static String dataPath;
	
	//export file
	protected File _exportFile;
	
	/**
	 * Get the name that will appear on the menu, e.g., "CSV"
	 * @return the name that will appear on the menu
	 */
	public abstract String getMenuName();
	
	/**
	 * Prepare to export the current hipo file. The file will be rewound and streamed
	 * from the beginning to the end
	 * @return <code>true</code> if export should proceed
	 */
	public abstract boolean prepareToExport(IExportFilter filter);
	
	/** 
	 * The next hipo event to export
	 * @param event the event
	 */
	public abstract void nextEvent(DataEvent event);
	
	/**
	 * Done streaming. This is a place where cleanup, flushing , closing,
	 * etc. should occur
	 */
	public abstract void done();
	
	/**
	 * 
	 * @param filterDescription the filter description.
	 * @param preferredExtenstion the preferred extension
	 * @param extensions        Variable length list of extensions to filter on. If
	 *                          no filter, pass null.
	 * @return
	 */
	protected File getFile(String filterDescription, String preferredExtenstion, String... extensions) {
		File file = null;
		
		if (dataPath == null) {
			dataPath = System.getProperty("user.home");
		}
		
		File hfile = EventManager.getInstance().getCurrentFile();
		if (hfile == null) {
			return null;
		}
		
		String defName = FileUtilities.bareName(hfile.getAbsolutePath(), false);
		defName += "." + preferredExtenstion;
		
		file = FileUtilities.saveFile(dataPath, defName, filterDescription, extensions);
	    if (file != null) {
	    	System.out.println("Selected: [" +  file.getPath() + "]");
	    }
		
		return file;
	}

}
