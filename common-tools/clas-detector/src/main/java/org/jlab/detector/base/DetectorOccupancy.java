/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class DetectorOccupancy {
    
    DetectorCollection<DetectorMeasurement>  occupancyCollection = 
            new DetectorCollection<DetectorMeasurement>();
    
    private int maxLayers     = 9;
    private int maxComponents = 72;
    private int ADCThreshold  = 100;
    private int TDCThreshold  = 100;
    
    public DetectorOccupancy(){
        
    }
    
    public DetectorOccupancy(int max_layers, int max_components){
        this.maxLayers     = max_layers;
        this.maxComponents = max_components;
    }
    
    public void addADCBank(DataBank bank){
        int nrows = bank.rows();
        for(int row = 0; row < nrows; row++){
            int    sector = bank.getByte(  "sector",    row);
            int     layer = bank.getByte(  "layer",     row);
            int component = bank.getShort( "component", row);
            int       adc = bank.getInt("ADC", row);
            if(adc>this.ADCThreshold){
                if(occupancyCollection.hasEntry(sector, layer, component)==true){
                    this.occupancyCollection.get(sector, layer, component).incrementADC();
                } else {
                    DetectorMeasurement measure = new DetectorMeasurement();
                    measure.incrementADC();
                    this.occupancyCollection.add(sector, layer, component, measure);
                }
            }
        }
    }
    
    public void addTDCBank(DataBank bank){
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
    
    public int getTDC(int sector, int layer, int component){
        if(this.occupancyCollection.hasEntry(sector, layer, component)==false)
            return 0;
        return this.occupancyCollection.get(sector, layer, component).TDCCount;
    }
    
    public int getADC(int sector, int layer, int component){
        if(this.occupancyCollection.hasEntry(sector, layer, component)==false)
            return 0;
        return this.occupancyCollection.get(sector, layer, component).ADCCount;
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
    
    public GraphErrors  getOccupancyGraph(){
        int maxADC = this.getMaxADC();
        Set<Long> keySet = this.occupancyCollection.getKeys();
        GraphErrors graph = new GraphErrors();
        graph.setMarkerSize(0); 
        graph.setLineColor(4);
        
        Set<Integer>  sectors = this.getCollection().getSectors();
        
        for(Integer sector : sectors){
            Set<Integer> layers = this.getCollection().getLayers(sector);
            for(Integer layer : layers){
                Set<Integer> components = this.getCollection().getComponents(sector, layer);
                for(Integer component : components){
                    double x = (sector) * 1.0 + (layer-1)*1.0/maxLayers + (component-1)*1.0/maxLayers/maxComponents;
                    DetectorMeasurement measure = this.occupancyCollection.get(sector, layer, component);
                    double intencity = ((double) measure.ADCCount)/maxADC;
                    graph.addPoint((double) x, 0.0, 0.0, intencity);
                }
            }
        }        
        return graph;
    }
    
    public GraphErrors  getOccupancyGraphTDC(){
        int maxTDC = this.getMaxTDC();
        System.out.println(" TDC max = " + maxTDC);
        Set<Long> keySet = this.occupancyCollection.getKeys();
        GraphErrors graph = new GraphErrors();
        graph.setMarkerSize(0); 
        graph.setLineColor(4);
        
        Set<Integer>  sectors = this.getCollection().getSectors();
        
        for(Integer sector : sectors){
            Set<Integer> layers = this.getCollection().getLayers(sector);
            for(Integer layer : layers){
                Set<Integer> components = this.getCollection().getComponents(sector, layer);
                for(Integer component : components){
                    double x = (sector) * 1.0 + (layer-1)*1.0/maxLayers + (component-1)*1.0/maxLayers/maxComponents;
                    DetectorMeasurement measure = this.occupancyCollection.get(sector, layer, component);
                    double intencity = ((double) measure.TDCCount)/maxTDC;
                    graph.addPoint((double) x, 0.0, 0.0, intencity);
                    System.out.println(" MAX = " + maxTDC + " " + sector + " " + layer + " " + component + " " + measure.TDCCount);
                }
            }
        }        
        return graph;
    }
    
    public DetectorCollection getCollection(){ return this.occupancyCollection;}
    
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
    
    public static void main(String[] args){
        HipoDataSource reader = new HipoDataSource();
        reader.open("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/ecreconstruction/gemc_dis.hipo");
        DetectorOccupancy occupancyECAL = new DetectorOccupancy();
        DetectorOccupancy occupancyFTOF = new DetectorOccupancy(3,63);
        DetectorOccupancy occupancyDC   = new DetectorOccupancy(36,112);
        
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            if(event.hasBank("ECAL::adc")==true){
                DataBank bank = event.getBank("ECAL::adc");
                occupancyECAL.addADCBank(bank);
            }
            if(event.hasBank("FTOF::adc")==true){
                DataBank bank = event.getBank("FTOF::adc");
                occupancyFTOF.addADCBank(bank);
            }
            if(event.hasBank("DC::tdc")==true){
                DataBank bank = event.getBank("DC::tdc");
                occupancyDC.addTDCBank(bank);
            }
        }
        
        GraphErrors graphECAL = occupancyECAL.getOccupancyGraph();
        GraphErrors graphFTOF = occupancyFTOF.getOccupancyGraph();
        GraphErrors graphDC   = occupancyDC.getOccupancyGraphTDC();
        TCanvas c1 = new TCanvas("c1",800,300);
        c1.divide(1, 3);
        c1.cd(0);
        graphECAL.setTitle("ECAL");
        c1.draw(graphECAL);
        c1.getCanvas().getPad(0).setAxisRange(1.0, 7.0, -1.2, 1.2);
        c1.cd(1);
        graphFTOF.setTitle("FTOF");
        c1.draw(graphFTOF);
        c1.getCanvas().getPad(1).setAxisRange(1.0, 7.0, -1.2, 1.2);
        c1.cd(2);
        graphDC.setTitle("DC");
        c1.draw(graphDC);
        c1.getCanvas().getPad(2).setAxisRange(1.0, 7.0, -1.2, 1.2);
    }
}
