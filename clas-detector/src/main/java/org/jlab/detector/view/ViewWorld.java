/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.view;

import org.jlab.groot.math.Dimension2D;

/**
 *
 * @author gavalian
 */
public class ViewWorld {
    
    Dimension2D   axisWorld = new Dimension2D();
    Dimension2D   axisView  = new Dimension2D();
    
    public ViewWorld(){
        
    }
    
    public void setWorld(Dimension2D worldDim){
        axisWorld.copy(worldDim);
    }
    
    public void setView(Dimension2D viewDim){
        
        this.axisView.copy(viewDim);
        this.updateAspectRatio();
        /*
        double aspectRatioW = axisWorld.getDimension(0).getLength()/axisWorld.getDimension(1).getLength();
        double aspectRatioV = viewDim.getDimension(0).getLength()/viewDim.getDimension(1).getLength();
        System.out.println("ASPECT RATIO : W = " + aspectRatioW + "  VIEW = " + aspectRatioV);
        
        double correctionX = 1.0;
        double correctionY = 1.0;
        double centerOffsetX = 0.0;
        double centerOffsetY = 0.0;
        
        
        if(aspectRatioV>aspectRatioW){
            correctionY = aspectRatioV/aspectRatioW;
            centerOffsetY = viewDim.getDimension(1).getMin() + 0.5*viewDim.getDimension(1).getLength();
            this.axisView.getDimension(1).setMinMax(
                centerOffsetY + viewDim.getDimension(1).getMin() - 0.5*correctionY*viewDim.getDimension(1).getLength(),               
                centerOffsetY + viewDim.getDimension(1).getMin() + 0.5*correctionY*viewDim.getDimension(1).getLength());
            this.axisView.getDimension(0).copy(viewDim.getDimension(0));
            //System.out.println(" correction Y = " + correctionY);
        } else {
            correctionX = aspectRatioW/aspectRatioV;
            centerOffsetX = viewDim.getDimension(0).getMin() + 0.5*viewDim.getDimension(0).getLength();
            this.axisView.getDimension(0).setMinMax(
                    centerOffsetX + viewDim.getDimension(0).getMin() - 0.5*correctionX*viewDim.getDimension(0).getLength(), 
                    centerOffsetX + viewDim.getDimension(0).getMin() + 0.5*correctionX*viewDim.getDimension(0).getLength());
            this.axisView.getDimension(1).copy(viewDim.getDimension(1));
        }*/
        /*
        double growX = correctionX * viewDim.getDimension(0).getLength() - viewDim.getDimension(0).getLength();
        this.axisView.getDimension(0).setMinMax(
                centerOffsetX + viewDim.getDimension(0).getMin() - 0.5*correctionX*viewDim.getDimension(0).getLength(), 
                centerOffsetX + viewDim.getDimension(0).getMin() + 0.5*correctionX*viewDim.getDimension(0).getLength());
        
        this.axisView.getDimension(1).setMinMax(
                centerOffsetY + viewDim.getDimension(1).getMin() - 0.5*correctionY*viewDim.getDimension(1).getLength(),               
                centerOffsetY + viewDim.getDimension(1).getMin() + 0.5*correctionY*viewDim.getDimension(1).getLength());
        */
    }

    public void updateAspectRatio(){
        
        double aspectRatioW = this.axisWorld.getDimension(0).getLength()/this.axisWorld.getDimension(1).getLength();
        double aspectRatioV = this.axisView.getDimension(0).getLength()/this.axisView.getDimension(1).getLength();
        //System.out.println(String.format("ASPECT RATIO WORLD = %8.3f  VIEW = %8.3f", aspectRatioW,aspectRatioV));
        
        if(aspectRatioV>aspectRatioW){
            double lenY = this.axisView.getDimension(1).getLength();
            double midY = this.axisView.getDimension(1).getMin() + 
                    0.5*lenY;
            double factor = aspectRatioV/aspectRatioW;
            double min = midY - factor*lenY*0.5;
            double max = midY + factor*lenY*0.5;
            this.axisView.getDimension(1).setMinMax(min, max);
            //System.out.println(String.format(" LENGTH = %8.3f   MID = %8.3f factor = %9.6f min/max = %9.6f %9.6f", 
            //        lenY,midY,factor,min,max));
        } else {
            double lenX = this.axisView.getDimension(0).getLength();
            double midX = this.axisView.getDimension(0).getMin() + 
                    0.5*lenX;
            double factor = aspectRatioW/aspectRatioV;
            double min = midX - factor*lenX*0.5;
            double max = midX + factor*lenX*0.5;
            this.axisView.getDimension(0).setMinMax(min, max);
        }
        
        
        //if(ar > 1.0){            
        //}
    }
    
    public double getViewX(double worldX){
        double fraction = this.axisWorld.getDimension(0).getFraction(worldX);
        return this.axisView.getDimension(0).getPoint(fraction);
    }
    
    public double getViewY(double worldY){
        double fraction = this.axisWorld.getDimension(1).getFraction(worldY);
        return this.axisView.getDimension(1).getPoint(fraction);
    }
    
    public void show(){
        System.out.println(" WORLD : " + axisWorld);
        System.out.println(" VIEW  : " + axisView);
    }
    
    public double getPointX(double value){
        double fraction = this.axisView.getDimension(0).getFraction(value);
        return this.axisWorld.getDimension(0).getPoint(fraction);
    }
    
    public double getPointY(double value){
        double fraction = this.axisView.getDimension(1).getFraction(value);
        //System.out.println(" FRACTION = for value " + value + " = " + fraction);
        return this.axisWorld.getDimension(1).getPoint(fraction);
    }
    
    public static void main(String[] args){
        
        ViewWorld  world = new ViewWorld();
        
        double sizeY = 2.25;
        
        world.setWorld(new Dimension2D(0,16.0,0.0,9.0));
        world.setView(new Dimension2D(0,8.0,4,sizeY+4));
        world.show();                
        /*System.out.println( " BRUT FORCE TESTY = " + 20 + "  world = " + world.getPointY(20.0));
        world.setView(new Dimension2D(0,160.0,40,80));
        world.show();                
        System.out.println( " BRUT FORCE TESTY = " + 60 + "  world = " + world.getPointY(60.0));
        
        System.out.println( " BRUT FORCE TESTX = " + 4.0  + "  world = " + world.getPointX(4.0));
        

        for(double d  = 0.0; d < 1.0; d+=0.1){
            System.out.println( " Y = " + d + "  world = " + world.getPointY(d));
        }*/
    }
}
