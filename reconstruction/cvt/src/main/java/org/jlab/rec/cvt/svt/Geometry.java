package org.jlab.rec.cvt.svt;

//import java.io.File;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.io.FileNotFoundException;
import org.jlab.geom.prim.Line3D;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.trajectory.Helix;

import org.jlab.detector.geant4.v2.SVT.*;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.detector.geant4.v2.SVT.AlignmentFactory;
import org.jlab.geometry.prim.Line3d;

public class Geometry {

    private SVTStripFactory _svtStripFactory;

    public SVTStripFactory getStripFactory() {
        return _svtStripFactory;
    }

    public void setSvtStripFactory(SVTStripFactory _svtStripFactory) {
        this._svtStripFactory = _svtStripFactory;
    }
    
    public Geometry() {
        //AlignmentFactory.VERBOSE=true;
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

    //for making BST outline fig (shift local origin to physical origin instead of active area):

    /* * * * * * * *
    Vector3d[] corners = this._svtStripFactory.getLayerCorners(layer-1, sector-1);
    returns array of 4 corners in order [origin, max width, max width and max length, max length]
    defined as:
      corners[0] = new Vector3d( 0,                         0, 0                        );
		  corners[1] = new Vector3d( SVTConstants.ACTIVESENWID, 0, 0                        );
		  corners[2] = new Vector3d( SVTConstants.ACTIVESENWID, 0, SVTConstants.STRIPLENMAX );
      corners[3] = new Vector3d( 0,                         0, SVTConstants.STRIPLENMAX );
    * * * * * * * */
    // Geometry implementation using the geometry package:  Charles Platt
    public Point3D getPlaneModuleOrigin(int sector, int layer) {
        
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);

        Line3d localLine = new Line3d(new Vector3d(-1-SVTConstants.ACTIVESENWID/2, 0, 0                        ),
                                      new Vector3d(-1-SVTConstants.ACTIVESENWID/2, 0, SVTConstants.ACTIVESENLEN) );
        Line3d labFrameLine = localLine.transformed(SVTConstants.getLabFrame( rm[0],
                                                                              sector-1,
                                                                              SVTConstants.LAYERRADIUS[ rm[0] ][ rm[1]],
                                                                              SVTConstants.Z0ACTIVE[    rm[0] ] ));
        // apply the shifts to both ends of labFrameLine
 	double scaleT = 1.0, scaleR = -1.0;// scale factors used for visualization. Not relevant here.
        //System.out.println(" GET MODULE O "+labFrameLine.origin().toString());
        //System.out.println(" GET MODULE 1 "+SVTConstants.getDataAlignmentSectorShift()[SVTConstants.convertRegionSector2Index( rm[0],sector-1)].toString());
        //System.out.println(" GET MODULE 2 "+new Point3D(SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1).x, SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1).y, SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1).z).toString());
        if(SVTConstants.getDataAlignmentSectorShift()[SVTConstants.convertRegionSector2Index( rm[0],sector-1)]==null)
            System.out.println(" SHIFT ARRAY NULL FOR "+rm[0]+" sect "+(sector-1));
 	AlignmentFactory.applyShift(labFrameLine.origin(),SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );
        AlignmentFactory.applyShift(labFrameLine.end(),   SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );
 
 	// return only the origin of the line.
 
 	return new Point3D(labFrameLine.origin().x, labFrameLine.origin().y, labFrameLine.origin().z);

      /* * * * * * * *
      // returns NPE:
        Vector3d[] corners = this._svtStripFactory.getLayerCorners(layer-1, sector-1);

        return new Point3D(corners[0].x, corners[0].y, corners[0].z);
      * * * * * * * */
    }

    public Point3D getPlaneModuleEnd(int sector, int layer) {
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);

        Line3d localLine = new Line3d(new Vector3d( 1+SVTConstants.ACTIVESENWID/2, 0, 0                        ),
                                      new Vector3d( 1+SVTConstants.ACTIVESENWID/2, 0, SVTConstants.ACTIVESENLEN) );
        Line3d labFrameLine = localLine.transformed(SVTConstants.getLabFrame( rm[0],
                                                                              sector-1,
                                                                              SVTConstants.LAYERRADIUS[ rm[0] ][ rm[1]],
                                                                              SVTConstants.Z0ACTIVE[    rm[0] ] ));
        // apply the shifts to both ends of labFrameLine
 	double scaleT = 1.0, scaleR = -1.0;// scale factors used for visualization. Not relevant here.
 	AlignmentFactory.applyShift(labFrameLine.origin(),SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );
        AlignmentFactory.applyShift(labFrameLine.end(),   SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );
 
