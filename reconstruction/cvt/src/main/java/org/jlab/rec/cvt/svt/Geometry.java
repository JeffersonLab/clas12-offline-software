package org.jlab.rec.cvt.svt;

//import java.io.File;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import Jama.Matrix;

import org.jlab.detector.geant4.v2.SVT.SVTConstants;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Triangle3d;
import org.jlab.rec.cvt.trajectory.Helix;

public class Geometry {
		
	double[][] Rx=new double[6][18];
	double[][] Ry=new double[6][18];
	double[][] Rz=new double[6][18];
	double[][] Cx=new double[6][18];
	double[][] Cy=new double[6][18];
	double[][] Cz=new double[6][18];
	
    public Geometry() {
    	for (int lay=0; lay<6;lay++) {
    		for (int sec=0; sec<18;sec++) {
    			this.setRx(lay+1,sec+1,0);this.setRy(lay+1,sec+1,0);this.setRz(lay+1,sec+1,0);
    			this.setCx(lay+1,sec+1,0);this.setCy(lay+1,sec+1,0);this.setCz(lay+1,sec+1,0);
    		}
     	}
    }

    // Comments on the Geometry of the BST 
    //------------------------------------
    // The BST geometry consists of 3 (or 4) superlayers of modules. 
    // Each superlayer contains two layers of modules, labeled A and B. 
    // Layer B corresponds to the top layer as seen from the outside of the detector, 
    // and Layer A to the layer underneath Layer B looking from the outside.  
    // Each module contains 3 sensors (hybrid, intermediate, far).
    // The hybrid, intermediate and far sensors are aligned in the direction of the beam 
    // corresponding to the positive z-axis in the laboratory frame.  
    // The coordinate system in the lab frame (center of the target) is a right handed system, with 
    // the z unit vector in the direction of the beam, and the y unit vector pointing up; 
    // the x unit vector points therefore to the left when looking in the direction of the beam.
    // The numbering convention for the sectors is as follows:
    // sector 1 modules oriented at 90 deg (80 deg) with respect to the y-axis for superlayers 1,2,4 (3); 
    // sector numbers increase in the clockwise direction (viewed in the direction of the beam).  
    // The strips in the hybrid sensor of Layer B are connected to the pitch adapter and 
    // and implanted with 156 micron pitch.  There are 256 strips oriented at graded angle 
    // from 0 to +3 deg with respect to the bottom edge of layer B which corresponds to the z-direction. 
    // Strip number 1 in Layer B is parallel to the bottom of the sensor.  
    //for making bst outline fig
    public Point3D getPlaneModuleOrigin(int sector, int layer) {
        //shift the local origin to the physical orign instead of active area
        Point3D point0 = new Point3D(transformToFrame(sector, layer, -1, 0, 0, "lab", ""));
        return point0;
    }

    public Point3D getPlaneModuleEnd(int sector, int layer) {
        //shift the local origin to the physical orign instead of active area
        Point3D point0 = new Point3D(transformToFrame(sector, layer, org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID + 1, 0, 0, "lab", ""));
        return point0;
    }

