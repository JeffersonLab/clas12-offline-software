//Author: David Payette

/* This code sorts pad signals which have been integrated into 120 ns time slices into tracks
 * based on their relative positions in space, and how close in time the signals occur
 */

package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.graphics.EmbeddedCanvas;

public class TrackFinder3 {

	public TrackFinder3(HitParameters params, boolean draw) {
		/*	
		 *Initializations 
		 */
		TrackUtils tutil = new TrackUtils();
		TrackMap TIDMap = new TrackMap();
		List<Integer> TIDList;
		Track track;
		HashMap<Integer, double[]> ADCMap = params.get_R_adc();
		Vector<Integer> PadNum = params.get_PadNum();
		int TrigWindSize = params.get_TrigWindSize();
		int StepSize = 120;
		double adcthresh = 5e-4; 
		int padloopsize = PadNum.size();
		boolean padSorted = false; 
		List<Integer> padTIDlist = new ArrayList<Integer>();
		List<Integer> padlist; 
		int pad = 0;
		double adc = 0; 
		int timeadjlimit = 4; 
		int parenttid = -1;
		String method = "phiz";
		int minhitcount = 5; 
		
		/*
		 * Main Algorithm
		 */
		TIMELOOP: //Loop over all times
		for(int time = 0; time < TrigWindSize; time += StepSize) { //Steps of 120 up to TrigWindSize = 10000
			
			PADLOOP: //Loop over all pads
			for(int padindex = 0; padindex < padloopsize; padindex++) {
				padSorted = false;  //Flag to be set when the pad is assigned to a track
				padTIDlist.clear(); //List of all TIDs assigned to the pad starts empty
				pad = PadNum.get(padindex);	
				adc = ADCMap.get(pad)[time];
				
				if(adc > adcthresh) { //pad adc threshold check
					PadVector PadVec = params.get_padvector(pad); //initializes the x,y,z,phi for pad
					TIDList = TIDMap.getAllTrackIDs();			 //Retrieve list of all available TIDs
					
					TIDLOOP: //Loop over all Track IDs 
					for(int tid : TIDList) {
						track = TIDMap.getTrack(tid);			 //Get track with current tid
						
						TIMECHECKLOOP: //Loop over current and former times
						for(int timecheck = time; timecheck > 0 && timecheck >= time - timeadjlimit*StepSize; timecheck -= StepSize) {
							padlist = track.getTimeSlice(timecheck); //Get pads assigned to current time slice
							if(!padlist.contains(pad)) {				//Ensures pad isn't already assigned here
								PADCHECKLOOP: //Loop over pads 
								for(int checkpad : padlist) {		
									PadVector checkpadvec = params.get_padvector(checkpad);	
									if(tutil.comparePads(PadVec, checkpadvec, method)) {	//compares the position of two pads
										track.addPad(time, pad);			//assign pad to track
										//TIDMap.updateTrack(tid, track);
										padSorted = true;				//flag set
										padTIDlist.add(tid);				//track the TID assigned
										break TIMECHECKLOOP;                            //no need to continue checking previous times
									} //END PAD COMPARE
									
								} //END PADCHECKLOOP
							
							} else {//pad is somehow in this time slice already so lets go ahead and add it to the current time slice
								track.addPad(time, pad);
								//TIDMap.updateTrack(tid, track);
								padSorted = true;
								padTIDlist.add(tid);
								break TIMECHECKLOOP;
							}
							
						} //END TIMECHECKLOOP 
						
					} //END TIDLOOP 
					
					if(!padSorted) { //we need a new TID if we get here, the pad was never assigned an ID
						TIDMap.addTrack(new Track(time,pad));
					}
					
					if(padTIDlist.size()>1) { //if a pad gets more than 1 ID let's merge the IDs	
						for(int tidtemp : padTIDlist) {
							if(tidtemp == padTIDlist.get(0)) {
								parenttid = padTIDlist.get(0);
							} else {
								TIDMap.mergeTracks(parenttid, tidtemp); 
							}
						}						
					}
					
				} //END ADC THRESH CHECK
				
			} //END PADLOOP
		
		} //END TIMELOOP
		
		//END MAIN ALGORITHM
		
		/*
		 * Clean up and flag tracks
		 */
		
		for(int tid : TIDMap.getAllTrackIDs()) { //We need to remove tracks with not enough pads to save time later
			Track tempt = TIDMap.getTrack(tid);
			if(tempt.uniquePadCountTotal() < minhitcount) {
				TIDMap.removeTrack(tid);
			}
		}
		
		//System.out.println("This event has " + TIDMap.getAllTrackIDs().size() + " tracks");
		
		//TODO Flag crossing tracks; for now flag all tracks
		for(int tid : TIDMap.getAllTrackIDs()) {
			Track t = TIDMap.getTrack(tid);
			t.flagTrack();
		}
		
		/*
		 * Output
		 */
		
		params.set_trackmap(TIDMap);
		
		/*
		 * Drawing for debugging
                 * To be removed
		 */
		
		if(draw) {
			
			HashMap<Integer,GraphErrors> gmapzvsphi = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cZvsPhi = new EmbeddedCanvas();
			JFrame jZvsPhi = new JFrame();
			jZvsPhi.setSize(800,600);
			
			HashMap<Integer,GraphErrors> gmapphivst = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cPhivsT = new EmbeddedCanvas();
			JFrame jPhivsT = new JFrame();
			jPhivsT.setSize(800,600);
			
			HashMap<Integer,GraphErrors> gmapzvst = new HashMap<Integer,GraphErrors>();
			EmbeddedCanvas cZvsT = new EmbeddedCanvas();
			JFrame jZvsT = new JFrame();
			jZvsT.setSize(800,600);
			int color = 1; 
			int style = 1;
			for(int tid : TIDMap.getAllTrackIDs()) {
				Track t = TIDMap.getTrack(tid);				
				gmapzvsphi.put(tid, new GraphErrors());
				gmapphivst.put(tid, new GraphErrors());
				gmapzvst.put(tid, new GraphErrors());
				for(int time : t.getAllTimeSlices()) {
					for(int padref : t.getTimeSlice(time)) {
						PadVector p = params.get_padvector(padref);
						gmapzvsphi.get(tid).addPoint(p.phi(), p.z(), 0, 0);
						gmapzvst.get(tid).addPoint(time, p.z(), 0, 0);
						gmapphivst.get(tid).addPoint(time, p.phi(), 0, 0);
					}
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
			
			jZvsPhi.setTitle("Track Finder Output");
			jZvsPhi.add(cZvsPhi);
			jZvsPhi.setVisible(true);
			
			jZvsT.setTitle("Track Finder Output");
			jZvsT.add(cZvsT);
			jZvsT.setVisible(true);
			
			jPhivsT.setTitle("Track Finder Output");
			jPhivsT.add(cPhivsT);
			jPhivsT.setVisible(true);
			
		}
		
	}
	
}
