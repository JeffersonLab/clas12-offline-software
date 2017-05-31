package cnuphys.bCNU.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlPrintStreamWriter implements XMLStreamWriter {
	
	//for writing arrays
	public static final String XmlArrayNumbersElementName = "numbers";
	public static final String XmlArrayTypeAttName = "type";
	public static final String XmlArrayNameAttName = "name";
	public static final String XmlArrayValueElementName = "value";


	/**
	 * A Stack of elements. When an element is started, it is popped onto the
	 * stack. When the element is ended, it is popped. At the end of writing the
	 * document, the stack should be empty.
	 */
	private Stack<String> elements = new Stack<String>();

	/**
	 * The stream to which all the output will go.
	 */

	private PrintStream out;

	/**
	 * Create an <code>XmlPrintStreamWriter</code> from a
	 * <code><PrintStream></code> object.
	 * 
	 * @param out the <code><PrintStream></code> object to use.
	 */
	public XmlPrintStreamWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Create an <code>XmlPrintStreamWriter</code> from a <code><File></code>
	 * object. If the file exists, then it will be truncated to zero size;
	 * otherwise, a new file will be created. The output will be written to the
	 * file and is buffered.
	 * 
	 * @param file the <code><File></code> object to use.
	 * @throws FileNotFoundException
	 */
	public XmlPrintStreamWriter(File file) throws FileNotFoundException {
		this(new PrintStream(file));
	}

	/**
	 * Create an <code>XmlPrintStreamWriter</code> from a fully qualified path
	 * name. If the file exists, then it will be truncated to zero size;
	 * otherwise, a new file will be created. The output will be written to the
	 * file and is buffered.
	 * 
	 * @param fullPathName the fully qualified path name to use.
	 * @throws FileNotFoundException
	 */
	public XmlPrintStreamWriter(String fullPathName)
			throws FileNotFoundException {
		this(new File(fullPathName));
	}

	/**
	 * Create an <code>XmlPrintStreamWriter</code> from a directory name and a
	 * bare file name.. If the file exists, then it will be truncated to zero
	 * size; otherwise, a new file will be created. The output will be written
	 * to the file and is buffered.
	 * 
	 * @param dirName the fully qualified path to the parent directory.
	 * @param bareFileName the name of just the file.
	 * @throws FileNotFoundException
	 */
	public XmlPrintStreamWriter(String dirName, String bareFileName)
			throws FileNotFoundException {
		this(new File(dirName, bareFileName));
	}

	/**
	 * This notifies the provided <code>XmlPrintStreamWritable</code>
	 * implementing object to go ahead and write itself out. The object is
	 * responsible for writing out a self contained XML fragment.
	 * 
	 * @param xmlPrintStreamWritable
	 */
	public void write(XmlPrintStreamWritable xmlPrintStreamWritable) {
		if (xmlPrintStreamWritable != null) {
			xmlPrintStreamWritable.writeXml(this);
		}
	}

	/**
	 * Close this writer and free any resources associated with the writer.
	 */
	@Override
	public void close() throws XMLStreamException {
		if (out != null) {
			out.close();
		}
	}

	/**
	 * Write any cached data to the underlying output mechanism.
	 */
	@Override
	public void flush() throws XMLStreamException {
		if (out != null) {
			out.flush();
		}
	}
	
	/**
	 * This writes an element using a Properties object for the name-value attributes.
	 * @param elementName the element name
	 * @param properties used to store the attribute name-value pairs
	 * @throws XMLStreamException
	 */
	public void writeElementWithProps(String elementName, Properties properties) throws XMLStreamException {
		
		writeStartElement(elementName);
		
		if (properties != null) {
			Enumeration<?> keys = properties.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				Object val = properties.get(key);
				writeAttribute(key, val.toString());
			}
		}
		closeBracket();
		
	    writeEndElement();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return null;
	}

	@Override
	public String getPrefix(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"getPrefix(String arg0) not implemented yet");
	}

	@Override
	public Object getProperty(String arg0) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void setDefaultNamespace(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"setDefaultNamespace(String arg0) not implemented yet");
	}

	@Override
	public void setNamespaceContext(NamespaceContext arg0)
			throws XMLStreamException {
		throw new XMLStreamException(
				"setNamespaceContext(NamespaceContext arg0) not implemented yet");
	}

	@Override
	public void setPrefix(String arg0, String arg1) throws XMLStreamException {
		throw new XMLStreamException(
				"setPrefix(String arg0, String arg1) not implemented yet");
	}

	@Override
	public void writeAttribute(String name, String value)
			throws XMLStreamException {
		out.print("\n " + name + "=\"" + value + "\"");
	}

	@Override
	public void writeAttribute(String name, String value, String s)
			throws XMLStreamException {
		out.print("\n " + name + "=\"" + value + "\"" + s);
	}

	@Override
	public void writeAttribute(String arg0, String arg1, String arg2,
			String arg3) throws XMLStreamException {
		throw new XMLStreamException(
				"writeAttribute(String arg0, String arg1, String arg2, String arg3) not implemented yet");
	}

	@Override
	public void writeCData(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"writeCData(String arg0) not implemented yet");
	}

	@Override
	public void writeCharacters(String s) throws XMLStreamException {
		out.print(s);
	}

	@Override
	public void writeCharacters(char[] arg0, int arg1, int arg2)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeProcessingInstruction(String arg0, String arg1) not implemented yet");
	}
	
	/**
	 * Write out a double array
	 * @param name
	 * @param array
	 * @throws XMLStreamException
	 */
	public void writeArray(String name, double array[]) throws XMLStreamException {
		writeArray(name, array, (array == null) ? 0 :  array.length);
	}

	public void writeArray(String name, double array[], int dataLen) throws XMLStreamException {
		writeStartElement(XmlArrayNumbersElementName);
		writeAttribute(XmlArrayNameAttName, name, " ");
		writeAttribute(XmlArrayTypeAttName, "array", ">");

		for (int i = 0; i < dataLen; i++) {
			writeCharacters("<" + XmlArrayValueElementName + ">"
					+ array[i] + "</" + XmlArrayValueElementName + ">");
			
			if (((i+1) % 4) == 0) {
				writeCharacters("\n");
			}
			else {
				writeCharacters(" ");
			}
		}

		writeEndElement();
		
	}
	
	public void writeArray(String name, long array[]) throws XMLStreamException {
		writeArray(name, array, (array == null) ? 0 :  array.length);
	}

	public void writeArray(String name, long array[], int dataLen) throws XMLStreamException {
		writeStartElement(XmlArrayNumbersElementName);
		writeAttribute(XmlArrayNameAttName, name, " ");
		writeAttribute(XmlArrayTypeAttName, "array", ">");

		for (int i = 0; i < dataLen; i++) {
			writeCharacters("<" + XmlArrayValueElementName + ">"
					+ array[i] + "</" + XmlArrayValueElementName + ">");
			
			if (((i+1) % 8) == 0) {
				writeCharacters("\n");
			}
			else {
				writeCharacters(" ");
			}
		}

		writeEndElement();
		
	}
	
	/**
	 * Write an XML comment to the output stream.
	 * 
	 * @param comment the bare comment. May be null. The XML tags will be added
	 *            automatically.
	 */
	@Override
	public void writeComment(String comment) throws XMLStreamException {
		if (out != null) {
			out.println("<!-- " + comment == null ? "" : comment + " -->");
		}
	}

	@Override
	public void writeDTD(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"writeDTD(String arg0) not implemented yet");
	}

	@Override
	public void writeDefaultNamespace(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"writeDefaultNamespace(String arg0) not implemented yet");
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		writeStartElement(localName);
		writeEndElement();
	}

	@Override
	public void writeEmptyElement(String arg0, String arg1)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeEmptyElement(String arg0, String arg1) not implemented yet");
	}

	@Override
	public void writeEmptyElement(String arg0, String arg1, String arg2)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeEmptyElement(String arg0, String arg1, String arg2) not implemented yet");
	}

	/**
	 * Flushes and closes the print stream. Call this when you are done.
	 */
	@Override
	public void writeEndDocument() throws XMLStreamException {

		if (!elements.isEmpty()) {
			throw new XMLStreamException("Dangling elements, including: "
					+ elements.lastElement());
		}

		flush();
		close();
	}

	/**
	 * Closes the current element, and removes it from the stack.
	 */
	@Override
	public void writeEndElement() throws XMLStreamException {
		String currentElement = elements.pop();
		out.println("</" + currentElement + ">");
	}
	
	/**
	 * Write a new line character
	 */
	public void newLine() {
		out.println();
	}

	@Override
	public void writeEntityRef(String arg0) throws XMLStreamException {
		throw new XMLStreamException(
				"writeEntityRef(String arg0) not implemented yet");
	}

	@Override
	public void writeNamespace(String arg0, String arg1)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeNamespace(String arg0, String arg1) not implemented yet");
	}

	@Override
	public void writeProcessingInstruction(String arg0)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeProcessingInstruction(String arg0) not implemented yet");
	}

	@Override
	public void writeProcessingInstruction(String arg0, String arg1)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeProcessingInstruction(String arg0, String arg1) not implemented yet");
	}

	/**
	 * Write the XML header. This method will default to utf-8 and version 1.0.
	 */
	@Override
	public void writeStartDocument() throws XMLStreamException {
		writeStartDocument("utf-8", "1.0");
	}

	/**
	 * Write the XML header. This method will default to utf-8.
	 * 
	 * @param version the version string, e.g., "1.0". (Don't embed the quote
	 *            characters).
	 */
	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		writeStartDocument("utf-8", version);
	}

	/**
	 * Write the XML header.
	 * 
	 * @param encoding the encoding string, e.g., "utf-8". (Don't embed the
	 *            quote characters).
	 * @param version the version string, e.g., "1.0". (Don't embed the quote
	 *            characters).
	 */
	@Override
	public void writeStartDocument(String encoding, String version) {
		if (out != null) {
			out.println("<?xml version=\"" + version + "\" encoding=\""
					+ encoding + "\" ?>");
		}
	}

	/**
	 * Starts a new element, with no namespace or prefix. If there is a current
	 * element, it finishes it by adding a close bracket, but does not close it.
	 * 
	 * @param localName the base element name.
	 */
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		// see if stack is not empty
//		if (!elements.isEmpty()) {
//			//String currentElem = elements.peek();
//			// the close bracket is written now
//			out.println("*>*");
//		}

		elements.push(localName);
		out.print("<" + localName);
	}
	
	public void closeBracket() {
		out.print(">\n");
	}

	@Override
	public void writeStartElement(String arg0, String arg1)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeStartElement(String arg0, String arg1) not implemented yet");
	}

	@Override
	public void writeStartElement(String arg0, String arg1, String arg2)
			throws XMLStreamException {
		throw new XMLStreamException(
				"writeStartElement(String arg0, String arg1, String arg2) not implemented yet");
	}

	/**
	 * main program for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// String homeDirectory = System.getProperty("user.home");
		// try {
		// XmlPrintStreamWriter xmlPrintStreamWriter = new
		// XmlPrintStreamWriter(homeDirectory, "test.xml");
		XmlPrintStreamWriter writer = new XmlPrintStreamWriter(
				System.out);
		try {
			writer.writeStartDocument();

			writer.writeStartElement("slideshow");
			
			Properties props = new Properties();
			props.put("ival", 7);
			props.put("bval", false);
			
			writer.writeElementWithProps("Element1", props);
		//	props.clear();

			writer.writeEndElement();
			writer.writeEndDocument();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
		// }
		// catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

	}

}