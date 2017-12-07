/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.DetectorDecoderView;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.detector.view.DetectorView2D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.IDataSet;
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
public class DaqPulsePlotter implements IDataEventListener,DetectorListener,ActionListener {
    Map<Integer,DetectorCollection<H1F> >  detectorMap = 
            new HashMap<Integer,DetectorCollection<H1F> >();

    JPanel          pane         = null;
    
    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();    
    DataSourceProcessorPane processorPane = null;
    
    //EmbeddedCanvasTabbed        canvasTab = new EmbeddedCanvasTabbed(true);
    //EmbeddedCanvasGroup        canvasTab = new EmbeddedCanvasGroup();
    
    JComboBox  comboDetector = null;
    JComboBox  comboSector = null;
    JComboBox  comboLayer = null;
    JComboBox  comboBunch = null;
    
    public String[] detectorNames = new String[]{"EC","FTOF","CTOF", "HTCC","LTCC"};
    public String[] detectorSectors = new String[] {"1","2","3","4","5","6"};
    public String[] detectorLayers = new String[] {"1","2","3","4","5","6","7","8","9","10"};
    public String[] detectorBunch = new String[] {"1","2","3","4","5","6","7","8","9","10"};
    
    int nDetectorsPerBunch = 12;
    
    
    DetectorPane2D            detectorView        = null;
    DetectorDecoderView       detectorDecoderView = null;
    
    public DaqPulsePlotter(){
        
        pane = new JPanel();
        pane.setLayout(new BorderLayout());
        //canvasTab.addCanvas("DAQ");
        processorPane = new DataSourceProcessorPane();
        processorPane.setDelay(2);
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        
        comboDetector = new JComboBox(this.detectorNames);
        comboSector = new JComboBox(this.detectorSectors);
        comboLayer = new JComboBox(this.detectorLayers);
        comboBunch = new JComboBox(this.detectorBunch);
        
        
        //buttonPane.add(comboDetector);
        //buttonPane.add(comboSector);
        //buttonPane.add(comboLayer);
        //buttonPane.add(new JLabel("Page Number"));
        //buttonPane.add(comboBunch);
        
        //JButton drawButton = new JButton("Draw");
        //drawButton.addActionListener(this);
        //buttonPane.add(drawButton);
        
        pane.add(buttonPane,BorderLayout.PAGE_START);
        
        JSplitPane   splitPane = new JSplitPane();
        
        detectorView = new DetectorPane2D();
        detectorView.getView().addDetectorListener(this);
        this.updateDetectorView();
        splitPane.setLeftComponent(detectorView);
       // splitPane.setRightComponent(canvasTab);
        
        pane.add(splitPane,BorderLayout.CENTER);
        //pane.add(canvasTab,BorderLayout.CENTER);
        pane.add(processorPane,BorderLayout.PAGE_END);
        this.processorPane.addEventListener(this);
    }
   
    
    public void updateDetectorView(){
        
        for(int sector = 1 ; sector < 7; sector++){
            List<DetectorShape2D> shapesPCAL = DetectorView2D.createSector(DetectorType.ECAL, sector, 3, Color.yellow, 180, 10);
            for(DetectorShape2D shape : shapesPCAL) { this.detectorView.getView().addShape("CLAS12", shape);}
            
            List<DetectorShape2D> shapesECIN = DetectorView2D.createSector(DetectorType.ECAL, sector, 4,6, Color.yellow, 190, 10);
            for(DetectorShape2D shape : shapesECIN) { this.detectorView.getView().addShape("CLAS12", shape);}
            
            List<DetectorShape2D> shapesECOUT = DetectorView2D.createSector(DetectorType.ECAL, sector, 7,9, Color.yellow, 200, 10);
            for(DetectorShape2D shape : shapesECOUT) { this.detectorView.getView().addShape("CLAS12", shape);}
            
            List<DetectorShape2D> shapesFTOF = DetectorView2D.createSector(DetectorType.FTOF, sector, 3, Color.blue, 100, 15);
            for(DetectorShape2D shape : shapesFTOF) { this.detectorView.getView().addShape("CLAS12", shape);}
        
            List<DetectorShape2D> shapesHTCC = DetectorView2D.createSector(DetectorType.HTCC, sector, 2, Color.GREEN, 40, 20);
            for(DetectorShape2D shape : shapesHTCC) { this.detectorView.getView().addShape("CLAS12", shape);}
        }
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
        detectorDecoder.translate(dataSet);
        detectorDecoder.fitPulses(dataSet);
        
        for(DetectorDataDgtz data : dataSet){
            this.addPulse(data);
        }
    }

