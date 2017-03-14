/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import javax.swing.JFrame;

/**
 *
 * @author gavalian
 */
public class ViewerTests {
    

    
    public static void createView(DetectorView2D view){
        
        int narcs = 20;
        
        for(int i = 0; i < narcs; i++){
            DetectorShape2D shape = new DetectorShape2D();
            shape.getDescriptor().setSectorLayerComponent(0, 0, i+1);
            double start = i*360.0/narcs;
            double step  = 360.0/narcs;
            double gap   = 60.0/narcs;            
            //double end   = (i+1)*360.0/narcs;            
            shape.createArc(10, 20, start - step*0.5, start + step*0.5);
            if(i%2==0) view.addShape("V", shape);
        }
        
        for(int i = 0; i < narcs; i++){
            DetectorShape2D shape = new DetectorShape2D();
            shape.getDescriptor().setSectorLayerComponent(0, 1, i+1);
            double start = i*360.0/narcs;
            double step  = 360.0/narcs;
            double gap   = 60.0/narcs;            
            //double end   = (i+1)*360.0/narcs;
            shape.setColor(220, 120, 120);
            shape.createArc(10, 20, start + 25.0- step*0.5, start + 25.0 + step*0.5);
            if(i%2==0) view.addShape("U", shape);
        }
        
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        
        //DetectorView2D view = new DetectorView2D();
        DetectorPane2D view = new DetectorPane2D();

        ViewerTests.createView(view.getView());
        view.updateBox(); 
        frame.add(view);
        //view.setSize(600, 600);
        frame.setSize(600,600);

        //frame.pack();
        frame.setVisible(true);
    }
}
