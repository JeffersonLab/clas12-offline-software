package org.jlab.rec.dc.segment;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.Hit;
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
			
			if(Constants.isCALIB()) {
				
				DataBank bankDGTZ = event.getBank("DC::tdc");
				
				int rows = bankDGTZ.rows();
				int[] sector = new int[rows];
				int[] layer = new int[rows];
				int[] wire = new int[rows];
				int[] tdc = new int[rows];
				int[] layerNum = new int[rows];
				int[] superlayerNum =new int[rows];
				for(int i = 0; i< rows; i++) {
					sector[i] = bankDGTZ.getByte("sector", i);
					layer[i] = bankDGTZ.getByte("layer", i);
					wire[i] = bankDGTZ.getShort("component", i);
					tdc[i] = bankDGTZ.getInt("TDC", i);		
					superlayerNum[i]=(layer[i]-1)/6 + 1;
					layerNum[i] = layer[i] - (superlayerNum[i] - 1)*6; 
				}
				
				
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
					
					for(int j = 0; j< rows; j++) {
						if(sector[j]== seg.get_Sector() && superlayerNum[j]== seg.get_Superlayer()) {
							if(layerNum[j]==l+1) {
								for(int wo =0; wo<2; wo++)
									if( Math.abs(trjWire-wire[j])==wo && tdc[j]>0)
										matchedHits[wo][l] = (j+1);
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
