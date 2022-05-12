/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rtpc.hit;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author davidpayette
 * Developed by Bradley Yale
 * Modified by S. Kuhn after M. Hattawy
 */
public class TrackDisentangler {
    private ReducedTrackMap RTIDMap = new ReducedTrackMap();
    private ReducedTrackMap NewTrackMap = new ReducedTrackMap();
    private ReducedTrack rtrack; 
    private int maxdeltat = 300;
    private int maxdeltatgap = 300;
    private double maxdeltaz = 8;
    private double maxdeltaphi = 0.10;
    private double maxdeltazgap = 10;
    private double maxdeltaphigap = 0.12;
    private double c0 = 20;
    private double c1 = 0.07;
    private double c2 = 8;
    private double c3 = 240;
    private int eventnum = 777;
    private double TFtotaltracktimeflag = 3700; //SEK: lowered value from 5000 - max drift time = 3458 ns

    public TrackDisentangler(HitParameters params, boolean disentangle, int eventnum){       
        
        if(disentangle){ // Should ANY tracks be disentangled?
            RTIDMap = params.get_rtrackmap();
            maxdeltat = params.get_tthreshTD();
            maxdeltatgap = params.get_tthreshTDgap();
            maxdeltaz = params.get_zthreshTD();
            maxdeltaphi = params.get_phithreshTD();
            maxdeltazgap = params.get_zthreshTDgap();
            maxdeltaphigap = params.get_phithreshTDgap();
            c0 = params.get_c0();
            c1 = params.get_c1();
            c2 = params.get_c2();
            c3 = params.get_c3();
          
            List<Integer> origtidlist = RTIDMap.getAllTrackIDs();
            for(int tid : origtidlist){ // Loop over all originally found tracks in an event
                rtrack = RTIDMap.getTrack(tid);           
                if(rtrack.isTrackFlagged()){ //Track has been flagged as suspicious by trackfinder
                    //  BEGIN disentangling/recombining a single existing track
                    //          FIRST: Create a map of new tracks to collect the pieces
                    NewTrackMap = new ReducedTrackMap();
                    rtrack.sortHits(); //hits are sorted in DEcreasing order of time
                    List<HitVector> hits = rtrack.getAllHits();
                    //BEGIN disentangling: Sort all hits in existing track into a set of new tracks 
                    for(HitVector hit : hits){
                       hit.flagHit(0);
                       sortHit(hit); // This is the entire disentangler part!

                    } //end of for loop for all hits in original track - Disentangler is done
                    List<Integer> newtidlist1 = NewTrackMap.getAllTrackIDs();  //NEW SEK: Temporary since we still want to remove bad tracks from the list

                    //NEW SEK The following is to remove "tracklets" that clearly haven't been properly disentangled as shown by total length in time
                    for(int tid1 : newtidlist1){
                        ReducedTrack t1 = NewTrackMap.getTrack(tid1);
                        if(Math.abs(t1.getLargeT()-t1.getSmallT()) > TFtotaltracktimeflag){
                          NewTrackMap.removeTrack(tid1);
                            }
                        } //END loop over all new tracklets to remove improperly long ones
                    List<Integer> newtidlist = NewTrackMap.getAllTrackIDs();  //Final list after bad tracklets have been removed

                    //BEGIN RECOMBINER - remove tracks that have been added to existing ones
                    List<Integer> removedtracks = new ArrayList<>();

                     for(int tid1 : newtidlist){ // SEKNEW: FIRST, find all "Forward tracks" (last to first):
                        for(int tid2 : newtidlist){
                            if(tid1 != tid2 && !removedtracks.contains(tid1) && !removedtracks.contains(tid2)){
                                boolean merged = false; 
                                ReducedTrack t1 = NewTrackMap.getTrack(tid1);
                                ReducedTrack t2 = NewTrackMap.getTrack(tid2);
                                List<HitVector> h2listfirst = new ArrayList<>();
                                List<HitVector> h1listlast = new ArrayList<>();
                                h1listlast.addAll(t1.getLastNHits(2));
                                h2listfirst.addAll(t2.getFirstNHits(2));
                                
                                FIRSTLAST:
                                    for(HitVector h1 : h1listlast){
                                        for(HitVector h2 : h2listfirst){
                                            if(compareHitsTime(h1,h2)){
                                                if(PredictHit(t1, h2, c0, c1, c2, c3)){ //NEW Implement PredictHit
                                                    NewTrackMap.mergeTracks(tid1, tid2); 
                                                    NewTrackMap.getTrack(tid1).sortHits();
                                                    removedtracks.add(tid2);
                                                    break FIRSTLAST;
                                                } // Endif PredictHit
                                            } // Endif compareHitsTime
                                        } // End for loop over h2listfirst
                                    } // End for loop over h1listlast and hence FIRSTLAST
                            } //Endif tid1 != tid2 and neither tid1 nor tid2 are removed
                        } // end the for loop of for(int tid2 : newtidlist)
                    } // end the for loop of for(int tid1 : newtidlist)
    
                    for(int tid1 : newtidlist){ // SEKNEW: Now combine backbenders
                    BACKBENDER: // SEKNEW: once we find a suitable match for tid1 to form a backbender, don't look for any additional ones!
                      for(int tid2 : newtidlist){
                           if(tid1 < tid2 && !removedtracks.contains(tid1) && !removedtracks.contains(tid2)){ // SEKNEW: each pairing should be tested only once!
                               boolean merged = false;
                               ReducedTrack t1 = NewTrackMap.getTrack(tid1);
                               ReducedTrack t2 = NewTrackMap.getTrack(tid2);
                               if(t2.getAllHits().size() > 10) { //SEKNEW: Only look for "serious" backbenders with lots of extra hits
                               List<HitVector> h1listlast = new ArrayList<>();
                               List<HitVector> h2listlast = new ArrayList<>();
                               h1listlast.addAll(t1.getLastNHits(2));
                               h2listlast.addAll(t2.getLastNHits(2));
                               
                               //  BACKBENDER SEKNEW: Moved up
                               for(HitVector h1 : h1listlast){
                                   for(HitVector h2: h2listlast){
                                       if(compareHitsTime(h1,h2)){
                                           if(PredictHit(t1, h2, c0, c1, c2, c3)){ //NEW Implement PredictHit
                                               NewTrackMap.mergeTracksBackbend(tid1, tid2);
                                               NewTrackMap.getTrack(tid1).sortHits();
                                               removedtracks.add(tid2);
                                               merged = true;
                                               break BACKBENDER;
                                               } // Endif PredictHit
                                           } // Endif compareHitsTime
                                       } // End for loop over h2listlast
                                   } // End for loop over h1listlast
                                } // End if t2 has at least 11 entries
                            } //Endif tid1 != tid2 and neither tid1 nor tid2 are removed
                       } // end the for loop of for(int tid2 : newtidlist)  and hence BACKBENDER
                   } // end the for loop of for(int tid1 : newtidlist)
                
                    //END Recombiner - now initial old track is removed and new tracks are added to RTIDMap
                    RTIDMap.removeTrack(tid);
                    newtidlist = NewTrackMap.getAllTrackIDs();
                    for(int tidfinal : newtidlist){
                       // remove tracks with hits less than 6 
                       if(NewTrackMap.getTrack(tidfinal).getAllHits().size() > 6){
                           // Before adding track to final list, sort and remove duplicates
                           rtrack = NewTrackMap.getTrack(tidfinal);
                           ReducedTrack Purgedrtrack = new ReducedTrack(); // new track to collect only unique hits
                           List<HitVector> hlist = rtrack.getAllHits(); //list of all hits in rtrack and then execute the following commands: 
                                              
                           for(HitVector a : hlist){ // First fix phi out of range
                              if(hlist.get(hlist.indexOf(a)).phi() > Math.PI){
                                 hlist.get(hlist.indexOf(a)).setphi(hlist.get(hlist.indexOf(a)).phi() - 2*Math.PI);
                              } else if (hlist.get(hlist.indexOf(a)).phi() < - Math.PI ) {
                                 hlist.get(hlist.indexOf(a)).setphi(hlist.get(hlist.indexOf(a)).phi() + 2*Math.PI);
                              } // Now remove repeated hits
                                                                 
                              if(hlist.indexOf(a) == 0){
                                 Purgedrtrack.addHit(a); // The first hit will always be kept
                              // } else if(hlist.get(hlist.indexOf(a)) != null && hlist.get(hlist.indexOf(a)-1) != null){
                              } else if(hlist.indexOf(a) > 0 ){
                                 HitVector comphit = Purgedrtrack.getLastHit();
                                 if(Math.abs(hlist.get(hlist.indexOf(a)).time() - comphit.time()) > 0.1 // hit times are at least 0.1 ns different
                                    || Math.abs(hlist.get(hlist.indexOf(a)).z() - comphit.z()) > 0.5 // hit z are at least 0.5 mm different
                                    || Math.abs(hlist.get(hlist.indexOf(a)).phi() - comphit.phi()) > 0.017){ // hit phi are at least 1 degree different
                                    Purgedrtrack.addHit(a); // only add if not identical to previous hit
                                          }
                              }
                           }
                           // and then add this purged track to the original track map, instead of rtrack:
                           RTIDMap.addTrack(Purgedrtrack);
                       } //endif size > 6
                    } // End for loop over all new tracks left after disentangling and recombining
                    
                } // Endif rtrack.isTrackFlagged() - Track is flagged to be disentangled - line 60
            }  // End for loop over all originally found tracks in an event - line 58

            params.set_rtrackmap(RTIDMap); //Final version of RTIDMap

        } // Endif disentangle - Should any tracks be disentangled? line 38
    } // End of Disentangler script (line 36)
    
