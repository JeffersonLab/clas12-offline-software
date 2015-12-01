package cnuphys.ced.plugin;

import java.io.File;
import java.util.Vector;

import org.clas.viewer.EventViewerModule;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.log.SimpleLogPane;
import cnuphys.bCNU.plugin.PluginManager;
import cnuphys.bCNU.util.Jar;
import cnuphys.bCNU.view.BaseView;

public class CedPluginManager extends PluginManager {
    
    //look for Gagik's panels.
    // the base class for bCNU plugins
    
    static EventViewerModule evm;
    static {
	evm = new EventViewerModule() {
	}; 
    }

    
    Vector<String> _jarsProcessed = new Vector<String>();

    public CedPluginManager(String pluginFolder) {
	super(pluginFolder);
//	System.err.println("PLUGIN CLAZ: " + _pluginClaz.getName());
    }
    
    @Override
    protected void searchDir(File dir, Vector<PHolder> v) {
	super.searchDir(dir, v);
    }

    @Override
    protected void searchJar(File file, Vector<PHolder> v) {
	super.searchJar(file, v);
	if (_jarsProcessed.contains(file.getAbsolutePath())) {
	    return;
	}
	_jarsProcessed.add(file.getAbsolutePath());
//	System.err.println("CED PLUGIN MANAGER found jar: " + file.getAbsolutePath());
	
	Vector<String> entries = Jar.getEntries(file.getAbsolutePath());

	for (String name : entries) {
//	    System.err.println("ENTRY: " + name);
	}

    }
    
    
    @Override
    protected void handleUnknownKlaz(Class claz) {
	System.err.println("Handle class: " + claz.getName());
	
//	if (evm.getClass().isAssignableFrom(claz)) {
	    try {
		Object o = claz.newInstance();
		System.err.println("LOADED Gagik Plugin");
		new PView((EventViewerModule)o);
		
	    } catch (InstantiationException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    }
//	}
	
    }


    class PView extends BaseView {
	public PView(EventViewerModule evm) {
		super(AttributeType.TITLE, "Log", AttributeType.ICONIFIABLE, true,
			AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE, true,
			AttributeType.RESIZABLE, true, AttributeType.WIDTH, 600,
			AttributeType.HEIGHT, 600, AttributeType.VISIBLE, false,
			AttributeType.VIEWTYPE, -999);
	    
		add(evm.getDetectorPanel());
	}
    }
}
