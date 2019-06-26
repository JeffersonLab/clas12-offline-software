//Author: David Payette

/* This code takes the tracks from the Track Finder, and reduces the signals in the track to single
 * values in time by taking a weighted average of the signal using the ADC value as the weight
 * The end result is the same tracks but with hits which now have non-discritized times (not in 
 * 120 ns slices) This is useful for the disentangler to split merged tracks
 */

package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class TimeAverage2 {
	
	public TimeAverage2(HitParameters params, boolean draw) {
		/*	
		 *Initializations 
		 */
                
		ReducedTrackMap RTIDMap = new ReducedTrackMap();
		ReducedTrack rtrack; 
		TrackMap TIDMap = params.get_trackmap();
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();		
		List<Integer> tids = TIDMap.getAllTrackIDs();
		Track track; 
		double adc = 0; 
		double adcmax = 0; 
		double averagetime = 0; 
		double adcthresh = 0; 
		double sumnum = 0; 
		double sumden = 0; 
		
		/*
		 * Main Algorithm
		 */
		
		for(int tid : tids) {
			track = TIDMap.getTrack(tid);
			boolean trackflag = track.isTrackFlagged();
			rtrack = new ReducedTrack();
			if(trackflag) {rtrack.flagTrack();}
			Set<Integer> l = track.uniquePadList();
			Set<Integer> timesbypad = new HashSet<Integer>();
			for(int pad : l) {
				adcmax = 0; 
				sumnum = 0; 
			 	sumden = 0; 
				timesbypad = track.PadTimeList(pad);
				for(int time : timesbypad) { //Loop to calculate maximum adc value
					adc = ADCMap.get(pad)[time];
					if(adc > adcmax) {
						adcmax = adc; 
					}
				}
				adcthresh = adcmax/2;
				for(int time : timesbypad) { //Loop to calculate weighted average time using ADC values which are above half of the maximum
					adc = ADCMap.get(pad)[time];
					if(adc > adcthresh) {
						sumnum += adc*time;
						sumden += adc;
					}
				}
				averagetime = sumnum/sumden;
				PadVector p = params.get_padvector(pad);
				HitVector v = new HitVector(pad,p.z(),p.phi(),averagetime,sumden);
				rtrack.addHit(v);
			}
			RTIDMap.addTrack(rtrack);			
		}
		
		/*
		 * Output
		 */
		
		params.set_rtrackmap(RTIDMap);
		
		/*
		 * Drawing for debugging
		 */
		
		if(draw) {
			
			HashMap<Integer,GraphErrors> gmapzvsphi = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cZvsPhi = new EmbeddedCanvas();
			JFrame jZvsPhi = new JFrame();
			jZvsPhi.setSize(800,800);
			
			HashMap<Integer,GraphErrors> gmapphivst = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cPhivsT = new EmbeddedCanvas();
			JFrame jPhivsT = new JFrame();
			jPhivsT.setSize(800,800);
			
			HashMap<Integer,GraphErrors> gmapzvst = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cZvsT = new EmbeddedCanvas();
			JFrame jZvsT = new JFrame();
			jZvsT.setSize(800,800);
			int color = 1; 
			int style = 1;
			for(int tid : RTIDMap.getAllTrackIDs()) {
				ReducedTrack t = RTIDMap.getTrack(tid);				
				gmapzvsphi.put(tid, new GraphErrors());
				gmapphivst.put(tid, new GraphErrors());
				gmapzvst.put(tid, new GraphErrors());
				for(HitVector v : t.getAllHits()) {
					double time = v.time();
					gmapzvsphi.get(tid).addPoint(v.phi(), v.z(), 0, 0);
					gmapzvst.get(tid).addPoint(time, v.z(), 0, 0);
					gmapphivst.get(tid).addPoint(time, v.phi(), 0, 0);
				}
				gmapzvsphi.get(tid).setMarkerColor(color);
				gmapzvsphi.get(tid).setMarkerSize(3);
				gmapzvsphi.get(tid).setMarkerStyle(style);
				
				gmapzvst.get(tid).setMarkerColor(color);
				gmapzvst.get(tid).setMarkerSize(3);
				gmapzvst.get(tid).setMarkerStyle(style);
			
				gmapphivst.get(tid).setMarkerColor(color);
				gmapphivst.get(tid).setMarkerSize(3);
				gmapphivst.get(tid).setMarkerStyle(style);
				
				cZvsPhi.draw(gmapzvsphi.get(tid),"same");
				cZvsT.draw(gmapzvst.get(tid),"same");
				cPhivsT.draw(gmapphivst.get(tid),"same");

				color++;
				if(color > 8) {
					color = 1;
					style++;
				}
				
			}
			
			jZvsPhi.setTitle("Time Average Output");
			jZvsPhi.add(cZvsPhi);
			jZvsPhi.setVisible(true);
			
			jZvsT.setTitle("Time Average Output");
			jZvsT.add(cZvsT);
			//jZvsT.setVisible(true);
			
			jPhivsT.setTitle("Time Average Output");
			jPhivsT.add(cPhivsT);
			//jPhivsT.setVisible(true);
			
		}
		
	}
	
}
