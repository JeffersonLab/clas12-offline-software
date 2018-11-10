package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.track.Track;

public class GMeasVecs {

	public List<GMeasVec> measurements = new ArrayList<GMeasVec>();
	
	public class GMeasVec {
		final int k;
		public int isl;
		public int ilayer;
		public int iwire;
		public double drift_dist;
		public double error;
		
		GMeasVec(int k){
			this.k = k;
		}

	}

	public void setGMeasVecs(Track trkcand, DCGeant4Factory DcDetector) {
		
		List<GHitOnTrack> hOTS = new ArrayList<GHitOnTrack>(); // the list of hits on track		
		FittedHit hitOnTrk;
		// loops over the regions (1 to 3) and the superlayers in a region (1 to 2) and obtains the hits on track
		for(int c = 0; c<3; c++) { // regions (1 to 3)
			for(int s =0; s<2; s++) { // superlayers in a region (1 to 2)
				for(int h =0; h<trkcand.get(c).get(s).size(); h++) { // hits in superlayer
					if(trkcand.get(c).get(s).get(h).get_Id()==-1)
						continue;
					hitOnTrk = trkcand.get(c).get(s).get(h);
					
					int slayr = trkcand.get(c).get(s).get(h).get_Superlayer();
					int ilayer = trkcand.get(c).get(s).get(h).get_Layer();
					int iwire = trkcand.get(c).get(s).get(h).get_Wire();
					
					//x = DcDetector.getWireMidpoint(slayr-1, ilayer-1, iwire-1).x;
					
					//double Z = hitOnTrk.get_Z();
					double d = trkcand.get(c).get(s).get(h).get_TimeToDistance();
					
					//exclude hits that have poor segment
					//if( (trkcand.get(c).get(s).get(h).get_X()- X)/trkcand.get(c).get(s).get(h).get_CellSize()/Math.cos(Math.toRadians(6.)) >1.5)
					//	continue;
					GHitOnTrack hot = new GHitOnTrack(slayr, ilayer, iwire, d);
					
					//double err_drift_dist = trkcand.get(c).get(s).get_fittedCluster().get_???();
					
					//hot._Unc = Math.sqrt(err_sl1*err_sl1*Z*Z + err_it1*err_it1);
					//hot._hitError = err_sl1*err_sl1*Z*Z + err_it1*err_it1 +2*Z*err_cov1;
					
					hOTS.add(hot);
					
				}
			}
		}
		//Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)
		
		// identify double hits and take the average position		
		/*for(int i = 0; i<hOTS.size(); i++) {
			if( i > 0 ) {
				if(hOTS.get(i-1)._Z == hOTS.get(i)._Z) {
					hOTS.get(i-1)._X = (hOTS.get(i-1)._X/(hOTS.get(i-1)._Unc*hOTS.get(i-1)._Unc) + hOTS.get(i)._X/(hOTS.get(i)._Unc*hOTS.get(i)._Unc) )/(1./(hOTS.get(i-1)._Unc*hOTS.get(i-1)._Unc) + 1./(hOTS.get(i)._Unc*hOTS.get(i)._Unc) );
					//hOTS.get(i-1)._hitError  = 1./Math.sqrt(1./(hOTS.get(i-1)._hitError*hOTS.get(i-1)._hitError) + 1./(hOTS.get(i)._hitError*hOTS.get(i)._hitError) );
					hOTS.remove(i);
				}
			}
		}*/
		
		measurements = new ArrayList<GMeasVec>(hOTS.size());
		
		for(int i = 0; i<hOTS.size(); i++) {
			GMeasVec meas = new GMeasVec(i);
			meas.isl = hOTS.get(i)._sl;
			meas.ilayer = hOTS.get(i)._lay;
			meas.iwire = hOTS.get(i)._wir;
			meas.drift_dist = hOTS.get(i)._dist;
			meas.error 	= hOTS.get(i)._hitError;
			this.measurements.add(i, meas);
		    //System.out.println(" Adding measuremnt "+i+") "+meas.x+", "+meas.z+" +/-"+meas.error+" tilt "+meas.tilt);
		}
	}
	
	private class GHitOnTrack implements Comparable<GHitOnTrack> {
	   public double _hitError;
	   private double _dist;
	   private int _sl;
	   private int _lay;
	   private int _wir;
	   
	   public GHitOnTrack(int superlayer, int layer, int wire, double d) {
		   _dist = d;
		   _sl   = superlayer;
		   _lay  = layer;
		   _wir  = wire;
	   }
        
		@Override
		public int compareTo(GHitOnTrack o) {
			if(this._lay == o._lay)
				return 0;
			if(this._lay>o._lay) {
				return 1;
			} else {
				return -1;
			}
		}
    }
}
