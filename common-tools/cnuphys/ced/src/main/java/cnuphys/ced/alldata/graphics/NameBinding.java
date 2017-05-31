package cnuphys.ced.alldata.graphics;

import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import cnuphys.bCNU.xml.XmlPrintStreamWritable;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;

/**
 * A simple class that binds a name like "x" or "theta" to abank.column,
 * like DC::dgtz.sector
 * @author heddle
 *
 */
public class NameBinding implements Comparable<NameBinding>, XmlPrintStreamWritable {

	public String varName;
	public String bankColumnName;
	
	public NameBinding(String vname, String bcname) {
		varName = vname;
		bankColumnName = bcname;
	}
	
	@Override
	public int compareTo(NameBinding o) {
		String lcv = varName.toLowerCase();
		String lco = o.varName.toLowerCase();
		return lcv.compareTo(lco);
	}
	
	
	@Override
	public void writeXml(XmlPrintStreamWriter xmlPrintStreamWriter) {
		Properties props = new Properties();
		props.put(XmlUtilities.XmlName, varName);
		props.put(XmlUtilities.XmlBankColumn, bankColumnName);
		try {
			xmlPrintStreamWriter.writeElementWithProps(XmlUtilities.XmlBinding, props);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}


}
