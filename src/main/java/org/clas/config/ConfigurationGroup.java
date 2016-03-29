/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clas.config;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

/**
 *
 * @author gavalian
 */
public class ConfigurationGroup {
    
    Map<String, ConfigurationItem>  configItems = new HashMap<String, ConfigurationItem>();
    TitledPane   titledPane = null;
    String       groupName = "group";
    GridPane     itemGrid  = null;
    
    public ConfigurationGroup(String name){
        this.groupName = name;
        /*
        this.titledPane = new TitledPane();
        this.titledPane.setText(name);
        this.itemGrid = new GridPane();
        this.itemGrid.setVgap(10);
        this.itemGrid.setHgap(20);
        this.itemGrid.setPadding(new Insets(10,10,10,10));
        
        this.titledPane.setContent(this.itemGrid);
        */
    }
    
    public String getName(){
        return this.groupName;
    }
    
    public ConfigurationItem  getItem(String iname){
        return this.configItems.get(iname);
    }
    public void add(String iname, Integer iv){
        ConfigurationItem<Integer> item = new ConfigurationItem<Integer>(iname,iv);
        this.configItems.put(iname, item);
    }
    
    public boolean hasItem(String item){
        return this.configItems.containsKey(item);
    }
    
    public void addItem(String iname, Integer... options){        
        ConfigurationItem<Integer> item = new ConfigurationItem<Integer>(iname, options);
        this.configItems.put(iname, item);
    }
    
    public void addItem(String itemname, Double... value){
        ConfigurationItem item = new ConfigurationItem<Double>(itemname,value);
        this.configItems.put(itemname, item);
    }
    public void addItem(String itemname, String... value){
        ConfigurationItem item = new ConfigurationItem<String>(itemname,value);
        this.configItems.put(itemname, item);
    }
    /*
    public void addItem(String itemname, Double... value){
        
        int row = this.configItems.size() + 1;
        this.configItems.put(itemname, new ConfigurationItem(itemname,value));
        Label  itemLabel = new Label(itemname + " :");
        this.itemGrid.add(itemLabel, 1,row);
        SpinnerValueFactory svf = new 
                SpinnerValueFactory.DoubleSpinnerValueFactory(-10000.0,10000.0, value,0.1);
        
        Spinner sp = new Spinner();
        sp.setValueFactory(svf);
        sp.setEditable(true);
        sp.setPrefWidth(120);
        sp.getValueFactory().setValue(value);
        (( TextField) sp.getEditor()).setOnAction(e-> {
            Double v = (Double) sp.getValue();
            configItems.get(itemname).setValue(v);
        });
        this.itemGrid.add(sp, 2, row);
    }*/
    /**
     * Add integer configuration item.
     * @param itemname
     * @param value 
     */
    public void addItem(String iname, Integer iv){
        ConfigurationItem<Integer> item = new ConfigurationItem<Integer>(iname,iv);
        this.configItems.put(iname, item);
        /*
        int row = this.configItems.size() + 1;
        this.configItems.put(itemname, new ConfigurationItem(itemname,value));
        Label  itemLabel = new Label(itemname + " :");
        this.itemGrid.add(itemLabel, 1,row);
        SpinnerValueFactory svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000);

        Spinner sp = new Spinner();
        sp.setValueFactory(svf);
        sp.setEditable(true);
        sp.setPrefWidth(120);
        sp.getValueFactory().setValue(value);
        (( TextField) sp.getEditor()).setOnAction(e-> {
            Integer v = (Integer) sp.getValue();
            configItems.get(itemname).setValue(v);
        });
        this.itemGrid.add(sp, 2, row);*/
    }
    
    /*
    public void addItem(String itemname, String[] options){
        int row = this.configItems.size() + 1;
        String value = options[0];
        this.configItems.put(itemname, new ConfigurationItem(itemname,value,options));
        Label  itemLabel = new Label(itemname + " :");

        ComboBox  cbox = new ComboBox();
        cbox.setPrefWidth(120);

        for(String t : options) cbox.getItems().add(t);
        cbox.getSelectionModel().select(0);        
        this.itemGrid.add(itemLabel, 1,row);
        this.itemGrid.add(cbox, 2, row);
        
        cbox.setOnAction(e -> {
            String change = (String) cbox.getSelectionModel().getSelectedItem();
            System.out.println("something has changed "+ itemname + " -> " + change);
            configItems.get(itemname).setValue(value);            
        });
    }*/
    
    public List< Map<String,Object> >  getGroupMaps(){
            List< Map<String,Object> > mapList = new ArrayList< Map<String,Object> >();
            for(Map.Entry<String,ConfigurationItem>  item : this.configItems.entrySet()){
                Map<String,Object> itemMap = item.getValue().getMap();
                itemMap.put("namespace", this.getName());
                mapList.add(itemMap);
            }
            return mapList;
    }
    
