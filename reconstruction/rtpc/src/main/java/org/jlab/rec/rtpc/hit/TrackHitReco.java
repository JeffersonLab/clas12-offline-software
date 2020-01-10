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
    final private double a_t1 = -2.48491E-4;
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
    
    
    private double larget;
    private double smallt;
    private double tdiff;
    private double Time;
    private int cellID;

    private double drifttime;
    private double r_rec;
    private double dphi;
    private double phi_rec;
    private double x_rec;
    private double y_rec;
    
    public TrackHitReco(HitParameters params) {

        HashMap<Integer, Double> tdiffmap = new HashMap<>();
        HashMap<Integer, List<RecoHitVector>> recotrackmap = new HashMap<>();
        ReducedTrackMap RTIDMap = params.get_rtrackmap();
        List<Integer> tids = RTIDMap.getAllTrackIDs();
        
        for(int TID : tids) {
            ReducedTrack track = RTIDMap.getTrack(TID);
            smallt = track.getSmallT();
            larget = track.getLargeT();
            tdiff = 6000 - larget;
            tdiffmap.put(TID, tdiff);
            recotrackmap.put(TID, new ArrayList<>());
            List<HitVector> allhits = track.getAllHits();
            
            for(HitVector hit : allhits) {			
                cellID = hit.pad();              
                Time = hit.time();
                Time += tdiff;
		           
                // find reconstructed position of ionization from Time info		                
                drifttime = Time-t_gap;
                r_rec = get_r_rec(hit.z(),drifttime); //in mm
                dphi = get_dphi(hit.z(),r_rec); // in rad
                
                phi_rec=hit.phi()-dphi-phi_gap;
                
                if(phi_rec<0.0) {
                    phi_rec+=2.0*Math.PI;
                }
                if(phi_rec>2.0*Math.PI){
                    phi_rec-=2.0*Math.PI;
                }

                // x,y,z pos of reconstructed track
                x_rec=r_rec*(Math.cos(phi_rec));
                y_rec=r_rec*(Math.sin(phi_rec));
                recotrackmap.get(TID).add(new RecoHitVector(cellID,x_rec,y_rec,hit.z(),tdiff,Time));
            }
        }
        
        params.set_recotrackmap(recotrackmap);
    }	
	
    private double get_rec_coef(double t1, double t2, double t3, double t4, double t5, double z2) {
        double z = z2/1000;
        return t1*z*z*z*z + t2*z*z*z + t3*z*z + t4*z + t5;
    }

    private double get_r_rec(double z,double t){
        a_t = get_rec_coef(a_t1,a_t2,a_t3,a_t4,a_t5,z);
        b_t = get_rec_coef(b_t1,b_t2,b_t3,b_t4,b_t5,z);
	return ((-(Math.sqrt(a_t*a_t+(4*b_t*t)))+a_t+(14*b_t))/(2*b_t))*10.0;
    }
    
    private double get_dphi(double z, double r){
        a_phi = get_rec_coef(a_phi1,a_phi2,a_phi3,a_phi4,a_phi5,z);
        b_phi = get_rec_coef(b_phi1,b_phi2,b_phi3,b_phi4,b_phi5,z);
        return a_phi*(7-r/10)+b_phi*(7-r/10)*(7-r/10); // in rad
    }
    
    
}




