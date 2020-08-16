//Author: David Payette

/* This code sorts pad signals which have been integrated into 120 ns time slices into tracks
 * based on their relative positions in space, and how close in time the signals occur
 */

package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.jlab.detector.calib.utils.ConstantsManager;


public class TrackFinder {
    
    private TrackUtils tutil = new TrackUtils();
    private TrackMap TIDMap = new TrackMap();
    private List<Integer> TIDList;
    private Track track;
    private ADCMap ADCMap;
    private List<Integer> PadList;
    private int TrigWindSize;
    private int StepSize = 120;//Bin Size of Dream Electronics Output
    private double adcthresh = 0; 
    private int padloopsize;// = PadList.size();
    private boolean padSorted = false; 
    private List<Integer> padTIDlist = new ArrayList<>();
    private List<Integer> padlist; 
    private int pad = 0;
    private double adc = 0; 
    private int timeadjlimit = 4; 
    private int parenttid = -1;
    private String method = "phiz";
    private int minhitcount = 5; 
    private double zthresh = 16;
    private double phithresh = 0.16;
    private double zthreshgap = 20;
    private double phithreshgap = 0.20;
    private double TFtotaltracktimeflag = 5000;
    private double TFtotalpadtimeflag = 1000;
    

    public TrackFinder(HitParameters params, boolean cosmic) {
        /*	
         *Initializations 
         */
        timeadjlimit = params.get_timeadjlimit();
        adcthresh = params.get_adcthresh();
        minhitcount = params.get_minhitspertrack();
        zthresh = params.get_zthreshTF();
        phithresh = params.get_phithreshTF();
        zthreshgap = params.get_zthreshTFgap();
        phithreshgap = params.get_phithreshTFgap();
        ADCMap = params.get_ADCMap();
        PadList = params.get_PadList();
        TFtotaltracktimeflag = params.get_TFtotaltracktimeflag();
        TFtotalpadtimeflag = params.get_TFtotalpadtimeflag();
        
        TrigWindSize = params.get_TrigWindSize();
        padloopsize = PadList.size();
        
        /*
         * Main Algorithm
         */
        TIMELOOP: //Loop over all times
        for(int time = 0; time < TrigWindSize; time += StepSize) { //Steps of 120 up to TrigWindSize = 10000

            PADLOOP: //Loop over all pads
            for(int padindex = 0; padindex < padloopsize; padindex++) {
                padSorted = false;  //Flag to be set when the pad is assigned to a track
                padTIDlist.clear(); //List of all TIDs assigned to the pad starts empty
                pad = PadList.get(padindex);	
                adc = ADCMap.getADC(pad,time);
                
                

                if(adc > adcthresh) { //pad adc threshold check

                    PadVector PadVec = params.get_padvector(pad); //initializes the x,y,z,phi for pad
                    TIDList = TIDMap.getAllTrackIDs(); //Retrieve list of all available TIDs

                    TIDLOOP: //Loop over all Track IDs 
                    for(int tid : TIDList) {
                        track = TIDMap.getTrack(tid); //Get track with current tid

                        TIMECHECKLOOP: //Loop over current and former times
                        for(int timecheck = time; timecheck > 0 && timecheck >= time - timeadjlimit*StepSize; timecheck -= StepSize) {
                            padlist = track.getTimeSlice(timecheck); //Get pads assigned to current time slice
                            if(!padlist.contains(pad)) { //Ensures pad isn't already assigned here
                                PADCHECKLOOP: //Loop over pads 
                                for(int checkpad : padlist) {		
                                    PadVector checkpadvec = params.get_padvector(checkpad);	
                                    if(tutil.comparePads(PadVec, checkpadvec, method, cosmic, zthresh, zthreshgap, phithresh, phithreshgap)) { //compares the position of two pads
                                        track.addPad(time, pad);			//assign pad to track
                                        padSorted = true;				//flag set
                                        padTIDlist.add(tid);				//track the TID assigned
                                        break TIMECHECKLOOP;                            //no need to continue checking previous times
                                    } //END PAD COMPARE

                                } //END PADCHECKLOOP

                            } else {//pad is somehow in this time slice already so lets go ahead and add it to the current time slice
                                    track.addPad(time, pad);
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
              
        
        if(!cosmic){
            //Flag crossing tracks
            int tmax = 0;
            int tmin = 0;
            for(int tid : TIDMap.getAllTrackIDs()) {          
                Track t = TIDMap.getTrack(tid); 
                for(int pad : t.uniquePadList()){
                    tmax = 0;
                    tmin = 1000000;
                    for(int time : t.PadTimeList(pad)){
                        if(time > tmax) tmax = time;
                        if(time < tmin) tmin = time;
                    }
                    if(tmax - tmin > TFtotalpadtimeflag){
                        t.flagTrack();
                        break;
                    }
                }
                List<Integer> times = t.getAllTimeSlices();
                Collections.sort(times);
                if(times.get(times.size()-1) - times.get(0) > TFtotaltracktimeflag) t.flagTrack();
            }
        }
        
        
        /*
         * Output
         */
       

        params.set_trackmap(TIDMap);

    }       
	
}
