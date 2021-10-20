package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.SVTGeometry;

/**
 * The crosses are objects used to find tracks and are characterized by a 3-D
 * point and a direction unit vector.
 *
 * @author ziegler
 *
 */
public class Cross extends ArrayList<Cluster> implements Comparable<Cross> {

    /**
     * serial id
     */
    private static final long serialVersionUID = 5317526429163382618L;
    public boolean isInSeed = false;
    private double cCrossRadius = 0;
    
    /**
     * @param detector SVT or BMT
     * @param detectortype detector type for BMT, C or Z detector
     * @param sector the sector (1...)
     * @param region the region (1...)
     * @param crid
     */
    public Cross(DetectorType detector, BMTType detectortype, int sector, int region, int crid) {
        this._Detector = detector;
        this._Type = detectortype;
        this._Sector = sector;
        this._Region = region;
        this._Id = crid;
        this._usedInXYcand = false;
        this._usedInZRcand = false;
    }
    
    private DetectorType _Detector;							//      the detector SVT or BMT
    private BMTType _Type;						//      the detector type for BMT, C or Z detector	
    private int _Sector;      							//	    sector [1...]
    private int _Region;    		 					//	    region [1,...]
    private int _OrderedRegion;                                                 // 1...3:SVT; 4...9: BMT
    private int _Id;									//		cross Id

    private boolean _usedInXYcand;   // used in patter recognition
    private boolean _usedInZRcand;
    
    public boolean is_usedInXYcand() {
		return _usedInXYcand;
	}

	public void set_usedInXYcand(boolean _usedInXYcand) {
		this._usedInXYcand = _usedInXYcand;
	}

	public boolean is_usedInZRcand() {
		return _usedInZRcand;
	}

	public void set_usedInZRcand(boolean _usedInZRcand) {
		this._usedInZRcand = _usedInZRcand;
	}

