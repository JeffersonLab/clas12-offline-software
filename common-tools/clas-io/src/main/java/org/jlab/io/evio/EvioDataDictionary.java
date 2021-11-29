package org.jlab.io.evio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.utils.DictionaryLoader;
import org.jlab.utils.FileUtils;
import org.jlab.utils.TablePrintout;

/**
 *
 * @author gavalian
 */
public class EvioDataDictionary implements DataDictionary {

	Logger LOGGER = Logger.getLogger(EvioDataDictionary.class.getName());
	private HashMap<String, EvioDataDescriptor> descriptors = new HashMap<String, EvioDataDescriptor>();

	public EvioDataDictionary() {

	}

	public EvioDataDictionary(String env, String relative_path) {
		this.initWithEnv(env, relative_path);
	}

	public EvioDataDictionary(String directory) {
		this.initWithDir(directory);
	}

	public void init(String format) {

	}

	public String getXML() {
		return "some xml";
	}

	public String[] getDescriptorList() {
		String[] names = new String[descriptors.keySet().size()];
		// ArrayList<String> array = new ArrayList();
		int icounter = 0;
		for (String key : descriptors.keySet()) {
			names[icounter] = key;
			icounter++;
		}
		return names;
		// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public DataDescriptor getDescriptor(String desc_name) {
		if (descriptors.containsKey(desc_name) == true) {
			return descriptors.get(desc_name);
		}
		return null;
	}

	public final void initWithEnv(String envname, String relative_path) {
		String ENVDIR = System.getenv(envname);
		if (ENVDIR == null) {
			LOGGER.log(Level.SEVERE,"---> Warning the CLAS12DIR environment is not defined.");
			return;
		}
		String dict_path = ENVDIR + "/" + relative_path;
		this.initWithDir(dict_path);
	}

	public final void clear() {
		this.descriptors.clear();
	}

	public final void initWithEnv(String envname) {
		this.initWithEnv(envname, "etc/bankdefs/clas12");
	}

	public final void initWithDir(String dirname) {
		ArrayList<String> ignorePrefixes = new ArrayList<String>();
		ignorePrefixes.add(".");
		ignorePrefixes.add("_");
		LOGGER.log(Level.INFO,"[EvioDataDictionary]---> loading bankdefs from directory : " + dirname);
		File dict_dir = new File(dirname);

		if (dict_dir.exists() == false) {
			LOGGER.log(Level.SEVERE,"[EvioDataDictionary]---> Directory does not exist.....");
			return;
		}

		ArrayList<String> xmlFileList = FileUtils.filesInFolder(dict_dir, "xml", ignorePrefixes);
		LOGGER.log(Level.INFO,"[EvioDataDictionary]------> number of XML files located  : " + xmlFileList.size());
		Integer counter = 0;
		for (String file : xmlFileList) {
			ArrayList<EvioDataDescriptor> descList = DictionaryLoader.getDescriptorsFromFile(file);
			// ArrayList<String> descList = DictionaryLoader.descriptorParseXMLtoString(file);
			for (EvioDataDescriptor desc : descList) {
				descriptors.put(desc.getName(), desc);
				// System.out.println(" rev = " + counter + " desc = " + desc.getName());
				// ArrayList<EvioDataDescriptor> descList =
				// EvioDataDescriptor desc = new EvioDataDescriptor();
				// desc.init(format);
				// descriptors.put(desc.getName(), desc);
				counter++;
			}
		}
		LOGGER.log(Level.INFO,"[EvioDataDictionary]--> total number of descriptors found  : " + counter.toString());
	}

	public void show() {
		TablePrintout table = new TablePrintout("Bank:Columns:Tag:Number", "42:8:8:8");
		for (Map.Entry<String, EvioDataDescriptor> entry : descriptors.entrySet()) {
			// System.out.println(" step 1 ");
			String name = entry.getKey();
			String[] info = new String[4];
			// System.out.println(" step 2 ");
			info[0] = name;
			// System.out.println(" step 3 ");
			Integer nentries = descriptors.get(name).getEntryList().length;
			info[1] = nentries.toString();
			Integer tag = descriptors.get(name).getProperty("tag");
			Integer num = descriptors.get(name).getProperty("num");
			info[2] = tag.toString();
			info[3] = num.toString();
			table.addData(info);
		}
		table.show();
	}

	/**
	 * returns a name for the variable given tag and number.
	 * 
	 * @param tag
	 *            tag of the variable
	 * @param num
	 *            num of the variable
	 * @return
	 */
	public String getNameByTagNum(int tag, int num) {

		String name = "undefined";
		for (Map.Entry<String, EvioDataDescriptor> desc : descriptors.entrySet()) {
			String[] entries = desc.getValue().getEntryList();
			for (String entryname : entries) {
				if (num == 0) {
					if (Integer.parseInt(desc.getValue().getPropertyString("parent_tag")) == tag) {
						return desc.getValue().getName().split("::")[0];
					}
					if (Integer.parseInt(desc.getValue().getPropertyString("container_tag")) == tag) {
						return desc.getValue().getName();
					}
				}
				if (desc.getValue().getProperty("tag", entryname) == tag && desc.getValue().getProperty("num", entryname) == num) {
					return new String(desc.getKey() + "." + entryname);
				}
			}
		}
		return name;
	}

	public DataBank createBank(String name, int rows) {
		if (descriptors.containsKey(name) == false) {
			LOGGER.log(Level.SEVERE,"[EvioDataDictionary]:: ERROR ---> no descriptor with name = " + name + " is found");
		}
		EvioDataDescriptor desc = descriptors.get(name);
		EvioDataBank bank = new EvioDataBank(desc);
		bank.allocate(rows);
		return bank;
	}

	public void addDescriptor(EvioDataDescriptor desc) {
		this.descriptors.put(desc.getName(), desc);
	}
}
