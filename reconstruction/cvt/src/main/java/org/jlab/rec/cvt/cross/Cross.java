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

	public void setusedInXYcand(boolean _usedInXYcand) {
		this._usedInXYcand = _usedInXYcand;
	}

	public boolean is_usedInZRcand() {
		return _usedInZRcand;
	}

	public void setusedInZRcand(boolean _usedInZRcand) {
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
		return _Id == other._Id;
	}
    
    public DetectorType getDetector() {
        return _Detector;
    }

    public void setDetector(DetectorType _Detector) {
        this._Detector = _Detector;
    }

    public BMTType getType() {
        return _Type;
    }

    public void setType(BMTType type) {
        this._Type = type;
    }

    /**
     *
     * @return the sector of the cross
     */
    public int getSector() {
        return _Sector;
    }

    /**
     * Sets the sector
     *
     * @param _Sector the sector of the cross
     */
    public void setSector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the region of the cross
     */
    public int getRegion() {
        return _Region;
    }

    /**
     * Sets the region
     *
     * @param _Region the region of the cross
     */
    public void setRegion(int _Region) {
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
    public int getId() {
        return _Id;
    }

    /**
     * Sets the cross ID
     *
     * @param _Id the id of the cross
     */
    public void setId(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public Point3D getPoint0() {
        return _Point0;
    }

    /**
     * Sets the cross 3-D point
     *
     * @param _Point a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public void setPoint0(Point3D _Point) {
        this._Point0 = _Point;
    }

    /**
     *
     * @return a 3-dimensional error on the 3-D point characterizing the
     * position of the cross in the tilted coordinate system.
     */
    public Point3D getPointErr0() {
        return _PointErr0;
    }

    /**
     * Sets a 3-dimensional error on the 3-D point
     *
     * @param _PointErr a 3-dimensional error on the 3-D point characterizing
     * the position of the cross in the tilted coordinate system.
     */
    public void setPointErr0(Point3D _PointErr) {
        this._PointErr0 = _PointErr;
    }

    /**
     *
     * @return a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public Point3D getPoint() {
        return _Point;
    }

    /**
     * Sets the cross 3-D point
     *
     * @param _Point a 3-D point characterizing the position of the cross in the
     * tilted coordinate system.
     */
    public void setPoint(Point3D _Point) {
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
    public Point3D getPointErr() {
        return _PointErr;
    }

    /**
     * Sets a 3-dimensional error on the 3-D point
     *
     * @param _PointErr a 3-dimensional error on the 3-D point characterizing
     * the position of the cross in the tilted coordinate system.
     */
    public void setPointErr(Point3D _PointErr) {
        this._PointErr = _PointErr;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Vector3D getDir() {
        return _Dir;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param trkDir the cross unit direction vector
     */
    public void setDir(Vector3D trkDir) {
        this._Dir = trkDir;
    }

    /**
     *
     * @return the cross unit direction vector
     */
    public Vector3D getDirErr() {
        return _DirErr;
    }

    /**
     * Sets the cross unit direction vector
     *
     * @param _DirErr the cross unit direction vector
     */
    public void setDirErr(Vector3D _DirErr) {
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
    public void setCluster1(Cluster seg1) {
        this._clus1 = seg1;
    }

    /**
     * Set the second Cluster (corresponding to the second superlayer in a
     * region)
     *
     * @param seg2 the Cluster (in the second superlayer) which is used to make
     * a cross
     */
    public void setCluster2(Cluster seg2) {
        this._clus2 = seg2;
    }

    /**
     *
     * @return he Cluster (in the first superlayer) which is used to make a
     * cross
     */
    public Cluster getCluster1() {
        return _clus1;
    }

    /**
     *
     * @return the Cluster (in the second superlayer) which is used to make a
     * cross
     */
    public Cluster getCluster2() {
        return _clus2;
    }

    public Cross getMatchedZCross() {
        return _MatchedZCross;
    }

    public void setMatchedZCross(Cross _MatchedZCross) {
        this._MatchedZCross = _MatchedZCross;
    }

    public Cross getMatchedCCross() {
        return _MatchedCCross;
    }

    public void setMatchedCCross(Cross _MatchedCCross) {
        this._MatchedCCross = _MatchedCCross;
    }
    
    public void reset() {
        this.setDir(null);
        this.setDirErr(null);
        if(this.getDetector()==DetectorType.BST)
            this.updateSVTCross(null);
        else
            this.updateBMTCross(null, null);
    }

    public void update(Point3D trackPos, Vector3D trackDir) {
        if(this.getDetector()==DetectorType.BST)
            this.updateSVTCross(trackDir);
        else
            this.updateBMTCross(trackPos, trackDir);
    }
    /**
     * Sets the cross parameters: the position and direction unit vector
     */
    public void updateBMTCross(Point3D trackPos, Vector3D trackDir) {
        
        if(this.getDetector()!=DetectorType.BMT) return;

        Point3D  crossPoint = this.getBMTCrossPoint(trackPos);
        Vector3D crossError = this.getBMTCrossError(trackPos);
        if (trackPos == null) {
            this.setPoint0(crossPoint);
            this.setPointErr0(crossError.toPoint3D());
        }
        this.setPoint(crossPoint);
        this.setPointErr(crossError.toPoint3D());
        this.setDir(trackDir);
    }

    private Point3D getBMTCrossPoint(Point3D trackPos) {
        Cluster cluster = this.getCluster1();
        
        Point3D cross = cluster.center();
        
        if(trackPos!=null) {
            Point3D local = new Point3D(trackPos);
            Point3D orig  = new Point3D(cluster.origin());
            cluster.getSeedStrip().toLocal().apply(local);
            cluster.getSeedStrip().toLocal().apply(orig);
            if(this.getType()==BMTType.C) {
                double phi  = Math.atan2(local.y(), local.x());
                double phi0 = Math.atan2(orig.y(), orig.x());
                double t = phi-phi0;
                if(Math.abs(t)>Math.PI) t-=Math.signum(t)*2*Math.PI;
                if(t<0) 
                    cross = cluster.origin();
                else if(t>cluster.getArc().theta())
                    cross = cluster.end();
                else {
                    cross = cluster.getArc().point(t);
                }
            }
            else {
                cross = cluster.getLine().distanceSegment(trackPos).origin();
            }
        }
        return cross;
    }   
    

    private Vector3D getBMTCrossError(Point3D trackPos) {
        Cluster cluster = this.getCluster1();
        Point3D cross   = this.getBMTCrossPoint(trackPos);
        
        Point3D local = new Point3D(cross);
        cluster.getSeedStrip().toLocal().apply(local);
        
        Vector3D error = new Vector3D(cluster.getS());
        error.scale(cluster.getResolution());
        error.setXYZ(Math.abs(error.x()), Math.abs(error.y()), Math.abs(error.z()));
        return error;
    }   
    
    /**
     * Sets the cross parameters: the position and direction unit vector
     * @param trackDir
     */
    public void updateSVTCross(Vector3D trackDir) {

        Cluster inlayerclus  = this.getCluster1();
        Cluster outlayerclus = this.getCluster2();
        if (inlayerclus == null || outlayerclus == null) { 
            return;
        }
        // RDV: z error is now smaller because resulting from strip resolution instead of +/- 1 strip
        Point3D  crossPoint = this.getSVTCrossPoint(trackDir);
        Vector3D crossError = this.getSVTCrossError(trackDir);
        
        if(crossPoint==null || crossError==null) {
            return;
        }
        
        if (trackDir == null) {
            this.setPoint0(crossPoint);
            this.setPointErr0(crossError.toPoint3D());
        }

        this.setPoint(crossPoint);
        this.setDir(trackDir);
        this.setPointErr(crossError.toPoint3D());

    }

    /**
     * Calculate the cross point from the two strips and the track direction
     * @param trackDir track direction
     * @return
     */
    public Point3D getSVTCrossPoint(Vector3D trackDir) {
        
        int layer  = this.getCluster1().getLayer();
        int sector = this.getCluster1().getSector();
        
        Point3D cross = Constants.getInstance().SVTGEOMETRY.getCross(sector, layer, this.getCluster1().getLine(), this.getCluster2().getLine(), trackDir);
  
        return cross;
    }

    /**
     * Calculate the cross position error from the two strips and the track direction
     * @param trackDir track direction
     * @return
     */
    public Vector3D getSVTCrossError(Vector3D trackDir) {
        Vector3D error = null;
       
        int layer  = this.getCluster1().getLayer();
        int sector = this.getCluster1().getSector();

        Point3D cross = this.getSVTCrossPoint(trackDir);
        if(cross!=null) {
            // get the strip resolution
            Point3D local = Constants.getInstance().SVTGEOMETRY.toLocal(layer, sector, cross);
            double sigma1 = Constants.getInstance().SVTGEOMETRY.getSingleStripResolution(layer, this.getCluster1().getSeedStrip().getStrip(), local.z());
            double sigma2 = Constants.getInstance().SVTGEOMETRY.getSingleStripResolution(layer, this.getCluster2().getSeedStrip().getStrip(), local.z());
            
            // get the error associated to each strip
            Vector3D error1 = this.getSVTCrossDerivative(1, trackDir).multiply(sigma1);
            Vector3D error2 = this.getSVTCrossDerivative(2, trackDir).multiply(sigma2);
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
     * @return
     */
    public Vector3D getSVTCrossDerivative(int icluster, Vector3D trackDir) {
        Vector3D error = null;
       
        // check the cluster to be used in the derivative calculation is either 1 or 2
        if(icluster<1 || icluster>2) return null;

        // if the croos position is not well defined, don't do anything
        Point3D cross = this.getSVTCrossPoint(trackDir);
        if(cross==null) return null;
         
        int layer  = this.getCluster1().getLayer();
        int sector = this.getCluster1().getSector();
        
        double delta = 1e-3; // 1micron shift
        
        // get the clusters
        Cluster clusA = this.getCluster1();
        Cluster clusB = this.getCluster2();
        if(icluster==2) {
            clusA = this.getCluster2();
            clusB = this.getCluster1();           
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
        if(clusA.getLayer()%2 == 1) {
            crossAPlus  = Constants.getInstance().SVTGEOMETRY.getCross(sector, layer, stripAPlus,  clusB.getLine(), trackDir);
            crossAMinus = Constants.getInstance().SVTGEOMETRY.getCross(sector, layer, stripAMinus, clusB.getLine(), trackDir);
        }
        else {
            crossAPlus  = Constants.getInstance().SVTGEOMETRY.getCross(sector, layer, clusB.getLine(),  stripAPlus, trackDir);
            crossAMinus = Constants.getInstance().SVTGEOMETRY.getCross(sector, layer, clusB.getLine(), stripAMinus, trackDir);
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
        String s = " cross:  " + this.getDetector() + " " + this.getType() + " ID " + this.getId() + " Sector " + this.getSector() 
                + " Region " + this.getRegion() + " sort "+this.getOrderedRegion()+" cosmic region "+this.getSVTCosmicsRegion();
        if(this.getPoint0()!=null) s += " Point " + this.getPoint().toString();
        if(this.getPoint0()!=null) s += " Point0 " + this.getPoint0().toString();
        if(this.getDir()!=null)    s += " Direction "+ this.getDir().toString();
        return s;
    }

    public int getSVTCosmicsRegion() {

        int theRegion = 0;
        if (this.getDetector()==DetectorType.BST) {
            if(this.getPoint0().y()<0) 
                theRegion = this.getRegion();
            else 
                theRegion = SVTGeometry.NREGIONS*2+1-this.getRegion();
        }
        
        return theRegion;
    }

    /**
     * Sorts crosses
     * @param arg
     */
    @Override
    public int compareTo(Cross arg) {

        int return_val = 0;
        if(Constants.getInstance().isCosmics) {
            int RegComp = this.getSVTCosmicsRegion() < arg.getSVTCosmicsRegion() ? -1 : this.getSVTCosmicsRegion() == arg.getSVTCosmicsRegion() ? 0 : 1;
            int IDComp = this.getId() < arg.getId() ? -1 : this.getId() == arg.getId() ? 0 : 1;

            return_val = ((RegComp == 0) ? IDComp : RegComp);
        } else {
            
            //int thisreg = (this.getDetector().equalsIgnoreCase("BMT")) ? 3 + bgeom.getLayer( this.getRegion(), this.getDetectorType()) : this.getRegion();
            //int argreg  = (arg.getDetector().equalsIgnoreCase("BMT"))  ? 3 + bgeom.getLayer( arg.getRegion(), arg.getDetectorType()) : arg.getRegion();
            int thisreg = this.getOrderedRegion();
            int argreg  = arg.getOrderedRegion();
            int RegComp = thisreg < argreg ? -1 : thisreg == argreg ? 0 : 1;
//            int RegComp = this.getRegion() < arg.getRegion() ? -1 : this.getRegion() == arg.getRegion() ? 0 : 1;
            
            // check that is not BMTC for phi comparison
            if( Double.isNaN(arg.getPoint().x())==false &&  Double.isNaN(this.getPoint().x())==false ) {
            	int PhiComp = this.getPoint0().toVector3D().phi() < arg.getPoint0().toVector3D().phi() ? -1 : this.getPoint0().toVector3D().phi() == arg.getPoint0().toVector3D().phi() ? 0 : 1;
            
            	return_val = ((RegComp == 0) ? PhiComp : RegComp);
            }
            else {
            	int ZComp = this.getPoint0().z() < arg.getPoint0().z() ? -1 : this.getPoint0().z() == arg.getPoint0().z() ? 0 : 1;
            	return_val = ((RegComp == 0) ? ZComp : RegComp);
            }
        }

        return return_val;
    }

    private int AssociatedTrackID = -1; // the track ID associated with that hit 

    public int getAssociatedTrackID() {
        return AssociatedTrackID;
    }

    public void setAssociatedTrackID(int associatedTrackID) {
        AssociatedTrackID = associatedTrackID;
    }

    /**
     * @return the Cross Radius
     */
    public double getRadius() {
        return Math.sqrt(this.getPoint().x()*this.getPoint().x()+this.getPoint().y()*this.getPoint().y());
    }

    public static void main(String arg[]) {

        // Geometry geo = new Geometry();

        ArrayList<Cross> testList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 1, 1 + i);
            c1.setPoint0(new Point3D(-1.2 - i, 66.87, 0));
            testList.add(c1);
        }
        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 3, 1 + i);
            c1.setPoint0(new Point3D(-1.2 + i, 123, 0));
            testList.add(c1);
        }

        for (int i = 0; i < 5; i++) {
            Cross c1 = new Cross(DetectorType.BST, BMTType.UNDEFINED, 1, 2, 1 + i);
            c1.setPoint0(new Point3D(-1.2 - i, 95, 0));
            testList.add(c1);
        }

        Collections.sort(testList);

        ArrayList<ArrayList<Cross>> theListsByRegion = new ArrayList<>();

        ArrayList<Cross> theRegionList = new ArrayList<>();
        if (testList.size() > 0) {
            theRegionList.add(testList.get(0)); // init
        }
        for (int i = 1; i < testList.size(); i++) {
            Cross c = testList.get(i);
            if (testList.get(i - 1).getRegion() != c.getRegion()) {
                theListsByRegion.add(theRegionList);    // end previous list by region
                theRegionList = new ArrayList<>(); // new region list
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