 	// return only the origin of this line.
        return new Point3D(labFrameLine.origin().x, labFrameLine.origin().y, labFrameLine.origin().z);
    }

    //*** 
    public int findSectorFromAngle(int layer, Point3D trkPoint) {
        if(trkPoint==null)
            return 0;
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);
        int Sect = -1;
        for (int s = 0; s < SVTConstants.NSECTORS[rm[0]] - 1; s++) {
            int sector = s + 1;
            Vector3D orig = new Vector3D(getPlaneModuleOrigin(sector, layer).x(),
                                         getPlaneModuleOrigin(sector, layer).y(), 0);
            Vector3D end  = new Vector3D(getPlaneModuleEnd(   sector, layer).x(),
                                         getPlaneModuleEnd(   sector, layer).y(), 0);
            Vector3D trk  = new Vector3D(                           trkPoint.x(),
                                                                    trkPoint.y(), 0);
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

        int region        = (int) (layer + 1) / 2; // min. value 1: (1+1)/2 -> 1; max. value 8: (8+1)/2 -> 4.5 -> 5?
        int myLayerIndex  = 2*(region - 1);        // always even
        int mySectorIndex = sector - 1;

        Line3d l1 = this._svtStripFactory.getStrip(myLayerIndex, mySectorIndex, 0);
        Line3d l2 = this._svtStripFactory.getStrip(myLayerIndex, mySectorIndex, 255);

        // subtracts origin of first strip from end of first strip; substracts origin of last strip from end of last strip;
        Vector3d u1 = l1.end().sub(l1.origin());
        Vector3d u2 = l2.end().sub(l2.origin());
        
        Vector3d n = u1.cross(u2).normalized();
        return new Vector3D(n.x, n.y, 0);
    }

    //***
    public double[] getLocCoord(double s1, double s2) { //2 top, 1 bottom

        double[] X = new double[2];
        double ialpha1 = (s1 - 1)   * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
        double ialpha2 = (s2 - 1)   * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = SVTConstants.ACTIVESENWID - interc1;
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

        if (s1 <= 1) {
            Xerr[1] = (getLocCoord(s1, s2 - 0.5)[1]
                    - getLocCoord(s1 + 1.5, s2 + 0.5)[1]);
        }
        if (s2 <= 1) {
            Xerr[1] = (getLocCoord(s1 - 0.5, s2)[1]
                    -  getLocCoord(s1 + 1.5, s2 + 2.5)[1]);
        }

        return Xerr;

    }

    public Point3D transformToFrame(int sector, int layer, double x, double y, double z, String frame, String MiddlePlane) {

        int[] rm  = SVTConstants.convertLayer2RegionModule(layer-1);

        double gap = 0;

        if (MiddlePlane.equals("middle")) {
            if ((layer - 1) % 2 == 0) { // for a cross take the bottom layer;
                int[] rm1 = SVTConstants.convertLayer2RegionModule(layer);
                int[] rm2 = SVTConstants.convertLayer2RegionModule(layer-1);
                //gap = SVTConstants.LAYERRADIUS[rm1[0]][rm1[1]]
                //    - SVTConstants.LAYERRADIUS[rm2[0]][rm2[1]]; //gap+=0.004;
            }
        }
        Point3D transf = null;
        if (frame.equals("lab")) {
            Line3d localLine = new Line3d(new Vector3d(x-SVTConstants.ACTIVESENWID/2, y, z),
                                          new Vector3d(x-SVTConstants.ACTIVESENWID/2, y, z)) ;
            Line3d labFrameLine = localLine.transformed(SVTConstants.getLabFrame( (int)((layer+1)/2) -1,
                                                                                  sector-1,
                                                                                  //= SVTConstants.LAYERRADIUS[rm[0]][rm[1]]
                                                                                  SVTConstants.LAYERRADIUS[rm[0]][rm[1]]+gap/2,
                                                                                  SVTConstants.Z0ACTIVE[rm[0]] ));
              
              
            //gpg apply the shifts to both ends of labFrameLine.
 	    double scaleT = 1.0, scaleR = -1.0;// scale factors used for visualization. Not relevant here.
 	    AlignmentFactory.applyShift(labFrameLine.origin(),SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );
 	    AlignmentFactory.applyShift(labFrameLine.end(),   SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1),scaleT,scaleR );

            transf= new Point3D(labFrameLine.origin().x,
                                labFrameLine.origin().y,
                                labFrameLine.origin().z);
        }
        if (frame.equals("local")) {
            Line3d glLine = new Line3d(new Vector3d(x,y,z),
                                       new Vector3d(x,y,z)) ;
            boolean flip = true;
            
            //gpg need to apply the reverse survey shifts here.
 	    double scaleT = 1.0, scaleR = -1.0;// scale factors used for visualization. Not relevant here.
 	    AlignmentFactory.applyInverseShift( glLine.origin(), SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1 ), scaleT, scaleR );
 	    AlignmentFactory.applyInverseShift( glLine.end(), SVTConstants.getDataAlignmentSectorShift()[SVTConstants.RSI[rm[0]][sector-1]], SVTAlignmentFactory.getIdealFiducialCenter( rm[0], sector-1 ), scaleT, scaleR );
  
            Line3d localLine = glLine.transformed(SVTConstants.getLabFrame( (int)((layer+1)/2) -1,
                                                                            sector-1,
                                                                            SVTConstants.LAYERRADIUS[rm[0]][rm[1]]+gap/2, 
                                                                            SVTConstants.Z0ACTIVE[rm[0]] ).invert());
            transf= new Point3D(localLine.origin().x+SVTConstants.ACTIVESENWID/2,
                                localLine.origin().y,
                                localLine.origin().z);
        }
        return transf;
    }
    //*** point and its error

    public double[] getCrossPars(int sector, int upperlayer, double s1, double s2, String frame, Vector3D trkDir) {
        double[] vals = new double[6];

        // if first iteration trkDir == null
        double s2corr = s2;
        // now use track info
        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, SVTConstants.MODULELEN);
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

        vals[0] = crPoint.x();
        vals[1] = crPoint.y();
        vals[2] = crPoint.z();
        double[] LCErr = getLocCoordErrs(upperlayer - 1, upperlayer, s1, s2corr, zf);
        double LCErr_x = LCErr[0];
        double LCErr_z = LCErr[1];

        // global rotation angle to get the error in the lab frame
        int layerIdx = upperlayer - 1;
        int[] rm = SVTConstants.convertLayer2RegionModule(upperlayer-1);

        // global rotation angle
        double Glob_rangl = ((double) -(sector - 1) / (double) SVTConstants.NSECTORS[rm[0]])
                                              * 2. * Math.PI + SVTConstants.PHI0;
        // angle to rotate to global frame
        double Loc_to_Glob_rangl = Glob_rangl - org.jlab.rec.cvt.svt.Constants.LOCZAXISROTATION;

        double cosRotation = Math.cos(Loc_to_Glob_rangl);
        double sinRotation = Math.sin(Loc_to_Glob_rangl);

        double yerr = Math.abs(cosRotation * LCErr_x);
        double xerr = Math.abs(sinRotation * LCErr_x);

        vals[3] = xerr;
        vals[4] = yerr;
        vals[5] = LCErr_z;

        if (LC_z > SVTConstants.MODULELEN + org.jlab.rec.cvt.svt.Constants.TOLTOMODULELEN * 2) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }

        // once there is a trk, the cross should be well calculated
        //if the local cross is not in the fiducial volume it is not physical
        if ((trkDir != null && (LC_x < 0 || LC_x > SVTConstants.ACTIVESENWID + org.jlab.rec.cvt.svt.Constants.TOLTOMODULEEDGE))
         || (trkDir != null && (LC_z < -org.jlab.rec.cvt.svt.Constants.TOLTOMODULELEN
                                         || LC_z > SVTConstants.MODULELEN   + org.jlab.rec.cvt.svt.Constants.TOLTOMODULELEN))) {
            return new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        }

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

        double alpha = SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);

        double b = SVTConstants.ACTIVESENWID;
        double P = SVTConstants.READOUTPITCH;

        double s = -1;

        if (layer % 2 == 1) {//layers 1,3,5 == bottom ==i ==>(1) : regular configuration
            //m1,b1
            s = (int) Math.floor((-x + b + alpha * z + 0.5 * P - SVTConstants.STRIPOFFSETWID) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = -Math.tan((sp - 1) * alpha) * z + b - sp * P + 0.5 * P - SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }
            s = newStrip;

        }
        if (layer % 2 == 0) {
            //layers 2,4,6 == top ==j ==>(2) : regular configuration
            //m2,b2		
            s = (int) Math.floor((x + alpha * z + 0.5 * P - SVTConstants.STRIPOFFSETWID) / (alpha * z + P));

            double delta = 99999;
            double sdelta = delta;
            double newStrip = s;
            for (int i = -1; i < 2; i++) {
                double sp = s + (double) i;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }

            s = newStrip;
            for (int i = -10; i <= 10; i++) {
                double sp = s + (double) i * 0.1;
                double x_calc = Math.tan((sp - 1) * alpha) * z + sp * P - 0.5 * P + SVTConstants.STRIPOFFSETWID;

                if (Math.abs(x - x_calc) < delta) {
                    sdelta = x - x_calc;
                    delta = Math.abs(sdelta);
                    newStrip = sp;
                }
            }
            s = newStrip;
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
        if (strip == SVTConstants.NSTRIPS) {
            StripUp = (double) SVTConstants.NSTRIPS; //edge strip
        }
        double StripDown = Strip - 1;
        if (strip == 1) {
            StripDown = 1; //edge strip
        }

        double pitchToNextStrp = Math.abs(getXAtZ(lay, (double) StripUp,   Z) - getXAtZ(lay, (double) Strip, Z)); // this is P- in the formula below
        double pitchToPrevStrp = Math.abs(getXAtZ(lay, (double) StripDown, Z) - getXAtZ(lay, (double) Strip, Z)); // this is P+ in the formula 

        // For a given strip (for which we estimate the resolution),
        // P+ is the pitch to the strip above (at z position) and P- to that below in the local coordinate system of the module.
        // The current design of the BST is simulated in gemc such that each strip provides hit-no-hit information,
        // and the single strip resolution is therefore given by the variance,
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
        double ialpha = (centroidstrip - 1) * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = SVTConstants.ACTIVESENWID - interc;
        double b2 = interc;

        Vector3D vecAlongStrip = new Vector3D();
        Point3D   pointOnStrip = new Point3D();
        Point3D       LocPoint = this.transformToFrame(sector, layer, point0.x(), point0.y(), point0.z(), "local", "");

        if (layer % 2 == 0) { //layers 2,4,6 == top ==j ==>(2) : regular configuration
            vecAlongStrip = new Vector3D(m2, 0, 1).asUnit();
            pointOnStrip  = new Point3D(b2,  0, 0);
        }
        if (layer % 2 == 1) { //layers 1,3,5 == bottom ==i ==>(1) : regular configuration
            vecAlongStrip = new Vector3D(m1, 0, 1).asUnit();
            pointOnStrip  = new Point3D(b1,  0, 0);
        }

        Vector3D r = LocPoint.vectorTo(pointOnStrip); //
        Vector3D d = r.cross(vecAlongStrip);
        
        Line3D l = new Line3D(pointOnStrip,
                              pointOnStrip.toVector3D().add(vecAlongStrip.multiply(10)));
        //fix for hemisphere
        return d.y()*Math.signum(this.findBSTPlaneNormal(sector, layer).y());

    }
    //****
    // in the local coordinate system 

    public double getXAtZ(int layer, double centroidstrip, double Z) {
        double X = 0;
        // local angle of  line graded from 0 to 3 deg.
        double ialpha = (centroidstrip - 1)   * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc = (centroidstrip - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        double m1 = -Math.tan(ialpha);
        double m2 = Math.tan(ialpha);
        double b1 = SVTConstants.ACTIVESENWID - interc;
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
        Vector3D trkDir_t = new Vector3D(tx / Math.sqrt(tx * tx + ty * ty),
                                         ty / Math.sqrt(tx * tx + ty * ty),
                                         0                                 );
        Vector3D n = findBSTPlaneNormal(sector, layer);

        if (org.jlab.rec.cvt.Constants.isCosmicsData() && Math.acos(n.dot(trkDir_t)) > Math.PI / 2) { // flip track dir for y<0 for cosmics:
            trkDir_t = new Vector3D(-trkDir_t.x(),
                                    -trkDir_t.y(),
                                     0            );
        }

        double TrkToPlnNormRelatAngl = Math.acos(n.dot(trkDir_t));
        double sign = Math.signum(n.cross(trkDir_t).z());

        //correction to the pitch to take into account the grading of the angle -- get the upper or lower strip depending on the trkdir
        double pitchcorr = SVTConstants.READOUTPITCH;

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

        double layerGap = SVTConstants.LAYERRADIUS[0][1]  // MODULERADIUS[1][0] = 65.447 + LAYRGAP + MODULEPOSFAC * SILICONTHICK = SVTConstants.LAYERRADIUS[0][1]
                        - SVTConstants.LAYERRADIUS[0][0]; // MODULERADIUS[0][0] = 65.447 - MODULEPOSFAC * SILICONTHICK           = SVTConstants.LAYERRADIUS[0][0]

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
        
        int[] rm  = SVTConstants.convertLayer2RegionModule(layer-1);

        int nstep = 1;
        double stepSize = 0.001;

        double Theta = Math.atan2((SVTConstants.ACTIVESENWID / 2),
                                   SVTConstants.LAYERRADIUS[rm[0]][rm[1]]); // = 

        double RMin = SVTConstants.LAYERRADIUS[rm[0]][rm[1]];
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

    public Point3D recalcCrossFromTrajectoryIntersWithModulePlanes(int s, double s1, double s2, int l1, int l2,
                                                                   double trajX1, double trajY1, double trajZ1,
                                                                   double trajX2, double trajY2, double trajZ2) {
        Point3D LocPoint1 = this.transformToFrame(s, l1, trajX1, trajY1, trajZ1, "local", "");
        Point3D LocPoint2 = this.transformToFrame(s, l2, trajX2, trajY2, trajZ2, "local", "");
        double m = (LocPoint1.x() - LocPoint2.x()) /
                   (LocPoint1.z() - LocPoint2.z());
        double b = LocPoint1.x() - m * LocPoint1.z();

        double ialpha1 = (s1 - 1) * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc1 = (s1 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
        double ialpha2 = (s2 - 1) * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
        //the active area starts at the first strip 	
        double interc2 = (s2 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;

        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
        // -------------------------------------
        double m1 = -Math.tan(ialpha1);
        double m2 = Math.tan(ialpha2);
        double b1 = SVTConstants.ACTIVESENWID - interc1;
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

        int[] rm1  = SVTConstants.convertLayer2RegionModule(l1-1);
        int[] rm2  = SVTConstants.convertLayer2RegionModule(l2-1);

        //path length tranversed inbetween modules
        double l = (SVTConstants.LAYERRADIUS[rm1[0]][rm1[1]] - SVTConstants.LAYERRADIUS[rm2[0]][rm2[1]]) / (n.dot(t));
        //Point inbetween the modules			

        Point3D Point = new Point3D(Point1.x() + t.x() * ((double) l / 2),
                                    Point1.y() + t.y() * ((double) l / 2),
                                    Point1.z() + t.z() * ((double) l / 2) );

        return Point;
    }

    public Line3d getStrip(int alayer, int asector, int astrip) {

      Line3d shiftedStrip = this._svtStripFactory.getStrip(alayer, asector, astrip); // shifted if bshift = true

      return shiftedStrip;
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
            double ialpha1 = (s1 - 1) * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
            //the active area starts at the first strip 	
            double interc1 = (s1 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
            double m1 = -Math.tan(ialpha1);
            double b1 = SVTConstants.ACTIVESENWID - interc1;

            z1 = 0;
            x1 = m1 * z1 + b1;
            z2 = SVTConstants.MODULELEN;
            x2 = m1 * z2 + b1;

            if (x2 < 0) {
                x2 = 0;
                z2 = -b1 / m1;
            }
        }

        if (slyr == 1) {
            double s2 = strip;
            double ialpha2 = (s2 - 1) * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
            //the active area starts at the first strip 	
            double interc2 = (s2 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
            double m2 = Math.tan(ialpha2);
            double b2 = interc2;

            z1 = 0;
            x1 = m2 * z1 + b2;
            z2 = SVTConstants.MODULELEN;
            x2 = m2 * z2 + b2;

            if (x2 > SVTConstants.ACTIVESENWID) {
                x2 = SVTConstants.ACTIVESENWID;
                z2 = (x2 - b2) / m2;
            }
        }
        X[0][0] = x1;
        X[0][1] = z1;
        X[1][0] = x2;
        X[1][1] = z2;

/*        
        System.out.println("createIdealStrip: " + this._svtStripFactory.createIdealStrip(strip, slyr));
        System.out.println("X: " + strip + " + " + slyr );
        System.out.println(" x1: " + X[0][0] );
        System.out.println(" z1: " + X[0][1] );
        System.out.println(" x2: " + X[1][0] );
        System.out.println(" z2: " + X[1][1] );
*/
        return X;
    }

    public static void main(String arg[]) throws FileNotFoundException {
    }
}