    public void show(){
        System.out.println("GROUP [" + this.getName() + "]  ITEMS = " + this.configItems.size());
        for(Map.Entry<String,ConfigurationItem>  entry : this.configItems.entrySet()){
            System.out.println(entry.getValue());
        }
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<String,ConfigurationItem>  entry : this.configItems.entrySet()){
            str.append(entry.getValue().toString());
            str.append("\n");
        }
        return str.toString();
    }
    public double getAsDouble(String item){
        ConfigurationItem  ci = this.getItem(item);
        if(ci.getValue() instanceof Double){
            return (Double) ci.getValue();
        }
        System.out.println("[Group::double] error -> item with name " + item +
                " is not a double");
        return 0.0;
    }
    public int getAsInteger(String item){
        ConfigurationItem  ci = this.getItem(item);
        if(ci.getValue() instanceof Integer){
            return (Integer) ci.getValue();
        }
        System.out.println("[Group::double] error -> item with name " + item +
                " is not an integer");
        return 0;
    }
    public String getAsString(String item){
        ConfigurationItem  ci = this.getItem(item);
        if(ci.getValue() instanceof String){
            return (String) ci.getValue();
        }
        System.out.println("[Group::double] error -> item with name " + item +
                " is not a string");
        return "";
    }
    
    public TitledPane  getGroupPane(){
        TitledPane titledPane = new TitledPane();
        titledPane.setText(this.groupName);
        GridPane itemGrid = new GridPane();
        //itemGrid.setVgap(10);
        //itemGrid.setHgap(20);
        itemGrid.setPadding(new Insets(10,10,10,10));
        
        
        int row = 1;
        for(Map.Entry<String,ConfigurationItem> entry : this.configItems.entrySet()){
            Node node = this.getItemNode(entry.getKey());
            itemGrid.add(node, 1, row);
            row++;
        }
        titledPane.setContent(itemGrid);
        return titledPane;
    }
    
    public Node getItemNode(String item){
        
        GridPane  grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
//        grid.setVgap(20);
        grid.setHgap(20);
        grid.add(new Label(item), 1, 0);
        if(this.configItems.get(item).getValue() instanceof String){
            ComboBox  cbox = new ComboBox();
            cbox.setPrefWidth(120);
            
            for(String t : (Set<String>) this.configItems.get(item).getOptions()) 
                cbox.getItems().add(t);
            
            cbox.getSelectionModel().select(0);
            cbox.setOnAction(e -> {
                String change = (String) cbox.getSelectionModel().getSelectedItem();
                System.out.println("something has changed "+ item + " -> " + change);
                configItems.get(item).setValue(change);                
                show();
        });
            grid.add(cbox, 2,0);
        }
        
        if(this.configItems.get(item).getValue() instanceof Integer){            
            SpinnerValueFactory svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000);
            Spinner sp = new Spinner();
            sp.setValueFactory(svf);
            sp.setEditable(true);
            sp.setPrefWidth(120);
            Integer value = (Integer) this.configItems.get(item).getValue();
            sp.getValueFactory().setValue(value);

            (( TextField) sp.getEditor()).setOnAction(e-> {
                String spinnerValue = (( TextField ) e.getSource()).getText();
                Integer v = Integer.parseInt(spinnerValue);
                configItems.get(item).setValue(v);
            });
            grid.add(sp, 2, 0);
        }
        
        if(this.configItems.get(item).getValue() instanceof Double){
            Double value = (Double) this.configItems.get(item).getValue();
            SpinnerValueFactory svf = new 
                SpinnerValueFactory.DoubleSpinnerValueFactory(-10000.0,10000.0, value,0.1);
            
            Spinner sp = new Spinner();
            sp.setValueFactory(svf);
            sp.setEditable(true);
            sp.setPrefWidth(120);
            
            sp.getValueFactory().setValue(value);            
            (( TextField) sp.getEditor()).setOnAction(e-> {
                String spinnerValue = (( TextField ) e.getSource()).getText();
                Double v = Double.parseDouble(spinnerValue);
                System.out.println("on action " + (( TextField ) e.getSource()).getText());
                configItems.get(item).setValue(v);
            });
            
            grid.add(sp, 2, 0);
        }        
        return grid;
    }
    
    public void setValue(String item, String value){
        if(this.configItems.get(item).getValue() instanceof String){
            this.configItems.get(item).setValue(value);
        } else {
            System.out.println("[group::setValue] error --> item "
            + item + " is not of type String");
        }
    }
    
    public void setValue(String item, Integer value){
        if(this.configItems.get(item).getValue() instanceof Integer){
            this.configItems.get(item).setValue(value);
        } else {
            System.out.println("[group::setValue] error --> item "
            + item + " is not of type Integer");
        }
    }
    
    public void setValue(String item, Double value){
        if(this.configItems.get(item).getValue() instanceof Double){
            this.configItems.get(item).setValue(value);
        } else {
            System.out.println("[group::setValue] error --> item "
            + item + " is not of type Double");
        }
    }
    
    public void changeValue(String itemname, String value){
        
    }
    public TitledPane getTitledPane(){
        return this.titledPane;
    }
    /**
     * Configuration item class for defining configurations.
     * @param <T> 
     */
    public class ConfigurationItem<T> {
        
        String           itemName = "";
        Boolean          isArray  = false;
        String           itemType = "string";
        List<T>        itemValues = new ArrayList<T>();
        Set<T>        itemOptions = new HashSet<T>();
        T             defaultValue = null;
        
        public ConfigurationItem(String name, T value){            
            this.itemName     = name;
            this.itemValues.add(value);
            this.defaultValue = value;
        }
        
        public ConfigurationItem(String name, T... options){
            this.itemName  = name;
            this.itemValues.add(options[0]);
            this.defaultValue = options[0]; 
            if(options.length>1){
                for(T opt : options){
                    itemOptions.add(opt);
                }
            }
        }
        
        public int getSize(){return this.itemValues.size();}
        public T   getValue(int index){return this.itemValues.get(index);}
        
        public void addValue(T value){
            
            if(this.isArray==false){
                this.itemValues.clear();
                this.itemValues.add(value);
            } else {
                this.itemValues.add(value);
            }
        }
        
        public void reset(){
            if(this.isArray==false){
                this.itemValues.clear();
                this.itemValues.add(this.defaultValue);
            } else {
                this.itemValues.clear();
            }
        }
        
        
        
        public String getOptionsString(){
            StringBuilder str = new StringBuilder();
            str.append("{ ");
            int icounter = 0;
            for(T item : this.itemOptions){
                if(icounter!=0) str.append(", ");
                str.append(item);
                icounter++;
            }
            str.append(" }");
            return str.toString();
        }
        
        public void     setValue(T value){
            System.out.println("changin value to " + value);
            if(this.itemOptions.size()==0){
                this.itemValues.set(0, value);
            } else {
                if(this.itemOptions.contains(value)==false){
                    System.out.println("[setValue] error : --> " +
                            value + " is not a valid value. use  " +
                            this.getOptionsString());
                } else {
                    this.itemValues.set(0, value);
                }
            }
        };
        
        public String   getName(){ return itemName;}
        public T        getValue(){ return this.itemValues.get(0);};
        public Set<T>   getOptions(){ return this.itemOptions;}
        /**
         * returns a map to be serialized.
         * @return 
         */
        public Map<String,Object> getMap(){
            
            Map<String,Object> map = new LinkedHashMap<String,Object>();
            map.put("name", this.itemName);

            if(this.getValue() instanceof String){
                map.put("type", "string");
            }
            if(this.getValue() instanceof Integer){
                map.put("type", "integer");
            }
            if(this.getValue() instanceof Double){
                map.put("type", "double");
            }
            map.put("value", this.getValue());
            if(this.itemOptions.size()>0){
                List<T> opt = new ArrayList<T>();
                for(T it : this.itemOptions){
                    opt.add(it);
                }
                //System.out.println("OPTIONS FLAG = " + opt);
                map.put("options", opt);
            }
            return map;
        }
        @Override
        public String  toString(){
            
            StringBuilder str = new StringBuilder();
            str.append(this.getName());
            str.append(" : ");
            str.append(this.getValue());
            str.append(" , ");
            str.append(this.getOptionsString());
            return str.toString();

        }
    }
    
    
    public static void main(String[] args){
        
        //ConfigurationGroup group = new ConfigurationGroup("group");
        //group.add("property" , 25, 12, 45, 67,89);
        //group.add("fillcolor", 32);
        //Map<String,Object> model = new HashMap<String,Object>();
        JsonObject model = Json.createObjectBuilder()
                .add("firstName", "Duke")
                .add("lastName", "Java")
                .add("age", 18).add("ratio",3.2).build();
        StringWriter stWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(stWriter);
        jsonWriter.writeObject(model);
        
        jsonWriter.close();
        
        String jsonData = stWriter.toString();
        System.out.println(jsonData);
        
        
        /*
        ConfigurationGroup group = new ConfigurationGroup("Canvas");
        group.addItem("FillColor", 1);
        group.addItem("LineColor", 4.5);
        group.addItem("LineStyle", 2,3,4,5,6);
        group.addItem("DrawAxis", "true","false");
        group.show();
        System.out.println("-----");
        group.getItem("DrawAxis").setValue("gt");
        group.getItem("LineStyle").setValue(7);
        group.getItem("FillColor").setValue(7);
        
        group.show();*/
    }
}
