package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;
import org.jlab.rec.dc.trajectory.TrajectoryFinder;
import org.jlab.rec.dc.trajectory.Vertex;

/**
 * A class with a method implementing an algorithm that finds lists of track candidates in the DC
 * @author ziegler
 *
 */

public class TrackCandListFinder {

	/**
	 * the tracking status = HitBased or TimeBased
	 */
	private String trking;
	
	public TrackCandListFinder(String stat) {
		trking = stat;
	}
	public DCSwimmer dcSwim = new DCSwimmer();
	/**
	 * 
	 * @param crossList the input list of crosses
	 * @return a list of track candidates in the DC
	 */
	public List<Track> getTrackCands(CrossList crossList) {
		List<Track> cands = new ArrayList<Track>();
		if(crossList.size()==0) {
			System.err.print("Error no tracks found");
			return cands;
		}
		
		for(int i = 0; i<crossList.size(); i++) {
			Track cand = new Track();
			List<Cross> crossesInTrk = crossList.get(i);
			TrajectoryFinder trjFind = new TrajectoryFinder();
			
			Trajectory traj = trjFind.findTrajectory(crossesInTrk);
            if(traj == null) 
            	continue;
            
			if(crossesInTrk.size()==3) {
							
				cand.addAll(crossesInTrk);
				
				cand.set_Sector(crossesInTrk.get(0).get_Sector());
				
				//cand.set_Region3CrossPoint();
				//cand.set_Region3CrossDir();
				
				cand.set_Trajectory(traj.get_Trajectory());
				cand.set_IntegralBdl(traj.get_IntegralBdl());
				

				if(cand.size()==3) {
					double theta3 = Math.atan(cand.get(2).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
			        double theta1 = Math.atan(cand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitSlope());
			        
			        double deltaTheta = theta3-theta1; 
			       
			        double iBdl = traj.get_IntegralBdl(); 
			        
			        double pxz = Math.abs(Constants.LIGHTVEL*iBdl/deltaTheta);
			        double thX = (cand.get(0).get_Dir().x()/cand.get(0).get_Dir().z());
			        double thY = (cand.get(0).get_Dir().y()/cand.get(0).get_Dir().z());
			        double py = Math.sqrt( (thX*thX+thY*thY+1)/(thX*thX+1) - 1 )*pxz;
			          
			        //positive charges bend outward for nominal GEMC field configuration
					int q = (int) Math.signum(deltaTheta); 
					
					q*=-1*Constants.getTORSCALE();						
					
					if(iBdl == 0 || (deltaTheta== 0)) {
						System.err.print("Error in estimating track candidate trajectory: integral_B_dl not found, no trajectory...");
					}

					if(iBdl != 0 || (deltaTheta != 0)) {
						
						double p = Math.sqrt(pxz*pxz+py*py);
						
						if(p>Constants.MAXTRKMOM || p< Constants.MINTRKMOM)
							continue;
						
						int totNbOfIterations = 10;
						int iterationNb = 0;
						
						cand.set_Q(q);
						// momentum correction using the swam trajectory iBdl
						cand.set_P(p);
						
						double fitChisq = Double.POSITIVE_INFINITY ;
						
						StateVec VecAtReg3MiddlePlane = new StateVec(cand.get(2).get_Point().x(),cand.get(2).get_Point().y(),
								cand.get(2).get_Dir().x()/cand.get(2).get_Dir().z(), cand.get(2).get_Dir().y()/cand.get(2).get_Dir().z());
						
						StateVec VecAtReg1MiddlePlane = new StateVec(cand.get(0).get_Point().x(),cand.get(0).get_Point().y(),
								cand.get(0).get_Dir().x()/cand.get(0).get_Dir().z(), cand.get(0).get_Dir().y()/cand.get(0).get_Dir().z());
												
						
						if(VecAtReg1MiddlePlane!=null) {
							if(trking == "TimeBased") {
								totNbOfIterations = 20;
							} else {
								totNbOfIterations = 10;
							}
								
							while(iterationNb < totNbOfIterations) {
								cand.set_StateVecAtReg1MiddlePlane(VecAtReg1MiddlePlane); 	
								
								KalFit kf = new KalFit(cand, "wires");
								if(kf.KalFitFail==true) {
									break;
								}
								
								kf.runKalFit(); 
								
								if(kf.chi2>fitChisq || kf.chi2>Constants.MAXCHI2+1 || Math.abs(kf.chi2-fitChisq)<0.0000001) {
									iterationNb = totNbOfIterations;
									continue;
								}
								if(!Double.isNaN(kf.KF_p) && kf.KF_p>Constants.MINTRKMOM) {
									cand.set_P(kf.KF_p);								
									cand.set_Q(kf.KF_q);
									cand.set_CovMat(kf.covMat);
									
									VecAtReg3MiddlePlane = new StateVec(kf.stateVec[0],kf.stateVec[1],kf.stateVec[2],kf.stateVec[3]);
									
									double pz = cand.get_P() / Math.sqrt(kf.stateVec[2]*kf.stateVec[2] + kf.stateVec[3]*kf.stateVec[3] + 1);
									
									dcSwim.SetSwimParameters(kf.stateVec[0],kf.stateVec[1],cand.get(2).get_Point().z(),
											-pz*kf.stateVec[2],-pz*kf.stateVec[3],-pz,
											 -cand.get_Q());

									double[] VecAtR1 = dcSwim.SwimToPlane(cand.get(0).get_Point().z());
									double xOr = VecAtR1[0];
									double yOr = VecAtR1[1];								
									double pxOr = -VecAtR1[3];
									double pyOr = -VecAtR1[4];
									double pzOr = -VecAtR1[5];
									VecAtReg1MiddlePlane = new StateVec(xOr,yOr,pxOr/pzOr,pyOr/pzOr);
									
								}
								fitChisq = kf.chi2;
								iterationNb++;
								
								cand.set_FitChi2(fitChisq); 
							}
							
						}	
						
						this.setTrackPars(cand, traj, trjFind, VecAtReg3MiddlePlane, cand.get(2).get_Point().z());
						
						if(cand.fit_Successful==false)
							continue;
						
						if((iterationNb>0 && cand.get_FitChi2()>Constants.MAXCHI2) || 
								(iterationNb!=0 && cand.get_FitChi2()==0))
							continue; // fails if after KF chisq exceeds cutoff or if KF fails 
						
							
						cand.set_Id(cands.size());
						
						cands.add(cand); 
					
					}
				}
			}
		}
		//this.setAssociatedIDs(cands);
		return cands;
	}
	
	private int getSector(double x, double y) {
		double phi = Math.toDegrees(FastMath.atan2(y, x));
		double ang = phi + 30;
		while (ang < 0) {
			ang += 360;
		}
		int sector = 1 + (int)(ang/60.);
		
		if(sector ==7 )
			sector =6;
		
		if ((sector < 1) || (sector > 6)) {
			System.err.println("Track sector not found....");
		}
		return sector;
	}
	
	public void setTrackPars(Track cand, Trajectory traj, TrajectoryFinder trjFind, StateVec stateVec, double z) {
		double pz = cand.get_P() / Math.sqrt(stateVec.tanThetaX()*stateVec.tanThetaX() + stateVec.tanThetaY()*stateVec.tanThetaY() + 1);
		
		
		dcSwim.SetSwimParameters(stateVec.x(),stateVec.y(),z,
				pz*stateVec.tanThetaX(),pz*stateVec.tanThetaY(),pz,
				 cand.get_Q());
		
		// swimming to a ref points outside of the last DC region
		double[] VecAtTarOut = dcSwim.SwimToPlane(592);
		double xOuter  = VecAtTarOut[0];
		double yOuter  = VecAtTarOut[1];
		double zOuter  = VecAtTarOut[2];
		double uxOuter = VecAtTarOut[3]/cand.get_P();
		double uyOuter = VecAtTarOut[4]/cand.get_P();
		double uzOuter = VecAtTarOut[5]/cand.get_P();
		Cross crossR = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
		Point3D xOuterExtp = crossR.getCoordsInLab(xOuter, yOuter, zOuter);
		Point3D uOuterExtp = crossR.getCoordsInLab(uxOuter, uyOuter, uzOuter);
		
		//set the pseudocross at extrapolated position
		cand.set_PostRegion3CrossPoint(xOuterExtp);
		cand.set_PostRegion3CrossDir(uOuterExtp);
		
		dcSwim.SetSwimParameters(stateVec.x(),stateVec.y(),z,
				-pz*stateVec.tanThetaX(),-pz*stateVec.tanThetaY(),-pz,
				 -cand.get_Q());
		
		//swimming to a ref point upstream of the first DC region
		double[] VecAtTarIn = dcSwim.SwimToPlane(180);
		
		if(VecAtTarIn==null) {
			cand.fit_Successful=false;
			return;
		}
		
		if(VecAtTarIn[6]+VecAtTarOut[6]<200) {
			cand.fit_Successful=false;
			return;
		}
		
		double xOr = VecAtTarIn[0];
		double yOr = VecAtTarIn[1];
		double zOr = VecAtTarIn[2];
		double pxOr = -VecAtTarIn[3];
		double pyOr = -VecAtTarIn[4];
		double pzOr = -VecAtTarIn[5];
		
		if(traj!=null && trjFind!=null)
			traj.set_Trajectory(trjFind.getStateVecsAlongTrajectory(xOr, yOr, pxOr/pzOr, pyOr/pzOr, cand.get_P(),cand.get_Q()));
		
		//cand.set_Vtx0_TiltedCS(trakOrigTiltSec);
		//cand.set_pAtOrig_TiltedCS(pAtOrigTiltSec.toVector3D());
		
		Cross C = new Cross(cand.get(2).get_Sector(), cand.get(2).get_Region(), -1);
		
		Point3D trkR1X = C.getCoordsInLab(xOr,yOr,zOr);
		Point3D trkR1P = C.getCoordsInLab(pxOr,pyOr,pzOr);
		cand.set_Region1TrackX(new Point3D(trkR1X.x(), trkR1X.y(), trkR1X.z()));
		cand.set_Region1TrackP(new Point3D(trkR1P.x(), trkR1P.y(), trkR1P.z()));
		
		Point3D R3TrkPoint = C.getCoordsInLab(stateVec.x(),stateVec.y(),z);
		Point3D R3TrkMomentum = C.getCoordsInLab(pz*stateVec.tanThetaX(),pz*stateVec.tanThetaY(),pz);
		
		
		dcSwim.SetSwimParameters(R3TrkPoint.x(), R3TrkPoint.y(), R3TrkPoint.z(), -R3TrkMomentum.x(), -R3TrkMomentum.y(), -R3TrkMomentum.z(), -cand.get_Q());
		double[] VecAtTarlab0 = dcSwim.SwimToPlaneLab(0.0);
		Vertex vtx = new Vertex();
		double[] Bvtx = new double[]{0.0,0.0};
		double[] Vt = vtx.VertexParams(VecAtTarlab0[0], VecAtTarlab0[1], VecAtTarlab0[2], -VecAtTarlab0[3], -VecAtTarlab0[4], -VecAtTarlab0[5], (double) cand.get_Q(), dcSwim.BfieldLab(VecAtTarlab0[0], VecAtTarlab0[1], VecAtTarlab0[2]).toVector3D().mag(), Bvtx[0], Bvtx[1]);
		//Vt = new double[]{VecAtTarlab0[0], VecAtTarlab0[1], VecAtTarlab0[2], -VecAtTarlab0[3], -VecAtTarlab0[4], -VecAtTarlab0[5],0};
		int sectorNearTarget = this.getSector(Vt[0], Vt[1]);
		
		int status = -1;
		if(sectorNearTarget==cand.get(0).get_Sector()) {
			status = 1;
		} else {
			status = 0;
		}
		
		double xOrFix = Vt[0];
		double yOrFix = Vt[1];
		double zOrFix = Vt[2];
		double pxOrFix = Vt[3];
		double pyOrFix = Vt[4];
		double pzOrFix = Vt[5];
		double arclen = Vt[6];
		
		double totPathLen = VecAtTarlab0[6] + VecAtTarOut[6] + arclen;
		cand.set_TotPathLen(totPathLen);
		
		cand.set_Vtx0(new Point3D(xOrFix,yOrFix, zOrFix));
		cand.set_pAtOrig(new Vector3D(pxOrFix, pyOrFix, pzOrFix));
	
		double[] VecAtHtccSurf = dcSwim.SwimToSphere(20);
		double xInner  = VecAtHtccSurf[0];
		double yInner  = VecAtHtccSurf[1];
		double zInner  = VecAtHtccSurf[2];
		double uxInner = VecAtHtccSurf[3]/cand.get_P();
		double uyInner = VecAtHtccSurf[4]/cand.get_P();
		double uzInner = VecAtHtccSurf[5]/cand.get_P();
		
		//set the pseudocross at extrapolated position
		cand.set_PreRegion1CrossPoint(new Point3D(xInner,yInner,zInner));
		cand.set_PreRegion1CrossDir(new Point3D(uxInner,uyInner,uzInner));
		
		cand.status = status;
		cand.fit_Successful=true;
		cand.set_TrackingInfoString(trking);
	}



	public void removeOverlappingTracks(List<Track> trkcands) {
		
		Collections.sort(trkcands);
		
		
		List<Track> selectedTracks =new ArrayList<Track>();
		
		ArrayList<ArrayList<Track>> lists = new ArrayList<ArrayList<Track>>();
		ArrayList<Track> list = new ArrayList<Track>();
		
		int id = trkcands.get(0).get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
		
		for(int i =0; i<trkcands.size(); i++) { 
			if(trkcands.get(i).get(0).get_Segment1().get(0).get_AssociatedHBTrackID() == id) {
				list.add(trkcands.get(i)); 
				id = trkcands.get(i).get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
			} else {
				lists.add(list); 
				list = new ArrayList<Track>();
				list.add(trkcands.get(i));	
				id = trkcands.get(i).get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
			}
		}
		lists.add(list);
		if(lists.size()==0)
			lists.add(list);
		
		for(int i =0; i<lists.size(); i++) {
			
			Track bestTrk = this.FindBestTrack(lists.get(i));
			if(bestTrk!=null)
				selectedTracks.add(bestTrk);
		}
		
		trkcands.removeAll(trkcands);
		trkcands.addAll(selectedTracks);
		
	}



	private Track FindBestTrack(ArrayList<Track> trkList) {
		double bestChi2 = 9999999;
		Track bestTrk = null;
		
		for(int i =0; i<trkList.size(); i++) {
			if(trkList.get(i).get_FitChi2()<bestChi2) {
				bestChi2 = trkList.get(i).get_FitChi2();
				bestTrk = trkList.get(i);
			}
		}
		return bestTrk;
	}
	


}
