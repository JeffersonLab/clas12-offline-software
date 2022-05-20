package org.jlab.rec.dc.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.clas.clas.math.FastMath;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

/**
 * A class to describe segment objects, where a Segment is a fitted cluster that
 * has been pruned of hits with bad residuals
 *
 * @author ziegler
 *
 */
public class Segment extends ArrayList<FittedHit> implements Comparable<Segment>,
        Cloneable {

    private static final Logger LOGGER = Logger.getLogger(Segment.class.getName());

    private static final long serialVersionUID = -997960312423538455L;
    private FittedCluster _fittedCluster;
    public boolean isOnTrack = false;


    private int     _Sector;      						// sector[1...6]
    private int     _Superlayer;    	 					// superlayer [1,...6]
    private int     _Id;							// cluster Id
    private double  _ResiSum;                                                   // sum of residuals for hits in segment
    private double  _TimeSum;                                                   // sum of times for hits in segment
    private Plane3D _fitPlane;
    private SegmentTrajectory _Trajectory;
    private int _Status = 1;
    private double[] _SegmentEndPoints;
    public int associatedCrossId = -1;
    
    @Override
    public Object clone(){  
        Segment segClone = (Segment)super.clone();
        segClone._ResiSum = this._ResiSum;                                                   // sum of residuals for hits in segment
        segClone._TimeSum = this._TimeSum;                                                   // sum of times for hits in segment
        segClone._fitPlane = this._fitPlane;
        segClone._Trajectory = this._Trajectory;
        segClone._Status = this._Status;
        segClone._SegmentEndPoints = this._SegmentEndPoints;
        segClone.associatedCrossId = this.associatedCrossId;
        segClone.isOnTrack = this.isOnTrack;
        return segClone;
    }
	
    /**
     * Construct the segment from the fitted cluster.
     *
     * @param fCluster the fitted Cluster
     */
    public Segment(FittedCluster fCluster) {
        for (int i = 0; i < fCluster.size(); i++) {
            this.add(fCluster.get(i));
        }
        this.set_fittedCluster(fCluster);
        this._Sector = fCluster.get_Sector();
        this._Superlayer = fCluster.get_Superlayer();
        this._Id = fCluster.get_Id();
        this.set_Status(Status());
    }
    
    public final int Status() {
        int stat = 0;    
        
        int L[] = new int[6];
        for (FittedHit aThi : this) {
            L[aThi.get_Layer() - 1]++;
        }
        for(int l = 0; l<6; l++) {
            if(L[l]==0 || L[l]>2)
                stat=1;
        }
        return stat;
    }
    
    /**
     *
     * @return the fitted cluster
     */
    public FittedCluster get_fittedCluster() {
        return _fittedCluster;
    }
    /**
     * Sets the fitted cluster
     *
     * @param _fittedCluster the fitted cluster
     */
    public final void set_fittedCluster(FittedCluster _fittedCluster) {
        this._fittedCluster = _fittedCluster;
    }

    /**
     *
     * @return the segment sector (1...6)
     */
    public int get_Sector() {
        return _Sector;
    }

    /**
     * Sets the segment sector
     *
     * @param _Sector the segment sector (1...6)
     */
    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the segment superlayer (1...6)
     */
    public int get_Superlayer() {
        return _Superlayer;
    }

    /**
     * Sets the superlayer
     *
     * @param _Superlayer the superlayer (1...6)
     */
    public void set_Superlayer(int _Superlayer) {
        this._Superlayer = _Superlayer;
    }

    /**
     *
     * @return the segment ID, where the id corresponds to the index in the
     * sequence of found segments
     */
    public int get_Id() {
        return _Id;
    }

    /**
     * Sets the segment ID
     *
     * @param _Id the segment ID
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...3)
     */
    public int get_Region() {
        return (this._Superlayer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...3)
     */
    public int get_RegionSlayer() {
        return (this._Superlayer + 1) % 2 + 1;
    }

    /**
     * 
     * @return sum of residuals for all hits in segment
     */
    public double get_ResiSum() {
        return _ResiSum;
    }

    /**
     * 
     * @param _ResiSum sum of residuals for all hits in segment
     */
    public void set_ResiSum(double _ResiSum) {
        this._ResiSum = _ResiSum;
    }

    /**
     * 
     * @return sum of the corrected (T0-subtracted) times of all hits in segment
     */
    public double get_TimeSum() {
        return _TimeSum;
    }

    /**
     * 
     * @param _TimeSum sum of the corrected (T0-subtracted) times of all hits in segment
     */
    public void set_TimeSum(double _TimeSum) {
        this._TimeSum = _TimeSum;
    }
    
    /**
     *
     * @param otherseg matching cluster in other superlayer in a region
     * @return a region-segment proximity condition
     */
    public boolean isCloseTo(Segment otherseg) {
        /// A region-segment contains two segments if they are in the same sector 
        /// and region and satisfy the proximity condition: \n\n
        /// <center><b>|Xwires<sub>2</sub>-Xwires<sub>1</sub>| = a*Xwires<sub>1</sub> + b</b></center>\n
        /// where a and b are DC parameters set by DC_RSEG_a and DC_RSEG_B .\n\n
        boolean value = false;
        //LOGGER.log(Level.FINE, "in Segment DeltaW "+Math.abs(this.getAvgwire()-otherseg.getAvgwire() )+
        //	" < ? "+(Constants.DC_RSEG_A * this.getAvgwire() + Constants.DC_RSEG_B));
        if (Math.abs(this.getAvgwire() - otherseg.getAvgwire()) < Constants.DC_RSEG_A * this.getAvgwire() + Constants.DC_RSEG_B) {
            value = true;
        }
        
        return value;
    }
    
    public boolean hasNoMatchingSegment(List<Segment> othersegs) {
        // if superlayer%2==0 (even) check superlayer%2==1 in the same region
        //
        /// A region-segment contains two segments if they are in the same sector 
        /// and region and satisfy the proximity condition: \n\n
        /// <center><b>|Xwires<sub>2</sub>-Xwires<sub>1</sub>| = a*Xwires<sub>1</sub> + b</b></center>\n
        /// where a and b are DC parameters set by DC_RSEG_a and DC_RSEG_B .\n\n
        boolean value = true;
        //LOGGER.log(Level.FINE, "in Segment DeltaW "+Math.abs(this.getAvgwire()-otherseg.getAvgwire() )+
        //	" < ? "+(Constants.DC_RSEG_A * this.getAvgwire() + Constants.DC_RSEG_B));
        for(Segment otherseg : othersegs) {
            if (Math.abs(this.getAvgwire() - otherseg.getAvgwire()) < Constants.DC_RSEG_A * this.getAvgwire() + Constants.DC_RSEG_B) {
                value = false; // found a matching segment
            }
        }
        
        return value;
    }

    public boolean hasConsistentSlope(Segment otherseg) {
        boolean value = false;

        if (this.get_fitPlane() != null && otherseg.get_fitPlane() != null) { 
            if (Math.abs(Math.toDegrees(Math.acos(this.get_fitPlane().normal().dot(otherseg.get_fitPlane().normal()))) - 12.) < Constants.SEGMENTPLANESANGLE) // the angle between the plane normals is 12 degrees with some tolerance
            {
                value = true;
            }
        }

        return value;
    }

    /**
     *
     * @return the average wire number for the segment (this is used in the
     * proximity condition employed in segment matching)
     */
    public double getAvgwire() {
        double avewire = 0;
        int hSize = this.size();
        for (int h = 0; h < hSize; h++) {
            avewire += this.get(h).get_Wire();
        }
        return ((double) avewire / hSize);
    }


    /**
     *
     * @return the plane containing the segment fitted-line representation
     */
    public Plane3D get_fitPlane() {
        return _fitPlane;
    }

    /**
     * Sets the segment endpoints in the sector coordinate system for ced
     * display
     * @param DcDetector
     */
    public void set_SegmentEndPointsSecCoordSys(DCGeant4Factory DcDetector) {

        //double Z_1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(0).getComponent(0).getMidpoint().z();
        double Z_1 = DcDetector.getWireMidpoint(this.get_Sector() - 1, this.get_Superlayer() - 1, 0, 0).z;
        double X_1 = this.get_fittedCluster().get_clusterLineFitSlope() * Z_1 + this.get_fittedCluster().get_clusterLineFitIntercept();

        double x1 = FastMath.cos(Math.toRadians(25.)) * X_1 + FastMath.sin(Math.toRadians(25.)) * Z_1;
        double z1 = -FastMath.sin(Math.toRadians(25.)) * X_1 + FastMath.cos(Math.toRadians(25.)) * Z_1;

        //double Z_2 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(5).getComponent(0).getMidpoint().z();
        double Z_2 = DcDetector.getWireMidpoint(this.get_Sector() - 1, this.get_Superlayer() - 1, 5, 0).z;
        double X_2 = this.get_fittedCluster().get_clusterLineFitSlope() * Z_2 + this.get_fittedCluster().get_clusterLineFitIntercept();

        double x2 = FastMath.cos(Math.toRadians(25.)) * X_2 + FastMath.sin(Math.toRadians(25.)) * Z_2;
        double z2 = -FastMath.sin(Math.toRadians(25.)) * X_2 + FastMath.cos(Math.toRadians(25.)) * Z_2;

        double[] EndPointsArray = new double[4];
        EndPointsArray[0] = x1;
        EndPointsArray[1] = z1;
        EndPointsArray[2] = x2;
        EndPointsArray[3] = z2;

        this.set_SegmentEndPoints(EndPointsArray);
    }

    /**
     * Sets the plane containing the segment fitted-line representation
     * @param DcDetector
     */
    public void set_fitPlane(DCGeant4Factory DcDetector) {
        if (this.get_fittedCluster().get_clusLine() == null) {
            System.err.println(" no clusterline for " + this.get_fittedCluster().printInfo());
            
            return;
        }
        this.set_SegmentEndPointsSecCoordSys(DcDetector);
        double dir_x = this.get_fittedCluster().get_clusLine().end().x() - this.get_fittedCluster().get_clusLine().origin().x();
        double dir_y = this.get_fittedCluster().get_clusLine().end().y() - this.get_fittedCluster().get_clusLine().origin().y();
        double dir_z = this.get_fittedCluster().get_clusLine().end().z() - this.get_fittedCluster().get_clusLine().origin().z();

        double dir = Math.sqrt(dir_x * dir_x + dir_y * dir_y + dir_z * dir_z);

        dir_x /= dir;
        dir_y /= dir;
        dir_z /= dir;

        Vector3D dirVec = new Vector3D(dir_x, dir_y, dir_z);

        this._fitPlane = calc_fitPlane(this.get_fittedCluster().get_clusLine().origin(),
                dirVec);
    }

    public double[] get_SegmentEndPoints() {
        return _SegmentEndPoints;
    }

    public void set_SegmentEndPoints(double[] _SegmentEndPoints) {
        this._SegmentEndPoints = _SegmentEndPoints;
    }

    /**
     *
     * @param refPoint the reference point on the segment plane
     * @param refDir the normal vector on the segment plane
     * @return the plane containing the segment line, the plane is characterized
     * by a point on the plane and a unit normal vector.
     */
    private Plane3D calc_fitPlane(Point3D refPoint, Vector3D refDir) {

        double X = Math.pow(-1, (this.get_Superlayer() - 1)) * FastMath.sin(Math.toRadians(6));
        double Y = FastMath.cos(Math.toRadians(6.));

        Vector3D plDir = new Vector3D(X, Y, 0);

        Vector3D normDir = plDir.cross(refDir);

        if (normDir.mag() > 1.e-10) {
            normDir.scale(1. / normDir.mag());
        } else {
            return new Plane3D(new Point3D(0, 0, 0), new Vector3D(0, 0, 0));
        }
        Plane3D fitPlane = new Plane3D(refPoint, normDir);

        return fitPlane;
    }

    /**
     * 
     * @return segment trajectory
     */
    public SegmentTrajectory get_Trajectory() {
        return _Trajectory;
    }
    /**
     * 
     * @param _Trajectory segment trajectory
     */
    public void set_Trajectory(SegmentTrajectory _Trajectory) {
        this._Trajectory = _Trajectory;
    }
    
    /**
     * 
     * @return word describing segment status (not yet used)
     */
    public int get_Status() {
        return _Status;
    }
    /**
     * 
     * @param _Status segment status word
     */
    public final void set_Status(int _Status) {
        this._Status = _Status;
    }

    /**
     *
     * @return the segment info.
     */
    public String printInfo() {
        String s = "Segment: ID " + this.get_Id() + " Sector " + this.get_Sector() + " Superlayer " + this.get_Superlayer() + " Size " + this.size();
        return s;
    }

    @Override
    public int compareTo(Segment arg) {

        // int return_val = 0 ;
        int CompSec = this._Sector < arg._Sector ? -1
                : this._Sector == arg._Sector ? 0 : 1;
        int CompLay = this._Superlayer < arg._Superlayer ? -1
                : this._Superlayer == arg._Superlayer ? 0 : 1;
        int CompId = this._Id < arg._Id ? -1
                : this._Id == arg._Id ? 0 : 1;

        int return_val1 = ((CompId == 0) ? CompLay : CompId);
        int return_val2 = ((CompSec == 0) ? return_val1 : CompSec);

        return return_val2;
    }

}
