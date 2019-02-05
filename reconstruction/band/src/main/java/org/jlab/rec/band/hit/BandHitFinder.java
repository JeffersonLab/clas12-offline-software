package org.jlab.rec.band.hit;

import java.util.ArrayList;


//import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.constants.Parameters;


public class BandHitFinder {

	public BandHitFinder(){
		// empty constructor
	}

	/** author:  F.Hauenstein
	 * This class contains the core of the code. The find hits method reconstruct good band hits from raw hits
	 *  using various cuts and matching.
     For the start good hits are defined with only one coincidence hit in the active area of a BAND area and no 
     hit in the veto counters.
     TODO: Implement geometry calculations
	 */
	
	public ArrayList<BandHit> findGoodHits(ArrayList<BandHitCandidate> candidates) 
	{

		Parameters.SetParameters();

		ArrayList<BandHit> coincidences = new ArrayList<BandHit>();      // array list of all coincidence hits in BAND with no veto fired
	

		if(candidates.size() > 0) {

			// Loop through the candidates array to find possible combinations of left and right.
			//in parallel store hits from veto in another array to speed up check later
			
			double xposHit = -1; 		//Position along the bar, determined from time difference
			double yposHit = -1;        //Position in vertical direction, determined from component and middle of the bar
			double zposHit = -1;        //Position alon the beam direction, determined from fired bar and distance measurements stored in Parameters.layerGap/zOffset
			double xposHitUnc = -1; 		//Uncertainty in position along the bar, 
			double yposHitUnc = -1;        //Uncertainty in position in vertical direction, 
			double zposHitUnc = -1;        //Uncertainty in position along the beam direction
			
			
			for(int i = 0; i < (candidates.size()); i++) 
			{	
				
				BandHitCandidate hit1 = candidates.get(i);   // first, get a hit
				//check if hit is in the veto counter (layer 6). If yes directly return empty ArrayList since it is no good event
				if (hit1.GetLayer() == 6) {
					//System.err.println("veto fired, event can not be good :-(");
					return new ArrayList<BandHit>();
				}

				//for each hit get sector, component and layer to check if there is an associated hit on the other side of the bar
				int sector = hit1.GetSector();    // the sector of the hit
				int layer = hit1.GetLayer();   // the layer associated with the hit
				int component = hit1.GetComponent(); // the component associated with the hit
				int side = hit1.GetSide(); // the order associated with the hit

				// Now loop through the candidates again and match any which has same sector,layer,component but
				//diffferent side. Off-set the start of the list to make sure no repeats:

				for (int j = i+1; j < candidates.size(); j++) 
				{	
					BandHitCandidate hit2 = candidates.get(j);   // get the next to compare for coincidence hits

					if (hit2.GetSector() != sector ) continue;             // both must be in the same sector
					if (hit2.GetLayer() != layer) continue;                // both must be in the same layer					
					if (hit2.GetComponent() != component) continue;             // both must have the same component
					
					//Check if side differs by one, if not it is some multihit or so.
					if (Math.abs(hit2.GetSide() - side) != 1) continue;
					
					double tdcleft = -1;
					double tdcright = -1;
					double adcleft = -1;
					double adcright = -1;
					if (hit1.GetSide() == 1) { //Hit1 is from left side PMT
						tdcleft = hit1.GetTimeCorr();
						tdcright = hit2.GetTimeCorr();
						adcleft = hit1.GetAttCorr();
						adcright = hit2.GetAttCorr();
					}
					else if (hit1.GetSide() == 2) { //Hit1 is from right side PMT
						tdcleft = hit1.GetTimeCorr();
						tdcright = hit2.GetTimeCorr();
						adcleft = hit1.GetAttCorr();
						adcright = hit2.GetAttCorr();
					}
					else { 
						System.err.println("BAND HIT FINDER. Found two hits with left and right side but can not assign which hide belongs to which side");
						continue;
					}
					

					double deltaT = tdcleft - tdcright;
					double tMean = (tdcleft + tdcright ) /2.0;
					
					//First cut : the time of hit has to be in a physical time window 
					// window set to 0-250ns for now	
					if (tMean < Parameters.minTime[layer-1] || tMean > Parameters.maxTime[layer-1]) {
						continue;
					}
					
					//Second Cut: Check if time difference is within time window of a bar 
					double maxDiffTime = Parameters.barLengthSector[sector-1]/Parameters.lightspeed;
					if (deltaT > maxDiffTime) continue;
					
					//Third Cut: Check if Energy deposit for both sides is over 2MeVee
				    if (hit1.GetAttCorr() < 2 || hit2.GetAttCorr() < 2)  continue;

					
					//Add here calculations of x,y, and z pos of hit and uncertainties	

	
					// Create a new BandHit and fill it with the relevant info:

					BandHit Hit = new BandHit();  // Takes as index the halfhits array indices of the two half-hits involved.

					Hit.SetSector(sector);
					Hit.SetLayer(layer);
					Hit.SetComponent(component);
					Hit.SetAdcLeft(adcleft);
					Hit.SetAdcRight(adcright);
					Hit.SetTdcLeft(tdcleft);
					Hit.SetTdcRight(tdcright);
					Hit.SetDiffTime(deltaT);
					Hit.SetMeanTime(tMean);
					Hit.SetX(xposHit);
					Hit.SetY(yposHit);
					Hit.SetZ(zposHit);
					Hit.SetUx(xposHitUnc);
					Hit.SetUy(yposHitUnc);
					Hit.SetUz(zposHitUnc);
					
					coincidences.add(Hit);

				}  // close loop over j
			} // close loop over i  		

			// At this stage an array of coincidence hits from type BandHit exists
			//We are only interested if we have exactly one hit surviving all cuts, thus "good" events should 
			//have a coincidences array of lenght one

		
			if (coincidences.size() == 1) return coincidences;
			else {
				return new ArrayList<BandHit>();
			}
		

		}  // closes if candidates array has non-zero entries...

		return coincidences; //this array is empty since the if condition for a non-zero of candidates was not true.

	} // findHits function		

} // BandHitFinder
