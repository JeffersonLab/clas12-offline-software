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

import javax.swing.filechooser.FileNameExtensionFilter;

public class PluginManager {
    
    //so we onlytry a name once
    private Vector<String> _attemptedLoads = new Vector<String>();

    private File _pluginDir;
    
    //the base class
    private Class<Plugin> pluginClaz;
    
    //filter on .class files
    private FilenameFilter filter = new FilenameFilter() {

	@Override
	public boolean accept(File dir, String name) {
	    System.err.println("TEST NAME: " + name);
	    return ((name != null) && name.endsWith(".class"));
	}
	
    };


    public PluginManager(String pluginFolder) {
	if (pluginFolder != null) {
	    _pluginDir = new File(pluginFolder);

	    TimerTask task = new TimerTask() {

		@Override
		public void run() {
		    processPluginFolder();
		}

	    };
	    
	    //check every 5 seconds
	    Timer _timer = new Timer();
	    _timer.scheduleAtFixedRate(task, 0, 5000);

	}

    }

    private void processPluginFolder() {
        System.err.println("PLUGINDir exists: " + _pluginDir.exists());
	if (_pluginDir.exists()) {
	     System.err.println(
	     "PLUGINDir is directory: " + _pluginDir.isDirectory());

	    if (_pluginDir.isDirectory()) {

		File files[] = _pluginDir.listFiles(filter);
		int count = (files == null) ? 0 : files.length;
		
		if (count == 0) {
		    System.err.println("No class files found");
		}
		else {
		    System.err.println(count + " class files found");
		    
		    URL url[] = new URL[count];
		    
		    int i = 0;
		    for (File file : files) {
			try {
			    URI uri = file.toURI();
			    url[i++] = uri.toURL();
			} catch (MalformedURLException e) {
			    e.printStackTrace();
			}
		    }
		    
		    URLClassLoader ucl = new URLClassLoader(url);
		    for (File file : files) {
			String bareName = file.getName().substring(0,
				file.getName().lastIndexOf('.'));
			boolean tried = _attemptedLoads.contains(bareName);

			if (tried) {
			    System.err.println("Already tried " + bareName);
			}
			else {

			    _attemptedLoads.add(bareName);
			    // System.err.println("LOAD " + bareName);
			    try {
				Class claz = ucl.loadClass(bareName);
				try {
				    claz.newInstance();
				} catch (InstantiationException e) {
				    e.printStackTrace();
				} catch (IllegalAccessException e) {
				    e.printStackTrace();
				}
			    } catch (ClassNotFoundException e) {
				e.printStackTrace();
			    }
			} // !tried
		    }
		}
	    }
	}

    }
}
