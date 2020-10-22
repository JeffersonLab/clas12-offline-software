package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.Collections;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.svt.Geometry;

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

    /**
     * @param detector SVT or BMT
     * @param detectortype detector type for BMT, C or Z detector
     * @param sector the sector (1...)
     * @param region the region (1...)
     * @param rid the cross ID (if there are only 3 crosses in the event, the ID
     * corresponds to the region index
     */
    public Cross(String detector, String detectortype, int sector, int region, int crid) {
        this._Detector = detector;
        this._DetectorType = detectortype;
        this._Sector = sector;
        this._Region = region;
        this._Id = crid;
        this._usedInXYcand = false;
        this._usedInZRcand = false;
    }
    
    private String _Detector;							//      the detector SVT or BMT
    private String _DetectorType;						//      the detector type for BMT, C or Z detector	
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
    
    public String get_Detector() {
        return _Detector;
    }

    public void set_Detector(String _Detector) {
        this._Detector = _Detector;
    }

    public String get_DetectorType() {
        return _DetectorType;
    }

    public void set_DetectorType(String _DetectorType) {
        this._DetectorType = _DetectorType;
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
    public void set_CrossParamsSVT(Vector3D dirAtBstPlane, Geometry geo) {

        Cluster inlayerclus = this.get_Cluster1();
        Cluster outlayerclus = this.get_Cluster2();
        if (inlayerclus == null || outlayerclus == null) { 
            return;
        }

        double[] Params = geo.getCrossPars(outlayerclus.get_Sector(), outlayerclus.get_Layer(),
                inlayerclus.get_Centroid(), outlayerclus.get_Centroid(), "lab", dirAtBstPlane);

        double val = Params[0];
        if (Double.isNaN(val)) {
            return; // cross not withing fiducial region
        }
        Point3D interPoint = new Point3D(Params[0], Params[1], Params[2]);

        Point3D interPointErr = new Point3D(Params[3], Params[4], Params[5]);

        if (dirAtBstPlane == null) {
            this.set_Point0(interPoint);
            this.set_PointErr0(interPointErr);
        }

        this.set_Point(interPoint);
        this.set_Dir(dirAtBstPlane);
        this.set_PointErr(interPointErr);

        //System.out.println("[Cross] in setCrossPars interPoint "+interPoint.toString());
        //if(dirAtBstPlane!=null)
        //	System.out.println("                              dirAtBstPlane "+dirAtBstPlane.toString());
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
                + " Point " + this.get_Point().toString()
                + " sort "+this.getOrderedRegion();
        
        
        return s;
    }

    public int get_SVTCosmicsRegion() {

        int theRegion = 0;
        if (this.get_Detector().equalsIgnoreCase("SVT")) {
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
             

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[4][0] + Constants.MODULERADIUS[5][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 6;
                } else {
                    theRegion = 1;
                }
            }

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[2][0] + Constants.MODULERADIUS[3][0]) * 0.5 < 15) {
                if (this.get_Point0().y() > 0) {
                    theRegion = 5;
                } else {
                    theRegion = 2;
                }
            }

            if (this.get_Point0().toVector3D().rho() - (Constants.MODULERADIUS[0][0] + Constants.MODULERADIUS[1][0]) * 0.5 < 15) {
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
        if (org.jlab.rec.cvt.Constants.isCosmicsData() == true) {
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

    public static void main(String arg[]) {

        Constants.Load();
        // Geometry geo = new Geometry();

        ArrayList<Cross> testList = new ArrayList<Cross>();

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross("SVT", "", 1, 1, 1 + i);
            c1.set_Point0(new Point3D(-1.2 - i, 66.87, 0));
            testList.add(c1);
        }
        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross("SVT", "", 1, 3, 1 + i);
            c1.set_Point0(new Point3D(-1.2 + i, 123, 0));
            testList.add(c1);
        }

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross("SVT", "", 1, 2, 1 + i);
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
            System.out.println(" i " + i);
            for (int j = 0; j < theListsByRegion.get(i).size(); j++) {
                Cross c = theListsByRegion.get(i).get(j);
                System.out.println(c.get_Detector() + " " + c.get_Region() + " " + c.get_Point0().toVector3D().phi());
            }
        }

    }

    public boolean isInFiducial(Geometry svt_geo) {
        boolean pass = true;
        Point3D LC = svt_geo.transformToFrame(this.get_Sector(), this.get_Cluster1().get_Layer(), this.get_Point().x(), this.get_Point().y(), this.get_Point().z(), "local", "");
        if (((LC.x() < -0.10 || LC.x() > Constants.ACTIVESENWIDTH + 0.10))
                || ((LC.z() < -1 || LC.z() > Constants.MODULELENGTH + 1))) {
            pass = false;
        }
        return pass;
    }

}
