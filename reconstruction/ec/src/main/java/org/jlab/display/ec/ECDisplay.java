/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.display.ec;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.geom.base.Detector;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.service.ec.ECCluster;
import org.jlab.service.ec.ECCommon;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.ec.ECPeak;
import org.jlab.service.ec.ECStrip;

/**
 *
 * @author gavalian
 */
public class ECDisplay extends JPanel implements IDataEventListener {
    
    DetectorPane2D            detectorView        = null;
    DataSourceProcessorPane processorPane = null;
    ECEngine                detectorEngine = new ECEngine();
    Detector ecDetector = null;
    
    public ECDisplay(){
        super();
        this.setLayout(new BorderLayout());
        detectorView = new DetectorPane2D();
        processorPane = new DataSourceProcessorPane();
        
        this.add(detectorView,BorderLayout.CENTER);
        this.add(processorPane,BorderLayout.PAGE_END);   
        processorPane.addEventListener(this);
        detectorEngine.init();
        ecDetector =  GeometryFactory.getDetector(DetectorType.ECAL);
    }
     

    public void dataEventAction(DataEvent de) {
        detectorEngine.processDataEvent(de);
        
        detectorView.getView().removeLayer("PCAL");
        detectorView.getView().removeLayer("ECIN");
        detectorView.getView().removeLayer("ECOUT");
        
        detectorView.getView().addLayer("PCAL");



        List<ECStrip>  ecStrips = ECCommon.initEC(de, ecDetector, detectorEngine.getConstantsManager(), 10);
        List<ECPeak> ecPeaksALL = ECCommon.createPeaks(ecStrips);
        List<ECPeak> ecPeaks    = ECCommon.processPeaks(ecPeaksALL);
        
        DetectorShape2D shapeP = new DetectorShape2D(DetectorType.ECAL,0,0,0);
        
        shapeP.createBarXY(900,900);
        detectorView.getView().addShape("PCAL", shapeP);
        System.out.println("***********************************************");
        System.out.println("Adding shapes " + ecStrips.size());
        
        for(ECPeak s : ecPeaks){
            
            DetectorShape2D shape = new DetectorShape2D(
                    DetectorType.ECAL,
                    s.getDescriptor().getSector(),s.getDescriptor().getLayer(),
                    s.getMaxStrip());
            System.out.println(s);
            shape.getShapePath().addPoint(s.getLine().origin());
            shape.getShapePath().addPoint(s.getLine().end());
            //System.out.println(s.getLine());
            if(shape.getDescriptor().getLayer()<4)
                detectorView.getView().addShape("PCAL", shape);
            
        }
        System.out.println("*****************  CLUSTERS ");
        List<ECCluster> cPCAL  = ECCommon.createClusters(ecPeaks,1);
        for(ECCluster c : cPCAL){
            System.out.println(c);
        }
        detectorView.repaint();
    }

    public void timerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void resetEventListener() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
      public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        ECDisplay display = new ECDisplay();
        frame.add(display);
        frame.setVisible(true);
    }

}
