package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.cvt.CentralTracker;
import org.jlab.rec.cvt.cross.Cross;

public class TrackSeederMkIII {
	
	public boolean MVT_greenlight=false;
	
	public TrackSeederMkIII() {
        
    }
	
	public List<Seed> findSeed(CentralTracker CVT, 
			   org.jlab.rec.cvt.svt.Geometry svt_geo, 
			   org.jlab.rec.cvt.bmt.Geometry bmt_geo, 
                                Swim swimmer) {

		List<Seed> seedlist = new ArrayList<Seed>();
		List<ArrayList<Cross>> trCands = new ArrayList<ArrayList<Cross>>();
		
		this.AnalyzeBMT(CVT, trCands);	
		
		if	(MVT_greenlight) {
				
		return seedlist;
		}
		else return null;
	}

	private void AnalyzeBMT(CentralTracker CVT, List<ArrayList<Cross>> trCands) {
		
		
	}
}
