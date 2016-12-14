/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class DetectorBankDgtz {
    
    public static int         BANKTYPE_ADC = 1;
    public static int         BANKTYPE_TDC = 2;
    public static int     BANKTYPE_ADC_TDC = 3;
    public static int BANKTYPE_ADCLR_TDCLR = 4;

    
    private List<DetectorDataDgtz>  dgtzData = new ArrayList<DetectorDataDgtz>();
    private String                  bankName = "FTOF::dgtz";
    private int                     bankType = DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR;
    
    public DetectorBankDgtz(){
        
    }
    
    public String getName(){
        return this.bankName;
    }
    
    private int getBankType(DataDescriptor desc){
        if(desc.hasEntries("ADCL","ADCR","TDCL","TDCR")==true) 
            return DetectorBankDgtz.BANKTYPE_ADCLR_TDCLR;
        if(desc.hasEntries("ADC","TDC")==true)
            return DetectorBankDgtz.BANKTYPE_ADC_TDC;
        if(desc.hasEntries("TDC")==true)
            return DetectorBankDgtz.BANKTYPE_TDC;
        return 0;
    }
    
    public final void readFromEvent(DataEvent event, String bank){
        DataDescriptor desc = event.getDictionary().getDescriptor(bank);
        
        
    }
    
    public final void readFromEvent(DataEvent event){
        this.readFromEvent(event, bankName);
    }
}
