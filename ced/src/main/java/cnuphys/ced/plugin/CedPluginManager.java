package cnuphys.ced.plugin;

import java.io.File;
import java.util.Vector;

import javax.swing.JPanel;

import org.jlab.clas.detector.DetectorType;
import org.jlab.clas12.basic.IDetectorModule;

import cnuphys.bCNU.plugin.PluginManager;
import cnuphys.bCNU.util.Jar;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;

public class CedPluginManager extends PluginManager {
    
    //look for Gagik's panels.
    // the base class for bCNU plugins
    
    static IDetectorModule evm;
    static {
	evm = new IDetectorModule() {

		@Override
		public String getAuthor() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public JPanel getDetectorPanel() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public DetectorType getType() {
			return null;
		}
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
		new PView((IDetectorModule)o);
		
	    } catch (InstantiationException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    }
//	}
	
    }


    class PView extends BaseView {
	public PView(IDetectorModule evm) {
		super(PropertySupport.TITLE, evm.getName(), PropertySupport.ICONIFIABLE, true,
			PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
			PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 600,
			PropertySupport.HEIGHT, 600, PropertySupport.VISIBLE, false,
			PropertySupport.VIEWTYPE, -999);
	    
		add(evm.getDetectorPanel());
	}
    }
}
