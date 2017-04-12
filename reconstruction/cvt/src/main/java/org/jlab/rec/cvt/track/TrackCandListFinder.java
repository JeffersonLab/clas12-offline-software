package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.fit.HelicalTrackFitter;
import org.jlab.rec.cvt.fit.StraightTrackFitter;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.svt.Geometry;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

import trackfitter.fitter.LineFitPars;
import trackfitter.fitter.LineFitter;
 

/**
 * A class with a method implementing an algorithm that finds lists of track candidates in the BST
 * @author ziegler
 *
 */

public class TrackCandListFinder {
	
	public TrackCandListFinder() {					
	}

	/**
	 * A class representing the measurement variables used to fit a helical track
	 * The track fitting algorithm employs the Karimaki algorithm to determine the circle fit paramters: the doca to the z axis, the phi at the doca, the curvature;
	 * and a linear regression line fitting algorithm to determine z at the doca and the helix dip angle
	 * @author ziegler
	 *
	 */
	private class HelixMeasurements {
		double[] _X ;			// x coordinate array
		double[] _Y ;			// y coordinate array (same size as X array)
		double[] _Z ;			// z coordinate array
		double[] _Rho ;			// rho (= sqrt(x^2 + y^2) for SVT; detector radius for BMT) coordinate array (same size as Z array)
		double[] _ErrZ ;		// z uncertainty (same size as Z array)
		double[] _ErrRho ;		// rho uncertainty (same size as Z array)
		double[] _ErrRt ;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)
		
