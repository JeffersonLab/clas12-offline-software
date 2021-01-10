package org.jlab.rec.band.hit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.constants.Parameters;


public class BandHitFinder {

	public BandHitFinder(){
		// empty constructor
	}

	/** author:  Efrain Segarra, Florian Hauenstein.
	 This class contains the core of the code. The find hits method reconstruct good band hits from raw hits
	 using various cuts and matching.
	 For the start good hits are defined with only up to 5 coincidence hit in the active area of a BAND area and no 
	 hit in the veto counters.
		TODO:
			- more sophisticated stuff for vetoing
			- keeping track of pmts that have already been matched
	*/

	public ArrayList<BandHit> findGoodHits(ArrayList<BandHitCandidate> candidates) {

		// array list of all coincidence hits in BAND with no veto fired
		ArrayList<BandHit> coincidences = new ArrayList<BandHit>();  
		Map<Integer,Integer> hasMatch 	= new HashMap<Integer,Integer>();

		boolean hasvetohit = false;
		//System.out.println("***** STARTING HIT CANDIDATE SKIMMER *****");
		//System.out.flush();
		// Loop through the candidates array to find possible combinations of left and right.
		if(candidates.size() > 0) {

			double xposHit = -1; 		// Position along the bar, determined from time difference
			double yposHit = -1;        // Position in vertical direction, determined from component and middle of the bar
			double zposHit = -1;        // Position along the beam direction, determined from fired bar and distance measurements stored in Parameters.layerGap/zOffset
			double xposHitUnc = -1; 	// Uncertainty in position along the bar, 
			double yposHitUnc = -1;     // Uncertainty in position in vertical direction, 
			double zposHitUnc = -1;     // Uncertainty in position along the beam direction

			for(int i = 0; i < (candidates.size()); i++) {	

				BandHitCandidate hit1 = candidates.get(i);

				//check if hit is in the veto counter (layer 6). 
				if (hit1.GetLayer() == 6) {
					//System.out.println("Candidate hit is veto hit");
					//System.out.flush();
					//  Vetos only have one PMT, so call them 'left' and add them to our
					//  coincidence list and move on
					
					BandHit Hit = new BandHit();  

					int sector 		= hit1.GetSector();    
					int layer 		= hit1.GetLayer(); 
					int component 		= hit1.GetComponent(); 

					double tdcleft 		= hit1.GetTimeCorr();
					double ftdcleft 	= hit1.GetFtdc();
					double adcleft 		= hit1.GetAdc();

					Hit.SetSector(sector);
					Hit.SetLayer(layer);
					Hit.SetComponent(component);
				
					Hit.SetMeanTime_TDC(0.);
					Hit.SetMeanTime_FADC(0.);
					Hit.SetDiffTime_TDC(0.);
					Hit.SetDiffTime_FADC(0.);
					Hit.SetAdcLeft(adcleft);
					Hit.SetAdcRight(0.);
					Hit.SetTLeft_FADC(ftdcleft);
					Hit.SetTRight_FADC(0.);
					Hit.SetTLeft_TDC(tdcleft);
					Hit.SetTRight_TDC(0.);
					Hit.SetX(0.);
					Hit.SetY(0.);
					Hit.SetZ(0.);
					Hit.SetUx(0.);
					Hit.SetUy(0.);
					Hit.SetUz(0.);
					
					if (hit1.GetSide() == 1) { //Veto has a left side PMT
						Hit.SetIndexLpmt(i);
						Hit.SetIndexRpmt(0);
					}
					else if (hit1.GetSide() == 2) { //Veto has a right side PMT
						Hit.SetIndexLpmt(0);
						Hit.SetIndexRpmt(i);
					}
					else { 
						System.err.println("BAND HIT FINDER. Side of Veto PMT could not be assigned");
						continue;
					}
					

					// Print for debugging:
					//Hit.Print();
					hasvetohit = true;
					coincidences.add(Hit);
					continue;
					//return new ArrayList<BandHit>();
				}

				// for each hit get sector, component and layer to check if there 
				// is an associated hit on the other side of the bar
				int sector 		= hit1.GetSector();    
				int layer 		= hit1.GetLayer(); 
				int component 		= hit1.GetComponent(); 
				int side 		= hit1.GetSide();
				int barKey 		= sector*100+layer*10+component;

				// Now loop through the candidates again and match any which has same sector,layer,component but
				// different side. Off-set the start of the list to make sure no repeats:
				for (int j = i+1; j < candidates.size(); j++) {	
					BandHitCandidate hit2 = candidates.get(j);   

					if (hit2.GetLayer() == 6) continue; // skip any veto because cannot pair them

					if (hit2.GetSector() != sector ) continue;       // both must be in the same sector
					if (hit2.GetLayer() != layer) continue;          // both must be in the same layer
					if (hit2.GetComponent() != component) continue;  // both must have the same component

					// Sanity check if side differs by one (there should be no multi hits stored)
					if (Math.abs(hit2.GetSide() - side) != 1) continue;

					//System.err.println("\tWiill attempt to pair s l c o " + (barKey*10+side) + " with " + (hit2.GetSector()*1000+hit2.GetLayer()*100+hit2.GetComponent()*10+hit2.GetSide()));

					double tdcleft = -1;
					double tdcright = -1;
					double adcleft = -1;
					double adcright = -1;
					double amplleft = -1;
					double amplright= -1;
					float ftdcleft = -1;
					float ftdcright = -1;
					int indexleft  = -1;
					int indexright = -1;
					if (hit1.GetSide() == 1) { //Hit1 is from left side PMT
						tdcleft 	= hit1.GetTimeCorr();
						tdcright 	= hit2.GetTimeCorr();
						ftdcleft 	= hit1.GetFtdc();
						ftdcright 	= hit2.GetFtdc();
						adcleft 	= hit1.GetAdc();
						adcright 	= hit2.GetAdc();
						amplleft	= hit1.GetAmpl();
						amplright	= hit2.GetAmpl();
						indexleft 	= i;
						indexright	= j;
					}
					else if (hit1.GetSide() == 2) { //Hit1 is from right side PMT
						tdcleft 	= hit2.GetTimeCorr();
						tdcright 	= hit1.GetTimeCorr();
						ftdcleft 	= hit2.GetFtdc();
						ftdcright 	= hit1.GetFtdc();
						adcleft 	= hit2.GetAdc();
						adcright 	= hit1.GetAdc();
						amplleft	= hit2.GetAmpl();
						amplright	= hit1.GetAmpl();
						indexright	= j;
						indexleft	= i;
					}
					else { 
						System.err.println("BAND HIT FINDER. Found two hits with left and right side but can not assign which hide belongs to which side");
						continue;
					}
					//System.out.println("\tCandidate hit information ordered as:");
					//System.out.println("\t\ttdcleft = "	+ tdcleft);
					//System.out.println("\t\tftdcleft = "	+ftdcleft);
					//System.out.println("\t\tadcleft = "	+ adcleft );
					//System.out.println("\t\tamplleft = "	+amplleft );
					//System.out.println("\t\ttdcright = "	+ tdcright);
					//System.out.println("\t\tftdcright = "	+ftdcright);
					//System.out.println("\t\tadcright = "	+ adcright );
					//System.out.println("\t\tamplright = "	+amplright );
					//System.out.flush();



					// -----------------------------------------------------------------------------------------------
					// First correction to apply is time-walk on TDC (FADC is assumed to have no TW correction):
					// 	for the left PMT:
					double time_walk_paramsL[] = CalibrationConstantsLoader.TIMEWALK_L.get( Integer.valueOf(barKey) );
					double parA1_L = time_walk_paramsL[0];
					double parB1_L = time_walk_paramsL[1];
					double parC1_L = time_walk_paramsL[2];
					double parA2_L = time_walk_paramsL[3];
					double parB2_L = time_walk_paramsL[4];
					double parC2_L = time_walk_paramsL[5];
					//System.out.println("Hit info before TW left: "+tdcleft+" "+parA1_L+" "+parB1_L+" "+parC1_L);
					tdcleft = tdcleft - ( parA1_L + parB1_L / Math.pow( amplleft , parC1_L ) ) - ( parA2_L + parB2_L / Math.pow( amplleft , parC2_L ) );
					//System.out.println("\t\tafter: "+tdcleft);
					//	for the right PMT:
					double time_walk_paramsR[] = CalibrationConstantsLoader.TIMEWALK_R.get( Integer.valueOf(barKey) );
					double parA1_R = time_walk_paramsR[0];
					double parB1_R = time_walk_paramsR[1];
					double parC1_R = time_walk_paramsR[2];
					double parA2_R = time_walk_paramsR[3];
					double parB2_R = time_walk_paramsR[4];
					double parC2_R = time_walk_paramsR[5];
					//System.out.println("Hit info before TW right: "+tdcright+" "+parA1_R+" "+parB1_R+" "+parC1_R);
					tdcright = tdcright - ( parA1_R + parB1_R / Math.pow( amplright , parC1_R ) ) - ( parA2_R + parB2_R / Math.pow( amplright , parC2_R ) );
					//System.out.println("\t\tafter: "+tdcright);
					
					// OLD Time-walk correction
					/*
					double time_walk_paramsL[] = CalibrationConstantsLoader.TIMEWALK_L.get( Integer.valueOf(barKey) );
					double parA_L = time_walk_paramsL[0];
					double parB_L = time_walk_paramsL[1];
					tdcleft  = tdcleft - (parA_L/Math.sqrt(adcleft ) + parB_L);
					double time_walk_paramsR[] = CalibrationConstantsLoader.TIMEWALK_R.get( Integer.valueOf(barKey) );
					double parA_R = time_walk_paramsR[0];
					double parB_R = time_walk_paramsR[1];
					tdcright = tdcright - (parA_R/Math.sqrt(adcright) + parB_R);
					// -----------------------------------------------------------------------------------------------
					*/					
					
					
					
					// -----------------------------------------------------------------------------------------------
					// Next we form (L-R) time and correct for the offset between the two for both TDC and FADC times:
					double tdiff_tdc  = (tdcleft - tdcright) - CalibrationConstantsLoader.TDC_T_OFFSET.get( Integer.valueOf(barKey) );
					double tdiff_fadc = (ftdcleft - ftdcright) - CalibrationConstantsLoader.FADC_T_OFFSET.get( Integer.valueOf(barKey) );
					//System.out.println("Offset: "+CalibrationConstantsLoader.TDC_T_OFFSET.get( Integer.valueOf(barKey) ));



					// -----------------------------------------------------------------------------------------------
					// Now we can load effective velocity in the bar and require that the time difference is 
					// less than the max allowed for the bar length with 10% safety net on the edge of the bar:
					double maxDiff_tdc = Parameters.barLengthSector[sector-1]/
						CalibrationConstantsLoader.TDC_VEFF.get( Integer.valueOf(barKey) );
					double maxDiff_fadc = Parameters.barLengthSector[sector-1]/
						CalibrationConstantsLoader.FADC_VEFF.get( Integer.valueOf(barKey) );
					if( Math.abs(tdiff_tdc)  > 1.1*maxDiff_tdc )continue;
					if( Math.abs(tdiff_fadc) > 1.1*maxDiff_fadc )continue;


					// -----------------------------------------------------------------------------------------------
					// Form the mean time for TDC and FADC and fix any offsets that are different layer-by-layer and 
					// paddle-by-paddle, which are given by the laser calibration runs. Then also subtract off a global
					// offset per bar which is a final re-calibration to the photon peak for each bar for each run number
					double mtime_tdc =
							( tdcleft + tdcright )/2. 
							  - Math.abs(CalibrationConstantsLoader.TDC_T_OFFSET.get( Integer.valueOf(barKey) ))/2. 
							  - CalibrationConstantsLoader.TDC_MT_P2P_OFFSET.get(Integer.valueOf(barKey) ) 
							  - CalibrationConstantsLoader.TDC_MT_L2L_OFFSET.get(Integer.valueOf(barKey) ) 
							  - CalibrationConstantsLoader.TDC_GLOB_OFFSET.get(Integer.valueOf(barKey) );
					double mtime_fadc = 
							( ftdcleft + ftdcright )/2.
							 - Math.abs(CalibrationConstantsLoader.FADC_T_OFFSET.get( Integer.valueOf(barKey) ))/2.
							 - CalibrationConstantsLoader.FADC_MT_P2P_OFFSET.get(Integer.valueOf(barKey) )
							 - CalibrationConstantsLoader.FADC_MT_L2L_OFFSET.get(Integer.valueOf(barKey) ) 
							 - CalibrationConstantsLoader.FADC_GLOB_OFFSET.get(Integer.valueOf(barKey) );
					//System.out.println("Global offsets: " + CalibrationConstantsLoader.TDC_GLOB_OFFSET.get(Integer.valueOf(barKey) )+" "+CalibrationConstantsLoader.FADC_GLOB_OFFSET.get(Integer.valueOf(barKey) ));

					// -----------------------------------------------------------------------------------------------
					// Using the effective velocity for each bar, get the position of the hit in x based on the TDC time
					// and then form the global position in the lab system:
					double xpos_tdc =  (-1./2.)* tdiff_tdc * CalibrationConstantsLoader.TDC_VEFF.get( Integer.valueOf(barKey) );
					double xpos_fadc = (-1./2.)* tdiff_fadc * CalibrationConstantsLoader.FADC_VEFF.get( Integer.valueOf(barKey) );
					//OLD xposHit = (xpos_tdc+xpos_fadc)/2.;
					xposHit = xpos_tdc;

					Double[] globPos = Parameters.barGeo.get( Integer.valueOf(barKey) );
					xposHit += globPos[0];
					yposHit = globPos[1];
					zposHit = globPos[2];
					xposHitUnc = 0.5 * CalibrationConstantsLoader.TDC_VEFF.get( Integer.valueOf(barKey) ) * 0.3; // Estimation of error, not perfect
					yposHitUnc = Parameters.thickness / Math.sqrt(12.);
					zposHitUnc = Parameters.thickness / Math.sqrt(12.);



					// -----------------------------------------------------------------------------------------------
					// Correct FADC ADC for attenuation length
					double sectorLen = Parameters.barLengthSector[sector-1];
					double mu_cm = CalibrationConstantsLoader.FADC_ATTEN_LENGTH.get( Integer.valueOf(barKey) ); // in [cm]
					double adcL_corr = adcleft * Math.exp( (sectorLen/2.-xpos_fadc) / mu_cm );
					double adcR_corr = adcright* Math.exp( (sectorLen/2.+xpos_fadc) / mu_cm );
				
					// -----------------------------------------------------------------------------------------------
					// Create a new BandHit and fill it with the relevant info:
					BandHit Hit = new BandHit();  

					Hit.SetSector(sector);
					Hit.SetLayer(layer);
					Hit.SetComponent(component);

					Hit.SetMeanTime_TDC(mtime_tdc);
					Hit.SetMeanTime_FADC(mtime_fadc);

					Hit.SetDiffTime_TDC(tdiff_tdc);
					Hit.SetDiffTime_FADC(tdiff_fadc);

					Hit.SetAdcLeft(adcL_corr);
					Hit.SetAdcRight(adcR_corr);
					

					Hit.SetTLeft_FADC(ftdcleft);
					Hit.SetTRight_FADC(ftdcright);

					Hit.SetTLeft_TDC(tdcleft);
					Hit.SetTRight_TDC(tdcright);

					Hit.SetX(xposHit);
					Hit.SetY(yposHit);
					Hit.SetZ(zposHit);
					Hit.SetUx(xposHitUnc);
					Hit.SetUy(yposHitUnc);
					Hit.SetUz(zposHitUnc);

					Hit.SetIndexLpmt(indexleft);
					Hit.SetIndexRpmt(indexright);

					coincidences.add(Hit);
					break; // Found a hit match for this bar, so let's move on to the next possible bar
				

				}  // close loop over j
			} // close loop over i  		

			// At this stage an array of coincidence hits from type BandHit exists. Now we can skim those coincidence
			// hits for some neutral candidate particles
			if( coincidences.size() > 0 ) return advancedHitFinder(coincidences, hasvetohit);

			else{ return new ArrayList<BandHit>(); }


		}  // closes if candidates array has non-zero entries...

		return coincidences; //this array is empty since the if condition for a non-zero of candidates was not true.

	} // findHits function		

