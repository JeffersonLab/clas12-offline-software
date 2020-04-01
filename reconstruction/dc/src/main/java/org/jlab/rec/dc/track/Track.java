package org.jlab.rec.dc.track;

//import Jama.Matrix;
import org.jlab.jnp.matrix.*;
import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.fit.StateVecsDoca;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;

/**
 * A class representing track candidates in the DC.  A track has a trajectory represented by an ensemble of geometrical state vectors along its path, 
 * a charge and a momentum
 * @author ziegler
 *
 */
public class Track extends Trajectory implements Comparable<Track>{

    /**
     * @return the finalStateVec
     */
    public StateVec getFinalStateVec() {
        return finalStateVec;
    }

    /**
     * @param finalStateVec the finalStateVec to set
     */
    public void setFinalStateVec(StateVec finalStateVec) {
        this.finalStateVec = finalStateVec;
    }

    /**
     * serialVersionUID
     */	
    private static final long serialVersionUID = 1763744434903318419L;

    private int _Q;
    private double _P;
    private Matrix _CovMat;

    private Point3D _Region3CrossPoint;
    private Point3D _Region3CrossDir;
    private Point3D _Region1CrossPoint;
    private Point3D _Region1CrossDir;
    private Point3D _Region1TrackX;
    private Point3D _Region1TrackP;

    private StateVec _StateVecAtReg1MiddlePlane;
    private int _Id = -1;			
    private double _totPathLen;
    private Point3D _trakOrig;
    private Vector3D _pOrig;
    private Point3D _Vtx0_TiltedCS;
    private Vector3D _pAtOrig_TiltedCS;
    private String _trking;
    private int _FitNDF;
    private double _fitChisq;
    public boolean fit_Successful;
    private int _missingSuperlayer;
    private int _fitConvergenceStatus;
    private StateVec finalStateVec ;
    
    
    public Track() {
    }
    /**
     * 
     * @return missing superlayer of the track
     */
    public int get_MissingSuperlayer() {
        return _missingSuperlayer;
    }
    /**
     * 
     * @param missingSuperlayer track missing superlayer
     */
    public void set_MissingSuperlayer(int missingSuperlayer) {
        this._missingSuperlayer = missingSuperlayer;
    }
    
    private int _Status=0;

    public int get_Status() {
        return _Status;
    }

    public void set_Status(int _Status) {
        this._Status = _Status;
    }
    
    /**
     * 
     * @return id of the track
     */
    public int get_Id() {
        return _Id;
    }
    /**
     * 
     * @param _Id track id
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }
    /**
     * 
     * @return the charge
     */
    public int get_Q() {
        return _Q;
    }
    /**
     * Sets the charge
     * @param _Q the charge
     */
    public void set_Q(int _Q) {
        this._Q = _Q;
    }
    /**
     * 
     * @return the total momentum value
     */
    public double get_P() {
        return _P;
    }
    /**
     * Sets the total momentum value
     * @param _P the total momentum value
     */
    public void set_P(double _P) {
        this._P = _P;
    }
    /**
     * 
     * @return point along track trajectory between the last layer of region 3 and the TOF panel 1b
     */
    public Point3D get_PostRegion3CrossPoint() {
        return _Region3CrossPoint;
    }
    /**
     * 
     * @param point a point along track trajectory between the last layer of region 3 and the TOF panel 1b
     */
    public void set_PostRegion3CrossPoint(Point3D point) {
        _Region3CrossPoint = point;
    }
    /**
     * 
     * @return unit direction vector of the track at a point along track trajectory between the last layer of region 3 and the TOF panel 1b
     */
    public Point3D get_PostRegion3CrossDir() {
        return _Region3CrossDir;
    }
    /**
     * 
     * @param dir unit direction vector of the track at a point along track trajectory between the last layer of region 3 and the TOF panel 1b
     */
    public void set_PostRegion3CrossDir(Point3D dir) {
        _Region3CrossDir = dir;
    }
    /**
     * 
     * @return point along track trajectory at the HTCC surface sphere
     */
    public Point3D get_PreRegion1CrossPoint() {
        return _Region1CrossPoint;
    }
    /**
     * 
     * @param point a point along track trajectory at the HTCC surface sphere
     */
    public void set_PreRegion1CrossPoint(Point3D point) {
        _Region1CrossPoint = point;
    }
    /**
     * 
     * @return unit direction vector of the track at a point along track trajectory at the HTCC surface sphere
     */
    public Point3D get_PreRegion1CrossDir() {
        return _Region1CrossDir;
    }
    /**
     * 
     * @param dir unit direction vector of the track at a point along track trajectory at the HTCC surface sphere
     */
    public void set_PreRegion1CrossDir(Point3D dir) {
        _Region1CrossDir = dir;
    }
    /**
     * 
     * @return track position at region 1
     */
    public Point3D get_Region1TrackX() {
        return _Region1TrackX;
    }
    /**
     * 
     * @param _Region1TrackX track position at region 1
     */
    public void set_Region1TrackX(Point3D _Region1TrackX) {
        this._Region1TrackX = _Region1TrackX;
    }
    /**
     * 
     * @return track momentum at region 1
     */
    public Point3D get_Region1TrackP() {
        return _Region1TrackP;
    }
    /**
     * 
     * @param _Region1TrackP track momentum at region 1
     */
    public void set_Region1TrackP(Point3D _Region1TrackP) {
        this._Region1TrackP = _Region1TrackP;
    }
    /**
     * 
     * @param totPathLen total pathlength of track from vertex to last reference point on trajectory (cm)
     */
    public void set_TotPathLen(double totPathLen) {
        _totPathLen = totPathLen;
    }
    /**
     * 
     * @return total pathlength of track from vertex to last reference point on trajectory (cm)
     */
    public double get_TotPathLen() {
        return _totPathLen;
    }
    /**
     * 
     * @param trakOrig track vertex position at the distance of closest approach to the beam axis (0,0)
     */
    public void set_Vtx0(Point3D trakOrig) {
        _trakOrig = trakOrig;
    }
    /**
     * 
     * @return track vertex position at the distance of closest approach to the beam axis (0,0)
     */
    public Point3D get_Vtx0() {
        return _trakOrig;
    }
    /**
     * 
     * @param pOrig track 3-momentum at the distance of closest approach to the beam axis (0,0)
     */
    public void set_pAtOrig(Vector3D pOrig) {
        _pOrig = pOrig;
    }
    /**
     * 
     * @return track 3-momentum at the distance of closest approach to the beam axis (0,0)
     */
    public Vector3D get_pAtOrig() {
        return _pOrig;
    }

