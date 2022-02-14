package org.jlab.rec.dc.cross;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.clas.math.FastMath;

//import org.apache.commons.math3.util.FastMath;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;

/**
 * The crosses are objects used to find tracks and are characterized by a 3-D
 * point and a direction unit vector.
 *
 * @author ziegler
 *
 */
public class Cross extends ArrayList<Segment> implements Comparable<Cross> {
    
    private static final Logger LOGGER = Logger.getLogger(Cross.class.getName());

    /**
     * serial id
     */
    private static final long serialVersionUID = 5317526429163382618L;
    /**
     *
     * @return serialVersionUID
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    private int _Sector;      							//	sector[1...6]
    private int _Region;    		 					//	region[1,...3]
    private int _Id;								//	cross Id

    // point parameters:
    private Point3D _Point;
    private Point3D _PointErr;
    private Point3D _Dir;
    private Point3D _DirErr;
    private Segment _seg1;
    private Segment _seg2;
    public boolean isPseudoCross = false;
    
    private final double cos_tilt = FastMath.cos(Math.toRadians(25.));
    private final double sin_tilt = FastMath.sin(Math.toRadians(25.));
    
    public int recalc;
    /**
     *
     * @param sector the sector (1...6)
     * @param region the region (1...3)
     * @param rid the cross ID (if there are only 3 crosses in the event, the ID
     * corresponds to the region index
     */
    public Cross(int sector, int region, int rid) {
        this._Sector = sector;
        this._Region = region;
        this._Id = rid;
    }

    /**
     *
     * @return the sector of the cross
     */
    public int get_Sector() {
        return _Sector;
    }

    /**
     * Sets the sector
     *
     * @param _Sector the sector of the cross
     */
    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the region of the cross
     */
    public int get_Region() {
        return _Region;
    }

    /**
     * Sets the region
     *
     * @param _Region the region of the cross
     */
    public void set_Region(int _Region) {
        this._Region = _Region;
    }

    /**
     *
     * @return the id of the cross
     */
    public int get_Id() {
        return _Id;
    }

    /**
     * Sets the cross ID
     *
     * @param _Id the id of the cross
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public Point3D get_Point() {
        return _Point;
    }

    /**
     * Sets the cross 3-D point
     *
     * @param _Point a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public void set_Point(Point3D _Point) {
        this._Point = _Point;
    }

    /**
     *
     * @return a 3-dimensional error on the 3-D point characterizing the
     * position of the cross in the tilted coordinate system.
     */
    public Point3D get_PointErr() {
        return _PointErr;
    }

