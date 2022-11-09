package org.jlab.detector.decode;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.HistogramPlotter;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author gavalian
 */
public class DetectorDecoderView extends JPanel implements IDataEventListener {

    CodaEventDecoder decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = null; 
    List<DetectorDataDgtz>  detectorData = new ArrayList<>();
    JTable dataTable = null;//new JTable();
    JSplitPane mainSplitPane = null;
    EmbeddedCanvas   dataCanvas = new EmbeddedCanvas();

    public DetectorDecoderView(){
        super();
        detectorDecoder = new DetectorEventDecoder();
        this.setLayout(new BorderLayout());
        this.initUI();
        mainSplitPane.setDividerLocation(0.5);
    }

    private void initUI(){
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        dataTable = new JTable();
        dataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){        
            @Override
            public void valueChanged(ListSelectionEvent e) {
                drawData(dataTable.getSelectedRow());
            }
        });
        JScrollPane   scrollPane = new JScrollPane(dataTable);
        mainSplitPane.setTopComponent(scrollPane);
        mainSplitPane.setBottomComponent(dataCanvas);
        this.add(mainSplitPane);
    }

    @Override
    public void dataEventAction(DataEvent event) {
        List<DetectorDataDgtz>  dataSet = decoder.getDataEntries((EvioDataEvent) event);
        detectorDecoder.translate(dataSet);
        detectorDecoder.fitPulses(dataSet);
        this.updateData(dataSet);
    }

    private void drawData(int index){
        try {
            if(this.detectorData.get(index).getADCSize()>0){
                int nbins = this.detectorData.get(index).getADCData(0).getPulseSize();
                H1F dh = new H1F("dh",nbins, 0.5, ((double) nbins) + 0.5);

                String xTitle = String.format("%s", this.detectorData.get(index).getDescriptor().toString());
                dh.setFillColor(43);
                for(int i = 0; i < nbins; i++){
                    dh.setBinContent(i, detectorData.get(index).getADCData(0).getPulseValue(i));
                }
                this.dataCanvas.clear();
                this.dataCanvas.divide(1, 1);
                this.dataCanvas.getPad(0).getAxisFrame().getAxisX().setTitle(xTitle);
                this.dataCanvas.getPad(0).addPlotter(new HistogramPlotter(dh));
                this.dataCanvas.update();
            }
        } catch (Exception e) {
            System.out.println("error drawing component " + index);
        }
    }

    public  void updateData(List<DetectorDataDgtz> data){
        this.detectorData.clear();
        this.detectorData.addAll(data);
        this.updateTableModel();
    }

    private void updateTableModel(){
        System.out.println( "number of rows to add = " + detectorData.size());
        dataTable.setModel(new DecoderTableModel(this.detectorData));        
    }

    @Override
    public void timerUpdate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetEventListener() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class DecoderTableModel extends AbstractTableModel {
        List<DetectorDataDgtz>  data = null;

        public DecoderTableModel(){
            data = null;
        }

        public DecoderTableModel(List<DetectorDataDgtz> list){
            data = list;
        }

        @Override
        public int getRowCount() {
            if(data==null) return 0;
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 8;
        }

        @Override
        public String getColumnName(int col){
            switch(col) {
                case 0: return "Detector";
                case 1: return "CRATE";
                case 2: return "SLOT";
                case 3: return "CHAN";
                case 4: return "SECTOR";
                case 5: return "LAYER";
                case 6: return "UNIT"; 
                case 7: return "TYPE"; 
                default: return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String type = "unknown";
            DetectorDataDgtz  dgtz = data.get(rowIndex);
            if(dgtz.getTDCSize()>0){
                type = String.format("TDC(  %d  )", dgtz.getTDCData(0).getTime());                
            } else if (dgtz.getADCSize()>0) {
                if(dgtz.getADCData(0).getPulseSize()>0){
                    type = "ADC (PULSE)"; 
                } else {
                    type = "ADC (  "+dgtz.getADCData(0).getADC() + "  )";
                }
            }

            switch(columnIndex) {
                case 0: return data.get(rowIndex).getDescriptor().getType().getName();
                case 1: return Integer.toString(data.get(rowIndex).getDescriptor().getCrate()); 
                case 2: return Integer.toString(data.get(rowIndex).getDescriptor().getSlot());
                case 3: return Integer.toString(data.get(rowIndex).getDescriptor().getChannel());
                case 4: return Integer.toString(data.get(rowIndex).getDescriptor().getSector());
                case 5: return Integer.toString(data.get(rowIndex).getDescriptor().getLayer());
                case 6: return Integer.toString(data.get(rowIndex).getDescriptor().getComponent()); 
                case 7: return type; 
                default: return "error";
            }
        }
    }

    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel  panel = new JPanel();
        panel.setLayout(new BorderLayout());
        DetectorDecoderView  viewer = new DetectorDecoderView();
        DataSourceProcessorPane processorPane = new DataSourceProcessorPane();
        processorPane.addEventListener(viewer);
        panel.add(viewer,BorderLayout.CENTER);
        panel.add(processorPane,BorderLayout.PAGE_END);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
