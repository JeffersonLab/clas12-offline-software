package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

public class TrackListFinder {

	private TrkSwimmer bstSwim = new TrkSwimmer();
	
	public TrackListFinder() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 * @param cands the list of track candidates
	 * @param geo the svt geometry
	 * @return the list of selected tracks
	 */
	public List<Track> getTracks(List<Track> cands, org.jlab.rec.cvt.svt.Geometry geo) {
		List<Track> tracks = new ArrayList<Track>();
		if(cands.size()==0) {
			System.err.print("Error no tracks found");
			return cands;
		}
		
		for(Track trk : cands) {
			
			this.assignTrkPID(trk);
			//KalFit kf = new KalFit(trk, geo);
			//kf.runKalFit(trk, geo);
			EnergyLossCorr elc = new EnergyLossCorr(trk);
			//System.out.println("******* before EL "+trk.get_P());
			elc.doCorrection(trk, geo);
			//System.out.println("*******  after EL "+trk.get_P());
			
			
			int charge = trk.get_Q();
			double maxPathLength =5.0 ;//very loose cut 
			bstSwim.SetSwimParameters(trk.get_helix(), maxPathLength, charge,trk.get_P());
			
			double[] pointAtCylRad = bstSwim.SwimToCylinder(Constants.CTOFINNERRADIUS);
			trk.set_TrackPointAtCTOFRadius(new Point3D(pointAtCylRad[0],pointAtCylRad[1],pointAtCylRad[2]));
			trk.set_TrackDirAtCTOFRadius(new Vector3D(pointAtCylRad[3],pointAtCylRad[4],pointAtCylRad[5]));			
			
			trk.set_pathLength(bstSwim.swamPathLength);
			
			//if(trk.passCand == true)
				tracks.add(trk);
			
		}
		return tracks;
	}
	private void assignTrkPID(Track trk) {
		
		int NbHits =0;
		double TotE =0;
		for(int i = 0; i<trk.size(); i++) {
			if(trk.get(i).get_Detector()!="SVT")
				continue;
			for(int j =0; j< trk.get(i).get_Cluster1().size(); j++) {
				TotE+= trk.get(i).get_Cluster1().get(j).get_Strip().get_Edep();
				NbHits++;
			}
			for(int j =0; j< trk.get(i).get_Cluster2().size(); j++) {
				TotE+= trk.get(i).get_Cluster2().get(j).get_Strip().get_Edep();
				NbHits++;
			}
		}
		TotE/=(double)NbHits;
		
		if(TotE<=org.jlab.rec.cvt.svt.Constants.PIDCUTOFF) 
			trk.set_PID("pion");
		if(TotE>org.jlab.rec.cvt.svt.Constants.PIDCUTOFF) 
			trk.set_PID("proton");
	}
}
