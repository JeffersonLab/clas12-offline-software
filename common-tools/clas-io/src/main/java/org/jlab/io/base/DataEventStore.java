/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.base;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class DataEventStore {
    private TreeMap<String,DataBank> store = new TreeMap<String,DataBank>();
    
    
    
    public DataEventStore(){
        
    }
    
    public DataEventStore(DataEvent event){
        this.init(event);
    }
    
    public final void init(DataEvent event){
        String[] banks = event.getBankList();
        store.clear();
        for(String bank : banks){
            DataBank  db = event.getBank(bank);
            if(db!=null){
                store.put(bank, db);
            } else {
                System.err.println("[DataEventStore::init] ----> error : reading bank "
                + " [" + bank + "]  failed....");
            }
        }
    }
        
    public DataBank getBank(String name){
        return this.store.get(name);
    }
    
    public boolean hasBank(String name){
        return this.store.containsKey(name);
    }        
    
    public void show(){
        for(Map.Entry<String,DataBank> bank : this.store.entrySet()){
            System.out.println(String.format("| %-24s | %6d | %6d |", bank.getKey(),
                    bank.getValue().rows(),bank.getValue().rows()));
        }
    }
}
