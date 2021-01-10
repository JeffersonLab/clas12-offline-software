package org.jlab.rec.rtpc.hit;

public class TrackUtils {
    private double adjthresh = 16; 				
    private double PhiDelta = 16; 
    private double ZDelta = 36; 
    //private double phithresh = 0.16;
    //private double zthresh = 16;
    private double zthresh = 200;
    private double phithresh = 7;
    private double zthreshgap = 20;
    private double phithreshgap = 0.20;

    public TrackUtils() {}

    public boolean comparePads(PadVector p1, PadVector p2, String Method, boolean cosmic, double dz, double dzgap, double dphi, double dphigap) {
        if(cosmic){
            zthresh = 200;
            phithresh = 7;  
            zthreshgap = zthresh;
            phithreshgap = phithresh;
        }else{
            zthresh = dz;
            phithresh = dphi;
            zthreshgap = dzgap;
            phithreshgap = dphigap;
        }
        if(Method == "ellipse") {return ellipseMethod(p1, p2);}
        if(Method == "phiz")    {return phizMethod(p1,p2);}
        else return false;
    }	

    private boolean ellipseMethod(PadVector p1, PadVector p2) {
        double p1x = p1.x(); 
        double p1y = p1.y();
        double p1z = p1.z();
        double p2x = p2.x(); 
        double p2y = p2.y();
        double p2z = p2.z();
        double EllipseDeltax = Math.abs(p1x-p2x)*Math.abs(p1x-p2x);
        double EllipseDeltay = Math.abs(p1y-p2y)*Math.abs(p1y-p2y);
        double EllipseDeltaz = Math.abs(p1z-p2z)*Math.abs(p1z-p2z);
        double EllipseTotal = ((EllipseDeltax+EllipseDeltay)/PhiDelta) + (EllipseDeltaz/ZDelta);
        if(EllipseTotal < adjthresh) return true;
        return false;
    }

    private boolean phizMethod(PadVector p1, PadVector p2) {
        double p1phi = p1.phi();
        double p2phi = p2.phi();
        double p1z = p1.z();
        double p2z = p2.z();
        double phidiff = Math.abs(p2phi-p1phi);
        double zdiff = Math.abs(p1z-p2z);
        //System.out.println(p1phi + " " + p2phi + " " + (p1phi - p2phi));
        if(phidiff < Math.PI){
            return phidiff < phithresh && zdiff < zthresh;
        }else{
            return Math.abs(phidiff-2*Math.PI) < phithreshgap && zdiff < zthreshgap;
        }
        
    }

}
