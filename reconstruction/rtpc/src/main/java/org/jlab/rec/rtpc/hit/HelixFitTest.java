/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.List;
import org.jlab.clas.physics.Vector3;

/**
 *
 * @author davidpayette
 */
public class HelixFitTest {
    public HelixFitTest(HitParameters params, int fitToBeamline){
        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();
        HashMap<Integer, FinalTrackInfo> finaltrackinfomap = new HashMap<>();
        double szpos[][] = new double[10000][3];
        int hit = 0;
        for(int TID : recotrackmap.keySet()){
            int numhits = recotrackmap.get(TID).size();
            double ADCsum = 0;
            for(hit = 0; hit < numhits; hit++){
                szpos[hit][0] = recotrackmap.get(TID).get(hit).x();
                szpos[hit][1] = recotrackmap.get(TID).get(hit).y();
                szpos[hit][2] = recotrackmap.get(TID).get(hit).z();
                ADCsum += recotrackmap.get(TID).get(hit).adc();
            }
            HelixFitJava h = new HelixFitJava();
            HelixFitObject ho = h.HelixFit(hit,szpos,fitToBeamline);
            Vector3 v1 = new Vector3();
            double dz = 0;

            dz = recotrackmap.get(TID).get(numhits-1).z() - recotrackmap.get(TID).get(0).z();
            v1 = new Vector3(recotrackmap.get(TID).get(0).x()-ho.get_A(),recotrackmap.get(TID).get(0).y()-ho.get_B(),0);          
            Vector3 v2 = new Vector3(recotrackmap.get(TID).get(numhits-1).x()-ho.get_A(),recotrackmap.get(TID).get(numhits-1).y()-ho.get_B(),0);
            double psi = Math.toRadians(v1.theta(v2)); //angle theta for helix
            double momfit =  ho.get_Mom();
            double gain = 1;
            double px = ho.get_px();
            double py = ho.get_py();
            double pz = ho.get_pz();
            double vz = ho.get_Z0();
            double theta = ho.get_Theta();
            double phi = ho.get_Phi();
            double tl = 0;
            if(ho.get_Rho() > 0) Math.sqrt(ho.get_Rho()*ho.get_Rho()*psi*psi + dz*dz);
            double dEdx = 0;
            if(tl != 0) dEdx = ADCsum/(gain*tl);
            finaltrackinfomap.put(TID, new FinalTrackInfo(px,py,pz,vz,theta,phi,numhits,tl,dEdx));
        }
        params.set_finaltrackinfomap(finaltrackinfomap);
    }
}