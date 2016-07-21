/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class EvioHipoEvent {
    
    public static EvioDataBank getBankFTOF(EvioDataBank ftof1a, EvioDataBank ftof1b,
            EvioDataBank ftof2){
        int nrows = ftof1a.rows() + ftof1b.rows() + ftof2.rows();
        
        EvioDataBank  bankFTOF = (EvioDataBank) EvioFactory.getDictionary().createBank("FTOF::dgtz", nrows);
        int crow = 0;
        for(int i = 0; i < ftof1a.rows(); i++){
            bankFTOF.setByte("sector", crow , (byte) ftof1a.getInt("sector", i));
            bankFTOF.setByte("layer", crow , (byte) 1);
            bankFTOF.setShort("component", crow , (short) ftof1a.getInt("paddle", i));
            bankFTOF.setInt("ADCL", crow , (short) ftof1a.getInt("ADCL", i));
            bankFTOF.setInt("ADCR", crow , (short) ftof1a.getInt("ADCR", i));
            bankFTOF.setInt("TDCL", crow , (short) ftof1a.getInt("TDCL", i));
            bankFTOF.setInt("TDCR", crow , (short) ftof1a.getInt("TDCR", i));
            crow++;
        }
        
        for(int i = 0; i < ftof1b.rows(); i++){
            bankFTOF.setByte("sector", crow , (byte) ftof1b.getInt("sector", i));
            bankFTOF.setByte("layer", crow , (byte) 2);
            bankFTOF.setShort("component", crow , (short) ftof1b.getInt("paddle", i));
            bankFTOF.setInt("ADCL", crow , (short) ftof1b.getInt("ADCL", i));
            bankFTOF.setInt("ADCR", crow , (short) ftof1b.getInt("ADCR", i));
            bankFTOF.setInt("TDCL", crow , (short) ftof1b.getInt("TDCL", i));
            bankFTOF.setInt("TDCR", crow , (short) ftof1b.getInt("TDCR", i));
            crow++;
        }
        
        for(int i = 0; i < ftof2.rows(); i++){
            bankFTOF.setByte("sector", crow , (byte) ftof2.getInt("sector", i));
            bankFTOF.setByte("layer", crow , (byte) 3);
            bankFTOF.setShort("component", crow , (short) ftof2.getInt("paddle", i));
            bankFTOF.setInt("ADCL", crow , (short) ftof2.getInt("ADCL", i));
            bankFTOF.setInt("ADCR", crow , (short) ftof2.getInt("ADCR", i));
            bankFTOF.setInt("TDCL", crow , (short) ftof2.getInt("TDCL", i));
            bankFTOF.setInt("TDCR", crow , (short) ftof2.getInt("TDCR", i));
            crow++;
        }
        return bankFTOF;
    }
    
    public static EvioDataBank getBankFTOF(EvioDataEvent event){
        List<EvioDataBank>  list = new ArrayList<EvioDataBank>();
        
        if(event.hasBank("FTOF1A::dgtz")==false){
            list.add( (EvioDataBank) EvioFactory.getDictionary().createBank("FTOF1A::dgtz", 0));
        } else {
            list.add( (EvioDataBank) event.getBank("FTOF1A::dgtz"));
        }
        
        if(event.hasBank("FTOF1B::dgtz")==false){
            list.add( (EvioDataBank) EvioFactory.getDictionary().createBank("FTOF1B::dgtz", 0));
        } else {
            list.add( (EvioDataBank) event.getBank("FTOF1B::dgtz"));
        }
        
        if(event.hasBank("FTOF2B::dgtz")==false){
            list.add( (EvioDataBank) EvioFactory.getDictionary().createBank("FTOF2B::dgtz", 0));
        } else {
            list.add( (EvioDataBank) event.getBank("FTOF2B::dgtz"));
        }
                
        return EvioHipoEvent.getBankFTOF(list.get(0), list.get(1), list.get(2));
    }
}
