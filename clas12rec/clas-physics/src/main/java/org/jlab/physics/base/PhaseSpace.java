/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.base;

import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author gavalian
 */
public class PhaseSpace {
    
    Map<String,DimensionSpace>  params = new LinkedHashMap<String,DimensionSpace>();
    JPanel   spacePanel   = null;
    
    public PhaseSpace(){
        
    }
    
    public Set<String> getKeys(){
        return params.keySet();
    }
    
    public void add(DimensionSpace space){
        this.params.put(space.getName(), space);
    }
    
    public void add(String name, double min, double max){
        this.add(new DimensionSpace(name,min,max));
    }
    
    public void add(String name, double value, double min, double max){
        DimensionSpace dim = new DimensionSpace(name,min,max);
        dim.setValue(value);
        this.add(dim);
    }
    
    public DimensionSpace getDimension(String name){
        return this.params.get(name);
    }    
    
    public JPanel  createPanel(){
        this.spacePanel = new JPanel();
        this.spacePanel.setLayout(new FlowLayout());
        for(Map.Entry<String,DimensionSpace> item : this.params.entrySet()){
            item.getValue().createPanel();
            JPanel panel = item.getValue().getPanel();
            this.spacePanel.add(panel);
        }
        return this.spacePanel;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int c = 0; c < 73; c++) str.append("*");
        str.append("\n");
        str.append(String.format("* %-24s * %12s * %12s * %12s * ",
                "Name", "Min","Max","Value"));
        str.append("\n");
        for(int c = 0; c < 73; c++) str.append("*");
        str.append("\n");
        for(Map.Entry<String,DimensionSpace> item : this.params.entrySet()){
            str.append(item.getValue().toString());
            str.append("\n");
        }
        for(int c = 0; c < 73; c++) str.append("*");
        str.append("\n");
        return str.toString();
    }
    
    public void show(){
        System.out.println(this.toString());
    }
    
    public void setRandom(){
        for(Map.Entry<String,DimensionSpace> entry : this.params.entrySet()){
            entry.getValue().setRandom();
        }        
    }
    
    public PhaseSpace  copy(){
        PhaseSpace space = new PhaseSpace();
        for(Map.Entry<String,DimensionSpace> entry : this.params.entrySet()){
            space.add(entry.getKey(), entry.getValue().getMin(), entry.getValue().getMax());
        }
        return space;
    }
    
    public static void main(String[] args){
        PhaseSpace space = new PhaseSpace();
        space.add("x" , 0.0, 1.0);
        space.add("pt" , 0.5, 1.5);
        
        System.out.println(space);
        
        JFrame frame = new JFrame();
        frame.add(space.createPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
