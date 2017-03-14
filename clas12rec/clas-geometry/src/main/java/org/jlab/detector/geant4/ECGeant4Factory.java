/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4;

import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ECGeant4Factory {
    
    
    
    public Geant4Basic   getPCAL(){
        Geant4Basic  mVolume = new Geant4Basic("PCAL","box",490.0,490.0,490.0);
        double xh  = 385.2/2.0;
        double dw       = 4.5;   
        double angle    = Math.toRadians(62.905);
        Line3D  midpointV = this.getMidpointLineV(1);
        for(int s = 0; s < 77; s++){
            //Line3D uStrip = this.stripU(1, s);
            Line3D uStrip = this.stripV(1, s);            
            Geant4Basic box = new Geant4Basic("","box",(0.8*dw)/2.0,uStrip.length()/2.0,2.0);
            Line3D center = midpointV.distance(uStrip);
            box.setRotation("xyz", 0.0, 0.0, angle);
            box.setPosition(center.midpoint().x(), center.midpoint().y(),0.0);
            //box.setPosition(uStrip.origin().x(), 0.0, 0.0);
            mVolume.getChildren().add(box);            
        }
        
         for(int s = 0; s < 84; s++){
            Line3D uStrip = this.stripU(1, s);
             //Line3D uStrip = this.stripV(1, s);            
            Geant4Basic box = new Geant4Basic("","box",(0.8*dw)/2.0,uStrip.length()/2.0,2.0);
            Line3D center = midpointV.distance(uStrip);
            //box.setRotation("xyz", 0.0, 0.0, angle);
            //box.setPosition(center.midpoint().x(), center.midpoint().y(),0.0);
            box.setPosition(uStrip.origin().x(), 0.0, 10.0);
            mVolume.getChildren().add(box);            
        }
         
         for(int n = 0; n < 12; n++){
             Geant4Basic led = this.getLayerVolume(1, 0.5);
             led.setPosition(0.0, 0.0, 20.0 + n*5.0);
             mVolume.getChildren().add(led);
         }
        return mVolume;
    }
    
    public Line3D lineV(int layer){
        Line3D  line = new Line3D();
        double xh    = 385.2/2.0;
        double yh    = 394.2/2.0;
        line.setOrigin(-xh, 0.0,0.0);
        line.setEnd(   xh, -yh,0.0);
        return line;
    }
    
    
    public Line3D getMidpointLineV(int layer){
        Line3D  vLine   = this.lineV(layer);
        Line3D  vLine2 = this.stripV(layer, 20);
        double angle    = Math.atan2(
                vLine2.midpoint().x() - vLine.midpoint().x(),
                vLine2.midpoint().y() - vLine.midpoint().y());
        return new Line3D(vLine.midpoint().x(),vLine.midpoint().y(),0.0,
        vLine.midpoint().x()+1000.0*Math.sin(angle),vLine.midpoint().y()+1000.0*Math.cos(angle),0.0);
    }
    
    public Line3D getMidpointLineW(int layer){
        Line3D  vLine   = this.lineW(layer);
        Line3D  vLine2 = this.stripW(layer, 20);
        double angle    = Math.atan2(
                vLine2.midpoint().x() - vLine.midpoint().x(),
                vLine2.midpoint().y() - vLine.midpoint().y());
        return new Line3D(vLine.midpoint().x(),
                vLine.midpoint().y(),0.0,
                vLine.midpoint().x()+1000.0*Math.sin(angle),
                vLine.midpoint().y()+1000.0*Math.cos(angle),0.0);
    }
    
    public Line3D lineW(int layer){
        Line3D  line = new Line3D();
        double xh    = 385.2/2.0;
        double yh    = 394.2/2.0;
        line.setOrigin(-xh, 0.0,0.0);
        line.setEnd(    xh,  yh,0.0);
        return line;
    }
    
    public Line3D lineU(int layer){
        Line3D  line = new Line3D();
        double xh    = 385.2/2.0;
        double yh    = 394.2/2.0;
        line.setOrigin( xh, -yh,0.0);
        line.setEnd(    xh,  yh,0.0);
        return line;
    }
    
    
    public Line3D stripU(int layer, int strip){
        double angle    = Math.toRadians(62.905);
        double noseAnle =  Math.toRadians(180.0 - 2.0*62.905);
        double xheight  = 385.2/2.0;
        double dw       = 4.5;        
        Line3D vLine  = this.lineV(layer);
        Line3D wLine  = this.lineW(layer);
        Line3D uLine  = this.lineU(layer);
        double offset = strip*dw + dw*0.5;
        Line3D crop  = new Line3D(uLine.origin().x()-offset,-1000.0,0.0, uLine.origin().x()-offset,+1000.0,0.0); 
        //System.out.println("U _ LINE");uLine.show();
        Line3D vi = crop.distance(vLine);
        Line3D wi = crop.distance(wLine);
        return new Line3D(vi.midpoint(),wi.midpoint());
    }
    
    public Line3D stripV(int layer, int strip){
        
        double angle = Math.toRadians(62.905);
        double noseAnle =  Math.toRadians(180.0 - 2.0*62.905);
        double alpha    = Math.toRadians(90.0-62.905);
        double xheight  = 385.2/2.0;
        double dw       = 4.5;
        Line3D vLine  = this.lineV(layer);
        Line3D wLine  = this.lineW(layer);
        Line3D uLine  = this.lineU(layer);
        double offset = strip*dw + dw*0.5;
        
        Line3D crop  = new Line3D(
                vLine.origin().x() + offset*Math.sin(alpha),
                vLine.origin().y() + offset*Math.cos(alpha),
                0.0, 
                vLine.end().x() + offset*Math.sin(alpha),
                vLine.end().y() + offset*Math.cos(alpha)
                ,0.0); 
        
        //System.out.println("U _ LINE");uLine.show();
        Line3D ui = crop.distance(uLine);
        Line3D wi = crop.distance(wLine);
        return new Line3D(ui.midpoint(),wi.midpoint());        
    }
    
    public Line3D stripW(int layer, int strip){
        double angle = Math.toRadians(62.905);
        double noseAnle =  Math.toRadians(180.0 - 2.0*62.905);
        double xheight  = 385.2/2.0;
        double dw       = 4.5;
        return null;
    }
    
    public Geant4Basic  getLayerVolume(int layer, double zw){
        double xh  = 385.2/2.0;
        double yh  = 394.2/2.0;
        Geant4Basic basic = new Geant4Basic("volume","trd",xh,xh,0.2,yh,zw);
        //Geant4Basic basic = new Geant4Basic("volume","box",xh,yh,zw);
        //System.out.println(basic.toString());
        return basic;
    }
    
    public static void main(String[] args){
        ECGeant4Factory  factory = new ECGeant4Factory();
        for(int s = 0; s < 84; s++){
            Line3D line = factory.stripU(1, s);
            line.show();
        }
    }
}
