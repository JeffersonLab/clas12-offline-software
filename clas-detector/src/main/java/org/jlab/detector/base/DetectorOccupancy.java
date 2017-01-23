/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

import java.util.List;
import org.jlab.io.base.DataBank;

/**
 *
 * @author gavalian
 */
public class DetectorOccupancy {
    
    DetectorCollection<DetectorMeasurement>  occupancyCollection = 
            new DetectorCollection<DetectorMeasurement>();
    
    public DetectorOccupancy(){
        
    }
    
    public void addADC(DataBank bank){
        int nrows = bank.rows();
        for(int row = 0; row < nrows; row++){
            int    sector = bank.getByte(  "sector",    row);
            int     layer = bank.getByte(  "layer",     row);
            int component = bank.getShort( "component", row);
            if(occupancyCollection.hasEntry(sector, layer, component)==true){
                this.occupancyCollection.get(sector, layer, component).incrementADC();
            } else {
                DetectorMeasurement measure = new DetectorMeasurement();
                measure.incrementADC();
                this.occupancyCollection.add(sector, layer, component, measure);
            }
        }
    }
    
    public void addTDC(DataBank bank){
        int nrows = bank.rows();
        for(int row = 0; row < nrows; row++){
            int    sector = bank.getByte(  "sector",    row);
            int     layer = bank.getByte(  "layer",     row);
            int component = bank.getShort( "component", row);
            if(occupancyCollection.hasEntry(sector, layer, component)==true){
                this.occupancyCollection.get(sector, layer, component).incrementTDC();
            } else {
                DetectorMeasurement measure = new DetectorMeasurement();
                measure.incrementTDC();
                this.occupancyCollection.add(sector, layer, component, measure);
            }
        }
    }
    
    
    
    public int getMaxADC(){
        int max = 0;
        List<DetectorMeasurement> measures = this.occupancyCollection.getList();
         for(DetectorMeasurement m : measures){
             if(m.ADCCount>max){
                 max = m.ADCCount;
             }
        }
        return max;
    }
    
    public int getMaxTDC(){
        int max = 0;
        List<DetectorMeasurement> measures = this.occupancyCollection.getList();
         for(DetectorMeasurement m : measures){
             if(m.TDCCount>max){
                 max = m.TDCCount;
             }
        }
        return max;
    }
    
    public void reset(){
        List<DetectorMeasurement> measures = this.occupancyCollection.getList();
        for(DetectorMeasurement m : measures){
            m.reset();
        }
    }
    
    public static class DetectorMeasurement {
        int ADCCount = 0;
        int TDCCount = 0;
        
        public DetectorMeasurement(){
            
        }
        
        public void reset(){
            this.ADCCount = 0;
            this.TDCCount = 0;
        }
        
        public void incrementADC(){
            this.ADCCount++;
        }
        
        public void incrementTDC(){
            this.TDCCount++;
        }
        
    }
}
