/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.jlab.clas.physics.Vector3;

/**
 *
 * @author davidpayette
 * Edited by M. Hattawy in 2022
 */
public class HelixFitTest {
    private double chi2termthreshold = 20;
    private double chi2percthreshold = 50;
    private double szpos[][];
    private int fittobeamline = 0;
    private double magfield = 0;
    private int minhitcount = 5;
    private HashMap<Integer, List<RecoHitVector>> recotrackmap = new HashMap<>();
    private HashMap<Integer, List<RecoHitVector>> newrecotrackmap = new HashMap<>();
    private HashMap<Integer, FinalTrackInfo> finaltrackinfomap = new HashMap<>();
    private boolean cosmic = false;
    private boolean chi2culling = true; 
    public HelixFitTest(HitParameters params, int fitToBeamline, double _magfield, boolean cosm, boolean chi2cull){
        magfield = _magfield;
	chi2culling = chi2cull; 
        fittobeamline = fitToBeamline;
        recotrackmap = params.get_recotrackmap();
        minhitcount = params.get_minhitspertrackreco();
        List<Integer> trackstoremove = new ArrayList<>();
        cosmic = cosm;
        chi2termthreshold = params.get_chi2termthreshold();
        chi2percthreshold = params.get_chi2percthreshold();
        for(int TID : recotrackmap.keySet()){
            findtrackparams(TID,recotrackmap.get(TID),0);
        }
        params.set_recotrackmap(newrecotrackmap);
        params.set_finaltrackinfomap(finaltrackinfomap);
    }
    private double phichi2(double phi0, double r, double R){
        double rterm = r/(2*R);
	    //if(Math.abs(r) > (2 * Math.abs(R))) rterm = 1; 
            if(Math.abs(r) > (2 * Math.abs(R))) rterm /= Math.abs(rterm); 
	    return Math.toRadians(phi0) - Math.asin(rterm);
    }
    private double zchi2(double z0, double theta0, double r, double R){
        R = Math.abs(R);
	    double rterm = r/(2*R);
	    //if(Math.abs(r) > (2 * Math.abs(R))) rterm = 1; 
            if(Math.abs(r) > (2 * Math.abs(R))) rterm /= Math.abs(rterm); 
            return z0 + 2*R*Math.asin(rterm)/Math.tan(Math.toRadians(theta0));
    }

