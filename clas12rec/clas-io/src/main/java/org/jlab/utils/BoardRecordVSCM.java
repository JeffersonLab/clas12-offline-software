/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils;

/**
 *
 * @author gavalian
 */
public class BoardRecordVSCM {
    public int slotid  = 0;
    public int chipid  = 0;
    public int channel = 0;
    public int bco     = 0;
    public int adc     = 0;
    public int hfcbid  = 0;
    public int bcostart = 0;
    public int bcostop  = 0;
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format(" SLOT %4d  HFCB %4d CHIP %4d CHANNEL %4d", 
                this.slotid,this.hfcbid, this.chipid, this.channel));
        return str.toString();
    }
}
