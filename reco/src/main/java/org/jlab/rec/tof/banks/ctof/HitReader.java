package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ctof.CTOFGeometry;
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
	public void fetch_Hits(DataEvent event, CTOFGeometry geometry, List<ArrayList<Path3D>> trks, List<double[]> paths) {
		
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
				
				hit.set_StatusWord(statusWord);
				// get the line in the middle of the paddle
				hit.set_paddleLine(hit.calc_PaddleLine(geometry)); 
				
				// add this hit
				hits.add(hit); 
			}
			
			List<Hit> updated_hits = matchHitsToCVTTrk(hits, geometry, trks, paths);
			
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

	private List<Hit> matchHitsToCVTTrk(List<Hit> hits, CTOFGeometry ctofDetector, List<ArrayList<Path3D>> trks, List<double[]> paths) { 
		
		if(trks == null)
			return hits; // no tracking information available
		
		List<Hit> hitList = new ArrayList<Hit>();
		
		for(Hit hit : hits) { // for each hit loop over the tracks in the event and find a match... will be redone in a more efficient way using the new geometry package
			
			ScintillatorPaddle geomPaddle = (ScintillatorPaddle) ctofDetector.getScintillatorPaddle(hit.get_Paddle());
			Line3D lineX = geomPaddle.getLine(); // Line representing the paddle 
			int NbofMatchedTrksforThisHit = 0;
			
			for(int i = 0; i<trks.get(hit.get_Sector()-1).size(); i++) { // looping over the tracks find the intersection of the track with that plane
				Path3D path = trks.get(hit.get_Sector()-1).get(i);
				Line3D intersect = path.distance(lineX); // intersection of the path with the paddle line
				
				boolean isTrkIntersPad = false ;
				if(intersect.length()<1) // not sure what the cut value should be
					isTrkIntersPad=false;
				
				if(isTrkIntersPad==false)
					continue;
				
				// there is a track intersection
				// make a new hit
				Hit newhit = new Hit(hit.get_Id(), hit.get_Panel(), hit.get_Sector(), hit.get_Paddle(), hit.get_ADC1(), hit.get_TDC1(), hit.get_ADC2(), hit.get_TDC2()) ;
				newhit.set_StatusWord(hit.get_StatusWord());			
				newhit.set_paddleLine(lineX);
				
		 		Point3D intP = intersect.end(); 
		 		
		        double deltaCTtoTOFx = intersect.end().x()-intersect.origin().x();
		        double deltaCTtoTOFy = intersect.end().y()-intersect.origin().y();
		        double deltaCTtoTOFz = intersect.end().z()-intersect.origin().z();
		        
		        Vector3D deltaCTtoTOF = new Vector3D(deltaCTtoTOFx,deltaCTtoTOFy,deltaCTtoTOFz);
		        
				// get the pathlength of the track from its origin to the mid-point between the track entrance and exit from the bar			
		        newhit.set_TrkPathLen(paths.get(newhit.get_Sector()-1)[i]+deltaCTtoTOF.mag());
				// get the coordinates for the track hit, which is defined as the mid-point between its entrance and its exit from the bar
		        newhit.set_TrkPosition(new Point3D(intP.x(), intP.y(),intP.z()));
				// local y:
				intP.rotateZ(-(hit.get_Paddle()-1)*Math.toRadians(7.5));
				newhit.set_yTrk(intP.y());
				
				// add it to the list
				hitList.add(newhit);
				NbofMatchedTrksforThisHit++;
			}
			if(NbofMatchedTrksforThisHit==0) // no track matched to this hit
				hitList.add(hit);
		}
		return hitList;
	}
	
	
}
