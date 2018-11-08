package org.jlab.rec.cvt.svt;

//import java.io.File;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.FileNotFoundException;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Triangle3d;
import org.jlab.rec.cvt.trajectory.Helix;

public class Geometry {

    public Geometry() {

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
        Point3D point0 = new Point3D(transformToFrame(sector, layer, Constants.ACTIVESENWIDTH + 1, 0, 0, "lab", ""));
        return point0;
    }

    //*** 
    public int findSectorFromAngle(int layer, Point3D trkPoint) {
        int Sect = Constants.NSECT[layer - 1];
        for (int s = 0; s < Constants.NSECT[layer - 1] - 1; s++) {
            int sector = s + 1;
            Vector3D orig = new Vector3D(getPlaneModuleOrigin(sector, layer).x(), getPlaneModuleOrigin(sector, layer).y(), 0);
            Vector3D end = new Vector3D(getPlaneModuleEnd(sector, layer).x(), getPlaneModuleEnd(sector, layer).y(), 0);
            Vector3D trk = new Vector3D(trkPoint.x(), trkPoint.y(), 0);
            orig.unit();
            end.unit();
            trk.unit();

            double phi1 = orig.dot(trk);
            double phi2 = trk.dot(end);
            double phiRange = orig.dot(end);

            if (Math.acos(phi1) < Math.acos(phiRange) && Math.acos(phi2) < Math.acos(phiRange)) {
                Sect = sector;
            }
        }
        return Sect;
    }

    //***
    public Vector3D findBSTPlaneNormal(int sector, int layer) {

        //double angle = 2.*Math.PI*((double)(sector-1)/(double)Constants.NSECT[layer-1]) + Math.PI/2.;
        double angle = 2. * Math.PI * ((double) -(sector - 1) / (double) Constants.NSECT[layer - 1]) + Constants.PHI0[layer - 1];

        return new Vector3D(Math.cos(angle), Math.sin(angle), 0);
    }
    //***

