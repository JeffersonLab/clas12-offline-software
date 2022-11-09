package org.jlab.rec.dc.trajectory;

import Jama.*;
import org.jlab.clas.clas.math.FastMath;
/**
 * Describes a track pars in the DC.  
 * @author ziegler
 *
 */
public class TrackVec extends Matrix {

    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1874984192960130771L;


    /**
     * Instantiates a new  vec.
     */
    public TrackVec() {
        super(6,1);
    }
    /**
     * Sets the.
     *
     * @param V the v
     */
    public void set(TrackVec V) {
            set(0,0,V.x());
            set(1,0,V.y());
            set(2,0,V.z());
            set(3,0,V.px());
            set(4,0,V.py());
            set(5,0,V.pz());
    }

    
    
    private double z;

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
    
    public void set(double x, double y, double z, double px, double py, double pz) {
            set(0,0,x);
            set(1,0,y);
            set(2,0,z);
            set(3,0,px);
            set(4,0,py);
            set(5,0,pz);
    }

    
    public TrackVec(double x, double y, double z, double px, double py, double pz) {
            super(6,1);
            set(0,0,x);
            set(1,0,y);
            set(2,0,z);
            set(3,0,px);
            set(4,0,py);
            set(5,0,pz);
    }

    /**
     * Instantiates a new vec
     *
     * @param v the v
     */
    public TrackVec(TrackVec v) {
            super(6,1);
            set(0,0,v.x());
            set(1,0,v.y());
            set(2,0,v.z());
            set(3,0,v.px());
            set(4,0,v.py());
            set(5,0,v.pz());
    }

    public TrackVec(double x, double y, double z, double px, double py, double pz, int sec) {
            super(6,1);
            this._sector = sec;
            set(0,0,x);
            set(1,0,y);
            set(2,0,z);
            set(3,0,px);
            set(4,0,py);
            set(5,0,pz);
    }

    /**
     * Instantiates a new vec
     *
     * @param v the v
     * @param sec
     */
    public TrackVec(TrackVec v, int sec) {
            super(6,1);
            this._sector = sec;
            set(0,0,v.x());
            set(1,0,v.y());
            set(2,0,v.z());
            set(3,0,v.px());
            set(4,0,v.py());
            set(5,0,v.pz());
    }

    /**
     * Instantiates a new  cev.
     *
     * @param m the m
     */
    private TrackVec(Matrix m) { //needed since Jama.Matrix cannot be casted into StateVec		
            super(6,1);
            set(0,0,m.get(0, 0));
            set(1,0,m.get(1, 0));
            set(2,0,m.get(2, 0));
            set(3,0,m.get(3, 0));
            set(4,0,m.get(4, 0));
            set(5,0,m.get(5, 0));
    }


    /**
     * Description of x().
     *
     * @return the x component
     */
    public double x() {
            return(get(0,0));
    }

    /**
     * Description of y().
     *
     * @return the y component
     */ 

    public double y() {
            return(get(1,0));
    }

    /**
     * Description of z().
     *
     * @return the z component
     */ 

    public double z() {
            return(get(2,0));
    }

    /**
     * Description of x().
     *
     * @return the x component
     */
    public double px() {
            return(get(3,0));
    }

    /**
     * Description of py().
     *
     * @return the py component
     */ 

    public double py() {
            return(get(4,0));
    }

    /**
     * Description of pz().
     *
     * @return the pz component
     */ 

    public double pz() {
            return(get(5,0));
    }

    public int FrameRefId = -1; //not set: 0: lab; 1:TSC
    
    public double[] TransformToLabFrame(int sector, double x, double y, double z) {
        double[] XZt = tilt(x,z, -1);
        double[] XY = rotateToSec( sector, XZt[0], y, 1);
        return new double[] {XY[0], XY[1], XZt[1]};
    }
    public void TransformToLabFrame(int sector) {
        if(FrameRefId == 0)
            return;
        double X[] = this.TransformToLabFrame(sector, x(), y(), z());
        double P[] = this.TransformToLabFrame(sector, px(), py(), pz());
        this.set(X[0], X[1], X[2], P[0], P[1], P[2]);
        FrameRefId = 0;
    }
    
