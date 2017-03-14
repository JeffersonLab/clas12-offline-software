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
    private List<String>               parsedInputList = new ArrayList<String>();
    private String                             program = "undefined";
    private boolean                  requiresInputList = true;
    private String                  programDescription = "";
    
    public OptionParser(){
        
    }
    
    public OptionParser(String pname){
        this.program = pname;
    }
    
    public void setDescription(String desc){
        this.programDescription = desc;
    }
    
    public void setRequiresInputList(boolean flag){
        this.requiresInputList = flag;
    }
    
    public void addRequired(String key){
        OptionValue option = new OptionValue(key);
        requiredOptions.put(key, option);
    }
    
    public void addRequired(String key,String desc){
        OptionValue option = new OptionValue(key);
        option.setDescription(desc);
        requiredOptions.put(key, option);
    }
    
    public void addOption(String key, String defaultValue){
        OptionValue option = new OptionValue(key,defaultValue);
        optionsDescriptors.put(key, option);
    }
    
    public void addOption(String key, String defaultValue, String description){
        OptionValue option = new OptionValue(key,defaultValue);
        option.setDescription(description);
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
    
    public boolean containsOptions(List<String> arguments, String... options){
        for(String argument : arguments){
            for(String option : options){
                if(argument.compareTo(option)==0) return true;
            }
        }        
        return false;
    }
    
    public String getUsageString(){
        
        StringBuilder str = new StringBuilder();
        
        str.append("     Usage : ").append(program).append(" ");
        for(Map.Entry<String,OptionValue> entry : this.requiredOptions.entrySet()){
            str.append(entry.getKey()).append(" [").
                    append(entry.getValue().getDescription()).append("] ");
        }
        
        if(this.requiresInputList==true) str.append(" [input1] [input2] ....");
        
        str.append("\n\n   Options :\n");
        for(Map.Entry<String,OptionValue> entry : this.optionsDescriptors.entrySet()){
            str.append("").append(String.format("%10s : %s (default = %s)", 
                    entry.getKey(),entry.getValue().getDescription(),entry.getValue().stringValue()));
            str.append("\n");
        }
        return str.toString();
    }
    
    public void printUsage(){
        System.out.println("\n\n");
        System.out.println("*******************************************");
        System.out.println("*      PROGRAM USAGE : by OptionParser    *");
        System.out.println("*******************************************");
        System.out.println("\n\n");
        System.out.println(this.getUsageString());
        System.out.println("\n\n");
    }
    
    public void parse(String[] args){
        List<String> arguments = new ArrayList<String>();
        for(String item : args){ arguments.add(item); }
        
        if(this.containsOptions(arguments, "-h","-help")==true){
            this.printUsage();
            System.exit(0);
        }

//this.show(arguments);
        for(Map.Entry<String,OptionValue> entry : this.requiredOptions.entrySet()){
            boolean status = entry.getValue().parse(arguments);
            if(status==false) { 
                this.parsedOptions.clear();
                this.printUsage();
                System.out.println(" \n*** ERROR *** Missing argument : " + entry.getValue().getOption());
                System.exit(0);
                return;
            }
            this.parsedOptions.put(entry.getValue().getOption(), entry.getValue());
        }
        for(Map.Entry<String,OptionValue> entry : this.optionsDescriptors.entrySet()){
            boolean status = entry.getValue().parse(arguments);
            this.parsedOptions.put(entry.getKey(), entry.getValue());
        }
        
        parsedInputList.clear();
        for(String item : arguments){
            if(item.startsWith("-")==false){
                this.parsedInputList.add(item);
            }
        }
        //this.show(arguments);
    }
    
    public List<String> getInputList(){
        return this.parsedInputList;
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
