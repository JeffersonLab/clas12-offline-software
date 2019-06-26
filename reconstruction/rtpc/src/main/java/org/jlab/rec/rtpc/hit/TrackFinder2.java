package org.jlab.rec.rtpc.hit;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import org.jlab.groot.data.*;
import org.jlab.groot.fitter.*;
import org.jlab.groot.graphics.*;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;
//import org.jlab.clas.physics.Vector3;



public class TrackFinder2 {
	
	/***************************************
	 * Author: David Payette
	 * 
	 * The function FindTrack2 is divided into 3 main parts, the sorting algorithm, the merging algorithm,
	 * and plotting. The sorting algorithm builds a map call TIDMap. It starts empty and will be filled with
	 * track IDs (TIDs), each one returning a map of time slices (120 ns) that each contain a vector of pads which were sorted
	 * together in this time slice. So TIDMap.get(1).get(120).get(3) will give you the fourth pad at 120 ns that has TID 1.
	 * The sorting algorithm contains a series of nested loops as follows: The TIMELOOP loops through values of time starting
	 * at 0 and moving in steps of 120 ns up to the TrigWindSize (10000 ns). Then there is a PADLOOP which loops through all 
	 * the pads which saw a signal in the simulation. The signal shape is created in PadHit for now since GEMC does not do signal 
	 * simulation for our purposes yet. This signal is then integrated into 120 ns bins which is based on the Dream chip timing.
	 * For each pad in the loop, it checks for the ADC value at the current time,
	 * and if it passes an ADC threshold, then it moves on to be sorted. The TIDLOOP loops through all elements of a vector TIDVec,
	 * which initially only contains TID 1 and will add TIDs as necessary. If the TIDMap does not contain the current TID in the 
	 * vector then it adds it to the map. Otherwise, the current pad in the loop is checked against all other pads assigned to 
	 * the current TID by comparing the distance between the pads to a preset ellipse formula. This check is done for the current
	 * time slice, and a set number (timeadj) of previous time slices. If a pad is close enough to another pad with an assigned
	 * TID then it is also assigned this TID. A pad is allowed to be assigned multiple TIDs at this stage, which will flag the 
	 * merging algorithm to remember to merge these TIDs later after the sort. If a pad makes it to the end of the TIDLOOP and still
	 * has not been sorted then it will be assigned a new TID as the new TID is added to the map. The merging algorithm looks 
	 * for TIDs which were flagged to be merged during sort and combines them all into the lowest numbered TID and then the 
	 * other TIDs are removed from the map. 
	 * 
	 * TIMELOOP: 0->10000 in 120 ns steps
	 * {
	 *      PADLOOP: All pads which have an ADC value that passes threshold for current time
	 *      {
	 *           TIDLOOP: Loop through TIDs in TIDVec
	 *           if(TID is in map){ Compare pad to other pads (PADINDEXLOOP) in current and previous times (PREVTIMELOOP) and see 
	 *           if it is near enough (using ellipse formula) }
	 *           else{ If pad has not been assigned at least one TID, assign it a new one and add it to the map }
	 *           
	 *           if(pad has more than one TID){ group the TIDs into MapCombine, using the smallest one as the key }	
	 *      }
	 * }				
	 * 
	 * Loop over the keys of MapCombine, and take all the TIDs assigned to each key and merge them into the key TID and 
	 * remove them from the TIDMap. 
	 * 
	 * Plot a bunch of stuff for testing. 
	 * 
	 * The ellipse formula looks like this:
	 * 
	 * (dx^2 + dy^2)/phi_0^2 + (dz^2)/z_0^2 <= adjthresh^2
	 * 
	 * dx, dy, and dz are the distances between two pads, phi_0 and z_0 are set parameters that define the ellipse,
	 * and adjthresh is the maximum level of adjacency we allow. This determines how many rings of adjacent pads are 
	 * close enough.
	 ****************************************/
	
