/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

/**
 *
 * @author gavalian
 */
public class BasicFADCFitter  implements IFADCFitter {

    private int  pedestalBinStart = 0;
    private int  pedestalLength   = 0;
    private int  pulseLength      = 0;
    private int  pulseBinStart    = 0;
    
    
    public BasicFADCFitter(){
        
    }
     
    public BasicFADCFitter(int pmin, int pmax, int amin, int amax){
        
    }
    
    public final BasicFADCFitter setPedestal(int pmin, int pmax){
        pedestalBinStart = pmin;
        pedestalLength   = pmax-pmin;
        return this;
    }
    
    public final BasicFADCFitter setPulse(int pmin, int pmax){
        pulseBinStart = pmin;
        pulseLength   = pmax - pmin;
        return this;
    }
    
    public void fit(DetectorDataDgtz.ADCData data) {
        if(data.getPulseSize()==0) {
            System.out.println("[SimpleFADCFitter] ---> there is no pulse in ADCData..");
            return;
        }
        
    }
    
}
