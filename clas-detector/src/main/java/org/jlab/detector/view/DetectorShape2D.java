/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class DetectorShape2D {
    
    DetectorDescriptor  desc = new DetectorDescriptor();
    Path3D              shapePath = new Path3D();
    int                 colorRed    = 80;
    int                 colorGreen  = 80;
    int                 colorBlue   = 220;
    
    int                 colorAlpha  = 255;
    int                 counter     = 0;
    int                 lineWidth   = 2;
    String              shapeTitle  = "";
    
    private             DetectorShape2D  activeShape = null;
        
    public DetectorShape2D(){
        
    }
    
    public DetectorShape2D(DetectorType type, int sector, int layer, int component){
        this.desc.setType(type);
        this.desc.setSectorLayerComponent(sector, layer, component);
        this.shapeTitle = String.format("DETECTOR %s SECTOR %4d LAYER %4d  UNIT %4d",
                type.getName(),sector,layer,component);
    }
    
    
    public DetectorDescriptor  getDescriptor(){ return desc;}
    public Path3D              getShapePath(){ return shapePath;}
    
    public void setColor(int r, int g, int b){
        this.colorRed = r;
        this.colorGreen = g;
        this.colorBlue  = b;
        this.colorAlpha = 255;
    }
    
    public void setColor(int r, int g, int b, int alpha){
        this.colorRed = r;
        this.colorGreen = g;
        this.colorBlue  = b;
        this.colorAlpha = alpha;
    }
    
    public Color getSwingColor(){
        return new Color(this.colorRed,this.colorGreen,this.colorBlue,this.colorAlpha);
    }
    
    public Color getSwingColorWithAlpha(int alpha){
        return new Color(this.colorRed,this.colorGreen,this.colorBlue,alpha);
    }
    
    public void createBarXY(double width, double height){
        this.shapePath.clear();
        this.shapePath.addPoint(-width/2.0, -height/2.0,0.0);
        this.shapePath.addPoint(-width/2.0,  height/2.0,0.0);
        this.shapePath.addPoint( width/2.0,  height/2.0,0.0);
        this.shapePath.addPoint( width/2.0, -height/2.0,0.0);        
    }
    
    public void createTrapXY(double dx1, double dx2, double dy){
        this.shapePath.clear();
        this.shapePath.addPoint(-dx1/2.0,  dy/2.0,  0.0);
        this.shapePath.addPoint( dx1/2.0,  dy/2.0,  0.0);
        this.shapePath.addPoint( dx2/2.0, -dy/2.0,  0.0);
        this.shapePath.addPoint(-dx2/2.0, -dy/2.0,  0.0);
    }
    
    public void createArc(double radiusInner, double radiusOutter,
            double angleStart, double angleEnd){
        
        this.shapePath.clear();
        int   numberOfPoints = 80;
        
        this.shapePath.addPoint(
                radiusInner*Math.cos(Math.toRadians(angleStart)),
                radiusInner*Math.sin(Math.toRadians(angleStart)),
                0.0);
        
        this.shapePath.addPoint(
                radiusOutter*Math.cos(Math.toRadians(angleStart)),
                radiusOutter*Math.sin(Math.toRadians(angleStart)),
                0.0);
        
        double step = (angleEnd - angleStart)/numberOfPoints;
        for(double angle = angleStart; angle < angleEnd; angle+=step){
            this.shapePath.addPoint(
                    radiusOutter*Math.cos(Math.toRadians(angle)),
                    radiusOutter*Math.sin(Math.toRadians(angle)),
                    0.0);
        }
        
        this.shapePath.addPoint(
                radiusOutter*Math.cos(Math.toRadians(angleEnd)),
                radiusOutter*Math.sin(Math.toRadians(angleEnd)),
                0.0);
        this.shapePath.addPoint(
                radiusInner*Math.cos(Math.toRadians(angleEnd)),
                radiusInner*Math.sin(Math.toRadians(angleEnd)),
                0.0);
        for(double angle = angleEnd; angle > angleStart; angle-=step){
            this.shapePath.addPoint(
                    radiusInner*Math.cos(Math.toRadians(angle)),
                    radiusInner*Math.sin(Math.toRadians(angle)),
                    0.0);
        }
    }
    
    public void reset(){
        this.counter = 0;
    }
    
    public int getCounter(){ return counter; }
    public DetectorShape2D setCounter(int c){
        this.counter = c;
        return this;
    }
    
    public void setColorByStatus(int status){
        int rs = status;
        if(status>10) rs = 10;
        if(status<0)  rs = 0;
        int red   = (25*rs);
        int green = (255-red);
        System.out.println(" setting color " + red + " " + green + " 0");
        this.setColor(red,green,0);
    }
    
    public boolean isContained(double x, double y){
        int i, j;
        boolean c = false;
        int nvert = shapePath.size();
        for (i = 0, j = nvert-1; i < nvert; j = i++) {
            if ( (( shapePath.point(i).y()>y) != (shapePath.point(j).y()>y)) &&
                    (x < ( shapePath.point(j).x()-shapePath.point(i).x()) * 
                    (y-shapePath.point(i).y()) / (shapePath.point(j).y()-shapePath.point(i).y()) +
                    shapePath.point(i).x()))
                c = !c;
        }
        return c;
        //return false;
    }
    
    
    public void drawShape(Graphics2D g2d, ViewWorld world, Color fillcolor, Color strokecolor){
        GeneralPath path = new GeneralPath();
        if(this.shapePath.size()>0){
            double xp = shapePath.point(0).x();
            double yp = shapePath.point(0).y();
            path.moveTo(world.getPointX(xp),world.getPointY(yp));
            for(int i = 1; i < shapePath.size(); i++){
                xp = shapePath.point(i).x();
                yp = shapePath.point(i).y();
                path.lineTo(world.getPointX(xp),world.getPointY(yp));
            }
            
            xp = shapePath.point(0).x();
            yp = shapePath.point(0).y();
            
            path.lineTo(world.getPointX(xp),world.getPointY(yp));
            g2d.setColor(fillcolor);
            g2d.fill(path);
            g2d.setColor(strokecolor);
            //g2d.setStroke(new BasicStroke(2));
            g2d.draw(path);
        }        
    }
    
    public GeneralPath  getGeneralPath(){
        GeneralPath path = new GeneralPath();
        
        return path;
    }

    
  
}