    /**
     * Method to assign a string indicated if the stage of tracking is hit-based or time-based
     * @param trking
     */
    public void set_TrackingInfoString(String trking) {
        _trking = trking;
    }
    /**
     * 
     * @return a string indicated if the stage of tracking is hit-based or time-based
     */
    public String get_TrackingInfoString() {
        return _trking;
    }
    /**
     * 
     * @param fitChisq Kalman fit chi^2
     */
    public void set_FitChi2(double fitChisq) {
        _fitChisq = fitChisq;		
    }
    /**
     * 
     * @return Kalman fit chi^2
     */
    public double  get_FitChi2() {
        return _fitChisq;		
    }
    /**
     * 
     * @return Kalman fit NDF
     */
    public int get_FitNDF() {
        return _FitNDF;
    }
    /**
     * 
     * @param _FitNDF Kalman fit NDF
     */
    public void set_FitNDF(int _FitNDF) {
        this._FitNDF = _FitNDF;
    }
    /**
     * 
     * @return Kalman fit covariance matrix
     */
    public Matrix get_CovMat() {
        return _CovMat;
    }
    /**
     * 
     * @param _CovMat Kalman fit covariance matrix
     */
    public void set_CovMat(Matrix _CovMat) {
        this._CovMat = _CovMat;
    }
    
    private double[][] _CovMatLab;
    public double[][] get_CovMatLab() {
        return _CovMatLab;
    }
    public void set_CovMatLab(double[][] _CovMatLab) {
        this._CovMatLab = _CovMatLab;
    }
    
    /**
     * 
     * @param fitConvergenceStatus fit convergence status 0 if OK, 1 if the fit exits before converging
     */
    public void set_FitConvergenceStatus(int fitConvergenceStatus) {
        this._fitConvergenceStatus = fitConvergenceStatus;
    }
    /**
     * 
     * @return fit convergence status (0 if OK, 1 if the fit exits before converging)
     */
    public int get_FitConvergenceStatus() {
        return _fitConvergenceStatus;
    }
    /**
     * 
     * @return the state vector in the tilted sector coordinate system at the mid-plane between the 2 superlayers in region 1
     */
    public StateVec get_StateVecAtReg1MiddlePlane() {
        return _StateVecAtReg1MiddlePlane;
    }
    /**
     * 
     * @param _StateVecAtReg1MiddlePlane the state vector in the tilted sector coordinate system at the mid-plane between the 2 superlayers in region 1
     */
    public void set_StateVecAtReg1MiddlePlane(StateVec _StateVecAtReg1MiddlePlane) {
        this._StateVecAtReg1MiddlePlane = _StateVecAtReg1MiddlePlane;
    }
    
