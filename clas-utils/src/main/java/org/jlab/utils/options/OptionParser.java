/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class OptionParser {
    
    private Map<String,OptionValue> optionsDescriptors = new TreeMap<String,OptionValue>();    
    private Map<String,OptionValue>    requiredOptions = new TreeMap<String,OptionValue>();
    private Map<String,OptionValue>      parsedOptions = new TreeMap<String,OptionValue>();
    
    public OptionParser(){
        
    }
    
    public void addRequired(String key){
        OptionValue option = new OptionValue(key);
        requiredOptions.put(key, option);
    }
    
    public void addOption(String key, String defaultValue){
        OptionValue option = new OptionValue(key,defaultValue);
        optionsDescriptors.put(key, option);
    }
    
    public boolean hasOption(String option){
        return this.parsedOptions.containsKey(option);
    }
    
    public OptionValue getOption(String option){
        return this.parsedOptions.get(option);
    }    
    private void show(List<String> list){
        for(int i = 0; i < list.size(); i++){
            System.out.printf("%5d : %s\n", i,list.get(i));
        }
    }
    
    public void show(){
        for(Map.Entry<String,OptionValue> entry : this.parsedOptions.entrySet()){
         System.out.printf("%12s : %s\n", entry.getKey(),entry.getValue().getValue());
        }
    }
    
    public void parse(String[] args){
        List<String> arguments = new ArrayList<String>();
        for(String item : args){ arguments.add(item); }
        //this.show(arguments);
        for(Map.Entry<String,OptionValue> entry : this.requiredOptions.entrySet()){
            boolean status = entry.getValue().parse(arguments);
            if(status==false) { 
                this.parsedOptions.clear();
                System.out.println(" \n*** ERROR *** Missing argument : " + entry.getValue().getOption());
                return;
            }
            this.parsedOptions.put(entry.getValue().getOption(), entry.getValue());
        }
        for(Map.Entry<String,OptionValue> entry : this.optionsDescriptors.entrySet()){
            boolean status = entry.getValue().parse(arguments);
            this.parsedOptions.put(entry.getKey(), entry.getValue());
        }       
        //this.show(arguments);
    }
    
    public static void main(String[] args){
        String[] options = new String[]{"-t","2","-r","5","-o","output.hipo"};
        OptionParser parser = new OptionParser();
        parser.addRequired("-o");
        parser.addOption("-r", "10");
        parser.addOption("-t", "25.0");
        parser.addOption("-d", "35");
        
        parser.parse(options);
        
        parser.show();        
    }
}