    public double[] getLocCoord(double s1, double s2) { //2 top, 1 bottom

        double[] X = new double[2];
        double ialpha1 = (s1 - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * Constants.PITCH + Constants.STRIPTSTART;
        double ialpha2 = (s2 - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * Constants.PITCH + Constants.STRIPTSTART;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = Constants.ACTIVESENWIDTH - interc1;
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

    public Point3D transformToFrame(int sector, int layer, double x, double y, double z, String frame, String MiddlePlane) {

        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) Constants.NSECT[layer - 1]) * 2. * Math.PI + Constants.PHI0[layer - 1];
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - Constants.LOCZAXISROTATION;

        double gap = 0;
        if (MiddlePlane.equals("middle")) {
            if ((layer - 1) % 2 == 0) { // for a cross take the bottom layer
                gap = Constants.MODULERADIUS[layer][sector - 1] - Constants.MODULERADIUS[layer - 1][sector - 1];
            }
        }
        double lTx = (Constants.MODULERADIUS[layer - 1][sector - 1] + 0.5 * gap) * Math.cos(Glob_rangl);
        double lTy = (Constants.MODULERADIUS[layer - 1][sector - 1] + 0.5 * gap) * Math.sin(Glob_rangl);
        double lTz = Constants.Z0[layer - 1];

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
    }
    //*** point and its error

    public double[] getCrossPars(int sector, int upperlayer, double s1, double s2, String frame, Vector3D trkDir) {
        double[] vals = new double[6];

        // if first iteration trkDir == null
        double s2corr = s2;
        // now use track info
        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, Constants.MODULELENGTH);
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

        Point3D crPoint = transformToFrame(sector, upperlayer - 1, LC_x, 0, LC_z, "lab", "middle");
        // test shifts
        //this.applyShift(crPoint, upperlayer / 2, sector);
        vals[0] = crPoint.x();
        vals[1] = crPoint.y();
        vals[2] = crPoint.z();

        double[] LCErr = getLocCoordErrs(upperlayer - 1, upperlayer, s1, s2corr, zf);
        double LCErr_x = LCErr[0];
        double LCErr_z = LCErr[1];

        // global rotation angle to get the error in the lab frame
        int layerIdx = upperlayer - 1;
        /*
			double Glob_rangl = ((double) (sector-1)/(double) Constants.NSECT[layerIdx])*2.*Math.PI;
	        // angle to rotate to global frame
	        double Loc_to_Glob_rangl = Glob_rangl-Constants.PHI0[layerIdx];
         */
        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) Constants.NSECT[layerIdx]) * 2. * Math.PI + Constants.PHI0[layerIdx];
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - Constants.LOCZAXISROTATION;

        double cosRotation = Math.cos(Loc_to_Glob_rangl);
        double sinRotation = Math.sin(Loc_to_Glob_rangl);

        double yerr = Math.abs(cosRotation * LCErr_x);
        double xerr = Math.abs(sinRotation * LCErr_x);

        vals[3] = xerr;
        vals[4] = yerr;
        vals[5] = LCErr_z;

        if (LC_z > Constants.MODULELENGTH + Constants.interTol * 2) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }
        // once there is a trk, the cross should be well calculated
        //if the local cross is not in the fiducial volume it is not physical
        if ((trkDir != null && (LC_x < 0 || LC_x > Constants.ACTIVESENWIDTH + Constants.TOLTOMODULEEDGE))
                || (trkDir != null && (LC_z < -Constants.interTol || LC_z > Constants.MODULELENGTH + Constants.interTol))) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }

        //if(vals[5]<Constants.Z0[upperlayer-1]-Constants.interTol || vals[5]>Constants.Z0[upperlayer-1]+Constants.MODULELENGTH+Constants.interTol) {	
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

    private double getCorrectedStrip(int sector, int upperlayer, double s2,
            Vector3D trkDir, double ZalongModule) {
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

        double alpha = Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);

        double b = Constants.ACTIVESENWIDTH;
        double P = Constants.PITCH;

        double s = -1;

        if (layer % 2 == 1) {//layers 1,3,5 == bottom ==i ==>(1) : regular configuration
            //m1,b1
            s = (int) Math.floor((-x + b + alpha * z + 0.5 * P - Constants.STRIPTSTART) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - Constants.STRIPTSTART;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - Constants.STRIPTSTART;

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
            s = (int) Math.floor((x + alpha * z + 0.5 * P - Constants.STRIPTSTART) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + Constants.STRIPTSTART;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + Constants.STRIPTSTART;

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
        if (strip == Constants.NSTRIP) {
            StripUp = (double) Constants.NSTRIP; //edge strip
        }
        double StripDown = Strip - 1;
        if (strip == 1) {
            StripDown = 1; //edge strip
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
        double ialpha = (centroidstrip - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * Constants.PITCH + Constants.STRIPTSTART;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = Constants.ACTIVESENWIDTH - interc;
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
    //****
    // in the local coordinate system 

    public double getXAtZ(int layer, double centroidstrip, double Z) {
        double X = 0;
        // local angle of  line graded from 0 to 3 deg.
        double ialpha = (centroidstrip - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * Constants.PITCH + Constants.STRIPTSTART;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = Constants.ACTIVESENWIDTH - interc;
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

        if (org.jlab.rec.cvt.Constants.isCosmicsData() && Math.acos(n.dot(trkDir_t)) > Math.PI / 2) // flip the direction of the track for y<0 for cosmics
        {
            trkDir_t = new Vector3D(-trkDir_t.x(), -trkDir_t.y(), 0);
        }

        double TrkToPlnNormRelatAngl = Math.acos(n.dot(trkDir_t));
        double sign = Math.signum(n.cross(trkDir_t).z());
        // int shift = (int)((Constants.LAYRGAP*n.cross(trkDir_t).z())/Constants.PITCH);
        //
        //correction to the pitch to take into account the grading of the angle -- get the upper or lower strip depending on the trkdir
        double pitchcorr = Constants.PITCH;

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

        double layerGap = Constants.MODULERADIUS[1][0] - Constants.MODULERADIUS[0][0];

        double shift = sign * layerGap * Math.tan(TrkToPlnNormRelatAngl) / pitchcorr;

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
    public Point3D intersectionOfHelixWithPlane(int layer, int sector, Helix helix) {

        int nstep = 1;
        double stepSize = 0.001;

        double Theta = Math.atan2((Constants.ACTIVESENWIDTH / 2), Constants.MODULERADIUS[layer - 1][sector - 1]);
        double RMin = Constants.MODULERADIUS[layer - 1][sector - 1];
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

    public Point3D recalcCrossFromTrajectoryIntersWithModulePlanes(int s, double s1, double s2,
        int l1, int l2, double trajX1, double trajY1, double trajZ1,
        double trajX2, double trajY2, double trajZ2) {
        Point3D LocPoint1 = this.transformToFrame(s, l1, trajX1, trajY1, trajZ1, "local", "");
        Point3D LocPoint2 = this.transformToFrame(s, l2, trajX2, trajY2, trajZ2, "local", "");
        double m = (LocPoint1.x() - LocPoint2.x()) / (LocPoint1.z() - LocPoint2.z());
        double b = LocPoint1.x() - m * LocPoint1.z();

        double ialpha1 = (s1 - 1) * org.jlab.rec.cvt.svt.Constants.STEREOANGLE / (double) (org.jlab.rec.cvt.svt.Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * org.jlab.rec.cvt.svt.Constants.PITCH + org.jlab.rec.cvt.svt.Constants.STRIPTSTART;
        double ialpha2 = (s2 - 1) * org.jlab.rec.cvt.svt.Constants.STEREOANGLE / (double) (org.jlab.rec.cvt.svt.Constants.NSTRIP - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * org.jlab.rec.cvt.svt.Constants.PITCH + org.jlab.rec.cvt.svt.Constants.STRIPTSTART;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = org.jlab.rec.cvt.svt.Constants.ACTIVESENWIDTH - interc1;
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
        double l = (org.jlab.rec.cvt.svt.Constants.MODULERADIUS[l2 - 1][0] - org.jlab.rec.cvt.svt.Constants.MODULERADIUS[l1 - 1][0]) / (n.dot(t));
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
            double ialpha1 = (s1 - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
            //the active area starts at the first strip 	
            double interc1 = (s1 - 0.5) * Constants.PITCH + Constants.STRIPTSTART;
            double m1 = -Math.tan(ialpha1);
            double b1 = Constants.ACTIVESENWIDTH - interc1;

            z1 = 0;
            x1 = m1 * z1 + b1;
            z2 = Constants.MODULELENGTH;
            x2 = m1 * z2 + b1;

            if (x2 < 0) {
                x2 = 0;
                z2 = -b1 / m1;
            }
        }

        if (slyr == 1) {
            double s2 = strip;
            double ialpha2 = (s2 - 1) * Constants.STEREOANGLE / (double) (Constants.NSTRIP - 1);
            //the active area starts at the first strip 	
            double interc2 = (s2 - 0.5) * Constants.PITCH + Constants.STRIPTSTART;
            double m2 = Math.tan(ialpha2);
            double b2 = interc2;

            z1 = 0;
            x1 = m2 * z1 + b2;
            z2 = Constants.MODULELENGTH;
            x2 = m2 * z2 + b2;

            if (x2 > Constants.ACTIVESENWIDTH) {
                x2 = Constants.ACTIVESENWIDTH;
                z2 = (x2 - b2) / m2;
            }
        }
        X[0][0] = x1;
        X[0][1] = z1;
        X[1][0] = x2;
        X[1][1] = z2;

        return X;

    }

    public void applyShift( Point3D cross, int region, int sector ) {
        Vector3d aPoint = new Vector3d(cross.x(), cross.y(), cross.z());
        Vector3d Cu_m = new Vector3d( -Constants.FIDCUX, 0.0, -Constants.FIDCUZ );
        Vector3d Cu_p = new Vector3d( Constants.FIDCUX, 0.0, -Constants.FIDCUZ );
        Vector3d Pk = new Vector3d( Constants.FIDPKX, 0.0, Constants.FIDPKZ0 + Constants.FIDPKZ1 );
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
        if( !(Constants.RA[region-1][sector-1] < 1E-3) ) {			
                aCenter.times( -1 ); // reverse translation
                aPoint.add( aCenter ) ; // move origin to center of rotation axis
                System.out.printf("        --> before rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
                Vector3d vecAxis = new Vector3d(Constants.RX[region-1][sector-1], Constants.RY[region-1][sector-1], Constants.RZ[region-1][sector-1] ).normalized();			
                vecAxis.rotate( aPoint, Math.toRadians(Constants.RA[region-1][sector-1]) );
                //aPoint.rotateZ(Math.toRadians(Constants.RA[region-1][sector-1]));
                System.out.printf(Constants.RA[region-1][sector-1]+"        --> rotat ax : % 8.4f % 8.4f % 8.4f\n", vecAxis.x, vecAxis.y, vecAxis.z );
                System.out.printf("        --> after rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
                aCenter.times( -1 ); // reverse translation
                aPoint.add( aCenter ) ;
                System.out.printf("        --> center : % 8.4f % 8.4f % 8.4f\n", aCenter.x, aCenter.y, aCenter.z );
                System.out.printf("        --> rotat : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
        }

        // do the translation here.
        Vector3d translationVec = new Vector3d( Constants.TX[region-1][sector-1], Constants.TY[region-1][sector-1], Constants.TZ[region-1][sector-1] );
        aPoint.set( aPoint.add( translationVec ) ); 
        //if(Math.abs(cross.x()-aPoint.x)<1 && Math.abs(cross.y()-aPoint.y)<1) {
        System.out.println("  unshifted \n"+cross.toString()+" in sector "+sector+" region "+region);
        cross.set(aPoint.x, aPoint.y, aPoint.z);
        System.out.printf("        --> shifted : % 8.4f % 8.4f % 8.4f\n", aPoint.x, aPoint.y, aPoint.z );
       // }
    }



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
	
    public static void main(String arg[]) throws FileNotFoundException {

        Constants.Load();

        int s1 = 1;
        int s2 = 1;

        Geometry geo = new Geometry();

        System.out.println("  old geom strip inter " + geo.getLocCoord(s1, s2)[0] + "," + geo.getLocCoord(s1, s2)[1]);

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

        //System.out.println(geo.calcNearestStrip(25.66, -66.55, 1.37,2, 10) );
        //System.out.println(geo.transformToFrame(8, 1, 66.3, 7.8, 38.6, "local", "").z()-Constants.ACTIVESENLEN*2-2*0.835-Constants.ACTIVESENLEN/2);
        /*
	    	Line3D stripLine1 = svt.createStrip(s1-1);
	    	Line3D stripLine2 = svt.createStrip(s2-1);	    	
			stripLine2.rotateZ(Math.toRadians(180));
			Transformation3D transform = new Transformation3D();
			transform.translateXYZ( -svt.DEADZNWID + svt.MODULEWID/2 , 0, -svt.DEADZNLEN + svt.MODULELEN/2 ); // align 
			
	    	transform.apply( stripLine1 );
			transform.apply( stripLine2 );
         */
 /*
	    	double[] LC = geo.getLocCoord(136,39);
			double LC_x = LC[0];
			double LC_z = LC[1];
			
			Point3D crPoint = geo.transformToFrame( 10,  2-1, LC_x, 0, LC_z, "lab", "middle");
			
	    	
	    	double m1 = (stripLine1.origin().x() - stripLine1.end().x() )/(stripLine1.origin().z() - stripLine1.end().z() );
	    	double m2 = (stripLine2.origin().x() - stripLine2.end().x() )/(stripLine2.origin().z() - stripLine2.end().z() );
	    	double b1 = stripLine1.origin().x() - stripLine1.origin().z()*m1;
	    	double b2 = stripLine2.origin().x() - stripLine2.origin().z()*m2;
	    	double z = (b2-b1)/(m1-m2);
			double x = m1*z +b1;
	    	System.out.println(" x "+x +" z "+z+" my geo "+crPoint.toString());
	    	
	    	System.out.println(geo.getPlaneModuleOrigin(1, 1).toString() );
         */
 /*
	    	int l = 1;
	    	int s = 6;
	    	double s10 = geo.calcNearestStrip(0., Constants.MODULERADIUS[l-1][s-1], 0, l, s);
	    	double s20 = geo.calcNearestStrip(0., Constants.MODULERADIUS[l][s-1], 0, l+1, s);
	    	
	    	double s1 = geo.calcNearestStrip(0.5, Constants.MODULERADIUS[l-1][s-1], 0, l, s);
	    	double s2 = geo.calcNearestStrip(0.5, Constants.MODULERADIUS[l][s-1], 0, l+1, s);
	    	
	    	
	    	System.out.println("D "+geo.getLocCoord(s10, s20)[0]+","+geo.getLocCoord(s10, s20)[1]+"  ;  "+geo.getLocCoord(s1, s2)[0]+","+geo.getLocCoord(s1, s2)[1]);
         */
    }

}
