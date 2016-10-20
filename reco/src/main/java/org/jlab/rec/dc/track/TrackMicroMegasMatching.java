package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.StateVec;


/**
 * A class to match a dc track to FMT hits
 * @author ziegler
 *
 */
public class TrackMicroMegasMatching {

	int[] trkSectors = null; // [TrkIdx]
	
	double[][][] crsXAtMMPlanes; // [TrkIdx][PlaneIdx][CoordIdx]
	
	int trkIdx = 0;
	public void getMicroMegasPoints(DataEvent event) {

		
		if(event.hasBank("FMTRec::Crosses")==false) {
			//System.err.println("there is no FMT bank ");
			
			return;
		}
        

		EvioDataBank mbank = (EvioDataBank) event.getBank("FMTRec::Crosses");

		double[] m_x = mbank.getDouble("x");
	    double[] m_y = mbank.getDouble("y");
	    double[] m_z = mbank.getDouble("z");
	    int[] trkId = mbank.getInt("trackID");
	    int mbankSize = m_x.length;
	    
	    if(mbankSize==0)
	    	return;
	    
	    
	    for(int i = 0; i< mbankSize; i++) {
	    	if(trkId[i]>trkIdx)
	    		trkIdx = trkId[i];
	    }
	    if(trkIdx<0)
	    	return;
	    
	    crsXAtMMPlanes = new double[trkIdx+1][3][3] ;// [TrkIdx][PlaneIdx][CoordIdx]
	    trkSectors = new int[trkIdx+1];
	   
	   
	    for(int i = 0; i< mbankSize; i++) {
	    	if(trkId[i]==-1)
	    		continue;
	    	for(int plIdx = 0; plIdx<3; plIdx++) {
		    	if(Math.abs(m_z[i]-1)<this.MMPlanesZ()[plIdx]) {
		    		
			    	crsXAtMMPlanes[trkId[i]][plIdx][0] = m_x[i];
			    	crsXAtMMPlanes[trkId[i]][plIdx][1] = m_y[i];
			    	crsXAtMMPlanes[trkId[i]][plIdx][2] = m_z[i];
		    	}
	    	}
	    	trkSectors[trkId[i]] = this.getSector(crsXAtMMPlanes[trkId[i]][2][0], crsXAtMMPlanes[trkId[i]][2][1], crsXAtMMPlanes[trkId[i]][2][2]); 
	    }    
	}
	
	private double[][] RotateToTiltedSectorFrame(double[][] PointAtMicroMegasMidPlanes) {
		
		double[][] PointArray = new double[3][3];
		
		if(PointAtMicroMegasMidPlanes==null)
			return PointArray;
		
		//rotate to tilted CS frame
    	double[] trkAtMMPlane1 = this.rotateToTiltedSectorCoordSys(PointAtMicroMegasMidPlanes[0][0], PointAtMicroMegasMidPlanes[0][1], PointAtMicroMegasMidPlanes[0][2]);
    	double[] trkAtMMPlane2 = this.rotateToTiltedSectorCoordSys(PointAtMicroMegasMidPlanes[1][0], PointAtMicroMegasMidPlanes[1][1], PointAtMicroMegasMidPlanes[1][2]);
    	double[] trkAtMMPlane3 = this.rotateToTiltedSectorCoordSys(PointAtMicroMegasMidPlanes[2][0], PointAtMicroMegasMidPlanes[2][1], PointAtMicroMegasMidPlanes[2][2]);
    	
    	
    	for(int i = 0; i<3; i++) {
    		PointArray[0][i] = trkAtMMPlane1[i];   	
    		PointArray[1][i] = trkAtMMPlane2[i];    	
    		PointArray[2][i] = trkAtMMPlane3[i];  	
    	}
    	
		return PointArray;
    	
	}
	/**
	 * 
	 * @param x0 in lab frame
	 * @param y0 in lab frame
	 * @param z0 in lab frame
	 * @param p0x in lab frame
	 * @param p0y in lab frame 
	 * @param p0z in lab frame
	 * @param q
	 * @return swam track parameters in lab CS
	 */
	private double[][] SwimToMicroMegasMidPlanes(double x0, double y0, double z0, double p0x, double p0y, double p0z, int q) {
		
		double[][] PointArray = new double[3][3];
		
    	//swim in lab frame to MM planes
		DCSwimmer swim = new DCSwimmer();
		
		
    	double[] MMZ = this.MMPlanesZ();
    	
    	swim.SetSwimParameters(x0, y0, z0, p0x, p0y, p0z, q);
    	
    	double[] VecAtMMPl1 = swim.SwimToPlaneLab(MMZ[0]);
    	
    	if(VecAtMMPl1!=null)
    		swim.SetSwimParameters(VecAtMMPl1[0],VecAtMMPl1[1],VecAtMMPl1[2],VecAtMMPl1[3],VecAtMMPl1[4],VecAtMMPl1[5],q);
    	
    	double[] VecAtMMPl2 = swim.SwimToPlaneLab(MMZ[1]);
    	
    	if(VecAtMMPl2 !=null)
    		swim.SetSwimParameters(VecAtMMPl2[0],VecAtMMPl2[1],VecAtMMPl2[2],VecAtMMPl2[3],VecAtMMPl2[4],VecAtMMPl2[5],q);
    	
    	double[] VecAtMMPl3 = swim.SwimToPlaneLab(MMZ[2]);
		
    	
    	if(VecAtMMPl1 == null || VecAtMMPl2 ==null || VecAtMMPl3 ==null)
    		return PointArray;
    	
    	for(int i =0; i<3; i++) {
	    	PointArray[0][i] = VecAtMMPl1[i];
	    	PointArray[1][i] = VecAtMMPl2[i];
	    	PointArray[2][i] = VecAtMMPl3[i];
    	}
    	return PointArray;
	}
	
