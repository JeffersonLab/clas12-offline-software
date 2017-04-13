/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.display.ec;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jlab.detector.decode.DetectorDecoderView;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.service.ec.ECEngine;

/**
 *
 * @author gavalian
 */
public class ECMonitor implements IDataEventListener {

    
    DataSourceProcessorPane processorPane = null;
    JPanel                    monitorPane = null;
    EmbeddedCanvasTabbed        tabCanvas = null;
    ECPionFinder               pionFinder = new ECPionFinder();
    ECEngine                   pionEngine = new ECEngine();
    
    H1F   H100_PION_MASS = null;
    List<H2F> histOccupancy = new ArrayList<H2F>();
    List<H1F>  histClusters = new ArrayList<H1F>();
    
    public ECMonitor(){
        initUI();
        initData();
        pionEngine.init();
    }

    
    private void initUI(){
        
        monitorPane = new JPanel();
        monitorPane.setLayout(new BorderLayout());
        processorPane = new DataSourceProcessorPane();
        processorPane.addEventListener(this);
        tabCanvas = new EmbeddedCanvasTabbed("Views","Ocupancy","Pion");
        
        tabCanvas.getCanvas("Pion").initTimer(3000);
        tabCanvas.getCanvas("Ocupancy").initTimer(3000);
        tabCanvas.getCanvas("Views").initTimer(3000);
        
        monitorPane.add(tabCanvas,BorderLayout.CENTER);
        monitorPane.add(processorPane,BorderLayout.PAGE_END);
    }
    
    private void initData(){
        H100_PION_MASS = new H1F("PionMass",120,0.02,0.45);
        H100_PION_MASS.setFillColor(43);
        this.tabCanvas.getCanvas("Pion").draw(H100_PION_MASS);
        
        tabCanvas.getCanvas("Ocupancy").divide(2, 3);
        tabCanvas.getCanvas("Views").divide(2, 3);
        
        for(int i = 0; i < 6; i++){
            H2F h2 = new H2F("HOCUP_S"+(i+1),80,0.5,78.5,9,0.5,9.5);
            this.histOccupancy.add(h2);
            tabCanvas.getCanvas("Ocupancy").cd(i);
            tabCanvas.getCanvas("Ocupancy").draw(h2);
            
            H1F h1 = new H1F("CLUSTERS_S"+(i+1),9,0.5,9.5);
            this.histClusters.add(h1);
            
            tabCanvas.getCanvas("Views").cd(i);
            tabCanvas.getCanvas("Views").draw(h1);
        }
        
        
    }
    
    public JPanel getPanel(){ return this.monitorPane;}
    
    @Override
    public void dataEventAction(DataEvent de) {
        
        try {
            
            this.pionEngine.processDataEvent(de);
            this.pionFinder.processEvent(de);
            //de.show();
            double mass = this.pionFinder.printMass();
            H100_PION_MASS.fill(mass);
            
            if(de.hasBank("ECAL::adc")==true){
                DataBank bank = de.getBank("ECAL::adc");
                int rows = bank.rows();
                for(int i = 0; i < rows; i++){
                    int sector = bank.getByte("sector", i);
                    int layer  = bank.getByte("layer", i);
                    int paddle = bank.getShort("component", i);
                    this.histOccupancy.get(sector-1).fill(paddle, layer);
                }
            }
            
            if(de.hasBank("ECAL::clusters")==true){
                DataBank bank = de.getBank("ECAL::clusters");
                int rows = bank.rows();
                for(int i = 0; i < rows; i++){
                    int sector = bank.getByte("sector", i);
                    int layer  = bank.getByte("layer", i);
                    //int paddle = bank.getShort("component", i);
                    this.histClusters.get(sector-1).fill(layer);
                }
            }
        } catch (Exception e) {
            System.out.println("-----> error with the event.....");
            e.printStackTrace();
        }
    }

    @Override
    public void timerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetEventListener() {
        System.out.println("\n    >>>> reset ec monitor");
        this.H100_PION_MASS.reset();
        for(H2F h : this.histOccupancy){
            h.reset();
        }
        for(H1F h : this.histClusters){
            h.reset();
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ECMonitor viewer = new ECMonitor();
        frame.add(viewer.getPanel());
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
}
