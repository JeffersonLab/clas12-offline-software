/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import org.jlab.io.base.DataEntryType;

/**
 *
 * @author gavalian
 */
public class EvioDataDescriptorEntry {
    String  section;
    String  name;
    Integer tag;
    Integer num;
    DataEntryType type;
    
    public EvioDataDescriptorEntry(String _s,String _n, Integer _t, Integer _nn, String _type)
    {
        section = _s;
        name = _n;
        tag  = _t;
        num  = _nn;
        String convertedType = this.getType(_type);
        type = DataEntryType.getType(convertedType);
    }
    
    private String  getType(String type){
        if(type.compareTo("CHAR8")==0) return "int8";
        if(type.compareTo("INT32")==0) return "int32";
        if(type.compareTo("LONG64")==0) return "int64";
        return type;
    }
}
