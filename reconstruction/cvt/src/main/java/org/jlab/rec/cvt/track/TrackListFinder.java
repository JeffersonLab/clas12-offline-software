package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
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
     * @param cvtSwim
     * @return the list of selected tracks
     */
    public static List<Track> getTracks(List<Track> cands, Swim cvtSwim) {
        List<Track> tracks = new ArrayList<>();
        if (cands.isEmpty()) {
            System.err.print("Error no tracks found");
            return cands;
        }

        // loop over candidates and set the trajectories
        
        for (Track trk : cands) { 
            if(trk.getHelix()!=null) {
                assignTrkPID(trk);
                //KalFit kf = new KalFit(trk, svt_geo);
                //kf.runKalFit(trk, svt_geo);
                //EnergyLossCorr elc = new EnergyLossCorr(trk);
                //System.out.println("******* before EL "+trk.getP());
                //elc.doCorrection(trk, svt_geo);
                //System.out.println("*******  after EL "+trk.getP());

                int charge = trk.getQ();
                double maxPathLength = 5.0;//very loose cut 
                cvtSwim.SetSwimParameters((trk.getHelix().xDCA()+trk.getHelix().getXb()) / 10, (trk.getHelix().yDCA()+trk.getHelix().getYb()) / 10, trk.getHelix().getZ0() / 10 , 
                        Math.toDegrees(trk.getHelix().getPhiAtDCA()), Math.toDegrees(Math.acos(trk.getHelix().cosTheta())),
                        trk.getP(), charge, 
                        maxPathLength) ;

                double[] pointAtCylRad = cvtSwim.SwimGenCylinder(new Point3D(0,0,0), new Point3D(0,0,1), Constants.CTOFINNERRADIUS/10, Constants.SWIMACCURACYCD/10);
                if(pointAtCylRad!=null) {
                    trk.setTrackPosAtCTOF(new Point3D(pointAtCylRad[0]*10, pointAtCylRad[1]*10, pointAtCylRad[2]*10));
                    trk.setTrackDirAtCTOF(new Vector3D(pointAtCylRad[3]*10, pointAtCylRad[4]*10, pointAtCylRad[5]*10));
                    trk.setPathToCTOF(pointAtCylRad[6]*10);
                }
                TrajectoryFinder trjFind = new TrajectoryFinder();

                Trajectory traj = trjFind.findTrajectory(trk, cvtSwim, "final");

                trk.setTrajectory(traj.getTrajectory());

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
            if (trk.get(i).getDetector() != DetectorType.BST) {
                continue;
            }
            for (int j = 0; j < trk.get(i).getCluster1().size(); j++) {
                TotE += trk.get(i).getCluster1().get(j).getStrip().getEdep();
                NbHits++;
            }
            for (int j = 0; j < trk.get(i).getCluster2().size(); j++) {
                TotE += trk.get(i).getCluster2().get(j).getStrip().getEdep();
                NbHits++;
            }
        }
        TotE /= (double) NbHits;

        if (TotE <= SVTParameters.PIDCUTOFF) {
            trk.setPID("pion");
        }
        if (TotE > SVTParameters.PIDCUTOFF) {
            trk.setPID("proton");
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

}
