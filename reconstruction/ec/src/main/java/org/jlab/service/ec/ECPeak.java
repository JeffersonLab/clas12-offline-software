package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */

public class ECPeak implements Comparable {
    
    private DetectorDescriptor  desc       = new DetectorDescriptor(DetectorType.ECAL);
    private List<ECStrip>       peakStrips = new ArrayList<ECStrip>();
    private Line3D              peakLine   = new Line3D();
    public int                  indexMaxStrip = -1;
    private int                 peakOrder     = -1;
    private byte                peakStatus    = 1;
    private boolean             peakSplit     = false;
    private double              peakDistanceEdge = 0.0;
    private double              peakMoment       = 0.0;
    private double              peakMoment2      = 0.0;
    private double              peakMoment3      = 0.0;
    private double              splitRatio       = -1;
    private int                 splitStrip       = 0;
    private int                 splitEnergy      = 0;
    private Map<Integer,Integer>      imap       = new HashMap<>();
    //private int                 peakID        = -1;
    static int ind[]  = {0,0,0,1,1,1,2,2,2}; 
    
    public ECPeak(ECStrip strip){
        desc.setSectorLayerComponent(strip.getDescriptor().getSector(), 
                                     strip.getDescriptor().getLayer(), 0);
        peakStrips.add(strip);
        peakLine.copy(strip.getLine());
        strip.setStripID(0);
        setimap(0, strip.getADC());
        indexMaxStrip = 0;
    }
    
    public void setOrder(int order)       {peakOrder = order;}
    public void setStatus(int val)        {peakStatus+=val;}
    public void setSplitRatio(double val) {splitRatio = val;}
    public void setPeakSplit(boolean val) {peakSplit = val;}
    public void setimap(int v1, int v2)   {imap.put(v1, v2);}
   
