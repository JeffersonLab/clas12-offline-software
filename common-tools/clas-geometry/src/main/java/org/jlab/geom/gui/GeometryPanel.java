/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author gavalian
 */
public class GeometryPanel extends JPanel {
    
    private final ArrayList<Line3D> lines = new ArrayList<Line3D>();
    private double xmax = 100;
    private double ymax = 100;

    public GeometryPanel(int xh, int yh, double xm, double ym){
        this.setPreferredSize(new Dimension(xh,yh));
        xmax = xm;
        ymax = ym;
    }
    
    public void addLineXY(Line3D line){
        Line3D cl = new Line3D();
        cl.setOrigin(
                line.origin().x(), 
                line.origin().y(), 
                line.origin().z());
        cl.setEnd(
                line.end().x(),
                line.end().y(),
                line.end().z()
        );
        lines.add(cl);
    }
    
    public void addLineXZ(Line3D line){
        Line3D cl = new Line3D();
        cl.setOrigin(
                line.origin().x(), 
                line.origin().z(), 
                line.origin().y());
        cl.setEnd(
                line.end().x(),
                line.end().z(),
                line.end().y()
        );
        lines.add(cl);
    }
    
    public void addLineYZ(Line3D line){
        Line3D cl = new Line3D();
        cl.setOrigin(
                line.origin().x(), 
                line.origin().y(), 
                line.origin().z());
        cl.setEnd(
                line.end().x(),
                line.end().y(),
                line.end().z()
        );
        lines.add(cl);
    }
    
    private int[] translateCoordinates(double x, double y){
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        
        x = -x;
        y = -y;
        double x_shifted = x + xmax;
        double y_shifted = y + ymax;
        double x_relative = x_shifted/(2.0*xmax);
        double y_relative = y_shifted/(2.0*ymax);
        double xc_r_off = x_relative*(xsize);
        double yc_r_off = y_relative*(ysize);
        
        int[] newcoords = new int[]{0,0};
        newcoords[0] = (int)(xc_r_off);
        newcoords[1] = (int)(yc_r_off);
        return newcoords;
    }
    
    private void drawAxis(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        g2d.drawLine(0,ysize/2,xsize,ysize/2);
        g2d.drawLine(xsize/2,0,xsize/2,ysize);
    }
    
    private void doDrawing(Graphics g) {
        this.drawAxis(g);
        Graphics2D g2d = (Graphics2D) g;
        int xsize = this.getSize().width;
        int ysize = this.getSize().height;
        //g2d.drawLine(0, 0, xsize/2,ysize/2);
        for(Line3D line : lines){
            int[] coords_o = this.translateCoordinates(
                    line.origin().x(), 
                    line.origin().y());
            int[] coords_e = this.translateCoordinates(
                    line.end().x(), 
                    line.end().y());
            g2d.drawLine(coords_o[0], coords_o[1], coords_e[0],coords_e[1]);
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        this.doDrawing(g);
    }
    
    
}
