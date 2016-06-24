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
public class DetectorLayerPanel extends JPanel implements MouseListener , MouseMotionListener {
    public  DetectorLayerUI layerUI = new DetectorLayerUI();
    public  IDetectorComponentSelection  selectionListener = null;
    public  Boolean MOUSEOVER_CALLBACK = true;
    
    public DetectorLayerPanel(){
        super();
        this.setPreferredSize(new Dimension(300,300));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        Graphics2D g2d = (Graphics2D) g;
        layerUI.draw2D(g2d, 0, 0, xsize, ysize);
    }

    public void setSelectionListener(IDetectorComponentSelection listener){
        this.selectionListener = listener;
    }
    
    public void mouseClicked(MouseEvent e) {
        System.out.println("\n\n==================================>");
        System.out.println("Mouse clicked (# of clicks: "
                    + e.getClickCount() + ")" + e.getX() + "  " + e.getY());
        DetectorComponentUI cui = this.layerUI.getClickedComponent(e.getX(),e.getY(),
                this.getSize().width, this.getSize().height);
        this.repaint();
        System.out.println(" REGION = " + this.layerUI.drawRegion);
        if(cui!=null){
            System.out.println("FOUND A HIT " + cui.COMPONENT);
            if(this.selectionListener!=null){
                this.selectionListener.detectorSelected(cui.SECTOR,cui.LAYER,cui.COMPONENT);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        /*
        System.out.println("Mouse PRESSED (# of clicks: "
                    + e.getClickCount() + ")" + e);
                */
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }

    public void mouseDragged(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mouseMoved(MouseEvent e) {
        System.out.println("MOUSE MOVED");
        if(this.MOUSEOVER_CALLBACK==true){
            DetectorComponentUI cui = this.layerUI.getClickedComponent(e.getX(),e.getY(),
                this.getSize().width, this.getSize().height);
            this.repaint();
            if(cui!=null){
            //System.out.println("FOUND A HIT " + cui.COMPONENT);
            if(this.selectionListener!=null){
                this.selectionListener.detectorSelected(cui.SECTOR,cui.LAYER,cui.COMPONENT);
            }
        }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
