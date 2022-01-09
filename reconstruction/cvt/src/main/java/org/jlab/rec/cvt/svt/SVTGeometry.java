package org.jlab.rec.cvt.svt;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.objects.Strip;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.rec.cvt.Constants;

public class SVTGeometry {

    private SVTStripFactory _svtStripFactory;
    public static final int NREGIONS = 3;
    public static final int NLAYERS  = 6;
    public static final int NSTRIPS  = SVTConstants.NSTRIPS;
    public static final int[] NSECTORS = new int[NLAYERS];
    
    
    
    public SVTGeometry(SVTStripFactory factory) {
        this._svtStripFactory = factory;
        this.init();
    }
  
    private void init() {
        for(int ilayer=0; ilayer<NLAYERS; ilayer++) {
            int iregion = ilayer/2;
            NSECTORS[ilayer] = SVTConstants.NSECTORS[iregion];
        }
    }
    

    public static double getLayerRadius(int layer) {
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);
        return SVTConstants.LAYERRADIUS[rm[0]][rm[1]];
    }
    
    public static double getRegionRadius(int region) {
        return (SVTConstants.LAYERRADIUS[region-1][0]+SVTConstants.LAYERRADIUS[region-1][1])/2;
    }
    
    public static double getLayerGap() {
        return (SVTConstants.LAYERRADIUS[0][1]-SVTConstants.LAYERRADIUS[0][0]);
    }
    
    public static double getLayerZ0(int layer) {
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);
        return SVTConstants.Z0ACTIVE[rm[0]];
    }
    
    public static double getSectorPhi(int layer, int sector) {
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);
        return SVTConstants.getPhi(rm[0], sector-1);
    }
    
    public static double getPitch() {
        return SVTConstants.READOUTPITCH;
    }
    
    public static double getActiveSensorWidth() {
        return SVTConstants.ACTIVESENWID;
    }
    
    public static double getActiveSensorLength() {
        return SVTConstants.ACTIVESENLEN;
    }
    
    public static double getModuleLength() {
        return SVTConstants.MODULELEN;
    }
    
    public static double getToverX0() {
        return 2*SVTConstants.SILICONTHK/SVTConstants.SILICONRADLEN;
    }
    
    public static double getMaterialThickness() {
        return SVTConstants.SILICONTHK;
    }
    
    public static double getZoverA() {
        return SVTConstants.ZOVERA;
    }
    public Line3D getStrip(int layer, int sector, int strip) {
        Line3d line = this._svtStripFactory.getShiftedStrip(layer-1, sector-1, strip-1);
        return new Line3D(line.origin().x,line.origin().y,line.origin().z,
                          line.end().x,   line.end().y,   line.end().z);
    }
    
    public Line3D getModule(int layer, int sector) {
        Line3d line = this._svtStripFactory.getModuleEndPoints(layer-1, sector-1);
        return new Line3D(line.origin().x,line.origin().y,line.origin().z,
                          line.end().x,   line.end().y,   line.end().z);        
    }
    
    public static int getModuleId(int layer, int sector) {
        return layer*100+sector;
    }
    
    public Vector3D getNormal(int layer, int sector) {
        Vector3d normal = this._svtStripFactory.getModuleNormal(layer-1, sector-1);
        return new Vector3D(normal.x, normal.y, normal.z);
    }
    
    public Plane3D getPlane(int layer, int sector) {
        return new Plane3D(this.getModule(layer, sector).midpoint(), this.getNormal(layer, sector));
    }
    
    public Line3D getStripProjection(Line3D strip, Plane3D plane, Vector3D dir) {
        Point3D stripO = new Point3D();
        plane.intersection(new Line3D(strip.origin(),dir), stripO);
        Point3D stripE = new Point3D();
        plane.intersection(new Line3D(strip.end(),dir), stripE);
        
        return new Line3D(stripO, stripE);
    }
    
    public Point3D getCross(int sector, int layer, Line3D strip1, Line3D strip2, Vector3D dir) {
        Point3D cross = null;
        
        // if direction is not provided, use normal to the plane
        Vector3D n = this.getNormal(layer, sector);
        if(dir != null) {
            n = new Vector3D(dir);
            n.unit();
        }
        
        Line3D strip2Projection = this.getStripProjection(strip2, this.getPlane(layer, sector), n);
        if(strip2Projection!=null) {
            cross = strip1.distance(strip2Projection).origin();
            if(dir!=null && !this.isInFiducial(layer, sector, cross)) cross = null;
        }
        return cross;
    }
    
    public Point3D toLocal(int layer, int sector, Point3D traj) {
        Vector3d local = this._svtStripFactory.transformToLocal(layer-1, sector-1, traj.x(), traj.y(), traj.z());
        return new Point3D(local.x, local.y, local.z);
    }
    
    public Point3D toGlobal(int layer, int sector, Point3D traj) {
        Vector3d lab = this._svtStripFactory.transformToLab(layer-1, sector-1, traj.x(), traj.y(), traj.z());
        return new Point3D(lab.x, lab.y, lab.z);
    }
    
    public Vector3D toLocal(int layer, int sector, Vector3D dir) {
        Vector3d local = this._svtStripFactory.transformToLocal(layer-1, sector-1, dir.x(), dir.y(), dir.z());
        Vector3d zero  = this._svtStripFactory.transformToLocal(layer-1, sector-1, 0, 0, 0);
        return new Vector3D(local.x-zero.x, local.y-zero.y, local.z-zero.z);
    }
    
    public Vector3D toGlobal(int layer, int sector, Vector3D dir) {
        Vector3d lab  = this._svtStripFactory.transformToLab(layer-1, sector-1, dir.x(), dir.y(), dir.z());
        Vector3d zero = this._svtStripFactory.transformToLab(layer-1, sector-1, 0, 0, 0);
        return new Vector3D(lab.x-zero.x, lab.y-zero.y, lab.z-zero.z);
    }
    
    public double getDoca(int layer, int sector, int strip, Point3D traj) {
        return this.getStrip(layer, sector, strip).distance(traj).length();
    }
    
    public double getResidual(int layer, int sector, int strip, Point3D traj) {
        Line3D dist = this.getStrip(layer, sector, strip).distance(traj);
        double side = -Math.signum(this.getStrip(layer, sector, strip).direction().cross(dist.direction()).dot(this.getNormal(layer, sector)));            
        return dist.length()*side;
    }
    
    /**
     * Return track vector for local angle calculations
     * 
     * 1) transform to the geometry service local frame first:
     * y axis pointing toward the center, 
     * z axis pointing downstream along the module
     * x axis given by y.cross(z)
     * 
     * 2) for even layers, it rotates by 180 deg to have the y axis
     * pointing outward
     * 
     * 3) rotates by 90 deg around X to have:
     * z axis normal to the module pointing inward for odd layers and outward for even ones
     * y axis pointing upstream along the module
     * x axis given by y.cross(z) along the short side of the module
     *
     * @param layer
     * @param sector
     * @param trackDir
     * @return 
    **/
    public Vector3D getLocalTrack(int layer, int sector, Vector3D trackDir) {
        Vector3D dir = this.toLocal(layer, sector, trackDir);
        if(layer%2==0) dir.rotateZ(Math.PI); // to get the Y axis to point
        dir.rotateX(Math.PI/2); 
        return dir;
    }

    /**
     * Returns angle of the track with respect to the normal to the module
     * in the x-z plane defined above
     * 
     * @param layer
     * @param sector
     * @param trackDir
     * @return
     */
    public double getLocalAngle(int layer, int sector, Vector3D trackDir) {
        Vector3D dir = this.getLocalTrack(layer, sector, trackDir);
        return Math.atan(dir.x()/dir.z());
    }
    
    public List<Surface> getSurfaces() {
        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(this.getShieldSurface());
        surfaces.add(this.getFaradayCageSurfaces(0));
        for(int i=1; i<=NLAYERS; i++) {
            surfaces.add(this.getSurface(i, 1));
        }
        surfaces.add(this.getFaradayCageSurfaces(1));
        return surfaces;
    }
        
    public Surface getSurface(int layer, int sector, int stripId, double centroid, Line3D stripLine, int hemisphere) {
        Surface surface = this.getSurface(layer, sector);
        surface.hemisphere = hemisphere;
        surface.strip = new Strip(stripId, centroid, stripLine);
        return surface;
    }

    public Surface getSurface(int layer, int sector) {
        Line3D module = this.getModule(layer, sector);
        Surface surface = new Surface(this.getPlane(layer, sector), 
                                      new Strip(0,0,0), 
                                      module.origin(), module.end(), 
                                      Constants.SWIMACCURACYSVT);
        surface.hemisphere = 0;
        surface.setLayer(layer);
        surface.setSector(sector);
        surface.setError(0); 
        if(layer%2==0) {
            surface.setl_over_X0(SVTGeometry.getToverX0());
            surface.setZ_over_A_times_l(SVTGeometry.getZoverA());
            surface.setThickness(SVTGeometry.getMaterialThickness());
        }
        surface.notUsedInFit=true;
        return surface;
    }
    
    private Surface getShieldSurface() {
        Point3D  center = new Point3D(0, 0, Constants.getZoffset()+SVTConstants.TSHIELDZPOS-SVTConstants.TSHIELDLENGTH/2);
        Point3D  origin = new Point3D(SVTConstants.TSHIELDRMAX, 0, SVTConstants.TSHIELDZPOS-SVTConstants.TSHIELDLENGTH/2);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D shieldCylinder = new Cylindrical3D(base, SVTConstants.TSHIELDLENGTH);
        Surface shieldSurface = new Surface(shieldCylinder, new Strip(0, 0, 0), Constants.DEFAULTSWIMACC);
        shieldSurface.setl_over_X0(SVTConstants.TSHIELDRADLEN);
        shieldSurface.setZ_over_A_times_l(SVTConstants.TSHILEDZOVERA);
        shieldSurface.setThickness(SVTConstants.TSHIELDRMAX-SVTConstants.TSHIELDRMIN);
        shieldSurface.notUsedInFit=true;
        return shieldSurface;
    }

    private Surface getFaradayCageSurfaces(int i) {
        Point3D  center = new Point3D(0, 0, SVTConstants.FARADAYCAGEZPOS[i]-SVTConstants.FARADAYCAGELENGTH[i]/2);
        Point3D  origin = new Point3D(SVTConstants.FARADAYCAGERMAX[i], 0, SVTConstants.FARADAYCAGEZPOS[i]-SVTConstants.FARADAYCAGELENGTH[i]/2);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D fcCylinder = new Cylindrical3D(base, SVTConstants.FARADAYCAGELENGTH[i]);
        Surface fcSurface = new Surface(fcCylinder, new Strip(0, 0, 0), Constants.DEFAULTSWIMACC);
        fcSurface.setl_over_X0(SVTConstants.FARADAYCAGERADLEN[i]);
        fcSurface.setZ_over_A_times_l(SVTConstants.FARADAYCAGEZOVERA[i]);
        fcSurface.setThickness(SVTConstants.FARADAYCAGERMAX[i]-SVTConstants.FARADAYCAGERMIN[i]);
        fcSurface.notUsedInFit=true;
        return fcSurface;
    }
    
    @Deprecated
    public int getSector(int layer, Point3D traj) {
        
        int[] rm = SVTConstants.convertLayer2RegionModule(layer-1);
        int Sect = -1;
        for (int s = 0; s < SVTConstants.NSECTORS[rm[0]]; s++) {
            int sector = s + 1;
            Line3D module = this.getModule(layer, sector);
            Vector3D orig = module.origin().toVector3D().asUnit();
            Vector3D end  = module.end().toVector3D().asUnit();
            Vector3D trk  = new Vector3D(traj.x(), traj.y(), 0);
            orig.unit();
            end.unit();
            trk.unit();

            double phi1 = Math.acos(orig.dot(trk));
            double phi2 = Math.acos(trk.dot(end));
            double phiRange = Math.acos(orig.dot(end));

            if (phi1<phiRange && phi2<phiRange) {
                Sect = sector;
            }
        }
        return Sect;
    }