	public ArrayList<BandHit> advancedHitFinder(ArrayList<BandHit> coincidences, boolean hasvetohit) 
	{

		/** author:  Efrain Segarra, Florian Hauenstein
		 * Currently this function sets status flags for EACH hit in an event. This depends on the length of the BandHit array and
		 * 	if a veto hit was found in the data from the findGoodHits function. Laserhits will get status 1. The rest either 0, 2 or 3
		 **/


		ArrayList<BandHit> betterHits = new ArrayList<BandHit>();
		int status_temp = -1; 
		//Set status bits for each hit, good hit status is 0 for each hit when #hits is < bandhits (standard 5).
		//If #hits > laserhitcutvalue (standard 100), status will be set to 1 (laser hits), other possibilities it will be 2
		//If there will be a veto hit the status values are changed to 3 from 0 or 2 (exlude if laser hit
		
		if (coincidences.size() > CalibrationConstantsLoader.CUT_LASERHITS_BAND) { status_temp = 1; } //laser hits
		else if (coincidences.size() < CalibrationConstantsLoader.CUT_NHITS_BAND) { status_temp = 0; } //coincidences.size > 0 is already checked before advanceHitFinder is called
		else { status_temp = 2; } //cosmics or other data, no veto hit
		
		//if hasvetohit flag is true and it is not a laser hit set status to 3 for all hits
		if (hasvetohit && status_temp != 1) { status_temp = 3; }
		
		
		for( int hit = 0 ; hit < coincidences.size() ; hit++){
			BandHit thisHit = coincidences.get(hit);
			thisHit.SetStatus(status_temp);	
			betterHits.add( thisHit );
		}
		return betterHits;
	}



} // BandHitFinder
