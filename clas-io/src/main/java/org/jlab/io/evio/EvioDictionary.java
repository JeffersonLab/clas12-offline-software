package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;
import org.jlab.utils.FileUtils;
import org.jlab.utils.StringUtils;
import org.jlab.utils.TablePrintout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class EvioDictionary implements DataDictionary {

	String dict;
	long checksum;
	HashMap<String, EvioDescriptor> descriptors;

	public EvioDictionary() {
		String CLAS12DIR = System.getenv("CLAS12DIR");
		if (CLAS12DIR == null) {
			System.out.println("---> Warning the CLAS12DIR environment is not defined.");
			return;
		}

		String dict_path = CLAS12DIR + "/lib/bankdefs/clas12";

		try {
			File dict_dir = new File(dict_path);
			this.setDictionary(dict_dir);
			/*
			 * } catch (NoSuchAlgorithmException | ParserConfigurationException | SAXException | IOException | TransformerException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); }
			 */ } catch (ParserConfigurationException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.err.println("[EvioDictionary] ----> loaded from directory : " + dict_path);
		System.err.println("[EvioDictionary] ----> number of descriptors : " + descriptors.size());

		// if()
	}

	public EvioDictionary(File dict_dir) {
		try {
			this.setDictionary(dict_dir);
			/*
			 * } catch (NoSuchAlgorithmException | ParserConfigurationException | SAXException | IOException | TransformerException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); }
			 */ } catch (ParserConfigurationException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SAXException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(EvioDictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void init(String xml_dict) {
		/*
		 * try { this.setDictionary(xml_dict); } catch (NoSuchAlgorithmException e) { e.printStackTrace(); } catch (ParserConfigurationException e) {
		 * e.printStackTrace(); } catch (SAXException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		 * 
		 */
	}

	public String getXML() {
		return dict;
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
		// return (String[]) descriptors.keySet().toArray();
	}

	public DataDescriptor getDescriptor(String bank_name) {
		return this.descriptors.get(bank_name);
	}

	public final void setDictionary(String xml_str) throws NoSuchAlgorithmException, ParserConfigurationException, SAXException, IOException {
		long new_checksum = StringUtils.calculateChecksum(xml_str);
		if (new_checksum != this.checksum) {
			this.dict = xml_str;
			this.checksum = new_checksum;
			this.rebuildHashMaps();
		}
	}

	private Document getDocument() throws ParserConfigurationException, SAXException, IOException {
		if (this.dict == null) {
			return null;
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(this.dict));
		Document doc = docBuilder.parse(is);
		doc.getDocumentElement().normalize();
		return doc;
	}

	private void rebuildHashMaps_2p0() throws ParserConfigurationException, SAXException, IOException {
		Document doc = this.getDocument();
		if (doc == null) {
			return;
		}
		this.descriptors = new HashMap<String, EvioDescriptor>();
		NodeList evio_dict_nodelist = doc.getElementsByTagName("evio_dictionary");

	}

	private void rebuildHashMaps() throws ParserConfigurationException, SAXException, IOException {
		Document doc = this.getDocument();
		if (doc == null) {
			return;
		}
		this.descriptors = new HashMap<String, EvioDescriptor>();
		NodeList evio_dict_nodelist = doc.getElementsByTagName("evio_dictionary");
		for (int i = 0; i < evio_dict_nodelist.getLength(); i++) {
			Element evio_dict = (Element) evio_dict_nodelist.item(i);
			NodeList bank_nodelist = evio_dict.getElementsByTagName("bank");
			for (int j = 0; j < bank_nodelist.getLength(); j++) {
				Element bank_elem = (Element) bank_nodelist.item(j);

				String name = bank_elem.getAttribute("name");
				if (name != null) {
					int tag = Integer.parseInt(bank_elem.getAttribute("tag"));

					ArrayList<String> col_names = new ArrayList<String>();
					HashMap<String, Integer> nums = new HashMap<String, Integer>();
					HashMap<String, Integer> types = new HashMap<String, Integer>();

					NodeList col_node_list = bank_elem.getElementsByTagName("column");

					for (int k = 0; k < col_node_list.getLength(); k++) {
						Element col_elem = (Element) col_node_list.item(k);
						String col_name = col_elem.getAttribute("name");
						Integer num = Integer.parseInt(col_elem.getAttribute("num"));
						String type_str = col_elem.getAttribute("type");

						Integer type;
						if (type_str.equals("int32")) {
							type = 1;
						} else if (type_str.equals("float32")) {
							type = 2;
						} else if (type_str.equals("float64")) {
							type = 3;
						} else {
							type = 0;
						}

						col_names.add(col_name);
						nums.put(col_name, num);
						types.put(col_name, type);
					}

					String[] cols = new String[col_names.size()];
					cols = col_names.toArray(cols);

					EvioDescriptor desc = new EvioDescriptor(name, tag, cols, nums, types);

					this.descriptors.put(name, desc);
				}
			}
		}
	}

	public void setDictionary(File dict_dir) throws ParserConfigurationException, SAXException, IOException, NoSuchAlgorithmException, TransformerException {
		ArrayList<String> ignorePrefixes = new ArrayList<String>();
		ignorePrefixes.add(".");
		ignorePrefixes.add("_");

		ArrayList<String> xmlFileList = null;
		if (dict_dir.isDirectory()) {
			String dirname = dict_dir.getName();
			boolean ignore = false;
			for (String p : ignorePrefixes) {
				if (dirname.startsWith(p)) {
					ignore = true;
					break;
				}
			}
			if (!ignore) {
				xmlFileList = FileUtils.filesInFolder(dict_dir, "xml", ignorePrefixes);
				// System.out.println("Reading in EVIO dictionaries:");
				// for (String x : xmlFileList) {
				// System.out.println(" "+x);
				// }
			}
		} else {
			xmlFileList = new ArrayList<String>();
			xmlFileList.add(dict_dir.getAbsolutePath());
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("evio_dictionary");
		doc.appendChild(root);

		for (String xmlFile : xmlFileList) {
			Document subdoc = docBuilder.parse(xmlFile);
			EvioDictionary.appendToDict(doc, subdoc);
		}

		doc.normalize();

		this.setDictionary(StringUtils.xmlToString(doc));
	}

	private static void appendToDict(Document target, Document source) {
		Element root = (Element) (target.getElementsByTagName("evio_dictionary").item(0));
		Element evio_dict = (Element) (source.getElementsByTagName("evio_dictionary").item(0));

		NodeList bank_list = evio_dict.getElementsByTagName("bank");
		for (int i = 0; i < bank_list.getLength(); i++) {
			Element bank = (Element) bank_list.item(i);
			String bank_name = bank.getAttribute("name");
			int tag = Integer.parseInt(bank.getAttribute("tag"));

			Element bank_elem = target.createElement("bank");
			bank_elem.setAttribute("name", bank_name);
			bank_elem.setAttribute("tag", Integer.toString(tag));

			NodeList col_list = bank.getElementsByTagName("column");
			for (int j = 0; j < col_list.getLength(); j++) {
				Element col = (Element) col_list.item(j);

				String col_name = col.getAttribute("name");
				if (!col_name.equals("")) {
					String num_str = col.getAttribute("num");
					String type_str = col.getAttribute("type");
					if (!num_str.equals("") && !type_str.equals("")) {
						int num = Integer.parseInt(num_str);

						Element col_elem = target.createElement("column");
						col_elem.setAttribute("name", col_name);
						col_elem.setAttribute("num", Integer.toString(num));
						col_elem.setAttribute("type", type_str);

						bank_elem.appendChild(col_elem);
					}
				}
			}

			root.appendChild(bank_elem);
		}
	}

	public void show() {
		TablePrintout table = new TablePrintout("Bank:Columns:Tag:Number", "42:8:8:8");
		for (Map.Entry<String, EvioDescriptor> entry : descriptors.entrySet()) {
			String name = entry.getKey();
			String[] info = new String[4];
			info[0] = name;
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

	public static void main(String[] args) {
		String indir = args[0];
		System.out.println(" ---> " + args[0]);
		EvioDictionary dict = new EvioDictionary(new File(indir));
		int tag = dict.getDescriptor("DC").getProperty("tag");
		int num = dict.getDescriptor("DC").getProperty("num", "x_avg");
		int typ = dict.getDescriptor("DC").getProperty("type", "x_avg");
		System.out.println("DC.x_avg (" + tag + ", " + num + ", " + typ + ")");
	}

	public DataBank createBank(String name, int rows) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}
}
