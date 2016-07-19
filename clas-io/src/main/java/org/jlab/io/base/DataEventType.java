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
public enum DataEventType {
    UNDEFINED          ( 0, "UNDEFINED"),
    EVENT_SINGLE       ( 1, "EVENT_SINGLE"),
    EVENT_START        ( 2, "EVENT_START"),
    EVENT_ACCUMULATE   ( 3, "EVENT_ACCUMULATE"),
    EVENT_STOP         ( 4, "EVENT_STOP");    
    
    private final int typeId;
    private final String typeName;
    
    DataEventType(){
        typeId = 0;
        typeName = "UNDEFINED";
    }
    
    DataEventType(int id, String name){
        typeId = id;
        typeName = name;
    }
    
    public DataEventType getType(int typeid){
        for(DataEventType id: DataEventType.values())
            if (id.typeId == typeid) 
                return id;
        return UNDEFINED;
    }
}