    public H1F getData(DetectorDataDgtz data){
        
        short[] array = data.getADCData(0).getPulseArray();
        int nsamples = 5;
        H1F h = new H1F("h",array.length,0,array.length);
        h.setTitle(String.format("HW [%3d,%3d,%3d] SW [%3d,%3d,%3d]", 
                data.getDescriptor().getCrate(),data.getDescriptor().getSlot(),
                data.getDescriptor().getChannel(),
                data.getDescriptor().getSector(),
                data.getDescriptor().getLayer(),
                data.getDescriptor().getComponent()
                ));
        int sum = 0;
        for(int i = 0; i <nsamples; i++){
            sum += array[i];
        }
        h.setFillColor(34);
        float ped = ((float ) sum)/nsamples;
        for(int i = 0; i < array.length; i++){
            h.setBinContent(i,  (( float ) array[i]) - ped);
        }
        return h;
    }
    
    public void addPulse(DetectorDataDgtz data){
        if(data.getADCSize()==0) return;
        short[] array = data.getADCData(0).getPulseArray();
        
        Integer dType = data.getDescriptor().getType().getDetectorId();
        if(this.detectorMap.containsKey(dType)==false){
            this.detectorMap.put(dType, new DetectorCollection<H1F>());
        }
        
        DetectorCollection<H1F> hStore = this.detectorMap.get(dType);
        
        H1F h1 = this.getData(data);
        int sector = data.getDescriptor().getSector();
        int  layer = data.getDescriptor().getLayer();
        int   comp = data.getDescriptor().getComponent();
        
        if(hStore.hasEntry(sector,layer,comp)==true){
            hStore.get(sector,layer,comp).add(h1);
        } else {
            hStore.add(sector,layer,comp, h1);
        }
    }
    
    @Override
    public void timerUpdate() {
        
    }

    @Override
    public void resetEventListener() {
        System.out.println(" DETECTORS Present = " + this.detectorMap.size());
        for(Map.Entry<Integer,DetectorCollection<H1F>> entry : this.detectorMap.entrySet()){
            Integer key = entry.getKey();
            System.out.println(DetectorType.getType(key) + " ---> " + entry.getValue().getList().size());
        }
        this.detectorMap.clear();
    }

    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println(" doing somethig");
        this.drawSectorLayer(shape.getDescriptor().getType(), 
                shape.getDescriptor().getSector(), 
                shape.getDescriptor().getLayer());
    }
    
    public void drawSectorLayer(DetectorType type, int sector, int layer){
        System.out.println(" looking for type = " + type);
        if(this.detectorMap.containsKey(type.getDetectorId())==true){
            System.out.println(" plotting type = " + type);
            DetectorCollection<H1F> collection = this.detectorMap.get(type.getDetectorId());
            Set<Integer> components = collection.getComponents(sector, layer);
            //EmbeddedCanvas c = this.canvasTab.getCanvas();
            //c.clear();
            //c.divide(3, 4);
            int counter = 0;
            int counterCanvas = 0;
            
            System.out.println("COLLECTION HAS " + collection.getList().size() 
                    + "  COMPONENTS = " + components.size());
            int   bunch = Integer.parseInt(this.comboBunch.getSelectedItem().toString());
            List<IDataSet> datasetList = new ArrayList<IDataSet>();
            for(Integer key : components){
                //c.cd(counterCanvas);
                H1F h = collection.get(sector,layer,key);
                datasetList.add(h);
                //c.draw(h);
            }
            counter++;
            //this.canvasTab.setData(datasetList);
        }
    }
    
    public JPanel getPanel(){return pane;}
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("Draw")==0){
            /*
            String name = this.comboDetector.getSelectedItem().toString();
            int  sector = Integer.parseInt(this.comboSector.getSelectedItem().toString());
            int   layer = Integer.parseInt(this.comboLayer.getSelectedItem().toString());
            int   bunch = Integer.parseInt(this.comboBunch.getSelectedItem().toString());
            System.out.println(name + " " + sector +  "  " + layer + " " + bunch);
            if(this.detectorMap.containsKey(DetectorType.getType(name).getDetectorId())==true){
                DetectorCollection<H1F> collection = this.detectorMap.get(DetectorType.getType(name).getDetectorId());                
                Set<Integer> components = collection.getComponents(sector, layer);
                //EmbeddedCanvas c = this.canvasTab.getCanvas();
                c.clear();
                c.divide(3, 4);
                int counter = 0;
                int counterCanvas = 0;
                System.out.println("COLLECTION HAS " + collection.getList().size() 
                        + "  COMPONENTS = " + components.size());
                
                for(Integer key : components){
                    if(counter>=(bunch-1)*this.nDetectorsPerBunch&&counter<(bunch)*this.nDetectorsPerBunch){
                        c.cd(counterCanvas);
                        H1F h = collection.get(sector,layer,key);
                        c.draw(h);
                        counterCanvas++;
                    }
                    counter++;
                }
            }*/
        }
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DaqPulsePlotter viewer = new DaqPulsePlotter();
        frame.add(viewer.getPanel());
        frame.setSize(900, 600);
        frame.setVisible(true);
    }

    
}