	public void FindTrack2(HitParameters params, boolean draw){
		/***************************************
		 * 	
		 * INITIALIZE VARIABLES
		 * 
		 ****************************************/
		//HashMap<Integer, HashMap<Integer, int[]>> TIDMap = new HashMap<Integer, HashMap<Integer, int[]>>();
		//HashMap<Integer, double[]> PadThresh = new HashMap<Integer, double[]>();
		//HashMap<Integer, HashMap<Integer,Vector<Integer>>> FixedTIDMap = new HashMap<Integer, HashMap<Integer,Vector<Integer>>>();
		//double PadPhistore = 0;
		//double PadZstore = 0;
		//boolean PadPhiChanged = false;
		//Necessary pad variables
		//double PAD_W = 2.79; //in mm
		//double PAD_S = 80.0; //in mm
		//double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
		//int padindexmax = 0; Not being used currently
		//double time1 = System.currentTimeMillis();
		/* Not being used currently
		 * int maxconcpads = 0;
		 * int concpads = 0;
		 * int maxconctime = 0;
		 */

		
		
		HashMap<Integer, double[]> ADCMap = params.get_R_adc(); //Key - Cell ID, Value - Array of signal height over time slices		
		
		/**
		 * TIDMap contains a HashMap of Vectors
		 * The key is Track ID (TID), which returns a map
		 * The key of the returned map is Time, which returns a vector
		 * The elements of the vector are pads 
		 * This stores all pads in each time slice, and all time slices for each TID
		 * Example :: Track ID 1, Time 120 contains pads: 3,4,5,7 so using the call
		 * TIDMap.get(1).get(120).get(3) will give you pad 5
		 */
		HashMap<Integer, HashMap<Integer,Vector<Integer>>> TIDMap = new HashMap<Integer, HashMap<Integer,Vector<Integer>>>();
		
		/**
		 * strkTIDMap uses the same structure as TIDMap but is used at the end to isolate
		 * TIDs which only contain single tracks. This map is all TIDs not sent to the disentangler
		 */
		HashMap<Integer, HashMap<Integer,Vector<Integer>>> strkTIDMap = new HashMap<Integer, HashMap<Integer,Vector<Integer>>>();

		
		Vector<Integer> PadNum = params.get_PadNum(); 	//Contains pads used in PadHit
		Vector<Integer> TIDVec = new Vector<>(); 	 	//Contains all TIDs and is added to throughout,
		TIDVec.add(1);                                	//Starts with TID 1
		
		
		int Pad = 0; 									//initializing pad
		double ADC = 0; 									//initializing ADC
		int TrigWindSize = params.get_TrigWindSize(); 	//Trigger Window Size = 10000
		//int StepSize = params.get_StepSize();
		int StepSize = 120; 								//Time stepsize in ns
		double adcthresh = 5e-4; 						//Arbitrary ADC threshold
				
		int min_pads = 0; //Minimum number of pads required to keep as a track
		
		//These vectors will contain x,y,z for each pad being checked
		//Vector3 TempPadCoords = new Vector3(); 
		//Vector3 CheckPadCoords = new Vector3();
		//Vector3 CheckPadPrevCoords = new Vector3();
		
		PadVector TempPadCoords;// = new PadVector(); 
		PadVector CheckPadCoords;
		PadVector CheckPadPrevCoords;
		
		int TID = 0; //initialize TID
				
		//used for plotting
		double plotphi = 0; 
		double plotz = 0;
				
		int timeadj = 2; 					//How many previous time slices to check in algorithm
		int event = params.get_eventnum(); 	//event number only used for file number
		
		
		//Initialize Ellipse Variables used for adjacency checks
		double adjthresh = 12; 				//sqrt(right hand side of ellipse formula)
		double EllipseDeltax = 0; 			//dx
		double EllipseDeltay = 0; 			//dy
		double EllipseDeltaz = 0; 			//dz
		double EllipseTotal = 0;  			//left hand side of ellipse formula
		double PhiDelta = 16; 	//phi_0
		double ZDelta = 36;  	//z_0
		
		double phithresh = 0.15;
		double zthresh = 10;
		
		//set variables for timing cut (currently time cut is not used, cuts on time happen later)
		int tmin = 0; 		//min timing in ns
		int trange = 20000; 	//added to min to calculate max in ns
		int tmax = tmin+trange;
			
		//Maps and variables used for combining TIDs later in the merge algorithm
		boolean PadSorted = false;
		Vector<Integer> PadTIDcollect = new Vector<Integer>();
		Vector<Integer> TIDcombine = new Vector<Integer>();
		HashMap<Integer,Vector<Integer>> MapCombine = new HashMap<Integer,Vector<Integer>>();
		int mapkey = 0;
		
		//Map of graphs of ADC vs Time
		HashMap<Integer,GraphErrors> gADCvsT = new HashMap<Integer,GraphErrors>();
			
		//g.setTitleX("Phi");
		//g.setTitleY("Z");
		//g.setMarkerSize(5);

		try {
			File f = new File("/Users/davidpayette/Documents/FileOutput/Output" + event + ".txt");
			f.delete();
			FileWriter write2 = new FileWriter("/Users/davidpayette/Documents/FileOutput/Output" + event + ".txt",true);
			//File f2 = new File("/Users/davidpayette/Documents/FileOutput/Master.txt");
			FileWriter write3 = new FileWriter("/Users/davidpayette/Documents/FileOutput/Master.txt",true);
		//Collections.sort(PadNum);
		//H1F t2 = new H1F("t2","t2",TrigWindSize/StepSize,0,TrigWindSize);
			
		GraphErrors t1 = new GraphErrors();
		int counter = 0;
		HashMap<Integer,Integer> countermap = new HashMap<Integer,Integer>();
			
		/***************************************
		 * 	
		 * SORTING ALGORITHM
		 * 
		 ****************************************/
		/**loop over all times in 120 ns slices**/
		TIMELOOP: //these labels are not used for the most part, but help to label the loops
		for(int t = 0; t < TrigWindSize; t += StepSize)
		{
			//System.out.println("Time is " + t);
			//concpads = 0;
			
			/**loop over all pads for each time slice**/
			int padloopsize = PadNum.size();
			PADLOOP:
			for(int p = 0; p < padloopsize; p++)
			{
				
				PadSorted = false;
				PadTIDcollect.clear();
				Pad = PadNum.get(p);

				//System.out.println(PadNum.size() + " size");
				//System.out.println(Pad + " " + t);
				
				ADC = ADCMap.get(Pad)[t];
				
				/**only pads which have an ADC value above threshold will be assigned a TID**/
				if(ADC > adcthresh)
				{
					//write3.write(ADC + "\n");
					/**This is used for testing something else**/
					if(Pad == 11091)
					{
						t1.addPoint(t, ADCMap.get(Pad)[t], 0, 0);
						//t2.fill(t);
					}
				    if(!countermap.containsKey(Pad))
					{
						countermap.put(Pad, 1);
					}
					else
					{
						counter = countermap.get(Pad);
						counter++;
						countermap.put(Pad,counter);
					}
								    
				    //Plots ADC vs T for each Pad
					if(!gADCvsT.containsKey(Pad))
					{
						gADCvsT.put(Pad, new GraphErrors());
						gADCvsT.get(Pad).addPoint(t, ADCMap.get(Pad)[t], 0, 0);
					}
					else
					{
						gADCvsT.get(Pad).addPoint(t, ADCMap.get(Pad)[t], 0, 0);
					}
					/*******************************************/
					//System.out.println("Pad to be checked " + Pad + " checked at time " + t);
					
					//returns x,y,z of the current Pad
					//Vector3 PadCoords = PadCoords(Pad);
					PadVector PadCoords = params.get_padvector(Pad);
					
					//System.out.println("Pad " + Pad + " has row and column values " + PadPhi + " " + PadZ);
					//g.addPoint(PadPhi, PadZ, 0, 0);
					
					/**loop through all TID's in a vector which will grow to include all future TID's**/
					int tidloopsize = TIDVec.size();
					TIDLOOP:
					for(int i = 0; i < tidloopsize; i++)
					{	
						TID = TIDVec.get(i); //returns current TID to sort into
						//System.out.println("Current TID " + TID);
						
						/**if TID is already in the map**/
						if(TIDMap.containsKey(TID))
						{							
							//System.out.println("TID " + TID + " is already in the map");
							
							/**loop through all pads in TIDMap and compare there row and column to current Pad**/
							PADINDEXLOOP:
							for(int padindex = 0; padindex < 100; padindex++) //TODO 100 is unnecessary placeholder
							{
								//System.out.println("Pad index is " + padindex);
								if(padindex < TIDMap.get(TID).get(t).size())
								{
									//CheckPadCoords = PadCoords(TIDMap.get(TID).get(t).get(padindex));	//Get the x,y,z of the pad we are checking against
									CheckPadCoords = params.get_padvector(TIDMap.get(TID).get(t).get(padindex));
									//System.out.println("At TID " + TID + " time " + t + " padindex " + padindex + " the check pad's location is " + checkpadphi + " " + checkpadz);
									//System.out.println("Current time " + checkpadphi + " " + PadPhi + " " + checkpadz + " " + PadZ + " " + Math.abs(checkpadphi-PadPhi) + " " + Math.abs(checkpadz - PadZ));
									//System.out.println("Current time slice " + PadPhi + " " + PadZ + " " + " " + checkpadphi + " " + checkpadz);
									//System.out.println("The comparison values for the pad to be sorted at current time are " + Math.abs(checkpadphi-PadPhi) + " " + Math.abs(checkpadz - PadZ));
									
									/**Check current time slice for adjacency**/
									/**Calculates the ellipse parameters**/
									EllipseDeltax = Math.abs(PadCoords.x()-CheckPadCoords.x())*Math.abs(PadCoords.x()-CheckPadCoords.x());
									EllipseDeltay = Math.abs(PadCoords.y()-CheckPadCoords.y())*Math.abs(PadCoords.y()-CheckPadCoords.y());
									EllipseDeltaz = Math.abs(PadCoords.z()-CheckPadCoords.z())*Math.abs(PadCoords.z()-CheckPadCoords.z());
									EllipseTotal = ((EllipseDeltax+EllipseDeltay)/PhiDelta) + (EllipseDeltaz/ZDelta);
									//System.out.println("Ellipse is " + EllipseTotal);
									
									/**
									 * If the ellipse equation falls within the accepted size assign the 
									 * TID of the checked pad to the current pad in the loop
									 **/
									//if(EllipseTotal <= adjthresh)
									if((Math.abs(CheckPadCoords.phi()-PadCoords.phi())<phithresh || (Math.abs(CheckPadCoords.phi()-PadCoords.phi()-2*Math.PI) < phithresh )) && Math.abs(CheckPadCoords.z()-PadCoords.z())<zthresh)
									{
										//System.out.println("Found 2");
										//System.out.println("Sorted Current");
										
										//Making sure that we aren't adding a pad to this time slice that is already there
										if(!TIDMap.get(TID).get(t).contains(Pad))
										{
											TIDMap.get(TID).get(t).add(Pad);
											PadTIDcollect.add(TID);
											//System.out.println(Pad + " got assigned " + TID + " at time " + t);
											PadSorted = true; //flag for the end of the TID loop
											break PADINDEXLOOP; //Stops checking current pad list for this TID since it's already sorted
										}
									}
									//else {System.out.println("Failed sorting test current");}
								}
								
								//System.out.println("new " + padindex);
								/**Check previous time slice(s) for adjacency**/
								if(t>0)
								{
									/**Loops through a variable number (timeadj) of previous time slices to see if it is adjacent to earlier pads**/
									PREVTIMELOOP:
									for(int prevtime = t - StepSize; (prevtime >= (t - (timeadj*StepSize))) && (prevtime >= 0); prevtime -= StepSize)
									{
										if(padindex < TIDMap.get(TID).get(prevtime).size())
										{					
											//CheckPadPrevCoords = PadCoords(TIDMap.get(TID).get(prevtime).get(padindex));	//Get the x,y,z of the pad we are checking against
											CheckPadPrevCoords = params.get_padvector(TIDMap.get(TID).get(prevtime).get(padindex));
											/**Calculates the ellipse parameters**/
											EllipseDeltax = Math.abs(PadCoords.x()-CheckPadPrevCoords.x())*Math.abs(PadCoords.x()-CheckPadPrevCoords.x());
											EllipseDeltay = Math.abs(PadCoords.y()-CheckPadPrevCoords.y())*Math.abs(PadCoords.y()-CheckPadPrevCoords.y());
											EllipseDeltaz = Math.abs(PadCoords.z()-CheckPadPrevCoords.z())*Math.abs(PadCoords.z()-CheckPadPrevCoords.z());
											EllipseTotal = ((EllipseDeltax+EllipseDeltay)/PhiDelta) + (EllipseDeltaz/ZDelta);
											//System.out.println("Ellipse is " + EllipseTotal);
											
											//System.out.println("Previous time slice " + PadPhi + " " + PadZ + " " + " " + checkpadphiprev + " " + checkpadzprev);
											//System.out.println("The comparison values for the pad to be sorted at previous time are " + Math.abs(checkpadphiprev-PadPhi) + " " + Math.abs(checkpadzprev - PadZ));
											/**
											 * If the ellipse equation falls within the accepted size assign the 
											 * TID of the checked pad to the current pad in the loop
											 **/
											//if(EllipseTotal <= adjthresh)
											if((Math.abs(CheckPadPrevCoords.phi()-PadCoords.phi())< phithresh || (Math.abs(CheckPadPrevCoords.phi()-PadCoords.phi()-2*Math.PI) < phithresh )) && Math.abs(CheckPadPrevCoords.z()-PadCoords.z())<zthresh)

											{
												//System.out.println("Found 3");
												//System.out.println("Sorted prev");
												
												//Making sure that we aren't adding a pad to this time slice that is already there
												if(!TIDMap.get(TID).get(t).contains(Pad))
												{
													PadTIDcollect.add(TID);
													//System.out.println(Pad + " got assigned " + TID + " at time " + t);
													TIDMap.get(TID).get(t).add(Pad);
													PadSorted = true;   //flag for the end of the TID loop
													break PADINDEXLOOP; //Stops checking current pad list for this TID since it's already sorted
												}
											}
											//else {System.out.println("Failed sorting test previous");}
										}
									} // End PREVTIMELOOP
								}
							} // End PADINDEXLOOP					
						}
						/**TID not already in map**/
						else
						{
							//System.out.println("TID not currently in map " + TID + " " + t + " " + Pad);
							/**Assigns the pad we are checking to this TID only if it never got assigned a new TID above**/
							if(!PadSorted)
							{
								TIDMap.put(TID, new HashMap<Integer, Vector<Integer>>());
								for(int time = 0; time < TrigWindSize; time += StepSize)
								{
									TIDMap.get(TID).put(time, new Vector<>()); //add TID to map
								}
								TIDMap.get(TID).get(t).add(Pad);
								//PadTIDcollect.add(TID);
								TIDVec.add(TID+1); //add TID to the list
							}							
							break TIDLOOP; //makes sure the TID loop doesn't go forever since we are adding a new TID to the list
						}
					} // End TIDLOOP //
					
					/** Pad has more than one TID in this time slice **/
					if(PadTIDcollect.size()>1) 
					{
						
						//System.out.println("PadTIDcollect is " + PadTIDcollect.size());
						/*for(int i : MapCombine.keySet())
						{
							if(MapCombine.get(i).size()>0)
							{
								for(int jZvsPhi = 0; jZvsPhi<MapCombine.get(i).size();jZvsPhi++)
								{
									TIDcombine.add(MapCombine.get(i).get(jZvsPhi));
									//System.out.println(i + " " + jZvsPhi + " " + MapCombine.get(i).get(jZvsPhi));
								}
							}
						}*/
						//System.out.println("done");
						
						Collections.sort(PadTIDcollect); //Orders the TIDs
						mapkey = PadTIDcollect.get(0);   //Uses the first one to merge into
						
						/**The mapkey is the TID we want all other applicable TIDs merged into later**/
						if(!MapCombine.containsKey(mapkey))
						{
							MapCombine.put(mapkey, new Vector<Integer>());
						}
						//System.out.println("TID to merge " + mapkey);
						
						/**Loops through remaining TIDs to be merged and puts them into a map together**/
						for(int i = 1; i < PadTIDcollect.size(); i++)
						{
							/*if(!TIDcombine.contains(PadTIDcollect.get(i)))
							{								
								MapCombine.get(mapkey).add(PadTIDcollect.get(i));
								//TIDcombine.add(PadTIDcollect.get(i));
							}*/
							if(!MapCombine.get(mapkey).contains(PadTIDcollect.get(i)))
							{
								MapCombine.get(mapkey).add(PadTIDcollect.get(i));
							}
							//System.out.println("TID to merge " + PadTIDcollect.get(i));
						}
						//TIDcombine.clear();
					}
				}
				//else {System.out.println("Pad " + Pad + " failed ADC threshold at time " + t);}
			} // End PADLOOP //
			
		} // End TIMELOOP //
		System.out.println("This event has " + TIDMap.size() + " tracks");
		EmbeddedCanvas chist = new EmbeddedCanvas();
		JFrame jhist = new JFrame();
		jhist.setSize(800, 600);
		if(draw == true)
		{
			chist.draw(t1);
			jhist.add(chist);
			jhist.setVisible(true);
			jhist.setTitle("histo");
		}
		
		
		/***************************************
		 * 	
		 * MERGE ALGORITHM
		 * 
		 ****************************************/	

			//Merge Tracks
		
		int TIDtomerge = 0; //TID we are putting into the TIDtokeep
		int TIDtokeep = 0;
		//Vector<Integer> TIDtoremove = new Vector<Integer>();
		//for(int i : TIDMap.keySet()) {System.out.println("all TIDs before " + i );}
		
		/**
		 * For each TID that needs other TIDs to be merged into it
		 * we get each TID from the Map assigned to the TIDtokeep key and merge the TIDs together
		 * then remove the other TIDs (TIDtomerge) from the map
		 */
		for(int j : MapCombine.keySet())
		{
			if(MapCombine.get(j).size() == 0) {break;}
			TIDtokeep = j;
			for(int i = 0; i < MapCombine.get(j).size(); i++)
			{
				TIDtomerge = MapCombine.get(j).get(i);
				//TIDtoremove.add(TIDtomerge);
				for(int time = 0 ; time < TrigWindSize; time+=StepSize)
				{
					if(TIDMap.containsKey(TIDtomerge) && TIDMap.containsKey(TIDtokeep))
					{
						if(TIDMap.get(TIDtomerge).get(time).size()>0)
						{
							for(int padindex = 0; padindex < TIDMap.get(TIDtomerge).get(time).size(); padindex++)
							{
								TIDMap.get(TIDtokeep).get(time).add(TIDMap.get(TIDtomerge).get(time).get(padindex));
							}
						}
					}
				}
				TIDMap.remove(TIDtomerge);		
			}
			//System.out.println(MapCombine.get(jZvsPhi).size());
		}

		//for(int i = 0; i < TIDtoremove.size(); i++) {TIDMap.remove(TIDtoremove.get(i));}
		//for(int i : TIDMap.keySet()) {System.out.println("all TIDs after " + i );}
		
		/***************************************
		 * 	
		 * PLOTS
		 * 
		 ****************************************/
		//TESTING STUFF
		int countermax = 0;
		int padmax = 0;	
		int howmanyhits = 0;
		for(int i : countermap.keySet())
		{
			howmanyhits += countermap.get(i);
			if(countermap.get(i)>countermax) 
			{
				countermax = countermap.get(i);				
				padmax = i;
				//System.out.println("countermax " + i + " " + countermax);
			}
		}
		System.out.println("this many hits " + howmanyhits + " this many pads " + countermap.size());
		
		
		HashMap<Integer,GraphErrors> gZvsPhi = new HashMap<Integer,GraphErrors>();
		//HashMap<Integer,TCanvas> graphmap2 = new HashMap<Integer,TCanvas>();
		//EmbeddedCanvas tcan = new EmbeddedCanvas();
		//TCanvas tcan = new TCanvas("t", 800, 600);
		for(int testTID : TIDMap.keySet())
		{
			for(int time = 0; time < TrigWindSize; time+=StepSize)
			{
				for(int pad = 0; pad < TIDMap.get(testTID).get(time).size(); pad++)
				{	
					//TempPadCoords = PadCoords(TIDMap.get(testTID).get(time).get(pad)); //gets x,y,z
					TempPadCoords = params.get_padvector(TIDMap.get(testTID).get(time).get(pad));
					//plotphi = Math.atan2(TempPadCoords.y(),TempPadCoords.x());	//Phi
					plotphi = TempPadCoords.phi();
					plotz = TempPadCoords.z(); //Z
					//System.out.println(plotphi + " " + plotz);

					if(!gZvsPhi.containsKey(testTID))
					{
						gZvsPhi.put(testTID, new GraphErrors());
					}

					gZvsPhi.get(testTID).addPoint(plotphi, plotz, 0, 0); //Plots Phi, Z for each track

					
					//write2.write(testTID + "\t" + time + "\t" + plotphi + "\t" + plotz + "\n");
				}
				
				
				/*if(TIDMap.get(testTID).get(a1).size()>0)
				{
					gZvsPhi.get(testTID).addPoint(0, 0, 0, 0);
					gZvsPhi.get(testTID).addPoint(6.5, 20, 0, 0);
					//tcan.draw(gZvsPhi.get(testTID));
					//tcan.save("/Users/davidpayette/Desktop/Plots/" + a1 + ".jpg");
					//tcan.repaint();
					graphmap2.put(a1, new TCanvas(" ",800,600));
					graphmap2.get(a1).draw(gZvsPhi.get(testTID));
					graphmap2.get(a1).save("/Users/davidpayette/Desktop/Plots/" + a1 + ".jpg");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}*/
				
			}
		}
		
		//F1D fitfunc1 = new F1D("f","[0]*x+[1]",0,9000);
		//F1D fitfunc1 = new F1D("p1");
		//fitfunc1.setParameter(0, 1);
		//fitfunc1.setParameter(1,0);
		
		//Vector3 vZvsT = new Vector3();
		GraphErrors gZvsT = new GraphErrors();
		EmbeddedCanvas cZvsT = new EmbeddedCanvas();
		Vector<Integer> PadTracker = new Vector<Integer>();
		JFrame jZvsT = new JFrame();
		jZvsT.setSize(800,600);
		
		/*int choosetid = 0;
		for(int t : TIDMap.get(choosetid).keySet())
		{
			//System.out.println(t);
			for(int p = 0; p<TIDMap.get(choosetid).get(t).size(); p++)
			{
				vZvsT = PadCoords(TIDMap.get(choosetid).get(t).get(p));
				gZvsT.addPoint(t,vZvsT.z(),0,0);
				if(!PadTracker.contains(TIDMap.get(choosetid).get(t).get(p)))
				{
					PadTracker.add(TIDMap.get(choosetid).get(t).get(p));
				}
				if(vZvsT.z() == -11)
				{
				//System.out.println(TIDMap.get(choosetid).get(t).get(p) + " " + vZvsT.z());
				}
			}
		}*/
		HashMap<Integer,EmbeddedCanvas> cADCvsT = new HashMap<Integer,EmbeddedCanvas>();
		GraphErrors gHitsvsPad = new GraphErrors();
		EmbeddedCanvas cHitsvsPad = new EmbeddedCanvas();
		JFrame jHitsvsPad = new JFrame();
		int mod25 = (PadTracker.size() - (PadTracker.size() % 25))/25; 
		//System.out.println(mod25);
		for(int k = 0; k <= mod25; k++)
		{
			cADCvsT.put(k, new EmbeddedCanvas());
			cADCvsT.get(k).divide(5, 5);
		}
		
		//System.out.println("size of PadTracker " + PadTracker.size());
		int numplots = 0;
		int whichplot = 0;
		for(int h = 0; h < PadTracker.size(); h++)
		{
			gHitsvsPad.addPoint(PadTracker.get(h), countermap.get(PadTracker.get(h)), 0, 0);
			cADCvsT.get(whichplot).cd(numplots);
			gADCvsT.get(PadTracker.get(h)).setTitle(""+PadTracker.get(h));
			gADCvsT.get(PadTracker.get(h)).addPoint(0, 0, 0, 0);
			gADCvsT.get(PadTracker.get(h)).addPoint(9000, 0, 0, 0);
			gADCvsT.get(PadTracker.get(h)).setMarkerSize(2);
			cADCvsT.get(whichplot).draw(gADCvsT.get(PadTracker.get(h)));
			if(h % 25 == 0 && h > 0)
			{
				whichplot++;
				numplots=0;
			}
			else
			{
				numplots++;
			}
		}
		if(draw == true)
		{
			gHitsvsPad.setMarkerSize(2);
			cHitsvsPad.draw(gHitsvsPad);
			jHitsvsPad.add(cHitsvsPad);
			jHitsvsPad.setTitle("jHitsvsPad");
			jHitsvsPad.setSize(800, 600);
			jHitsvsPad.setVisible(true);
			HashMap<Integer,JFrame> jADCvsT = new HashMap<Integer,JFrame>();
			for(int k : cADCvsT.keySet())
			{
				jADCvsT.put(k, new JFrame());
				jADCvsT.get(k).setSize(1200,800);
				jADCvsT.get(k).setTitle("jADCvsT" + " " + k);
				jADCvsT.get(k).add(cADCvsT.get(k));
				jADCvsT.get(k).setVisible(true);
			}
			//DataFitter.fit(fitfunc1, gZvsT, "QER");
			//System.out.println(fitfunc1.getChiSquare());
			
			gZvsT.setMarkerSize(1);
			cZvsT.draw(gZvsT);
			//cZvsT.draw(fitfunc1,"same");
			jZvsT.add(cZvsT);
			jZvsT.setVisible(true);
		}
		int tlargest = 0;
		//System.out.println("before " + TIDMap.size());
		//int loopsize = TIDMap.size();
		Vector<Integer> toremove = new Vector<Integer>();
		Vector<Integer> PadList = new Vector<Integer>();
		for(int testTID : TIDMap.keySet())
		{
			
			for(int t = 0; t < TrigWindSize; t += StepSize)
			{
				for(int pad = 0; pad < TIDMap.get(testTID).get(t).size(); pad++)
				{
					if(!PadList.contains(TIDMap.get(testTID).get(t).get(pad)))
					{
						PadList.add(TIDMap.get(testTID).get(t).get(pad));
					}
					
					if(t > tlargest)
					{
						tlargest = t;
					}
					if(t>4000)
					{
						//System.out.println(pad + " " + t);
					}
				}
			}
			//System.out.println(tlargest);
			if(tlargest < tmin || tlargest > tmax)
			{
				toremove.add(testTID);
				//TIDMap.remove(testTID);
				//gZvsPhi.remove(testTID);
				
			}
			//System.out.println("large T " + tlargest);
			tlargest = 0;
			if(PadList.size() <= min_pads)
			{
				if(!toremove.contains(testTID))
				{
					toremove.add(testTID);
				}
			}
			PadList.clear();
		}
		HashMap<Integer, Vector<Integer>> padmap = new HashMap<Integer, Vector<Integer>>();
		int padlook = 0;
		for(int i = 0; i < toremove.size(); i++)
		{
			TIDMap.remove(toremove.get(i));
		}
		HashMap<Integer, H2F> hZvsT = new HashMap<Integer, H2F>();
		HashMap<Integer, H2F> hPhivsT = new HashMap<Integer, H2F>();
		//Vector3 padv = new Vector3();
		PadVector padv;
		double padphi = 0;
		double padz = 0;
		for(int tid : TIDMap.keySet())
		{
			padmap.put(tid, new Vector<Integer>());
			hPhivsT.put(tid, new H2F("hPhivsT","hPhivsT",80,0,9600,90,-Math.PI,Math.PI));
			hZvsT.put(tid, new H2F("hZvsT","hZvsT",80,0,9600,50,-200,200));
			
			for(int time : TIDMap.get(tid).keySet())
			{
				for(int p = 0; p < TIDMap.get(tid).get(time).size(); p++)
				{
					/*padlook = TIDMap.get(tid).get(time).get(p);
					if(!padmap.get(tid).contains(padlook))
					{
						padmap.get(tid).add(padlook);
					}*/
					//padv = PadCoords(TIDMap.get(tid).get(time).get(p));
					padv = params.get_padvector(TIDMap.get(tid).get(time).get(p));
					//padphi = Math.atan2(padv.y(),padv.x());
					padphi = padv.phi();
					padz = padv.z();
					//System.out.println(time + " " + padz);
					hZvsT.get(tid).fill((double)time, padz);
					hPhivsT.get(tid).fill((double)time, padphi);
				}
			}
		}
		int pcount = 0;
		toremove.clear();
		double histmaxavez = 0;
		double histmaxavephi = 0;
	
		//System.out.println("HERE " + hZvsT.size());
		EmbeddedCanvas chZvsT = new EmbeddedCanvas();
		EmbeddedCanvas chPhivsT = new EmbeddedCanvas();
		JFrame jhZvsT = new JFrame();
		JFrame jhPhivsT = new JFrame();
		chZvsT.divide(5,5);
		chPhivsT.divide(5, 5);
		jhZvsT.setSize(1200,1200);
		jhPhivsT.setSize(1200,1200);
		jhZvsT.setTitle("Z vs T 2d Hist");
		jhPhivsT.setTitle("Phi vs T 2d Hist");
		for(int i : hZvsT.keySet())
		{
			histmaxavez+=hZvsT.get(i).getMax();
			histmaxavephi+=hPhivsT.get(i).getMax();
		}
		histmaxavez/=hZvsT.size();
		histmaxavephi/=hPhivsT.size();
		for(int i : hZvsT.keySet())
		{
			//System.out.println(hZvsT.get(i).getMax() + " " + hPhivsT.get(i).getMax() + " " + histmaxavez + " " + histmaxavephi);
			//if(hZvsT.get(i).getMax() < 10 || hPhivsT.get(i).getMax() < 10)
			if(hZvsT.get(i).getMax() < histmaxavez*0.75 && hPhivsT.get(i).getMax() < histmaxavephi*0.75)
			{
				if(!toremove.contains(i))
				{
					toremove.add(i);
				}
			}
			chZvsT.cd(pcount);
			chPhivsT.cd(pcount);
			chZvsT.draw(hZvsT.get(i));
			chPhivsT.draw(hPhivsT.get(i));
			pcount++;
		}
		jhZvsT.add(chZvsT);
		jhPhivsT.add(chPhivsT);
		
		if(draw) {
			jhZvsT.setVisible(true);
			jhPhivsT.setVisible(true);
		}
		//for(int i : TIDMap.keySet()) {System.out.println(i + " before");}
		for(int i = 0; i < toremove.size(); i++)
		{
			//System.out.println(TIDMap.get(toremove.get(i)).size());
			strkTIDMap.put(toremove.get(i), TIDMap.get(toremove.get(i)));
			TIDMap.remove(toremove.get(i));
		}
		/*for(int i : strkTIDMap.keySet())
		{
			for(int j : strkTIDMap.get(i).keySet())
			{
				for(int k = 0; k < strkTIDMap.get(i).get(j).size(); k ++)
				{
					System.out.println(x);
				}
			}
		}*/
		//for(int i : TIDMap.keySet()) {System.out.println(i + " after");}
		//for(int i = 0; i < toremove.size())
		/*for(int tid : TIDMap.keySet())
		{
			for(int p = 0; p < padmap.get(tid).size(); p++)
			{
				padv = PadCoords(padmap.get(tid).get(p));
				padphi = Math.atan2(padv.y(),padv.x());
				padz = padv.z();
				for(int time : TIDMap.get(tid).keySet())
				{
					if(TIDMap.get(tid).get(time).contains(padmap.get(tid).get(p)))
					{
						
					}
				}
			}
		}*/
		
		//System.out.println(tlargest);
		int color = 1;
		int style = 1;
		
		//for(int i : TIDMap.keySet()) {System.out.println(i);}
		//System.out.println("after " + TIDMap.size());
		//System.out.println(maxconcpads + " " + maxconctime + " " + TIDVec.size());
		if(draw)
		{
			
			EmbeddedCanvas cZvsPhi = new EmbeddedCanvas();
			JFrame jZvsPhi = new JFrame();
			jZvsPhi.setSize(800,600);
			for(int i : TIDMap.keySet())
			{
				//System.out.println(i);
				//if(i == 1)
				//{
				gZvsPhi.get(i).setMarkerColor(color);
				if(false)//if(i == 1)
				{
					gZvsPhi.get(i).setMarkerSize(6);
				}
				else
				{
					gZvsPhi.get(i).setMarkerSize(2);
				}
				//gZvsPhi.get(i).addPoint(0, 0, 0, 0);
				//gZvsPhi.get(i).addPoint(180, 100, 0, 0);
				
				//gZvsPhi.get(i).setMarkerStyle(style);
				
				cZvsPhi.draw(gZvsPhi.get(i),"same");
				//cZvsPhi.save("/Users/davidpayette/Desktop/Plots/g" + i + ".png");
				color++;
				style++;
				
				//}
				//System.out.println("key " + i + " " + TIDMap.size());
			}
			jZvsPhi.setTitle("Track Finder Output");
			jZvsPhi.add(cZvsPhi);
			jZvsPhi.setVisible(true);
		
		}
		write2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i : TIDMap.keySet())
		{
			//System.out.println("TIDMap TIDs " + i);
		}
		for(int i : strkTIDMap.keySet())
		{
			//System.out.println("strkTIDMap TIDs " + i);
		}
		params.set_TIDMap(TIDMap);
		params.set_strkTIDMap(strkTIDMap);
		//TIDMap.clear();
		//System.out.println(System.currentTimeMillis()-time1);
	}
	
