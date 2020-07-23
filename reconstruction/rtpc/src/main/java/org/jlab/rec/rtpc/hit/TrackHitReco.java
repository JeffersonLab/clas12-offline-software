//Author: David Payette and Nate Dzbenski

/* This code takes the time-reduced tracks produced by the Time Average, as well as the original hits 
 * and uses a fit formula from garfield++ to calculate the hit's position in the drift region
 * based on the time of the signal. We use the original hits to see how well the formula performs 
 * when we include factors such as time shifts and non-uniform magnetic fields
 */

package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TrackHitReco {
    
    //MAGBOLTZ PARAMETERS DO NOT TOUCH
    /*final private double a_t1 = -2.48491E-4;
    final private double a_t2 = 2.21413E-4;
    final private double a_t3 = -3.11195E-3;
    final private double a_t4 = -2.75206E-1;
    final private double a_t5 = 1.74281E3;
    private double a_t;

    final private double b_t1 = 2.48873E-5;
    final private double b_t2 = -1.19976E-4;
    final private double b_t3 = -3.75962E-3;
    final private double b_t4 = 5.33100E-2;
    final private double b_t5 = -1.25647E2;
    private double b_t;
    
    final private double a_phi1 = -3.32718E-8;
    final private double a_phi2 = 1.92110E-7;
    final private double a_phi3 = 2.16919E-6;
    final private double a_phi4 = -8.10207E-5;
    final private double a_phi5 = 1.68481E-1;
    private double a_phi;
    
    final private double b_phi1 = -3.23019E-9;
    final private double b_phi2 = -6.92075E-8;
    final private double b_phi3 = 1.24731E-5;
    final private double b_phi4 = 2.57684E-5;
    final private double b_phi5 = 2.10680E-2;
    private double b_phi;

    final private double t_2GEM2 = 296.082;
    final private double t_2GEM3 = 296.131;
    final private double t_2PAD = 399.09;
    final private double t_gap = t_2GEM2 + t_2GEM3 + t_2PAD;

    final private double phi_2GEM2 = 0.0492538;
    final private double phi_2GEM3 = 0.0470817;
    final private double phi_2PAD = 0.0612122;
    final private double phi_gap = phi_2GEM2 + phi_2GEM3 + phi_2PAD;
    */
    
    private double larget;
    private double smallt;
    private HitVector smallthit;
    private HitVector largethit;
    private PadVector smalltpadvec;
    private PadVector largetpadvec;
    private double tcathode;
    private double tdiffshort;
    private double tdifflong;
    private double tdiff;
    private double Time;
    private int cellID;

    private double drifttime;
    private double r_rec;
    private double dphi;
    private double phi_rec;
    private double x_rec;
    private double y_rec;
    
    private double[] a_t = new double[5];
    private double at = 0;
    private double[] b_t = new double[5];
    private double bt = 0;
    private double[] a_phi = new double[5];
    private double aphi = 0;
    private double bphi = 0;
    private double[] b_phi = new double[5];
    private double[] c_phi = new double[5];
    private double[] c_t = new double[5];
    private double ct = 0;
    private double cphi = 0;
    private double tl = 0;
    private double tp = 0;
    private double tr = 0;
    private double tshiftfactorshort = 1;
    private double tshiftfactorlong = 0;
    private int smalltpad;
    private int largetpad;
    private int minhitcount = 5;
    
    private boolean _cosmic = false;
    
    public TrackHitReco(HitParameters params, List<Hit> rawHits, boolean cosmic, double magfield){
        
        _cosmic = cosmic;
        a_t = params.get_atparms();
        b_t = params.get_btparms();
        a_phi = params.get_aphiparms();
        b_phi = params.get_bphiparms();
        c_phi = params.get_cphiparms();
        c_t = params.get_ctparms();
        tl = params.get_tl();
        tp = params.get_tp();
        tr = params.get_tr();
        tcathode = params.get_tcathode();
        tshiftfactorshort = params.get_tshiftfactorshort();
        tshiftfactorlong = params.get_tshiftfactorlong();
        minhitcount = params.get_minhitspertrackreco();
        
        HashMap<Integer, List<RecoHitVector>> recotrackmap = new HashMap<>();
        ReducedTrackMap RTIDMap = params.get_rtrackmap();
        List<Integer> tids = RTIDMap.getAllTrackIDs();

        for(int TID : tids) {
            double adc = 0;
            ReducedTrack track = RTIDMap.getTrack(TID);
            List<HitVector> allhits = track.getAllHits();
            if(allhits.size() < minhitcount) continue;
            track.sortHits();
            smallthit = track.getSmallTHit();
            largethit = track.getLargeTHit();
            smallt = smallthit.time();
            larget = largethit.time();
            largetpad = largethit.pad();
            tdiffshort = tcathode - smallt;
            tdifflong = tcathode - larget;
            tdiffshort *= tshiftfactorshort;
            tdifflong *= tshiftfactorlong;
            tdiff = tdiffshort + tdifflong;
            recotrackmap.put(TID, new ArrayList<>());

            for(HitVector hit : allhits) {
                adc += hit.adc();
                cellID = hit.pad();              
                Time = hit.time();

                if(!cosmic){
                    Time += tdiff;
                }
		           
                // find reconstructed position of ionization from Time info		                
                drifttime = Time;
                r_rec = get_r_rec(hit.z(),drifttime); //in mm
                dphi = get_dphi(hit.z(),r_rec,magfield); // in rad
                phi_rec=hit.phi()-dphi;
                if(cosmic) phi_rec = hit.phi();
                
                if(phi_rec<-Math.PI) {
                    phi_rec+=2.0*Math.PI;
                }
                if(phi_rec>Math.PI){
                    phi_rec-=2.0*Math.PI;
                }

                // x,y,z pos of reconstructed track
                x_rec=r_rec*(Math.cos(phi_rec));
                y_rec=r_rec*(Math.sin(phi_rec));
                if(!Double.isNaN(x_rec) && !Double.isNaN(y_rec) && !Double.isNaN(hit.z())){
                    recotrackmap.get(TID).add(new RecoHitVector(cellID,x_rec,y_rec,hit.z(),r_rec,phi_rec,tdiff,Time,hit.adc(),smallthit,largethit));
                }
            }
        }
 
        params.set_recotrackmap(recotrackmap);
    }	
	
    private double get_rec_coef(double[] parms, double z2) {
        double z = z2/10;
        return parms[4]*z*z*z*z + parms[3]*z*z*z + parms[2]*z*z + parms[1]*z + parms[0];
    }

    private double get_r_rec(double z,double t){
        at = get_rec_coef(a_t,z) + tl + tp + tr;
        bt = get_rec_coef(b_t,z);
        ct = 100*get_rec_coef(c_t,z);
        double x = (t-at)/bt;
        double rmax = 70;
        double rmin = 30;
        return Math.sqrt(rmax*rmax*(1-x) + rmin*rmin*x + ct*(1-x)*x);
    }
    
    private double get_dphi(double z, double r, double magfield){
        aphi = get_rec_coef(a_phi,z);
        bphi = get_rec_coef(b_phi,z);
        cphi = get_rec_coef(c_phi,z);
        //return aphi*(7-r/10)+bphi*(7-r/10)*(7-r/10) + phigap; // in rad
        return aphi + bphi * Math.log(70/r) + 100*cphi*((1/(r*r))-(1/(70*70)));
    }
    

    
}




