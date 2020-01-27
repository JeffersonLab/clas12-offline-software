//Author: David Payette and Nate Dzbenski

/* This code takes the time-reduced tracks produced by the Time Average, as well as the original hits 
 * and uses a fit formula from garfield++ to calculate the hit's position in the drift region
 * based on the time of the signal. We use the original hits to see how well the formula performs 
 * when we include factors such as time shifts and non-uniform magnetic fields
 */

package org.jlab.rec.rtpc.hit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;

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
    private double b_phi;*/
    private double a_t;
    private double b_t;
    private double a_phi;
    private double b_phi;
    
    //COSMIC MAGBOLTZ PARAMETERS
    final private double a_t1 = 0;//2.29627e-05;
    final private double a_t2 = 0;//-3.93146e-05;
    final private double a_t3 = 0;//-5.28600e-03;
    final private double a_t4 = 0;//2.78240e-02;
    final private double a_t5 = 6.96387e+02;//6.96145e+02;
    
    final private double b_t1 = 0;//-7.35833e-06;
    final private double b_t2 = 0;//6.81331e-05;
    final private double b_t3 = 0;//1.53858e-03;
    final private double b_t4 = 0;//-2.31373e-02;
    final private double b_t5 = -4.73759e+01;//-4.74214e+01;
    
    final private double a_phi1 = 0;//1.12871e-08;
    final private double a_phi2 = 0;//1.50882e-08;
    final private double a_phi3 = 0;//-3.83492e-06;
    final private double a_phi4 = 0;//-3.43608e-06;
    final private double a_phi5 = 0;//2.16630e-05;
    
    final private double b_phi1 = 0;//-5.03125e-09;
    final private double b_phi2 = 0;//-4.94344e-09;
    final private double b_phi3 = 0;//1.77409e-06;
    final private double b_phi4 = 0;//2.73528e-06;
    final private double b_phi5 = 0;//-3.45254e-05;
    
    final private double t_2GEM2 = 296.082;
    final private double t_2GEM3 = 296.131;
    final private double t_2PAD = 399.09;
    final private double t_gap = 500;//t_2GEM2 + t_2GEM3 + t_2PAD;

    final private double phi_2GEM2 = 0.0492538;
    final private double phi_2GEM3 = 0.0470817;
    final private double phi_2PAD = 0.0612122;
    final private double phi_gap = 0;//phi_2GEM2 + phi_2GEM3 + phi_2PAD;
    
    
    private double larget;
    private double smallt;
    private double tcathode;
    private double tdiff;
    private double Time;
    private int cellID;

    private double drifttime;
    private double r_rec;
    private double dphi;
    private double phi_rec;
    private double x_rec;
    private double y_rec;
   
    private boolean cosmic = true;
    
    public TrackHitReco(HitParameters params, List<Hit> rawHits) {

        HashMap<Integer, Double> tdiffmap = new HashMap<>();
        HashMap<Integer, List<RecoHitVector>> recotrackmap = new HashMap<>();
        ReducedTrackMap RTIDMap = params.get_rtrackmap();
        List<Integer> tids = RTIDMap.getAllTrackIDs();


        

        for(int TID : tids) {
            double adc = 0;
            ReducedTrack track = RTIDMap.getTrack(TID);
            //System.out.println(track.getAllHits().size() + " number of hits");
            track.sortHits();
            smallt = track.getSmallT();
            larget = track.getLargeT();
            
            
            //tdiff = 6000 - larget;
            
            if(cosmic) tcathode = 2000;         
            else tcathode = 6000;
            tdiff = tcathode - larget;

            tdiffmap.put(TID, tdiff);
            recotrackmap.put(TID, new ArrayList<>());
            List<HitVector> allhits = track.getAllHits();

            for(HitVector hit : allhits) {
                adc += hit.adc();
                cellID = hit.pad();              
                Time = hit.time();

                //System.out.println("Track Reco " + Time);
                //Time += tdiff;
		           
                // find reconstructed position of ionization from Time info		                
                drifttime = Time-t_gap;
                r_rec = get_r_rec(hit.z(),Time,tcathode,1800); //in mm
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
            //write.write(adc + "\r\n");
        }
 
        params.set_recotrackmap(recotrackmap);
    }	
	
    private double get_rec_coef(double t1, double t2, double t3, double t4, double t5, double z2) {
        double z = 0;//z2/1000;
        return t1*z*z*z*z + t2*z*z*z + t3*z*z + t4*z + t5;
    }

    private double get_r_rec(double z,double t, double t_cathode, double t_max){
        a_t = get_rec_coef(a_t1,a_t2,a_t3,a_t4,a_t5,z);
        b_t = get_rec_coef(b_t1,b_t2,b_t3,b_t4,b_t5,z);
	//return ((-(Math.sqrt(a_t*a_t+(4*b_t*t*t_cathode/t_max)))+a_t+(14*b_t))/(2*b_t))*10.0;
        return Math.sqrt((70*70*(1-((t-t_cathode)/t_max)))+(30*30*((t-t_cathode)/t_max)));// - 40;
    }
    
    private double get_dphi(double z, double r){
        a_phi = get_rec_coef(a_phi1,a_phi2,a_phi3,a_phi4,a_phi5,z);
        b_phi = get_rec_coef(b_phi1,b_phi2,b_phi3,b_phi4,b_phi5,z);
        return a_phi*(7-r/10)+b_phi*(7-r/10)*(7-r/10); // in rad
    }
    
    
}