    private void sortHit(HitVector hit){
        List<Integer> TIDList = NewTrackMap.getAllTrackIDs();
        boolean hitsorted = false;
        for(int tidsub : TIDList){ // SEK: Changed "tid" to "tidsub" to not confuse with Parent ID
            ReducedTrack t = NewTrackMap.getTrack(tidsub);
            HitVector comphit = t.getLastHit();
            if(compareHitsTime(hit,comphit)){
                if(t.getAllHits().size() < c0 && hitsorted){ // SEKNEW: Prevent short stubs from acquiring more hits if they are already sorted
                    hitsorted = true;
                }else if(PredictHit(t, hit, c0, c1, c2, c3)){ // SEKNEW: Look if track t points to hit; c2 = Delta-z max is hardwired here
                    t.addHit(hit);
                    hitsorted = true;
                } // SEKNEW: End of 2 possibilities for a potential hit to be added to existing subtrack
            } // End if compareHitsTime
        } // End of loop over all existing tracks
        if(!hitsorted){
            ReducedTrack newt = new ReducedTrack();
            newt.addHit(hit);
            NewTrackMap.addTrack(newt);
        }
    } //End sortHit
    
    private boolean compareHits(HitVector a, HitVector b){
        double phi1 = a.phi();
        double phi2 = b.phi();
        if(phi1 < 0) phi1 += 2*Math.PI;
        if(phi2 < 0) phi2 += 2*Math.PI;
        double zdiff = a.z() - b.z();
        double phidiff = Math.abs(phi1 - phi2);
        if(phidiff > Math.PI){
            return Math.abs(phidiff - 2*Math.PI) < maxdeltaphigap && zdiff < maxdeltazgap;
        }else{
            return phidiff < maxdeltaphi && zdiff < maxdeltaz;
        }
    } //End compareHits
    