    public int    getOrder()      {return peakOrder;}    
    public byte   getStatus()     {return peakStatus;}    
    public double getSplitRatio() {return splitRatio;}    
    public int    getSplitStrip() {return splitStrip;}
    public Line3D getLine()       {return peakLine;}
    
  
    public String getString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < this.peakStrips.size(); i++){
            if(i!=0); str.append(",");
            str.append(String.format("%.5f",peakStrips.get(i).getEnergy()));
        }
        return str.toString();
    }
    public void setPeakId(int id){
        for(ECStrip strip : peakStrips) strip.setPeakId(id);
    }
    public double getStripEnergy(int strip){ return this.peakStrips.get(strip).getEnergy();}
    
    public double[] getEnergies(){
        double[] data = new double[peakStrips.size()];
        for(int k = 0; k < data.length; k++) data[k] = peakStrips.get(k).getEnergy();
        return data;
    }
    
    public double[] getEnergiesLog(){
        double[] data = new double[peakStrips.size()];
        for(int k = 0; k < data.length; k++) data[k] = Math.log(peakStrips.get(k).getEnergy()*1000);
        return data;
    }
    
    public double getEnergy(){
        double energy = 0.0;
        for(ECStrip strip : peakStrips) energy += strip.getEnergy();
        return energy;
    }
    
    public double getEnergy(Point3D point){
        double energy = 0.0;
        for(ECStrip strip : peakStrips) energy += strip.getEnergy(point);
        return energy;
    }
        
    public double getTime(){
        if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getTime();
        return 0.0;
    }

    public double getTime(Point3D point) {
		if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getTime(point);
		return 0.0;
	}
    
    public double getFTime(){
        if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getFTime();
        return 0.0;
    }
    
    public double getFTime(Point3D point) {
		if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getFTime(point);
		return 0.0;
	}
    
    public double getDTime(){
        if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getDTime();
        return 0.0;
    }
    
    public double getDTime(Point3D point) {
		if(indexMaxStrip >= 0 && indexMaxStrip < peakStrips.size()) return peakStrips.get(indexMaxStrip).getDTime(point);
		return 0.0;
	}   
    
    public DetectorDescriptor getDescriptor(){
        return desc;
    }
    
    public short getDBStatus() {
        return peakStrips.get(indexMaxStrip).getDBStatus();
    }  
    
    public ECStrip getMaxECStrip() {
    	return peakStrips.get(indexMaxStrip);
    }
    
    public int getMaxStrip(){
        return peakStrips.get(indexMaxStrip).getDescriptor().getComponent();
    }
    
    public boolean  addStrip(ECStrip strip){
        for(ECStrip s : peakStrips){
            if(s.isNeighbour(strip)){
                peakStrips.add(strip);
                if(strip.getEnergy()>peakStrips.get(indexMaxStrip).getEnergy()){
                    indexMaxStrip = peakStrips.size()-1;
                    peakLine.copy(strip.getLine());
                }
                return true;
            }
        }
        return false;
    }
    
    public List<ECStrip> getStrips(){
        return peakStrips;
    }  
    
    public int getADC(){
        int adc = 0;
        for(ECStrip s : peakStrips) adc+= s.getADC();
        return adc;
    }
    
    public void redoPeakLine(){
        
        Point3D pointOrigin = new Point3D(0.0,0.0,0.0);
        Point3D pointEnd    = new Point3D(0.0,0.0,0.0);
        peakDistanceEdge = 0.0;
        peakMoment       = 0.0;
        peakMoment2      = 0.0;
        peakMoment3      = 0.0;
        
        double logSumm = 0.0;       
        double peakEnergy = (ECCommon.logParam==1.0)?1.0:getEnergy()*1000; 
        
        for(int i = 0; i < peakStrips.size(); i++){
            Line3D line = peakStrips.get(i).getLine();

            double energyMev = peakStrips.get(i).getEnergy()*1000.0; //raw strip energy, no attenuation correction           
            double le = Math.max(0.,ECCommon.logParam + Math.log(energyMev/peakEnergy)); //logParam==0 used in pass1
            //NaN for energyMeV<=1 MeV le=0 !!!
            peakDistanceEdge += peakStrips.get(i).getDistanceEdge()*le; //used for lu, lv, lw fiducials
            
            pointOrigin.setX(pointOrigin.x()+line.origin().x()*le);
            pointOrigin.setY(pointOrigin.y()+line.origin().y()*le);
            pointOrigin.setZ(pointOrigin.z()+line.origin().z()*le);
            
            pointEnd.setX(pointEnd.x()+line.end().x()*le);
            pointEnd.setY(pointEnd.y()+line.end().y()*le);
            pointEnd.setZ(pointEnd.z()+line.end().z()*le);
            
            logSumm += le;            
        }
        
        
        peakDistanceEdge = peakDistanceEdge/logSumm;
        
        peakLine.set(
                pointOrigin.x()/logSumm,
                pointOrigin.y()/logSumm,
                pointOrigin.z()/logSumm,
                pointEnd.x()/logSumm,
                pointEnd.y()/logSumm,
                pointEnd.z()/logSumm
        );
        
        logSumm = 0.0; // added according to issue 1057
        
        // Shower peak moments
        for(int i = 0; i < peakStrips.size(); i++){            
            double stripDistance = peakStrips.get(i).getDistanceEdge();
            double dist = peakDistanceEdge - stripDistance;
            double energyMev = peakStrips.get(i).getEnergy()*1000.0;
            double energyLog = Math.log(energyMev);
            peakMoment  += dist*dist*dist*dist*energyLog;           
            peakMoment2 += dist*dist*energyLog;
            peakMoment3 += dist*dist*dist*energyLog;
            logSumm += energyLog;
        }
        
        peakMoment = peakMoment/logSumm;
        
        if(peakMoment2<0.0000000001){
            peakMoment2 = 4.5/12.0;
        } else {
            peakMoment2 = peakMoment2/logSumm;
        }
        
        double sigma3    = Math.sqrt(peakMoment2);
        
        peakMoment3 = peakMoment3/logSumm/(sigma3*sigma3*sigma3);
        peakMoment  = peakMoment/logSumm/(sigma3*sigma3*sigma3*sigma3);
    }
    
    public double getDistanceEdge(){
        return peakDistanceEdge;
    }
    
    public double getMoment(){
        return peakMoment;
    }
    
    public double getMoment2(){
        return peakMoment2;
    }
    
    public double getMoment3(){
        return peakMoment3;
    }
    
    public int getMultiplicity(){
        return peakStrips.size();
    }
    
    public int getCoord(){
        double energy_summ = 0.0;
        double energy_norm = 0.0;
        for(ECStrip strip : peakStrips){
	        int str = strip.getDescriptor().getComponent() - 1;
	        str = str*8+4;
            energy_norm += strip.getEnergy()*str;
            energy_summ += strip.getEnergy();
        }        
        return (int) (energy_norm/energy_summ);
    }
    
    double integral(int strip, boolean rl){ //rl=false/true: adc summed for strip indices to the right/left of input strip
        int count = 0, intg = 0;
        setSplitStrip(strip);
        for(int i = (rl)?0:strip+1; i < ((rl)?strip:peakStrips.size());i++) {count++;intg += peakStrips.get(i).getADC();}        
        return ((double) intg) - ((double) splitEnergy)*count;        
    }
    
    public int getSplitIndex(int val) { //For testing of various peak splitter methods
    	switch (val) {
    	case 0: return gg1_getSplitIndex();
    	case 1: return gg2_getSplitIndex();
    	case 2: return new_getSplitIndex();
    	case 3: return MinMax1();
    	case 4: return MinMax2();
    	}
    	return 0;
    }
    
    int gg1_getSplitIndex(){ //split0: Gagik's original splitter used in pass1 (called getSplitStrip in previous releases)
        int     split = -1;
        double  ratio = 0.0;
        
        if(peakStrips.size()>ECCommon.splitThresh[ind[getDescriptor().getLayer()-1]]){
            for(int i = 1; i < peakStrips.size()-1; i++){
                double left  = integral(i, false);
                double right = integral(i, true);
                double lf_ratio = left/right;
                if(ECCommon.debugSplit) {
                	int  s = peakStrips.get(i).getDescriptor().getSector();
                	int il = peakStrips.get(i).getDescriptor().getLayer(); 
                    double oleft  = integral_old(i, false);
                    double oright = integral_old(i, true);
//               	    System.out.println("getSplitStrip: "+s+" "+il+" "+i+" "+left+" "+right+" "+oleft+" "+oright);
                }
                if(left>0.0&&right>0.0&&lf_ratio>ratio){
                    split = i;
                    ratio = lf_ratio;
                }
            }
        }
        return split;
    }
        
    int gg2_getSplitIndex(){ //split1: lcsmith variation implementing splitRules to reject duplicate peaks 
        int     split = -1;
        double  ratio_lo = 0.05, ratio_hi = 1e7;
        int splitStripLast=-100, splitEnergyLast=0;
        
        if(!peakSplit && peakStrips.size()>ECCommon.splitThresh[ind[getDescriptor().getLayer()-1]]){
            for(int i = 1; i < peakStrips.size()-1; i++){
                double right = integral_old(i, false);
                double  left = integral_old(i, true);
                double rl_ratio = right/left;
                if(ECCommon.debugSplit) {
                	int  s = peakStrips.get(i).getDescriptor().getSector();
                	int il = peakStrips.get(i).getDescriptor().getLayer(); 
                	int ip = peakStrips.get(i).getDescriptor().getComponent();
//               	    System.out.println("getSplitIndex: "+s+" "+il+" "+ip+" "+peakStrips.size()+" "+i+" "+right+" "+left+" "+splitEnergy+" "+rl_ratio);
                }
                boolean splitRule0 = right>0.0 && left>0.0;
                boolean splitRule1 = rl_ratio>ratio_lo && rl_ratio<ratio_hi;
                boolean splitRule2 = splitStrip-splitStripLast==1;
                boolean splitRule3 = splitEnergy < splitEnergyLast;
                
                if(splitRule0){
                    int lastsplit = split;
 
                    if(!splitRule2 &&  splitRule1) {split = i; splitStripLast = splitStrip;  splitEnergyLast = splitEnergy;}
                    if( splitRule2 &&  splitRule3) {split = i; splitStripLast = -100;        splitEnergyLast = -100;}
                    if( splitRule2 && !splitRule3) {split = lastsplit; splitStripLast = -100;splitEnergyLast = -100;}
                    ratio_lo = rl_ratio;
                    splitRatio      = ratio_lo;                                     
                }
            }
        }
        return split;
    }
    
    int new_getSplitIndex() { //split2: lcsmith method sorts striplist by energy + dipfinder.  Peaklist split only once.
    	int split = -1;   	
        if(!peakSplit && peakStrips.size()>ECCommon.splitThresh[ind[getDescriptor().getLayer()-1]]){
        	ECCommon.stripSortMethod=ECCommon.splitMethod; 
         	List<ECStrip> sortStrips = new ArrayList<ECStrip>(); sortStrips.addAll(peakStrips); Collections.sort(sortStrips);          	
        	return setSplitStrip(getDipIndex(sortpair(sortStrips,0,1)));
        }    	
    	return split;
    }
        
    int[] sortpair(List<ECStrip> list, int i0, int i1) {
    	int[] out = {0,0};
   	    out[0] = Math.min(list.get(i0).getStripID(),list.get(i1).getStripID());
   	    out[1] = Math.max(list.get(i0).getStripID(),list.get(i1).getStripID());    	   
//	    return out[1]-out[0]==1 ? ((i1-i0)>1?out:sortpair(list,i0+1,i1+1)):out;
	    return out;
    }
    
    public int isGood(){
        int maxStrip = this.getMaxStrip();
        int  nStrips = this.peakStrips.size();
        if(maxStrip==0&&nStrips>2){
            if(this.getStripEnergy(maxStrip+1)>this.getStripEnergy(maxStrip+2)) return 1;
        } 
        
        if(maxStrip==nStrips-1&&nStrips>2){
            if(this.getStripEnergy(maxStrip-1)>this.getStripEnergy(maxStrip-2)) return 1;
        }
        
        if(nStrips>2) return 2;
        
        return 0;
    }
    
    int getDipIndex(int[] in) {
    	int i0=in[0], i1=in[1];
    	switch (i1-i0) {
    	case 0: return   -1;
    	case 1: return   -1;
    	case 2: return i1-1;
    	case 3: return imap.get(i1-1)<imap.get(i0+1)?i1-1:i0+1; //need to check energy asymmetry
    	case 4: return imap.get(i1-1)<imap.get(i0+1)?i1-1:i0+1;
    	case 5: return imap.get(i1-1)<imap.get(i0+1)?i1-2:i0+2;
    	}
    	return -1;		
    }
    
    boolean isMax(List<ECStrip> s, int n, double num, int i, int j) {                
       if (i >= 0 && s.get(i).getEnergy() > num) return false;           
       if (j < n  && s.get(j).getEnergy() > num) return false;
       return true;
    }
  
    boolean isMin(List<ECStrip> s, int n, double num, int i, int j) {     
       if (i >= 0 && s.get(i).getEnergy() < num) return false;     
       if (j < n  && s.get(j).getEnergy() < num) return false;
       return true;
    }
    
    boolean goodX(List<ECStrip> list) { //this should be using peak energy not strip
    	double e1 = list.get(0).getEnergy(), e2 = list.get(1).getEnergy();
    	return (Math.abs((e1-e2)/(e1+e2)))<0.80;
    }
   
    
    int MinMax1() { //finds peak with _- or -_ or  _-_ pattern
        List<ECStrip> mxs = new ArrayList<ECStrip>();
        List<Integer> mns = new ArrayList<Integer>();
        List<ECStrip>  sl = new ArrayList<ECStrip>(); sl.addAll(peakStrips);  
        
        if(!peakSplit && peakStrips.size()>ECCommon.splitThresh[ind[getDescriptor().getLayer()-1]]){
        	
        	for (int i = 0; i < sl.size(); i++) if (isMax(sl, sl.size(), sl.get(i).getEnergy(), i-1, i+1)) mxs.add(sl.get(i)); 
        	for (int i = 0; i < sl.size(); i++) if (isMin(sl, sl.size(), sl.get(i).getEnergy(), i-1, i+1)) mns.add(i); 
        	      
        	ECCommon.stripSortMethod=ECCommon.splitMethod; Collections.sort(mxs); 
        	
        	if(ECCommon.debugSplit) {        	
        		System.out.println(" ");
        		for (ECStrip s : mxs) System.out.println("Max1: "+peakStrips.get(s.getStripID()).toString());
        		for (Integer i : mns) System.out.println("Min1: "+peakStrips.get(i).toString());
        	}
        	
            if(mxs.size()>=2 && mns.size()>0 && goodX(mxs)) {
            	for (Integer i : mns) {
            		if  (mxs.get(0).getStripID()<mxs.get(1).getStripID()) {
            		  if (sl.get(i).getStripID()>mxs.get(0).getStripID()&&
        			      sl.get(i).getStripID()<mxs.get(1).getStripID()) return setSplitStrip(i);
            		}
            		if  (mxs.get(0).getStripID()>mxs.get(1).getStripID()) {
              		  if (sl.get(i).getStripID()<mxs.get(0).getStripID()&&
          			      sl.get(i).getStripID()>mxs.get(1).getStripID()) return setSplitStrip(i);
              		}
            	}
            }
        }        
        return setSplitStrip(-1);                
    } 
    
    
    int MinMax2() { //finds peak with _-_ pattern
        List<Integer> MaxInd = new ArrayList<Integer>();
        List<Integer> MinInd = new ArrayList<Integer>();  	
        List<ECStrip> s = new ArrayList<ECStrip>(); s.addAll(peakStrips);
        if(!peakSplit && peakStrips.size()>ECCommon.splitThresh[ind[getDescriptor().getLayer()-1]]){
            boolean directionUp = peakStrips.get(0).getEnergy() <= peakStrips.get(1).getEnergy(); 
            
        	for (int i = 0; i < s.size()-1; i++) {
        		if(      directionUp && s.get(i+1).getEnergy()<s.get(i).getEnergy()) {MaxInd.add(i); directionUp = false;}
        		else if(!directionUp && s.get(i+1).getEnergy()>s.get(i).getEnergy()) {MinInd.add(i); directionUp = true; }
        	}  
        	
//        	for (Integer i : MaxInd) System.out.println("Max2: "+peakStrips.get(i).toString());
//        	for (Integer i : MinInd) System.out.println("Min2: "+peakStrips.get(i).toString());
        
        	boolean patt0 = MaxInd.size()==2 && MinInd.size()==1;
        	boolean patt1 = MaxInd.size()==2 && MinInd.size()==2;
        	boolean patt2 = MaxInd.size()==2 && MinInd.size()==3;
        	boolean patt3 = MaxInd.size()==1 && MinInd.size()==1;
        
        	boolean cond0 = (patt1||patt2)&& s.get(MinInd.get(0)).getStripID()>s.get(MaxInd.get(0)).getStripID()&&
        			                         s.get(MinInd.get(0)).getStripID()<s.get(MaxInd.get(1)).getStripID();
        
        	boolean cond1 = (patt1||patt2)&& s.get(MinInd.get(1)).getStripID()>s.get(MaxInd.get(0)).getStripID()&&
		                                     s.get(MinInd.get(1)).getStripID()<s.get(MaxInd.get(1)).getStripID();
        
        	boolean cond2 =         patt2 && s.get(MinInd.get(2)).getStripID()>s.get(MaxInd.get(0)).getStripID()&&
                                             s.get(MinInd.get(2)).getStripID()<s.get(MaxInd.get(1)).getStripID();
       
        	if(patt0) return setSplitStrip(MinInd.get(0));
        	if(cond0) return setSplitStrip(MinInd.get(0));
        	if(cond1) return setSplitStrip(MinInd.get(1));
        	if(cond2) return setSplitStrip(MinInd.get(2));
        	if(patt3) return setSplitStrip(-1);        	
        }   
        return setSplitStrip(-1);                
    }
    
    int setSplitStrip(int strip) {
    	if(strip==-1) return -1;
        splitEnergy = peakStrips.get(strip).getADC();
        splitStrip  = peakStrips.get(strip).getDescriptor().getComponent();
    	return strip;
    }
    
    double integral_old(int strip, boolean right){
        int count = 0;
        int intg  = 0;
        setSplitStrip(strip);
        if(!right){
            for(int i = strip + 1; i < peakStrips.size(); i++){
                count++;
                intg += peakStrips.get(i).getADC();
            }
            
        } else {
            for(int i = strip - 1; i >=0; i--){
                count++;
                intg += peakStrips.get(i).getADC();
            }
        }
        
        double norm = ((double) intg) - ((double) splitEnergy)*count; 
        
        if(ECCommon.debugSplit) System.out.println(right+" "+count+" "+intg+" "+splitEnergy+" "+norm);

        return norm;
    }

    public List<ECPeak>  splitPeak(int strip){     
        List<ECPeak>  twoPeaks = new ArrayList<ECPeak>();
        ECPeak  leftPeak = new ECPeak(peakStrips.get(0)); leftPeak.setSplitRatio(splitRatio); leftPeak.setPeakSplit(true);
        for(int i = 1; i < strip; i++) { leftPeak.addStrip(peakStrips.get(i));}
        ECPeak  rightPeak = new ECPeak(peakStrips.get(strip)); rightPeak.setSplitRatio(splitRatio); rightPeak.setPeakSplit(true);
        for(int i = strip+1; i < peakStrips.size(); i++) { rightPeak.addStrip(peakStrips.get(i));}
        twoPeaks.add(leftPeak);
        twoPeaks.add(rightPeak);
        return twoPeaks;
    }
    
    public int compareTo(Object o) {
        ECPeak ob = (ECPeak) o;
        if(ob.getDescriptor().getSector()     < desc.getSector())    return  1;
        if(ob.getDescriptor().getSector()     > desc.getSector())    return -1;
        if(ob.getDescriptor().getLayer()      < desc.getLayer())     return  1;
        if(ob.getDescriptor().getLayer()      > desc.getLayer())     return -1;
        if(ob.getADC()                        > getADC())            return  1;
        if(ob.getADC()                        < getADC())            return -1;
        return -1;
    }      
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> peak  ( %3d %3d) STATUS=%1d ENERGY=%6.4f TIME=%6.2f FTIME=%6.2f\n", 
                desc.getSector(),desc.getLayer(), getStatus(), getEnergy(), getTime(), getFTime() ));
        str.append(peakLine.toString());
        str.append("\n");
        for(ECStrip strip : peakStrips){
            str.append("\t\t");
            str.append(strip.toString());
            str.append("\n");
        }
        
        return str.toString();
    }
    
}
