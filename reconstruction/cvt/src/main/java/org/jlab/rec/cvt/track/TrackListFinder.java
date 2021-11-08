package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

public class TrackListFinder {
    
    public TrackListFinder() {
        // TODO Auto-generated constructor stub
    }

    /**
     *
     * @param cands the list of track candidates
     * @param svt_geo the svt geometry
     * @param bmt_geo
     * @param ctof_geo
     * @param cnd_geo
     * @param cvtSwim
     * @return the list of selected tracks
     */
    public static List<Track> getTracks(List<Track> cands, 
            SVTGeometry svt_geo, BMTGeometry bmt_geo,
            CTOFGeant4Factory ctof_geo, Detector cnd_geo,
            Swim cvtSwim) {
        List<Track> tracks = new ArrayList<>();
        if (cands.isEmpty()) {
            System.err.print("Error no tracks found");
            return cands;
        }

        // loop over candidates and set the trajectories
        
        for (Track trk : cands) { 
            if(trk.get_helix()!=null) {
                assignTrkPID(trk);
                //KalFit kf = new KalFit(trk, svt_geo);
                //kf.runKalFit(trk, svt_geo);
                //EnergyLossCorr elc = new EnergyLossCorr(trk);
                //System.out.println("******* before EL "+trk.get_P());
                //elc.doCorrection(trk, svt_geo);
                //System.out.println("*******  after EL "+trk.get_P());

                int charge = trk.get_Q();
                double maxPathLength = 5.0;//very loose cut 
                cvtSwim.SetSwimParameters((trk.get_helix().xdca()+Constants.getXb()) / 10, (trk.get_helix().ydca()+Constants.getYb()) / 10, trk.get_helix().get_Z0() / 10 , 
                        Math.toDegrees(trk.get_helix().get_phi_at_dca()), Math.toDegrees(Math.acos(trk.get_helix().costheta())),
                        trk.get_P(), charge, 
                        maxPathLength) ;

                double[] pointAtCylRad = cvtSwim.SwimGenCylinder(new Point3D(0,0,0), new Point3D(0,0,1), Constants.CTOFINNERRADIUS/10, Constants.SWIMACCURACYCD/10);
                if(pointAtCylRad!=null) {
                    trk.se_TrackPosAtCTOF(new Point3D(pointAtCylRad[0]*10, pointAtCylRad[1]*10, pointAtCylRad[2]*10));
                    trk.set_TrackDirAtCTOF(new Vector3D(pointAtCylRad[3]*10, pointAtCylRad[4]*10, pointAtCylRad[5]*10));
                    trk.set_PathToCTOF(pointAtCylRad[6]*10);
                }
                TrajectoryFinder trjFind = new TrajectoryFinder();

                Trajectory traj = trjFind.findTrajectory(trk, svt_geo, bmt_geo, ctof_geo, cnd_geo, cvtSwim, "final");

                trk.set_Trajectory(traj.get_Trajectory());

                //if(trk.passCand == true)
                tracks.add(trk);
            }
        }
        return tracks;
    }

    private static void assignTrkPID(Track trk) {

        int NbHits = 0;
        double TotE = 0;
        for (int i = 0; i < trk.size(); i++) {
            if (trk.get(i).get_Detector() != DetectorType.BST) {
                continue;
            }
            for (int j = 0; j < trk.get(i).get_Cluster1().size(); j++) {
                TotE += trk.get(i).get_Cluster1().get(j).get_Strip().get_Edep();
                NbHits++;
            }
            for (int j = 0; j < trk.get(i).get_Cluster2().size(); j++) {
                TotE += trk.get(i).get_Cluster2().get(j).get_Strip().get_Edep();
                NbHits++;
            }
        }
        TotE /= (double) NbHits;

        if (TotE <= SVTParameters.PIDCUTOFF) {
            trk.set_PID("pion");
        }
        if (TotE > SVTParameters.PIDCUTOFF) {
            trk.set_PID("proton");
        }
    }
    
     
    public static void removeBadTracks(List<Track> trkcands) {
        if (trkcands == null) {
            return;
        }

        int initial_size = trkcands.size();

        for (int i = 1; i < initial_size + 1; i++) {
            if (!trkcands.get(initial_size - i).isGood()) {
                trkcands.remove(initial_size - i);
            }
        }
    }

