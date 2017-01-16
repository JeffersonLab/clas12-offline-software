/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.base;

/**
 *
 * @author gavalian
 */
public enum DataSourceType {
    
    UNDEFINED    ( 0, "UNDEFINED"),
    FILE         ( 1, "EVENT_SINGLE"),
    STREAM       ( 2, "EVENT_START");
 
    private final int typeId;
    private final String typeName;
    
    DataSourceType(){
        typeId = 0;
        typeName = "UNDEFINED";
    }
    
    DataSourceType(int id, String name){
        typeId = id;
        typeName = name;
    }
    
    public DataSourceType getType(int typeid){
        for(DataSourceType id: DataSourceType.values())
            if (id.typeId == typeid) 
                return id;
        return UNDEFINED;
    }
}
