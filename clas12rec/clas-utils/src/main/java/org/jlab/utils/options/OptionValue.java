/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.options;

import java.util.List;

/**
 *
 * @author gavalian
 */
public class OptionValue {
    
    private String optionString = "-h";
    private String optionValue  = "";
    private String optionDescription = "";
    private String optionDefault     = "0";
            
    public OptionValue() { }
    public OptionValue(String opt) {  setOption(opt);}
    public OptionValue(String opt,String value) {  setOption(opt); setValue(value);}
    
    public final OptionValue setOption(String opt){ this.optionString = opt; return this;}
    public final OptionValue setValue(String value){ this.optionValue = value; return this;}
    public final OptionValue setDefault(String value){ this.optionDefault = value; return this;}
    public final OptionValue setDescription(String desc){ this.optionDescription = desc; return this;}    
    
    public String  getOption() { return this.optionString;}
    public String  getValue()  { return this.optionValue;}
    public String  getDescription() { return this.optionDescription;}
    public String  getDefault() { return this.optionDefault;}
    
    public int     intValue()     { return Integer.parseInt(this.optionValue);}
    public double  doubleValue()  { return Double.parseDouble(this.optionValue);}
    public String  stringValue()  { return this.optionValue;}
    
    private int getOptionIndex(List<String> options){
        for(int i = 0; i < options.size(); i++){
            if(options.get(i).startsWith(optionString)==true)
                return i;
        }
        return -1;
    }
    
    public boolean parse(List<String> arguments){        
        int index = this.getOptionIndex(arguments);
        if(index<0) return false;
        if(index>arguments.size()-2){
            System.out.println("****>>>> error : argument " + this.optionString 
                    + " must follow with a value");
            return false;
        }
        this.optionValue = arguments.get(index+1);
        arguments.remove(index+1);
        arguments.remove(index);
        return true;
    }
}
