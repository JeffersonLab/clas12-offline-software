/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jlab.io.base.DataDescriptor;
import org.jlab.utils.TablePrintout;

/**
 *
 * @author gavalian
 */
public class EvioDataDescriptor implements DataDescriptor {
	private String descriptorName = "UNDEF";
	private Integer descriptorContainerTag = 0;
	private Integer descriptorContainerNum = 0;
	private ArrayList<String> entryNames = new ArrayList<String>();
	private Map<String, EvioDataDescriptorEntry> descriptorEntries = new LinkedHashMap<String, EvioDataDescriptorEntry>();
	private HashMap<String, String> descriptorProperties = new HashMap<String, String>();

	public EvioDataDescriptor(String name, String parenttag, String containertag) {
		this.descriptorName = name;
		descriptorProperties.put("parent_tag", parenttag);
		descriptorProperties.put("container_tag", containertag);
	}

	public EvioDataDescriptor(String format) {
		this.init(format);
	}

	public void init(String s) {
		descriptorEntries.clear();
		String[] tokens = s.split("/");
		// System.out.println(" N - tokens = " + tokens.length);
		String[] header = tokens[0].split(":");
		this.descriptorName = header[0] + "::" + header[1];
		String section_name = header[1];
		this.descriptorContainerTag = Integer.parseInt(header[2]);
		this.descriptorContainerNum = Integer.parseInt(header[3]);
		this.descriptorProperties.put("parent_tag", header[2]);
		this.descriptorProperties.put("container_tag", header[3]);
		Integer sectionTag = Integer.parseInt(header[3]);

		for (int loop = 1; loop < tokens.length; loop++) {
			String[] entryParams = tokens[loop].split(":");
			this.addEntry(this.descriptorName, entryParams[0], sectionTag, Integer.parseInt(entryParams[1]), entryParams[2]);
		}
		/*
		 * descriptorName = tokens[0];
		 * 
		 * descriptorContainerTag = Integer.parseInt(tokens[1]); descriptorContainerNum = Integer.parseInt(tokens[2]); ArrayList<String> nnn = new
		 * ArrayList<String>(); for(int loop = 3 ; loop < tokens.length; loop+=4){ nnn.add(tokens[loop]); descriptorEntries.put(tokens[loop], new
		 * EvioDataDescriptorEntry("generic",tokens[loop], Integer.parseInt(tokens[loop+1]), Integer.parseInt(tokens[loop+2]), tokens[loop+3] )); }
		 */
		// entryNames = new String[nnn.size()];
		// for(int loop = 0; loop < nnn.size(); loop++){
		// entryNames[loop] = nnn.get(loop);
		// }
		// entryNames = (String[]) nnn.toArray();
	}

	public void addEntry(String section, String name, Integer tag, Integer num, String type) {
		descriptorEntries.put(name, new EvioDataDescriptorEntry(section, name, tag, num, type));
		entryNames.add(name);
	}

	public String[] getEntryList() {
		// return entryNames;
		String[] entries = new String[descriptorEntries.size()];
		for (int loop = 0; loop < entryNames.size(); loop++) {
			entries[loop] = entryNames.get(loop);
		}
		return entries;
	}

	public String getName() {
		return descriptorName;
	}

	public int getProperty(String property_name, String entry_name) {
		int ret = -1;
		if (descriptorEntries.containsKey(entry_name) == false) {
			System.err.println("[EvioDataDescriptor] ERROR : getProperty requested for " + " unknown filed " + entry_name);
			return ret;
		}

		if (property_name.equals("tag") == true) {
			return descriptorEntries.get(entry_name).tag;
		}

		if (property_name.equals("num") == true) {
			return descriptorEntries.get(entry_name).num;
		}

		if (property_name.equals("type") == true) {
			return descriptorEntries.get(entry_name).type.id();
		}

		return ret;
	}

	public int getProperty(String property_name) {
		if (property_name.equals("tag") == true) {
			return descriptorContainerTag;
		}
		if (property_name.equals("num") == true) {
			return descriptorContainerNum;
		}
		return 0;
	}

	public void show() {
		System.out.println("\n\n>>> BANK name = " + this.getName() + " tag = " + this.getPropertyString("parent_tag"));
		String[] entry_names = this.getEntryList();
		TablePrintout table = new TablePrintout("Column:Tag:Number:Type", "24:8:8:12");
		for (String item : entry_names) {
			String[] tdata = new String[4];
			tdata[0] = item;
			tdata[1] = descriptorEntries.get(item).tag.toString();
			tdata[2] = descriptorEntries.get(item).num.toString();
			tdata[3] = descriptorEntries.get(item).type.stringName();
			table.addData(tdata);
		}
		table.show();
	}

	public String getXML() {
		StringBuilder str = new StringBuilder();

		return str.toString();
	}

	public void setPropertyString(String name, String value) {
		if (descriptorProperties.containsKey(name) == true) {
			descriptorProperties.remove(name);
		}
		descriptorProperties.put(name, value);
	}

	public String getPropertyString(String property_name) {
		if (descriptorProperties.containsKey(property_name) == true) {
			return descriptorProperties.get(property_name);
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.descriptorName.replace("::", ":"));
		str.append(":");
		str.append(this.descriptorProperties.get("parent_tag"));
		str.append(":");
		str.append(this.descriptorProperties.get("container_tag"));

		// for(Map.Entry<String,EvioDataDescriptorEntry> entry : this.descriptorEntries.entrySet()){
		for (String item : this.getEntryList()) {
			str.append("/");
			str.append(this.descriptorEntries.get(item).name);
			str.append(":");
			str.append(this.descriptorEntries.get(item).num);
			str.append(":");
			str.append(this.descriptorEntries.get(item).type.stringName());
		}
		return str.toString();
	}

	public static void main(String[] args) {
		EvioDataDescriptor desc = new EvioDataDescriptor("DC::true", "120", "0");
		desc.init("DC:true:1200:1201/sector:1:float64/layer:2:int64/wire:3:int64");
		desc.show();
		System.out.println(desc);
		EvioDataDescriptor desc2 = new EvioDataDescriptor(desc.toString());
		desc2.show();
	}

	public boolean hasEntry(String entry) {
		return this.descriptorEntries.containsKey(entry);
	}

	public boolean hasEntries(String... entries) {
		for (String item : entries)
			if (this.hasEntry(item) == false)
				return false;
		return true;
	}
}
