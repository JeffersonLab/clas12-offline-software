/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.ui;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class BankEntryMasks {

    Map<String,String> bankMasks = new HashMap<String,String>();
    
    public BankEntryMasks(){
        
    }
    
    public String getMask(String bankName){
        return bankMasks.get(bankName);
    }
    
    
    public void setMask(String bankName, String mask){
        bankMasks.put(bankName, mask);
    }
    
}
