/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.examples;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author gavalian
 */
public class DetectorTiming implements IDataEventListener,DetectorListener {
    JPanel          pane         = null;

    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();    
    DataSourceProcessorPane processorPane = null;
    EmbeddedCanvasTabbed        canvasTab = new EmbeddedCanvasTabbed();
    
    
    public DetectorTiming(){
        pane = new JPanel();
        pane.setLayout(new BorderLayout());
        for(int i = 1; i <= 6; i++){
            canvasTab.addCanvas("Sector " + i);
        }
        
        processorPane = new DataSourceProcessorPane();
        processorPane.setDelay(500);
        pane.add(canvasTab,BorderLayout.CENTER);
        pane.add(processorPane,BorderLayout.PAGE_END);
        this.processorPane.addEventListener(this);
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
        detectorDecoder.translate(dataSet);
        detectorDecoder.fitPulses(dataSet);
        
        List<DetectorDataDgtz>   PCAL = DetectorDataDgtz.getDataADC(dataSet, DetectorType.EC, 2, 1);
        List<DetectorDataDgtz>  ECIN = DetectorDataDgtz.getDataADC(dataSet, DetectorType.EC, 2, 4);
        System.out.println("SIZE = " + dataSet.size() + "  PCAL = " + PCAL.size() + " ECIN = " + ECIN.size());
        this.canvasTab.getCanvas("Sector 2").clear();
        this.canvasTab.getCanvas("Sector 2").divide(1, 4);
        this.canvasTab.getCanvas("Sector 2").cd(2);
        int counter = 1;
        for(DetectorDataDgtz data : PCAL){
            short[] array = data.getADCData(0).getPulseArray();
            H1F h = new H1F("PCAL_"+counter,"",array.length,0.0,(double) array.length);
            for(int i = 0; i < array.length; i++){
                h.setBinContent(i, array[i]);
            }
            this.canvasTab.getCanvas("Sector 2").draw(h, "same");
            counter++;
        }
        
        this.canvasTab.getCanvas("Sector 2").cd(3);
        counter = 1;
        for(DetectorDataDgtz data : ECIN){
            short[] array = data.getADCData(0).getPulseArray();
            H1F h = new H1F("ECIN_"+counter,"",array.length,0.0,(double) array.length);
            for(int i = 0; i < array.length; i++){
                h.setBinContent(i, array[i]);
            }
            this.canvasTab.getCanvas("Sector 2").draw(h, "same");
            counter++;
        }
    }
    
    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        
    }

    @Override
    public void processShape(DetectorShape2D shape) {
        
    }
    
    public JPanel getPanel(){return pane;}
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DetectorTiming viewer = new DetectorTiming();
        frame.add(viewer.getPanel());
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
}