    /**
     * Sets a 3-dimensional error on the 3-D point
     *
     * @param _PointErr a 3-dimensional error on the 3-D point characterizing
     * the position of the cross in the tilted coordinate system.
     */
    public void set_PointErr(Point3D _PointErr) {
        this._PointErr = _PointErr;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Point3D get_Dir() {
        return _Dir;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _Dir the cross unit direction vector
     */
    public void set_Dir(Point3D _Dir) {
        this._Dir = _Dir;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Point3D get_DirErr() {
        return _DirErr;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _DirErr the cross unit direction vector
     */
    public void set_DirErr(Point3D _DirErr) {
        this._DirErr = _DirErr;
    }


    /**
     * Sorts crosses by azimuth angle values
     * @param arg
     */
    @Override
    public int compareTo(Cross arg) {
        double theta_rad1 = FastMath.atan2(this.get_Point().y(), this.get_Point().x());
        double theta_deg1 = (theta_rad1 / Math.PI * 180.) + (theta_rad1 > 0 ? 0 : 360.);
        double theta_rad2 = FastMath.atan2(arg.get_Point().y(), arg.get_Point().x());
        double theta_deg2 = (theta_rad2 / Math.PI * 180.) + (theta_rad2 > 0 ? 0 : 360.);
        

        int RegComp = this.get_Region() < arg.get_Region() ? -1 : this.get_Region() == arg.get_Region() ? 0 : 1;
        int AngComp = theta_deg1 < theta_deg2 ? -1 : theta_deg1 == theta_deg2 ? 0 : 1;

        return ((RegComp == 0) ? AngComp : RegComp);
        
//        if (theta_deg1 < theta_deg2) {
//            return 1;
//        } else {
//            return -1;
//        }
    }


    /**
     * Set the first segment (corresponding to the first superlayer in a region)
     *
     * @param seg1 the segment (in the first superlayer) which is used to make a
     * cross
     */
    public void set_Segment1(Segment seg1) {
        this._seg1 = seg1;
    }

    /**
     * Set the second segment (corresponding to the second superlayer in a
     * region)
     *
     * @param seg2 the segment (in the second superlayer) which is used to make
     * a cross
     */
    public void set_Segment2(Segment seg2) {
        this._seg2 = seg2;
    }

    /**
     *
     * @return he segment (in the first superlayer) which is used to make a
     * cross
     */
    public Segment get_Segment1() {
        return _seg1;
    }

    /**
     *
     * @return the segment (in the second superlayer) which is used to make a
     * cross
     */
    public Segment get_Segment2() {
        return _seg2;
    }

    /**
     * Sets the cross parameters: the position and direction unit vector
     * @param DcDetector
     */
    public void set_CrossParams(DCGeant4Factory DcDetector) {

        //double z = GeometryLoader.dcDetector.getSector(0).getRegionMiddlePlane(this.get_Region()-1).point().z();
        double z = DcDetector.getRegionMidpoint(this.get_Region() - 1).z;

        double wy_over_wx = (Math.cos(Math.toRadians(6.)) / Math.sin(Math.toRadians(6.)));
        double val_sl1 = this._seg1.get_fittedCluster().get_clusterLineFitSlope();
        double val_sl2 = this._seg2.get_fittedCluster().get_clusterLineFitSlope();
        double val_it1 = this._seg1.get_fittedCluster().get_clusterLineFitIntercept();
        double val_it2 = this._seg2.get_fittedCluster().get_clusterLineFitIntercept();

        double x = 0.5 * (val_it1 + val_it2) + 0.5 * z * (val_sl1 + val_sl2);
        double y = 0.5 * wy_over_wx * (val_it2 - val_it1) + 0.5 * wy_over_wx * z * (val_sl2 - val_sl1);

        this.set_Point(new Point3D(x, y, z));

        double tanThX = val_sl2;
        double tanThY = FastMath.atan2(y, z);
        double uz = 1. / Math.sqrt(1 + tanThX * tanThX + tanThY * tanThY);
        double ux = uz * tanThX;
        double uy = uz * tanThY;

        Point3D dirVec = new Point3D(ux, uy, uz);
        this.set_Dir(dirVec);

        if (this.get_Dir().z() == 0) {
            return;
        }

        // Error ...  propagated from errors on slopes and intercepts
        double err_sl1 = this._seg1.get_fittedCluster().get_clusterLineFitSlopeErr();
        double err_sl2 = this._seg2.get_fittedCluster().get_clusterLineFitSlopeErr();
        double err_it1 = this._seg1.get_fittedCluster().get_clusterLineFitInterceptErr();
        double err_it2 = this._seg2.get_fittedCluster().get_clusterLineFitInterceptErr();
        double err_cov1 = this._seg1.get_fittedCluster().get_clusterLineFitSlIntCov();
        double err_cov2 = this._seg2.get_fittedCluster().get_clusterLineFitSlIntCov();

        //double err_x = 0.5*Math.sqrt(err_it1*err_it1+err_it2*err_it2 + z*z*(err_sl1*err_sl1+err_sl2*err_sl2) );
        //double err_y = 0.5*wy_over_wx*Math.sqrt(err_it1*err_it1+err_it2*err_it2 +z*z*(err_sl1*err_sl1+err_sl2*err_sl2) );
        double err_x_fix = 0.5 * Math.sqrt(err_it1 * err_it1 + err_it2 * err_it2 + z * z * (err_sl1 * err_sl1 + err_sl2 * err_sl2) + 2 * z * err_cov1 + 2 * z * err_cov2);
        double err_y_fix = 0.5 * wy_over_wx * Math.sqrt(err_it1 * err_it1 + err_it2 * err_it2 + z * z * (err_sl1 * err_sl1 + err_sl2 * err_sl2) + 2 * z * err_cov1 + 2 * z * err_cov2);

        this.set_PointErr(new Point3D(err_x_fix, err_y_fix, 0));

        double inv_N_sq = 1. / (0.25 * val_sl1 * val_sl1 * (1 + wy_over_wx * wy_over_wx) + 0.25 * val_sl2 * val_sl2 * (1 + wy_over_wx * wy_over_wx) + 0.5 * val_sl1 * val_sl2 * (1 - wy_over_wx * wy_over_wx) + 1);
        double inv_N = Math.sqrt(inv_N_sq);
        double del_inv_n_del_sl1err = -0.25 * (val_sl1 * (1 + wy_over_wx * wy_over_wx) + val_sl2 * (1 - wy_over_wx * wy_over_wx)) * inv_N * inv_N_sq;
        double del_inv_n_del_sl2err = -0.25 * (val_sl2 * (1 + wy_over_wx * wy_over_wx) + val_sl1 * (1 - wy_over_wx * wy_over_wx)) * inv_N * inv_N_sq;

        double err_x2 = 0.5 * (inv_N + (val_sl1 + val_sl2) * del_inv_n_del_sl1err) * (inv_N + (val_sl1 + val_sl2) * del_inv_n_del_sl1err) * err_sl1 * err_sl1
                + 0.5 * (inv_N + (val_sl1 + val_sl2) * del_inv_n_del_sl2err) * (inv_N + (val_sl1 + val_sl2) * del_inv_n_del_sl2err) * err_sl2 * err_sl2;

        double err_y2 = 0.5 * (-inv_N * wy_over_wx + (val_sl2 - val_sl1) * wy_over_wx * del_inv_n_del_sl1err) * (-inv_N * wy_over_wx + (val_sl2 - val_sl1) * wy_over_wx * del_inv_n_del_sl1err) * err_sl1 * err_sl1
                + 0.5 * (inv_N * wy_over_wx + (val_sl2 - val_sl1) * del_inv_n_del_sl2err) * (inv_N * wy_over_wx + (val_sl2 - val_sl1) * del_inv_n_del_sl2err) * err_sl2 * err_sl2;

        double err_z2 = del_inv_n_del_sl1err * del_inv_n_del_sl1err * err_sl1 * err_sl1 + del_inv_n_del_sl2err * del_inv_n_del_sl2err * err_sl2 * err_sl2;

        double err_xDir = Math.sqrt(err_x2);
        double err_yDir = Math.sqrt(err_y2);
        double err_zDir = Math.sqrt(err_z2);

        Point3D estimDirErr = new Point3D(err_xDir, err_yDir, err_zDir);

        this.set_DirErr(estimDirErr);

        if (this._seg1.get_Id() == -1 || this._seg2.get_Id() == -1) {
            this.isPseudoCross = true;
        }
    }

    /**
     *
     * @return the track info.
     */
    public String printInfo() {
        String s = "DC cross: ID " + this.get_Id() + " Sector " + this.get_Sector() + " Region " + this.get_Region()
                + " Point " + this.get_Point().toString() + "  Dir " + this.get_Dir().toString();
        return s;
    }
    
    /**
     *
     * @param X
     * @param Y
     * @param Z
     * @return rotated coords from tilted sector coordinate system to the sector
     * coordinate system
     */
    public Point3D getCoordsInSector(double X, double Y, double Z) {
        double rz = -X * sin_tilt + Z * cos_tilt;
        double rx = X * cos_tilt + Z * sin_tilt;

        return new Point3D(rx, Y, rz);
    }

    /**
     *
     * @param X
     * @param Y
     * @param Z
     * @return rotated coords from tilted sector coordinate system to the lab
     * frame
     */
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
    }
    /*
	public double[] ReCalcPseudoCross(Cross c2, Cross c3) {
	
		double[] XY = new double[2];
		Vector3D fitLine = new Vector3D(c3.get_Point().x()-c2.get_Point().x(), c3.get_Point().y()-c2.get_Point().y(), 0);
		fitLine.unit();
		
		Vector3D pseuLine = new Vector3D(this.get_Point().x()-c2.get_Point().x(), this.get_Point().y()-c2.get_Point().y(), 0);
		pseuLine.unit();
		
		double alpha = Math.acos(fitLine.dot(pseuLine));
		
		double X = (this.get_Point().x()-c2.get_Point().x())*Math.cos(alpha) + (this.get_Point().y()-c2.get_Point().y())*Math.sin(alpha) + c2.get_Point().x();
		double Y = -(this.get_Point().x()-c2.get_Point().x())*Math.sin(alpha) + (this.get_Point().y()-c2.get_Point().y())*Math.cos(alpha) + c2.get_Point().y();
		
		if(this.recalc==0) {
			
			LOGGER.log(Level.FINE, this.recalc+"] "+this.printInfo()+" alpha "+Math.toDegrees(alpha)+" X "+X+" Y "+Y+c2.printInfo()+" "+c3.printInfo());
			this.set_Point(new Point3D(X, Y, this.get_Point().z()));
		}
		XY[0] = X;
		XY[1] = Y;
		
		return XY;
		//LOGGER.log(Level.FINE, this.recalc+"] "+this.printInfo()+" alpha "+Math.toDegrees(alpha)+" X "+X+" Y "+Y+c2.printInfo()+" "+c3.printInfo());
	}
     */

    public void set_CrossDirIntersSegWires() {
        double wy_over_wx = (FastMath.cos(Math.toRadians(6.)) / FastMath.sin(Math.toRadians(6.)));
        double val_sl1 = this._seg1.get_fittedCluster().get_clusterLineFitSlope();
        double val_sl2 = this._seg2.get_fittedCluster().get_clusterLineFitSlope();
        double val_it1 = this._seg1.get_fittedCluster().get_clusterLineFitIntercept();
        double val_it2 = this._seg2.get_fittedCluster().get_clusterLineFitIntercept();
        
        LOGGER.log(Level.FINE, this._seg1.printInfo()+this._seg2.printInfo()+" insterWire: seg1 "+new Point3D(val_sl1, val_it1,999).toString()+
                    " seg2 "+new Point3D(val_sl2, val_it2,999).toString()
                   );
        for(int i =0; i<this.get_Segment1().size(); i++) {
            this.calc_IntersectPlaneAtZ(this.get_Segment1().get(i).get_Z(), wy_over_wx, val_sl1, val_sl2, val_it1, val_it2, this.get_Segment1().get(i));
        }
        for(int i =0; i<this.get_Segment2().size(); i++) {
            this.calc_IntersectPlaneAtZ(this.get_Segment2().get(i).get_Z(), wy_over_wx, val_sl1, val_sl2, val_it1, val_it2, this.get_Segment2().get(i));
        }
        
    }
   
    private void calc_IntersectPlaneAtZ(double z, double wy_over_wx, double val_sl1, double val_sl2, double val_it1, double val_it2, FittedHit hit) {
       
        LOGGER.log(Level.FINE, " .....insterWire: seg1 "+new Point3D(val_sl1, val_it1,z).toString()+
                    " seg2 "+new Point3D(val_sl2, val_it2,z).toString()
                   );
        double x = 0.5 * (val_it1 + val_it2) + 0.5 * z * (val_sl1 + val_sl2);
        double y = 0.5 * wy_over_wx * (val_it2 - val_it1) + 0.5 * wy_over_wx * z * (val_sl2 - val_sl1);
        
        if(hit.getCrossDirIntersWire()!=null && hit.getCrossDirIntersWire().x()!=x)
                LOGGER.log(Level.FINE, "Already exists "+hit.getCrossDirIntersWire().toString()+" for "
                +hit.printInfo() +"new "+new Point3D(x,y,z).toString());
        
        hit.setCrossDirIntersWire(new Point3D(x,y,z));
    } 
}
