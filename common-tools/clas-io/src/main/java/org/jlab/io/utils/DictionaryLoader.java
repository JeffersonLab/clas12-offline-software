/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jlab.io.evio.EvioDataDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author gavalian
 */
public class DictionaryLoader {
    
    
    public static ArrayList<EvioDataDescriptor> getDescriptorsFromFile(String xmlfile){
        ArrayList<EvioDataDescriptor> list = new ArrayList<EvioDataDescriptor>();
        
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();            
            Document doc = docBuilder.parse(xmlfile);
            
            NodeList evio_dict_nodelist = doc.getElementsByTagName("evio_dictionary");
            for (int b = 0; b < evio_dict_nodelist.getLength(); b++) {
                Element evio_nodes = (Element) evio_dict_nodelist.item(b);
                NodeList evio_bank_nodes = evio_nodes.getElementsByTagName("bank");
                for (int s = 0; s < evio_bank_nodes.getLength(); s++) {
                    Element evio_bank = (Element) evio_bank_nodes.item(s);
                    ArrayList<EvioDataDescriptor> descList = DictionaryLoader.parseBankXMLtoDescriptorList(evio_bank);
                    for(EvioDataDescriptor item : descList){
                        list.add(item);
                    }
                    //String oneBank = DictionaryLoader.parseBankXMLtoString(evio_bank);
                    //dictionary_list.add(oneBank);
                }

            }
            return list;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
                Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    
    public static ArrayList<String> descriptorParseXMLtoString(String xmlfile) {

        ArrayList<String> dictionary_list = new ArrayList<String>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();            
            Document doc = docBuilder.parse(xmlfile);
            
            NodeList evio_dict_nodelist = doc.getElementsByTagName("evio_dictionary");
            for (int b = 0; b < evio_dict_nodelist.getLength(); b++) {
                Element evio_nodes = (Element) evio_dict_nodelist.item(b);
                NodeList evio_bank_nodes = evio_nodes.getElementsByTagName("bank");
                for (int s = 0; s < evio_bank_nodes.getLength(); s++) {
                    
                   Element evio_bank = (Element) evio_bank_nodes.item(s);
                   String oneBank = DictionaryLoader.parseBankXMLtoString(evio_bank);
                   dictionary_list.add(oneBank);
                }

            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
                Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return dictionary_list;
    }
    /**
     * Parse an XML string into a descriptors.
     * @param xmlstring
     * @return 
     */
    public static ArrayList<String> parseXMLString(String xmlstring){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlstring));
            Document doc = docBuilder.parse(is);
            return DictionaryLoader.parseDocumentXMLtoString(doc);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DictionaryLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /**
     * 
     * @param doc
     * @return 
     */
    public static ArrayList<String> parseDocumentXMLtoString(Document doc){
        ArrayList<String> dictionary_list = new ArrayList<String>();
        NodeList evio_bank_nodes = doc.getElementsByTagName("bank");
        for (int s = 0; s < evio_bank_nodes.getLength(); s++) {
            Element evio_bank = (Element) evio_bank_nodes.item(s);
            String oneBank = DictionaryLoader.parseBankXMLtoString(evio_bank);
            dictionary_list.add(oneBank);
        }
        return dictionary_list;
    }
    
    public static String parseBankXMLtoString(Element bankElement){
        StringBuilder str = new StringBuilder();
        String bank_name = bankElement.getAttribute("name");
        String bank_tag  = bankElement.getAttribute("tag");                    
        str.append(bank_name);
        str.append(":");
        str.append(bank_tag);
        str.append(":0");
        NodeList evio_section = bankElement.getElementsByTagName("section");
        for(int e = 0 ; e < evio_section.getLength(); e++){                        
            Element evio_entry = (Element) evio_section.item(e);
            int section_tag = Integer.parseInt(evio_entry.getAttribute("tag"));
            NodeList evio_columns = evio_entry.getElementsByTagName("column");
            for(int c = 0 ; c < evio_columns.getLength(); c++){
                Element evio_col = (Element) evio_columns.item(c);
                String entry_name  = evio_col.getAttribute("name");
                String entry_num   = evio_col.getAttribute("num");
                String entry_type  = evio_col.getAttribute("type");
                str.append(":");
                str.append(entry_name);
                str.append(":");
                str.append(section_tag);
                str.append(":");
                str.append(entry_num);
                str.append(":");
                str.append(entry_type);
                
                //System.err.println("Entry = " + entry_name + " " 
                //        + section_tag + "  " + entry_num 
                //        + "  " + entry_type);
            }
        }
        System.err.println("--> " + str.toString());
        //dictionary_list.add(str.toString());
        return str.toString();
    }
    
    public static ArrayList<EvioDataDescriptor> parseBankXMLtoDescriptorList(Element bankElement){
        ArrayList<EvioDataDescriptor> list = new ArrayList<EvioDataDescriptor>();

        String bank_name = bankElement.getAttribute("name");
        String bank_tag  = bankElement.getAttribute("tag");
        NodeList evio_section = bankElement.getElementsByTagName("section");

        for(int e = 0 ; e < evio_section.getLength(); e++){
            StringBuilder str = new StringBuilder();
            Element evio_entry = (Element) evio_section.item(e);
            
            Integer    section_tag  = Integer.parseInt(evio_entry.getAttribute("tag"));
            String section_name = evio_entry.getAttribute("name");
            str.append(bank_name);
            str.append("::");
            str.append(section_name);
            EvioDataDescriptor desc = new EvioDataDescriptor(str.toString(),bank_tag,section_tag.toString());
            NodeList evio_columns = evio_entry.getElementsByTagName("column");
            for(int c = 0 ; c < evio_columns.getLength(); c++){
                Element evio_col = (Element) evio_columns.item(c);
                
                String entry_name  = evio_col.getAttribute("name");
                String entry_num   = evio_col.getAttribute("num");
                String entry_type  = evio_col.getAttribute("type");
                desc.addEntry(section_name, entry_name, section_tag,
                        Integer.parseInt(entry_num), entry_type);
                //System.err.println("Entry = " + str.toString() + "  " + entry_name + " " 
                //        + section_tag + "  " + entry_num 
                //        + "  " + entry_type);
            }
            list.add(desc);
        }
        //System.err.println("--> " + str.toString());
        //dictionary_list.add(str.toString());
        //return str.toString();
        return list;
    }
}