	// point parameters:
    private Point3D _Point;
    private Point3D _PointErr;
    private Point3D _Point0;
    private Point3D _PointErr0;
    private Vector3D _Dir;
    private Vector3D _DirErr;

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cross other = (Cross) obj;
		if (_Id != other._Id)
			return false;
		return true;
	}
    
    public DetectorType get_Detector() {
        return _Detector;
    }

    public void set_Detector(DetectorType _Detector) {
        this._Detector = _Detector;
    }

    public BMTType get_Type() {
        return _Type;
    }

    public void set_Type(BMTType type) {
        this._Type = type;
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
     * @return the _OrderedRegion
     */
    public int getOrderedRegion() {
        return _OrderedRegion;
    }

    /**
     * @param _OrderedRegion the _OrderedRegion to set
     */
    public void setOrderedRegion(int _OrderedRegion) {
        this._OrderedRegion = _OrderedRegion;
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
    public Point3D get_Point0() {
        return _Point0;
    }

    /**
     * Sets the cross 3-D point
     *
     * @param _Point a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public void set_Point0(Point3D _Point) {
        this._Point0 = _Point;
    }

    /**
     *
     * @return a 3-dimensional error on the 3-D point characterizing the
     * position of the cross in the tilted coordinate system.
     */
    public Point3D get_PointErr0() {
        return _PointErr0;
    }

    /**
     * Sets a 3-dimensional error on the 3-D point
     *
     * @param _PointErr a 3-dimensional error on the 3-D point characterizing
     * the position of the cross in the tilted coordinate system.
     */
    public void set_PointErr0(Point3D _PointErr) {
        this._PointErr0 = _PointErr;
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
    
    public double getY() {
        return this._Point.y();
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
    public Vector3D get_Dir() {
        return _Dir;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param trkDir the cross unit direction vector
     */
    public void set_Dir(Vector3D trkDir) {
        this._Dir = trkDir;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Vector3D get_DirErr() {
        return _DirErr;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _DirErr the cross unit direction vector
     */
    public void set_DirErr(Vector3D _DirErr) {
        this._DirErr = _DirErr;
    }

    /**
     *
     * @return serialVersionUID
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    private Cluster _clus1;
    private Cluster _clus2;

    private Cross _MatchedZCross;
    private Cross _MatchedCCross;

    /**
     * Set the first cluster (corresponding to the first superlayer in a region)
     *
     * @param seg1 the Cluster (in the first superlayer) which is used to make a
     * cross
     */
    public void set_Cluster1(Cluster seg1) {
        this._clus1 = seg1;
    }

    /**
     * Set the second Cluster (corresponding to the second superlayer in a
     * region)
     *
     * @param seg2 the Cluster (in the second superlayer) which is used to make
     * a cross
     */
    public void set_Cluster2(Cluster seg2) {
        this._clus2 = seg2;
    }

    /**
     *
     * @return he Cluster (in the first superlayer) which is used to make a
     * cross
     */
    public Cluster get_Cluster1() {
        return _clus1;
    }

    /**
     *
     * @return the Cluster (in the second superlayer) which is used to make a
     * cross
     */
    public Cluster get_Cluster2() {
        return _clus2;
    }

    public Cross get_MatchedZCross() {
        return _MatchedZCross;
    }

    public void set_MatchedZCross(Cross _MatchedZCross) {
        this._MatchedZCross = _MatchedZCross;
    }

    public Cross get_MatchedCCross() {
        return _MatchedCCross;
    }

    public void set_MatchedCCross(Cross _MatchedCCross) {
        this._MatchedCCross = _MatchedCCross;
    }

    /**
     * Sets the cross parameters: the position and direction unit vector
     */
    public void setBMTCrossPosition(Point3D trackPos) {

        Cluster cluster  = this.get_Cluster1();
        
        if(this.get_Detector()!=DetectorType.BMT) return;

        Point3D  crossPoint = this.getBMTCrossPoint(trackPos);
        Vector3D crossError = this.getBMTCrossError(trackPos);
        if (trackPos == null) {
            this.set_Point0(crossPoint);
            this.set_PointErr0(crossError.toPoint3D());
        }
        this.set_Point(crossPoint);
        this.set_PointErr(crossError.toPoint3D());
    }

    private Point3D getBMTCrossPoint(Point3D trackPos) {
        Cluster cluster = this.get_Cluster1();
        
        Point3D cross = cluster.center();
        
        if(trackPos!=null) {
            Point3D local = new Point3D(trackPos);
            cluster.get_SeedStrip().toLocal().apply(local);
            if(this.get_Type()==BMTType.C) {
                double phi  = Math.atan2(local.y(), local.x());
                double phi0 = Math.atan2(cluster.origin().y(), cluster.origin().x());
                double t = phi-phi0;
                if(Math.abs(t)>Math.PI) t-=Math.signum(t)*2*Math.PI;
                if(t<0) 
                    cross = cluster.origin();
                else if(t>cluster.get_Arc().theta())
                    cross = cluster.end();
                else {
                    cross = cluster.get_Arc().point(t);
                }
            }
            else {
                cross = cluster.getLine().distanceSegment(trackPos).origin();
            }
        }
        return cross;
    }   
    

    private Vector3D getBMTCrossError(Point3D trackPos) {
        Cluster cluster = this.get_Cluster1();
        Point3D cross   = this.getBMTCrossPoint(trackPos);
        
        Point3D local = new Point3D(cross);
        cluster.get_SeedStrip().toLocal().apply(local);
        
        Vector3D error = new Vector3D(cluster.getS());
        error.scale(cluster.get_Resolution());
        error.setXYZ(Math.abs(error.x()), Math.abs(error.y()), Math.abs(error.z()));
        return error;
    }   
    
    /**
     * Sets the cross parameters: the position and direction unit vector
     */
    public void setSVTCrossPosition(Vector3D trackDir, SVTGeometry geo) {

        Cluster inlayerclus  = this.get_Cluster1();
        Cluster outlayerclus = this.get_Cluster2();
        if (inlayerclus == null || outlayerclus == null) { 
            return;
        }
        // RDV: z error is now smaller because resulting from strip resolution instead of +/- 1 strip
        Point3D  crossPoint = this.getSVTCrossPoint(trackDir, geo);
        Vector3D crossError = this.getSVTCrossError(trackDir, geo);
        
        if(crossPoint==null || crossError==null) {
            return;
        }
        
        if (trackDir == null) {
            this.set_Point0(crossPoint);
            this.set_PointErr0(crossError.toPoint3D());
        }

        this.set_Point(crossPoint);
        this.set_Dir(trackDir);
        this.set_PointErr(crossError.toPoint3D());

    }

    /**
     * Calculate the cross point from the two strips and the track direction
     * @param trackDir track direction
     * @param geo      SVT geometry class
     * @return
     */
    public Point3D getSVTCrossPoint(Vector3D trackDir, SVTGeometry geo) {
        
        int layer  = this.get_Cluster1().get_Layer();
        int sector = this.get_Cluster1().get_Sector();
        
        Point3D cross = geo.getCross(sector, layer, this.get_Cluster1().getLine(), this.get_Cluster2().getLine(), trackDir);
  
        return cross;
    }

    /**
     * Calculate the cross position error from the two strips and the track direction
     * @param trackDir track direction
     * @param geo      VT geometry
     * @return
     */
    public Vector3D getSVTCrossError(Vector3D trackDir, SVTGeometry geo) {
        Vector3D error = null;
       
        int layer  = this.get_Cluster1().get_Layer();
        int sector = this.get_Cluster1().get_Sector();

        Point3D cross = this.getSVTCrossPoint(trackDir, geo);
        if(cross!=null) {
            // get the strip resolution
            Point3D local = geo.toLocal(layer, sector, cross);
            double sigma1 = geo.getSingleStripResolution(layer, this.get_Cluster1().get_SeedStrip().get_Strip(), local.z());
            double sigma2 = geo.getSingleStripResolution(layer, this.get_Cluster2().get_SeedStrip().get_Strip(), local.z());
            
            // get the error associated to each strip
            Vector3D error1 = this.getSVTCrossDerivative(1, trackDir, geo).multiply(sigma1);
            Vector3D error2 = this.getSVTCrossDerivative(2, trackDir, geo).multiply(sigma2);
            if(error1!=null && error2!=null)
                error = new Vector3D(Math.sqrt(error1.x()*error1.x()+error2.x()*error2.x()),
                                     Math.sqrt(error1.y()*error1.y()+error2.y()*error2.y()),
                                     Math.sqrt(error1.z()*error1.z()+error2.z()*error2.z()));
//            if(error.x()==0 && error.y()==0) System.out.println(cross.toString() + "\n" + error.toString());
        }
        return error;
    }
    /**
     * Calculate the cross derivative for the translation of one strip
     * useful for the error calculation
     * @param trackDir track direction
     * @param geo      VT geometry
     * @return
     */
    public Vector3D getSVTCrossDerivative(int icluster, Vector3D trackDir, SVTGeometry geo) {
        Vector3D error = null;
       
        // check the cluster to be used in the derivative calculation is either 1 or 2
        if(icluster<1 || icluster>2) return null;

        // if the croos position is not well defined, don't do anything
        Point3D cross = this.getSVTCrossPoint(trackDir, geo);
        if(cross==null) return null;
         
        int layer  = this.get_Cluster1().get_Layer();
        int sector = this.get_Cluster1().get_Sector();
        
        double delta = 1e-3; // 1micron shift
        
        // get the clusters
        Cluster clusA = this.get_Cluster1();
        Cluster clusB = this.get_Cluster2();
        if(icluster==2) {
            clusA = this.get_Cluster2();
            clusB = this.get_Cluster1();           
        }
        
        // shift the selected cluster to the left and right of the line
        Vector3D t = clusA.getLine().direction().asUnit().cross(clusA.getN()).multiply(delta);
        Line3D stripAPlus  = new Line3D(clusA.getLine()); 
        Line3D stripAMinus = new Line3D(clusA.getLine()); 
        stripAPlus.translateXYZ(  t.x(), t.y(), t.z());
        stripAMinus.translateXYZ(-t.x(),-t.y(),-t.z());
            
        // calculate the shifted cross positions
        Point3D crossAPlus  = null;
        Point3D crossAMinus = null;
        if(clusA.get_Layer()%2 == 1) {
            crossAPlus  = geo.getCross(sector, layer, stripAPlus,  clusB.getLine(), trackDir);
            crossAMinus = geo.getCross(sector, layer, stripAMinus, clusB.getLine(), trackDir);
        }
        else {
            crossAPlus  = geo.getCross(sector, layer, clusB.getLine(),  stripAPlus, trackDir);
            crossAMinus = geo.getCross(sector, layer, clusB.getLine(), stripAMinus, trackDir);
        }
        
        // if at least one is non-null, calculate the derivative
        if(crossAPlus!=null && crossAMinus!=null) {
            error = crossAMinus.vectorTo(crossAPlus).multiply(1/delta);            
        }
        else if(crossAPlus!=null) {
            error = cross.vectorTo(crossAPlus).multiply(2/delta);
        }
        else if(crossAMinus!=null) {
            error = crossAMinus.vectorTo(cross).multiply(2/delta);
        }
        
        return error;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + _Id;
        return result;
    }
    
    @Override
    public String toString() {
    	return this.printInfo();
    }
    
    public String printInfo() {
        String s = " cross:  " + this.get_Detector() + " ID " + this.get_Id() + " Sector " + this.get_Sector() + " Region " + this.get_Region()
                + " Point " + this.get_Point().toString()+ " Point0 " + this.get_Point0().toString()
                + " sort "+this.getOrderedRegion()+" cosmic region "+this.get_SVTCosmicsRegion();
        
        
        return s;
    }

    public int get_SVTCosmicsRegion() {

        int theRegion = 0;
        if (this.get_Detector()==DetectorType.BST) {
            /*
            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[6][0] + Constants.MODULERADIUS[7][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 8;
                } else {
                    theRegion = 1;
                }
            }

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[4][0] + Constants.MODULERADIUS[5][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 7;
                } else {
                    theRegion = 2;
                }
            }

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[2][0] + Constants.MODULERADIUS[3][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 6;
                } else {
                    theRegion = 3;
                }
            }

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[0][0] + Constants.MODULERADIUS[1][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 5;
                } else {
                    theRegion = 4;
                }
            }
            */
             

            if (this.get_Point0().toVector3D().rho() - SVTGeometry.getRegionRadius(3) < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 6;
                } else {
                    theRegion = 1;
                }
            }

            if (this.get_Point0().toVector3D().rho() - SVTGeometry.getRegionRadius(2) < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 5;
                } else {
                    theRegion = 2;
                }
            }

            if (this.get_Point0().toVector3D().rho() - SVTGeometry.getRegionRadius(1) < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 4;
                } else {
                    theRegion = 3;
                }
            }
        }

        return theRegion;
    }

    /**
     * Sorts crosses
     */
    @Override
    public int compareTo(Cross arg) {

        int return_val = 0;
        if (Constants.isCosmicsData() == true) {
            int RegComp = this.get_SVTCosmicsRegion() < arg.get_SVTCosmicsRegion() ? -1 : this.get_SVTCosmicsRegion() == arg.get_SVTCosmicsRegion() ? 0 : 1;
            int IDComp = this.get_Id() < arg.get_Id() ? -1 : this.get_Id() == arg.get_Id() ? 0 : 1;

            return_val = ((RegComp == 0) ? IDComp : RegComp);
        } else {
            
            //int thisreg = (this.get_Detector().equalsIgnoreCase("BMT")) ? 3 + bgeom.getLayer( this.get_Region(), this.get_DetectorType()) : this.get_Region();
            //int argreg  = (arg.get_Detector().equalsIgnoreCase("BMT"))  ? 3 + bgeom.getLayer( arg.get_Region(), arg.get_DetectorType()) : arg.get_Region();
            int thisreg = this.getOrderedRegion();
            int argreg  = arg.getOrderedRegion();
            int RegComp = thisreg < argreg ? -1 : thisreg == argreg ? 0 : 1;
//            int RegComp = this.get_Region() < arg.get_Region() ? -1 : this.get_Region() == arg.get_Region() ? 0 : 1;
            
            // check that is not BMTC for phi comparison
            if( Double.isNaN(arg.get_Point().x())==false &&  Double.isNaN(this.get_Point().x())==false ) {
            	int PhiComp = this.get_Point0().toVector3D().phi() < arg.get_Point0().toVector3D().phi() ? -1 : this.get_Point0().toVector3D().phi() == arg.get_Point0().toVector3D().phi() ? 0 : 1;
            
            	return_val = ((RegComp == 0) ? PhiComp : RegComp);
            }
            else {
            	int ZComp = this.get_Point0().z() < arg.get_Point0().z() ? -1 : this.get_Point0().z() == arg.get_Point0().z() ? 0 : 1;
            	return_val = ((RegComp == 0) ? ZComp : RegComp);
            }
        }

        return return_val;
    }

    private int AssociatedTrackID = -1; // the track ID associated with that hit 

    public int get_AssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void set_AssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

    /**
     * @return the cCrossRadius
     */
    public double getcCrossRadius() {
        return cCrossRadius;
    }

    /**
     * @param cCrossRadius the cCrossRadius to set
     */
    public void setcCrossRadius(double cCrossRadius) {
        this.cCrossRadius = cCrossRadius;
    }

    public static void main(String arg[]) {

        // Geometry geo = new Geometry();

        ArrayList<Cross> testList = new ArrayList<Cross>();

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 1, 1 + i);
            c1.set_Point0(new Point3D(-1.2 - i, 66.87, 0));
            testList.add(c1);
        }
        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 3, 1 + i);
            c1.set_Point0(new Point3D(-1.2 + i, 123, 0));
            testList.add(c1);
        }

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 2, 1 + i);
            c1.set_Point0(new Point3D(-1.2 - i, 95, 0));
            testList.add(c1);
        }

        Collections.sort(testList);

        ArrayList<ArrayList<Cross>> theListsByRegion = new ArrayList<ArrayList<Cross>>();

        ArrayList<Cross> theRegionList = new ArrayList<Cross>();
        if (testList.size() > 0) {
            theRegionList.add(testList.get(0)); // init
        }
        for (int i = 1; i < testList.size(); i++) {
            Cross c = testList.get(i);
            if (testList.get(i - 1).get_Region() != c.get_Region()) {
                theListsByRegion.add(theRegionList);    // end previous list by region
                theRegionList = new ArrayList<Cross>(); // new region list
            }
            theRegionList.add(c);
        }
        theListsByRegion.add(theRegionList);

        // check that the correct lists are created
        for (int i = 0; i < theListsByRegion.size(); i++) {
            for (int j = 0; j < theListsByRegion.get(i).size(); j++) {
                Cross c = theListsByRegion.get(i).get(j);
            }
        }

    }

}
