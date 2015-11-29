package cnuphys.bCNU.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Plugin mamanger handles dynamic loading of plugins
 * 
 * @author heddle
 *
 */
public class PluginManager {

    // so we only try a name once
    private Vector<String> _attemptedLoads = new Vector<String>();

    // the bad dir holding the plugins
    private File _pluginDir;

    private int pdirLen;
    
    // the base class
    private Class<Plugin> _pluginClaz;

    // filter on .class files and subdirectories
    private FilenameFilter filter = new FilenameFilter() {

	@Override
	public boolean accept(File dir, String name) {
	    File theFile = new File(dir, name);
	    return (theFile.isDirectory() || name.endsWith(".class"));
	}

    };

    /**
     * Create a plugin mananger
     * 
     * @param pluginFolder the base folder
     */
    public PluginManager(String pluginFolder) {
	try {
	    _pluginClaz = (Class<Plugin>) Class.forName("cnuphys.bCNU.plugin.Plugin");
	} catch (ClassNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	if (pluginFolder != null) {
	    _pluginDir = new File(pluginFolder);

	    if (_pluginDir.exists() && _pluginDir.isDirectory()) {
		pdirLen = pluginFolder.length();

		// check for plugins on a timer so that can
		// be dropped in even after app is running
		TimerTask task = new TimerTask() {

		    @Override
		    public void run() {
			// processPluginFolder();
			Vector<PHolder> pholders = new Vector<PHolder>();
			searchDir(_pluginDir, pholders);

			if (!pholders.isEmpty()) {
			    loadClasses(pholders);
			}
		    }

		};

		// check every 5 seconds
		Timer _timer = new Timer();
		_timer.scheduleAtFixedRate(task, 0, 5000);
	    }
	} // plugin folder not null

    }
    
    private void loadClasses(Vector<PHolder> v) {
	URL url[] = new URL[v.size()];
	
	int i = 0;
	for (PHolder ph : v) {
	    URI uri = ph.file.toURI();
	    try {
		url[i++] = uri.toURL();
	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    }	    
	}
	
	URLClassLoader ucl = new URLClassLoader(url);
	for (PHolder ph : v) {
	    try {
		Class claz = ucl.loadClass(ph.className);
		
		//make sure it is a subclass of Plugin
		if (_pluginClaz.isAssignableFrom(claz)) {
		try {
		    claz.newInstance();
		} catch (InstantiationException e) {
		    e.printStackTrace();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		}
		}
	    } catch (ClassNotFoundException e1) {
		e1.printStackTrace();
	    }
	}

    }
    
    // process the folder
//    private void processPluginFolder() {
//	// System.err.println("PLUGINDir exists: " + _pluginDir.exists());
//	if (_pluginDir.exists()) {
//	    // System.err.println(
//	    // "PLUGINDir is directory: " + _pluginDir.isDirectory());
//
//	    if (_pluginDir.isDirectory()) {
//
//		File files[] = _pluginDir.listFiles(filter);
//		int count = (files == null) ? 0 : files.length;
//
//		if (count == 0) {
//		    System.err.println("No class files found");
//		}
//		else {
//		    System.err.println(count + " class files found");
//
//		    URL url[] = new URL[count];
//
//		    int i = 0;
//		    for (File file : files) {
//			try {
//			    URI uri = file.toURI();
//			    url[i++] = uri.toURL();
//			} catch (MalformedURLException e) {
//			    e.printStackTrace();
//			}
//		    }
//
//		    URLClassLoader ucl = new URLClassLoader(url);
//		    for (File file : files) {
//			String bareName = file.getName().substring(0,
//				file.getName().lastIndexOf('.'));
//			boolean tried = _attemptedLoads.contains(bareName);
//
//			if (tried) {
//			    System.err.println("Already tried " + bareName);
//			}
//			else {
//
//			    _attemptedLoads.add(bareName);
//			    // System.err.println("LOAD " + bareName);
//			    try {
//				Class claz = ucl.loadClass(bareName);
//				try {
//				    claz.newInstance();
//				} catch (InstantiationException e) {
//				    e.printStackTrace();
//				} catch (IllegalAccessException e) {
//				    e.printStackTrace();
//				}
//			    } catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			    }
//			} // !tried
//		    }
//		}
//	    }
//	}
//
//    }

    /**
     * Search a directory for classes that are plugins.
     * 
     * @param dir the file the is a directory in the classpath
     * @param v the vector to which we add any matching classes.
     */
    private void searchDir(File dir, Vector<PHolder> v) {
	// System.out.println("Searching directory: " + dir.getAbsolutePath());

	String[] files = dir.list(filter);
	if ((files == null) || (files.length < 1)) {
	    return;
	}
	// used to remove the leading class path part

	for (String fileName : files) {
	    File file = new File(dir.getAbsolutePath(), fileName);

	    if (file.isDirectory()) {
		searchDir(file, v);
	    }
	    else { // is a file

		String klass = file.getAbsolutePath();

		klass = klass.substring(pdirLen + 1).replace(File.separatorChar,
			'.');
		// remove .class
		klass = klass.substring(0, klass.lastIndexOf('.'));

		if (!_attemptedLoads.contains(klass)) {
		    System.err.println("KLAS: " + klass);
		    _attemptedLoads.add(klass);
		    v.add(new PHolder(file, klass));
		}

		// // remove the classPath part
		// klass = klass.substring(cp_index);
		//
		// // remove .class
		// klass = klass.substring(0, klass.lastIndexOf('.'));
		//
		// // change separator to "."
		// klass = klass.replace(File.separatorChar, '.');
		//
		// // now check that the candidate is a direct descendant
		// // of the plugin class
		// try {
		// Class rClass = Class.forName(klass);
		// Class bClass = rClass.getSuperclass();
		//
		// if ((bClass != null)
		// && (bClass.getName().equals(className) == true)) {
		// v.add(rClass);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	    }
	}
    }
    
    class PHolder {
	public File file;
	public String className;
	
	public PHolder(File file, String className) {
	    this.file = file;
	    this.className = className;
	}
    }
}
