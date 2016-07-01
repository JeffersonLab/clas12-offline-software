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
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorDecoderView;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author gavalian
 */
public class RawEventViewer implements IDataEventListener {
    
    DetectorPane2D            detectorView        = null;
    DetectorDecoderView       detectorDecoderView = null;
    
    
    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
    
    DataSourceProcessorPane processorPane = null;
    
    JPanel          pane         = null;
    
    public RawEventViewer() {
        
        pane = new JPanel();
        pane.setLayout(new BorderLayout());
        
        JSplitPane   splitPane = new JSplitPane();
        
        detectorView = new DetectorPane2D();
        this.updateDetectorView();
        splitPane.setLeftComponent(detectorView);
        
        detectorDecoderView = new DetectorDecoderView();
        splitPane.setRightComponent(detectorDecoderView);
        
        pane.add(splitPane,BorderLayout.CENTER);
        processorPane = new DataSourceProcessorPane();
        pane.add(processorPane,BorderLayout.PAGE_END);
        
        
        this.processorPane.addEventListener(this);
    }
   
    
    public final void updateDetectorView(){
        double FTOFSize = 500.0;
        int[]     npaddles = new int[]{23,64,5};
        String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        for(int sector = 1; sector <= 6; sector++){
            double rotation = Math.toRadians(sector-1)*(360.0/6);
            for(int layer = 1; layer <=3; layer++){
                int width = 6;
                for(int paddle = 1; paddle < npaddles[layer-1]; paddle++){
                    
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    shape.createBarXY(20, width);
                    shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0.0);
                    shape.getShapePath().rotateZ(rotation);
                    detectorView.getView().addShape(names[layer-1], shape);
                }
            }
        }
        
        detectorView.updateBox();
    }
    
    public JPanel  getPanel(){
        return pane;
    }

    @Override
    public void dataEventAction(DataEvent event) {
        List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
        detectorDecoder.translate(dataSet);
        detectorDecoder.fitPulses(dataSet);
        //System.out.println(" processed the event data set Size = " + dataSet.size());
        //detectorData.clear();
        //detectorData.addAll(dataSet);
        //this.updateTableModel();
        detectorDecoderView.updateData(dataSet);
        
        detectorView.getView().fill(dataSet, "same");
        //detectorDecoderView.repaint();
    }

    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        RawEventViewer viewer = new RawEventViewer();
        frame.add(viewer.getPanel());
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
}
