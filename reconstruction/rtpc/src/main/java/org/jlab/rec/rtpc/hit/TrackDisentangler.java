/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author davidpayette
 */
public class TrackDisentangler {
    private ReducedTrackMap RTIDMap = new ReducedTrackMap();
    private ReducedTrackMap NewTrackMap = new ReducedTrackMap();
    private ReducedTrack rtrack; 
    private int maxdeltat = 300;
    private int maxdeltatgap = 300;
    private double maxdeltaz = 8;
    private double maxdeltaphi = 0.10;
    private double maxdeltazgap = 10;
    private double maxdeltaphigap = 0.12;
    
    public TrackDisentangler(HitParameters params, boolean disentangle){       
        
        if(disentangle){
            RTIDMap = params.get_rtrackmap();
            maxdeltat = params.get_tthreshTD();
            maxdeltatgap = params.get_tthreshTDgap();
            maxdeltaz = params.get_zthreshTD();
            maxdeltaphi = params.get_phithreshTD();
            maxdeltazgap = params.get_zthreshTDgap();
            maxdeltaphigap = params.get_phithreshTDgap();

            List<Integer> origtidlist = RTIDMap.getAllTrackIDs();
            for(int tid : origtidlist){
                rtrack = RTIDMap.getTrack(tid);           
                if(rtrack.isTrackFlagged()){
                    NewTrackMap = new ReducedTrackMap();
                    rtrack.sortHits();
                    List<HitVector> hits = rtrack.getAllHits();
                    for(HitVector hit : hits){
                        sortHit(hit);
                    }
                    List<Integer> newtidlist = NewTrackMap.getAllTrackIDs();  
                    List<Integer> removedtracks = new ArrayList<>();
                    for(int tid1 : newtidlist){
                        for(int tid2 : newtidlist){
                            if(tid1 != tid2 && !removedtracks.contains(tid1) && !removedtracks.contains(tid2)){
                                ReducedTrack t1 = NewTrackMap.getTrack(tid1);
                                ReducedTrack t2 = NewTrackMap.getTrack(tid2);
                                List<HitVector> h1list = new ArrayList<>();
                                List<HitVector> h2list = new ArrayList<>();
                                h1list.addAll(t1.getFirstNHits(2));
                                h1list.addAll(t1.getLastNHits(2));
                                h2list.addAll(t2.getFirstNHits(2));
                                h2list.addAll(t2.getLastNHits(2));
                                HITSLOOP:
                                for(HitVector h1 : h1list){
                                    for(HitVector h2 : h2list){
                                        if(compareHits(h1,h2)){
                                            NewTrackMap.mergeTracks(tid1, tid2); 
                                            NewTrackMap.getTrack(tid1).sortHits();
                                            removedtracks.add(tid2);
                                            break HITSLOOP;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    RTIDMap.removeTrack(tid);
                    newtidlist = NewTrackMap.getAllTrackIDs();
                    for(int tidfinal : newtidlist){
                        RTIDMap.addTrack(NewTrackMap.getTrack(tidfinal));
                    }
                }
            }
            params.set_rtrackmap(RTIDMap);
        }
        
        
    }
    
    private void sortHit(HitVector hit){
        List<Integer> TIDList = NewTrackMap.getAllTrackIDs();
        boolean hitsorted = false;
        for(int tid : TIDList){
            ReducedTrack t = NewTrackMap.getTrack(tid);
            HitVector comphit = t.getLastHit();
            if(compareHitsTime(hit,comphit)){
                t.addHit(hit);
                hitsorted = true;
            }            
        }
        if(!hitsorted){
            ReducedTrack newt = new ReducedTrack();
            newt.addHit(hit);
            NewTrackMap.addTrack(newt);
        }
    }
    
    private boolean compareHits(HitVector a, HitVector b){
        double zdiff = a.z() - b.z();
        double phidiff = Math.abs(a.phi() - b.phi());
        if(phidiff > Math.PI){
            return Math.abs(phidiff - 2*Math.PI) < maxdeltaphigap && zdiff < maxdeltazgap;
        }else{
            return phidiff < maxdeltaphi && zdiff < maxdeltaz;
        }
    }
    
    private boolean compareHitsTime(HitVector a, HitVector b){
        double zdiff = a.z() - b.z();
        double phidiff = Math.abs(a.phi() - b.phi());
        boolean torder = false;
        if(a.time() < b.time()) torder = true;
        if(phidiff > Math.PI){
            return b.time() - a.time() < maxdeltatgap && torder && Math.abs(phidiff - 2*Math.PI) < maxdeltaphigap && zdiff < maxdeltazgap;
        }else{
            return b.time() - a.time() < maxdeltat && torder && phidiff < maxdeltaphi && zdiff < maxdeltaz;
        }
    }
}

