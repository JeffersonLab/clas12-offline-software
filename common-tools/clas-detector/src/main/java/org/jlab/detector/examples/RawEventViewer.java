package org.jlab.detector.examples;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorDataFilter.DetectorDataFilterPane;
import org.jlab.detector.decode.DetectorDecoderView;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.decode.FADCData;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.logging.DefaultLogger;

/**
 *
 * @author gavalian
 */
public class RawEventViewer implements IDataEventListener,DetectorListener {
    
    DetectorPane2D            detectorView        = null;
    DetectorDecoderView       detectorDecoderView = null;
    
    
    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
    
    DataSourceProcessorPane  processorPane = null;
    DetectorDataFilterPane   detectorFilterPane = null;
    
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
        
        
        detectorFilterPane = new DetectorDataFilterPane();
        
        pane.add(detectorFilterPane, BorderLayout.PAGE_START);
        
        pane.add(splitPane,BorderLayout.CENTER);
        processorPane = new DataSourceProcessorPane();
        pane.add(processorPane,BorderLayout.PAGE_END);
        
        this.detectorView.getView().addDetectorListener(this);
        this.processorPane.addEventListener(this);
    }
   
    
    public final void updateDetectorView(){
        double FTOFSize = 500.0;
        int[]     npaddles = new int[]{64,23,5};
        int[]     widths   = new int[]{6,15,25};
        int[]     lengths  = new int[]{6,15,25};
        
        String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        for(int sector = 1; sector <= 6; sector++){
            double rotation = Math.toRadians(sector-1)*(360.0/6);
            
            for(int layer = 1; layer <=3; layer++){
            
                int width  = widths[layer-1];
                int length = lengths[layer-1];
                
                for(int paddle = 1; paddle < npaddles[layer-1]; paddle++){
                    
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    shape.createBarXY(20 + length*paddle, width);
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
        List<DetectorDataDgtz>  dataSetFull = decoder.getDataEntries((EvioDataEvent) event);
        
        List<FADCData>  fadcPacked = decoder.getADCEntries((EvioDataEvent) event);
        if(fadcPacked!=null){
            List<DetectorDataDgtz> dataSetADC = FADCData.convert(fadcPacked);
            dataSetFull.addAll(dataSetADC);
        }

        
        detectorDecoder.translate(dataSetFull);
        detectorDecoder.fitPulses(dataSetFull);
        
        List<DetectorDataDgtz>  dataSet = this.detectorFilterPane.getFilter().filter(dataSetFull);
        //System.out.println(" EVENT TYPE = " + event.getType());
        //System.out.println(" processed the event data set Size = " + dataSet.size());
        //detectorData.clear();
        //detectorData.addAll(dataSet);
        //this.updateTableModel();
        if(event.getType()==DataEventType.EVENT_SINGLE){
            detectorDecoderView.updateData(dataSet);
            detectorView.getView().fill(dataSet, "");
        } else {
            detectorView.getView().fill(dataSet, "same");
        }
        
        if(event.getType()==DataEventType.EVENT_START){
            this.clearHistograms();
        }
        
        if(event.getType()==DataEventType.EVENT_ACCUMULATE){
            this.fillhistograms();
        }
        
        if(event.getType()==DataEventType.EVENT_STOP){
            this.analyzeData();
        }
        detectorView.update();
        //detectorDecoderView.repaint();
    }

    public void clearHistograms(){
        
    }
    
    
    public void analyzeData(){
        
    }
    
    public void fillhistograms(){
        
    }
    
    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        
    }
    
    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
    }
    
    public static void main(String[] args){
        DefaultLogger.debug();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        RawEventViewer viewer = new RawEventViewer();
        frame.add(viewer.getPanel());
        frame.setSize(900, 600);
        frame.setVisible(true);
    }

   
}
