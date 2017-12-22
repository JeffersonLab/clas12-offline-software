package org.jlab.rec.dc.hit;

import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.List;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.utils.groups.IndexedTable;
/**
 * A hit that was used in a fitted cluster. It extends the Hit class and
 * contains local and sector coordinate information at the MidPlane. An estimate
 * for the Left-right Ambiguity is assigned based on the linear fit to the wire
 * position residual.
 *
 * @author ziegler
 *
 */
public class FittedHit extends Hit implements Comparable<Hit> {

    

    /**
     *
     * @param sector (1...6)
     * @param superlayer (1...6)
     * @param layer (1...6)
     * @param wire (1...112)
     * @param time (for gemc output without digitization)
     */
    public FittedHit(int sector, int superlayer, int layer, int wire,
            double time, double docaEr, double B, int id) {
        super(sector, superlayer, layer, wire, time, docaEr, B, id);

        this.set_lX(layer);
        this.set_lY(layer, wire);
    }

    private double _X;              	// X at Z in local coord. system
    private double _XMP;            	// X at the MidPlane in sector coord. system
    private double _Z;              	// Z in the sector coord. system
    private double _lX;				// X in local coordinate system used in hit-based fit to cluster line
    private double _lY;				// Y in local coordinate system used in hit-based fit to cluster line
    private double _Residual;			// cluster line  to the wire position resid
    private double _TimeResidual = 0;	// cluster line  to the wire position time-resid
    private int _LeftRightAmb;		// Left-Right Ambiguity value	-1 --> y-fit <0 --> to the left of the wire ==> y = y-_leftRight*TimeToDist

    private double _QualityFac;
    private int _TrkgStatus = -1;	//  TrkgStatusFlag factor (-1: no fit; 0: hit-based trking fit; 1: time-based trking fit)
    private double _ClusFitDoca = -1;
    private double _TrkFitDoca = -1;
    private double _TimeToDistance = 0;
    private double _Beta = 1.0;
    
    private StateVec _AssociatedStateVec;

    
    
    /**
     *
     * @return the local hit x-position in the local superlayer coordinate
     * system; used in cluster-finding algo. to fit the hit-based wire positions
     */
    public double get_lX() {
        return _lX;
    }

    /**
     *
     * @param layerValue layer number from 1 to 6
     */
    public void set_lX(double layerValue) {
        this._lX = layerValue;
    }

    /**
     *
     * @return the local hit y-position in the local superlayer coordinate
     * system; used in cluster-finding algo. to fit the hit-based wire positions
     */
    public double get_lY() {
        return _lY;
    }

    /**
     *
     * @param layer layer number from 1 to 6
     * @param wire wire number from 1 to 112 sets the center of the cell as a
     * function of wire number in the local superlayer coordinate system.
     */
    public void set_lY(int layer, int wire) {
        double y = this.calcLocY(layer, wire);
        this._lY = y;
    }

    /**
     *
     * @return The approximate uncertainty on the hit position using the inverse
     * of the gemc smearing function
     */
    public double get_PosErr(double B, IndexedTable constants0, IndexedTable constants1, TimeToDistanceEstimator tde) {

        double err = this.get_DocaErr();

        if (this._TrkgStatus != -1) {
            if (this.get_TimeToDistance() == 0) // if the time-to-dist is not set ... set it
            {
                set_TimeToDistance(1.0, B, constants1, tde);
            }

            err = Constants.CELLRESOL; // default
            double x = this.get_Doca() / this.get_CellSize();
            //double p1 = CCDBConstants.getPAR1()[this.get_Sector() - 1][this.get_Superlayer() - 1];
            //double p2 = CCDBConstants.getPAR2()[this.get_Sector() - 1][this.get_Superlayer() - 1];
            //double p3 = CCDBConstants.getPAR3()[this.get_Sector() - 1][this.get_Superlayer() - 1];
            //double p4 = CCDBConstants.getPAR4()[this.get_Sector() - 1][this.get_Superlayer() - 1];
            //double scale = CCDBConstants.getSCAL()[this.get_Sector() - 1][this.get_Superlayer() - 1];
            double p1 = constants0.getDoubleValue("parameter1", this.get_Sector(),this.get_Superlayer(),0);
            double p2 = constants0.getDoubleValue("parameter2", this.get_Sector(),this.get_Superlayer(),0);
            double p3 = constants0.getDoubleValue("parameter3", this.get_Sector(),this.get_Superlayer(),0);
            double p4 = constants0.getDoubleValue("parameter4", this.get_Sector(),this.get_Superlayer(),0);
            double scale = constants0.getDoubleValue("scale", this.get_Sector(),this.get_Superlayer(),0);
            
            err = (p1 + p2 / ((p3 + x) * (p3 + x)) + p4 * Math.pow(x, 8)) * scale * 0.1; //gives a reasonable approximation to the measured CLAS resolution (in cm! --> scale by 0.1 )
            
        }

        return err;
    }

