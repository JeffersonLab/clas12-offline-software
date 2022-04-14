/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.groups;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 * @param <T> - indexed collection type
 */
public class IndexedList<T> {

    private final Map<Long,T>  collection = new LinkedHashMap<Long,T>();
    private int                indexSize = 3;
    
    public IndexedList(){
        
    }
    
    public IndexedList(int indsize){
        this.indexSize = indsize;
    }
    
    public void add(T item, int... index){
        if(index.length!=this.indexSize){
            System.out.println("HashCollection:: error can not add item, inconsistency of index count.");
            return;
        }
        long code = IndexGenerator.hashCode(index);
        this.collection.put(code, item);
    }
    
    public boolean hasItem(int... index){
        if(index.length!=this.indexSize) return false;
        long code = IndexGenerator.hashCode(index);
        return this.collection.containsKey(code);
    }
    
    public T getItem(int... index){
        if(index.length!=this.indexSize) return null;
        long code = IndexGenerator.hashCode(index);
        return this.collection.get(code);
    }
    
    public void clear(){this.collection.clear();}
    public int  getIndexSize(){ return this.indexSize;}
    public Map<Long,T> getMap(){ return this.collection;}
    
    public void show(){        
        for(Map.Entry<Long,T>  entry : this.collection.entrySet()){
            System.out.println(String.format("[%s] : ", 
                    IndexGenerator.getString(entry.getKey())) + entry.getValue());
        }
    }
    
    /**
     * Index Generator class for generating a Long number out of 
     * up to 4 indecies.
     */
    public static class IndexGenerator {
        
        static int[] byteShits = new int[]{48,32,16,0};
        
        public static long hashCode(int... indecies){
            long result = (long) 0;
           
            if (indecies.length > byteShits.length) {
                throw new IllegalArgumentException("# indices is larger than "+byteShits.length);
            }

            for(int loop = 0; loop < indecies.length; loop++){
                long patern = (((long) indecies[loop])&0x000000000000FFFF)<<IndexGenerator.byteShits[loop]; 
                result = (result | patern);
            }
            return result;
        }
        
        public static int getIndex(long hashcode, int order){
            int result = (int) (hashcode>>IndexGenerator.byteShits[order])&0x000000000000FFFF;
            return result;
        }
        
        public static String  getString(long hashcode){
            StringBuilder str = new StringBuilder();
            for(int loop = 0; loop <4; loop++){
                str.append(String.format("%5d", IndexGenerator.getIndex(hashcode, loop)));
            }
            return str.toString();
        }
    }
}
