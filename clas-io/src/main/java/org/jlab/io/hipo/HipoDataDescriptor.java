/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.List;
import org.jlab.hipo.data.HipoNodeType;
import org.jlab.hipo.schema.Schema;
import org.jlab.hipo.schema.Schema.SchemaEntry;
import org.jlab.io.base.DataDescriptor;

/**
 *
 * @author gavalian
 */
public class HipoDataDescriptor implements DataDescriptor {

    private final Schema hipoSchema = new Schema();
    
    public HipoDataDescriptor(){

    }
     
    public HipoDataDescriptor(Schema schema){
        this.init(schema);
    }
    
    public final void init(Schema schema){
        hipoSchema.copy(schema);
    }
    
    public void init(String s) {
        this.hipoSchema.setFromText(s);
    }

    @Override
    public String[] getEntryList() {
        List<String>  entryList = hipoSchema.schemaEntryList();
        String[] entries = new String[entryList.size()];
        int counter = 0;
        for(int i = 0; i < entryList.size(); i++){
            entries[i] = entryList.get(i);
        }
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
        if(property_name.compareTo("type")==0){
            SchemaEntry entry = this.hipoSchema.getEntry(entry_name);
            if(entry!=null){
                if(entry.getType()==HipoNodeType.BYTE)  return 1;
                if(entry.getType()==HipoNodeType.SHORT) return 2;
                if(entry.getType()==HipoNodeType.INT) return 3;
                if(entry.getType()==HipoNodeType.FLOAT) return 5;
                if(entry.getType()==HipoNodeType.DOUBLE) return 6;
                return 0;
            }
        }
        return 0;
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
        System.out.println(this.hipoSchema.toString());
    }

    public String getName() {
        return this.hipoSchema.getName();
    }
    
    public Schema getSchema(){ return this.hipoSchema;}
}