    public static void removeOverlappingTracks(List<Track> tracks) {
            if(tracks==null)
                return;
            
        List<Track> selectedTracks =  new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            boolean overlap = false;
            Track t1 = tracks.get(i);
            for(int j=0; j<tracks.size(); j++ ) {
                Track t2 = tracks.get(j);
                if(i!=j && t1.overlapWith(t2) && !t1.betterThan(t2)) {
                    overlap=true;
                }
            }
            if(!overlap) selectedTracks.add(t1);
        }
//        if(selectedTracks.size()<tracks.size()) {
//            for(Track t : tracks) System.out.println("Tracks " + t.toString());
//            for(Track t : selectedTracks) System.out.println("Selected " + t.toString());
//        }
        tracks.removeAll(tracks);
        tracks.addAll(selectedTracks);
    }
    
    public static void checkForOverlaps(List<Track> tracks, String msg) {
        for (int i = 0; i < tracks.size(); i++) {
            Track t1 = tracks.get(i);
            for(int j=0; j<tracks.size(); j++ ) {
                Track t2 = tracks.get(j);
                if(i!=j && t1.overlapWith(t2)) {
                    System.out.println(msg + " " + "overlap");
                }
            }
        }        
    }
    
    @Deprecated
    public void removeOverlappingTracksOld(List<Track> trkcands) {
            if(trkcands==null)
                return;
            List<Track> selectedTracks =new ArrayList<Track>();
            List<Track> list = new  ArrayList<Track>();
            List<Track> rejected = new  ArrayList<Track>();
            for(int i =0; i<trkcands.size(); i++) { 
                
                list.clear();
                if(trkcands.get(i)==null) {
                    continue;
                }
                if( rejected.contains( trkcands.get(i) ) ) continue;
                
                this.getOverlapLists(trkcands.get(i), trkcands, list);
                
                Track selectedTrk = this.FindBestTrack(list);
                if(selectedTrk==null)
                    continue;
                if(selectedTracks.contains(selectedTrk)==false)
                        selectedTracks.add(selectedTrk);
                
                list.remove(selectedTrk);

                for( Track t : list ) {
                	if( ! rejected.contains(t) ) rejected.add(t);
                }

            }
            if( rejected != null )
            	selectedTracks.removeAll(rejected);
            if(trkcands!=null)
                trkcands.removeAll(trkcands);
            if(selectedTracks!=null)
                trkcands.addAll(selectedTracks);
    }

    @Deprecated
    private boolean ListContainsTrack(List<Track> selectedTracks, Track selectedTrk) { 
            // not used. Now Track extends Comparables
            boolean isInList = false;
            for(Track trk : selectedTracks) {
                    if(trk.get_Id()==selectedTrk.get_Id())
                            isInList=true;
            }
            return isInList;
    }


    @Deprecated
    private void getOverlapLists(Track track, List<Track> trkcands, List<Track> list) {
    // --------------------------------------------------------------------
    //  two tracks are considered the same if they share at least 2 crosses
    // --------------------------------------------------------------------
			
    	for( Track t : trkcands ) {
    		int N = 0;
    		for( Cross c : t ) {

          // do not check on BMTC
          if( c.get_Type()==BMTType.C) continue;

    			if( track.contains(c) ) { N++;  }
    		}
    		if( N >= 2 ) list.add( t );
    	}
    	
    }

    private static Track FindBestTrack(List<Track> trkList) {
    // --------------------------------------------------------------------
    //  Select the candidate with the highest number of NDF
    //    if two have the same ndf, get the one with the better chi2/ndf
    // --------------------------------------------------------------------
            double bestChi2 = 9999999;
            int ndf = 0;
            Track bestTrk = null;

            for(int i =0; i<trkList.size(); i++) {

                    if( Double.isNaN(trkList.get(i).getChi2()) == false && 
                        trkList.get(i).getChi2() < 1e4 &&
                        trkList.get(i).getNDF()>=ndf) {

                        ndf = trkList.get(i).getNDF();
                        if( trkList.get(i).getNDF()==ndf ) {
                        	if(trkList.get(i).getChi2()/(double)trkList.get(i).getNDF()<bestChi2) {
		                      bestChi2 = trkList.get(i).getChi2()/(double)trkList.get(i).getNDF();
		                      bestTrk = trkList.get(i);
                        	}
                        }
                        else {
                        	bestChi2 = trkList.get(i).getChi2()/(double)trkList.get(i).getNDF();
                        	bestTrk = trkList.get(i);
                        }
                }
            }
            return bestTrk;
    }

}
