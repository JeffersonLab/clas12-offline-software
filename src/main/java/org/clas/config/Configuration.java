/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.config;

import org.clas.config.ConfigurationGroup;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import org.clas.utils.CoatUtilsFile;
import org.clas.utils.CoatUtilsJson;

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
    
    public void parseLine(String line){
        
        String itemName   = CoatUtilsJson.getValueString(line, "name");
        String groupName  = CoatUtilsJson.getValueString(line, "namespace");
        String type       = CoatUtilsJson.getValueString(line, "type");
        String value      = CoatUtilsJson.getValueString(line, "value");
        String options    = CoatUtilsJson.getListString(line, "options");
        
        //System.out.println("TYPE = " + type);
        if(type.compareTo("string")==0){
                if(options!=null){
                    List<String>  optList = CoatUtilsJson.getListAsString(options);
                    //System.out.println(">>>>>>>>>>>>" + optList);
                    String[] optString = new String[optList.size()];
                    for(int i=0;i<optList.size();i++) optString[i] = optList.get(i);
                    this.addItem(groupName,itemName,optString);
                    this.setValue(groupName, itemName, value);
                } else {
                    //System.out.println("******************* TYPE STRING");
                    this.addItem( groupName,itemName, value );
                }
        }
        /**
         * setting integer group values
         */
        if(type.compareTo("integer")==0){
            if(options!=null){
                List<String>  optList = CoatUtilsJson.getListAsString(options);
                //System.out.println(">>>>>>>>>>>>" + optList);
                Integer[] optString = new Integer[optList.size()];
                for(int i=0;i<optList.size();i++) optString[i] = Integer.parseInt(optList.get(i));
                this.addItem(groupName,itemName,optString);
                this.setValue(groupName, itemName, Integer.parseInt(value));
            } else {
                //System.out.println("******************* TYPE STRING");
                this.addItem( groupName,itemName, Integer.parseInt(value));
            }
        }
        /**
         * setting double group values
         */
        if(type.compareTo("double")==0){
            if(options!=null){
                List<String>  optList = CoatUtilsJson.getListAsString(options);
                //System.out.println(">>>>>>>>>>>>" + optList);
                Double[] optString = new Double[optList.size()];
                for(int i=0;i<optList.size();i++) optString[i] = Double.parseDouble(optList.get(i));
                this.addItem(groupName,itemName,optString);
                this.setValue(groupName, itemName, Double.parseDouble(value));
            } else {
                //System.out.println("******************* TYPE STRING");
                this.addItem( groupName,itemName, Double.parseDouble(value));
            }
        }
        
        
    }
    
    public void readFile(String filename){
        List<String>  readLines = CoatUtilsFile.readFile(filename);
        //System.out.println("N LINES = " + readLines.size());
        System.out.println("[reading configuration] ---> " + filename);
        for(String line : readLines){
            if(line.startsWith("#")==true) continue;
            //System.out.println("[parsing line] --> " + line);
            
            this.parseLine(line);
            
            //System.out.println(" group = " + groupName + " item = " + itemName
            //+ " type = " + type);
            //System.out.println("LINE : " + name);
            //System.out.println("JSON : " + map);
        }
    }
    
    public void readFile(){
        String envResource = CoatUtilsFile.getResourceDir("HOME", ".coat/configurations/"+configFile);
        this.readFile(envResource);
        this.show();
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
        /*List<String>  fileLines = new ArrayList<String>();
        for(Map.Entry<String,ConfigurationGroup> entry : this.cGroups.entrySet()){
            fileLines.add(entry.getValue().toString());
        }
        CoatUtilsFile.writeFile(filename, fileLines);*/
        this.writeFile(filename);
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
    
    
    public void setValue(String group, String item, String value){
        this.cGroups.get(group).setValue(item, value);
    }
    
    public void setValue(String group, String item, Integer value){
        this.cGroups.get(group).setValue(item, value);
    }
    
    public void setValue(String group, String item, Double value){
        this.cGroups.get(group).setValue(item, value);
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
    
    
    public void writeFile(String filename){
        
        List<String>  writeLines = new ArrayList<String>();
        
        for(Map.Entry<String, ConfigurationGroup>  group : this.cGroups.entrySet()){
            List< Map<String,Object> > objects = group.getValue().getGroupMaps();
            for(Map<String,Object> map : objects){
                String jsonString = CoatUtilsJson.getJsonMap(map);
                //System.out.println(stWriter.toString());
                writeLines.add(jsonString);
            }            
        }
        CoatUtilsFile.writeFile(filename, writeLines);
    }
    
    public static void main(String[] args){
        Configuration config = new Configuration();
        config.addItem("DCHB", "torus", 0.5);
        config.addItem("DCHB", "solenoid", 1,2,3,4,5);
        config.setValue("DCHB", "solenoid", 3);
        config.addItem("EB", "ecmatching", 20.0);
        config.addItem("DCTB", "kalman","true","false");
        config.setValue("DCTB", "kalman","false");
        config.addItem("EB", "ftofmatching", 30.0);
        config.show();
        config.writeFile("test.json");
        
        Configuration  newConfig = new Configuration();
        newConfig.readFile("test.json");
        System.out.println("\n\n\n======>");
        newConfig.show();
        
       
    }
}
