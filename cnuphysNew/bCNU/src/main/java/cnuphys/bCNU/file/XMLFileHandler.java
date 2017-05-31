/**
 * 
 */
package cnuphys.bCNU.file;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import cnuphys.bCNU.view.XMLView;

/**
 * @author heddle
 * 
 */
public class XMLFileHandler extends AFileHandler {

	/**
	 * An xml file needs to be opened. This could be the result of a drag and
	 * drop, or a open file dialog.
	 * 
	 * @param parent
	 *            the object being affected, usually but not always a view
	 * @param file
	 *            the file to handle.
	 */
	@Override
	public void handleOpen(Object parent, File file) {
		System.err.println("HandleOpen XML: view: "
				+ parent.getClass().getName());

		if (parent instanceof XMLView) {
			try {
				((XMLView) parent).getSaxTree().buildTree(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

}