//    //***
//    public double[] getLocCoord(double s1, double s2) { //2 top, 1 bottom
//
//        double[] X = new double[2];
//        double ialpha1 = (s1 - 1)   * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
//        //the active area starts at the first strip 	
//        double interc1 = (s1 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
//        double ialpha2 = (s2 - 1)   * SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);
//        //the active area starts at the first strip 	
//        double interc2 = (s2 - 0.5) * SVTConstants.READOUTPITCH + SVTConstants.STRIPOFFSETWID;
//
//        // Equation for strip line is x = mz + b [i.e. z is the direction of the length of the module]
//        // -------------------------------------
//        double m1 = -Math.tan(ialpha1);
//        double m2 = Math.tan(ialpha2);
//        double b1 = SVTConstants.ACTIVESENWID - interc1;
//        double b2 = interc2;
//
//        double z = (b2 - b1) / (m1 - m2);
//        double x = m1 * z + b1;
//        X[0] = x;
//        X[1] = z;
//
//        return X;
//    }
//
//    public double[] getLocCoordErrs(int lay1, int lay2, double s1, double s2, double z) {
//        double[] Xerr = new double[2];
//
//        double sigma1 = getSingleStripResolution(lay1, (int) s1, z);
//        double sigma2 = getSingleStripResolution(lay2, (int) s2, z);
//
//        Xerr[0] = Math.sqrt(sigma1 * sigma1 + sigma2 * sigma2);
//
//        Xerr[1] = (getLocCoord(s1 - 0.5, s2 - 0.5)[1]
//                - getLocCoord(s1 + 0.5, s2 + 0.5)[1]);
//
//        if (s1 <= 1) {
//            Xerr[1] = (getLocCoord(s1, s2 - 0.5)[1]
//                    - getLocCoord(s1 + 1.5, s2 + 0.5)[1]);
//        }
//        if (s2 <= 1) {
//            Xerr[1] = (getLocCoord(s1 - 0.5, s2)[1]
//                    -  getLocCoord(s1 + 1.5, s2 + 2.5)[1]);
//        }
//
//        return Xerr;
//
//    }
     
    
//    public double[] getCrossPars(int sector, int upperlayer, double s1, double s2, Vector3D trkDir) {
//        
//        double[] vals = new double[]{Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
//
//        // if first iteration trkDir == null
//        double s2corr = s2;
//        // now use track info
//        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, SVTConstants.MODULELEN);
//        double z = getLocCoord(s1, s2corr)[1];
//        //update using the corrected z
//        s2corr = this.getCorrectedStrip(sector, upperlayer, s2, trkDir, z);
//        double zf = getLocCoord(s1, s2corr)[1];
//
//        if (upperlayer % 2 != 0) // should not happen as is upper layer...but just in case
//        {
//            s2corr = s2;
//        }
//
//        double[] LC    = getLocCoord(s1, s2corr);
//        double[] LCErr = getLocCoordErrs(upperlayer - 1, upperlayer, s1, s2corr, zf);
//        
//        Point3D crPoint = this.toGlobal(upperlayer-1, sector, new Point3D(LC[0], 0, LC[1]));
//        Vector3D  crErr = this.toGlobal(upperlayer-1, sector, new Vector3D(LCErr[0], 0, LCErr[1]));
//        
//        double LCErr_x = LCErr[0];
//        double LCErr_z = LCErr[1];
//
//        // global rotation angle to get the error in the local frame
//        int[] rm = SVTConstants.convertLayer2RegionModule(upperlayer-1);
//
//        // global rotation angle
//        double Glob_rangl = ((double) -(sector - 1) / (double) SVTConstants.NSECTORS[rm[0]])
//                                              * 2. * Math.PI + SVTConstants.PHI0;
//        // angle to rotate to global frame
//        double LOCZAXISROTATION = -Math.toRadians(90.);
//        double Loc_to_Glob_rangl = Glob_rangl - LOCZAXISROTATION;
//
//        double cosRotation = Math.cos(Loc_to_Glob_rangl);
//        double sinRotation = Math.sin(Loc_to_Glob_rangl);
//
//        double yerr = Math.abs(cosRotation * LCErr_x);
//        double xerr = Math.abs(sinRotation * LCErr_x);
//
//        // RDV: check because old errors seems to be rotated by 90 deg
////        System.out.println("Cross:\n" + xerr + " " + yerr + " " + LCErr_z);
////        System.out.println(crErr.toString());
////        System.out.println(crPoint.toString());
//        
//        if (LC[1] < -SVTConstants.LENGTHTOL || LC[1] > SVTConstants.MODULELEN + SVTConstants.LENGTHTOL * 2) {
//            return vals;
//        }
//        // once there is a trk, the cross should be well calculated
//        //if the local cross is not in the fiducial volume it is not physical
//        else if ((trkDir != null && (LC[0] < -SVTConstants.SIDETOL   || LC[0] > SVTConstants.ACTIVESENWID + SVTConstants.SIDETOL))
//              || (trkDir != null && (LC[1] < -SVTConstants.LENGTHTOL || LC[1] > SVTConstants.MODULELEN    + SVTConstants.LENGTHTOL))) {
//            return vals;
//        }
//        else {
//            vals[0] = crPoint.x();
//            vals[1] = crPoint.y();
//            vals[2] = crPoint.z();
//            vals[3] = Math.abs(crErr.x());
//            vals[4] = Math.abs(crErr.y());
//            vals[5] = Math.abs(crErr.z());
//        }
//
//        return vals;
//
//    }
    
