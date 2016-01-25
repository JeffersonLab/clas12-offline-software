package cnuphys.bCNU.xml;

public interface XmlPrintStreamWritable {

	/**
	 * This is implemented by objects that can write themselves out to XML. The
	 * object should call the <code>XmlPrintStreamWriter</code> to do the
	 * writing. They should write a complete XML fragment--complete and properly
	 * nested XML records.
	 * 
	 * @param xmlPrintStreamWriter
	 */
	public void writeXml(XmlPrintStreamWriter xmlPrintStreamWriter);
}
