package cnuphys.bCNU.xml;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Environment;

public class XmlSupport {

	/** A common attribute name for a value" */
	public static final String XmlValAttName = "val";

	/** A common attribute name for a name" */
	public static final String XmlNameAttName = "name";

	/** A common attribute name for left" */
	public static final String XmlLeftAttName = "left";

	/** A common attribute name for top" */
	public static final String XmlTopAttName = "top";

	/** A common attribute name for width" */
	public static final String XmlWidthAttName = "width";

	/** A common attribute name for height" */
	public static final String XmlHeightAttName = "height";

    /** Filter so only files with specified extensions are seen in file viewer. */
   private static FileNameExtensionFilter xmlFilter;
   
   /** the last accessed directory */
   private static String dataFilePath;
	
   static {
	   xmlFilter = new FileNameExtensionFilter("*.xml", "xml");
	   dataFilePath = Environment.getInstance().getHomeDirectory();
   }
   
   /**
    * Replace xml special characters with their escape sequence
    * @param input the input string
    * @return the valis xml output string
    */
   public static String validXML(String input) {
	   String output = new String(input);
	  output = output.replace("&", "&amp;");
	  output = output.replace("<", "&lt;");
	  output = output.replace(">", "&gt;");
	  output = output.replace("\"", "&quot;");
	  output = output.replace("'", "&#39;");
	  
	  return output;
   }
   
   /**
    * Convenience method for adding a rectangle to a set of properties so it will
    * later be written out as attributes.
    * @param props the Properties to add to
    * @param rectangle the Rectangle in question
    */
	public static void addRectangleAttribute(Properties props, Rectangle rect) {
		props.put(XmlLeftAttName, rect.x);
		props.put(XmlTopAttName, rect.y);
		props.put(XmlWidthAttName, rect.width);
		props.put(XmlHeightAttName, rect.height);
	}

	/**
	 * Export  to an xml file. This will will bring up the save dialog.
	 * @param canvas the canvas to export
	 */
	public static void save(XmlPrintStreamWritable rootWritable) {
        JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(xmlFilter);
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (selectedFile != null) {
				
				if (selectedFile.exists()) {
					int answer = JOptionPane.showConfirmDialog(null,
							selectedFile.getAbsolutePath()
									+ "  already exists. Do you want to overwrite it?",
							"Overwite Existing File?", JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, ImageManager.cnuIcon);

					if (answer != JFileChooser.APPROVE_OPTION) {
						return;
					}
				} // end file exists check

				
				dataFilePath = selectedFile.getParent();
			
				try {
					save(rootWritable, selectedFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Export the plot data to an xml file. 
	 * @param canvas the canvas to export
	 * @param xmlFile the file that will hold the plots on the canvas
	 * @throws FileNotFoundException 
	 */
	public static void save(XmlPrintStreamWritable writable, File xmlFile) throws FileNotFoundException {
		XmlPrintStreamWriter writer = new XmlPrintStreamWriter(xmlFile);
		
		try {
			writer.writeStartDocument();
			writable.writeXml(writer);
			writer.writeEndDocument();
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
		}

		
		try {
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple file selection using the xml filter
	 * @return the selected file, or <code>null</code>
	 */
	public static File openXmlFile() {
		File file = null;
		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(xmlFilter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			if (file != null) {
				dataFilePath = file.getParent();
			}
		}
		return file;
	}
	
	
	/**
	 * Select a plot xml file and open it, replacing whatever (if anything) is currently
	 * on the canvas. This will call an open file dialog.
	 * @param canvas the canvas that will receive the new plot(s).
	 */
	public static void open(final String rootElementName) {
        JFileChooser chooser = new JFileChooser(dataFilePath);
        chooser.setSelectedFile(null);
		chooser.setFileFilter(xmlFilter);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
			if (selectedFile != null) {
				dataFilePath = selectedFile.getParent();
				open(rootElementName, selectedFile);
			}
        }
	}
	
	/**
	 * Select a plot xml file and open it, replacing whatever (if anything) is currently
	 * on the canvas.
	 * @param canvas the canvas that will receive the new plot(s).to openthat will hold the plots on the canvas
	 */
	public static void open(final String rootElementName, File xmlFile) {
		//get the dom model
		Document dom = XmlDomParser.getDomObject(xmlFile.getPath());

//Get the list of notes
		
		NodeList nodes = dom.getElementsByTagName(rootElementName);
		if (nodes != null) {
			System.err.println("FOUND " + nodes.getLength() + " for: " + rootElementName);
		}
	}
}
