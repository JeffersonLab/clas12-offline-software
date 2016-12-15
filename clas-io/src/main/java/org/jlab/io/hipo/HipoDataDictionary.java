/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jlab.hipo.data.HipoNode;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataDictionary;


/**
 *
 * @author gavalian
 */
public class HipoDataDictionary implements DataDictionary {
    
    private final Map<String,HipoDataDescriptor>  descriptors = new HashMap<String,HipoDataDescriptor>();
    
    public void init(String format) {
        System.out.println("---- INITIALIZATION NOT IMPLEMENTED ----");
    }

    public String getXML() {
        return "<xml></xml>";
    }

    
    
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

    public DataDescriptor getDescriptor(String desc_name) {
        return descriptors.get(desc_name);
    }

    public DataBank createBank(String name, int rows) {
        Map<Integer,HipoNode>  map = descriptors.get(name).getSchema().createNodeMap(rows);
        HipoDataBank bank = new HipoDataBank(map,descriptors.get(name).getSchema());
        return bank;
    }
    
}