    /**
     *
     * @return the time residual |fit| - |y| from the fit to the wire positions
     * in the superlayer
     */
    public double get_TimeResidual() {
        return _TimeResidual;
    }

    /**
     *
     * @param _TimeResidual the residual |fit| - |y| from the fit to the hit
     * positions in the superlayer
     */
    public void set_TimeResidual(double _TimeResidual) {
        this._TimeResidual = _TimeResidual;
    }

    /**
     *
     * @return the residual from the fit to the wire positions in the superlayer
     */
    public double get_Residual() {
        return _Residual;
    }

    /**
     *
     * @param _Residual the residual from the fit to the hit positions in the
     * superlayer
     */
    public void set_Residual(double _Residual) {
        this._Residual = _Residual;
    }

    /**
     *
     * @return an integer representative of the estimate of the left-right
     * ambiguity obtained from pattern recognition. -1(+1): the track went to
     * the left(right) of the wire; 0: the left-right ambiguity could not be
     * resolved.
     */
    public int get_LeftRightAmb() {
        return _LeftRightAmb;
    }

    /**
     *
     * @param leftRightAmb an integer representative of the estimate of the
     * left-right ambiguity obtained from pattern recognition. -1(+1): the track
     * went to the left(right) of the wire; 0: the left-right ambiguity could
     * not be resolved.
     */
    public void set_LeftRightAmb(int leftRightAmb) {
        this._LeftRightAmb = leftRightAmb;
    }

    /**
     *
     * @return a quality factor representative of the quality of the fit to the
     * hit
     */
    public double get_QualityFac() {
        return _QualityFac;
    }

    /**
     *
     * @param _QualityFac is a quality factor representative of the quality of
     * the fit to the hit
     */
    public void set_QualityFac(double _QualityFac) {
        this._QualityFac = _QualityFac;
    }

    /**
     *
     * @return an integer representative of the stage of the pattern recognition
     * and subsequent KF fit for that hit. -1: the hit has not yet been fit and
     * is the input of hit-based tracking; 0: the hit has been successfully
     * involved in hit-based tracking and has a well-defined time-to-distance
     * value; 1: the hit has been successfully involved in track fitting.
     */
    public int get_TrkgStatus() {
        return _TrkgStatus;
    }

    /**
     *
     * @param trkgStatus is an integer representative of the stage of the
     * pattern recognition and subsequent KF fit for that hit. -1: the hit has
     * not yet been fit and is the input of hit-based tracking; 0: the hit has
     * been successfully involved in hit-based tracking and has a well-defined
     * time-to-distance value; 1: the hit has been successfully involved in
     * track fitting.
     */
    public void set_TrkgStatus(int trkgStatus) {
        _TrkgStatus = trkgStatus;
    }

    /**
     *
     * @return the calculated distance (in cm) from the time (in ns)
     */
    public double get_TimeToDistance() {
        return _TimeToDistance;
    }

    /**
     *
     * @param alpha the local angle of the track
     * @return the reduced angle in radians between the range of 0 and 30 deg.
     */
    double reducedAngle(double alpha) {
        double ralpha = 0;

        ralpha = Math.abs(alpha);

        while (ralpha > Math.PI / 3.) {
            ralpha -= Math.PI / 3.;
        }
        if (ralpha > Math.PI / 6.) {
            ralpha = Math.PI / 3. - ralpha;
        }

        return ralpha;
    }

