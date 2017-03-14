/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.base;

import java.util.HashMap;

/**
 *
 * @author gavalian
 */
public class BasicDataDescriptor implements DataDescriptor {
	private HashMap<String, String> descProperties = new HashMap<String, String>();
	private String descName = "UNDEF";
	private String jsonObjectString = "";

	public BasicDataDescriptor(String name) {
		descName = name;
	}

	public void init(String s) {
		// JSonObject model = new Json.createObjectBuilder();
	}

	public String[] getEntryList() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public String getName() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public String getXML() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int getProperty(String property_name, String entry_name) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int getProperty(String property_name) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setPropertyString(String name, String value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public String getPropertyString(String property_name) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void show() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public boolean hasEntry(String entry) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public boolean hasEntries(String... entries) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

}
