package org.jlab.rec.dc.banks;
 
import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

import trackfitter.fitter.utilities.*;
import Jama.Matrix;
/**
 * A class to fill the reconstructed DC banks
 * @author ziegler
 *
 */


public class RecoBankWriter {

	/**
	 * 
	 * Writes output banks
	 *
	 */

	public RecoBankWriter() {
		// empty constructor
		
	}
	public void updateListsListWithClusterInfo(List<FittedHit> fhits,
			List<FittedCluster> clusters) {
		for(int i = 0; i<clusters.size(); i++) {
			
			for(int j = 0; j<clusters.get(i).size(); j++) {
				
				clusters.get(i).get(j).set_AssociatedClusterID(clusters.get(i).get_Id());
				
				for(int k =0; k<fhits.size(); k++) {
					if(fhits.get(k).get_Id()==clusters.get(i).get(j).get_Id()) {
						fhits.remove(k);	
						fhits.add(clusters.get(i).get(j));
							
					}
				}
			}
		}

	}
	
	public EvioDataBank fillHBHitsBank(EvioDataEvent event, List<FittedHit> hitlist) {
		
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBHits",hitlist.size());
    
		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("id",i, hitlist.get(i).get_Id());
			bank.setInt("superlayer",i, hitlist.get(i).get_Superlayer());
			bank.setInt("layer",i, hitlist.get(i).get_Layer());
			bank.setInt("sector",i, hitlist.get(i).get_Sector());
			bank.setInt("wire",i, hitlist.get(i).get_Wire());
			bank.setDouble("time",i, hitlist.get(i).get_Time());
			bank.setDouble("doca",i, hitlist.get(i).get_Doca());
			bank.setDouble("docaError",i, hitlist.get(i).get_DocaErr());
			bank.setDouble("trkDoca", i, hitlist.get(i).get_ClusFitDoca());
			bank.setDouble("locX",i, hitlist.get(i).get_lX());
			bank.setDouble("locY",i, hitlist.get(i).get_lY());
			bank.setDouble("X",i, hitlist.get(i).get_X());
			bank.setDouble("Z",i, hitlist.get(i).get_Z());
			bank.setInt("LR",i, hitlist.get(i).get_LeftRightAmb());
			bank.setInt("clusterID", i, hitlist.get(i).get_AssociatedClusterID());
			bank.setInt("trkID", i, hitlist.get(i).get_AssociatedHBTrackID());
		}
		//System.out.println(" Created Bank "); bank.show();
		return bank;

	}

	/**
	 * 
	 * @param event the EvioEvent
	 * @return clusters bank
	 */
	public EvioDataBank fillHBClustersBank(EvioDataEvent event, List<FittedCluster> cluslist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBClusters",cluslist.size());
		
		int[] hitIdxArray= new int[12];
		
		for(int i =0; i< cluslist.size(); i++) {
			for(int j =0; j<hitIdxArray.length; j++) {
				hitIdxArray[j] = -1;
			}
			double chi2 =0;
			
			bank.setInt("ID",i, cluslist.get(i).get_Id());
			bank.setInt("superlayer",i, cluslist.get(i).get_Superlayer());
			bank.setInt("sector",i, cluslist.get(i).get_Sector());			
			
			bank.setDouble("avgWire", i, cluslist.get(i).getAvgwire());
			bank.setInt("size", i, cluslist.get(i).size());
			
			double fitSlope= cluslist.get(i).get_clusterLineFitSlope();			
			double fitInterc =cluslist.get(i).get_clusterLineFitIntercept();
			
			bank.setDouble("fitSlope", i, fitSlope);
			bank.setDouble("fitSlopeErr", i, cluslist.get(i).get_clusterLineFitSlopeErr());
			bank.setDouble("fitInterc", i, fitInterc);
			bank.setDouble("fitIntercErr", i, cluslist.get(i).get_clusterLineFitInterceptErr());
			
			for(int j = 0; j<cluslist.get(i).size(); j++) {		
				if(j<hitIdxArray.length)
					hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
				
				double residual = cluslist.get(i).get(j).get_ClusFitDoca()/(cluslist.get(i).get(j).get_CellSize()/Math.sqrt(12.));
				chi2+= residual*residual;
			}
			bank.setDouble("fitChisqProb", i, ProbChi2perNDF.prob(chi2, cluslist.get(i).size()-2));
			
			for(int j =0; j<hitIdxArray.length; j++) {
				String hitStrg = "Hit";
				hitStrg+=(j+1);
				hitStrg+="_ID";
				bank.setInt(hitStrg, i, hitIdxArray[j]);
			}
		}

		return bank;
		
	}
	
	/**
	 * 
	 * @param event the EvioEvent
	 * @return segments bank
	 */
	public EvioDataBank fillHBSegmentsBank(EvioDataEvent event, List<Segment> seglist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBSegments", seglist.size());
    
		int[] hitIdxArray= new int[12]; // only saving 12 hits for now
				
		for(int i =0; i< seglist.size(); i++) {
			for(int j =0; j<hitIdxArray.length; j++) {
				hitIdxArray[j] = -1;
			}
			
			double chi2 =0;
			
			bank.setInt("ID",i, seglist.get(i).get_Id());
			//bank.setInt("size", i, seglist.get(i).size());
			bank.setInt("superlayer",i, seglist.get(i).get_Superlayer());
			bank.setInt("sector",i, seglist.get(i).get_Sector());	
			
			FittedCluster cls= seglist.get(i).get_fittedCluster();
			bank.setInt("Cluster_ID",  i, cls.get_Id());
			
			bank.setDouble("avgWire", i, cls.getAvgwire());
			bank.setInt("size", i, seglist.get(i).size());
			
			bank.setDouble("fitSlope", i, cls.get_clusterLineFitSlope());
			bank.setDouble("fitSlopeErr", i, cls.get_clusterLineFitSlopeErr());
			bank.setDouble("fitInterc", i, cls.get_clusterLineFitIntercept());
			bank.setDouble("fitIntercErr", i, cls.get_clusterLineFitInterceptErr());
			
			bank.setDouble("SegEndPoint1X", i, seglist.get(i).get_SegmentEndPoints()[0]);
			bank.setDouble("SegEndPoint1Z", i, seglist.get(i).get_SegmentEndPoints()[1]);
			bank.setDouble("SegEndPoint2X", i, seglist.get(i).get_SegmentEndPoints()[2]);
			bank.setDouble("SegEndPoint2Z", i, seglist.get(i).get_SegmentEndPoints()[3]);
			
			for(int j = 0; j<seglist.get(i).size(); j++) {	
				if(j<hitIdxArray.length)
					hitIdxArray[j] = seglist.get(i).get(j).get_Id();
				
				double residual = seglist.get(i).get(j).get_ClusFitDoca()/(seglist.get(i).get(j).get_CellSize()/Math.sqrt(12.));
				chi2+= residual*residual;
			}
			bank.setDouble("fitChisqProb", i, ProbChi2perNDF.prob(chi2, seglist.get(i).size()-2));
			
			for(int j =0; j<hitIdxArray.length; j++) {
				String hitStrg = "Hit";
				hitStrg+=(j+1);
				hitStrg+="_ID";
				bank.setInt(hitStrg, i, hitIdxArray[j]);
			}
		}

		return bank;
		
	}

	/**
	 * 
	 * @param event the EvioEvent
	 * @return segments bank
	 */
	public EvioDataBank fillHBSegmentsTrajectoryBank(EvioDataEvent event, List<Segment> seglist) {
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBSegmentTrajectory", seglist.size()*6);
	   		
		int index =0;
		for(int i =0; i< seglist.size(); i++) {
			SegmentTrajectory trj = seglist.get(i).get_Trajectory();
			for(int l =0; l<6; l++) {
				bank.setInt("segmentID",index, trj.get_SegmentId());
				bank.setInt("sector",index, trj.get_Sector());	
				bank.setInt("superlayer",index, trj.get_Superlayer());
				bank.setInt("layer",index, (l+1));
				bank.setInt("matchedHitID", index, trj.getMatchedHitId()[l]);
				bank.setDouble("trkDoca", index, trj.getTrkDoca()[l]);
				index++;
			}
		}
		return bank;
	}
	/**
	 * 
	 * @param event the EvioEvent
	 * @return crosses bank
	 */
	public EvioDataBank fillHBCrossesBank(EvioDataEvent event, List<Cross> crosslist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBCrosses", crosslist.size());
    
		for(int i =0; i< crosslist.size(); i++) {
			bank.setInt("ID",i, crosslist.get(i).get_Id());
			bank.setInt("sector",i, crosslist.get(i).get_Sector());	
			bank.setInt("region", i, crosslist.get(i).get_Region());
			bank.setDouble("x", i, crosslist.get(i).get_Point().x());
			bank.setDouble("y", i, crosslist.get(i).get_Point().y());
			bank.setDouble("z", i, crosslist.get(i).get_Point().z());
			bank.setDouble("err_x", i, crosslist.get(i).get_PointErr().x());
			bank.setDouble("err_y", i, crosslist.get(i).get_PointErr().y());
			bank.setDouble("err_z", i, crosslist.get(i).get_PointErr().z());
			bank.setDouble("ux", i, crosslist.get(i).get_Dir().x());
			bank.setDouble("uy", i, crosslist.get(i).get_Dir().y());
			bank.setDouble("uz", i, crosslist.get(i).get_Dir().z());
			bank.setDouble("err_ux", i, crosslist.get(i).get_DirErr().x());
			bank.setDouble("err_uy", i, crosslist.get(i).get_DirErr().y());
			bank.setDouble("err_uz", i, crosslist.get(i).get_DirErr().z());
			bank.setInt("Segment1_ID", i, crosslist.get(i).get_Segment1().get_Id());
			bank.setInt("Segment2_ID", i, crosslist.get(i).get_Segment2().get_Id());
		}

		return bank;
		
	}
	
	
	public EvioDataBank fillHBTracksBank(EvioDataEvent event, List<Track> candlist) {

		
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("HitBasedTrkg::HBTracks",candlist.size());
    
		for(int i =0; i< candlist.size(); i++) {
			bank.setInt("ID",i, candlist.get(i).get_Id());
			bank.setInt("sector",i, candlist.get(i).get_Sector());	
			bank.setInt("q", i, candlist.get(i).get_Q());
			bank.setDouble("p", i, candlist.get(i).get_P());
			bank.setDouble("c1_x", i, candlist.get(i).get_PreRegion1CrossPoint().x());
			bank.setDouble("c1_y", i, candlist.get(i).get_PreRegion1CrossPoint().y());
			bank.setDouble("c1_z", i, candlist.get(i).get_PreRegion1CrossPoint().z());
			bank.setDouble("c1_ux", i, candlist.get(i).get_PreRegion1CrossDir().x());
			bank.setDouble("c1_uy", i, candlist.get(i).get_PreRegion1CrossDir().y());
			bank.setDouble("c1_uz", i, candlist.get(i).get_PreRegion1CrossDir().z());
			bank.setDouble("c3_x", i, candlist.get(i).get_PostRegion3CrossPoint().x());
			bank.setDouble("c3_y", i, candlist.get(i).get_PostRegion3CrossPoint().y());
			bank.setDouble("c3_z", i, candlist.get(i).get_PostRegion3CrossPoint().z());
			bank.setDouble("c3_ux", i, candlist.get(i).get_PostRegion3CrossDir().x());
			bank.setDouble("c3_uy", i, candlist.get(i).get_PostRegion3CrossDir().y());
			bank.setDouble("c3_uz", i, candlist.get(i).get_PostRegion3CrossDir().z());
			bank.setDouble("t1_x", i, candlist.get(i).get_Region1TrackX().x());
			bank.setDouble("t1_y", i, candlist.get(i).get_Region1TrackX().y());
			bank.setDouble("t1_z", i, candlist.get(i).get_Region1TrackX().z());
			bank.setDouble("t1_px", i, candlist.get(i).get_Region1TrackP().x());
			bank.setDouble("t1_py", i, candlist.get(i).get_Region1TrackP().y());
			bank.setDouble("t1_pz", i, candlist.get(i).get_Region1TrackP().z());
			bank.setDouble("pathlength", i, candlist.get(i).get_TotPathLen()); 
			bank.setDouble("Vtx0_x",  i,candlist.get(i).get_Vtx0().x() );
			bank.setDouble("Vtx0_y",  i,candlist.get(i).get_Vtx0().y() );
			bank.setDouble("Vtx0_z",  i,candlist.get(i).get_Vtx0().z() );			
			bank.setDouble("p0_x",  i,candlist.get(i).get_pAtOrig().x() );
			bank.setDouble("p0_y",  i,candlist.get(i).get_pAtOrig().y() );
			bank.setDouble("p0_z",  i,candlist.get(i).get_pAtOrig().z() );
			bank.setInt("Cross1_ID", i,candlist.get(i).get(0).get_Id());
			bank.setInt("Cross2_ID", i,candlist.get(i).get(1).get_Id());
			bank.setInt("Cross3_ID", i,candlist.get(i).get(2).get_Id());		
			bank.setInt("status", i, candlist.get(i).status);
		}
		
		return bank;
		
	}

	DCSwimmer swim2 = new DCSwimmer();
	public EvioDataBank fillR3CrossfromMCTrack(EvioDataEvent event) {
		
		EvioDataBank gbank = (EvioDataBank) event.getBank("GenPart::true");
        
        double[] vx = gbank.getDouble("vx");
        double[] vy = gbank.getDouble("vy");
        double[] vz = gbank.getDouble("vz");
        double[] px = gbank.getDouble("px");
        double[] py = gbank.getDouble("py");
        double[] pz = gbank.getDouble("pz");
        int[] pid = gbank.getInt("pid");
        
        
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("DEBUG::R3Cross", vx.length);
		
		for(int i =0; i< vx.length; i++) {
			
			if(pid[i]==22)
			continue;
			
			double theta = Math.acos(pz[i]/Math.sqrt(px[i]*px[i]+py[i]*py[i]+pz[i]*pz[i]));
			double theta_deg = Math.toDegrees(theta);
			
			if(theta_deg<5 || theta_deg>35)
				continue;
			
			int q = (int) Math.signum(pid[i]);
			if(Math.abs(pid[i])==11 || Math.abs(pid[i])==13) // charge is -pid for e and mu
					q = -q;
			
			double phi = Math.toDegrees(Math.atan2(vy[i], vx[i]));
			double ang = phi + 30;
			while (ang < 0) {
				ang += 360;
			}
			int sector = 1 + (int)(ang/60.);
			
			if(sector ==7 )
				sector =6;
			
			// rotate in the tilted sector coord sys
			double rx =  vx[i]*Math.cos((sector-1)*Math.toRadians(-60.))-vy[i]*Math.sin((sector-1)*Math.toRadians(-60.));
	        double sy =  vx[i]*Math.sin((sector-1)*Math.toRadians(-60.))+vy[i]*Math.cos((sector-1)*Math.toRadians(-60.));
	        double sz =  -rx*Math.sin(Math.toRadians(-25.))+vz[i]*Math.cos(Math.toRadians(-25.));
	        double sx =  rx*Math.cos(Math.toRadians(-25.))+vz[i]*Math.sin(Math.toRadians(-25.));
	        
	        double rpx =  px[i]*Math.cos((sector-1)*Math.toRadians(-60.))-py[i]*Math.sin((sector-1)*Math.toRadians(-60.));
	        double spy =  px[i]*Math.sin((sector-1)*Math.toRadians(-60.))+py[i]*Math.cos((sector-1)*Math.toRadians(-60.));
	        double spz =  -rpx*Math.sin(Math.toRadians(-25.))+pz[i]*Math.cos(Math.toRadians(-25.));
	        double spx =  rpx*Math.cos(Math.toRadians(-25.))+pz[i]*Math.sin(Math.toRadians(-25.));
	        
	        spx/=1000;
	        spy/=1000;
	        spz/=1000;
	        
	        
	        swim2.SetSwimParameters(sx, sy, sz, spx, spy, spz, q);
	      
	        
	    	double[] result = swim2.SwimToPlane(469.37);
	    	
	    	double cx = result[0];
	    	double cy = result[1];
	    	double cz = result[2];
	    	double cpx = result[3];
	    	double cpy = result[4];
	    	double cpz = result[5];
	        
	    	double p = Math.sqrt(cpx*cpx+cpy*cpy+cpz*cpz);
	    	
	        
	        Cross cross1 = new Cross(sector, 3, -1);
			
			Point3D crossPos = cross1.getCoordsInLab(cx, cy, cz);
			Point3D crossDir = cross1.getCoordsInLab(cpx/p, cpy/p, cpz/p);
			
			bank.setDouble("c3_x", i, crossPos.x());
			bank.setDouble("c3_y", i, crossPos.y());
			bank.setDouble("c3_z", i, crossPos.z());
			bank.setDouble("c3_ux", i, crossDir.x());
			bank.setDouble("c3_uy", i, crossDir.y());
			bank.setDouble("c3_uz", i, crossDir.z());

		}
		return bank;
		
		
	}


	/**
	 * 
	 * @param event the EvioEvent
	 * @return hits bank
	 *
	 */
	public EvioDataBank fillTBHitsBank(EvioDataEvent event,List<FittedHit> hitlist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBHits", hitlist.size());
      
		for(int i =0; i< hitlist.size(); i++) {
			bank.setInt("id",i, hitlist.get(i).get_Id());
			bank.setInt("superlayer",i, hitlist.get(i).get_Superlayer());
			bank.setInt("layer",i, hitlist.get(i).get_Layer());
			bank.setInt("sector",i, hitlist.get(i).get_Sector());
			bank.setInt("wire",i, hitlist.get(i).get_Wire());
			
			bank.setDouble("X",i, hitlist.get(i).get_X());
			bank.setDouble("Z",i, hitlist.get(i).get_Z());
			bank.setInt("LR",i, hitlist.get(i).get_LeftRightAmb());
			
			bank.setDouble("time",i, hitlist.get(i).get_Time());
			bank.setDouble("doca",i, hitlist.get(i).get_Doca());
			bank.setDouble("docaError",i, hitlist.get(i).get_DocaErr());
			bank.setDouble("trkDoca", i, hitlist.get(i).get_ClusFitDoca());
			
			bank.setInt("clusterID", i, hitlist.get(i).get_AssociatedClusterID());
			bank.setInt("trkID", i, hitlist.get(i).get_AssociatedTBTrackID());
			bank.setDouble("timeResidual", i, hitlist.get(i).get_TimeResidual());
			
		}
		
		return bank;

	}

	/**
	 * 
	 * @param event the EvioEvent
	 * @return clusters bank
	 */
	public EvioDataBank fillTBClustersBank(EvioDataEvent event, List<FittedCluster> cluslist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBClusters", cluslist.size());

		int[] hitIdxArray= new int[12];
		
		for(int i =0; i< cluslist.size(); i++) {
			for(int j =0; j<hitIdxArray.length; j++) {
				hitIdxArray[j] = -1;
			}
			double chi2 = 0;
			
			bank.setInt("ID",i, cluslist.get(i).get_Id());
			bank.setInt("superlayer",i, cluslist.get(i).get_Superlayer());
			bank.setInt("sector",i, cluslist.get(i).get_Sector());			

			bank.setDouble("avgWire", i, cluslist.get(i).getAvgwire());
			bank.setInt("size", i, cluslist.get(i).size());
			
			double fitSlope= cluslist.get(i).get_clusterLineFitSlope();			
			double fitInterc =cluslist.get(i).get_clusterLineFitIntercept();
			
			bank.setDouble("fitSlope", i, fitSlope);
			bank.setDouble("fitSlopeErr", i, cluslist.get(i).get_clusterLineFitSlopeErr());
			bank.setDouble("fitInterc", i, fitInterc);
			bank.setDouble("fitIntercErr", i, cluslist.get(i).get_clusterLineFitInterceptErr());
			
			for(int j = 0; j<cluslist.get(i).size(); j++) {		
				if(j<hitIdxArray.length)
					hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
				
				double residual = cluslist.get(i).get(j).get_ClusFitDoca()/(cluslist.get(i).get(j).get_CellSize()/Math.sqrt(12.));
				chi2+= residual*residual;
			}
			bank.setDouble("fitChisqProb", i, ProbChi2perNDF.prob(chi2, cluslist.get(i).size()-2));
			
			
			for(int j =0; j<hitIdxArray.length; j++) {
				String hitStrg = "Hit";
				hitStrg+=(j+1);
				hitStrg+="_ID";
				bank.setInt(hitStrg, i, hitIdxArray[j]);
			}
		}

		return bank;
		
	}
	
	/**
	 * 
	 * @param event the EvioEvent
	 * @return segments bank
	 */
	public EvioDataBank fillTBSegmentsBank(EvioDataEvent event, List<Segment> seglist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBSegments", seglist.size());
    
		int[] hitIdxArray= new int[12];
				
		for(int i =0; i< seglist.size(); i++) {
			for(int j =0; j<hitIdxArray.length; j++) {
				hitIdxArray[j] = -1;
			}
			
			double chi2 =0;
			
			bank.setInt("ID",i, seglist.get(i).get_Id());
			bank.setInt("superlayer",i, seglist.get(i).get_Superlayer());
			bank.setInt("sector",i, seglist.get(i).get_Sector());	
			FittedCluster cls= seglist.get(i).get_fittedCluster();
			bank.setInt("Cluster_ID",  i, cls.get_Id());
			
			bank.setDouble("avgWire", i, cls.getAvgwire());
			bank.setInt("size", i, seglist.get(i).size());
			bank.setDouble("fitSlope", i, cls.get_clusterLineFitSlope());
			bank.setDouble("fitSlopeErr", i, cls.get_clusterLineFitSlopeErr());
			bank.setDouble("fitInterc", i, cls.get_clusterLineFitIntercept());
			bank.setDouble("fitIntercErr", i, cls.get_clusterLineFitInterceptErr());
			
			bank.setDouble("SegEndPoint1X", i, seglist.get(i).get_SegmentEndPoints()[0]);
			bank.setDouble("SegEndPoint1Z", i, seglist.get(i).get_SegmentEndPoints()[1]);
			bank.setDouble("SegEndPoint2X", i, seglist.get(i).get_SegmentEndPoints()[2]);
			bank.setDouble("SegEndPoint2Z", i, seglist.get(i).get_SegmentEndPoints()[3]);
			
			for(int j = 0; j<seglist.get(i).size(); j++) {	
				if(j<hitIdxArray.length)
					hitIdxArray[j] = seglist.get(i).get(j).get_Id();
				
				double residual = seglist.get(i).get(j).get_ClusFitDoca()/(seglist.get(i).get(j).get_CellSize()/Math.sqrt(12.));
				chi2+= residual*residual;
			}
			bank.setDouble("fitChisqProb", i, ProbChi2perNDF.prob(chi2, seglist.get(i).size()-2));
			
			
			for(int j =0; j<hitIdxArray.length; j++) {
				String hitStrg = "Hit";
				hitStrg+=(j+1);
				hitStrg+="_ID";
				bank.setInt(hitStrg, i, hitIdxArray[j]);
			}
		}

		return bank;
		
	}

	/**
	 * 
	 * @param event the EvioEvent
	 * @return segments bank
	 */
	public EvioDataBank fillTBSegmentsTrajectoryBank(EvioDataEvent event, List<Segment> seglist) {
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBSegmentTrajectory", seglist.size()*6);
	   	
		int index =0;
		for(int i =0; i< seglist.size(); i++) {
			SegmentTrajectory trj = seglist.get(i).get_Trajectory();
			for(int l =0; l<6; l++) {
				bank.setInt("segmentID",index, trj.get_SegmentId());
				bank.setInt("sector",index, trj.get_Sector());	
				bank.setInt("superlayer",index, trj.get_Superlayer());
				bank.setInt("layer",index, (l+1));
				bank.setInt("matchedHitID", index, trj.getMatchedHitId()[l]);
				bank.setDouble("trkDoca", index, trj.getTrkDoca()[l]);
				index++;
			}
		}
		return bank;
	}
	/**
	 * 
	 * @param event the EvioEvent
	 * @return crosses bank
	 */
	public EvioDataBank fillTBCrossesBank(EvioDataEvent event, List<Cross> crosslist) {

		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBCrosses", crosslist.size());
    
		for(int i =0; i< crosslist.size(); i++) {
			bank.setInt("ID",i, crosslist.get(i).get_Id());
			bank.setInt("sector",i, crosslist.get(i).get_Sector());	
			bank.setInt("region", i, crosslist.get(i).get_Region());
			bank.setDouble("x", i, crosslist.get(i).get_Point().x());
			bank.setDouble("y", i, crosslist.get(i).get_Point().y());
			bank.setDouble("z", i, crosslist.get(i).get_Point().z());
			bank.setDouble("err_x", i, crosslist.get(i).get_PointErr().x());
			bank.setDouble("err_y", i, crosslist.get(i).get_PointErr().y());
			bank.setDouble("err_z", i, crosslist.get(i).get_PointErr().z());
			bank.setDouble("ux", i, crosslist.get(i).get_Dir().x());
			bank.setDouble("uy", i, crosslist.get(i).get_Dir().y());
			bank.setDouble("uz", i, crosslist.get(i).get_Dir().z());
			bank.setDouble("err_ux", i, crosslist.get(i).get_DirErr().x());
			bank.setDouble("err_uy", i, crosslist.get(i).get_DirErr().y());
			bank.setDouble("err_uz", i, crosslist.get(i).get_DirErr().z());
			bank.setInt("Segment1_ID", i, crosslist.get(i).get_Segment1().get_Id());
			bank.setInt("Segment2_ID", i, crosslist.get(i).get_Segment2().get_Id());
			
		}

		return bank;
		
	}
	
	/**
	 * 
	 * @param event the EvioEvent
	 * @return segments bank
	 */
	public EvioDataBank fillTBTracksBank(EvioDataEvent event, List<Track> candlist) {
		
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("TimeBasedTrkg::TBTracks",candlist.size());
    
		for(int i =0; i< candlist.size(); i++) { 
			bank.setInt("ID",i, candlist.get(i).get_Id());
			bank.setInt("sector",i, candlist.get(i).get_Sector());	
			bank.setInt("q", i, candlist.get(i).get_Q());
			bank.setDouble("p", i, candlist.get(i).get_P());
			bank.setDouble("c1_x", i, candlist.get(i).get_PreRegion1CrossPoint().x());
			bank.setDouble("c1_y", i, candlist.get(i).get_PreRegion1CrossPoint().y());
			bank.setDouble("c1_z", i, candlist.get(i).get_PreRegion1CrossPoint().z());
			bank.setDouble("c1_ux", i, candlist.get(i).get_PreRegion1CrossDir().x());
			bank.setDouble("c1_uy", i, candlist.get(i).get_PreRegion1CrossDir().y());
			bank.setDouble("c1_uz", i, candlist.get(i).get_PreRegion1CrossDir().z());
			bank.setDouble("c3_x", i, candlist.get(i).get_PostRegion3CrossPoint().x());
			bank.setDouble("c3_y", i, candlist.get(i).get_PostRegion3CrossPoint().y());
			bank.setDouble("c3_z", i, candlist.get(i).get_PostRegion3CrossPoint().z());
			bank.setDouble("c3_ux", i, candlist.get(i).get_PostRegion3CrossDir().x());
			bank.setDouble("c3_uy", i, candlist.get(i).get_PostRegion3CrossDir().y());
			bank.setDouble("c3_uz", i, candlist.get(i).get_PostRegion3CrossDir().z());
			bank.setDouble("t1_x", i, candlist.get(i).get_Region1TrackX().x());
			bank.setDouble("t1_y", i, candlist.get(i).get_Region1TrackX().y());
			bank.setDouble("t1_z", i, candlist.get(i).get_Region1TrackX().z());
			bank.setDouble("t1_px", i, candlist.get(i).get_Region1TrackP().x());
			bank.setDouble("t1_py", i, candlist.get(i).get_Region1TrackP().y());
			bank.setDouble("t1_pz", i, candlist.get(i).get_Region1TrackP().z()); 
			bank.setDouble("pathlength", i, candlist.get(i).get_TotPathLen()); 
			bank.setDouble("Vtx0_x",  i,candlist.get(i).get_Vtx0().x() );
			bank.setDouble("Vtx0_y",  i,candlist.get(i).get_Vtx0().y() );
			bank.setDouble("Vtx0_z",  i,candlist.get(i).get_Vtx0().z() );
			bank.setDouble("p0_x",  i,candlist.get(i).get_pAtOrig().x() );
			bank.setDouble("p0_y",  i,candlist.get(i).get_pAtOrig().y() );
			bank.setDouble("p0_z",  i,candlist.get(i).get_pAtOrig().z() );
			bank.setInt("Cross1_ID", i,candlist.get(i).get(0).get_Id());
			bank.setInt("Cross2_ID", i,candlist.get(i).get(1).get_Id());
			bank.setInt("Cross3_ID", i,candlist.get(i).get(2).get_Id());
			bank.setInt("status", i, candlist.get(i).status);
			Matrix covMat = new Matrix(5,5);
			if(candlist.get(i).get_CovMat()!=null)
				covMat = candlist.get(i).get_CovMat();
			
			double[][] c = new double[covMat.getRowDimension()][covMat.getColumnDimension()];		
			
			for(int rw = 0; rw< covMat.getRowDimension(); rw++) {
				for(int cl = 0; cl< covMat.getColumnDimension(); cl++) {
					c[rw][cl] = covMat.get(rw, cl);
				}	
			}
			bank.setDouble("C11", i, c[0][0]);
			bank.setDouble("C12", i, c[0][1]);
			bank.setDouble("C13", i, c[0][2]);
			bank.setDouble("C14", i, c[0][3]);
			bank.setDouble("C15", i, c[0][4]);
			bank.setDouble("C21", i, c[1][0]);
			bank.setDouble("C22", i, c[1][1]);
			bank.setDouble("C23", i, c[1][2]);
			bank.setDouble("C24", i, c[1][3]);
			bank.setDouble("C25", i, c[1][4]);
			bank.setDouble("C31", i, c[2][0]);
			bank.setDouble("C32", i, c[2][1]);
			bank.setDouble("C33", i, c[2][2]);
			bank.setDouble("C34", i, c[2][3]);
			bank.setDouble("C35", i, c[2][4]);
			bank.setDouble("C41", i, c[3][0]);
			bank.setDouble("C42", i, c[3][1]);
			bank.setDouble("C43", i, c[3][2]);
			bank.setDouble("C44", i, c[3][3]);
			bank.setDouble("C45", i, c[3][4]);
			bank.setDouble("C51", i, c[4][0]);
			bank.setDouble("C52", i, c[4][1]);
			bank.setDouble("C53", i, c[4][2]);
			bank.setDouble("C54", i, c[4][3]);
			bank.setDouble("C55", i, c[4][4]);
			
			bank.setDouble("fitChisq", i, candlist.get(i).get_FitChi2());
		}
		
		return bank;
		
	}
	
	
	
	
	public List<FittedHit> createRawHitList(List<Hit> hits) {
		
		List<FittedHit> fhits = new ArrayList<FittedHit>();
		
		for(int i = 0; i<hits.size(); i++) {
			
			FittedHit fhit = new FittedHit(hits.get(i).get_Sector(), hits.get(i).get_Superlayer(),
					hits.get(i).get_Layer(), hits.get(i).get_Wire(), hits.get(i).get_Time(),hits.get(i).get_DocaErr(), hits.get(i).get_Id());
			fhit.set_Doca(hits.get(i).get_Doca());
			fhits.add(fhit);
		}
		return fhits;
	}
	
	public void fillAllHBBanks(DataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
			List<Segment> segments, List<Cross> crosses,
			List<Track> trkcands) {
		
		if(event == null)
			return;
		
		
		if(trkcands!=null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillHBSegmentsBank((EvioDataEvent) event, segments),
						rbc.fillHBCrossesBank((EvioDataEvent) event, crosses), 
						rbc.fillHBTracksBank((EvioDataEvent) event, trkcands)
						);
				
		}
		if(crosses!=null && trkcands == null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillHBSegmentsBank((EvioDataEvent) event, segments),
						rbc.fillHBCrossesBank((EvioDataEvent) event, crosses)
						);
		}
		if(segments!=null && crosses == null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillHBSegmentsBank((EvioDataEvent) event, segments)
						);
		}
		if(clusters!=null && segments == null) {

			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillHBClustersBank((EvioDataEvent) event, clusters)
					);
		}
		if(fhits!=null && clusters == null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits)
					);
		}
	}
	

	public void fillAllTBBanks(EvioDataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
			List<Segment> segments, List<Cross> crosses,
			List<Track> trkcands) {
		
		if(event == null)
			return;
		
		if(trkcands!=null) {
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillTBSegmentsBank((EvioDataEvent) event, segments),
						rbc.fillTBCrossesBank((EvioDataEvent) event, crosses), 
						rbc.fillTBTracksBank((EvioDataEvent) event, trkcands));
				
		}
		if(crosses!=null && trkcands == null) {
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillTBSegmentsBank((EvioDataEvent) event, segments),
						rbc.fillTBCrossesBank((EvioDataEvent) event, crosses));
		}
		if(segments!=null && crosses == null) {
		event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillTBSegmentsBank((EvioDataEvent) event, segments));
		}
		if(segments!=null && crosses == null) {

			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillTBSegmentsBank((EvioDataEvent) event, segments));
		}
		if(clusters!=null && segments == null) {
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters));
		}
		
		if(fhits!=null && clusters == null)
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits));
	}
	
	public void fillAllHBBanksCalib(EvioDataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
			List<Segment> segments, List<Cross> crosses,
			List<Track> trkcands) {
		
		if(event == null)
			return;
		
		
		if(trkcands!=null) {	
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillHBSegmentsBank((EvioDataEvent) event, segments),
					rbc.fillHBSegmentsTrajectoryBank((EvioDataEvent) event, segments),
					rbc.fillHBCrossesBank((EvioDataEvent) event, crosses), 
					rbc.fillHBTracksBank((EvioDataEvent) event, trkcands)
					);
				
		}
		if(crosses!=null && trkcands == null) {			
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillHBSegmentsBank((EvioDataEvent) event, segments),
					rbc.fillHBSegmentsTrajectoryBank((EvioDataEvent) event, segments),
					rbc.fillHBCrossesBank((EvioDataEvent) event, crosses)
					);			
		}
		if(segments!=null && crosses == null) {			
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillHBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillHBSegmentsBank((EvioDataEvent) event, segments),
					rbc.fillHBSegmentsTrajectoryBank((EvioDataEvent) event, segments)
					);		
		}
		if(clusters!=null && segments == null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillHBClustersBank((EvioDataEvent) event, clusters)
					);
		}
		if(fhits!=null && clusters == null) {
			event.appendBanks(rbc.fillHBHitsBank((EvioDataEvent) event, fhits)
					);
		}
	}
	

	public void fillAllTBBanksCalib(EvioDataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
			List<Segment> segments, List<Cross> crosses,
			List<Track> trkcands) {
		
		if(event == null)
			return;
		
		if(trkcands!=null) {		
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillTBSegmentsBank((EvioDataEvent) event, segments),
					rbc.fillTBSegmentsTrajectoryBank((EvioDataEvent) event, segments),
					rbc.fillTBCrossesBank((EvioDataEvent) event, crosses), 
					rbc.fillTBTracksBank((EvioDataEvent) event, trkcands));			
		}
		if(crosses!=null && trkcands == null) {			
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillTBSegmentsBank((EvioDataEvent) event, segments),
					rbc.fillTBSegmentsTrajectoryBank((EvioDataEvent) event, segments),
					rbc.fillTBCrossesBank((EvioDataEvent) event, crosses));			
		}
		if(segments!=null && crosses == null) {		
				event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
						rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
						rbc.fillTBSegmentsBank((EvioDataEvent) event, segments),
						rbc.fillTBSegmentsTrajectoryBank((EvioDataEvent) event, segments));		
		}
		if(segments!=null && crosses == null) {
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters),
					rbc.fillTBSegmentsBank((EvioDataEvent) event, segments));
		}
		if(clusters!=null && segments == null) {
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits),
					rbc.fillTBClustersBank((EvioDataEvent) event, clusters));
		}
		
		if(fhits!=null && clusters == null)
			event.appendBanks(rbc.fillTBHitsBank((EvioDataEvent) event, fhits));
	}
	
}