    private void findtrackparams(int TID, List<RecoHitVector> track, int iter){
        List<RecoHitVector> trackhitsA = new ArrayList<>();
        List<RecoHitVector> trackhitsB = new ArrayList<>();
        List<RecoHitVector> originaltrack = new ArrayList<>(track);
        int counter = 0;
        if(track.get(0).flag() > 0 ){
            for(RecoHitVector hit : track){
                 if(hit.flag() == 1 ) trackhitsA.add(hit);
                 if(hit.flag() == 2) trackhitsB.add(hit);
            }
        }

        HelixFitJava h = new HelixFitJava();
        HelixFitObject ho = new HelixFitObject();
        int numhits = track.size();
        int numhitstrack = numhits;
        int hit = 0; 
        boolean trackaused = false;
        boolean trackbused = false; 
        int numhitsA = 0;
        int numhitsB = 0; 
        if(trackhitsA.size() > 10 ) {
            numhitsA = (int) Math.round(0.9*trackhitsA.size());
            numhitsB = trackhitsB.size();
            szpos = new double[numhitsA][3];
            hit = 0; 
            for(hit = 0; hit < numhitsA; hit++){
                szpos[hit][0] = trackhitsA.get(hit).x();
                szpos[hit][1] = trackhitsA.get(hit).y();
                szpos[hit][2] = trackhitsA.get(hit).z();
            }
            ho = h.HelixFit(hit,szpos,fittobeamline);   
            ho.set_magfield(magfield);         
            if(ho.get_Rho() > 0 && trackhitsB.size() > 10 ){ 
                ho = new HelixFitObject();
                szpos = new double[numhitsB][3];
                hit = 0; 
                for(hit = 0; hit < numhitsB; hit++){
                    szpos[hit][0] = trackhitsB.get(hit).x();
                    szpos[hit][1] = trackhitsB.get(hit).y();
                    szpos[hit][2] = trackhitsB.get(hit).z();
                }
                ho = h.HelixFit(hit,szpos,fittobeamline);
                ho.set_magfield(magfield); 
                trackbused = true; 
                numhits = numhitsB;
                track = trackhitsB;
            }else{
                trackaused = true; 
                numhits = numhitsA;
                track = trackhitsA;
            }
          }else{
            szpos = new double[numhits][3];
            hit = 0; 
            for(hit = 0; hit < numhits; hit++){
                szpos[hit][0] = track.get(hit).x();
                szpos[hit][1] = track.get(hit).y();
                szpos[hit][2] = track.get(hit).z();
            }
            ho = h.HelixFit(hit,szpos,fittobeamline); 
            ho.set_magfield(magfield);
              }

        double ADCsum = 0;
        hit = 0;
        for(hit = 0; hit < numhits; hit++){
            ADCsum += track.get(hit).adc();
        }

        //Do I use the full track for these calculations, or only the fitted part? 

        double dz = 0;

        dz = track.get(numhits-1).z() - track.get(0).z();
        Vector3 v1 = new Vector3(track.get(0).x()-ho.get_A(),track.get(0).y()-ho.get_B(),0);
        Vector3 v2 = new Vector3(track.get(numhits-1).x()-ho.get_A(),track.get(numhits-1).y()-ho.get_B(),0);
        double psi = Math.toRadians(v1.theta(v2)); //angle theta for helix
        double momfit =  ho.get_Mom();
        double px = ho.get_px();
        double py = ho.get_py();
        double pz = ho.get_pz();
        double vz = ho.get_Z0();
        double theta = ho.get_Theta();
        double phi = ho.get_Phi();
        double tl = 0;
        double R = ho.get_Rho();
        double A = ho.get_A();
        double B = ho.get_B();
        double chi2 = 0;
        double hitphi = 0;
        double hitz = 0;
        double hitr = 0;
        double denphi = 0.0001;
        double denz = 1.44;
        double chi2phiterm = 0;
        double chi2zterm = 0;
        double chi2term = 0;
        boolean removehits = false;
        List<Integer> hitstoremove = new ArrayList<>();
        //calculate chi2
        for(hit = 0; hit < numhits; hit++){
            hitr = track.get(hit).r();
            hitphi = track.get(hit).phi();
            hitz = track.get(hit).z();
            chi2phiterm = (hitphi - phichi2(phi,hitr,R));
            if(chi2phiterm < -Math.PI) chi2phiterm += 2*Math.PI;
            else if(chi2phiterm > Math.PI) chi2phiterm -= 2*Math.PI;
            chi2phiterm = chi2phiterm*chi2phiterm;
            chi2phiterm /= denphi;
            chi2zterm = (hitz - zchi2(vz,theta,hitr,R))*(hitz - zchi2(vz,theta,hitr,R))/denz;
            chi2term = chi2phiterm + chi2zterm;
            chi2 += chi2term;
            //if(chi2term > chi2termthreshold && iter == 0 && !cosmic && chi2culling) hitstoremove.add(hit); //if the hit messes up chi2 too much we are going to remove it
             if(chi2term > chi2termthreshold && iter == 0 && !cosmic && chi2culling && track.get(hit).flag() == 0) hitstoremove.add(hit); 
        }

        if(hitstoremove.size() > 0 && iter == 0 && !cosmic && chi2culling){ //make a new track containing the hits leftover and fit it again
            if(((double)hitstoremove.size()/(double)numhits) >= chi2percthreshold/100) removehits = true;
            List<RecoHitVector> newtrack = new ArrayList<>();
            for(int i = 0; i < numhits; i++){
                if(!hitstoremove.contains(i)) newtrack.add(track.get(i));
            }
            if(!newtrack.isEmpty() && !removehits) findtrackparams(TID,newtrack,1);
            return;
        }

        R = Math.abs(R);
        chi2 += (R-Math.sqrt(A*A + B*B))*(R-Math.sqrt(A*A + B*B));
        chi2 /= 2*numhits - 4;

        if(R > 0) tl = Math.sqrt(R*R*psi*psi + dz*dz);
        double dEdx = 0;
        if(Double.isNaN(tl)) tl = 0;
        if(tl != 0 && !Double.isNaN(tl)) dEdx = ADCsum/tl;
        if(TID != 0 && numhits > minhitcount){
            newrecotrackmap.put(TID, originaltrack);
            finaltrackinfomap.put(TID, new FinalTrackInfo(px,py,pz,vz,theta,phi,numhits,tl,ADCsum,dEdx,ho.get_Rho(),A,B,chi2));
        }
    }
}
