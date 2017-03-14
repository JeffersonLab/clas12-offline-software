package org.jlab.io.base;

public interface DataDictionary {
	void init(String format);
	String getXML();
	String[] getDescriptorList();
	DataDescriptor getDescriptor(String desc_name);
        DataBank       createBank(String name, int rows);
}