	/**Returns x, y, z as a vector, given pad number**/
	/*
	private Vector3 PadCoords(int cellID) {
		
		double PAD_W = 2.79; // in mm
		double PAD_S = 80.0; //in mm
        double PAD_L = 4.0; // in mm
	    double RTPC_L= 384.0; // in mm	    
	    double phi_pad = 0;		    
	    //double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
        double Num_of_Cols = RTPC_L/PAD_L;
	    double PI=Math.PI;
	    double z0 = -(RTPC_L/2.0);
	    double phi_per_pad = PAD_W/PAD_S; // in rad		
        double chan = (double)cellID;       
        double col = chan%Num_of_Cols;
        double row=(chan-col)/Num_of_Cols;
        double z_shift = row%4;
   
        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
        
        if(phi_pad>= 2.0*PI) 
        {
        		phi_pad -= 2.0*PI;
        }
        if(phi_pad<0) 
        	{
        		phi_pad += 2.0*PI;
        	}
   
        double z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
        
        Vector3 PadCoords = new  (PAD_S*Math.cos(phi_pad),PAD_S*Math.sin(phi_pad),z_pad);
        return PadCoords;
		
	}
	
	*/
	
	
	//old functions, maybe used later
	/*private double PadPhi(int cellID) {
		
		double PAD_W = 2.79; // in mm
		double PAD_S = 80.0; //in mm
        double PAD_L = 4.0; // in mm
	    double RTPC_L=400.0; // in mm
	    
	    double phi_pad = 0;
		    
	    double Num_of_Rows = (2.0*(Math.PI)*PAD_S)/PAD_W;
        double Num_of_Cols = RTPC_L/PAD_L;
	    double TotChan = Num_of_Rows*Num_of_Cols;
		    
	    double PI=Math.PI;
		    
	    

	    double phi_per_pad = PAD_W/PAD_S; // in rad
		
		double chan = (double)cellID;
		double col = chan%Num_of_Cols;
		double row=(chan-col)/Num_of_Cols;
        
        
          //double z_shift = 0.;
   
        phi_pad=(row*phi_per_pad)+(phi_per_pad/2.0);
        if(phi_pad>= 2.0*PI) {
        	phi_pad -= 2.0*PI;
        }
        if(phi_pad<0) 
        	{
        	phi_pad += 2.0*PI;
        	}
   
       return phi_pad;
		
	}	
	private double PadZ(int cellID)
	{
	
		double RTPC_L=400.0; // in mm
		double PAD_L = 4.0; // in mm
		double Num_of_Cols = RTPC_L/PAD_L;
		double z0 = -(RTPC_L/2.0); // front of RTPC in mm at the center of the pad
		double chan = (double)cellID;
		double col = chan%Num_of_Cols;
        double row=(chan-col)/Num_of_Cols;
        double z_shift = row%4;
        double z_pad = 0;
        double testnum = (row-z_shift)/4;
        
        z_pad=z0+(col*PAD_L)+(PAD_L/2.0)+z_shift;
        //if(z_shift == 0 && row > 0) {return col-1;}
        //else{return col;}
        return z_pad;//-testnum;
	}*/

}
