package cnuphys.ced.clasio;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import cnuphys.bCNU.util.FileUtilities;

public class EvioFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			String bname = FileUtilities.bareName(f.getPath(), true);
			return bname.toLowerCase().contains(".evio");
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "Evio Event Files";
	}

}