    private boolean compareHitsTime(HitVector a, HitVector b){
        double phi1 = a.phi();
        double phi2 = b.phi();
        if(phi1 < 0) phi1 += 2*Math.PI;
        if(phi2 < 0) phi2 += 2*Math.PI;
        double zdiff = Math.abs(a.z() - b.z());
        double phidiff = Math.abs(phi1 - phi2);
        double timediff = Math.abs(b.time() - a.time());
        //boolean torder = false;
        //if(a.time() < b.time()) torder = true;
        if(phidiff > Math.PI){
            //return b.time() - a.time() < maxdeltatgap && torder && Math.abs(phidiff - 2*Math.PI) < maxdeltaphigap && zdiff < maxdeltazgap;
            return timediff < maxdeltatgap && Math.abs(phidiff - 2*Math.PI) < maxdeltaphigap && zdiff < maxdeltazgap;
        }else{
            //return b.time() - a.time() < maxdeltat && torder && phidiff < maxdeltaphi && zdiff < maxdeltaz;
            return timediff < maxdeltat && phidiff < maxdeltaphi && zdiff < maxdeltaz;
        }
    } //End compareHitsTime
    
    private boolean PredictHit(ReducedTrack t, HitVector hit, double c0, double c1, double c2, double c3){
        // Check whether there are at least c0 hits in t and return “TRUE” if not
        if( t.getAllHits().size() < c0 ) { return true;}

        // If there are at least c0 hits, we would use
        List<HitVector> hlist = new ArrayList<>();
        hlist.addAll(t.getLastNHits((int)c0));
        // to get those last c0 hits.

        // Then, we calculate the following quantities for the linear extrapolation:
        // - taver = average of a.time() 
        // - phiaver = average of a.phi()
        // - zaver = average of a.z()

        double taver = 0;
        double phiaver = 0;
        double zaver = 0;
        // Average the parameters       
        for(HitVector a : hlist){
            // Add the hit times
            taver += a.time();
            // Compare phi values in the c0 reduced HitVector list, add or subtract 2*pi
            if(hlist.indexOf(a) > 0 && hlist.get(hlist.indexOf(a)) != null && hlist.get(hlist.indexOf(a)-1) != null){

                if(hlist.get(hlist.indexOf(a)).phi() > hlist.get(hlist.indexOf(a)-1).phi() + Math.PI){
                    hlist.get(hlist.indexOf(a)).setphi(hlist.get(hlist.indexOf(a)).phi() - 2*Math.PI);
                } else if ( hlist.get(hlist.indexOf(a)).phi() < hlist.get(hlist.indexOf(a)-1).phi() - Math.PI ){
                    hlist.get(hlist.indexOf(a)).setphi(hlist.get(hlist.indexOf(a)).phi() + 2*Math.PI);
                }
            }
            // Add the phi's (after setting the "phi branches")
            phiaver += a.phi();
            // Add the zeds
            zaver += a.z();
        }
        // Average the quantities
        taver /= hlist.size();
        phiaver /= hlist.size();
        zaver /= hlist.size();

        double ttest = 0;
        double phitest = 0;
        double ztest = 0;
        // Find which of these quantities has max range, to determine which to use for extrapolating
        for(HitVector a : hlist){
            ttest += (a.time() - taver)*(a.time() - taver)/120/120;
            phitest += (a.phi() - phiaver)*(a.phi() - phiaver)/0.034/0.034;
            ztest += (a.z() - zaver)*(a.z() - zaver)/4/4;
        }

        double var = 0;
        double tcovar = 0;
        double phicovar = 0;
        double zcovar = 0;
        double phislope = 0;
        double zslope = 0;
        double tslope = 0;
        double phipred = 0;
        double zpred = 0;
        double tpred = 0;
        double phidiff = 0;
        double zdiff = 0;
        double tdiff = 0;
        // Predict if the new hit is in the track
        // Using the test quantities, decide which parameter to extrapolate with

        // If the time difference is largest:
        if(ttest > phitest && ttest > ztest){

            // Calculate the phi, z covariance, time variance
            for(HitVector a : hlist){
                var += (a.time() - taver)*(a.time() - taver);
                phicovar += (a.phi() - phiaver)*(a.time() - taver); // SEK: fixed mistake
                zcovar += (a.z() - zaver)*(a.time() - taver); // SEK: fixed mistake
            }

            // Calculate the phi, z slopes with time variance
            if(var!=0){
                phislope = phicovar/var;
                zslope = zcovar/var;
            }

            phipred = phiaver + phislope*(hit.time() - taver);
            zpred = zaver + zslope*(hit.time() - taver);

            phidiff = Math.abs(hit.phi() - phipred);
            if(phidiff > Math.PI){
                phidiff = Math.abs(phidiff - 2*Math.PI);
            }
            zdiff = Math.abs(hit.z() - zpred);

            return phidiff < c1 && zdiff < c2;

        // If phi has the largest variance:
        } else if(phitest > ttest && phitest > ztest){

            // Calculate the t, z covariance, phi variance
            for(HitVector a : hlist){
                var += (a.phi() - phiaver)*(a.phi() - phiaver);
                tcovar += (a.time() - taver)*(a.phi() - phiaver); // SEK: fixed mistake
                zcovar += (a.z() - zaver)*(a.phi() - phiaver); // SEK: fixed mistake
            }

            // Calculate the t, z slopes with phi variance
            if(var!=0){
                tslope = tcovar/var;
                zslope = zcovar/var;
            }

            // Correct the phi of the new hit, if needed SEK: BUT ONLY LOCALLY!!!

            double phi1 = hit.phi();  //Use local copy, not track hit
            if( phi1 > phiaver + Math.PI){
                phi1 -= 2*Math.PI;
            } else if ( phi1 < phiaver - Math.PI ){
                phi1 += 2*Math.PI;
            }
            tpred = taver + tslope*(phi1 - phiaver);
            zpred = zaver + zslope*(phi1 - phiaver);

            tdiff = Math.abs(hit.time() - tpred);
            zdiff = Math.abs(hit.z() - zpred);

            return tdiff < c3 && zdiff < c2;

        // Or, if z has the largest variance:
        } else { // if(ztest > ttest && ztest > phitest){
// Changed so that we do at least one test under all circumstances
            // Calculate the t, phi slopes with z variance
            for(HitVector a : hlist){
                var += (a.z() - zaver)*(a.z() - zaver);
                tcovar += (a.time() - taver)*(a.z() - zaver); // SEK: fixed mistake
                phicovar += (a.phi() - phiaver)*(a.z() - zaver); // SEK: fixed mistake
            }

            if(var!=0){
                tslope = tcovar/var;
                phislope = phicovar/var;
            }

            tpred = taver + tslope*(hit.z() - zaver);
            phipred = phiaver + phislope*(hit.z() - zaver);

            tdiff = Math.abs(hit.time() - tpred);
            phidiff = Math.abs(hit.phi() - phipred);
            if(phidiff > Math.PI){
                phidiff = Math.abs(phidiff - 2*Math.PI);
            }

            return tdiff < c3 && phidiff < c1;

//       } else {
//
//           return true;
        }
    } //End PredictHit

    public class FileWrite {
        public void writeToFile(String message, String FILE_NAME) {
            try {
                FileWriter fstream = new FileWriter(FILE_NAME, true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.append(message + "\n");
                out.close();
            } catch (Exception e) {
                System.out.print("File access error: " + e.getMessage());
            }
        }
    } // End public class FileWrite
} // End public class TrackDisentangler line 19
