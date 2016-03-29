/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.config;

import org.clas.config.ConfigurationGroup;
import com.sun.media.jfxmedia.logging.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import org.clas.config.ConfigurationGroup.ConfigurationItem;
import org.clas.utils.CoatUtilsFile;

/**
 *
 * @author gavalian
 */
public class Configuration {
    
    private Map<String,ConfigurationGroup>  cGroups =
            new HashMap<String,ConfigurationGroup>();
    
    private String  configFile   = "";
    private Boolean isPersistant = false;
    private String  configName   = "";

    public Configuration(){
        
    }
    
    public Configuration(String name, String file){
        this.configName = name;
        this.configFile = file;
        this.readFile();
        this.update();
    }
    
    public void readFile(){
        String envResource = CoatUtilsFile.getResourceDir("HOME", ".coat/configurations/"+configFile);
        List<String> configString = CoatUtilsFile.readFile(envResource);
            for(String lines : configString){
                System.out.println(lines);
            }
    }
    
    public void update(){
        String envResource = CoatUtilsFile.getResourceDir("HOME", ".coat/configurations/");
        File  dir = new File(envResource);
        //System.out.println("directory exists ? " + dir.exists());
        if(dir.exists()==false){
            //System.out.println("CREATE DIRECTORY " + envResource);
            Boolean success = dir.mkdirs();
            //System.out.println("CREATE DIRECTORY BOOLEAN " + success);
        }
        String filename = envResource + configFile;
        List<String>  fileLines = new ArrayList<String>();
        for(Map.Entry<String,ConfigurationGroup> entry : this.cGroups.entrySet()){
            fileLines.add(entry.getValue().toString());
        }
        CoatUtilsFile.writeFile(filename, fileLines);
    }
    
    public Node  getConfigPane(){
        Accordion accordion = new Accordion();
        for(Map.Entry<String,ConfigurationGroup> group : this.cGroups.entrySet()){
            accordion.getPanes().add(group.getValue().getGroupPane());
        }
        return accordion;
    }
    
    public void addGroup(ConfigurationGroup group){
        this.cGroups.put(group.getName(), group);
    }
    
    public void addItem(String group, String name, String... values){
        if(this.cGroups.containsKey(group)==false){
            this.cGroups.put(group, new ConfigurationGroup(group));           
        }
        if(this.cGroups.get(group).hasItem(name)==false){
            this.cGroups.get(group).addItem(name, values);
        }
    }
    
    public void addItem(String group, String name, Integer... values){
        if(this.cGroups.containsKey(group)==false){
            this.cGroups.put(group, new ConfigurationGroup(group));           
        }
        if(this.cGroups.get(group).hasItem(name)==false){
            this.cGroups.get(group).addItem(name, values);
        }
    }
    
    public void addItem(String group, String name, Double... values){
        if(this.cGroups.containsKey(group)==false){
            this.cGroups.put(group, new ConfigurationGroup(group));           
        }
        if(this.cGroups.get(group).hasItem(name)==false){
            this.cGroups.get(group).addItem(name, values);
        }
    }
    
    public void show(){
        System.out.println("CONFIGURATION: GROUPS " + this.cGroups.size());
        for(Map.Entry<String,ConfigurationGroup> entry : this.cGroups.entrySet()){
            entry.getValue().show();
        }
    }
    
    public double getAsDouble(String group, String item){
        return this.cGroups.get(group).getAsDouble(item);        
    }
    public int   getAsInteger(String group, String item){
        return this.cGroups.get(group).getAsInteger(item);
    }
    public String getAsString(String group, String item){
        return this.cGroups.get(group).getAsString(item);
    }
    
    public Map<String,ConfigurationGroup> getGroups(){
        return this.cGroups;    
    }
    
    public ConfigurationGroup  getGroup(String group){
        return this.cGroups.get(group);
    }
    
    public void merge(Configuration config){
        for(Map.Entry<String,ConfigurationGroup> entry : config.getGroups().entrySet()){
            if(this.cGroups.containsKey(entry.getKey())==true){
                System.out.println("error: group with name " + entry.getKey() +
                        " already exists.");
            } else {
                this.cGroups.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
