package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

public class TrackListFinder {

    private TrkSwimmer bstSwim = new TrkSwimmer();

    public TrackListFinder() {
        // TODO Auto-generated constructor stub
    }

    /**
     *
     * @param cands the list of track candidates
     * @param svt_geo the svt geometry
     * @return the list of selected tracks
     */
    public List<Track> getTracks(List<Track> cands, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        List<Track> tracks = new ArrayList<Track>();
        if (cands.size() == 0) {
            System.err.print("Error no tracks found");
            return cands;
        }

        // remove clones
        //ArrayList<Track> passedcands = this.rmHelicalTrkClones(org.jlab.rec.cvt.svt.Constants.removeClones, cands);
        // loop over candidates and set the trajectories
        
        for (Track trk : cands) {

            this.assignTrkPID(trk);
            //KalFit kf = new KalFit(trk, svt_geo);
            //kf.runKalFit(trk, svt_geo);
            //EnergyLossCorr elc = new EnergyLossCorr(trk);
            //System.out.println("******* before EL "+trk.get_P());
            //elc.doCorrection(trk, svt_geo);
            //System.out.println("*******  after EL "+trk.get_P());

            int charge = trk.get_Q();
            double maxPathLength = 5.0;//very loose cut 
            bstSwim.SetSwimParameters(trk.get_helix(), maxPathLength, charge, trk.get_P());

            double[] pointAtCylRad = bstSwim.SwimToCylinder(Constants.CTOFINNERRADIUS);
            trk.set_TrackPointAtCTOFRadius(new Point3D(pointAtCylRad[0], pointAtCylRad[1], pointAtCylRad[2]));
            trk.set_TrackDirAtCTOFRadius(new Vector3D(pointAtCylRad[3], pointAtCylRad[4], pointAtCylRad[5]));

            trk.set_pathLength(bstSwim.swamPathLength);

            TrajectoryFinder trjFind = new TrajectoryFinder();

            Trajectory traj = trjFind.findTrajectory(trk.get_Id(), trk.get_helix(), trk, svt_geo, bmt_geo, "final");

            trk.set_Trajectory(traj.get_Trajectory());

            //if(trk.passCand == true)
            tracks.add(trk);

        }
        return tracks;
    }

    private void assignTrkPID(Track trk) {

        int NbHits = 0;
        double TotE = 0;
        for (int i = 0; i < trk.size(); i++) {
            if (trk.get(i).get_Detector() != "SVT") {
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

        if (TotE <= org.jlab.rec.cvt.svt.Constants.PIDCUTOFF) {
            trk.set_PID("pion");
        }
        if (TotE > org.jlab.rec.cvt.svt.Constants.PIDCUTOFF) {
            trk.set_PID("proton");
        }
    }
    
     
    public void removeOverlappingTracks(List<Track> trkcands) {

            List<Track> selectedTracks =new ArrayList<Track>();
            List<Track> list = new  ArrayList<Track>();
            for(int i =0; i<trkcands.size(); i++) { 
                    list.clear();
                    this.getOverlapLists(trkcands.get(i), trkcands, list);
                    Track selectedTrk = this.FindBestTrack(list);
                    if(this.ListContainsTrack(selectedTracks, selectedTrk)==false)
                            selectedTracks.add(selectedTrk);
            }
            trkcands.removeAll(trkcands);
            trkcands.addAll(selectedTracks);
    }

    private boolean ListContainsTrack(List<Track> selectedTracks, Track selectedTrk) {
            boolean isInList = false;
            for(Track trk : selectedTracks) {
                    if(trk.get_Id()==selectedTrk.get_Id())
                            isInList=true;
            }
            return isInList;
    }

    private void getOverlapLists(Track track, List<Track> trkcands, List<Track> list) {
            for(int i =0; i<trkcands.size(); i++) { 
                    if( (track.get(0).get_Id()!=-1 && track.get(0).get_Id()==trkcands.get(i).get(0).get_Id()) || 
                                    (track.get(1).get_Id()!=-1 && track.get(1).get_Id()==trkcands.get(i).get(1).get_Id()) || 
                                    (track.get(2).get_Id()!=-1 && track.get(2).get_Id()==trkcands.get(i).get(2).get_Id()) ) {
                            list.add(trkcands.get(i));
                    }
            }
    }

    private Track FindBestTrack(List<Track> trkList) {
            double bestChi2 = 9999999;
            Track bestTrk = null;

            for(int i =0; i<trkList.size(); i++) {
                    if(trkList.get(i).getChi2()/(double)trkList.get(i).getNDF()<bestChi2) {
                            bestChi2 = trkList.get(i).getChi2()/(double)trkList.get(i).getNDF();
                            bestTrk = trkList.get(i);
                    }
            }
            return bestTrk;
    }

}
