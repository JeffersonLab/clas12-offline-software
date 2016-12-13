/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.system;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class CommandLineParser {
       private  Map<String,CommandLineOption>  parserCommands = 
            new LinkedHashMap<String,CommandLineOption>();
    private  List<CommandLineOption>  currentCommand = new 
            ArrayList<CommandLineOption>();
    
    public CommandLineParser(){}
    
    public void addCommand(String command){
        CommandLineOption cmd = new CommandLineOption(command);
        this.parserCommands.put(command, cmd);
        currentCommand.clear();
        currentCommand.add(cmd);
    }
    
    public CommandLineOption getCommand(String command){
        return this.parserCommands.get(command);
    }
    
    public CommandLineOption getCommand(){
        return this.currentCommand.get(0);
    }
    
    public void parse(String[] args){
        
        if(args.length<1) return;
        
        
        
        if(this.parserCommands.size()==1){
            this.currentCommand.get(0).parse(args, 0);
        } else {
            String command = args[0];
            if(this.parserCommands.containsKey(command)==true){
                this.currentCommand.clear();
                this.currentCommand.add(this.parserCommands.get(command));
                this.currentCommand.get(0).parse(args, 1);
            } else {
                System.out.println("command not found : " + command);
            }
        }
    }
    
    public void show(){
        System.out.println("");
        for(Map.Entry<String,CommandLineOption> entry : this.parserCommands.entrySet()){
            System.out.println(" COMMAND : " + entry.getKey());
            entry.getValue().printUsage("program");
        }
    }
    /**
     * This class holds all parameters required, optional and flags
     * for given command in the command line;
     */
    public static class CommandLineOption {
        
        private Map<String,CommandLineItem> requiredParameters =
                new LinkedHashMap<String,CommandLineItem>();
        
        private Map<String,CommandLineItem> optionalParameters =
                new LinkedHashMap<String,CommandLineItem>();
        
        private Map<String,CommandLineItem> flagParameters = 
                new  LinkedHashMap<String,CommandLineItem>();
        
        private Map<String,CommandLineItem> parsedParameters =
                new LinkedHashMap<String,CommandLineItem>();
        
        private List<String>  commandInputs = new ArrayList<String>();
        
        private Map<String,String>  commandDefinitions = new LinkedHashMap<String,String>();
        
        private String       listDeliminator = ":";
        
        private String command = "-run";
        
        public CommandLineOption(){
        }
        
        public CommandLineOption(String cmd){
            this.command = cmd;
        }
        
        public String getCommand(){ return command;}
        
        public void setCommand(String cmd){
            this.command = cmd;
        }            
    
        public void addCommand(String command, String explanation){
            this.commandDefinitions.put(command, explanation);
        }
        
        public void printCommands(){
            System.out.println();
            System.out.println(" Commands : ");
            //for(Map.Entry<String,String> entry : );
        }
        
        public void setListDeliminator(String delim){
            this.listDeliminator = delim;
        }
        
        public void addRequiredParameter(String par, String description){
            CommandLineItem item = new CommandLineItem(par,description);
            item.setRequired(true);
            this.requiredParameters.put(item.getName(), item);
        }
    
    public void addOptionalParameter(String par, String dvalue, String description){
        CommandLineItem item = new CommandLineItem(par,description);
        item.setDefaultValue(dvalue);
        item.setRequired(false);
        this.optionalParameters.put(item.getName(), item);
    }
    
    public void addFlag(String name, String description){
        CommandLineItem item = new CommandLineItem(name,description);
        item.setRequired(false);
        this.flagParameters.put(name, item);
    }
    
    public void parse(String[] args, int startIndex){
        
        int icounter = startIndex;
        
        while(icounter<args.length){
            if(args[icounter].startsWith("-")==true){
                String argument = args[icounter];
                icounter++;
                if(this.flagParameters.containsKey(argument)==true){
                    CommandLineItem item = new CommandLineItem(argument);
                    item.setValue("1");
                    this.parsedParameters.put(argument, item);
                } else {
                    if(this.optionalParameters.containsKey(argument)==false&&
                            this.optionalParameters.containsKey(argument)==false){
                        System.out.println("[ArgumentParser] --> warning : parsing input "
                        + " parameter [" + argument +"] which is not declared");
                    }
                    CommandLineItem item = new CommandLineItem(argument);
                    String value = args[icounter];
                    icounter++;
                    item.setValue(value);
                    this.parsedParameters.put(argument, item);                    
                }
            } else {
                this.commandInputs.add(args[icounter]);
                icounter++;
            }
        }
    }
    
    public String  getOption(String opt){
        if(this.parsedParameters.containsKey(opt)==true) 
            return this.parsedParameters.get(opt).getValue();
        if(this.optionalParameters.containsKey(opt)==true){
            optionalParameters.get(opt).getDefaultValue();
        }
        System.out.println("[getOption] error : can not find option [" + opt + "]");
        return null;
    }
    
    public void printUsage(String programName){
        
        System.out.println("\n\nUsage : " + programName);
        System.out.println();
        System.out.println("   Required :");
        for(Map.Entry<String,CommandLineItem> entry : this.requiredParameters.entrySet()){
            System.out.println(entry.getValue().getDescriptionString(12, false));
        }
        System.out.println("   Optional :");
        for(Map.Entry<String,CommandLineItem> entry : this.optionalParameters.entrySet()){
            System.out.println(entry.getValue().getDescriptionString(12, true));
        }
        System.out.println("   Flags :");
        for(Map.Entry<String,CommandLineItem> entry : this.flagParameters.entrySet()){
            System.out.println(entry.getValue().getDescriptionString(12, true));
        }
    }
    
    public void show(){
        System.out.println(" PARSER OUTPUT -----> " + this.command);
        for(Map.Entry<String,CommandLineItem> entry : this.parsedParameters.entrySet()){
            System.out.println(entry.getValue().getItemString(12, true));
        }
        
        for(String item : this.getInputList()){
            System.out.println("input ->> " + item);
        }
    }
    public void    explainMissing(){
         for(Map.Entry<String,CommandLineItem> entry : this.requiredParameters.entrySet()){
            if(this.parsedParameters.containsKey(entry.getKey())==false){
                System.out.println("\n\n You are missing an argument : \n");
                System.out.println(entry.getValue().getName() + " " + entry.getValue().getDescription());
                System.out.println("\n---\n\n");                
            }
        }
    }
    
    public boolean containsRequired(){
        for(Map.Entry<String,CommandLineItem> entry : this.requiredParameters.entrySet()){
            if(this.parsedParameters.containsKey(entry.getKey())==false) return false;
        }
        return true;
    }
    
    public int getAsInt(String opt){
        String value = this.getOption(opt);
        if(value==null) return 0;
        return Integer.parseInt(value);
    }
    
    public String[] getAsStringList(String opt){
        String value = this.getOption(opt);
        if(value==null) return new String[0];
        String[] tokens = value.split(":");
        return tokens;
    }
    
    public int[]  getAsIntList(String opt){
        String[] opts = getAsStringList(opt);
        int[]  result = new int[opts.length];
        for(int i= 0; i < result.length; i++) {
            try{
                result[i] = Integer.parseInt(opts[i]);
            } catch(Exception ex){
                System.out.println("[getAsIntList] error parsing number as integer " + opts[i]);
                result[i] = 0;
            }
        }
        return result;
    }
    
    public double[]  getAsDoubleList(String opt){
        String[] opts = getAsStringList(opt);
        double[]  result = new double[opts.length];
        for(int i= 0; i < result.length; i++) {
            try{
                result[i] = Double.parseDouble(opts[i]);
            } catch(Exception ex){
                System.out.println("[getAsDoubleList] error parsing number as double " + opts[i]);
                result[i] = 0.0;
            }
        }
        return result;
    }
    
    public double getAsDouble(String opt){
        String value = this.getOption(opt);
        if(value==null) return 0.0;
        return Double.parseDouble(value);
    }
        
    public String getAsString(String opt){
        String value = this.getOption(opt);
        if(value==null) return "";
        return value;
    }
    
    public List<String>  getInputList(){
        return this.commandInputs;
    }
    
    public boolean hasOption(String opt){
        if(this.parsedParameters.containsKey(opt)==true) return true;
        return (this.optionalParameters.containsKey(opt)==true);
    }
    }

 
    /**
     * internal class implementing items to be stored in the parser.
     */
    public static class CommandLineItem {
        
        private String   itemName      = "-";
        private boolean  isRequired    = true;
        private String   description   = "defualt";
        private String   defaultValue  = "0";
        private String   actualValue   = "0";
        private int      itemsToFollow = 1;
        
        public CommandLineItem(String name){
            this.itemName = name;            
        }
        
        public CommandLineItem(String name, String desc){
            this.itemName    = name;
            this.description = desc;
        }
        
        public void setName(String name){
            this.itemName = name;
        }
        
        public String getName(){ return itemName;}
        public String getDescription(){ return description;}
        
        public String getDefaultValue(){ return this.defaultValue;}
        public String getValue(){ return this.actualValue;}
        
        public void   setValue(String value){ this.actualValue = value;}
        public void   setDefaultValue(String value){ this.defaultValue = value;}
        public void   setDescription(String desc){ description = desc;}
        public boolean isRequired(){ return this.isRequired;}
        
        
        public String  getDescriptionString(int legth, boolean isFlag){
            StringBuilder str = new StringBuilder();
            str.append("\t");
            str.append(String.format("%12s : %s",this.getName(),this.getDescription()));
            if(isFlag==true) {
                str.append(String.format(" (default value = %s) ",this.defaultValue));
            }
            return str.toString();
        }
        
        
        public String  getItemString(int legth, boolean isFlag){
            StringBuilder str = new StringBuilder();
            str.append("\t");
            str.append(String.format("%12s : %s",this.getName(),this.getValue()));            
            return str.toString();
        }
        
        public void setRequired(boolean flag){
            this.isRequired = flag;
        }
    }
}
