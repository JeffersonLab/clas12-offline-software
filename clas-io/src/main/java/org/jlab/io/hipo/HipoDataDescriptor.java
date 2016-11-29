/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import org.jlab.hipo.schema.Schema;
import org.jlab.io.base.DataDescriptor;

/**
 *
 * @author gavalian
 */
public class HipoDataDescriptor extends Schema implements DataDescriptor {

    public void init(String s) {
        this.setFromText(s);
    }

    public String[] getEntryList() {
        String[] entries = new String[1];
        return entries;
    }

    public String getXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean hasEntry(String entry) {
        return (this.getEntry(entry)!=null);
    }

    public boolean hasEntries(String... entries) {
        for(String entry : entries){
            if(hasEntry(entry)==false) return false;
        }
        return true;
    }

    public int getProperty(String property_name, String entry_name) {
        return 1;
    }

    public int getProperty(String property_name) {
        return 1;
    }

    public void setPropertyString(String name, String value) {
        
    }

    public String getPropertyString(String property_name) {
        return "undefined";
    }

    public void show() {
        
    }
    
}