    private Track _AssociatedHBTrack;
    /**
     * 
     * @param _trk associated track for Hit-Based tracking
     */
    public void set_AssociatedHBTrack(Track _trk) {
        _AssociatedHBTrack = _trk;
    }
    /**
     * 
     * @return track associated with the hit for Hit-Based tracking
     */
    public Track get_AssociatedHBTrack() {
        return _AssociatedHBTrack;
    }
    
    private List<Segment> _ListOfHBSegments = new ArrayList<Segment>();

    public List<Segment> get_ListOfHBSegments() {
        return _ListOfHBSegments;
    }

    public void set_ListOfHBSegments(List<Segment> _listOfHBSegments) {
        this._ListOfHBSegments = _listOfHBSegments;
    }
    
    private List<FittedHit> _hitsOnTrack;
    public void setHitsOnTrack(List<FittedHit> fhits) {
        _hitsOnTrack = fhits;
    }
    public List<FittedHit> getHitsOnTrack() {
        return _hitsOnTrack;
    }
    
    
    public boolean isGood() {
        boolean isGood=true;
        if(this._trakOrig.distance(0, 0, 0)>Constants.htccRadius) isGood=false;
        return isGood;
    }
    /**
     * Basic track info
     */
    public void printInfo() {
        System.out.println("Track "+this._Id+" Q= "+this._Q+" P= "+this._P);
    }
    @Override
    public int compareTo(Track arg) {
            /*
            // Sort by cross in R1, R2, R3
            int idtrk = this.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrk0 = arg.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();

            int return_val = idtrk < idtrk0   ? -1 : idtrk  == idtrk0   ? 0 : 1;		

            return return_val; 
            */
            int idtrkSeg1 = this.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg1a = arg.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg2 = this.get(0).get_Segment2().get(0).get_AssociatedHBTrackID();
            int idtrkSeg2a = arg.get(0).get_Segment2().get(0).get_AssociatedHBTrackID();

            int idtrkSeg3 = this.get(1).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg3a = arg.get(1).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg4 = this.get(1).get_Segment2().get(0).get_AssociatedHBTrackID();
            int idtrkSeg4a = arg.get(1).get_Segment2().get(0).get_AssociatedHBTrackID();

            int idtrkSeg5 = this.get(2).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg5a = arg.get(2).get_Segment1().get(0).get_AssociatedHBTrackID();
            int idtrkSeg6 = this.get(2).get_Segment2().get(0).get_AssociatedHBTrackID();
            int idtrkSeg6a = arg.get(2).get_Segment2().get(0).get_AssociatedHBTrackID();


            int return_val1 = idtrkSeg1 < idtrkSeg1a   ? -1 : idtrkSeg1  == idtrkSeg1a   ? 0 : 1;		
            int return_val2 = idtrkSeg2 < idtrkSeg2a   ? -1 : idtrkSeg2  == idtrkSeg2a   ? 0 : 1;
            int return_val3 = idtrkSeg3 < idtrkSeg3a   ? -1 : idtrkSeg3  == idtrkSeg3a   ? 0 : 1;
            int return_val4 = idtrkSeg4 < idtrkSeg4a   ? -1 : idtrkSeg4  == idtrkSeg4a   ? 0 : 1;
            int return_val5 = idtrkSeg5 < idtrkSeg5a   ? -1 : idtrkSeg5  == idtrkSeg5a   ? 0 : 1;
            int return_val6 = idtrkSeg6 < idtrkSeg6a   ? -1 : idtrkSeg6  == idtrkSeg6a   ? 0 : 1;

            int return_val_a1 = ((return_val1 ==0) ? return_val2 : return_val1);  
            int return_val_a2 = ((return_val2 ==0) ? return_val_a1 : return_val2);
            int return_val_a3 = ((return_val3 ==0) ? return_val_a2 : return_val3);
            int return_val_a4 = ((return_val4 ==0) ? return_val_a3 : return_val4);
            int return_val_a5 = ((return_val5 ==0) ? return_val_a4 : return_val5);
            int return_val_a6 = ((return_val6 ==0) ? return_val_a5 : return_val6);

            int returnSec = this.get_Sector() < arg.get_Sector() ? -1 : this.get_Sector() == arg.get_Sector() ? 0 : 1; 

            return ((returnSec ==0) ? return_val_a6 : returnSec);
    }
    private double[] q = new double[6]; // the variables in the lab
    private double[] t = new double[6]; // the variables in the tilted coordinate system (TCS)
    private double[][] C = new double[6][6]; // C' = F^T C F
    private double[][] CF = new double[5][6];
    private void reset() {
        for (int i = 0; i < 6; i++) {
            q[i] = 0;
            t[i] = 0;
            for (int j = 0; j < 6; j++) {
                C[i][j] = 0;
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                CF[i][j] = 0;
            }
        }
    }
    Matrix result = new Matrix();
    Matrix jac = new Matrix();
    Matrix jacT = new Matrix();
   /**
    * Jacobian calculations by Luca Marsicano and Mylene Caudron
    * @param sector
    * @param stateVecAtVtx
    * @param covMatAtVtx
    * @param z 
    */
    public void getCovMatToLab(int sector, StateVecsDoca.StateVec stateVecAtVtx, Matrix covMatAtVtx, double z) {
        this.reset();
        
        //the parameters in the TCS
        t[0] = stateVecAtVtx.x;
        t[1] = stateVecAtVtx.y;
        t[2] = z;
        t[3] = stateVecAtVtx.tx;
        t[4] = stateVecAtVtx.ty;
        t[5] = stateVecAtVtx.Q;
        double Q = Math.signum(stateVecAtVtx.Q);
        //the parameters in the lab
        q[0] = this.get_Vtx0().x();
        q[1] = this.get_Vtx0().y();
        q[2] = this.get_Vtx0().z();
        q[3] = this.get_pAtOrig().x();
        q[4] = this.get_pAtOrig().y();
        q[5] = this.get_pAtOrig().z();
        
        double thetaS = Math.PI/3*(sector -1);
        double thetaT = Math.toDegrees(-25.0);
        
        double cS = Math.cos(thetaS);
        double sS = Math.sin(thetaS);
        double cT = Math.cos(thetaT);
        double sT = Math.sin(thetaT);
        
        double del_xH_del_xT = cS*cT;
        double del_xH_del_yT = - sS;
        double del_xH_del_zT = cS*sT;
        
        double del_yH_del_xT = sS*cT;
        double del_yH_del_yT = cS;
        double del_yH_del_zT = sS*sT;
        
        double del_zH_del_xT = -sT;
        double del_zH_del_zT = cT;
        
        double del_pxH_del_txT = Q/t[5] *
                (cS*cT*t[4]*t[4]+sS*t[3]*t[4]-cS*sT*t[3]+cS*cT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pxH_del_tyT = Q/t[5] *
                (-sS*t[3]*t[3]-cS*cT*t[3]*t[4]-cS*sT*t[4]-sS)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pxH_del_QT = Q/(t[5]*t[5]) *
                (-cS*cT*t[3]+sS*t[4]-cS*sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_txT = Q/t[5] *
                (sS*cT*t[4]*t[4]-cS*t[3]*t[4]-sS*sT*t[3]+sS*cT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_tyT = Q/t[5] *
                (cS*t[3]*t[3]-sS*cT*t[3]*t[4]-sS*sT*t[4]+cS)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_QT = Q/(t[5]*t[5]) *
                (-sS*cT*t[3]-cS*t[4]-sS*sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_txT = Q/t[5] *
                (-sT*t[4]*t[4]-cT*t[3]-sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_tyT = Q/t[5] *
                (sT*t[3]*t[4]-cT*t[4])/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_QT = Q/(t[5]*t[5]) *
                (sT*t[3]-cT*t[4])/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double[][] F = new double[][]{
            {del_xH_del_xT, del_yH_del_xT, del_zH_del_xT, 0, 0, 0},
            {del_xH_del_yT, del_yH_del_yT, 0, 0, 0, 0},
            {0, 0, 0, del_pxH_del_txT, del_pyH_del_txT, del_pzH_del_txT},
            {0, 0, 0, del_pxH_del_tyT, del_pyH_del_tyT, del_pzH_del_tyT},
            {0, 0, 0, del_pxH_del_QT, del_pyH_del_QT, del_pzH_del_QT}};
        
        double[][] FT = new double[][]{
            {del_xH_del_xT, del_xH_del_yT, 0, 0, 0},
            {del_yH_del_xT, del_yH_del_yT, 0, 0, 0},
            {del_zH_del_xT, 0, 0, 0, 0},
            {0, 0, del_pxH_del_txT, del_pxH_del_tyT, del_pxH_del_QT},
            {0, 0, del_pyH_del_txT, del_pyH_del_tyT, del_pyH_del_QT}, 
            {0, 0, del_pzH_del_txT, del_pzH_del_tyT, del_pzH_del_QT}};
        
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i < 6; i++) {
                 for (int j = 0; j < 5; j++) {
                     CF[k][i] += F[j][i] * covMatAtVtx.get(k, j);
                }
            }
        }
        
        for (int k = 0; k < 6; k++) {
            for (int i = 0; i < 5; i++) {
                 for (int j = 0; j < 6; j++) {
                     C[k][j] += CF[i][k] * FT[j][i];
                }
            }
        }
        
        
        
        this.set_CovMatLab(C);
    }

}
