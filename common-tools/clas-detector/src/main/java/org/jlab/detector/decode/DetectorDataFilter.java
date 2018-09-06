/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javax.swing.JTextField;
import org.jlab.jnp.hipo4.data.DataType;

/**
 *
 * @author gavalian
 */
public class DetectorDataFilter {
    
    private List<Integer>         detectorCRATES = new ArrayList<Integer>();
    private List<DetectorType>     detectorTypes = new ArrayList<DetectorType>();
    
    public DetectorDataFilter(){
        
    }
    
    public DetectorDataFilter addCrate(int crate){ this.detectorCRATES.add(crate);  return this; }
    
    public DetectorDataFilter addDetector( DetectorType type) { 
        this.detectorTypes.add(type);
        return this;
    }
    
    public List<DetectorDataDgtz>  filter(List<DetectorDataDgtz> data){
        List<DetectorDataDgtz> filtered = new ArrayList<DetectorDataDgtz>();        
        for(DetectorDataDgtz dgtz : data){
            boolean add_data = true;
            if(this.detectorTypes.size()>0){
                boolean type_pass = false;
                for(DetectorType type : detectorTypes){
                    if(dgtz.getDescriptor().getType()==type){
                        type_pass = true;
                    }
                }
                if(type_pass!=true) add_data = false;
            }
            
            if(detectorCRATES.size()>0){
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
    
    public static class DetectorDataFilterPane extends JPanel implements ActionListener {
        
        DetectorDataFilter filter = new DetectorDataFilter();
        
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
        }
        
        public DetectorDataFilter getFilter(){return this.filter;}

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().compareTo("add_detector")==0){
                Object[] possibilities = {"ECAL","FTOF","DC","BST","FTCAL","FTTRK"};
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
               
                
            }
            
            if(e.getActionCommand().compareTo("Reset")==0){
                this.filter.reset();
            }
        }
        
        
    }
}
