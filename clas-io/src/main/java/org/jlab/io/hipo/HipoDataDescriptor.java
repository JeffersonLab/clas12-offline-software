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
public class HipoDataDescriptor implements DataDescriptor {

    private final Schema hipoSchema = new Schema();
    
    public void init(Schema schema){
        hipoSchema.copy(schema);
    }
    
    public void init(String s) {
        this.hipoSchema.setFromText(s);
    }

    public String[] getEntryList() {
        String[] entries = new String[1];
        return entries;
    }

    public String getXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean hasEntry(String entry) {
        return (this.hipoSchema.getEntry(entry)!=null);
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

    public String getName() {
        return this.hipoSchema.getName();
    }
    
    public Schema getSchema(){ return this.hipoSchema;}
}
