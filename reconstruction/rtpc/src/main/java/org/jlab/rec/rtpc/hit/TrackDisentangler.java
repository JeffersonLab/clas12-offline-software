/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;
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
    
    public TrackDisentangler(ReducedTrackMap rtmap){//HitParameters params){
        //RTIDMap = params.get_rtrackmap();
        RTIDMap = rtmap;
        System.out.println("Before " + NewTrackMap.getAllTrackIDs().size());
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

        System.out.println("After " + NewTrackMap.getAllTrackIDs().size());
        
        List<GraphErrors> gzphi = new ArrayList<>();
        List<GraphErrors> gzt = new ArrayList<>();
        List<GraphErrors> gphit = new ArrayList<>();
        EmbeddedCanvas c = new EmbeddedCanvas();
        c.divide(1,3);
        JFrame j = new JFrame();
        int color = 1;
        int index = 0;
        for(int tid : RTIDMap.getAllTrackIDs()){

            ReducedTrack t = RTIDMap.getTrack(tid);
            gzphi.add(new GraphErrors());
            gzt.add(new GraphErrors());
            gphit.add(new GraphErrors());
            for(HitVector hit : t.getAllHits()){
                gzphi.get(index).addPoint(hit.phi(), hit.z(), 0, 0);
                gzt.get(index).addPoint(hit.time(), hit.z(),0,0);
                gphit.get(index).addPoint(hit.time(),hit.phi(),0,0);
            }
            gzphi.get(index).setMarkerColor(color);
            gzphi.get(index).setMarkerSize(2);
            gzt.get(index).setMarkerColor(color);
            gzt.get(index).setMarkerSize(2);
            gphit.get(index).setMarkerColor(color);
            gphit.get(index).setMarkerSize(2);
            c.cd(0);
            c.draw(gzphi.get(index),"same");
            c.cd(1);
            c.draw(gzt.get(index),"same");
            c.cd(2);
            c.draw(gphit.get(index),"same");
            index++;
            color++;
        }
        j.add(c);
        j.setSize(800,800);
        j.setVisible(true);
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
    
    public static void main(String[] args){
       ReducedTrackMap rtmap = new ReducedTrackMap();
       ReducedTrack t1 = new ReducedTrack();
       double zstep = 0; 
       for(int time = 0; time <= 3600; time+=120){
           zstep = 4*time/120;
           System.out.println("zstep " + zstep);
           t1.addHit(new HitVector(1,zstep,(0.04/120)*time,time,0));
           
           t1.addHit(new HitVector(1,-zstep + 40,-(0.04/120)*time + 1.1,time,0));
           System.out.println("hits " + t1.getLastHit().z() + " " + t1.getLastHit().phi());
           t1.addHit(new HitVector(1,-zstep + 60,-(0.04/120)*time + 0.8,time,0));
           
       }
       t1.flagTrack();
       rtmap.addTrack(t1);
       TrackDisentangler td = new TrackDisentangler(rtmap);
       

    }
}

