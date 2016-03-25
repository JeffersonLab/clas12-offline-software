/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clas.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

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
    
    
    public void add(String iname, Integer iv){
        ConfigurationItem<Integer> item = new ConfigurationItem<Integer>(iname,iv);
        this.configItems.put(iname, item);
    }
    
    public void add(String iname, Integer iv, Integer... options){
        ConfigurationItem<Integer> item = new ConfigurationItem<Integer>(iname,iv, options);
        this.configItems.put(iname, item);
    }
    
    public void addItem(String itemname, Double value){
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
    }
    /**
     * Add integer configuration item.
     * @param itemname
     * @param value 
     */
    public void addItem(String itemname, Integer value){
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
        this.itemGrid.add(sp, 2, row);
    }
    
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
        
    }
    
    public void show(){
        for(Map.Entry<String,ConfigurationItem>  entry : this.configItems.entrySet()){
            System.out.println(entry.getValue());
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
        T               itemValue;
        T             defaultValue;
        Node          uiNode      = null;
        Class<T>      type        = null;
        
        public ConfigurationItem(String name, T value){
            this.itemName     = name;
            this.defaultValue = value;
            this.isArray      = false;
            this.addValue(value);
        }
        
        public ConfigurationItem(String name, T value, T... options){
            this.itemName  = name;
            this.defaultValue = value;
            this.addValue(value);
            
            for(T opt : options){
                itemOptions.add(opt);
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
        
        public void getNode(){
            if(this.uiNode==null){
                this.buildNode(itemOptions);
            }
        }
        
        private void buildNode(Set<T> options){
            
            GridPane gp = new GridPane();            
            gp.setPadding(new Insets(10,10,10,10));
            gp.setVgap(10);
            gp.setHgap(20);
            Label nL = new Label(this.itemName + " :");
            gp.add(nL, 1, 0);
            
            if(this.defaultValue instanceof Integer){
                SpinnerValueFactory svf = new 
                        SpinnerValueFactory.IntegerSpinnerValueFactory(-1000, 1000);
                Spinner sp = new Spinner();
                sp.setValueFactory(svf);
                sp.setEditable(true);
                sp.setPrefWidth(120);
                sp.getValueFactory().setValue((Integer) this.getValue());
                (( TextField) sp.getEditor()).setOnAction(e-> {
                    Integer v = (Integer) sp.getValue();
                    setValue((T) v);
                    System.out.println("[item] " + itemName +  
                            "changing the value " + v);
                });
                gp.add(sp, 2, 0);
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
        
        public void     setValue(T value){
            itemValue = value;
        };
        
        public String   getName(){ return itemName;}
        public T        getValue(){ return this.itemValues.get(0);};
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
    
    
    public static void main(String[] args){
        
        ConfigurationGroup group = new ConfigurationGroup("group");
        group.add("property" , 25, 12, 45, 67,89);
        group.add("fillcolor", 32);        
        //System.out.println("printing the group");
        group.show();
    }
}
