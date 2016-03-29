/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.config;

import org.clas.config.ConfigurationGroup;
import com.sun.media.jfxmedia.logging.Logger;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.json.stream.JsonParser;
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
    
    
    public void readFile(String filename){
        List<String>  readLines = CoatUtilsFile.readFile(filename);
        System.out.println("N LINES = " + readLines.size());
        for(String line : readLines){
            if(line.startsWith("#")==true) continue;
            StringReader  reader = new StringReader(line);
            JsonReader parser = Json.createReader(reader);
            JsonObject  map = parser.readObject();
            String  name    = map.getString("name");
            String  name_sc = map.getString("namespace");
            String  type    = map.getString("type");

            if(map.containsKey("options")==true){
                JsonArray  array = map.getJsonArray("options");
                int nsize = array.size();
                if(type.compareTo("string")==0){
                    String[] options = new String[nsize];
                    for(int loop = 0; loop < nsize; loop++){
                        options[loop] = array.getString(loop);
                    }
                    this.addItem(name_sc, name, options);
                    String value = map.getString("value");
                    this.setValue(name_sc, name, value);
                    //this.addItem(name_sc, name, value);         
                }
                if(type.compareTo("integer")==0){
                    Integer[] options = new Integer[nsize];
                    for(int loop = 0; loop < nsize; loop++){
                        options[loop] = array.getInt(loop);
                    }
                    this.addItem(name_sc, name, options);
                    Integer value = map.getInt("value");
                    this.setValue(name_sc, name, value);
                    //this.addItem(name_sc, name, value);         
                }
                
            } else {
                if(type.compareTo("string")==0){                
                    String value = map.getString("value");
                    this.addItem(name_sc, name, value);                
                }
                if(type.compareTo("integer")==0){
                    Integer value = map.getInt("value");
                    this.addItem(name_sc, name, value);                
                }
                if(type.compareTo("double")==0){
                    //String value = map.getString("value");
                    //JsonObject  value = map.getJsonObject("value");    
                    JsonNumber  number = map.getJsonNumber("value");                
                    this.addItem(name_sc, name, number.doubleValue());
                }
            }

            //System.out.println("LINE : " + name);
            //System.out.println("JSON : " + map);
        }
    }
    
    public void readFile(){
        String envResource = CoatUtilsFile.getResourceDir("HOME", ".coat/configurations/"+configFile);
        this.readFile(envResource);
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
            for(Map<String,Object>  map : objects){
                JsonObjectBuilder model = Json.createObjectBuilder();
                for(Map.Entry<String,Object> mapItem : map.entrySet()){
                    if(mapItem.getValue() instanceof Integer){
                        model.add(mapItem.getKey(), (int) mapItem.getValue());
                    }
                    if(mapItem.getValue() instanceof String){
                        model.add(mapItem.getKey(), (String) mapItem.getValue());
                    }
                    if(mapItem.getValue() instanceof Double){
                        model.add(mapItem.getKey(), (double) mapItem.getValue());
                    }
                    if(mapItem.getValue() instanceof List){
                        JsonArrayBuilder  aBuilder = Json.createArrayBuilder();
                        for(Object obj : (List) mapItem.getValue()){
                            if(obj instanceof String){
                                aBuilder.add((String) obj);
                            }
                            if(obj instanceof Integer){
                                aBuilder.add((Integer) obj);
                            }
                            if(obj instanceof Double){
                                aBuilder.add((Double) obj);
                            }
                        }
                        JsonArray jarray = (JsonArray) aBuilder.build();
                        model.add(mapItem.getKey(), jarray);
                    }
                    //model.add(mapItem.getKey(), mapItem.getValue());                   
                }
                JsonObject jsonObject = model.build();
                StringWriter stWriter = new StringWriter();
                JsonWriter jsonWriter = Json.createWriter(stWriter);
                jsonWriter.writeObject(jsonObject);
                jsonWriter.close();
                //System.out.println(stWriter.toString());
                writeLines.add(stWriter.toString());
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
        config.addItem("EB", "ftofmatching", 30.0);
        config.show();
        config.writeFile("test.json");
        
        Configuration  newConfig = new Configuration();
        newConfig.readFile("test.json");
        newConfig.show();
    }
}