//    private double getCorrectedStrip(int sector, int upperlayer, double s2, Vector3D trkDir, double ZalongModule) {
//        double s2corr = s2;
//        // second iteration: there is a track direction
//        if (trkDir != null) {
//            double stripCorr = getStripIndexShift(sector, upperlayer, trkDir, s2, ZalongModule);
//            if (s2 > 1) {
//                s2corr = s2 + stripCorr;
//            }
//            if (s2 == 1) {
//                if (stripCorr >= 0) {
//                    s2corr = s2 + stripCorr;
//                }
//                if (stripCorr < 0) {
//                    s2corr = s2;
//                }
//            }
//        }
//        return s2corr;
//    }

    public int calcNearestStrip(double X, double Y, double Z, int layer, int sect) {

        Point3D LocPoint = this.toLocal(layer, sect, new Point3D(X,Y,Z));

        double x = LocPoint.x();
        double z = LocPoint.z();

        double alpha = SVTConstants.STEREOANGLE / (double) (SVTConstants.NSTRIPS - 1);

        double b = SVTConstants.ACTIVESENWID;
        double P = SVTConstants.READOUTPITCH;

        double s = -1000;

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
        //allow 2 strips off
        if (s <= 1 && s > -2) {
            s = 1;
        }
        if (s >= 256 && s < 259) {
            s = 256;
        }

        //System.out.println(" layer "+layer+" sector "+sect+" strip "+s);
        return (int) s;
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
//    public double getStripIndexShift(int sector, int layer, Vector3D trkDir, double s2, double z) {
//
//        double tx = trkDir.x();
//        double ty = trkDir.y();
//        Vector3D trkDir_t = new Vector3D(tx,ty,0);
//        trkDir_t.unit();
//        Vector3D n = this.getNormal(layer, sector);
//        
//        if (Math.acos(n.dot(trkDir_t)) > Math.PI / 2) { // flip track dir for y<0 for cosmics:
//            trkDir_t.negative();
//        }
//
//        double TrkToPlnNormRelatAngl = Math.acos(n.dot(trkDir_t));
//        double sign = Math.signum(n.cross(trkDir_t).z());
//
//        //correction to the pitch to take into account the grading of the angle -- get the upper or lower strip depending on the trkdir
//        double pitchcorr = SVTConstants.READOUTPITCH;
//
//        if (s2 > 2 && s2 < 255) {
//            double pitchToNextStrp = Math.abs(getXAtZ(layer, (double) s2 + 1, z) - getXAtZ(layer, (double) s2, z));
//            double pitchToPrevStrp = Math.abs(getXAtZ(layer, (double) s2 - 1, z) - getXAtZ(layer, (double) s2, z));
//            pitchcorr = (pitchToNextStrp + pitchToPrevStrp) / 2.;
//        }
//        if (s2 <= 2) {
//            pitchcorr = Math.abs(getXAtZ(layer, (double) s2 + 1, z) - getXAtZ(layer, (double) s2, z));
//        }
//        if (s2 == 256) {
//            pitchcorr = Math.abs(getXAtZ(layer, (double) s2 - 1, z) - getXAtZ(layer, (double) s2, z));
//        }
//
//        double layerGap = SVTConstants.LAYERRADIUS[0][1]  // MODULERADIUS[1][0] = 65.447 + LAYRGAP + MODULEPOSFAC * SILICONTHICK = SVTConstants.LAYERRADIUS[0][1]
//                        - SVTConstants.LAYERRADIUS[0][0]; // MODULERADIUS[0][0] = 65.447 - MODULEPOSFAC * SILICONTHICK           = SVTConstants.LAYERRADIUS[0][0]
//
//        double shift = sign * layerGap * Math.tan(TrkToPlnNormRelatAngl) / pitchcorr;
//
//        return -shift;
//    }
    //***

    public boolean isInFiducial(int layer, int sector, Point3D traj) {
        
        Point3D local = this.toLocal(layer, sector, traj);
        
        if(local.x()<-SVTConstants.SIDETOL || local.x()>(SVTConstants.ACTIVESENWID+SVTConstants.SIDETOL))
            return false;
        else if(local.z()<-SVTConstants.LENGTHTOL || local.z()>(SVTConstants.MODULELEN+SVTConstants.LENGTHTOL))
            return false;
        else
            return true;
    }
}
