/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter implements FilenameFilter {

	List<String> _extensions;

	public ExtensionFileFilter(List<String> extensions) {
		_extensions = extensions;
	}

	@Override
	/*
	 * Return <code>true</code> if this file should be shown in the directory
	 * pane, <code>false</code> if it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	 * @see #getExtension
	 * 
	 * @see FileFilter#accepts
	 * 
	 * @return <code>true</code> if this file should be shown.
	 */
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {

				// exclude dirs that start with a dot
				if (f.getName().startsWith(".")) {
					return false;
				}

				return true;
			}
			String extension = getExtension(f);
			if (extension == null) {
				return false;
			}
			if (extension != null && _extensions.contains(getExtension(f))) {
				return true;
			}
		}
		return false;
	}

	public static ExtensionFileFilter createFileFilter(String extensions[]) {
		if ((extensions == null) || (extensions.length < 1)) {
			return null;
		}
		Vector<String> list = new Vector<String>(extensions.length);
		for (String extension : extensions) {
			list.add(extension);
		}
		return new ExtensionFileFilter(list);
	}

	/**
	 * Return the extension portion of the file's name.
	 * 
	 * @see #getExtension
	 * @see FileFilter#accept
	 * @return the extension portion of the file's name.
	 */
	public String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * @param dir
	 *            the directory in which the file was found.
	 * @param name
	 *            the name of the file.
	 */
	@Override
	public boolean accept(File dir, String name) {
		return accept(new File(dir, name));
	}

}
