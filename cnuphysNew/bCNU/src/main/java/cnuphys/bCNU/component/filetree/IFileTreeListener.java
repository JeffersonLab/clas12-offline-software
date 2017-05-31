/**
 * 
 */
package cnuphys.bCNU.component.filetree;

import java.io.File;
import java.util.EventListener;
import java.util.List;

public interface IFileTreeListener extends EventListener {
	public void fileDoubleClicked(String fullPath);

	public void filesDoubleClicked(List<File> files);
}