    public StateVec getAssociatedStateVec() {
        return _AssociatedStateVec;
    }

    public void setAssociatedStateVec(StateVec _AssociatedStateVec) {
        this._AssociatedStateVec = _AssociatedStateVec;
    }
    /**
     * sets the calculated distance (in cm) from the time (in ns)
     */

    public void set_TimeToDistance(double cosTrkAngle, double B, IndexedTable tab,TimeToDistanceEstimator tde) {     
        
        double distance = 0;
        int slIdx = this.get_Superlayer() - 1;
        int secIdx = this.get_Sector() - 1;
        if (_TrkgStatus != -1 && this.get_Time() > 0) {
           
            double alpha = Math.acos(cosTrkAngle);
            double ralpha = this.reducedAngle(alpha);
            double beta = this.get_Beta(); 
            double x = this.get_ClusFitDoca();
            //TimeToDistanceEstimator tde = new TimeToDistanceEstimator();
            double deltatime_beta = 0;
            
            if (x != -1) {
                //deltatime_beta = (Math.sqrt(x * x + (CCDBConstants.getDISTBETA()[this.get_Sector() - 1][this.get_Superlayer() - 1] * beta * beta) * (CCDBConstants.getDISTBETA()[this.get_Sector() - 1][this.get_Superlayer() - 1] * beta * beta)) - x) / CCDBConstants.getV0()[this.get_Sector() - 1][this.get_Superlayer() - 1];
                deltatime_beta = (Math.sqrt(x * x + (tab.getDoubleValue("distbeta", this.get_Sector(), this.get_Superlayer(),0) * beta * beta) * (tab.getDoubleValue("distbeta", this.get_Sector(), this.get_Superlayer(),0) * beta * beta)) - x) / tab.getDoubleValue("v0", this.get_Sector(), this.get_Superlayer(),0);

            }

            double correctedTime = (this.get_Time() - deltatime_beta);
            if(correctedTime<=0)
                correctedTime=0.01;

            distance = tde.interpolateOnGrid(B, Math.toDegrees(ralpha), correctedTime, secIdx, slIdx) ;
            
        }
        
        this.set_Doca(distance);
        this._TimeToDistance = distance;
    }

    public double get_ClusFitDoca() {
        return _ClusFitDoca;
    }

    public void set_ClusFitDoca(double _ClusFitDoca) {
        this._ClusFitDoca = _ClusFitDoca;
    }

    public double get_TrkFitDoca() {
        return _TrkFitDoca;
    }

    public void set_TrkFitDoca(double _TrkFitDoca) {
        this._TrkFitDoca = _TrkFitDoca;
    }

    public void fix_TimeToDistance(double cellSize) {
        this._TimeToDistance = cellSize;
    }

    /**
     *
     * @return the hit x-position at the mid-plane (y=0) in the tilted sector
     * coordinate system
     */
    public double get_X() {
        return _X;
    }

    /**
     *
     * @param _X is the hit x-position at the mid-plane (y=0) in the tilted
     * sector coordinate system
     */
    public void set_X(double _X) {
        this._X = _X;
    }

    public double get_XMP() {
        return _XMP;
    }

    public void set_XMP(double _XMP) {
        this._XMP = _XMP;
    }

    /**
     *
     * @return the hit z-position at the mid-plane (y=0) in the tilted sector
     * coordinate system
     */
    public double get_Z() {
        return _Z;
    }

    /**
     *
     * @param _Z is the hit z-position at the mid-plane (y=0) in the tilted
     * sector coordinate system
     */
    public void set_Z(double _Z) {
        this._Z = _Z;
    }

    
    /**
     * A method to update the hit position information after the fit to the
     * local coord.sys. wire positions
     */
    public void updateHitPosition(DCGeant4Factory DcDetector) {

        //double z = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(this.get_Layer()-1).getComponent(this.get_Wire()-1).getMidpoint().z();
        double z = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1).z;

