package org.jlab.service.ec;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */

public class ECStrip implements Comparable {
    
	/*This is the time to consider two nearby strips (same detector view!) in time. 
	 * There's no correction (yet) for hit position along the bar
	 * However, two bars are near-by and, if the hit is really a matching hit, it is basically in the same position
	 * in the two bars. Hence, the time difference should be very small
	 */
	
	private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.ECAL);
    
    private int                iADC = 0;
    private int                iTDC = 0;
    private float             iTADC = 0;
    private double            iGain = 1.0;
    private double     iADC_to_MEV  = 1.0/10000.0;
    private double          iAttenA = 1.0;
    private double          iAttenB = 50000.0;
    private double          iAttenC = 0.0;
    private double          iAttenD = 40.0;
    private double          iAttenE = 400.0;
    private double    iTimA0=0,iTimA1=0,iTimA2=0,iTimA3=0,iTimA4=0; //pass1
    private double    fTim00,iTim00 = 0; // Global TDC offset
    private double    fTimA0,dTimA0 = 0; // Offset in ns (before applying a1)
    private double    fTimA1,dTimA1 = 1; // ns -> TDC conv. factor (TDC = ns/a1)
    private double    fTimA2,dTimA2 = 0; 
    private double    fTimA3,dTimA3 = 0; 
    private double    fTimA4,dTimA4 = 0; 
    private double    fTimA5,dTimA5 = 0; 
    private double    fTimA6,dTimA6 = 0; 
    private double           dTimA7 = 0;
    private double           dTimA8 = 0;
    private int        triggerPhase = 0;
    private double            dveff = 18.1; // Effective velocity of scintillator light (cm/ns) using DISC/TDC timing (pass2)
    private double            fveff = 18.1; // Effective velocity of scintillator light (cm/ns) using FADC timing (pass2)
    private double             veff = 18.1; // pass1
    private int                  id = -1;   // ID (row number) of the corresponding hit in the ADC bank for truth matching
    private int             stripId = -1;   // Id (row number) of the peak striplist that this hit belongs to
    private int              peakID = -1;   // ID of peak this hit belongs to (all strips belonging to a peak have this same number)
    private int           clusterId = -1;   // Id (row number) of the cluster that this hit belongs to
    
    private short            status = 0;
	
    private double            tdist = 0;
    private double            edist = 0;
    
    private Line3D         stripLine = new Line3D();
    private double stripDistanceEdge = 0.0;
    
    private static double  coincTIME = 25.; //ns. 	
    private double                   time = 0;
    private double              fgtw,dgtw = 0; //global time walk correction
    
    private EnergyCorrection          ecc = null;
    private TimeCorrection     tc,ftc,dtc = null; 
    
    public ECStrip(int sector, int layer, int component){
        desc.setSectorLayerComponent(sector, layer, component);
        ecc = new corrEnergy(); 
        ftc = ECCommon.usePass2Timing ? new ExtendedTWCFTime() : new ExtendedTWCTime(); //FADC timing pass2:pass1
        dtc = ECCommon.usePass2Timing ? new ExtendedTWCDTime() : new ExtendedTWCTime(); //TDC  timing pass2:pass1
        tc  = ECCommon.useFADCTime ? ftc : dtc; //user selected for FADC:TDC timing calibration 
    }
    
    abstract class EnergyCorrection {
        public abstract double getRawEnergy(); 
        public abstract double getEcorr(double dist);
        public abstract double getEnergy(Point3D point);
    }
    
    abstract class TimeCorrection {
        public abstract double getRawTime();
        public abstract double getPhaseCorrectedTime();
        public abstract double getTWCTime();    	
        public abstract double getTime();
    }
        
    public class corrEnergy extends EnergyCorrection {    	
        public double getRawEnergy() {
    	    return iADC*iGain*iADC_to_MEV;
        }

        public double getEcorr(double dist) {
            return iAttenA*(Math.exp(-dist/iAttenB)+iAttenD*Math.exp(-dist/iAttenE)) + iAttenC;   	
        } 
        
        public double getEnergy(Point3D point) {
            edist = point.distance(stripLine.end());
            return getRawEnergy()/getEcorr(edist);
        }
    }       

    public class SimpleTWCTime extends TimeCorrection {    	
        public double getRawTime(){
           	return iTDC * iTimA1;
        }
        
        public double getPhaseCorrectedTime() { 
            return iTDC * iTimA1 - triggerPhase;
        } 
         	
        public double getTWCTime() {
        	double radc = Math.sqrt(iADC);
          	return  getPhaseCorrectedTime() - iTimA2/radc;
        }  
        
    	public double getTime() {
    		return getTWCTime() - iTimA0;
    	}
    }
    
    public class ExtendedTWCTime extends TimeCorrection {  //pass 1 TWC  	
        public double getRawTime(){
           	return iTDC * iTimA1;
        }
        
        public double getPhaseCorrectedTime() { 
            return iTDC * iTimA1 - triggerPhase;
        } 
        
        public double getExtendedTWC(double x) {
        	return iTimA2/x + iTimA3 + iTimA4/Math.sqrt(x);
        }
        
        public boolean test() {
        	return true;
        }        
        
        public double getTWCTime() {
        	double radc = Math.sqrt(iADC); 
          	return getPhaseCorrectedTime() - dgtw/radc - getExtendedTWC(radc) - iTim00;          	
        } 
        
    	public double getTime() {    		
          	return getTWCTime() - iTimA0;          	
        }  
    } 
    
    public class ExtendedTWCDTime extends TimeCorrection {   //pass 2 TWC DSC/TDC 
        public double getRawTime(){
           	return iTDC * dTimA1;
        }
        
        public double getPhaseCorrectedTime() { 
            return iTDC * dTimA1 - triggerPhase;
        } 
        
        public double getExtendedTWC(double x) {
        	if(dTimA4==0 || dTimA6==0 || dTimA7==0) return 0;
            return  dTimA2+Math.exp(-(x-dTimA3)/dTimA4)+1-Math.exp(-(dTimA5-x)/dTimA6)-Math.exp(-(x-dTimA3*0.95)/dTimA7)*Math.pow(x,dTimA8);
        }
               
        public double getTWCTime() {
        	double radc = Math.sqrt(iADC);
          	return getPhaseCorrectedTime() - dgtw/radc - getExtendedTWC(radc) - iTim00;          	
        } 
        
    	public double getTime() {
          	return getTWCTime() - dTimA0;          	
        }  
    }
    
    public class ExtendedTWCFTime extends TimeCorrection {  //pass 2 TWC FADC
        public double getRawTime(){
           	return iTADC;
        }
        
        public double getPhaseCorrectedTime() {         	
            return iTADC;  
        } 
        
        public double getExtendedTWC(double x) {
        	if(fTimA4==0 || fTimA6==0) return 0;
        	return fTimA2 + Math.exp(-(x-fTimA3)/fTimA4)+1-Math.exp( (x-fTimA5)/fTimA6);
        }
        
    	public double getTWCTime() {
        	double radc = Math.sqrt(iADC);
          	return getRawTime() - fgtw/radc - getExtendedTWC(radc) - fTim00;          	
    	} 
    	
    	public double getTime() {
          	return getTWCTime() - fTimA0;    
    	}	
    }     
	
    public DetectorDescriptor getDescriptor(){
    	return desc;
    }
    
    public void setStatus(int val) {
    	status = (short) val;
    }
    
    public short getDBStatus() {
    	return (short) (desc.getComponent()*10 + status);
    } 
    
    public ECStrip setADC(int adc){
        iADC = adc;
        return this;
    }
    
    public ECStrip setTDC(int tdc){ // DSC/TDC timing
        iTDC = tdc;
        return this;
    }
    
    public ECStrip setTADC(float tdc) { // FADC timing
    	iTADC = tdc;
    	return this;
    }
	
    public int getADC(){
        return iADC;
    }

    public int getTDC(){
        return ECCommon.useFADCTime ? (int) (iTADC/iTimA1) : iTDC;
    }
    
    public double getRawTime(){
       	return tc.getRawTime();
    }
    
    public double getPhaseCorrectedTime() { 
        return tc.getPhaseCorrectedTime();
    }
    
    public double getRawTime(boolean phaseCorrection) {
 	    return phaseCorrection ? getPhaseCorrectedTime():getRawTime();
    }

    public double getTWCTime() {
    	return tc.getTWCTime();    	
    }
    
    public boolean useFT() {
    	boolean test1 = ECCommon.useFADCTime;
    	boolean test2 = ECCommon.useFTpcal && desc.getLayer()==1;
    	boolean test3 = ECCommon.useDTCorrections && getDTime()<=0;
    	return test1 || test2 || test3;
    }
    
    public double getTime() {
        return (useFT() ? getFTime():getDTime());    	
    }
    
    public double getDTime() {
    	return dtc.getTime();
    }
    
    public double getFTime() {
    	return ftc.getTime();
    }
	             
    public double getEnergy(){
        return ecc.getRawEnergy();
    }
    
    public void setDistanceEdge(double val){
        stripDistanceEdge = val;
    }
    
    public double getDistanceEdge(){
        return stripDistanceEdge;
    }
  
    public void setPeakId(int val){ 
    	peakID = val;
    }
    
    public int getPeakId(){
      	return peakID;      	
    }

    public void setID(int val){
        id = val;    
    }

    public int getID(){
        return id;
    }

    public void setStripID(int val){
        stripId = val;    
    }

    public int getStripID(){
        return stripId;
    }
    
    public void setClusterId(int val){
        clusterId = val;
    }

    public int getClusterId(){
        return clusterId;
    }
    
    public void setAttenuation(double a, double b, double c, double d, double e){
        iAttenA = a;
        iAttenB = b;
        iAttenC = c;
        iAttenD = d;
        iAttenE = e;
    }
    
    public void setTriggerPhase(int val) {
        triggerPhase = val;
    } 
    
    public void setVeff(double val) {
        veff = val;
    }

    public void setDVeff(double val) {
        dveff = val;
    } 
    
    public void setFVeff(double val) {
        fveff = val;
    }
    
    public double getDVeff() {
       return dveff;
    } 
    
    public double getFVeff( ) {
       return fveff;
    } 
    
    public double getVeff() {
        return (useFT() ? fveff : (ECCommon.usePass2Timing ? dveff:veff));
    }
           
    public void setGain(double val){
        iGain = val;
    }
    
    public void setITime(double a0, double a1, double a2, double a3, double a4) {
        iTimA0 = a0;
        iTimA1 = a1;
        iTimA2 = a2;
        iTimA3 = a3;
        iTimA4 = a4;
    }
    
    public void setDTime(double a0, double a1, double a2, double a3, double a4, double a5, double a6, double a7, double a8) {
        dTimA0 = a0;
        dTimA1 = a1;
        dTimA2 = a2;
        dTimA3 = a3;
        dTimA4 = a4;
        dTimA5 = a5;
        dTimA6 = a6;
        dTimA7 = a7;
        dTimA8 = a8;
    }
    
    public void setFTime(double a0, double a1, double a2, double a3, double a4, double a5, double a6) {
    	fTimA0 = a0;
    	fTimA1 = a1;
    	fTimA2 = a2;
    	fTimA3 = a3;
    	fTimA4 = a4;
    	fTimA5 = a5;
    	fTimA6 = a6;
    }    
    
    public void setDtimeGlobalTimeWalk(double val) {
    	dgtw = val;
    }
    
    public void setFtimeGlobalTimeWalk(double val) {
    	fgtw = val;
    }  
	
    public void setDtimeGlobalTimingOffset(double val) {
        iTim00 = val;
    }
    
    public void setFtimeGlobalTimingOffset(double val) {
        fTim00 = val;
    }
    
    public double[] getITiming() {
        double[] array = new double[5];
        array[0] = iTimA0;
        array[1] = iTimA1;
        array[2] = iTimA2;
        array[3] = iTimA3;
        array[4] = iTimA4;
        return array;    
    }
    
    public double[] getDTiming() {
        double[] array = new double[9];
        array[0] = dTimA0;
        array[1] = dTimA1;
        array[2] = dTimA2;
        array[3] = dTimA3;
        array[4] = dTimA4;
        array[5] = dTimA5;
        array[6] = dTimA6;
        array[7] = dTimA7;
        array[8] = dTimA8;
        return array;    
    } 
    
    public double[] getFTiming() {
        double[] array = new double[7];
        array[0] = fTimA0;
        array[1] = fTimA1;
        array[2] = fTimA2;
        array[3] = fTimA3;
        array[4] = fTimA4;
        array[5] = fTimA5;
        array[6] = fTimA6;
        return array;    
    }	
    
    public double getEnergy(Point3D point){
    	return ecc.getEnergy(point);
    }
    
    public double getEcorr(double dist) {
        return ecc.getEcorr(dist);    	
    }
    
    public double getTime(Point3D point) { 		
        tdist = point.distance(stripLine.end());
        time =  getTime() - tdist/getVeff();
        return time;
    } 
    
    public double getFTime(Point3D point) {		
        tdist = point.distance(stripLine.end());
        time =  getFTime() - tdist/getFVeff();
        return time;
    }
    
    public double getDTime(Point3D point) {		
        tdist = point.distance(stripLine.end());
        time =  getDTime() - tdist/getDVeff();
        return time;
    } 
    
    public double getPointTime() {return time;}
	
    public double getEdist() {return edist;}
	
    public double getTdist() {return tdist;}
    
    public Line3D getLine()  {return stripLine;}
    
    public boolean isNeighbour(ECStrip strip){
        if(strip.getDescriptor().getSector() == desc.getSector() &&
           strip.getDescriptor().getLayer()  == desc.getLayer()){
           if(Math.abs(strip.getDescriptor().getComponent()-desc.getComponent())<=ECCommon.touchID) return true;
        }
        return false;
    }
    
    public boolean isInTime(ECStrip strip) {
        if (Math.abs(getTime() - strip.getTime()) < ECStrip.coincTIME) return true;
        return false;
    } 
    
    public int compareTo(Object o) {
        ECStrip ob = (ECStrip) o;
        if (ECCommon.stripSortMethod==0) {
            if(ob.getDescriptor().getSector()     < desc.getSector())    return  1;
            if(ob.getDescriptor().getSector()     > desc.getSector())    return -1;
            if(ob.getDescriptor().getLayer()      < desc.getLayer())     return  1;
            if(ob.getDescriptor().getLayer()      > desc.getLayer())     return -1;
            if(ob.getDescriptor().getComponent() <  desc.getComponent()) return  1;
            if(ob.getDescriptor().getComponent() == desc.getComponent()) return  0;
        } else {
        	if(ob.getEnergy()                       > getEnergy())            return  1;
        	if(ob.getEnergy()                       < getEnergy())            return -1;
        }
        return -1;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> strip (%3d %3d %3d) ADC/TDC/FTDC  %5d %5d %5.1f  ENERGY=%6.4f TIME=%6.2f DTIME=%6.2f FTIME=%6.2f FDIST=%6.2f S/P/C=(%2d %2d %2d)", 
                desc.getSector(),desc.getLayer(),desc.getComponent(),
                iADC,iTDC,iTADC,getEnergy(),getTime(),getDTime(),getFTime(),getTdist(),stripId,peakID,clusterId));
        str.append(String.format("  GAIN (%5.3f) ATT (%5.1f)", 
                iGain,iAttenB));
        return str.toString();
    }
}
