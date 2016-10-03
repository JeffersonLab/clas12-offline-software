package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.CTOFDetHit;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ctof.CalibrationConstantsLoader;
import org.jlab.rec.tof.hit.ctof.Hit;

/**
 * 
 * @author ziegler
 *
 */
public class HitReader {

	public HitReader() {
		// TODO Auto-generated constructor stub
	}

	private List<Hit> _CTOFHits ;
	
	public List<Hit> get_CTOFHits() {
		return _CTOFHits;
	}

	public void set_CTOFHits(List<Hit> _Hits) {
		this._CTOFHits = _Hits;
	}

	/**
	 * 
	 * @param event the evio event
	 * @param geometry the CTOF geometry from package
	 */
	public void fetch_Hits(DataEvent event, CTOFGeant4Factory geometry, List<Line3d> trks, double[] paths) {
		
		if(event.hasBank("CTOF::dgtz")==false) {
			//System.err.println("there is no CTOF bank ");
			
			_CTOFHits = new ArrayList<Hit>();
			
			return;
		}

		
		if(event.hasBank("CTOF::dgtz")==true) {
			EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("CTOF::dgtz");
			
			int[] id 		= bankDGTZ.getInt("hitn");
			int[] paddle 	= bankDGTZ.getInt("paddle");
			int[] ADCU 		= bankDGTZ.getInt("ADCU");
			int[] ADCD 		= bankDGTZ.getInt("ADCD");
			int[] TDCU 		= bankDGTZ.getInt("TDCU");
			int[] TDCD 		= bankDGTZ.getInt("TDCD");
			
			// Instantiates the list of hits
			List<Hit> hits = new ArrayList<Hit>();
			
			for(int i = 0; i<id.length; i++){
			    // get the status				
				int statusU = CalibrationConstantsLoader.STATUSU[0][0][paddle[i]-1];
				int statusD = CalibrationConstantsLoader.STATUSD[0][0][paddle[i]-1]; 
				
				String statusWord = this.set_StatusWord(statusU, statusD, ADCU[i], TDCU[i], ADCD[i], TDCD[i]);
				
				// create the hit object
				Hit hit = new Hit(id[i], 1, 1, paddle[i], ADCU[i], TDCU[i], ADCD[i], TDCD[i]) ;
				hit.setPaddleLine(geometry);
				hit.set_StatusWord(statusWord);
				
	    	    // add this hit
	            hits.add(hit); 
			}

			List<Hit> updated_hits = matchHitsToCVTTrk(hits, geometry, trks, paths) ;
			
			for(Hit hit : updated_hits) {
				hit.set_HitParameters(1);
			}
			
			// fill the list of TOF hits
			this.set_CTOFHits(updated_hits);
		}

	}

	 
	
	public String set_StatusWord(int statusU, int statusD, int ADCU, int TDCU, int ADCD, int TDCD) {
		String statusWord = new String(); //ADCLU TDCU ADCD TDCD
		// selected ranges TDC in [0,1000], ADC in [0, 8192] requirement given by passTDC and passADC methods
		
		switch(statusU) {
			case 0:	statusWord = (""+1*passADC(ADCU)+""+1*passTDC(TDCU)+"");// fully functioning
					break;
			case 1:	statusWord = ("0"+""+1*passTDC(TDCU)+""); 				// no ADC
					break;
			case 2:	statusWord = (""+1*passADC(ADCU)+""+"0"); 				// no TDC
					break;
			case 3:	statusWord = "00";										// no TDC, no ADC
					break;
		} 
		switch(statusD) {
			case 0:	statusWord += (""+1*passADC(ADCD)+""+1*passTDC(TDCD)+"");// fully functioning
					break;
			case 1:	statusWord += ("0"+""+1*passTDC(TDCD)+""); 				// no ADC
					break;
			case 2:	statusWord += (""+1*passADC(ADCD)+""+"0"); 				// no TDC
					break;
			case 3:	statusWord += "00";										// no TDC, no ADC
					break;

		} 
		
	return statusWord; 
		
	}

	private int passTDC(int tDC) {
		// selected ranges TDC in [0, ? 1000] // what is the upper limit?
		int pass =0;
		//if(tDC>100 &&  tDC<300)
			pass = 1;
		return pass;
	}

	private int passADC(int aDC) {
		// selected ranges  ADC in [0, ? 8192]
		int pass =0;
		//if(aDC>700 && aDC<7000)
			pass = 1;
		return pass;
	}

	private List<Hit> matchHitsToCVTTrk(List<Hit>CTOFhits, CTOFGeant4Factory ctofDetector, List<Line3d> trks, double[] paths) { 
		if(trks==null || trks.size()==0)
			return CTOFhits; // no hits were matched with DC tracks
		
		// Instantiates the list of hits
		List<Hit> hitList = new ArrayList<Hit>();
					
		for(int i = 0; i<trks.size(); i++) { // looping over the tracks find the intersection of the track with that plane
			Line3d trk = trks.get(i);
			
			CTOFDetHit[] HitArray = new CTOFDetHit[48] ;
			List<DetHit> hits = ctofDetector.getIntersections(trk);
			
			if(hits != null && hits.size()>0) {
				for(DetHit hit: hits){
					CTOFDetHit fhit = new CTOFDetHit(hit);
					HitArray[fhit.getPaddle()-1] = fhit;
				}
			}
			for(Hit fhit : CTOFhits) {
				if(HitArray[fhit.get_Paddle()-1]==null) { // there is no track matched to this hit
					hitList.add(fhit);	// add this hit to the output list anyway
				}
			}
			
			for(Hit fhit : CTOFhits) {
				if(HitArray[fhit.get_Paddle()-1]!=null) {
					CTOFDetHit matchedHit = HitArray[fhit.get_Paddle()-1];
					
					// create a new FTOF hit for each intersecting track with this hit counter 
					// create the hit object
					Hit hit = new Hit(fhit.get_Id(), fhit.get_Panel(), fhit.get_Sector(), fhit.get_Paddle(), fhit.get_ADC1(), fhit.get_TDC1(), fhit.get_ADC2(), fhit.get_TDC2()) ;
					hit.set_StatusWord(fhit.get_StatusWord());			
					hit.set_paddleLine(fhit.get_paddleLine());
					hit.set_matchedTrackHit(matchedHit);
					hit.set_matchedTrack(trk);
					// get the pathlength of the track from its origin to the mid-point between the track entrance and exit from the bar
					double deltaPath = matchedHit.origin().distance(matchedHit.mid()); 
					hit.set_TrkPathLen(paths[i]+deltaPath);
					// get the coordinates for the track hit, which is defined as the mid-point between its entrance and its exit from the bar
					hit.set_TrkPosition(new Point3D(matchedHit.mid().x,matchedHit.mid().y,matchedHit.mid().z));
					
					// compute the local y at the middle of the bar :
					//----------------------------------------------
			        Point3D origPaddleLine = hit.get_paddleLine().origin();
			        Point3D trkPosinMidlBar = new Point3D(matchedHit.mid().x, matchedHit.mid().y, matchedHit.mid().z);
			        double Lov2 = hit.get_paddleLine().length()/2;
			        double barOrigToTrkPos = origPaddleLine.distance(trkPosinMidlBar);
			        // local y:
			        hit.set_yTrk(barOrigToTrkPos-Lov2);
			        //---------------------------------------
					hitList.add(hit);
				}
			}
		}
		return hitList;
	}

	
}
