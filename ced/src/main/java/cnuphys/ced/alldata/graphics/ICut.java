package cnuphys.ced.alldata.graphics;

import cnuphys.bCNU.xml.XmlPrintStreamWritable;

public interface ICut extends XmlPrintStreamWritable {

	public boolean pass(double val);
	
	public boolean pass(int index);
	
	public void setActive(boolean active);
	
	public boolean isActive();
	
	public String getName();

	public String plotText();
	
	public String getCutType();
}
