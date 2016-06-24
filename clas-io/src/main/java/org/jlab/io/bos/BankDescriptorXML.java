/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author gavalian
 */
@XmlRootElement(name="bank")
public class BankDescriptorXML {
    private ArrayList<BankEntryXML> entries = new ArrayList<BankEntryXML>();
    private String bankName;
    
    public BankDescriptorXML(){
        
    }
    
    public BankDescriptorXML(String name){
        bankName = name;
    }
    
    @XmlAttribute(name="name")
    public String getBankName(){
        return bankName;
    }
    
    public void setBankName(String nm){
        bankName = nm;
    }
    
    @XmlElement(name="enties")
    public ArrayList<BankEntryXML> getEntries(){
        return entries;
    }
    
    public void setEntries(ArrayList<BankEntryXML> en){
        entries =en;
    }
    
    private String correctNameToFourChars(String name){
        if(name.length()==4) return name;

        if(name.length()<4){
            byte[] stb = new byte[4];
            byte[] str = name.getBytes();
            for(int loop = 0; loop < 4; loop++){
                if(loop<str.length){
                    stb[loop] = str[loop];
                } else {
                    stb[loop] = ' ';
                }
            }
            
            return new String(stb);
        }
        return name;
    }
}
