//Author: David Payette

/* This code takes the tracks from the Track Finder, and reduces the signals in the track to single
 * values in time by taking a weighted average of the signal using the ADC value as the weight
 * The end result is the same tracks but with hits which now have non-discritized times (not in 
 * 120 ns slices) This is useful for the disentangler to split merged tracks
 */

package org.jlab.rec.rtpc.hit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeAverage {
	
    private ReducedTrackMap RTIDMap = new ReducedTrackMap();
    private ReducedTrack rtrack; 
    private TrackMap TIDMap;
    private ADCMap ADCMap;		
    private List<Integer> tids;
    private Track track; 
    private double adc = 0; 
    private double adcmax = 0; 
    private double averagetime = 0; 
    private double adcthresh = 0; 
    private double sumnum = 0; 
    private double sumden = 0; 
    
    public TimeAverage(HitParameters params) {
        /*	
         *Initializations 
         */
        TIDMap = params.get_trackmap();
        ADCMap = params.get_ADCMap();
        tids = TIDMap.getAllTrackIDs();

        /*
         * Main Algorithm
         */
		
        for(int tid : tids) { //Loop over all tracks
            track = TIDMap.getTrack(tid);
            boolean trackflag = track.isTrackFlagged();
            rtrack = new ReducedTrack();
            if(trackflag) {rtrack.flagTrack();}
            Set<Integer> l = track.uniquePadList();
            Set<Integer> timesbypad = new HashSet<>();
            for(int pad : l) {
                adcmax = 0; 
                sumnum = 0; 
                sumden = 0; 
                timesbypad = track.PadTimeList(pad);
                for(int time : timesbypad) { //Loop to calculate maximum adc value
                    adc = ADCMap.getADC(pad,time);
                    if(adc > adcmax) {
                        adcmax = adc; 
                    }
                }
                adcthresh = adcmax/2;
                for(int time : timesbypad) { //Loop to calculate weighted average time using ADC values which are above half of the maximum
                    adc = ADCMap.getADC(pad,time);
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
    }	
}