        //double z1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(1).getComponent(this.get_Wire()-1).getMidpoint().z();
        //double z0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(0).getComponent(this.get_Wire()-1).getMidpoint().z();
        double z1 = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 1, this.get_Wire() - 1).z;
        double z0 = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 0, this.get_Wire() - 1).z;
        double deltaz = Math.abs(z1 - z0);
        //double xMin = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(1).getComponent(0).getMidpoint().x();
        double xMin = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 1, 0).x;

        double x = xMin + (this.get_Wire() - 1) * 2 * deltaz * Math.tan(Math.PI / 6);
        if (this.get_Layer() % 2 == 1) {
            x += deltaz * Math.tan(Math.PI / 6);
        }
        //
        //double z = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(this.get_Layer()-1).getComponent(this.get_Wire()-1).getMidpoint().z();
        //x = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(this.get_Layer()-1).getComponent(this.get_Wire()-1).getMidpoint().x();
        x = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1).x;

        //
        this.set_X(x);
        this.set_Z(z);

        

    }

    /**
     * A method to update the hit position information after the fit to the wire
     * positions employing hit-based tracking algorithms has been performed.
     */
    public void updateHitPositionWithTime(double cosTrkAngle, double B, IndexedTable tab, DCGeant4Factory DcDetector, TimeToDistanceEstimator tde) {
        if (this.get_Time() > 0) {
            this.set_TimeToDistance(cosTrkAngle, B, tab, tde);
        }

        //double z1 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(1).getComponent(this.get_Wire()-1).getMidpoint().z();
        //double z0 = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(0).getComponent(this.get_Wire()-1).getMidpoint().z();
        double z1 = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 1, this.get_Wire() - 1).z;
        double z0 = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 0, this.get_Wire() - 1).z;
        double deltaz = Math.abs(z1 - z0);
        //double xMin = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(1).getComponent(0).getMidpoint().x();
        double xMin = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, 1, 0).x;

        double x = xMin + (this.get_Wire() - 1) * 2 * deltaz * Math.tan(Math.PI / 6);

        if (this.get_Layer() % 2 == 1) {
            x += deltaz * Math.tan(Math.PI / 6);
        }

        //double z = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(this.get_Layer()-1).getComponent(this.get_Wire()-1).getMidpoint().z();
        //x = GeometryLoader.dcDetector.getSector(0).getSuperlayer(this.get_Superlayer()-1).getLayer(this.get_Layer()-1).getComponent(this.get_Wire()-1).getMidpoint().x();
        double z = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1).z;
        x = DcDetector.getWireMidpoint(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1).x;

        //this.set_X(x+this.get_LeftRightAmb()*this.get_TimeToDistance());
        double MPCorr = 1;
        if (cosTrkAngle > 0.8 & cosTrkAngle <= 1) {
            MPCorr = cosTrkAngle;
        }

        this.set_X(x + this.get_LeftRightAmb() * (this.get_TimeToDistance() / MPCorr) / Math.cos(Math.toRadians(6.)));
        this.set_Z(z);

    }

    
    /**
     *
     * @param otherHit
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public boolean isSameAs(FittedHit otherHit) {
        FittedHit thisHit = this;
        boolean cmp = false;
        if (thisHit.get_Time() == otherHit.get_Time()
                && thisHit.get_Sector() == otherHit.get_Sector()
                && thisHit.get_Superlayer() == otherHit.get_Superlayer()
                && thisHit.get_Layer() == otherHit.get_Layer()
                && thisHit.get_Wire() == otherHit.get_Wire()) {
            cmp = true;
        }
        return cmp;
    }

    /**
     *
     * @param arg0 the other hit
     * @return an int used to sort a collection of hits by layer number
     */
    public int compareTo(FittedHit arg0) {
        if (this.get_Layer() > arg0.get_Layer()) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Sets the time using TOF
     */
    public void set_Time(double t) {
        super.set_Time(t);
    }

    public String printInfo() {
        //double xr = this._X*Math.cos(Math.toRadians(25.))+this._Z*Math.sin(Math.toRadians(25.));		
        //double zr = this._Z*Math.cos(Math.toRadians(25.))-this._X*Math.sin(Math.toRadians(25.));
        String s = "DC Fitted Hit: ID " + this.get_Id() + " Sector " + this.get_Sector() + " Superlayer " + this.get_Superlayer() + " Layer " + this.get_Layer() + " Wire " + this.get_Wire() + " Time " + this.get_Time()
                + "  LR " + this.get_LeftRightAmb() + " doca " + this.get_TimeToDistance() + " updated pos  " + this._X + " clus "
                + this._AssociatedClusterID;
        return s;
    }

    private int _AssociatedClusterID = -1;
    public boolean RemoveFlag = false;

    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }

    private int _AssociatedHBTrackID;

    public void set_AssociatedHBTrackID(int _id) {
        _AssociatedHBTrackID = _id;
    }

    public int get_AssociatedHBTrackID() {
        return _AssociatedHBTrackID;
    }
    private int _AssociatedTBTrackID;

    public void set_AssociatedTBTrackID(int _id) {
        _AssociatedTBTrackID = _id;
    }

    public int get_AssociatedTBTrackID() {
        return _AssociatedTBTrackID;
    }

    // intersection of cross direction line with the hit wire (TCS)
    private Point3D CrossDirIntersWire;

    public Point3D getCrossDirIntersWire() {
        return CrossDirIntersWire;
    }

    public void setCrossDirIntersWire(Point3D CrossDirIntersWire) {
        this.CrossDirIntersWire = CrossDirIntersWire;
    }
    
    public double get_Beta() {
        return _Beta;
    }
    
    public void set_Beta(double beta) {
        _Beta = beta;
    }

    public double calc_SignalPropagAlongWire(DCGeant4Factory DcDetector) {
        
        Vector3d WireEnd;
        int end = Constants.STBLOC[this.get_Sector()-1][this.get_Superlayer()-1];
        if(end>0) {
            WireEnd = DcDetector.getWireRightend(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        } else {
            WireEnd = DcDetector.getWireLeftend(this.get_Superlayer() - 1, this.get_Layer() - 1, this.get_Wire() - 1);
        }
        
        double X = this.getCrossDirIntersWire().x();
        double Y = this.getCrossDirIntersWire().y();
        
        double r2 = (X-WireEnd.x)*(X-WireEnd.x) + (Y-WireEnd.y)*(Y-WireEnd.y);
        
        return Math.sqrt(r2);
    }
    
    private double _SignalPropagAlongWire;

    public double getSignalPropagAlongWire() {
        return _SignalPropagAlongWire;
    }

    public void setSignalPropagAlongWire(DCGeant4Factory DcDetector) {
        this._SignalPropagAlongWire = this.calc_SignalPropagAlongWire( DcDetector);
    }
    
    private double _SignalPropagTimeAlongWire;

    public double getSignalPropagTimeAlongWire() {
        return _SignalPropagTimeAlongWire;
    }

    public void setSignalPropagTimeAlongWire(DCGeant4Factory DcDetector) {
        this.setSignalPropagAlongWire( DcDetector);
        this._SignalPropagTimeAlongWire = this._SignalPropagAlongWire/(Constants.SPEEDLIGHT*0.7);
    }

    
    private double _SignalTimeOfFlight;

    public double getSignalTimeOfFlight() {
        return _SignalTimeOfFlight;
    }

    public void setSignalTimeOfFlight() {
        if(this.get_Beta()>0 && this.getAssociatedStateVec()!=null)
            this._SignalTimeOfFlight = this.getAssociatedStateVec().getPathLength()/(Constants.SPEEDLIGHT*this.get_Beta());
    }
    
    private double _T0SubTime;

    public double getT0SubTime() {
        return _T0SubTime;
    }

    public void setT0SubTime(double _T0SubTime) {
        this._T0SubTime = _T0SubTime;
    }
    
    private double _tFlight;
    private double _tProp;
    
    public double getTFlight() {
        return _tFlight;
    }

    public void setTFlight(double tFlight) {
        this._tFlight = tFlight;
    }

    public double getTProp() {
        return _tProp;
    }

    public void setTProp(double tProp) {
        this._tProp = tProp;
    }
 
    
}
