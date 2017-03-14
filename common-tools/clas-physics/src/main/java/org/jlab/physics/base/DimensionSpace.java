/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.base;

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 *
 * @author gavalian
 */
public class DimensionSpace {
    
    
    String dimName  = "unknown";
    double dimValue = 0.5;
    double dimMinimum = 0.0;
    double dimMaximum = 1.0;
    double constrainUpper = 1.0;
    double constrainLower = 0.0;
    int    numberOfBins   = 1;
    
    JPanel dimPanel   = null;
    
    public DimensionSpace(){
        
    }
    
    public DimensionSpace(String name, double min, double max){
        this.set(name, min, max);
    }
    
    public final void set(String name, double min, double max){
        this.dimName    = name;
        this.dimMinimum = min;
        this.dimMaximum = max;    
        this.dimValue   = min + 0.5*(max-min);
    }
    
    public void reset(){
        this.constrainUpper = 1.0;
        this.constrainLower = 0.0;
    }
    
    public void setContsrains(double lower, double upper){
        this.constrainUpper = upper;
        this.constrainLower = lower;
    }
    
    public double getRandom(){
        double rndm = Math.random();
        double low  = this.dimMinimum + (this.dimMaximum - this.dimMinimum)*this.constrainLower;
        double max  = this.dimMinimum + (this.dimMaximum - this.dimMinimum)*this.constrainUpper;
        return low + rndm*(max-low);
    }
    
    public void setRandom(){
        this.dimValue = this.getRandom();
    }
    
    public int getBin(){
        double delta = (this.dimMaximum-this.dimMinimum)/this.numberOfBins;
        double w = (this.dimValue-this.dimMinimum)/delta;
        int  bin = (int) w;
        return bin;
    }
    
    public int  getNBins(){
        return this.numberOfBins;
    }
    
    public void setNBins(int bins){
        this.numberOfBins = bins;
    }
    
    public String getName(){ return this.dimName;}
    
    public void   setValue(double value){ this.dimValue = value;}
    
    public void setValueNormalized(double value){
        if(value<0.0||value>1.0){
            System.out.println("error: normalized value has to be 0.0-1.0");
        } else {
            this.dimValue = this.dimMinimum + value*(this.dimMaximum-this.dimMinimum);
        }
    }
    
    public double getValue() { return this.dimValue;}
    public double getMin() { return this.dimMinimum;}
    public double getMax() { return this.dimMaximum;}
    
    public void createPanel(){
        this.dimPanel = new JPanel();
        this.dimPanel.setLayout(new FlowLayout());
        JLabel  nameLabel = new JLabel(this.dimName);
        JSlider jsUpper = new JSlider(JSlider.HORIZONTAL,
                0, 100, 0);
        JSlider jsLower = new JSlider(JSlider.HORIZONTAL,
                0, 100, 100);
        
        this.dimPanel.add(nameLabel);
        this.dimPanel.add(jsUpper);
        this.dimPanel.add(jsLower);
    }
    
    public JPanel  getPanel(){ return this.dimPanel;}
    
    @Override
    public String toString(){
        StringBuilder  str = new StringBuilder();
        str.append(String.format("* %-24s * %12.6f * %12.6f * %12.6f * ",
                this.dimName,this.dimMinimum,this.dimMaximum,this.dimValue));
        return str.toString();
    }
}
