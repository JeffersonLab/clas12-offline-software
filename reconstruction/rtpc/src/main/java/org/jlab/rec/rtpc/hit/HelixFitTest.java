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
    public HelixFitTest(HitParameters params, int fitToBeamline, double _magfield, boolean cosm){
        magfield = _magfield;
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
        return Math.toRadians(phi0) - Math.asin(r/(2*R));
    }
    private double zchi2(double z0, double theta0, double r, double R){
        R = Math.abs(R);
        return z0 + 2*R*Math.asin(r/(2*R))/Math.tan(Math.toRadians(theta0));
    }

    private void findtrackparams(int TID, List<RecoHitVector> track, int iter){
        szpos = new double[10000][3];
        int numhits = track.size();
        double ADCsum = 0;
        int hit = 0;
        for(hit = 0; hit < numhits; hit++){
            szpos[hit][0] = track.get(hit).x();
            szpos[hit][1] = track.get(hit).y();
            szpos[hit][2] = track.get(hit).z();
            ADCsum += track.get(hit).adc();
        }
        HelixFitJava h = new HelixFitJava();
        HelixFitObject ho = h.HelixFit(hit,szpos,fittobeamline);
        ho.set_magfield(magfield);
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
            if(chi2term > chi2termthreshold && iter == 0 && !cosmic) hitstoremove.add(hit); //if the hit messes up chi2 too much we are going to remove it
        }

        if(hitstoremove.size() > 0 && iter == 0 && !cosmic){ //make a new track containing the hits leftover and fit it again
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
            newrecotrackmap.put(TID, track);
            finaltrackinfomap.put(TID, new FinalTrackInfo(px,py,pz,vz,theta,phi,numhits,tl,ADCsum,dEdx,ho.get_Rho(),A,B,chi2));
        }

    }
}