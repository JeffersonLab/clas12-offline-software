/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author gavalian
 */
public class DetectorViewPanel extends JPanel {
    
    private		JTabbedPane tabbedPane;
    
    public DetectorViewPanel(){
        super();
        //this.setPreferredSize(new Dimension(600,600));
        this.setLayout(new BorderLayout());
        this.initComponents();
    }
    
    private void initComponents(){
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane,BorderLayout.CENTER);
    }
    
    public void addDetectorLayer(String name, DetectorLayerPanel panel){
        tabbedPane.addTab( name, panel);
    }
    
    public void addDetectorLayer(String name, DetectorShape3DPanel panel){
        tabbedPane.addTab( name, panel);
    }
}
