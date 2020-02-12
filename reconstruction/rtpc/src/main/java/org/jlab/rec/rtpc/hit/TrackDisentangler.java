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
    private double maxdeltaz = 8;
    private double maxdeltaphi = 0.10;
    
    public TrackDisentangler(HitParameters params){
        
        RTIDMap = params.get_rtrackmap();
        maxdeltat = params.get_tthreshTD();
        maxdeltaz = params.get_zthreshTD();
        maxdeltaphi = params.get_phithreshTD();
        
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
                            HitVector h1 = t1.getLastHit();
                            HitVector h2 = t2.getLastHit();
                            if(Math.abs(h1.z() - h2.z()) < maxdeltaz && 
                              (Math.abs(h1.phi() - h2.phi()) < maxdeltaphi || Math.abs(h1.phi() - h2.phi() - 2*Math.PI) < maxdeltaphi)){
                                NewTrackMap.mergeTracks(tid1, tid2); 
                                NewTrackMap.getTrack(tid1).sortHits();
                                removedtracks.add(tid2);
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
    
    private void sortHit(HitVector hit){
        List<Integer> TIDList = NewTrackMap.getAllTrackIDs();
        boolean hitsorted = false;
        for(int tid : TIDList){
            ReducedTrack t = NewTrackMap.getTrack(tid);
            HitVector comphit = t.getLastHit();
            if(comphit.time() - hit.time() < maxdeltat &&
               hit.time() < comphit.time() &&
               Math.abs(hit.z() - comphit.z()) < maxdeltaz &&
               (Math.abs(hit.phi() - comphit.phi()) < maxdeltaphi || Math.abs(hit.phi() - comphit.phi() - Math.PI * 2) < maxdeltaphi)){
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
}