 	private double[] MMPlanesZ(){
		double[] Z = new double[3];
		Z[0] = 29.5;
		Z[1] = 31.5;
		Z[2] = 33.5;
		
		return Z;
	}
	// borrowed from Dave's ced Geometry Manager code
	private int getSector(double x, double y, double z) {
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
	private double[] rotateToTiltedSectorCoordSys(double x, double y, double z) {
		double[] XinSec = new double[3];
		double[] XinTiltSec = new double[3];
		
		int sector = this.getSector(x, y, z);
		
		if ((sector < 1) || (sector > 6)) {
			return new double[3];
		}
		if (sector == 1) {
			XinSec[0] = x;
			XinSec[1] = y;
		}
		else {

			double midPlanePhi = Math.toRadians(60*(sector-1));
			double cosPhi = Math.cos(midPlanePhi);
			double sinPhi = Math.sin(midPlanePhi);
			XinSec[0] = cosPhi*x + sinPhi*y;
			XinSec[1] = -sinPhi*x + cosPhi*y;
		}
				
		//z coordinates are the same
		XinSec[2] = z;
		
		// rotate in tilted sector
		XinTiltSec[2] = XinSec[0]*Math.sin(Math.toRadians(25.))+XinSec[2]*Math.cos(Math.toRadians(25.));
        XinTiltSec[0] = XinSec[0]*Math.cos(Math.toRadians(25.))-XinSec[2]*Math.sin(Math.toRadians(25.));
        XinTiltSec[1] = XinSec[1];
        
        return XinTiltSec;
	}
	
	public void matchTrackToMM(Track thecand) {
		
		if(trkSectors==null)
			return;
		// swim track from Origin to MM Planes to match
		
		double matchCutOff = 2.0; // ? 2cm guess
		
		double x0 = thecand.get_Vtx0().x();
		double y0 = thecand.get_Vtx0().y();
		double z0 = thecand.get_Vtx0().z();
		double p0x = thecand.get_pAtOrig().x();
		double p0y = thecand.get_pAtOrig().y();
		double p0z = thecand.get_pAtOrig().z();
		int q = thecand.get_Q();
		
		double[][] PointArray = this.SwimToMicroMegasMidPlanes(x0, y0, z0, p0x, p0y, p0z, q);
		
		
		List<Point3D> matchedMicroMegasPoints = new ArrayList<Point3D>();
		
		boolean matched = true;
		
		for(int i =0; i<trkIdx+1; i++) {
			
			//if(this.getSector(PointArray[2][0], PointArray[2][1], PointArray[2][2])!=trkSectors[0])
			//	continue;
			
			for(int ic = 0 ; ic<3; ic++) {
	    		for(int jc =0; jc<3; jc++) { 
	    			
	    			if(Math.abs(crsXAtMMPlanes[i][ic][jc]-PointArray[ic][jc])>matchCutOff) // Do :  set pass criteria and save the matched MM points -- add a method to cand to set/get each MM point valu
	    				matched = false;
	    		}
			}
			
			if(matched == true) {
				double[][] RotatedPoints = RotateToTiltedSectorFrame(crsXAtMMPlanes[i]); // rotate in the analysis frame to save the points
				for(int j = 0; j<3; j++)  {
					matchedMicroMegasPoints.add(new Point3D(RotatedPoints[j][0], RotatedPoints[j][1], RotatedPoints[j][2]));
				}
			}
			
		}
		if(matched==false)
			return;
		
		thecand.set_MicroMegasPointsList(matchedMicroMegasPoints);
		
	}
	
	public void reFitTrackWithMicroMegas(Track cand, TrackCandListFinder candFind,int totNbOfIterations) {
		
		if(cand.get_MicroMegasPointsList()==null || cand.get_MicroMegasPointsList().size()<3)
			return;
		
		int iterationNb =0;
		double fitChisq = Double.POSITIVE_INFINITY;
		StateVec VecAtReg3MiddlePlane = new StateVec();
		
		while(iterationNb < totNbOfIterations) {
			
			KalFit kf = new KalFit(cand, "wires");
			if(kf.KalFitFail==true) {
				break;
			}
			kf.runKalFit(); 
			if(kf.chi2>fitChisq) {
				iterationNb = totNbOfIterations;
				continue;
			}
			if(!Double.isNaN(kf.KF_p) && kf.KF_p>Constants.MINTRKMOM) {
				cand.set_P(kf.KF_p);								
				cand.set_Q(kf.KF_q);
				cand.set_CovMat(kf.covMat);
				
				VecAtReg3MiddlePlane = new StateVec(kf.stateVec[0],kf.stateVec[1],kf.stateVec[2],kf.stateVec[3]);
				
				
			}
			fitChisq = kf.chi2;
			iterationNb++;
			
			cand.set_FitChi2(fitChisq);
		}
			
		
		candFind.setTrackPars(cand, null, null, VecAtReg3MiddlePlane, cand.get(2).get_Point().z());
	}
	
	
}
