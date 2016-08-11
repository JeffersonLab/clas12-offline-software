/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.base.Detector;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ECCommon {
    
    /**
     * Returns array of strips for EC given the EC bank, EC detector
     * and constants manager.
     * @param event 
     * @param detector
     * @param manager
     * @param run
     * @return 
     */
    
    public static List<ECStrip>  initStrips(DataEvent event, 
            Detector detector, ConstantsManager manager, int run){
                
        List<ECStrip>  strips = new ArrayList<ECStrip>();
        IndexedTable   atten  = manager.getConstants(run, "/calibration/ec/attenuation");
        
        if(event.hasBank("EC::dgtz")==true){
            EvioDataBank ecBank = (EvioDataBank) event.getBank("EC::dgtz");
            int nrows = ecBank.rows();
            for(int row = 0; row < nrows; row++){
                
                int sector    = (int) ecBank.getByte("sector", row);
                int layer     = (int) ecBank.getByte("layer", row);
                int component = (int) ecBank.getByte("component", row);
                
                ECStrip strip = new ECStrip(
                        sector,layer,component
                );
                
                strip.setADC(ecBank.getInt("ADC", row));
                strip.setTDC(ecBank.getInt("TDC", row));
                if(atten!=null){
                    /*strip.setAttenuation(
                            atten.getDoubleValue("A", sector,layer,component),
                            atten.getDoubleValue("B", sector,layer,component),
                            atten.getDoubleValue("C", sector,layer,component)
                    );*/
                } else {
                    System.out.println(manager.toString());
                }
            }
        }
        
        return strips;
    }
}
