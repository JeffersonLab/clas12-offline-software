/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.object;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class DetectorBank {
    private List<Integer>  adcL   = new ArrayList<Integer>();
    private List<Integer>  adcR   = new ArrayList<Integer>();
    private List<Integer>  sector = new ArrayList<Integer>();

    public DetectorBank(){
        
    }
    
    @EvioDataType(parent=1500,tag=1502,num=3,type="int32")
    public List getADCL(){
        //System.out.println(" method called ");
        return this.adcL;
    }
    
    @EvioDataType(parent=1500,tag=1502,num=4,type="int32")
    public List getADCR(){
        //System.out.println(" method called ");
        return this.adcR;
    }
    
    @EvioDataType(parent=1500,tag=1502,num=1,type="int32")
    public List getSector(){
        //System.out.println(" method called ");
        return this.sector;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(" ADCL : ");
        for(Integer value : this.adcL){
            str.append(value);
            str.append(" ");
        }
        str.append("\n");
        
        str.append(" ADCR : ");
        for(Integer value : this.adcR){
            str.append(value);
            str.append(" ");
        }
        str.append("\n");
        str.append(" sector : ");
        for(Integer value : this.sector){
            str.append(value);
            str.append(" ");
        }
        str.append("\n");
        return str.toString();
    }
}
