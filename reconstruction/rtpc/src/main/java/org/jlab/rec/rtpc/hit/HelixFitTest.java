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
    public HelixFitTest(HitParameters params, int fitToBeamline, double magfield){
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
            ho.set_magfield(magfield);
            Vector3 v1 = new Vector3();
            double dz = 0;

            dz = recotrackmap.get(TID).get(numhits-1).z() - recotrackmap.get(TID).get(0).z();
            v1 = new Vector3(recotrackmap.get(TID).get(0).x()-ho.get_A(),recotrackmap.get(TID).get(0).y()-ho.get_B(),0);          
            Vector3 v2 = new Vector3(recotrackmap.get(TID).get(numhits-1).x()-ho.get_A(),recotrackmap.get(TID).get(numhits-1).y()-ho.get_B(),0);
            double psi = Math.toRadians(v1.theta(v2)); //angle theta for helix
            double momfit =  ho.get_Mom();
            double px = ho.get_px();
            double py = ho.get_py();
            double pz = ho.get_pz();
            double vz = ho.get_Z0();
            double theta = ho.get_Theta();
            double phi = ho.get_Phi();
            double tl = 0;
            double R = Math.abs(ho.get_Rho());
            double A = ho.get_A();
            double B = ho.get_B();
            double chi2 = 0;
            double hitphi = 0;
            double hitz = 0;
            double hitr = 0;
            double denphi = 0.0001;
            double denz = 1.44;
            double chi2phiterm = 0;
            for(hit = 0; hit < numhits; hit++){
                hitr = recotrackmap.get(TID).get(hit).r();
                hitphi = recotrackmap.get(TID).get(hit).phi();
                hitz = recotrackmap.get(TID).get(hit).z();
                chi2phiterm = (hitphi - phichi2(phi,hitr,R))*(hitphi - phichi2(phi,hitr,R));
                chi2phiterm = Math.min(chi2phiterm, Math.min(chi2phiterm - 2*Math.PI,chi2phiterm + 2*Math.PI));
                chi2 += chi2phiterm/denphi;
                chi2 += (hitz - zchi2(vz,theta,hitr,R))*(hitz - zchi2(vz,theta,hitr,R))/denz;
            }
            chi2 += (R-Math.sqrt(A*A + B*B))*(R-Math.sqrt(A*A + B*B));
            chi2 /= numhits - 3;
            if(R > 0) tl = Math.sqrt(R*R*psi*psi + dz*dz);
            double dEdx = 0;
            if(Double.isNaN(tl)) tl = 0;
            if(tl != 0 && !Double.isNaN(tl)) dEdx = ADCsum/tl;          
            if(TID != 0) finaltrackinfomap.put(TID, new FinalTrackInfo(px,py,pz,vz,theta,phi,numhits,tl,ADCsum,dEdx,ho.get_Rho(),A,B,chi2));
        }
        params.set_finaltrackinfomap(finaltrackinfomap);
    }
    private double phichi2(double phi0, double r, double R){
        return Math.toRadians(phi0) + Math.asin(r/(2*R));
    }
    private double zchi2(double z0, double theta0, double r, double R){
        return z0 + 2*R*Math.asin(r/(2*R))/Math.tan(Math.toRadians(theta0));
    }
}