package org.jlab.io.evio;

import java.util.HashMap;
import java.util.Map;

import org.jlab.io.base.DataDescriptor;
import org.jlab.utils.TablePrintout;

public class EvioDescriptor implements DataDescriptor {

	String[] entries;
	String name;
	int tag;
	HashMap<String, Integer> nums;
	HashMap<String, Integer> types;

	public EvioDescriptor(String name, int tag, String[] col_names, HashMap<String, Integer> nums, HashMap<String, Integer> types) {
		this.name = name;
		this.tag = tag;
		this.entries = col_names;
		this.nums = nums;
		this.types = types;
	}

	public String toString() {
		String ret = this.name + "    tag: " + this.tag + "\n";
		for (String e : this.entries) {
			ret += "    " + this.nums.get(e) + "    " + e + " (" + this.types.get(e) + ")\n";
		}
		return ret;
	}

	public String[] getEntryList() {
		return this.entries;
	}

	public String getName() {
		return this.name;
	}

	public int getProperty(String property_name, String entry_name) {
		int ret = -1;
		if ("tag".equals(property_name)) {
			ret = tag;
		} else if (property_name == "num") {
			ret = this.nums.get(entry_name);
		} else if (property_name == "type") {
			ret = this.types.get(entry_name);
		}
		return ret;
	}

	public int getProperty(String property_name) {
		int ret = -1;
		if (property_name == "tag") {
			ret = tag;
		}
		return ret;
	}

	public void init(String s) {
		// do nothing for EVIO
	}

	public void show() {
		System.out.println("-----> DataBankDescriptor NAME = " + this.name);
		TablePrintout table = new TablePrintout("Column:Tag:Number:Type", "24:8:8:8");
		Integer banktag = tag;
		for (Map.Entry<String, Integer> item : nums.entrySet()) {
			String[] tdata = new String[4];
			tdata[0] = item.getKey();
			tdata[1] = banktag.toString();
			tdata[2] = item.getValue().toString();
			tdata[3] = types.get(item.getKey()).toString();
			table.addData(tdata);
			/*
			 * System.out.println(String.format("---------> %12s  tag=%5d  num=%5d  type=%5d", item.getKey(),tag,item.getValue(),types.get(item.getKey())));
			 */
		}
		table.show();
	}

	public int getType(String name) {
		if (this.types.containsKey(name) == true) {
			return this.types.get(name);
		}
		return -1;
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
		return this.types.containsKey(entry);
	}

	public boolean hasEntries(String... entries) {
		for (String item : entries)
			if (this.hasEntry(item) == false)
				return false;
		return true;
	}
}
