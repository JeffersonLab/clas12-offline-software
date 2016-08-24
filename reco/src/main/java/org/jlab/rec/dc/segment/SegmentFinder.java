package org.jlab.rec.dc.segment;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

/**
 * A Segment is a fitted cluster that has been pruned of hits with bad residuals (see Constants)
 * @author ziegler
 *
 */
public class SegmentFinder {

	/**
	 * @param allClusters  the list of fitted clusters
	 * @return the list of segments obtained from the clusters
	 */
	public List<Segment> get_Segments(List<FittedCluster> allClusters, DataEvent event) {
		List<Segment> segList = new ArrayList<Segment>();
		for(FittedCluster fClus : allClusters) {
			
			if(fClus.size()>Constants.MAXCLUSSIZE)
				continue;
			if(fClus.get_TrkgStatus()==-1) {
				System.err.print("Error -- the clusters must be fit prior to making segments");
				return segList;
			}
			
			Segment seg = new Segment(fClus);
			seg.set_fitPlane();	
			
			if(Constants.isCalibrationRun) {
				// get all the hits to obtain layer efficiency
				EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("DC::dgtz");
		        
				int[] hitno = bankDGTZ.getInt("hitn");
		        int[] sector = bankDGTZ.getInt("sector");
				int[] slayer = bankDGTZ.getInt("superlayer");
				int[] layer = bankDGTZ.getInt("layer");
				int[] wire = bankDGTZ.getInt("wire");
				
				// Get the Segment Trajectory
				SegmentTrajectory trj = new SegmentTrajectory();
				trj.set_SegmentId(seg.get_Id());
				trj.set_Superlayer(seg.get_Superlayer());
				trj.set_Sector(seg.get_Sector());
				double[] trkDocas = new double[6];
				int[] matchHits = new int[6];
				
				int[][] matchedHits = new int[3][6]; // first arrays = how many wires off
				for(int i1 =0; i1<3; i1++)
					for(int i2 =0; i2<6; i2++)
						matchedHits[i1][i2] = -1;
				
				for(int l = 0; l<6; l++) {
					double z = GeometryLoader.dcDetector.getSector(0).getSuperlayer(seg.get_Superlayer()-1).getLayer(l).getComponent(0).getMidpoint().z();
					double trkXMP = seg.get_fittedCluster().get_clusterLineFitSlopeMP()*z+seg.get_fittedCluster().get_clusterLineFitInterceptMP();				
					double trkX = seg.get_fittedCluster().get_clusterLineFitSlope()*z+seg.get_fittedCluster().get_clusterLineFitIntercept();
					
					if(trkX==0)
						continue; // should always get a cluster fit
					int trjWire = trj.getWireOnTrajectory(seg.get_Superlayer(), l+1, trkXMP);
					
					double x = GeometryLoader.dcDetector.getSector(0).getSuperlayer(seg.get_Superlayer()-1).getLayer(l).getComponent(trjWire-1).getMidpoint().x();
					double cosTrkAngle = Math.cos(Math.toRadians(6.))*Math.sqrt(1.+seg.get_fittedCluster().get_clusterLineFitSlope()*seg.get_fittedCluster().get_clusterLineFitSlope());
					double calc_doca = (x-trkX)*cosTrkAngle;
					trkDocas[l] = calc_doca;
					
					for(int j = 0; j< hitno.length; j++) {
						if(sector[j]== seg.get_Sector() && slayer[j]== seg.get_Superlayer()) {
							if(layer[j]==l+1) {
								for(int wo =0; wo<2; wo++)
									if( Math.abs(trjWire-wire[j])==wo)
										matchedHits[wo][l] = j;
							}
						}
					}
					matchHits[l] = -1;
					for(int wo =0; wo<2; wo++)
						if(matchedHits[wo][l] !=-1) {
							matchHits[l] = matchedHits[wo][l] ;
							wo =2;
						}
				}
				trj.setTrkDoca(trkDocas);
				trj.setMatchedHitId(matchHits);
			
				seg.set_Trajectory(trj);
			}
			segList.add(seg);
		}
//		this.setAssociatedID(segList);
		return segList;
		
	}
	
	

}
