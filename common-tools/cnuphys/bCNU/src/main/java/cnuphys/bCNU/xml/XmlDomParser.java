package cnuphys.bCNU.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cnuphys.bCNU.log.Log;

public class XmlDomParser {

	/**
	 * Return a dom object corresponding to an xml file.
	 * 
	 * @param fullPath the full path of the XML file
	 * @return the dom object (or <code>null</code>
	 */
	public static Document getDomObject(String fullPath) {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse(new File(fullPath));

		} catch (ParserConfigurationException e) {
			Log.getInstance().exception(e);
		} catch (SAXException e) {
			Log.getInstance().exception(e);
		} catch (IOException e) {
			Log.getInstance().exception(e);
		}

		return dom;
	}

	/**
	 * Given an xml element and the tag name, look for the tag and get the text
	 * content.
	 * 
	 * @param element the xml element.
	 * @param tagName the name of the tag.
	 * @return the text value.
	 */
	public static String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	/**
	 * Given an xml element and the tag name, look for the tag and get the
	 * integer content.
	 * 
	 * @param element the xml element.
	 * @param tagName the name of the tag.
	 * @return the int value, or Integer.MIN_VALUE on error.
	 */
	public static int getIntValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		if (textVal == null) {
			return Integer.MIN_VALUE;
		}
		try {
			return Integer.parseInt(textVal);
		} catch (NumberFormatException e) {
			Log.getInstance().exception(e);
			return Integer.MIN_VALUE;

		}
	}

	/**
	 * Given an xml element and the tag name, look for the tag and get the
	 * boolean content.
	 * 
	 * @param element the xml element.
	 * @param tagName the name of the tag.
	 * @return the boolean value. If there is any problem, <code>false</code> is
	 *         returned.
	 */
	public static boolean getBooleanValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		if (textVal == null) {
			return false;
		}
		try {
			return Boolean.parseBoolean(textVal);
		} catch (NumberFormatException e) {
			Log.getInstance().exception(e);
			return false;

		}
	}

	/**
	 * Given an xml element and the tag name, look for the tag and get the
	 * double content.
	 * 
	 * @param element the xml element.
	 * @param tagName the name of the tag.
	 * @return the double value, or Double.NaN on error.
	 */
	public static double getDoubleValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		if (textVal == null) {
			return Double.NaN;
		}
		try {
			return Double.parseDouble(textVal);
		} catch (NumberFormatException e) {
			Log.getInstance().exception(e);
			return Double.NaN;

		}
	}

}
