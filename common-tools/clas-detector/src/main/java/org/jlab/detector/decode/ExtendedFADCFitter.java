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
    
    public int     t0,adc,ped,pedsum;
    public int     thresholdCrossing,pulsePeakValue, pulsePeakPosition, pulseWidth;
    public double  baseline, rms;
    private int    tcourse, tfine;
    
    public ExtendedFADCFitter(){
        
    }
    
    public void fitCourseTime(int nsa, int nsb, int tet, int pedr, short[] pulse) {
        pedsum=0;adc=0;mmsum=0;summing_in_progress=0;
        for (int mm=0; mm<pulse.length; mm++) {
            if(mm>p1 && mm<=p2)  pedsum+=pulse[mm];
            if(mm==p2)           pedsum=pedsum/(p2-p1);				
            if(mm>p2) {
                if (pedr==0) ped=pedsum;
                if (pedr!=0) ped=pedr;
                if ((summing_in_progress==0) && pulse[mm]>ped+tet) {
                    summing_in_progress=1;
                    tcourse = mm;
                    t0 = (tcourse << 6);
                    for (int ii=1; ii<nsb+1;ii++) adc+=(pulse[mm-ii]-ped);
                    mmsum=nsb;
                }
                if(summing_in_progress>0 && mmsum>(nsa+nsb)) summing_in_progress=-1;
                if(summing_in_progress>0) {adc+=(pulse[mm]-ped); mmsum++;}
            }
        }
    }
        
    public void fit(int nsa, int nsb, int tet, int pedr, short[] pulse) {
            t0=0; adc=0; ped=0; pedsum=0; baseline=0; rms=0; 
            thresholdCrossing=0; pulsePeakValue=0; pulsePeakPosition=0; pulseWidth=0;
            tcourse=0; tfine=0;
            double noise  = 0;
            int    tstart = pedistalMaxBin+1;
            int    tcross = 0; 
            int    pmax   = 0;
            int    ppos   = 0;
            // calculate pedestal means and noise
            if (pedr!=0) ped=pedr;        // use default mode 7 pedestal range (1-4)
            if(pulse.length<p2+1 && pedr==0) {
                for (int bin = 0; bin < pulse.length; bin++) {
                    pedsum += pulse[bin];
                }  
                ped=pedsum/pulse.length;
                return;
            }
            if (pedr==0) {
                tstart = p2+1;
                for (int bin = p1+1; bin < p2+1; bin++) {
                    pedsum += pulse[bin];
                    noise  += pulse[bin] * pulse[bin];
                }
                baseline = ((double) pedsum)/ (p2 - p1);
                ped = pedsum=pedsum/(p2-p1);	//(int) baseline;
                rms = Math.sqrt(noise / (p2 - p1) - baseline * baseline);
            }
            // find threshold crossing
            for (int bin=tstart; bin<pulse.length; bin++) {
                if(pulse[bin]>ped+tet) {
                    tcross=bin;
                    thresholdCrossing=tcross;
                    break;
                }
            }
            if(tcross>0) {
                // calculate integral and find maximum
                for (int bin=Math.max(0,tcross-nsb); bin<Math.min(pulse.length,tcross+nsa+1); bin++) { // sum should be up to tcross+nsa (without +1), this was added to match the old fit method
                    adc+=pulse[bin]-ped;
                    if(bin>=tcross && pulse[bin]>pmax) {
                        pmax=pulse[bin];
                        ppos=bin;
                    }
                }
                pulsePeakPosition=ppos;
                pulsePeakValue=pmax;
                // calculating mode 7 pulse time    
                double halfMax = (pmax+baseline)/2;
                int s0 = -1;
                int s1 = -1;
                for (int bin=tcross-1; bin<Math.min(pulse.length-1,ppos+1); bin++) {
                    if (pulse[bin]<=halfMax && pulse[bin+1]>halfMax) {
                        s0 = bin;
                        break;
                    }
                }
                for (int bin=ppos; bin<Math.min(pulse.length-1,tcross+nsa); bin++) {
                    if (pulse[bin]>halfMax && pulse[bin+1]<=halfMax) {
                        s1 = bin;
                        break;
                    }
                }
                if(s0>-1) { 
                    int a0 = pulse[s0];
                    int a1 = pulse[s0+1];
                    // set course time to be the sample before the 50% crossing
                    tcourse = s0;
                    // set the fine time from interpolation between the two samples before and after the 50% crossing (6 bits resolution)
                    tfine   = ((int) ((halfMax - a0)/(a1-a0) * 64));
                    t0      = (tcourse << 6) + tfine;
                }
                if(s1>-1 && s0>-1) {
                    int a0 = pulse[s1];
                    int a1 = pulse[s1+1];
                    pulseWidth  = s1 - s0;
                }
//                System.out.println(ped + " " + pmax + " " + adc + " " + tcross + " " + ((float) tcourse+tfine/64.) + " " + ppos);
            }

    }

    private double findPedestal(DetectorDataDgtz.ADCData data){
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
            double ped = this.findPedestal(data);
        }
    }
    
    
}
