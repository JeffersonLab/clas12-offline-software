/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

/**
 *
 * @author gavalian
 */
public class ExtendedFADCFitter implements IFADCFitter {

    private int pedistalMinBin = 1;
    private int pedistalMaxBin = 5;
    
    int p1=1,p2=15;
    int mmsum,summing_in_progress;
    
    public int t0,adc,ped,pedsum;
    
    public ExtendedFADCFitter(){
        
    }
    
    public void fit(int nsa, int nsb, int tet, int pedr, short[] pulse) {
        pedsum=0;adc=0;mmsum=0;summing_in_progress=0;
        for (int mm=0; mm<pulse.length; mm++) {
            if(mm>p1 && mm<=p2)  pedsum+=pulse[mm];
            if(mm==p2)           pedsum=pedsum/(p2-p1);				
            if(mm>p2) {
                if (pedr==0) ped=pedsum;
                if (pedr!=0) ped=pedr;
                if ((summing_in_progress==0) && pulse[mm]>ped+tet) {
                    summing_in_progress=1;
                    t0 = mm;
                    for (int ii=1; ii<nsb+1;ii++) adc+=(pulse[mm-ii]-ped);
                    mmsum=nsb;
                }
                if(summing_in_progress>0 && mmsum>(nsa+nsb)) summing_in_progress=-1;
                if(summing_in_progress>0) {adc+=(pulse[mm]-ped); mmsum++;}
            }
        }
    }
        
    
    private double findPedistal(DetectorDataDgtz.ADCData data){
        int ped = 0;
        for(int i = this.pedistalMinBin; i < this.pedistalMaxBin; i++){
            ped += data.getPulseValue(i);
        }
        double ped_norm =   ((double) ped) / (Math.abs(this.pedistalMaxBin-this.pedistalMinBin)); 
        return ped_norm;
    }
    
    private int findMaximumADC(DetectorDataDgtz.ADCData data){
        int bin = this.pedistalMaxBin ;
        int max = data.getPulseValue(pedistalMaxBin);
        
        for(int i = this.pedistalMaxBin; i < data.getPulseSize(); i++){
            if(data.getPulseValue(i)>max){
                max = data.getPulseValue(i);
                bin = i;
            }
        }
        return bin;
    }
    
    private int findPulseIntegral(DetectorDataDgtz.ADCData data, int maxValueBin, int nsa, int nsb){
        int min_bin = maxValueBin - nsb;
        int max_bin = maxValueBin + nsa;
        if(min_bin>=0&&max_bin<data.getPulseSize()){
            
        } else {
            System.out.println("MIN _ MAX out of range....");            
        }
        return 0;
    }
    
    @Override
    public void fit(DetectorDataDgtz.ADCData data) {
        if(data.getPulseSize()>this.pedistalMaxBin){
            double ped = this.findPedistal(data);
        }
    }
    
    
}
