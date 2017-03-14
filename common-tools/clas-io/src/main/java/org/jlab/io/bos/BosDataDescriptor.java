/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import java.util.HashMap;
import java.util.Map;

import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEntryType;

/**
 *
 * @author gavalian
 */
public class BosDataDescriptor implements DataDescriptor {
	private String bankname = "UNDEF";

	private HashMap<String, Integer> entryOffsets = new HashMap<String, Integer>();
	private HashMap<String, DataEntryType> entryTypes = new HashMap<String, DataEntryType>();
	private int totalRowSize = 0;

	public BosDataDescriptor() {

	}

	public BosDataDescriptor(String name) {
		bankname = name;
	}

	public void init(String s) {
		String[] entries = s.split(":");

		int startOffset = 0;
		for (String entry : entries) {
			String[] pair = entry.split("/");
			DataEntryType dt = DataEntryType.getType(pair[1]);
			entryTypes.put(pair[0], dt);
			entryOffsets.put(pair[0], startOffset);
			startOffset += dt.size();
		}
		totalRowSize = startOffset;
	}

	public String[] getEntryList() {
		String[] entrylist = new String[entryTypes.size()];
		int icount = 0;
		for (Map.Entry<String, Integer> entry : entryOffsets.entrySet()) {
			entrylist[icount] = entry.getKey();
			icount++;
		}
		return entrylist;
	}

	public String getName() {
		return bankname;
	}

	public int getProperty(String property_name, String entry_name) {
		if (property_name.compareTo("offset") == 0) {
			return entryOffsets.get(entry_name);
		}
		if (property_name.compareTo("type") == 0) {
			return entryTypes.get(entry_name).id();
		}
		return 0;
	}

	public int getProperty(String property_name) {
		if (property_name.compareTo("banksize") == 0)
			return totalRowSize;
		return 0;
	}

	public void show() {
		System.out.println(">>>>>>>>>>>>>>> BANK " + this.getName() + "  ***** SIZE = " + this.getProperty("banksize"));
		for (Map.Entry<String, Integer> entry : entryOffsets.entrySet()) {
			System.out.println(String.format("%12s : %6d %6d ", entry.getKey(), entry.getValue(), entryTypes.get(entry.getKey()).id()));
		}
	}

	public String getXML() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setPropertyString(String name, String value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public String getPropertyString(String property_name) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public boolean hasEntry(String entry) {
		return this.entryTypes.containsKey(entry);
	}

	public boolean hasEntries(String... entries) {
		for (String item : entries)
			if (this.hasEntry(item) == false)
				return false;
		return true;
	}

}
