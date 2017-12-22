/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

/**
 *
 * @author gavalian
 */
public class DetectorPane2D extends JPanel implements ActionListener {
    
    JPanel          buttonsPane = null;
    JPanel          toolbarPane = null;
    DetectorView2D       view2D = new DetectorView2D();
    List<JCheckBox>       checkButtons = new ArrayList<JCheckBox>();
    JPanel                checkBoxPane = null;
    
    public DetectorPane2D(){
        super();
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
        initUI();
    }
    
    public final void initUI(){
        buttonsPane = new JPanel();
        toolbarPane = new JPanel();
        buttonsPane.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        this.add(toolbarPane,BorderLayout.PAGE_START);
        this.add(view2D,BorderLayout.CENTER);
        this.add(buttonsPane,BorderLayout.PAGE_END);
        
        
        JButton chooseColor = new JButton("background");
        chooseColor.addActionListener(this);
        this.buttonsPane.add(chooseColor);
        
        this.checkBoxPane = new JPanel();
    }
    
    public void updateBox(){
        
        this.checkButtons.clear();
        this.checkBoxPane.removeAll();
        
        for(String name : this.view2D.getLayerNames()){
            JCheckBox  cb = new JCheckBox(name);
            cb.setSelected(true);
            
            cb.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox box = (JCheckBox) e.getItem();
                    //System.out.println("changed " + box.getActionCommand());
                    if(box.isSelected()==false){
                        view2D.setLayerActive(box.getActionCommand(), false);
                        view2D.repaint();
                    } else {
                        view2D.setLayerActive(box.getActionCommand(), true);
                        view2D.repaint();
                    }
                }
            });
            
            System.out.println(" adding check box " + name);
            checkBoxPane.add(cb);
        }
        this.buttonsPane.add(checkBoxPane);
        System.out.println(" check box created");
        
        JCheckBox  hitMap = new JCheckBox("Hit Map");
        hitMap.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox box = (JCheckBox) e.getItem();
                    if(box.isSelected()==false){
                        view2D.setHitMap(false);
                        view2D.repaint();
                    } else { 
                        view2D.setHitMap(true);
                        view2D.repaint();
                    } 
                }
        }
        );
        this.toolbarPane.add(hitMap);
    }
    
    public DetectorView2D  getView(){
        return this.view2D;
    }

    public JPanel getToolbar() {
        return toolbarPane;
    }
    
    public void update(){
        this.getView().repaint();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("background")==0){
            Color c = JColorChooser.showDialog(this, "Choose a Color",Color.BLACK);
            if (c != null){
                System.out.println("change background color");
                //view2D.setBackground(c);
                view2D.changeBackground(c);
            } else {
                System.out.println(" color is NULL ");
            }

        }
    }
}
