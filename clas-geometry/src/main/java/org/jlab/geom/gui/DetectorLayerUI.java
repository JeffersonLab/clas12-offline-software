/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

/**
 *
 * @author gavalian
 */
public class DetectorLayerUI {
    
    private ArrayList<DetectorComponentUI>  components = new ArrayList<DetectorComponentUI>();
    public  Rectangle  drawRegion = new Rectangle();
    public  IDetectorComponentSelection  selectionListener = null;
    public  Color   componentColorEven = new Color(77,176,221);
    public  Color   componentColorOdd  = new Color(137,216,68);
    public  Color   componentColorSelected = new Color(210,79,68);
    
    public DetectorLayerUI(){
        
    }
    
    public void setComponents(ArrayList<DetectorComponentUI> uic){
        this.components = uic;
    }
    
    public void reset(){
        for(DetectorComponentUI comp : this.components){
            comp.isActive = false;
        }
    }

    public void setSelectionListener(IDetectorComponentSelection listener){
        this.selectionListener = listener;
    }
    
    public void updateDrawRegion(){
        drawRegion.x = 0;
        drawRegion.y = 0;
        drawRegion.height = 0;
        drawRegion.width  = 0;
        for(DetectorComponentUI comp : components){
            Rectangle bound = comp.shapePolygon.getBounds();
            //System.out.println(bound);
            if(bound.x<drawRegion.x) drawRegion.x = bound.x;
            if(bound.y<drawRegion.y) drawRegion.y = bound.y;
            if((bound.x+bound.width)>(drawRegion.x+drawRegion.width)){
                //System.out.println("yeap X");
                drawRegion.width = (bound.x + bound.width) - drawRegion.x;
            }
            
            if((bound.y+bound.height)>(drawRegion.y+drawRegion.height)){
                //System.out.println("yeap");
                drawRegion.height = (bound.y + bound.height) - drawRegion.y;
            }
        }
        /*
        drawRegion.x = -450;
        drawRegion.y = -450;
        drawRegion.width = 900;
        drawRegion.height = 900;
        */
        if(drawRegion.width>drawRegion.height){
            drawRegion.height = drawRegion.width;
        } else {
            drawRegion.width = drawRegion.height;
        }
        
        if(drawRegion.height>drawRegion.width){
            drawRegion.width = drawRegion.height;
        }
        
        double xfraction = this.drawRegion.width*0.1;
        double yfraction = this.drawRegion.height*0.1;
        
        this.drawRegion.x = (int) (this.drawRegion.x - xfraction);
        this.drawRegion.y = (int) (this.drawRegion.y - yfraction);
        this.drawRegion.width = this.drawRegion.width + (int) (2*xfraction);
        this.drawRegion.height = this.drawRegion.height + (int) (2*yfraction);
        
        /*
        System.out.println("X/Y " + this.drawRegion.x + "  " + this.drawRegion.y +
                 "   W/H " + this.drawRegion.width + "  " + this.drawRegion.height);
                */
    }
    
    
    public int getX(float x, int w){        
        double relX = (x - this.drawRegion.x)/this.drawRegion.width;
        return (int) (relX*w);
    }
    
    public int getY(float y, int h){
        double relY = (y - this.drawRegion.y)/this.drawRegion.height;
        return (int) (relY*h);
    }
    
    public int getCoordinateX(int x, int w){
        double relX = ((double) x) /w;
        return (int) (this.drawRegion.x + relX*this.drawRegion.width);
    }
    
    public int getCoordinateY(int y, int h){
        double relX = ((double)y)/h;
        return (int) (this.drawRegion.y + relX*this.drawRegion.height);
    }
    
    public DetectorComponentUI  getClickedComponent(int x, int y, int width, int height){
        this.reset();
        int xc = this.getCoordinateX(x,width);
        int yc = this.getCoordinateY(y,height);
        System.out.println("X/Y " + x + "  " + y + " W/H  " + width + " " + height + "  XC/YC = " + xc + "  " + yc);
        int counter = 0;
        for(DetectorComponentUI comp : this.components){
            counter++;
            //System.out.println("COMPONENT " + counter);
            //comp.show();
            if(comp.shapePolygon.contains(xc, yc)==true){
                comp.isActive = true;
                return comp;
            }
        }
        return null;
    }
    
    public void draw2D(Graphics2D g2d, int xoff, int yoff, int width, int height){
        this.updateDrawRegion();
        RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_TEXT_ANTIALIASING,
             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        //Color activeColor = new Color(210,79,68);
        
        g2d.setColor(new Color(165,155,155));
        g2d.fillRect(xoff, yoff, width, height);
        //System.out.println("Drawing Detector Layer SIZE = " + this.components.size());
        float[] value = new float[6];
        for(DetectorComponentUI comp : this.components){
            int[] x = comp.shapePolygon.xpoints;
            int[] y = comp.shapePolygon.ypoints;
            //System.out.println("POLYGON SIZE = " + x.length);
            GeneralPath path = new GeneralPath();
            path.moveTo(getX(x[0],width), getY(y[0],height));
            for(int loop = 1; loop < x.length; loop++){
                path.lineTo(getX(x[loop],width), getY(y[loop],height));
            }
            
            path.closePath();
            //g2d.draw(path);
            
            g2d.setColor(this.componentColorOdd);
            if(comp.COMPONENT%2==0) g2d.setColor(this.componentColorEven);
            if(comp.isActive) g2d.setColor(this.componentColorSelected);
            
            g2d.fill(path);
            g2d.setColor(Color.black);
            g2d.draw(path);
        }
    }
}