    //*** 
    public int findSectorFromAngle(int layer, Point3D i) {
//        int Sect = -1;
//        for (int s = 0; s < Constants.NSECT[(layer-1)/2]; s++) {
//            int sector = s + 1;
//            Vector3D orig = new Vector3D(getPlaneModuleOrigin(sector, layer).x(), getPlaneModuleOrigin(sector, layer).y(), 0);
//            Vector3D end = new Vector3D(getPlaneModuleEnd(sector, layer).x(), getPlaneModuleEnd(sector, layer).y(), 0);
//            Vector3D trk = new Vector3D(trkPoint.x(), trkPoint.y(), 0);
//            orig.unit();
//            end.unit();
//            trk.unit();
//
//            double phi1 = orig.dot(trk);
//            double phi2 = trk.dot(end);
//            double phiRange = orig.dot(end);
//
//            if (Math.acos(phi1) < Math.acos(phiRange) && Math.acos(phi2) < Math.acos(phiRange)) {
//                Sect = sector;
//            }
//        }
    	double step=2*Math.PI/((double)org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2]);
    	double ang=Math.atan2(i.y(), i.x());
    	ang=-(ang+Math.PI/2.-step/2.);
    	if (ang<0) ang+=2*Math.PI;
    	int Sect= (int)(ang/step) +1;
    	if (Sect>org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2]) Sect=1;
    	if (Sect<1) Sect=org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2];
        return Sect;
    }

    //***
    public Vector3D findBSTPlaneNormal(int sector, int layer) {

        double angle = 2. * Math.PI * ((double) -(sector - 1) / (double) org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2]) + org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;

        return new Vector3D(Math.cos(angle), Math.sin(angle), 0);
    }
    
    public double findBSTPlaneAngle(int sector, int layer) {

        //double angle = 2.*Math.PI*((double)(sector-1)/(double)Constants.NSECT[(layer-1)/2]) + Math.PI/2.;
        double angle = 2. * Math.PI * ((double) -(sector - 1) / (double) org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2]) + org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;

        return angle;
    }
    //***

    public double[] getLocCoord(double s1, double s2) { //2 top, 1 bottom

        double[] X = new double[2];
        double ialpha1 = (s1 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;
        double ialpha2 = (s2 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID - interc1;
        double b2 = interc2;

        double z = (b2 - b1) / (m1 - m2);
        double x = m1 * z + b1;
        X[0] = x;
        X[1] = z;

        return X;

    }

    public double[] getLocCoordErrs(int lay1, int lay2, double s1, double s2, double z) {
        double[] Xerr = new double[2];

        double sigma1 = getSingleStripResolution(lay1, (int) s1, z);
        double sigma2 = getSingleStripResolution(lay2, (int) s2, z);

        Xerr[0] = Math.sqrt(sigma1 * sigma1 + sigma2 * sigma2);

        Xerr[1] = (getLocCoord(s1 - 0.5, s2 - 0.5)[1]
                - getLocCoord(s1 + 0.5, s2 + 0.5)[1]);

        //Xerr[0] = (getLocCoord( s1-0.5,  s2-0.5)[0]+
        //		-getLocCoord( s1+0.5,  s2+0.5)[0]);
        if (s1 <= 1) {
            Xerr[1] = (getLocCoord(s1, s2 - 0.5)[1]
                    - getLocCoord(s1 + 1.5, s2 + 0.5)[1]);
        }
        if (s2 <= 1) {
            Xerr[1] = (getLocCoord(s1 - 0.5, s2)[1]
                    - getLocCoord(s1 + 1.5, s2 + 2.5)[1]);
        }

        return Xerr;

    }
    //***

   /* public Point3D transformToFrame(int sector, int layer, double x, double y, double z, String frame, String MiddlePlane) {
    	
        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) Constants.NSECT[(layer-1)/2]) * 2. * Math.PI + Constants.PHI0[(layer-1)/2];
        
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - Constants.LOCZAXISROTATION;

        double gap = 0;
        if (MiddlePlane.equals("middle")) {
            if (((layer-1)/2) % 2 == 0) { // for a cross take the bottom layer
                gap = Constants.MODULERADIUS[layer][sector - 1] - Constants.MODULERADIUS[(layer-1)/2][sector - 1];
            }
        }
       
        double lTx = (Constants.MODULERADIUS[(layer-1)/2][sector - 1] + 0.5 * gap) * Math.cos(Glob_rangl);
        double lTy = (Constants.MODULERADIUS[(layer-1)/2][sector - 1] + 0.5 * gap) * Math.sin(Glob_rangl);
        double lTz = Constants.Z0[(layer-1)/2];

        //rotate and translate
        double cosRotation = Math.cos(Loc_to_Glob_rangl);
        double sinRotation = Math.sin(Loc_to_Glob_rangl);

        double xt = 0;
        double yt = 0;
        double zt = 0;

        if (frame.equals("lab")) {
            xt = (x - 0.5 * Constants.ACTIVESENWIDTH) * cosRotation - y * sinRotation + lTx;
            yt = (x - 0.5 * Constants.ACTIVESENWIDTH) * sinRotation + y * cosRotation + lTy;
            zt = z + lTz;
        }
        if (frame.equals("local")) {
            xt = (x - lTx) * cosRotation + (y - lTy) * sinRotation + 0.5 * Constants.ACTIVESENWIDTH;
            yt = -(x - lTx) * sinRotation + (y - lTy) * cosRotation;
            zt = z - lTz;
        }
        return new Point3D(xt, yt, zt);
    }*/
      
public Point3D transformToFrame(int sector, int layer, double x, double y, double z, String frame, String MiddlePlane) {
	
		double RADIUS=org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer-1)/2][(layer-1)%2];
		if (MiddlePlane.equals("middle")) RADIUS=(org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer-1)/2][0]+org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer-1)/2][1])/2.;
    	
        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(layer-1)/2]) * 2. * Math.PI + org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;
        
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;

        double lTx = RADIUS  * Math.cos(Glob_rangl);
        double lTy = RADIUS  * Math.sin(Glob_rangl);
        double lTz = org.jlab.detector.geant4.v2.SVT.SVTConstants.Z0ACTIVE[(layer-1)/2];//-org.jlab.detector.geant4.v2.SVT.SVTConstants.DEADZNLEN;

        //rotate and translate
        double cosRotation = Math.cos(Loc_to_Glob_rangl);
        double sinRotation = Math.sin(Loc_to_Glob_rangl);

        double xt = 0;
        double yt = 0;
        double zt = 0;

        if (frame.equals("lab")) {
            xt = (x - 0.5 * org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID-this.getLocTx(layer, sector))  * cosRotation - y * sinRotation + lTx;
            yt = (x - 0.5 * org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID-this.getLocTx(layer, sector)) * sinRotation + y * cosRotation + lTy;
            zt = z + lTz;
        }
        if (frame.equals("local")) {
            xt = (x - lTx) * cosRotation + (y - lTy) * sinRotation + 0.5 * org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID+this.getLocTx(layer,sector);
            yt = -(x - lTx) * sinRotation + (y - lTy) * cosRotation;
            zt = z - lTz;
        }
      
        return new Point3D(xt, yt, zt);
    }
    //*** point and its error

   public double[] getCrossPars(int sector, int upperlayer, double s1, double s2, String frame, Vector3D trkDir) {
        double[] vals = new double[6];

        // if first iteration trkDir == null
        double s2corr = s2;
        // now use track info
        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, org.jlab.detector.geant4.v2.SVT.SVTConstants.MODULELEN);
        double z = getLocCoord(s1, s2corr)[1];
        //update using the corrected z
        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, z);

        double zf = getLocCoord(s1, s2corr)[1];

        if (upperlayer % 2 != 0) // should not happen as is upper layer...but just in case
        {
            s2corr = s2;
        }

        double[] LC = getLocCoord(s1, s2corr);
        double LC_x = LC[0];
        double LC_z = LC[1];

        Point3D crPoint = transformToFrame(sector, upperlayer-1, LC_x, 0, LC_z, "lab", "middle");
        // test shifts
        //this.applyShift(crPoint, upperlayer / 2, sector);
        vals[0] = crPoint.x();
        vals[1] = crPoint.y();
        vals[2] = crPoint.z();

        double[] LCErr = getLocCoordErrs(upperlayer-1, upperlayer, s1, s2corr, zf);
        double LCErr_x = LCErr[0];
        double LCErr_z = LCErr[1];

        // global rotation angle to get the error in the lab frame
        int layerIdx = upperlayer-1;
        /*
			double Glob_rangl = ((double) (sector-1)/(double) Constants.NSECT[layerIdx])*2.*Math.PI;
	        // angle to rotate to global frame
	        double Loc_to_Glob_rangl = Glob_rangl-Constants.PHI0[layerIdx];
        */ 
        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[layerIdx/2]) * 2. * Math.PI + org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - org.jlab.detector.geant4.v2.SVT.SVTConstants.PHI0;

        double cosRotation = Math.cos(Loc_to_Glob_rangl);
        double sinRotation = Math.sin(Loc_to_Glob_rangl);

        double yerr = Math.abs(cosRotation * LCErr_x);
        double xerr = Math.abs(sinRotation * LCErr_x);

        vals[3] = xerr;
        vals[4] = yerr;
        vals[5] = LCErr_z;

        if (LC_z > org.jlab.detector.geant4.v2.SVT.SVTConstants.MODULELEN + org.jlab.rec.cvt.Constants.interTol) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        // once there is a trk, the cross should be well calculated
        //if the local cross is not in the fiducial volume it is not physical
        if ((trkDir != null && (LC_x < 0 || LC_x > org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID + org.jlab.rec.cvt.Constants.ToModuleEdge))
                || (trkDir != null && (LC_z < -org.jlab.rec.cvt.Constants.interTol || LC_z > org.jlab.detector.geant4.v2.SVT.SVTConstants.MODULELEN + org.jlab.rec.cvt.Constants.interTol))) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }

        //if(vals[5]<Constants.Z0[upper(layer-1)/2]-Constants.interTol || vals[5]>Constants.Z0[upper(layer-1)/2]+Constants.MODULELENGTH+Constants.interTol) {	
        //	return new double[] {Double.NaN,0,Double.NaN,Double.NaN, Double.NaN, Double.NaN};
        //}
        double[] values = new double[6];
        if (frame.equals("lab")) {
            values = vals;
        }
        if (frame.equals("local")) {
            values = new double[]{LC_x, 0, LC_z, LCErr_x, 0, LCErr_z};
        }

        return values;

    }

    private double getCorrectedStrip(int sector, int upperlayer, double s2, Vector3D trkDir, double ZalongModule) {
        double s2corr = s2;
        // second iteration: there is a track direction
        if (trkDir != null) {
            double stripCorr = getStripIndexShift(sector, upperlayer, trkDir, s2, ZalongModule);
            if (s2 > 1) {
                s2corr = s2 + stripCorr;
            }
            if (s2 == 1) {
                if (stripCorr >= 0) {
                    s2corr = s2 + stripCorr;
                }
                if (stripCorr < 0) {
                    s2corr = s2;
                }
            }
        }
        return s2corr;
    }

    public double calcNearestStrip(double X, double Y, double Z, int layer, int sect) {
    	
        Point3D LocPoint = this.transformToFrame(sect, layer, X, Y, Z, "local", "");

        double x = LocPoint.x();
        double z = LocPoint.z();

        double alpha = org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);

        double b = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID;
        double P = org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH;

        double s = -1;

        if (layer % 2 == 1) {//layers 1,3,5 == bottom ==i ==>(1) : regular configuration
            //m1,b1
            s = (int) Math.floor((-x + b + alpha * z + 0.5 * P - org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }
            s = newStrip;

            // charge sharing digitization routine in GEMC
            /*if(sdelta>(P+z*Math.tan(alpha))/4.)
					s= newStrip-0.5;
				if(sdelta<-(P+z*Math.tan(alpha))/4.)
					s= newStrip+0.5;
				//s=(-x+b+alpha*z)/(alpha*z+P); */
            //System.out.println(" nearest strip "+s+" at ("+X+", "+Y+", "+Z+"); delta = "+delta);
        }
        if (layer % 2 == 0) {
            //layers 2,4,6 == top ==j ==>(2) : regular configuration
            //m2,b2		
            s = (int) Math.floor((x + alpha * z + 0.5 * P - org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }
            s = newStrip;
            // charge sharing digitization routine in GEMC
            /*if(sdelta>(P+z*Math.tan(alpha))/4.)
					s= newStrip+0.5;
				if(sdelta<-(P+z*Math.tan(alpha))/4.)
					s= newStrip-0.5;
				//s=(x+alpha*z)/(alpha*z+P); */
            //System.out.println(" nearest strip "+s+" at ("+X+", "+Y+", "+Z+"); delta = "+delta);
        }
        if (s <= 1) {
            s = 1;
        }
        if (s >= 256) {
            s = 256;
        }

        //System.out.println(" layer "+layer+" sector "+sect+" strip "+s);
        return s;
    }
    //****

    public double getSingleStripResolution(int lay, int strip, double Z) { // as a function of local z
        double Strip = (double) strip;
        double StripUp = Strip + 1;
        if (strip == org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS) {
            StripUp = (double) org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS-1; //edge strip so let's consider that there is a virtual next strip with same pitch
        }
        double StripDown = Strip - 1;
        if (strip == 1) {
            StripDown = 2; //edge strip so let's consider that there is a virtual next strip with same pitch
        }

        double pitchToNextStrp = Math.abs(getXAtZ(lay, (double) StripUp, Z) - getXAtZ(lay, (double) Strip, Z)); // this is P- in the formula below
        double pitchToPrevStrp = Math.abs(getXAtZ(lay, (double) StripDown, Z) - getXAtZ(lay, (double) Strip, Z)); // this is P+ in the formula below 

        // For a given strip (for which we estimate the resolution), P+ is the pitch to the strip above (at z position) and P- to that below in the local coordinate system of the module.
        // The current design of the BST is simulated in gemc such that each strip provides hit-no-hit information, and the single strip resolution is 
        // therefore given by the variance,
        // sigma^2 = (2/[P+ + P-]) {integral_{- P-/2}^{P+/2) x^2 dx -[integral_{- P-/2}^{P+/2) x dx]^2}
        //this gives, sigma^2 = [1/(P+ + P-)]*[ (P+^3 + P-^3)/12 - (P+^2 - P-^2)^2/[32(P+ + P-)] ]
        double Pp2 = pitchToNextStrp * pitchToNextStrp;
        double Pp3 = pitchToNextStrp * Pp2;
        double Pm2 = pitchToPrevStrp * pitchToPrevStrp;
        double Pm3 = pitchToPrevStrp * Pm2;
        double Psum = pitchToNextStrp + pitchToPrevStrp;
        double invPsum = 1. / Psum;
        double firstTerm = (Pp3 + Pm3) / 12.;
        double secondTerm = ((Pp2 - Pm2) * (Pp2 - Pm2) * invPsum) / 32.;
        double strip_sigma_sq = (firstTerm - secondTerm) * invPsum;

        double strip_sigma = Math.sqrt(strip_sigma_sq);
       
        return strip_sigma;
    }
    //****

    public double getDOCAToStrip(int sector, int layer, double centroidstrip, Point3D point0) {

        // local angle of  line graded from 0 to 3 deg.
        double ialpha = (centroidstrip - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID - interc;
        double b2 = interc;

        Vector3D vecAlongStrip = new Vector3D();
        Point3D pointOnStrip = new Point3D();
        Point3D LocPoint = this.transformToFrame(sector, layer, point0.x(), point0.y(), point0.z(), "local", "");

        if (layer % 2 == 0) { //layers 2,4,6 == top ==j ==>(2) : regular configuration
            vecAlongStrip = new Vector3D(m2, 0, 1).asUnit();
            pointOnStrip = new Point3D(b2, 0, 0);
        }
        if (layer % 2 == 1) { //layers 1,3,5 == bottom ==i ==>(1) : regular configuration
            vecAlongStrip = new Vector3D(m1, 0, 1).asUnit();
            pointOnStrip = new Point3D(b1, 0, 0);
        }

        Vector3D r = LocPoint.vectorTo(pointOnStrip); //LocPoint.diff(pointOnStrip);

        Vector3D d = r.cross(vecAlongStrip);

        return d.y();

    }
    
    
    //***
    public Point3D intersectionOfHelixWithPlane(int layer, int sector, Helix helix) {

        int nstep = 1;
        double stepSize = 0.001;

        double Theta = Math.atan2((org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID / 2), org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer - 1)/2][(layer-1)%2]);
        double RMin = org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer - 1)/2][(layer-1)%2];
        double RMax = RMin / Math.cos(Theta);
        double R = RMin;

        Point3D InterPoint = helix.getPointAtRadius(R);

        double minDelta = RMax - RMin;

        while (R < RMax) {

            Point3D I = helix.getPointAtRadius(R);
            Vector3D Inorm = I.toVector3D().asUnit();

            double Rinter = RMin / findBSTPlaneNormal(sector, layer).dot(Inorm);
            double y_rho = Math.sqrt(I.x() * I.x() + I.y() * I.y());
            if (Math.abs(Rinter - y_rho) < minDelta) {
                InterPoint = I;
                minDelta = Math.abs(Rinter - y_rho);
            }
            R += nstep * stepSize;
            nstep++;

        }
        return InterPoint;

    }
    
    public int getIntersectionSector(Helix helix, int layer) {
    	Point3D Or = this.getPlaneModuleOrigin(1, layer);
        
        double cs=helix.getCurvilinearAbsAtRadius(Math.sqrt(Or.x()*Or.x()+Or.y()*Or.y()));
        Vector3D Ideal=helix.getHelixPoint(cs);
        Point3D I=new Point3D(Ideal.x(), Ideal.y(), Ideal.z());
        int sector=findSectorFromAngle(layer, I);
    
        return sector;
    }
    
    public double getRefinedIntersection(Helix helix, int layer, int sector) {
    	//Find the intersection between the helix and the ideal svt plane
    	Point3D Or = this.transformToFrame(sector, layer, 15*org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID, 0, 0, "lab", "");
               
        double cs=org.jlab.rec.cvt.Constants.KFitterStepsize; 
        
        Vector3D norm=this.findBSTPlaneNormal(sector, layer);
    	
        double range=2*org.jlab.rec.cvt.Constants.KFitterStepsize; //mm... computing distance of 3 points to cylinder or plane
    	double csold=cs;
    	for (int iter=0;iter<5;iter++) {
    		Vector3D inter=Point_LabToDetFrame(layer, sector, helix.getHelixPoint(cs));
    		Vector3D interinf=Point_LabToDetFrame(layer, sector, helix.getHelixPoint(cs-range));
    		Vector3D intersup=Point_LabToDetFrame(layer, sector, helix.getHelixPoint(cs+range));
    		    		
    		//When inter is in the plane of the module, the scalar product with the normal vector is 0
    		inter.setXYZ(inter.x()-Or.x(), inter.y()-Or.y(), inter.z()-Or.z());
    		interinf.setXYZ(interinf.x()-Or.x(), interinf.y()-Or.y(), interinf.z()-Or.z());
    		intersup.setXYZ(intersup.x()-Or.x(), intersup.y()-Or.y(), intersup.z()-Or.z());
    	
    		double[][] A=new double[3][3];
    		double[][] B=new double[3][1];
    	
    		B[0][0]=inter.dot(norm); B[0][0]=B[0][0]*B[0][0];
    		B[1][0]=interinf.dot(norm); B[1][0]=B[1][0]*B[1][0];
    		B[2][0]=intersup.dot(norm); B[2][0]=B[2][0]*B[2][0];
    	
    		A[0][0]=cs*cs;
    		A[0][1]=cs;
    		A[0][2]=1;
    	
    		A[1][0]=(cs-range)*(cs-range);
    		A[1][1]=cs-range;
    		A[1][2]=1;
    	
    		A[2][0]=(cs+range)*(cs+range);
    		A[2][1]=cs+range;
    		A[2][2]=1;
    		
    		
    		Matrix matA=new Matrix(A);
    		if (matA.det()>1.e-20) {
    			Matrix invA=matA.inverse();
    			Matrix matB=new Matrix(B);
    			Matrix result=invA.times(matB);
    		
    			cs=-result.get(1, 0)/2./result.get(0, 0);
    			range=Math.abs(cs-csold)/10.;
    		}
    	}
        
          	
    	return cs;
    }
    
    
    //****
    // in the local coordinate system 

    public double getXAtZ(int layer, double centroidstrip, double Z) {
        double X = 0;
        // local angle of  line graded from 0 to 3 deg.
        double ialpha = (centroidstrip - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;
       
        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID - interc;
        double b2 = interc;

        if (layer % 2 == 0) { //layers 2,4,6 == top ==j ==>(2) : regular configuration

            X = m2 * Z + b2;
        }
        if (layer % 2 == 1) { //layers 1,3,5 == bottom ==i ==>(1) : regular configuration

            X = m1 * Z + b1;
        }

        return X;
    }

    //***
    public double getStripIndexShift(int sector, int layer, Vector3D trkDir, double s2, double z) {

        double tx = trkDir.x();
        double ty = trkDir.y();
        Vector3D trkDir_t = new Vector3D(tx / Math.sqrt(tx * tx + ty * ty), ty / Math.sqrt(tx * tx + ty * ty), 0);
        Vector3D n = findBSTPlaneNormal(sector, layer);

        double TrkToPlnNormRelatAngl = Math.acos(n.dot(trkDir_t));
        double sign = Math.signum(n.cross(trkDir_t).z());
        // int shift = (int)((Constants.LAYRGAP*n.cross(trkDir_t).z())/Constants.PITCH);
        //
        //correction to the pitch to take into account the grading of the angle -- get the upper or lower strip depending on the trkdir
        double pitchcorr = org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH;

        if (s2 > 2 && s2 < 255) {
            double pitchToNextStrp = Math.abs(getXAtZ(layer, (double) s2 + 1, z) - getXAtZ(layer, (double) s2, z));
            double pitchToPrevStrp = Math.abs(getXAtZ(layer, (double) s2 - 1, z) - getXAtZ(layer, (double) s2, z));
            pitchcorr = (pitchToNextStrp + pitchToPrevStrp) / 2.;
        }
        if (s2 <= 2) {
            pitchcorr = Math.abs(getXAtZ(layer, (double) s2 + 1, z) - getXAtZ(layer, (double) s2, z));
        }
        if (s2 == 256) {
            pitchcorr = Math.abs(getXAtZ(layer, (double) s2 - 1, z) - getXAtZ(layer, (double) s2, z));
        }

        double shift = sign * org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERGAPTHK * Math.tan(TrkToPlnNormRelatAngl) / pitchcorr;

        return -shift;
    }
    //***

    public double planeNormDotTrkDir(int sector, int layer, Point3D trkDir, double s2, double z) {
        double tx = trkDir.x();
        double ty = trkDir.y();
        Vector3D trkDir_t = new Vector3D(tx / Math.sqrt(tx * tx + ty * ty), ty / Math.sqrt(tx * tx + ty * ty), 0);
        Vector3D n = findBSTPlaneNormal(sector, layer);

        return Math.abs(n.dot(trkDir_t));
    }

    //***
    public Point3D recalcCrossFromTrajectoryIntersWithModulePlanes(int s, double s1, double s2,
        int l1, int l2, double trajX1, double trajY1, double trajZ1,
        double trajX2, double trajY2, double trajZ2) {
        Point3D LocPoint1 = this.transformToFrame(s, l1, trajX1, trajY1, trajZ1, "local", "");
        Point3D LocPoint2 = this.transformToFrame(s, l2, trajX2, trajY2, trajZ2, "local", "");
        double m = (LocPoint1.x() - LocPoint2.x()) / (LocPoint1.z() - LocPoint2.z());
        double b = LocPoint1.x() - m * LocPoint1.z();

        double ialpha1 = (s1 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;
        double ialpha2 = (s2 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID - interc1;
        double b2 = interc2;

        double z1 = (b - b1) / (m1 - m);
        double x1 = m1 * z1 + b1;
        double z2 = (b - b2) / (m2 - m);
        double x2 = m2 * z2 + b2;

        Point3D Point1 = this.transformToFrame(s, l1, x1, 0, z1, "lab", "");
        Point3D Point2 = this.transformToFrame(s, l2, x2, 0, z2, "lab", "");
        // unit vec along dir of track
        Vector3D t = new Vector3D(Point2.x() - Point1.x(), Point2.y() - Point1.y(), Point2.z() - Point1.z()).asUnit();
        //normal to plane of module
        Vector3D n = this.findBSTPlaneNormal(s, l1);
        //path length tranversed inbetween modules
        double l = (org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(l2 - 1)/2][(l2 - 1)%2] - org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(l1 - 1)/2][(l1 - 1)%2]) / (n.dot(t));
        //Point inbetween the modules			

        Point3D Point = new Point3D(Point1.x() + t.x() * ((double) l / 2), Point1.y() + t.y() * ((double) l / 2), Point1.z() + t.z() * ((double) l / 2));

        return Point;
    }

    public double[][] getStripEndPoints(int strip, int slyr) { //1 top, 0 bottom

        double[][] X = new double[2][2];

        double z1 = 0;
        double x1 = 0;
        double z2 = 0;
        double x2 = 0;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        if (slyr == 0) {
            double s1 = strip;
            double ialpha1 = (s1 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
            //the active area starts at the first strip 	
            double interc1 = (s1 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;
            double m1 = -Math.tan(ialpha1);
            double b1 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID - interc1;

            z1 = 0;
            x1 = m1 * z1 + b1;
            z2 = org.jlab.detector.geant4.v2.SVT.SVTConstants.MODULELEN;
            x2 = m1 * z2 + b1;

            if (x2 < 0) {
                x2 = 0;
                z2 = -b1 / m1;
            }
        }

        if (slyr == 1) {
            double s2 = strip;
            double ialpha2 = (s2 - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
            //the active area starts at the first strip 	
            double interc2 = (s2 - 0.5) * org.jlab.detector.geant4.v2.SVT.SVTConstants.READOUTPITCH + org.jlab.detector.geant4.v2.SVT.SVTConstants.STRIPOFFSETWID;
            double m2 = Math.tan(ialpha2);
            double b2 = interc2;

            z1 = 0;
            x1 = m2 * z1 + b2;
            z2 = org.jlab.detector.geant4.v2.SVT.SVTConstants.MODULELEN;
            x2 = m2 * z2 + b2;

            if (x2 > org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID) {
                x2 = org.jlab.detector.geant4.v2.SVT.SVTConstants.ACTIVESENWID;
                z2 = (x2 - b2) / m2;
            }
        }
        X[0][0] = x1;
        X[0][1] = z1;
        X[1][0] = x2;
        X[1][1] = z2;

        return X;

    }

    /*public void applyShift( Point3D cross, int region, int sector ) {
        Vector3d aPoint = new Vector3d(cross.x(), cross.y(), cross.z());
        Vector3d Cu_m = new Vector3d( -SVTConstants.FIDCUX, 0.0, -SVTConstants.FIDCUZ );
        Vector3d Cu_p = new Vector3d( SVTConstants.FIDCUX, 0.0, -SVTConstants.FIDCUZ );
        Vector3d Pk = new Vector3d( SVTConstants.FIDPKX, 0.0, SVTConstants.FIDPKZ0 + SVTConstants.FIDPKZ1 );
        double fidOriginZ = SVTConstants.Z0ACTIVE[region-1] - SVTConstants.DEADZNLEN - SVTConstants.FIDORIGINZ;
        double heatSinkTotalThk = SVTConstants.MATERIALDIMENSIONS.get("heatSink")[1];
        double radius = SVTConstants.SUPPORTRADIUS[region-1] + heatSinkTotalThk;
       
        Transform labFrame = SVTConstants.getLabFrame( region-1, sector-1, radius, fidOriginZ );
        Cu_m.transform(labFrame);
        Cu_p.transform(labFrame);
        Pk.transform(labFrame);
        Triangle3d fidTri3D = new Triangle3d( Cu_m, Cu_p, Pk );
        Vector3d aCenter = fidTri3D.center();
        {
                System.out.printf("PN: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );

                System.out.printf("SC: % 8.3f % 8.3f % 8.3f\n", aCenter.x, aCenter.y, aCenter.z );
        }

        // do the rotation here.
        if( !(SVTConstants.RA[region-1][sector-1] < 1E-3) ) {			
                aCenter.times( -1 ); // reverse translation
                aPoint.add( aCenter ) ; // move origin to center of rotation axis
                System.out.printf("        --> before rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
                Vector3d vecAxis = new Vector3d(SVTConstants.RX[region-1][sector-1], SVTConstants.RY[region-1][sector-1], SVTConstants.RZ[region-1][sector-1] ).normalized();			
                vecAxis.rotate( aPoint, Math.toRadians(SVTConstants.RA[region-1][sector-1]) );
                //aPoint.rotateZ(Math.toRadians(Constants.RA[region-1][sector-1]));
                System.out.printf(SVTConstants.RA[region-1][sector-1]+"        --> rotat ax : % 8.4f % 8.4f % 8.4f\n", vecAxis.x, vecAxis.y, vecAxis.z );
                System.out.printf("        --> after rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
                aCenter.times( -1 ); // reverse translation
                aPoint.add( aCenter ) ;
                System.out.printf("        --> center : % 8.4f % 8.4f % 8.4f\n", aCenter.x, aCenter.y, aCenter.z );
                System.out.printf("        --> rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
        }

        // do the translation here.
        Vector3d translationVec = new Vector3d( SVTConstants.TX[region-1][sector-1], SVTConstants.TY[region-1][sector-1], SVTConstants.TZ[region-1][sector-1] );
        aPoint.set( aPoint.add( translationVec ) ); 
        //if(Math.abs(cross.x()-aPoint.x)<1 && Math.abs(cross.y()-aPoint.y)<1) {
        System.out.println("  unshifted \n"+cross.toString()+" in sector "+sector+" region "+region);
        cross.set(aPoint.x, aPoint.y, aPoint.z);
        System.out.printf("        --> shifted : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
       // }
    }*/



/**
 * Applies the inverse of the given alignment shift to the given point.  gilfoyle 12/21/17
 * 
 * @param aPoint a point in the lab frame
 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
 * @param aCenter a point about which to rotate the first point (for example the midpoint of the ideal fiducials)
 * @param aScaleT a scale factor for the translation shift
 * @param aScaleR a scale factor for the rotation shift
 * @throws IllegalArgumentException incorrect number of elements in shift array
 */
public static void applyInverseShift( Vector3d aPoint, double[] aShift, Vector3d aCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
{

        double tx = aShift[0]; // The Java language has references but you cannot dereference the memory addresses like you can in C++.
        double ty = aShift[1]; // The Java runtime does have pointers, but they're not accessible to the programmer. (no pointer arithmetic)
        double tz = aShift[2];
        double rx = aShift[3];
        double ry = aShift[4];
        double rz = aShift[5];
        double ra = aShift[6];

        tx *= aScaleT;
        ty *= aScaleT;
        tz *= aScaleT;
        ra *= aScaleR;


        {
                System.out.printf("PN: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
                System.out.printf("ST: % 8.3f % 8.3f % 8.3f\n", tx, ty, tz );
                System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", rx, ry, rz, Math.toDegrees(ra) );
                System.out.printf("SC: % 8.3f % 8.3f % 8.3f\n", aCenter.x, aCenter.y, aCenter.z );
        }

        // undo the translation.
        Vector3d translationVec = new Vector3d( -tx, -ty, -tz );
        aPoint.set( aPoint.add( translationVec ) );

        // test size of rotation - too small creates errors.
        if( !(ra < 1E-3) )
        {			
                aCenter.times( -1 ); // reverse translation
                aPoint.set( aPoint.add( aCenter ) ); // move origin to center of rotation axis

                //System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );

                Vector3d vecAxis = new Vector3d( rx, ry, rz ).normalized();			
                vecAxis.rotate( aPoint, -ra );

                //System.out.printf("PR: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );

                aCenter.times( -1 ); // reverse translation
                aPoint.set( aPoint.add( aCenter ) );

                //System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
        }


        System.out.printf("PS: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
    }

	public Vector3D Slope_LabToDetFrame(int layer, int sector, Vector3D slope) {	
		Vector3D new_slope = new Vector3D();
		new_slope.setX(slope.x()); new_slope.setY(slope.y()); new_slope.setZ(slope.z());
		new_slope.rotateX(this.getRx(layer,sector));
		new_slope.rotateY(this.getRy(layer,sector));
		new_slope.rotateZ(this.getRz(layer,sector));
	
		return new_slope;
	}
 
	public Vector3D Point_LabToDetFrame(int layer, int sector, Vector3D point) {	
		Vector3D new_point = new Vector3D();
		new_point.setX(point.x()); new_point.setY(point.y()); new_point.setZ(point.z());
		new_point.rotateX(this.getRx(layer,sector));
		new_point.rotateY(this.getRy(layer,sector));
		new_point.rotateZ(this.getRz(layer,sector));
		new_point.setX(new_point.x()+this.getCx(layer,sector));
		new_point.setY(new_point.y()+this.getCy(layer,sector));
		new_point.setZ(new_point.z()+this.getCz(layer,sector));
				
		return new_point;
	}
 
	public Vector3D Slope_DetToLabFrame(int layer, int sector, Vector3D slope) {	
		Vector3D new_slope = new Vector3D();
		new_slope.setX(slope.x()); new_slope.setY(slope.y()); new_slope.setZ(slope.z());
		new_slope.rotateZ(-this.getRz(layer,sector));
		new_slope.rotateY(-this.getRy(layer,sector));
		new_slope.rotateX(-this.getRx(layer,sector));
	
		return new_slope;
	}
  
	public Vector3D Point_DetToLabFrame(int layer, int sector, Vector3D point) {	
		Vector3D new_point = new Vector3D();
		new_point.setX(point.x()); new_point.setY(point.y()); new_point.setZ(point.z());
		new_point.setX(new_point.x()-this.getCx(layer,sector));
		new_point.setY(new_point.y()-this.getCy(layer,sector));
		new_point.setZ(new_point.z()-this.getCz(layer,sector));
		new_point.rotateZ(-this.getRz(layer,sector));
		new_point.rotateY(-this.getRy(layer,sector));
		new_point.rotateX(-this.getRx(layer,sector));
	
		return new_point;
	}
	
	public Vector3D getIntersectWithRay(int layer, int sectorcluster, Vector3D slope_lab, Vector3D pt_lab) {
		Vector3D inter=new Vector3D();
		//int sector=findSectorFromAngle(layer,pt_line);
		inter.setXYZ(Double.NaN, Double.NaN, Double.NaN);
		if (sectorcluster>0) {
			Vector3D n=findBSTPlaneNormal(sectorcluster, layer);
			Point3D p=getPlaneModuleOrigin(sectorcluster, layer);
			
			Vector3D dir_line=this.Slope_LabToDetFrame(layer, sectorcluster, slope_lab);
			Vector3D pt_line=this.Point_LabToDetFrame(layer, sectorcluster, pt_lab);
		
			if (dir_line.x()*n.x()+dir_line.y()*n.y()+dir_line.z()*n.z()==0) inter.setXYZ(Double.NaN, Double.NaN, Double.NaN);
			else {
				double lambda=(n.x()*(p.x()-pt_line.x())+n.y()*(p.y()-pt_line.y())+n.z()*(p.z()-pt_line.z()))
						/(dir_line.x()*n.x()+dir_line.y()*n.y()+dir_line.z()*n.z());
				inter.setX(lambda*dir_line.x()+pt_line.x());
				inter.setY(lambda*dir_line.y()+pt_line.y());
				inter.setZ(lambda*dir_line.z()+pt_line.z());
				
			}
		}
		else inter.setXYZ(Double.NaN, Double.NaN, Double.NaN);
		return inter;
	}
	
	public ArrayList<Integer> getSectIntersect(int layer, Vector3D dir_line, Vector3D pt_line) {
		Point3D inter=new Point3D();
		inter.set(Double.NaN, Double.NaN, Double.NaN);
		ArrayList<Integer> hit_sec=new ArrayList<Integer>();
		for (int sec=1;sec<this.getNbModule(layer)+1;sec++) {
			Vector3D n=findBSTPlaneNormal(sec, layer);
			Point3D p=getPlaneModuleOrigin(sec, layer);
		
			if (dir_line.x()*n.x()+dir_line.y()*n.y()+dir_line.z()*n.z()!=0) {
				double lambda=(n.x()*(p.x()-pt_line.x())+n.y()*(p.y()-pt_line.y())+n.z()*(p.z()-pt_line.z()))
						/(dir_line.x()*n.x()+dir_line.y()*n.y()+dir_line.z()*n.z());
				inter.setX(lambda*dir_line.x()+pt_line.x());
				inter.setY(lambda*dir_line.y()+pt_line.y());
				inter.setZ(lambda*dir_line.z()+pt_line.z());
				if (sec==this.findSectorFromAngle(layer, inter)) hit_sec.add(sec);
			}
		
		}
		return hit_sec;
	}
	
	public double getResidual_line(int layer, int sector, double strip, Vector3D point) {
		double dist=0;
		Point3D Loc=new Point3D(this.transformToFrame(sector, layer, point.x(), point.y(), point.z(), "local", ""));
		double x_strip=this.getXAtZ(layer, strip, Loc.z());
		double ialpha1 = (strip - 1) * org.jlab.detector.geant4.v2.SVT.SVTConstants.STEREOANGLE / (double) (org.jlab.detector.geant4.v2.SVT.SVTConstants.NSTRIPS - 1);
		dist=(Loc.x()-x_strip)*Math.cos(ialpha1);
		return dist;
	}
	
	public double getLocalX(double xdet, double ydet, double zdet, int lay, int sec) {
		Point3D Loc=new Point3D(this.transformToFrame(sec, lay, xdet, ydet, zdet, "local", "")); //Prediction
		return Loc.x();
	}
	
	public double getMeasurementAtZ(double xdet, double ydet, double zdet,int layer, int sector, double strip) {
		Point3D Loc=new Point3D(this.transformToFrame(sector, layer, xdet, ydet, zdet, "local", ""));
		double x_strip=this.getXAtZ(layer, strip, Loc.z());
		return x_strip;
	}
	
	public double[] ComputeAngles(int lay, int sec, Vector3D direction) {
    	double[] angle=new double[3];
    	Vector3D er=this.findBSTPlaneNormal(sec, lay);
    	Vector3D ez=new Vector3D(0,0,1);
    	Vector3D etheta=er.cross(ez);
    	    	
    	
    	double dot_theta=direction.dot(etheta);
    	etheta.setXYZ(dot_theta*etheta.x(), dot_theta*etheta.y(), 0);
    	
    	Vector3D dir_rTheta=new Vector3D(direction.x(),direction.y(),0);
    	    	
    	angle[0]=er.angle(direction);//angle with er
    	angle[1]=er.angle(dir_rTheta);//Angle in er ethteta plane
    	angle[2]=er.angle(direction.sub(etheta));//Angle in er/ez plane
    	
    	return angle;
    }
	
    public static void main(String arg[]) throws FileNotFoundException {

        
        int s1 = 1;
        int s2 = 1;

        Geometry geo = new Geometry();

        //System.out.println("  old geom strip inter " + geo.getLocCoord(s1, s2)[0] + "," + geo.getLocCoord(s1, s2)[1]);

        /*
	    	 * X[0][0] = x1;
			X[0][1] = z1;
			X[1][0] = x2;
			X[1][1] = z2;
         */
        //System.out.println(" end points 1"+geo.getStripEndPoints(s1, 0)[0][0]+", "+geo.getStripEndPoints(s1, 0)[0][1]);
        //System.out.println(" end points "+(geo.getStripEndPoints(10, 1)[0][0]-Constants.ACTIVESENWIDTH/2)+", "+geo.getStripEndPoints(10, 1)[0][1]+"; "
        //		+(geo.getStripEndPoints(10, 1)[1][0]-Constants.ACTIVESENWIDTH/2)+", "+geo.getStripEndPoints(10, 1)[1][1]);
        double[][] X = geo.getStripEndPoints(s1, 0);
        double[][] Y = geo.getStripEndPoints(s2, 1);
        System.out.println(" ep1 loc x " + X[0][0] + ", y" + X[0][1] + ", x " + X[1][0] + ", y " + X[1][1] + " ep2 loc x " + Y[0][0] + ", y" + Y[0][1] + ", x " + Y[1][0] + ", y " + Y[1][1]);
        Point3D EP1u = geo.transformToFrame(1, 1, X[0][0], 0, X[0][1], "lab", "");
        Point3D EP2u = geo.transformToFrame(1, 1, X[1][0], 0, X[1][1], "lab", "");

        Point3D EP1v = geo.transformToFrame(1, 2, Y[0][0], 0, Y[0][1], "lab", "");
        Point3D EP2v = geo.transformToFrame(1, 2, Y[1][0], 0, Y[1][1], "lab", "");
        System.out.println(EP1u.toString());
        System.out.println(EP2u.toString());
        System.out.println(EP1v.toString());
        System.out.println(EP2v.toString());
    }

	public int getNbModule(int lay) {
		// TODO Auto-generated method stub
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.NSECTORS[(lay-1)/2];
	}
	
	public void setRx(int lay, int sec, double rx) {
		Rx[lay-1][sec-1]=rx;
	}
	
	public void setRy(int lay, int sec, double ry) {
		Ry[lay-1][sec-1]=ry;
	}
	
	public void setRz(int lay, int sec, double rz) {
		Rz[lay-1][sec-1]=rz;
	}
	
	public void setCx(int lay, int sec, double cx) {
		Cx[lay-1][sec-1]=cx;
	}
	
	public void setCy(int lay, int sec, double cy) {
		Cy[lay-1][sec-1]=cy;
	}
	
	public void setCz(int lay, int sec, double cz) {
		Cz[lay-1][sec-1]=cz;
	}
	
	public double getRx(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Rx[lay-1][sec-1];
	}
	
	public double getRy(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Ry[lay-1][sec-1];
	}
	
	public double getRz(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Rz[lay-1][sec-1];
	}
	
	public double getCx(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Tx[lay-1][sec-1];
	}
	
	public double getCy(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Ty[lay-1][sec-1];
	}
	
	public double getCz(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.Tz[lay-1][sec-1];
	}
	
	public double getLocTx(int lay, int sec) {
		if (!org.jlab.rec.cvt.Constants.WithAlignment) return 0.0;
		return org.jlab.detector.geant4.v2.SVT.SVTConstants.LocTx[lay-1][sec-1];
	}
	
	public void LoadMisalignmentFromFile(String FileName) throws IOException{
		File GeoTrans=new File(FileName);
		
		String separator = "\\s+";
		
		if (GeoTrans.exists()) {
			System.out.println("Opening misalignment file for SVT: "+FileName);
			String[] line=new String[8];
			int linenumber=0;
			Scanner input = new Scanner(GeoTrans);
            while (input.hasNextLine()) {
            	line = input.nextLine().trim().replaceAll(separator, " ").split(separator);
            	if (Integer.parseInt(line[0])<=6) {
            	//Rx   Ry    Rz    Tx    Ty     Tz => order of columns inside the file
            		this.setRx(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[2]));
            		this.setRy(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[3]));
            		this.setRz(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[4]));
            		this.setCx(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[5]));
            		this.setCy(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[6]));
            		this.setCz(Integer.parseInt(line[0]),Integer.parseInt(line[1]), Double.parseDouble(line[7]));
            	}
			//linenumber++;
			}
		}
		
	}

	public double getRadius(int layer) {
		// TODO Auto-generated method stub
		double Rm=org.jlab.detector.geant4.v2.SVT.SVTConstants.LAYERRADIUS[(layer - 1)/2][(layer - 1)%2];
		return Rm;
	}

}
