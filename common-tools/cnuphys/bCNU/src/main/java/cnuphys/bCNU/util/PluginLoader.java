package cnuphys.bCNU.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Vector;

import cnuphys.bCNU.log.Log;

public class PluginLoader {

	// the root directory holding the plugins
	private File _pluginDir;

	// the number of files in the plugin folder
	private int _pdirLen;

	// the total class path
	private String _classPath;

	// the class object for the plugin being searched for
	private Class _claz;

	// classes to exclude
	private Vector<String> _excludes;

	// filter on .class files, jar files, And subdirectories
	private static final FilenameFilter _filter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String name) {
			File theFile = new File(dir, name);
			//exclude jar files untill we need to include them
//			return (theFile.isDirectory() || name.endsWith(".class") || name.endsWith(".jar"));
			return (theFile.isDirectory() || name.endsWith(".class"));
		}

	};
	
	private PluginLoader() {
		
	}

	/**
	 * Plugin (actually any object) loader for a given class
	 * 
	 * @param classPath
	 *            the overall class path that will be searched
	 * @param claz
	 *            the class to look for
	 * @param excludes
	 *            a list of excluded classes
	 */
	public PluginLoader(String classPath, Class claz, Vector<String> excludes) {
		_classPath = classPath;
		_claz = claz;
		_excludes = excludes;
	}

	/**
	 * Plugin (actually any object) loader for a given class
	 * 
	 * @param classPath
	 *            the overall class path that will be searched
	 * @param className the class namme
	 * @param excludes
	 *            a list of excluded classes
	 */
	public PluginLoader(String classPath, String className, Vector<String> excludes) {
		_classPath = classPath;
		try {
			_claz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		_excludes = excludes;
	}

	/**
	 * Load the plugins
	 * 
	 * @return And array of objects that can were instantiated
	 */
	public List<Object> load() {
		Vector<Object> objects = new Vector<Object>();
		if ((_classPath == null) || (_claz == null)) {
			return objects;
		}

		// get the class path pieces which should be directories Or jar files
		String cptokens[] = Environment.getInstance().splitPath(_classPath);

		if (cptokens != null) {
			for (String cptoken : cptokens) {
				discoverPlugins(cptoken, objects);
			}
		}

		return objects;

	}

	// discover plugins in a single classpath token
	private void discoverPlugins(String cpToken, List<Object> objects) {
		if (cpToken == null) {
			return;
		}

		// does it exist?
		File file = new File(cpToken);
		if (!file.exists()) {
			if (cpToken.endsWith("jar")) {
				jarSearch(cpToken, objects);
			} else {
				Log.getInstance().warning("Classpath token: [" + cpToken + "] not recognized");
			}
			return;
		}

		if (file.isDirectory()) {
			Log.getInstance().info("Searching for plugins in directory: [" + cpToken + "]");
			dirSearch(cpToken, objects);
			return;
		} else if (file.isFile()) {
			String extension = FileUtilities.getExtension(file);
			if ((extension != null) && (extension.toLowerCase().equals("jar"))) {
				Log.getInstance().info("Searching for plugins in jar file: [" + cpToken + "]");
				// TODO init this search
				return;
			}
		}

		Log.getInstance().warning("Classpath token: [" + cpToken + "] is neither a directory Or a jar file. Ignoring.");

	}

	/**
	 * Create a plugin mananger
	 * 
	 * @param pluginFolder
	 *            the base folder
	 */
	private void dirSearch(String pluginFolder, List<Object> objects) {

		if (pluginFolder != null) {
			_pluginDir = new File(pluginFolder);

			if (_pluginDir.exists() && _pluginDir.isDirectory()) {
				_pdirLen = pluginFolder.length();

				Vector<PHolder> pholders = new Vector<PHolder>();

				// this is the start of the recursive search
				searchDir(_pluginDir, pholders);

				if (!pholders.isEmpty()) {
					loadClasses(objects, pholders);
				}
			}
		} // plugin folder not null

	}

	// search a jar token
	private void jarSearch(String jarToken, List<Object> objects) {
		Log.getInstance().info("Jar classpath token: [" + jarToken + "]");

		String pattern = File.separator + "jar";
		String fileName = jarToken.replace(pattern, ".jar");
		File file = new File(fileName);
		Log.getInstance().info("Jar file name: [" + file.getAbsolutePath() + "]  exists: " + file.exists());
		if (file.exists()) {
			List<String> entries = Jar.getEntries(file.getPath());
			Vector<PHolder> v = new Vector<PHolder>();

			for (String s : entries) {
				if (s.endsWith(".class")) {
					try {
						s = s.replace('/', '.');
						s = s.replace('\\', '.');
						// strip .class
						int index = s.lastIndexOf(".class");
						s = s.substring(0, index);
	//					Log.getInstance().info("  entry: [" + s + "]");
						Class claz = Class.forName(s);
	//					Log.getInstance().info("  class found: [" + s + "]");

						if (!isAbstract(claz) && !isAnonClass(claz) && (_claz.isAssignableFrom(claz))) {
							Object o = claz.newInstance();
							objects.add(o);
							Log.getInstance().info("  instantiated: [" + o.getClass().getName() + "]");
						}
					} catch (ClassNotFoundException e) {
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

				}
			}
		}
		// System.exit(0);
	}
	
	public static String getFullClassName(File file) throws IOException {           
 
        PluginLoader pl = new PluginLoader();
        MCL mcl = pl.new MCL(file);
        
        String name = mcl.getClassname();
        return name;
    }

	
	public static Object instantiateFromClassFile(File file, String className) {
		URI uri = file.toURI();
		try {
			URL url[] = {uri.toURL()};
			URLClassLoader ucl = new URLClassLoader(url);
			Class claz = ucl.loadClass(className);
			if (!isAbstract(claz)) {
				return claz.newInstance();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	// load the classes
	private void loadClasses(List<Object> objects, Vector<PHolder> v) {
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
//klk649
		try (URLClassLoader ucl = new URLClassLoader(url)) {
			if (ucl != null) {
				for (PHolder ph : v) {
					try {
						Class claz = ucl.loadClass(ph.className);
	
						// make sure it is a subclass of Plugin And not abstract
						if (!isAbstract(claz) && !isAnonClass(claz) && (_claz.isAssignableFrom(claz))) {
						//	Log.getInstance().debug("Potential plugin: " + claz.getName());
							try {
								// call plugin's null constructor
								objects.add(claz.newInstance());
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				} // for
			} // ucl != null
	
//				try {
//					ucl.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// is the class abstract?
	private static boolean isAbstract(Class claz) {
		if (claz == null) {
			return false;
		}

		return Modifier.isAbstract(claz.getModifiers());
	}
	
	private static boolean isAnonClass(Class claz) {
		if (claz == null) {
			return false;
		}

		return claz.getName().contains("$");
	}


	/**
	 * Search a directory for classes that are plugins.
	 * 
	 * @param dir
	 *            the file the is a directory in the classpath
	 * @param v
	 *            the vector to which we add any matching classes.
	 */
	private void searchDir(File dir, Vector<PHolder> v) {
		// System.out.println("Searching directory: " + dir.getAbsolutePath());

		String[] files = dir.list(_filter);
		if ((files == null) || (files.length < 1)) {
			return;
		}
		// used to remove the leading class path part

		for (String fileName : files) {
			File file = new File(dir.getAbsolutePath(), fileName);

			// System.err.println("FILE: " + file.getAbsolutePath());
			if (file.isDirectory()) {
				searchDir(file, v);
			} else if (file.getAbsolutePath().endsWith(".class")) {
				String klass;
				try {
					klass = getFullClassName(file);
					if ((_excludes == null) || !_excludes.contains(klass)) {
						// System.err.println("KLAS: " + klass);
						// _attemptedLoads.add(klass);
						v.add(new PHolder(file, klass));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * Search a jar file for plugins
	 * 
	 * @param file
	 * @param v
	 */
	protected void searchJar(File file, Vector<PHolder> v) {
	}

	/**
	 * Container class to hold a file And a classname
	 * 
	 * @author heddle
	 *
	 */
	protected class PHolder {
		public File file;
		public String className;

		public PHolder(File file, String className) {
			this.file = file;
			this.className = className;
		}
	}
	
	class MCL extends ClassLoader {
		
		ByteBuffer bb;
		
		public MCL(File file) {
	        FileChannel roChannel;
			try {
				roChannel = new RandomAccessFile(file, "r").getChannel();
		        bb = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size());         
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
		
		
    	public String getClassname() {
     		Class<?> clazz = defineClass((String)null, bb, (ProtectionDomain)null);
    		return (clazz == null) ? null : clazz.getName();
    	}
	}
}