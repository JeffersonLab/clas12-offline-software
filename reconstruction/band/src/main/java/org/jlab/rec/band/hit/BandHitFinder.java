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
     For the start good hits are defined with only up to 5 coincidence hit in the active area of a BAND area and no 
     hit in the veto counters.
     TODO: Implement geometry calculations
	 */
	
	public ArrayList<BandHit> findGoodHits(ArrayList<BandHitCandidate> candidates) 
	{

		Parameters.SetParameters();

		ArrayList<BandHit> coincidences = new ArrayList<BandHit>();      // array list of all coincidence hits in BAND with no veto fired
	

		if(candidates.size() > 0) {

			// Loop through the candidates array to find possible combinations of left and right.
			// check directly if veto counter has a hit and return empty array to speed up loop.
			
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
					double ftdcleft = -1;
					double ftdcright = -1;
					if (hit1.GetSide() == 1) { //Hit1 is from left side PMT
						
						//adcleft = hit1.GetAttCorr();
						//adcright = hit2.GetAttCorr();
						tdcleft = hit1.GetTimeCorr();
						tdcright = hit2.GetTimeCorr();
						ftdcleft = hit1.GetFtdc();
						ftdcright = hit2.GetFtdc();
						adcleft = hit1.GetAdc();
						adcright = hit2.GetAdc();
					}
					else if (hit1.GetSide() == 2) { //Hit1 is from right side PMT
						tdcleft = hit2.GetTimeCorr();
						tdcright = hit1.GetTimeCorr();
						ftdcleft = hit2.GetFtdc();
						ftdcright = hit1.GetFtdc();
						adcleft = hit2.GetAdc();
						adcright = hit1.GetAdc();
						//adcleft = hit2.GetAttCorr();
						//adcright = hit1.GetAttCorr();
					}
					else { 
						System.err.println("BAND HIT FINDER. Found two hits with left and right side but can not assign which hide belongs to which side");
						continue;
					}
					

					double deltaT = tdcleft - tdcright;
					double tMean = (tdcleft + tdcright ) /2.0;
					
					//First cut : the time of hit has to be in a physical time window 
					// window set to 0-250ns for now	
				   // if (tMean < Parameters.minTime[layer-1] || tMean > Parameters.maxTime[layer-1]) {
				//		continue;
				//	}
					
					//Second Cut: Check if time difference is within time window of a bar 
				//	double maxDiffTime = Parameters.barLengthSector[sector-1]/Parameters.lightspeed;
				//	if (Math.abs(deltaT) > maxDiffTime) continue;
					
					//Third Cut: Check if Energy deposit for both sides is over 2MeVee
				//    if (hit1.GetAttCorr() < 2 || hit2.GetAttCorr() < 2)  continue;

					
					//Add here calculations of x,y, and z pos of hit and uncertainties	

	
					// Create a new BandHit and fill it with the relevant info:

					BandHit Hit = new BandHit();  
					
					Hit.SetSector(sector);
					Hit.SetLayer(layer);
					Hit.SetComponent(component);
					Hit.SetAdcLeft(adcleft);
					Hit.SetAdcRight(adcright);
					Hit.SetTdcLeft(tdcleft);
					Hit.SetTdcRight(tdcright);
					Hit.SetFtdcLeft(ftdcleft);
					Hit.SetFtdcRight(ftdcright);
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
			//We are only interested if we have five or less hits surviving all cuts, thus "good" events should 
			//have a coincidences array of length 5 or less

			//Next line is for the future when the advancedHitFinder is correctly implemented
		//	return advancedHitFinder(coincidences);
		
			
			if (coincidences.size() <=10 && coincidences.size()>0) {
				System.out.println("In BandHitFinder found " + coincidences.size() + " coincidence hits");
				for (int i = 0; i < (coincidences.size()); i++) {
					System.out.println("Hit "+i+" : sector "+ coincidences.get(i).GetSector()+ " layer "+ coincidences.get(i).GetLayer()+" component " + coincidences.get(i).GetComponent());
				}
				return coincidences;
			}
			else {
				return new ArrayList<BandHit>();
			}
		 

		}  // closes if candidates array has non-zero entries...

		return coincidences; //this array is empty since the if condition for a non-zero of candidates was not true.

	} // findHits function		
	
	public ArrayList<BandHit> advancedHitFinder(ArrayList<BandHit> coincidences) 
	{
		
		/** author:  F.Hauenstein
		 * This function implements better reconstruction methods for BandHits. It is called from BandHitFinder::findGoodHits function
		 * so that all hits given to this function are already calibrated and coincidences on a bar
	 	 */
		
		ArrayList<BandHit> betterHits = new ArrayList<BandHit>();      // array list of all coincidence hits in BAND with no veto fired

		//
		
		return betterHits;
	}
	
	

} // BandHitFinder
