package org.jlab.detector.decode;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author gavalian
 */
public class DetectorDataFilter {
    
    private List<Integer>         detectorCRATES = new ArrayList<>();
    private List<DetectorType>     detectorTypes = new ArrayList<>();
    
    public DetectorDataFilter(){
        
    }

    public DetectorDataFilter addCrate(int crate){ this.detectorCRATES.add(crate);  return this; }

    public DetectorDataFilter addDetector( DetectorType type) { 
        this.detectorTypes.add(type);
        return this;
    }

    public List<DetectorDataDgtz>  filter(List<DetectorDataDgtz> data){
        List<DetectorDataDgtz> filtered = new ArrayList<>();        
        for(DetectorDataDgtz dgtz : data){
            boolean add_data = true;
            if(!this.detectorTypes.isEmpty()){
                boolean type_pass = false;
                for(DetectorType type : detectorTypes){
                    if(dgtz.getDescriptor().getType()==type){
                        type_pass = true;
                    }
                }
                if(type_pass!=true) add_data = false;
            }

            if(!detectorCRATES.isEmpty()){
                boolean crate_pass = false;
                for(Integer crate : detectorCRATES){
                    if(dgtz.getDescriptor().getCrate()==crate){
                        crate_pass = true;
                    }
                }
                if(crate_pass!=true) add_data = false;
            }

            if(add_data == true ) filtered.add(dgtz);
        }
        return filtered;
    }

    public void reset(){
        this.detectorTypes.clear();
        this.detectorCRATES.clear();
    }

    @Override
    public String toString(){
       StringBuilder str = new StringBuilder();
       int counter = 0;
       str.append("[");
       for(DetectorType type : this.detectorTypes){
           if(counter!=0) str.append(",");
           str.append(type.getName());
           counter++;
       }
       str.append("]");
       counter = 0;
       str.append("[");
       for(Integer crate : this.detectorCRATES){
           if(counter!=0) str.append(",");
           str.append(crate);
           counter++;
       }
       str.append("]");
       return str.toString();
    }

    public static class DetectorDataFilterPane extends JPanel implements ActionListener {

        DetectorDataFilter filter = new DetectorDataFilter();
        JLabel             filterText = null;

        public DetectorDataFilterPane(){
            super();
            setLayout(new FlowLayout());
            initUI();
        }

        private void initUI(){
            this.add(new JLabel("Detector Type : "));
            JButton buttonAddDetector = new JButton("+");
            buttonAddDetector.setActionCommand("add_detector");
            buttonAddDetector.addActionListener(this);
            this.add(buttonAddDetector);
            this.add(new JLabel("Crate : "));
            JButton buttonAddCrate = new JButton("+");
            buttonAddCrate.setActionCommand("add_crate");
            buttonAddCrate.addActionListener(this);
            this.add(buttonAddCrate);

            JButton buttonAddReset = new JButton("Reset");
            buttonAddReset.addActionListener(this);
            this.add(buttonAddReset);

            filterText = new JLabel();
            filterText.setText(filter.toString());

            this.add(filterText);
        }

        public DetectorDataFilter getFilter(){return this.filter;}

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().compareTo("add_detector")==0){
                Object[] possibilities = {"ECAL","FTOF","DC","BST","FTCAL","FTTRK", "RF"};
                String s = (String) JOptionPane.showInputDialog(
                    this,
                    "Add Detector to the filter:\n"
                    ,
                    "Detector Filter",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities,
                    "FTOF");
                if ((s != null) && (s.length() > 0)) {
                    DetectorType type = DetectorType.getType(s);
                    this.filter.addDetector(type);
                }
            }
            if(e.getActionCommand().compareTo("add_crate")==0){
               Object[] possibilities = new Object[99];
               for(int i = 0; i < 99; i++){
                   Integer crate = i+1;
                   possibilities[i] = crate.toString();
               }
               String s = (String) JOptionPane.showInputDialog(
                    this,
                    "Add Crate to the filter:\n"
                    ,
                    "Detector Filter",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities,
                    "FTOF");
                if ((s != null) && (s.length() > 0)) {
                    Integer crate = Integer.parseInt(s);
                    this.filter.addCrate(crate);
                }
            }

            if(e.getActionCommand().compareTo("Reset")==0){
                this.filter.reset();
            }

            this.filterText.setText(filter.toString());
        }

    }
}
