/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author davidpayette
 */
public class HelixFitTest {
    public HelixFitTest(HitParameters params){
        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();
        HashMap<Integer, FinalTrackInfo> finaltrackinfomap = new HashMap<>();
        double szpos[][] = new double[1000][3];
        int hit = 0;
        for(int TID : recotrackmap.keySet()){
            for(hit = 0; hit < recotrackmap.get(TID).size(); hit++){
                szpos[hit][0] = recotrackmap.get(TID).get(hit).x();
                szpos[hit][1] = recotrackmap.get(TID).get(hit).y();
                szpos[hit][2] = recotrackmap.get(TID).get(hit).z();
            }
            HelixFitJava h = new HelixFitJava();
            HelixFitObject ho = h.HelixFit(hit,szpos,1);
            double momfit =  ho.get_Mom();
            double px = ho.get_px();
            double py = ho.get_py();
            double pz = ho.get_pz();
            double vz = ho.get_Z0();
            double tl = ho.get_trackl();
            double dEdx = ho.get_dEdx();
            finaltrackinfomap.put(TID, new FinalTrackInfo(px,py,pz,vz,tl,dEdx));
        }
        params.set_finaltrackinfomap(finaltrackinfomap);
    }
}