		HelixMeasurements(double[] X ,
					 double[] Y ,
					 double[] Z ,
					 double[] Rho ,
					 double[] ErrZ ,
					 double[] ErrRho ,
					 double[] ErrRt ) {
            this._X = X;
            this._Y = Y;
            this._Z = Z;
            this._Rho = Rho;
            this._ErrZ = ErrZ;
            this._ErrRho = ErrRho;
            this._ErrRt = ErrRt;
        }
	}
	
	/**
	 * A class representing the measurement variables used to fit a straight track
	 * @author ziegler
	 *
	 */
	private class RayMeasurements {
		double[] _X ;			// x coordinate array
		double[] _Y ;			// y coordinate array (same size as X array)
		double[] _Z ;			// z coordinate array
		double[] _Y_prime ;		// y coordinate array (same size as Z array)
		double[] _ErrZ ;		// z uncertainty (same size as Z array)
		double[] _ErrY_prime ;	// y uncertainty (same size as Z array)
		double[] _ErrRt ;		// sqrt(x^2 + y^2)  uncertainty array (same size as X & Y arrays)

		RayMeasurements(double[] X ,
					 double[] Y ,
					 double[] Z ,
					 double[] Y_prime ,
					 double[] ErrZ ,
					 double[] ErrY_prime ,
					 double[] ErrRt ) {
            this._X = X;
            this._Y = Y;
            this._Z = Z;
            this._Y_prime = Y_prime;
            this._ErrZ = ErrZ;
            this._ErrY_prime = ErrY_prime;
            this._ErrRt = ErrRt;
        }
	}
	/**
	 * 
	 * @param crossList the input list of crosses
	 * @return an array list of track candidates in the SVT
	 */
	public ArrayList<Track> getHelicalTrack(CrossList crossList, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
		
		ArrayList<Track> cands = new ArrayList<Track>();
		
		if(crossList.size()==0) {
			System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
			return cands;
		}
		// instantiating the Helical Track fitter
		HelicalTrackFitter fitTrk = new HelicalTrackFitter();
		// sets the index according to assumption that the track comes from the origin or not
		int shift =0;
		if(org.jlab.rec.cvt.Constants.trk_comesfrmOrig)
			shift =1;

		// interate the fit a number of times set in the constants file
		int Max_Number_Of_Iterations = org.jlab.rec.cvt.svt.Constants.BSTTRKINGNUMBERITERATIONS;
		
		// loop over the cross list and do the fits to the crosses
		for(int i = 0; i<crossList.size(); i++) {
			// for debugging purposes only sets all errors to 1
			boolean ignoreErr = org.jlab.rec.cvt.svt.Constants.ignoreErr;
			
			int Number_Of_Iterations = 0;
			// do till the number of iterations is reached			
			while(Number_Of_Iterations<=Max_Number_Of_Iterations) {
				Number_Of_Iterations++;
				fitTrk = new HelicalTrackFitter();
				// get the measuerement arrays for the helical track fit
				HelixMeasurements MeasArrays = this.get_HelixMeasurementsArrays(crossList.get(i), shift, ignoreErr, false);
				
				double[] X = MeasArrays._X;					
				double[] Y = MeasArrays._Y;
				double[] Z = MeasArrays._Z;
				double[] Rho = MeasArrays._Rho;
				double[] ErrZ = MeasArrays._ErrZ;
				double[] ErrRho = MeasArrays._ErrRho;
				double[] ErrRt = MeasArrays._ErrRt;
				
				// do the fit to X, Y taking ErrRt uncertainties into account to get the circle fit params, 
				// and do the fit to Rho, Z taking into account the uncertainties in Rho and Z into account to get the linefit params
				fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
				
				// if the fit failed then use the uncorrected SVT points since the z-correction in resetting the SVT cross points sometimes fails 
				if(fitTrk.get_helix()==null) {
					//System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
					MeasArrays = this.get_HelixMeasurementsArrays(crossList.get(i), shift, ignoreErr, true);
					
					X = MeasArrays._X;					
					Y = MeasArrays._Y;
					Z = MeasArrays._Z;
					Rho = MeasArrays._Rho;
					ErrZ = MeasArrays._ErrZ;
					ErrRho = MeasArrays._ErrRho;
					ErrRt = MeasArrays._ErrRt;
					
					fitTrk.fit(X, Y, Z, Rho, ErrRt, ErrRho, ErrZ);
					Number_Of_Iterations=Max_Number_Of_Iterations+1;
					//if(fitTrk.get_helix()==null) 
						//System.err.println("Error in Helical Track fitting -- helix not found -- refit FAILED");
				}
				
				// if the fit is successful
				if(fitTrk.get_helix()!=null && fitTrk.getFit()!=null) {	
					Track cand = new Track(fitTrk.get_helix());
					cand.addAll(crossList.get(i));
					//cand.set_HelicalTrack(fitTrk.get_helix());			done in Track constructor			
					cand.update_Crosses(svt_geo);
										
					cand.set_circleFitChi2PerNDF(fitTrk.get_chisq()[0]/(int)(X.length-3)); // 3 fit params					
					cand.set_lineFitChi2PerNDF(fitTrk.get_chisq()[1]/(int)(Z.length-2)); // 2 fit params
																		
					if(Number_Of_Iterations==Max_Number_Of_Iterations) {							
						cands.add(cand); // dump the cand							
					}
				}
			}
		}	
		// remove clones
		ArrayList<Track> passedcands = this.rmHelicalTrkClones(org.jlab.rec.cvt.svt.Constants.removeClones, cands);
		// loop over candidates and set the trajectories
		for(int ic = 0; ic< passedcands.size(); ic++) {
			Helix trkHelix = passedcands.get(ic).get_helix();
			
			TrajectoryFinder trjFind = new TrajectoryFinder();
			
			Trajectory traj = trjFind.findTrajectory(passedcands.get(ic).get_Id(), trkHelix, passedcands.get(ic), svt_geo, bmt_geo, "final");				
				
			passedcands.get(ic).set_Trajectory(traj.get_Trajectory());												
			
			passedcands.get(ic).set_Id(ic);
		
		}
		
		return passedcands;
		
	}


	/**
	 * 
	 * @param SVTCrossList the input list of crosses
	 * @return an array list of track candidates in the SVT
	 */
	public ArrayList<StraightTrack> getStraightTracks(CrossList SVTCrosses, List<Cross>BMTCrosses, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
		
		ArrayList<StraightTrack> cands = new ArrayList<StraightTrack>();
		
		if(SVTCrosses.size()==0) {
			System.err.print("Error in estimating track candidate trajectory: less than 3 crosses found");
			return cands;
		}
		
		StraightTrackFitter fitTrk = new StraightTrackFitter();
		
		for(int i = 0; i<SVTCrosses.size(); i++) {
			ArrayList<Cross> crossesToFit = new ArrayList<Cross>();
			// remove SVT regions
			for(Cross crossInTrackToFit : SVTCrosses.get(i)) {			
				if(crossInTrackToFit.get_Region()!=org.jlab.rec.cvt.svt.Constants.BSTEXCLUDEDFITREGION) {// remove the crosses from the exluded region to fit the track
					crossesToFit.add(crossInTrackToFit);
				}
			}
			
			if(crossesToFit.size()<3) 
				continue;
			
			fitTrk = new StraightTrackFitter();
			RayMeasurements MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false);
			
			
			LineFitter linefitYX = new LineFitter();
	  		boolean linefitresultYX = linefitYX.fitStatus(MeasArrays._Y, MeasArrays._X, MeasArrays._ErrRt, new double[MeasArrays._ErrRt.length], MeasArrays._Y.length);
	  		
	        // prelim cross corrections
  			LineFitPars linefitparsYX = linefitYX.getFit();
  			StraightTrack cand = new StraightTrack(null);
  			cand.addAll(crossesToFit);
  			if(linefitresultYX && linefitparsYX!=null)  {
  				cand.update_Crosses(linefitparsYX.slope(),0,svt_geo);
  			}
  			// update measurements
  			MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, false);
			
  			
  			// fit SVt crosses
			fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
			//create the cand
			if(fitTrk.get_ray()!=null) {
				cand = new StraightTrack(fitTrk.get_ray());
				cand.addAll(crossesToFit);
			}
			if(fitTrk.get_ray()==null) {
				//System.err.println("Error in  Track fitting -- ray not found -- trying to refit using the uncorrected crosses...");
				MeasArrays = this.get_RayMeasurementsArrays(crossesToFit, false, true);
				
				fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
				//create the cand
				cand = new StraightTrack(fitTrk.get_ray());
				cand.addAll(crossesToFit);
				if(fitTrk.get_ray()==null) 
					continue;
					//System.err.println("Error in  Track fitting -- track not found -- refit FAILED");
			}
			cand.update_Crosses(cand.get_ray().get_yxslope(),cand.get_ray().get_yxinterc(),svt_geo);
			
			
			// eliminate bad residuals
			this.EliminateStraightTrackOutliers(crossesToFit, fitTrk, svt_geo);
			if(crossesToFit.size()<3) 
				continue;
			
			fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
			//create the cand
			if(fitTrk.get_ray()!=null) {
				cand = new StraightTrack(fitTrk.get_ray());
				cand.addAll(crossesToFit);
			}
			
			// match to Micromegas
			
			ArrayList<Cross> crossesToFitWithBMT = new ArrayList<Cross>();
			crossesToFitWithBMT.addAll(crossesToFit);
			
			
			ArrayList<Cross> BMTmatches = this.matchTrackToMM(BMTCrosses, cand, bmt_geo);
			crossesToFitWithBMT.addAll(BMTmatches);
			
			// reset the arrays
			RayMeasurements NewMeasArrays = new RayMeasurements(null, null, null, null, null, null, null);
			
			
			for(int iter =0; iter<11; iter++) {
				// refit with Micromegas
				NewMeasArrays = this.get_RayMeasurementsArrays(crossesToFitWithBMT, false, false);
				
				fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);	
				
				//create the cand
				if(fitTrk.get_ray()!=null) {
					cand = new StraightTrack(fitTrk.get_ray());
					cand.addAll(crossesToFit);
					cand.update_Crosses(cand.get_ray().get_yxslope(),cand.get_ray().get_yxinterc(),svt_geo);
					crossesToFitWithBMT = new ArrayList<Cross>();
					crossesToFitWithBMT.addAll(cand);
					crossesToFitWithBMT.addAll(BMTmatches);
					
				}
			}
			// reset the arrays
			//NewMeasArrays = this.get_RayMeasurementsArrays(crossesToFitWithBMT, false, false);
			//fitTrk.fit(MeasArrays._X, MeasArrays._Y, MeasArrays._Z, MeasArrays._Y_prime, MeasArrays._ErrRt, MeasArrays._ErrY_prime, MeasArrays._ErrZ);
			//System.out.println(" *** NEW Refit cand with mm "+cand.get_ray().get_dirVec().toString()+
			//		" slope xy "+fitTrk.get_ray().get_yxslope()+" slope yz "+fitTrk.get_ray().get_yzslope()+
			//		" intec xy "+fitTrk.get_ray().get_yxinterc()+" inter yz "+fitTrk.get_ray().get_yzinterc());
			// if fit OK
			if(fitTrk.get_ray()!=null) {	
				cand = new StraightTrack(fitTrk.get_ray());
				
				cand.addAll(crossesToFit);
				
				//cand.update_Crosses(fitTrk.get_ray().get_yxslope(),fitTrk.get_ray().get_yzslope(),svt_geo);
			
				//System.out.println(" *** Refit cand with mm after reset SVT crosses "+cand.get_ray().get_dirVec().toString()+
				//		" slope xy "+cand.get_ray().get_yxslope()+" slope yz "+cand.get_ray().get_yzslope()+
				//		" intec xy "+cand.get_ray().get_yxinterc()+" inter yz "+cand.get_ray().get_yzinterc());
				//KalFitCosmics kf = new KalFitCosmics(cand, svt_geo);
				//kf.runKalFit(cand, svt_geo); // for now the KF runs on just SVT
				//cand.update_Crosses(fitTrk.get_ray().get_yxslope(),fitTrk.get_ray().get_yzslope(),svt_geo);
				
				// add BMT
				//cand.addAll(BMTCrosses);
				//for(Cross c : cand)
				//	c.set_Dir(cand.get_ray().get_dirVec());
				//cand.set_chi2(fitTrk.get_rayfitoutput().get_chisq()[0]+fitTrk.get_rayfitoutput().get_chisq()[1]);
				cand.set_ndf(NewMeasArrays._Y.length + NewMeasArrays._Y_prime.length -4);
				double chi2 = cand.calc_straightTrkChi2();
				cand.set_chi2(chi2);
				//if(cand.get_ndf()>0) {							
					cands.add(cand); // dump the cand							
				//}
			
			}
		}	
		
		ArrayList<StraightTrack> passedcands = this.rmStraightTrkClones(org.jlab.rec.cvt.svt.Constants.removeClones, cands);
		//ArrayList<StraightTrack> passedcands = this.rmStraightTrkClones(false, cands);
		
		for(int ic = 0; ic< passedcands.size(); ic++) {
			Ray trkRay = passedcands.get(ic).get_ray();
			
			TrajectoryFinder trjFind = new TrajectoryFinder();
			
			Trajectory traj = trjFind.findTrajectory(passedcands.get(ic).get_Id(), trkRay, passedcands.get(ic), svt_geo, bmt_geo);				
				
			passedcands.get(ic).set_Trajectory(traj.get_Trajectory());												
			
			passedcands.get(ic).set_Id(ic);
			
			this.upDateCrossesFromTraj(passedcands.get(ic), traj, svt_geo);
		
		}
		
		return passedcands;
	}


	public void upDateCrossesFromTraj(StraightTrack cand, Trajectory trj, org.jlab.rec.cvt.svt.Geometry geo) {
		
		double[][][] trajPlaneInters = trj.get_SVTIntersections();
		ArrayList<Cross> hitsOnTrack = new ArrayList<Cross>();
		for(Cross c : cand)
			if(c.get_Detector()=="SVT")
				hitsOnTrack.add(c);
		
  		for(int j =0; j< hitsOnTrack.size(); j++) {
  			
  			int l1 = hitsOnTrack.get(j).get_Cluster1().get_Layer();
  			int s = hitsOnTrack.get(j).get_Cluster1().get_Sector();
  			double s1 = hitsOnTrack.get(j).get_Cluster1().get_Centroid();
  			int l2 = hitsOnTrack.get(j).get_Cluster2().get_Layer();
  			double s2 = hitsOnTrack.get(j).get_Cluster2().get_Centroid();
  			
  			double trajX1 = trajPlaneInters[l1-1][s-1][0] ;
			double trajY1 = trajPlaneInters[l1-1][s-1][1] ;
			double trajZ1 = trajPlaneInters[l1-1][s-1][2] ;
			double trajX2 = trajPlaneInters[l2-1][s-1][0] ;
			double trajY2 = trajPlaneInters[l2-1][s-1][1] ;
			double trajZ2 = trajPlaneInters[l2-1][s-1][2] ;
			
			if(trajX1 == -999 || trajX2 == -999)
				continue;
			
			Point3D Point =geo.recalcCrossFromTrajectoryIntersWithModulePlanes( s, s1, s2, l1,  l2,  trajX1,  trajY1,  trajZ1,  trajX2,  trajY2,  trajZ2);
			
			//set the cross to that point
			//System.out.println(" trajX1 "+trajX1+" trajY1 "+trajY1+" trajZ1 "+trajZ1+" trajX2 "+trajX2+" trajY2 "+trajY2+" trajZ2 "+trajZ2);
			hitsOnTrack.get(j).set_Point(Point);
						
			hitsOnTrack.get(j).set_Dir(trj.get_ray().get_dirVec());
			
  		}	
	}
	

	private HelixMeasurements get_HelixMeasurementsArrays(ArrayList<Cross> arrayList,
			int shift, boolean ignoreErr, boolean resetSVTMeas) {
		
		
		ArrayList<Cross> SVTcrossesInTrk = new ArrayList<Cross>();
		ArrayList<Cross> BMTCdetcrossesInTrk = new ArrayList<Cross>();
		ArrayList<Cross> BMTZdetcrossesInTrk = new ArrayList<Cross>();
		
		//make lists
		for(Cross c : arrayList) {
			
			if(c.get_Detector()=="SVT") 
				SVTcrossesInTrk.add(c);
			
			if(c.get_Detector()=="BMT") { // Micromegas
				if(c.get_DetectorType()=="C") {//C-detector --> only Z defined
					BMTCdetcrossesInTrk.add(c); 
				}
				if(c.get_DetectorType()=="Z") {//Z-detector --> only phi defined
					BMTZdetcrossesInTrk.add(c); 
				}
			}
		}
			
			
		double[] X = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()+shift];
		double[] Y = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()+shift];
		double[] Z = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()+shift];
		double[] Rho = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()+shift];
		double[] ErrZ = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()+shift];
		double[] ErrRho = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()+shift];
		double[] ErrRt = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()+shift];
		
		if(shift ==1) {
			X[0] = 0;
			Y[0] = 0;
			Z[0] = 0;
			Rho[0] =0;
			ErrRt[0] = org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT;
			ErrZ[0] = org.jlab.rec.cvt.svt.Constants.ZVTXCONSTRAINT;		
			ErrRho[0] = org.jlab.rec.cvt.svt.Constants.RHOVTXCONSTRAINT;										
		}
		
		for(int j= shift; j<shift+SVTcrossesInTrk.size(); j++) {
			
			X[j] = SVTcrossesInTrk.get(j-shift).get_Point().x();
			Y[j] = SVTcrossesInTrk.get(j-shift).get_Point().y();
			Z[j] = SVTcrossesInTrk.get(j-shift).get_Point().z();
			Rho[j] = Math.sqrt(SVTcrossesInTrk.get(j-shift).get_Point().x()*SVTcrossesInTrk.get(j-shift).get_Point().x() + 
					SVTcrossesInTrk.get(j-shift).get_Point().y()*SVTcrossesInTrk.get(j-shift).get_Point().y());
			ErrRho[j] = Math.sqrt(SVTcrossesInTrk.get(j-shift).get_PointErr().x()*SVTcrossesInTrk.get(j-shift).get_PointErr().x() + 
					SVTcrossesInTrk.get(j-shift).get_PointErr().y()*SVTcrossesInTrk.get(j-shift).get_PointErr().y());
			ErrRt[j] = Math.sqrt(SVTcrossesInTrk.get(j-shift).get_PointErr().x()*SVTcrossesInTrk.get(j-shift).get_PointErr().x() + 
					SVTcrossesInTrk.get(j-shift).get_PointErr().y()*SVTcrossesInTrk.get(j-shift).get_PointErr().y());
			ErrZ[j] = SVTcrossesInTrk.get(j-shift).get_PointErr().z();		
			
		}
		
		if(ignoreErr==true) 
			for(int j= 0; j<shift+SVTcrossesInTrk.size(); j++) {	
				ErrRt[j] = 1;
				ErrRho[j] = 1;
				ErrZ[j] = 1;										
			}
		int j0 = SVTcrossesInTrk.size();	
		for(int j= shift+j0; j<shift+j0+BMTZdetcrossesInTrk.size(); j++) {						
				X[j] = BMTZdetcrossesInTrk.get(j-shift-j0).get_Point().x();
				Y[j] = BMTZdetcrossesInTrk.get(j-shift-j0).get_Point().y();						
				ErrRt[j] = Math.sqrt(BMTZdetcrossesInTrk.get(j-shift-j0).get_PointErr().x()*BMTZdetcrossesInTrk.get(j-shift-j0).get_PointErr().x() + 
						BMTZdetcrossesInTrk.get(j-shift-j0).get_PointErr().y()*BMTZdetcrossesInTrk.get(j-shift-j0).get_PointErr().y());				
		}
		
		if(ignoreErr==true) 
			for(int j= shift+SVTcrossesInTrk.size(); j<shift+SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size(); j++) {						
				X[j] = 1;
				Y[j] = 1;						
				ErrRt[j] = 1;				
			}

		
		for(int j= shift+j0; j<shift+j0+BMTCdetcrossesInTrk.size(); j++) {
			Z[j] = BMTCdetcrossesInTrk.get(j-shift-j0).get_Point().z();
			Rho[j] = org.jlab.rec.cvt.bmt.Constants.CRCRADIUS[BMTCdetcrossesInTrk.get(j-shift-j0).get_Region()-1]+org.jlab.rec.cvt.bmt.Constants.LYRTHICKN;
			ErrRho[j] = 0.01; // check this error on thickness measurement					
			ErrZ[j] = BMTCdetcrossesInTrk.get(j-shift-j0).get_PointErr().z();		
			
		}
		if(resetSVTMeas) {
			//System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
			for(int j= shift; j<shift+j0; j++) {
				X[j] = SVTcrossesInTrk.get(j-shift).get_Point0().x();
				Y[j] = SVTcrossesInTrk.get(j-shift).get_Point0().y();
				Z[j] = SVTcrossesInTrk.get(j-shift).get_Point0().z();
				ErrRho[j] = Math.sqrt(SVTcrossesInTrk.get(j-shift).get_PointErr().x()*SVTcrossesInTrk.get(j-shift).get_PointErr().x() + 
						SVTcrossesInTrk.get(j-shift).get_PointErr().y()*SVTcrossesInTrk.get(j-shift).get_PointErr().y());
				ErrZ[j] = SVTcrossesInTrk.get(j-shift).get_PointErr0().z();										
			}
		}
	//	if(Constants.DEBUGMODE) {
	//		System.out.println(" FIT ARRAYS ");
	//		for(int i =0; i<X.length; i++)
	//			System.out.println("X["+i+"] = "+X[i]+ "  Y["+i+"] = "+Y[i]);
	//		for(int i =0; i<Z.length; i++)
	//			System.out.println("Rho["+i+"] = "+Rho[i]+ "  Z["+i+"] = "+Z[i]);
	//	}
		HelixMeasurements MeasArray = new HelixMeasurements( X , Y , Z , Rho , ErrZ , ErrRho , ErrRt );
		
		return MeasArray;
	}

	private RayMeasurements get_RayMeasurementsArrays(ArrayList<Cross> arrayList, boolean ignoreErr, boolean resetSVTMeas) {
		
		
		ArrayList<Cross> SVTcrossesInTrk = new ArrayList<Cross>();
		ArrayList<Cross> BMTCdetcrossesInTrk = new ArrayList<Cross>();
		ArrayList<Cross> BMTZdetcrossesInTrk = new ArrayList<Cross>();
		
		//make lists
		for(Cross c : arrayList) {
			
			if(c.get_Detector()=="SVT") 
				SVTcrossesInTrk.add(c);
			if(c.get_Detector()=="BMT") { // Micromegas
				if(c.get_DetectorType()=="C") {//C-detector --> only Z defined
					BMTCdetcrossesInTrk.add(c);
				}
				if(c.get_DetectorType()=="Z") {//Z-detector --> only phi defined
					BMTZdetcrossesInTrk.add(c);
				}
			}
		}
			
			
		double[] X = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()];
		double[] Y = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()];
		double[] Z = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()];
		double[] Y_prime = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()];
		double[] ErrZ = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()];
		double[] ErrY_prime = new double[SVTcrossesInTrk.size()+BMTCdetcrossesInTrk.size()];
		double[] ErrRt = new double[SVTcrossesInTrk.size()+BMTZdetcrossesInTrk.size()];
		
		
		for(int j= 0; j< SVTcrossesInTrk.size(); j++) {
			
			X[j] = SVTcrossesInTrk.get(j).get_Point().x();
			Y[j] = SVTcrossesInTrk.get(j).get_Point().y();
			Z[j] = SVTcrossesInTrk.get(j).get_Point().z();
			Y_prime[j] = SVTcrossesInTrk.get(j).get_Point().y();
			ErrY_prime[j] = SVTcrossesInTrk.get(j).get_PointErr().y();
			ErrRt[j] = Math.sqrt(SVTcrossesInTrk.get(j).get_PointErr().x()*SVTcrossesInTrk.get(j).get_PointErr().x() + 
					SVTcrossesInTrk.get(j).get_PointErr().y()*SVTcrossesInTrk.get(j).get_PointErr().y());
			ErrZ[j] = SVTcrossesInTrk.get(j).get_PointErr().z();		
			
		}
		
		if(ignoreErr==true) 
			for(int j= 0; j<SVTcrossesInTrk.size(); j++) {	
				ErrRt[j] = 1;
				ErrY_prime[j] = 1;
				ErrZ[j] = 1;										
			}

		int j0 = SVTcrossesInTrk.size();
		for(int j= j0; j<j0+BMTZdetcrossesInTrk.size(); j++) {						
				X[j] = BMTZdetcrossesInTrk.get(j-j0).get_Point().x();
				Y[j] = BMTZdetcrossesInTrk.get(j-j0).get_Point().y();						
				ErrRt[j] = Math.sqrt(SVTcrossesInTrk.get(j-j0).get_PointErr().x()*SVTcrossesInTrk.get(j-j0).get_PointErr().x() + 
						SVTcrossesInTrk.get(j-j0).get_PointErr().y()*SVTcrossesInTrk.get(j-j0).get_PointErr().y());			
		}
		
		if(ignoreErr==true) 
			for(int j= j0; j<j0+BMTZdetcrossesInTrk.size(); j++) {						
				X[j] = 1;
				Y[j] = 1;						
				ErrRt[j] = 1;				
			}

		for(int j= j0; j<j0+BMTCdetcrossesInTrk.size(); j++) {
			Z[j] = BMTCdetcrossesInTrk.get(j-j0).get_Point().z();
			Y_prime[j] = BMTCdetcrossesInTrk.get(j-j0).get_Point().y();
			ErrY_prime[j] = 0.; 
			ErrZ[j] = SVTcrossesInTrk.get(j-j0).get_PointErr().z();;		
			
		}
		if(resetSVTMeas) {
			//System.err.println("Error in Helical Track fitting -- helix not found -- trying to refit using the uncorrected crosses...");
			for(int j= 0; j<SVTcrossesInTrk.size(); j++) {
				X[j] = SVTcrossesInTrk.get(j).get_Point0().x();
				Y[j] = SVTcrossesInTrk.get(j).get_Point0().y();
				Z[j] = SVTcrossesInTrk.get(j).get_Point0().z();
				ErrY_prime[j] = SVTcrossesInTrk.get(j).get_Point0().y();
				ErrZ[j] = SVTcrossesInTrk.get(j).get_PointErr0().z();										
			}
		}

		RayMeasurements MeasArray = new RayMeasurements( X , Y , Z , Y_prime , ErrZ , ErrY_prime , ErrRt );
		
		return MeasArray;
	}

	private void resetUnusedCross(Cross cross, org.jlab.rec.cvt.svt.Geometry geo) {
		
		cross.set_Dir(new Vector3D(0,0,0));
		
		cross.set_CrossParamsSVT(null, geo);
	}

	/**
	 * 
	 * @param MMCrosses the BMT crosses
	 * @param thecand the straight track candidate
	 * @param geo the BMT geometry
	 * @return an arraylist of BMT crosses matched to the track
	 */
	public ArrayList<Cross> matchTrackToMM(List<Cross> MMCrosses, StraightTrack thecand, org.jlab.rec.cvt.bmt.Geometry geo) {
		
		double matchCutOff = org.jlab.rec.cvt.svt.Constants.COSMICSMINRESIDUAL; // ?  guess
				
		ArrayList<Cross> BMTCrossList = new ArrayList<Cross>();
		StraightTrack thecand2 = thecand;
		if(thecand2==null)
			return BMTCrossList;
		
		if(MMCrosses==null || MMCrosses.size()==0)
			return BMTCrossList;
		
		
		
		// for now we are only linking to the region 3 BMT since the current configuration in 4 double SVT layers + 1 double BMT layers
		//------------------------------------------------------------------------------------------------------
		ArrayList<Cross> MatchedMMCrossZDet = this.MatchMMCrossZ(3,thecand2.get_ray(), MMCrosses, Math.toRadians(2)); // 2 degrees opening angle
		if(MatchedMMCrossZDet.size()>0) 
			BMTCrossList.addAll(MatchedMMCrossZDet);
		
		ArrayList<Cross> MatchedMMCrossCDet = this.MatchMMCrossC(3, thecand2.get_ray(), MMCrosses, matchCutOff, geo);
		if(MatchedMMCrossCDet.size()>0) 
			BMTCrossList.addAll(MatchedMMCrossCDet);
		
		
		for(Cross Ccross : MatchedMMCrossCDet)
			for(Cross Zcross : MatchedMMCrossZDet)
				if( (Ccross.get_Cluster2().get_Layer()-Zcross.get_Cluster1().get_Layer())==1 && (Ccross.get_Cluster2().get_Sector()==Zcross.get_Cluster1().get_Sector())) {
					Ccross.set_MatchedZCross(Zcross);
					Zcross.set_MatchedCCross(Ccross);
				}
		
		return BMTCrossList;
	}

	private ArrayList<Cross> MatchMMCrossC(int Region, Ray ray, List<Cross> MMCrosses, double matchCutOff, org.jlab.rec.cvt.bmt.Geometry geo) {

		ArrayList<Cross> matchedMMCrosses = new ArrayList<Cross>();
		
		double R = org.jlab.rec.cvt.bmt.Constants.CRCRADIUS[Region-1];		     // R for C detector
		
		double sx = ray.get_yxslope();
		double ix = ray.get_yxinterc();
		double sz = ray.get_yzslope();
		double iz = ray.get_yzinterc();
		
		double D = sx*sx*ix*ix - (1+sx*sx)*(ix*ix-R*R);                
		
		double[] y = null ;
		// calculate the y position of the trajectory ray at the radius of the C detector
		if(D ==0) {
			y = new double[1];
			y[0] = -sx*ix/(1+sx*sx);
		}
		
		if(D>0) {
			y = new double[2];
			y[0] = (-sx*ix+Math.sqrt(D))/(1+sx*sx);
			y[1] = (-sx*ix-Math.sqrt(D))/(1+sx*sx);
		}
		for(int j =0; j< y.length; j++) {
			double x = sx*y[j] + ix;
			double phi = Math.atan2(y[j], x);
			// calculate the z position of the trajectory at the radius of the C detector
			double z = sz*y[j] + iz;
			
			double doca = 1000;
			Cross closestCross = null;
			// match the cross z measurement closest to the calculated z
			for(int i =0; i < MMCrosses.size(); i++) {
				if(Double.isNaN(MMCrosses.get(i).get_Point0().z()))
					continue;
				double m_z = MMCrosses.get(i).get_Point().z();
				int sector = geo.isInSector(MMCrosses.get(i).get_Region()*2, phi);
				
				if(sector!=MMCrosses.get(i).get_Sector())
					continue;
				double d = Math.abs(z-m_z); 
				if(d<doca) { 
					doca = d;
					closestCross=MMCrosses.get(i);
					
					closestCross.set_Point(new Point3D(x,y[j],m_z));
					closestCross.set_PointErr(new Point3D(10, 10, MMCrosses.get(i).get_PointErr().z())); // to do : put in the correct error
				} 
			}
		
			if(closestCross!=null)	
				if(doca<matchCutOff*100) {
					matchedMMCrosses.add(closestCross);			
				}
			
		}
		
		return matchedMMCrosses;
	}

	private ArrayList<Cross> MatchMMCrossZ(int Region, Ray ray, List<Cross> MMCrosses, double matchCutOff) {

		ArrayList<Cross> matchedMMCrosses = new ArrayList<Cross>();
		
		double R =	org.jlab.rec.cvt.bmt.Constants.CRZRADIUS[Region-1];		     // R for Z detector
		double sx = ray.get_yxslope();
		double ix = ray.get_yxinterc();
		double sz = ray.get_yzslope();
		double iz = ray.get_yzinterc();
		
		double D = sx*sx*ix*ix - (1+sx*sx)*(ix*ix-R*R);                
		
		double[] y = null ;
		
		if(D ==0) {
			y = new double[1];
			y[0] = -sx*ix/(1+sx*sx);
		}
		
		if(D>0) {
			y = new double[2];
			y[0] = (-sx*ix+Math.sqrt(D))/(1+sx*sx);
			y[1] = (-sx*ix-Math.sqrt(D))/(1+sx*sx);
		}
		for(int j =0; j< y.length; j++) {
			double x = sx*y[j] + ix;
			double z = sz*y[j] + iz;
			
			double minCosAngMeasToTrk = -1;
			Cross closestCross = null;
			
			for(int i =0; i < MMCrosses.size(); i++) {
				
				// measuring phi
				if(Double.isNaN(MMCrosses.get(i).get_Point0().x()))
					continue;
				double m_x = MMCrosses.get(i).get_Point().x();
				double m_y = MMCrosses.get(i).get_Point().y();
				
				double cosAngMeasToTrk = (x*m_x+y[j]*m_y)/ Math.sqrt((x*x+y[j]*y[j])*(m_x*m_x+m_y*m_y)); // the cosine between the measured (x,y) cross coords and the (x,y) trajectory should be close to 1
				// opening angle must be within about 11 degrees to start the search for nearest point
				if(cosAngMeasToTrk<0.98)
					continue;
				
				if(Math.acos(cosAngMeasToTrk)<Math.acos(minCosAngMeasToTrk)) { 
					minCosAngMeasToTrk = cosAngMeasToTrk;
					closestCross=MMCrosses.get(i);
					closestCross.set_Point(new Point3D(m_x,m_y,z));
					closestCross.set_PointErr(new Point3D(MMCrosses.get(i).get_PointErr().x(), MMCrosses.get(i).get_PointErr().y(), 10.)); // to do : put in the correct error
				} 
			}
			if(closestCross!=null)	{
				if(minCosAngMeasToTrk<matchCutOff) 
					matchedMMCrosses.add(closestCross);
					
			}
		}
				
		return matchedMMCrosses;
	}
	

	private ArrayList<Track> rmHelicalTrkClones(boolean removeClones, ArrayList<Track> cands) {
			ArrayList<Track> passedcands = new ArrayList<Track>();
		if(removeClones ==false)
			passedcands = cands;
			
		if(removeClones ==true)
			if(cands.size()>0) {
				// clean up duplicates:
				for(int k = 0; k< cands.size(); k++) {
					for(int k2 = 0; k2< cands.size(); k2++) {
						if(k2==k)
							continue;
						int overlaps =0;
						for(int k3 =0; k3<cands.get(k).size(); k3++) {
							if(cands.get(k2).containsCross(cands.get(k).get(k3))) {
								overlaps++;
								
							}
						}
						if(overlaps>1) {
							if( (cands.get(k2).get_circleFitChi2PerNDF()+cands.get(k2).get_lineFitChi2PerNDF()) > (cands.get(k).get_circleFitChi2PerNDF()+cands.get(k).get_lineFitChi2PerNDF())) {
								
								cands.get(k2).set_Id(-999);
								
							}
						}
					}
				}
				for(int k = 0; k< cands.size(); k++) {
					if(cands.get(k).get_Id()!=-999) {
						passedcands.add(cands.get(k));
						
					}
				}
			}
	
		return passedcands;
	}
	private ArrayList<StraightTrack> rmStraightTrkClones(boolean removeClones, ArrayList<StraightTrack> cands) {
		ArrayList<StraightTrack> passedcands = new ArrayList<StraightTrack>();
	if(removeClones ==false)
		passedcands = cands;
		
	if(removeClones ==true)
		if(cands.size()>0) {
			// clean up duplicates:
			for(int k = 0; k< cands.size(); k++) {
				for(int k2 = 0; k2< cands.size(); k2++) {
					if(k2==k)
						continue;
					int overlaps =0;
					for(int k3 =0; k3<cands.get(k).size(); k3++) {
						if(cands.get(k2).containsCross(cands.get(k).get(k3))) {
							overlaps++;
							
						}
					}
					if(overlaps>1) {
						if( (cands.get(k2).get_chi2()) > (cands.get(k).get_chi2())) {
							
							cands.get(k2).set_Id(-999);
							
						}
					}
				}
			}
			for(int k = 0; k< cands.size(); k++) {
				if(cands.get(k).get_Id()!=-999) {
					passedcands.add(cands.get(k));
					
				}
			}
		}

		return passedcands;
	}
		
	private void EliminateStraightTrackOutliers(ArrayList<Cross> crossesToFit,
			StraightTrackFitter fitTrk,org.jlab.rec.cvt.svt.Geometry svt_geo) {
			for(int j =0; j< crossesToFit.size(); j++)
				if( Math.abs(fitTrk.get_ray().get_yxslope()*crossesToFit.get(j).get_Point().y()+fitTrk.get_ray().get_yxinterc()-crossesToFit.get(j).get_Point().x() )>org.jlab.rec.cvt.svt.Constants.COSMICSMINRESIDUAL ||
						Math.abs(fitTrk.get_ray().get_yzslope()*crossesToFit.get(j).get_Point().y()+fitTrk.get_ray().get_yzinterc()-crossesToFit.get(j).get_Point().z() )>org.jlab.rec.cvt.svt.Constants.COSMICSMINRESIDUALZ ) {
					resetUnusedCross(crossesToFit.get(j), svt_geo);
					crossesToFit.remove(j);
				}
		}


	public void matchClusters(List<Cluster> sVTclusters, TrajectoryFinder tf, org.jlab.rec.cvt.svt.Geometry svt_geo, org.jlab.rec.cvt.bmt.Geometry bmt_geo, boolean trajFinal,
			ArrayList<StateVec> trajectory, int k) {
		
		Collections.sort(sVTclusters);
		for(StateVec st : trajectory) {
			for(Cluster cls : sVTclusters) {
				if(cls.get_AssociatedTrackID()!=-1)
					continue;
				if(st.get_SurfaceSector() != cls.get_Sector())
					continue;
				if(st.get_SurfaceLayer() != cls.get_Layer())
					continue;
				if(Math.abs(st.get_CalcCentroidStrip()-cls.get_Centroid())<4) {
					tf.setHitResolParams("SVT", cls.get_Sector(), cls.get_Layer(), cls,
							st, svt_geo, bmt_geo,  trajFinal);
					//System.out.println("trying to associate a cluster ");cls.printInfo(); System.out.println(" to "+st.get_CalcCentroidStrip()+" dStp = "+(st.get_CalcCentroidStrip()-cls.get_Centroid()));
					cls.set_AssociatedTrackID(k);
					for(FittedHit h : cls)
						h.set_AssociatedTrackID(k);
				}
			}
		}
	}

}
