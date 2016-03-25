/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import com.sun.media.jfxmedia.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author gavalian
 */
public class Configuration {
    
    private Map<String,Object>  config = new HashMap<String,Object>();
    private Map<String, Map<String,ConfigurationItem> >  configItems = 
            new HashMap<String, Map<String,ConfigurationItem>>();
    
    public Configuration(){
        
    }
    /**
     * Add string property to the configuration
     * @param system
     * @param item
     * @param value
     * @param options 
     */
    public void addItem(String system, String item, String value, String... options){
        ConfigurationItem<String> citem = new ConfigurationItem<String>(item,value,options);
        
        if(this.configItems.containsKey(system)==false){
            this.configItems.put(system, new HashMap<String,ConfigurationItem>());
        }
        if(this.configItems.get(system).containsKey(item)==true){
            System.out.println("[Configuration::addItem] (error) ---> "+
            " Entry with name [" + item + "]  already exists in system = ["
            + system + "]");
        } else {
            this.configItems.get(system).put(item,citem);
        }
    }
    /**
     * add integer item to configuration.
     * @param system
     * @param item
     * @param value
     * @param options 
     */
    public void addItem(String system, String item, Integer value, Integer... options){

        ConfigurationItem<Integer> citem = new ConfigurationItem<Integer>(item,value,options);
        
        if(this.configItems.containsKey(system)==false){
            this.configItems.put(system, new HashMap<String,ConfigurationItem>());
        }
        if(this.configItems.get(system).containsKey(item)==true){
            System.out.println("[Configuration::addItem] (error) ---> "+
            " Entry with name [" + item + "]  already exists in system = ["
            + system + "]");
        } else {
            this.configItems.get(system).put(item,citem);
        }
    }
    /**
     * add integer property without any limit on what can be set with modifier.
     * @param system
     * @param item
     * @param value 
     */
    public void addItem(String system, String item, Integer value){

        ConfigurationItem<Integer> citem = new ConfigurationItem<Integer>(item,value);
        
        if(this.configItems.containsKey(system)==false){
            this.configItems.put(system, new HashMap<String,ConfigurationItem>());
        }
        
        if(this.configItems.get(system).containsKey(item)==true){
            System.out.println("[Configuration::addItem] (error) ---> "+
            " Entry with name [" + item + "]  already exists in system = ["
            + system + "]");
        } else {
            this.configItems.get(system).put(item,citem);
        }
    }
    
    
    
    public void show(){
        for(Map.Entry<String, Map<String,ConfigurationItem> > entry : this.configItems.entrySet()){
            System.out.println("system ----> " + entry.getKey());
            for(Map.Entry<String,ConfigurationItem> item : entry.getValue().entrySet()){
                System.out.println("\t\t item --> " + item.getKey());
                System.out.println("\t\t\t value --> " + item.getValue().getValue());//.getClass().getName());
                System.out.println("\t\t\t options --> " + item.getKey());
            }
        }
    }
    
    public void setValue(String system, String item, String value){
        ConfigurationItem  cItem = this.configItems.get(system).get(item);
        Set<String>      options = cItem.getOptions();
        if(options.contains(value)==false){
            Logger.logMsg(Logger.ERROR,value + " is not alowed for system="+system + ", item="+item);
            System.out.println(system + " ---> " + cItem);
        } else {
            cItem.setValue(value);
        }
    }    
    /**
     * Merges given configuration with existing configuration.
     * checks are performed to avoid overwriting existing items.
     * @param cfg 
     */
    public void merge(Configuration cfg){
        for(Map.Entry<String, Map<String, ConfigurationItem> > entry : cfg.configItems.entrySet()){
            if(this.configItems.containsKey(entry.getKey())==false){
                this.configItems.put(entry.getKey(), new HashMap<String,ConfigurationItem>());
            }
            for(Map.Entry<String,ConfigurationItem>  item : entry.getValue().entrySet()){
                if(this.configItems.get(entry.getKey()).containsKey(item.getKey())==true){
                    System.out.println("[Configuration::merge] (error) ---> system="
                    + entry.getKey() + " already contains item="+item.getKey());
                } else {
                    this.configItems.get(entry.getKey()).put(item.getKey(), item.getValue());
                }
            }
        }
    }
    /**
     * returns a set of system items.
     * @return 
     */
    public Set<String>  getMapKeys(){
        Set<String> set = new HashSet<String>();
        for(String key : this.configItems.keySet()){
            set.add(key);
        }
        return set;
    }
    /**
     * returns set of item keys for given system
     * @param system
     * @return 
     */
    public Set<String>  getItemKeys(String system){
        Set<String> set = new HashSet<String>();
        if(this.configItems.containsKey(system)==true){
            for(String key : this.configItems.get(system).keySet()){
                set.add(key);
            }
        }
        return set;
    }
    /**
     * returns ConfigurationItem object for given system and given item.
     * @param system
     * @param item
     * @param error
     * @return 
     */
    public ConfigurationItem   getItem(String system, String item, Boolean error){
        if(this.configItems.containsKey(system)==false){
            if(error==true){
                System.out.println("[Configuration::getItem] (error) ---> can not find "
                + " entry with system="+ system);
            }
            return null;
        }
        if(this.configItems.get(system).containsKey(item)==false){
            if(error==true){
                System.out.println("[Configuration::getItem] (error) ---> can not find "
                + " entry with system="+ system + " item="+item);
            }
            return null;
        }
        
        return this.configItems.get(system).get(item);
    }
    
    public static void main(String[] args){
        Logger.setLevel(Logger.DEBUG);
        
        Configuration config = new Configuration();
        config.addItem("DCHB", "torus", 1);
        config.addItem("DCHB", "kalman", "false","true","false");

        config.show();
        config.setValue("DCHB", "kalman", "trop");
        config.show();
    }
    /**
     * Configuration item class for defining configurations.
     * @param <T> 
     */
    public class ConfigurationItem<T> {
        String           itemName = "";
        String           itemType = "string";
        Set<T>      itemOptions  = new HashSet<T>();
        T               itemValue;
        
        public ConfigurationItem(String name, T value){
            this.itemName  = name;
            this.itemValue = value;
        }
        
        public ConfigurationItem(String name, T value, T... options){
            this.itemName  = name;
            this.itemValue = value;
            for(T opt : options){
                itemOptions.add(opt);
            }
        }
        public void     setValue(T value){itemValue = value;};
        public String   getName(){ return itemName;}
        public T        getValue(){ return itemValue;};
        public Set<T>  getOptions(){ return this.itemOptions;}

        @Override
        public String  toString(){
            StringBuilder str = new StringBuilder();
            str.append(this.getName());
            str.append(" : ");
            str.append(this.getValue());
            if(this.itemOptions.size()>0){
                str.append("  { ");
                for(T opt : itemOptions){
                    str.append(opt);
                    str.append(",");
                }
                str.append(" } ");
            }
            return str.toString();
        }
    }
}
