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
public enum DataEntryType {
    
    UNDEFINED (0,0,"undefined"),
    BYTE      (1,1,"int8"),
    SHORT     (2,2,"int16"),
    INTEGER   (3,4,"int32"),
    LONG      (4,8,"int64"),
    FLOAT     (5,4,"float32"),
    DOUBLE    (6,8,"float64");
    //CHAR8     (7,1,"CHAR8"),
    //INT32     (8,4,"INT32");
    
    
    private final int    typeId;
    private final int    typeSize;
    private final String typeName;
    
    private DataEntryType(int tpid, int size, String name){
        this.typeId    = tpid;
        this.typeSize  = size;
        this.typeName  = name;
    }

    public int id(){
        return typeId;
    }

    public int size(){
        return typeSize;
    }
    
    public String stringName(){
        return typeName;
    }
    
    public static DataEntryType getType(String type){
        for(DataEntryType dt: DataEntryType.values()){
            if (dt.stringName().equals(type.trim())) return dt;
        }
        return UNDEFINED;
    }
    
    public static DataEntryType getType(int _id){
        for(DataEntryType dt: DataEntryType.values()){
            if (dt.id()==_id) return dt;
        }
        return UNDEFINED;
    }
}
