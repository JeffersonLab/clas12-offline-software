/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.schema.Schema;
import org.jlab.jnp.hipo.schema.SchemaFactory;


/**
 *
 * @author gavalian
 */
public class Hipo3DataDictionary implements DataDictionary {
    
    private final Map<String,Hipo3DataDescriptor>  descriptors = new HashMap<String,Hipo3DataDescriptor>();
    
    public Hipo3DataDictionary(){
        
        SchemaFactory factory = new SchemaFactory();
        factory.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        List<Schema> entries = factory.getSchemaList();
        //System.out.println(" schema size = " + entries.size());
        for(Schema sch : entries){
            Hipo3DataDescriptor desc = new Hipo3DataDescriptor(sch);
            descriptors.put(desc.getName(), desc);
            //System.out.println("name = " + sch.getName() + "  desc = " + desc.getName());
        }
        System.out.println("  >>>>> loading default dictionary : entries = " + descriptors.size());
    }
    
    @Override
    public void init(String format) {
        System.out.println("---- INITIALIZATION NOT IMPLEMENTED ----");
    }

    @Override
    public String getXML() {
        return "<xml></xml>";
    }

    
    
    @Override
    public String[] getDescriptorList() {
        Set<String>  list = this.descriptors.keySet();
        int counter = 0;
        String[] tokens = new String[list.size()];
        for(String item : list){
            tokens[counter] = item;
            counter++;
        }
        return tokens;
    }

    @Override
    public DataDescriptor getDescriptor(String desc_name) {
        return descriptors.get(desc_name);
    }

    @Override
    public DataBank createBank(String name, int rows) {
        Map<Integer,HipoNode>  map = descriptors.get(name).getSchema().createNodeMap(rows);
        Hipo3DataBank bank = new Hipo3DataBank(map,descriptors.get(name).getSchema());
        return bank;
    }
    
    public static void main(String[] args){
        System.setProperty("CLAS12DIR", "/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava");
        Hipo3DataDictionary dict = new Hipo3DataDictionary();
        
        String[] list = dict.getDescriptorList();
        for(String item : list){
            System.out.println("---> " + item);
            Hipo3DataDescriptor desc = (Hipo3DataDescriptor) dict.getDescriptor(item);
            String[] entries = desc.getEntryList();
            for(int i = 0; i < entries.length; i++){
                System.out.println("\t\t---> " + entries[i]);
            }
        }
    }
}