    public double[] TransformToTiltSectorFrame(int sector, double x, double y, double z) {
        double[] XY = rotateToSec( sector, x, y, -1);
        double[] XZt = tilt(XY[0],z, 1);
        return new double[] {XZt[0], XY[1], XZt[1]};
    }
    public void TransformToTiltSectorFrame() {
        if(_sector == 0) {
            _sector = this.calcSector(x(), y());
        }
        if(FrameRefId == 1 )
            return;
        double X[] = this.TransformToTiltSectorFrame(_sector, x(), y(), z());
        double P[] = this.TransformToTiltSectorFrame(_sector, px(), py(), pz());
        this.set(X[0], X[1], X[2], P[0], P[1], P[2]);
        FrameRefId = 1;
        
        
    }
    /**
     * 
     * @param X
     * @param Z
     * @param t <0 going to lab, >0 going to tilted system
     * @return 
     */
    public double[] tilt(double X, double Z, int t) {
        double rz = (double)t *X * sin_tilt + Z * cos_tilt;
        double rx = X * cos_tilt -(double)t* Z * sin_tilt;
        
        return new double[] {rx, rz};
    }
    /**
     * 
     * @param sector
     * @param x
     * @param y
     * @param t >0 going from TSC to LAB
     * @return 
     */
    public double[] rotateToSec(int sector, double x, double y, int t) {
        if(sector>0 && sector<7) {
            double rx = x * FastMath.cos((sector - 1) * t*rad60) - y * FastMath.sin((sector - 1) * t*rad60);
            double ry = x * FastMath.sin((sector - 1) * t*rad60) + y * FastMath.cos((sector - 1) * t*rad60);
            
            return new double[] {rx, ry};
        } else {
            return null;
        }
    }
    private int _sector = 0;
    /**
     * @return the _sector
     */
    public int getSector() {
        return _sector;
    }

    /**
     * @param _sector the _sector to set
     */
    public void setSector(int _sector) {
        this._sector = _sector;
    }
    
    private int calcSector(double x, double y) {
        double phi = Math.toDegrees(org.apache.commons.math3.util.FastMath.atan2(y, x));
        double ang = phi + 30;
        while (ang < 0) {
            ang += 360;
        }
        int sector = 1 + (int) (ang / 60.);

        if (sector == 7) {
            sector = 6;
        }

        if ((sector < 1) || (sector > 6)) {
            System.err.println("Track sector not found....");
        }
        return sector;
    }
    private final double cos_tilt = FastMath.cos(Math.toRadians(25.));
    private final double sin_tilt = FastMath.sin(Math.toRadians(25.));
    
    private final double rad60 = Math.toRadians(60.);
    /*
    public Point3D getCoordsInLab(double X, double Y, double Z) {
        Point3D PointInSec = this.getCoordsInSector(X, Y, Z);
        double rx = PointInSec.x() * FastMath.cos((this.get_Sector() - 1) * Math.toRadians(60.)) - PointInSec.y() * FastMath.sin((this.get_Sector() - 1) * Math.toRadians(60.));
        double ry = PointInSec.x() * FastMath.sin((this.get_Sector() - 1) * Math.toRadians(60.)) + PointInSec.y() * FastMath.cos((this.get_Sector() - 1) * Math.toRadians(60.));

        return new Point3D(rx, ry, PointInSec.z());
    }
    
    public Point3D getCoordsInTiltedSector(double X, double Y, double Z) {
        double rx = X * FastMath.cos((this.get_Sector() - 1) * Math.toRadians(-60.)) - Y * FastMath.sin((this.get_Sector() - 1) * Math.toRadians(-60.));
        double ry = X * FastMath.sin((this.get_Sector() - 1) * Math.toRadians(-60.)) + Y * FastMath.cos((this.get_Sector() - 1) * Math.toRadians(-60.));
       
        double rtz = rx * sin_tilt + Z * cos_tilt;
        double rtx = rx * cos_tilt - Z * sin_tilt;
         
        return new Point3D(rtx, ry, rtz);
    */
}
