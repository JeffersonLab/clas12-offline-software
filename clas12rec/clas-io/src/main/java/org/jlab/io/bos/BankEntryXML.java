/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author gavalian
 */
@XmlRootElement(name="entry")
public class BankEntryXML {
    private String entryName;
    private String entryTypeString;
    
    public BankEntryXML(String name, String type){
        entryName = name;
        entryTypeString = type;
    }
    
    public BankEntryXML(){
        
    }
    
    @XmlAttribute(name="name")
    public String getEntryName(){
        return entryName;
    }
    
    public void setEntryName(String name){
        entryName = name;
    }
    @XmlAttribute(name="type")
    public String getEntryTypeString(){
        return entryTypeString;
    }
    
    public void setEntryTypeString(String type){
        entryTypeString = type;
    }
}
