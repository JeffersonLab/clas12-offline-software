/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.data.analysis;

import java.util.ArrayList;

/**
 *
 * @author gavalian
 */
public class UnbinnedData {
    private ArrayList<Double> unbinned_data = new ArrayList<Double>();    
    public  UnbinnedData(){
        
    }
    
    public int size(){
        return unbinned_data.size();
    }
    
    public double data(int index){
        return unbinned_data.get(index);
    }
    
    public void addData(double x){
        unbinned_data.add(x);
    }
    
    public double mean(){
        double integral = 0.0;
        for(Double value : unbinned_data){
            integral += value;
        }
        return integral/unbinned_data.size();
    }
    
    public double rms(){
        double mean = this.mean();
        double rms_sum = 0.0;
        for(Double value : unbinned_data){
            rms_sum += (value-mean)*(value-mean);
        }
        return Math.sqrt(rms_sum/unbinned_data.size());
    }
    
    public double min(){
        if(unbinned_data.isEmpty()) return 0.0;
        double d_min = unbinned_data.get(0);
        for(Double x : unbinned_data){
            if(x<d_min) d_min = x;
        }
        return d_min;
    }
    
    public double max(){
        if(unbinned_data.isEmpty()) return 0.0;
        double d_max = unbinned_data.get(0);
        for(Double x : unbinned_data){
            if(x>d_max) d_max = x;
        }
        return d_max;
    }
}
