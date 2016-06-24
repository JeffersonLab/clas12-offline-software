/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;

/**
 *
 * @author gavalian
 */
public class DetectorShape3DPanel extends JPanel implements MouseListener , MouseMotionListener {
    
    private DetectorShape3DStore shapeStore = new DetectorShape3DStore();
    public  IDetectorComponentSelection  selectionListener = null;
    public  Boolean MOUSEOVER_CALLBACK = true;
    
    public DetectorShape3DPanel(){
        super();
        this.setPreferredSize(new Dimension(300,300));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    public DetectorShape3DStore  getStore(){ return this.shapeStore;}
    
    public void setSelectionListener(IDetectorComponentSelection listener){
        this.selectionListener = listener;
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        Graphics2D g2d = (Graphics2D) g;
        this.shapeStore.draw2D(g2d, 0, 0, xsize, ysize);
    }
    
    public void setColorIntensity(IDetectorShapeIntensity intens){
        this.shapeStore.setIntensityMap(intens);
    }
    
    public void mouseClicked(MouseEvent e) {
        DetectorShape3D cui = this.shapeStore.getSelectedShape(e.getX(),e.getY(),
                this.getSize().width, this.getSize().height);
        this.repaint();
        //System.out.println(" REGION = " + this.layerUI.drawRegion);
        if(cui!=null){
            //System.out.println("FOUND A HIT " + cui.COMPONENT);
            if(this.selectionListener!=null){
                this.selectionListener.detectorSelected(cui.SECTOR,cui.LAYER,cui.COMPONENT);
            }
        }

    }

    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }

    public void mouseDragged(MouseEvent e) {
        
    }

    public void mouseMoved(MouseEvent e) {
        
        if(this.MOUSEOVER_CALLBACK==true){
            this.shapeStore.reaset();
            DetectorShape3D cui = this.shapeStore.getSelectedShape(e.getX(),e.getY(),
                this.getSize().width, this.getSize().height);
        this.repaint();
        //System.out.println(" REGION = " + this.layerUI.drawRegion);
        if(cui!=null){
            //System.out.println("FOUND A HIT " + cui.COMPONENT);
            if(this.selectionListener!=null){
                this.selectionListener.detectorSelected(cui.SECTOR,cui.LAYER,cui.COMPONENT);
            }
        }
        }
        
    }
    
}
