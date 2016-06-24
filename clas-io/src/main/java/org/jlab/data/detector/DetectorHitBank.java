/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.detector;

import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class DetectorHitBank {
    
    private DetectorType  detectorType = DetectorType.UNDEFINED;
    private TreeMap<Integer,Object>  bankData = new TreeMap<Integer,Object>();
    
    public DetectorHitBank(){
        
    }
    
    public DetectorHitBank(DetectorType type){
        this.detectorType = type;
    }
    
    public DetectorHitBank(DetectorType type,int size){
        this.detectorType = type;
        this.allocate(size);
    }
    
    public final void allocate(int rows){
        bankData.clear();
        bankData.put(1, new byte[rows]);  // sector
        bankData.put(2, new byte[rows]);  // layer 
        bankData.put(3, new byte[rows]);  // component
        bankData.put(4, new byte[rows]);  // CLUSTER ID
        bankData.put(5, new float[rows]); // Energy
        bankData.put(6, new float[rows]); // Time
        bankData.put(7, new float[rows]); // Hit Coordinate - X
        bankData.put(8, new float[rows]); // Hit Coordinate - Y
        bankData.put(9, new float[rows]); // Hit Coordinate - Z        
    }
    
    public int getSector(int index){
        return ((byte[])this.bankData.get(1))[index];
    }
    
    public int getLayer(int index){
        return ((byte[])this.bankData.get(2))[index];
    }
    
    public int getComponent(int index){
        return ((byte[])this.bankData.get(3))[index];
    }
    
    public int getRows(){
        return ((byte[])this.bankData.get(1)).length;
    }
    
    public double getTime(int index){
        return ((float[]) this.bankData.get(6))[index];
    }
    
    public double getEnergy(int index){
        return ((float[]) this.bankData.get(5))[index];
    }
    
    public double getX(int index){
        return ((float[]) this.bankData.get(7))[index];
    }
    
    public double getY(int index){
        return ((float[]) this.bankData.get(8))[index];
    }
    
    public double getZ(int index){
        return ((float[]) this.bankData.get(9))[index];
    }
    
    public void setXYZ(float x, float y, float z, int index){
        
    }
    
    public String rawToString(int index){
        StringBuilder str = new StringBuilder();
        str.append(String.format("S/L/C [%4d %4d %4d]", 
                this.getSector(index),this.getLayer(index),this.getComponent(index)));
        str.append(String.format("  Energy/Time [%8.5f %8.5f]", this.getEnergy(index),this.getTime(index)));
        str.append(String.format("  X/Y/Z", 
                this.getX(index),this.getY(index),this.getZ(index)
                ));
        return str.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int loop = 0; loop < this.getRows(); loop++){
            str.append(this.rawToString(loop));
        }
        return str.toString();
    }
}
