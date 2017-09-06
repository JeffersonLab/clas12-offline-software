/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.examples;

import java.awt.BorderLayout;
import java.util.ArrayList;
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
import org.jlab.groot.graphics.EmbeddedCanvas;
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
        processorPane.setDelay(1500);
        pane.add(canvasTab,BorderLayout.CENTER);
        pane.add(processorPane,BorderLayout.PAGE_END);
        this.processorPane.addEventListener(this);
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
        detectorDecoder.translate(dataSet);
        detectorDecoder.fitPulses(dataSet);
        
        
        for(int sector = 1; sector <=6 ; sector++){
            String name = "Sector " + sector;
            EmbeddedCanvas canvas = this.canvasTab.getCanvas(name);
            canvas.clear();
            canvas.divide(1, 5);
            List<H1F> hPCAL = this.getHistograms(dataSet, DetectorType.ECAL, sector, 1);
            canvas.cd(0);
            for(H1F h : hPCAL){ 
                h.setTitle("ADC PULSES [PCAL]");
                canvas.draw(h, "same");
            }
            canvas.cd(1);
            List<H1F> hECIN = this.getHistograms(dataSet, DetectorType.ECAL, sector, 4);
            for(H1F h : hECIN){ h.setTitle("ADC PULSES [EC]"); canvas.draw(h, "same");}
            canvas.cd(2);
            List<H1F> hFTOF1A = this.getHistograms(dataSet, DetectorType.FTOF, sector, 1);
            for(H1F h : hFTOF1A){ h.setTitle("ADC PULSES [FTOF 1A]"); canvas.draw(h, "same");}
            canvas.cd(3);
            List<H1F> hFTOF1B = this.getHistograms(dataSet, DetectorType.FTOF, sector, 2);
            for(H1F h : hFTOF1B){ h.setTitle("ADC PULSES [FTOF 1B]"); canvas.draw(h, "same");}
            canvas.cd(4);
            List<H1F> hHTCC = this.getHistograms(dataSet, DetectorType.HTCC, sector);
            for(H1F h : hHTCC){ canvas.draw(h, "same");}            
        }                        
    }
    
    public List<H1F>  getHistograms(List<DetectorDataDgtz>  dataSet, DetectorType type, int sector){
        List<DetectorDataDgtz>   PCAL = DetectorDataDgtz.getDataADC(dataSet, type,sector);
        List<H1F>  histograms = new ArrayList<H1F>();
        int counter = 1;
        for(DetectorDataDgtz data : PCAL){
            short[] array = data.getADCData(0).getPulseArray();
            H1F h = new H1F(type.getName()+"_S_" + sector + "_I_" + counter,
                    "",array.length,0.0,(double) array.length);
            for(int i = 0; i < array.length; i++){
                h.setBinContent(i, array[i]);
            }
            //this.canvasTab.getCanvas("Sector 2").draw(h, "same");
            h.setTitle("ADC PULSES ["+type.getName()+"]");
            h.setTitleX("ADC channels (4 ns)");
            histograms.add(h);
            counter++;
        }
        return histograms;
    }
    
    public List<H1F>  getHistograms(List<DetectorDataDgtz>  dataSet, DetectorType type, int sector, int layer){
        List<DetectorDataDgtz>   PCAL = DetectorDataDgtz.getDataADC(dataSet, type,sector,layer);
        List<H1F>  histograms = new ArrayList<H1F>();
        int counter = 1;
        for(DetectorDataDgtz data : PCAL){
            short[] array = data.getADCData(0).getPulseArray();
            H1F h = new H1F(type.getName()+"_S_" + sector + "_L_" + layer + "_I_" + counter,
                    "",array.length,0.0,(double) array.length);
            for(int i = 0; i < array.length; i++){
                h.setBinContent(i, array[i]);
            }
            //this.canvasTab.getCanvas("Sector 2").draw(h, "same");
            h.setTitle("ADC PULSES ["+type.getName()+"]");
            h.setTitleX("ADC channels (4 ns)");
            histograms.add(h);
            counter++;
        }
        return histograms;
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
