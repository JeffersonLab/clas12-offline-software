package org.jlab.rec.dc.track;

import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;

import Jama.Matrix;

/**
 * A class representing track candidates in the DC. A track has a trajectory
 * represented by an ensemble of geometrical state vectors along its path, a
 * charge and a momentum
 *
 * @author ziegler
 *
 */
public class Track extends Trajectory implements Comparable<Track> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1763744434903318419L;

    public Track() {
    }

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

    private List<Point3D> _MicroMegasPointsList;

    private int _Id = -1;

    public int get_Id() {
        return _Id;
    }

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
     *
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
     *
     * @param _P the total momentum value
     */
    public void set_P(double _P) {
        this._P = _P;
    }

    public Point3D get_PostRegion3CrossPoint() {
        return _Region3CrossPoint;
    }

    public void set_PostRegion3CrossPoint(Point3D point) {
        _Region3CrossPoint = point;
    }

    public Point3D get_PostRegion3CrossDir() {
        return _Region3CrossDir;
    }

    public void set_PostRegion3CrossDir(Point3D dir) {
        _Region3CrossDir = dir;
    }

    public Point3D get_PreRegion1CrossPoint() {
        return _Region1CrossPoint;
    }

    public void set_PreRegion1CrossPoint(Point3D point) {
        _Region1CrossPoint = point;
    }

    public Point3D get_PreRegion1CrossDir() {
        return _Region1CrossDir;
    }

    public void set_PreRegion1CrossDir(Point3D dir) {
        _Region1CrossDir = dir;
    }

    public Point3D get_Region1TrackX() {
        return _Region1TrackX;
    }

    public void set_Region1TrackX(Point3D _Region1TrackX) {
        this._Region1TrackX = _Region1TrackX;
    }

    public Point3D get_Region1TrackP() {
        return _Region1TrackP;
    }

    public void set_Region1TrackP(Point3D _Region1TrackP) {
        this._Region1TrackP = _Region1TrackP;
    }

    public List<Point3D> get_MicroMegasPointsList() {
        return _MicroMegasPointsList;
    }

    public void set_MicroMegasPointsList(List<Point3D> _MicroMegasPointsList) {
        this._MicroMegasPointsList = _MicroMegasPointsList;
    }

    private double _totPathLen;
    private Point3D _trakOrig;
    private Vector3D _pOrig;
    private Point3D _Vtx0_TiltedCS;
    private Vector3D _pAtOrig_TiltedCS;

    public void set_TotPathLen(double totPathLen) {
        _totPathLen = totPathLen;
    }

    public void set_Vtx0(Point3D trakOrig) {
        _trakOrig = trakOrig;
    }

    public void set_pAtOrig(Vector3D pOrig) {
        _pOrig = pOrig;
    }

    public double get_TotPathLen() {
        return _totPathLen;
    }

    public Point3D get_Vtx0() {
        return _trakOrig;
    }

    public Vector3D get_pAtOrig() {
        return _pOrig;
    }
    /*
	public Point3D get_Vtx0_TiltedCS() {
		return _Vtx0_TiltedCS;
	}
	public void set_Vtx0_TiltedCS(Point3D _Vtx0_TiltedCS) {
		this._Vtx0_TiltedCS = _Vtx0_TiltedCS;
	}

	public Vector3D get_pAtOrig_TiltedCS() {
		return _pAtOrig_TiltedCS;
	}
	public void set_pAtOrig_TiltedCS(Vector3D _pAtOrig_TiltedCS) {
		this._pAtOrig_TiltedCS = _pAtOrig_TiltedCS;
	}
     */
    private String _trking;

    /**
     * Method to assign a string indicated if the stage of tracking is hi-based
     * or time-based
     *
     * @param trking
     */
    public void set_TrackingInfoString(String trking) {
        _trking = trking;
    }

    public String get_TrackingInfoString() {
        return _trking;
    }

    private int _FitNDF;
    private double _fitChisq;
    public boolean fit_Successful;
    public int status;

    public void set_FitChi2(double fitChisq) {
        _fitChisq = fitChisq;
    }

    public double get_FitChi2() {
        return _fitChisq;
    }

    public int get_FitNDF() {
        return _FitNDF;
    }

    public void set_FitNDF(int _FitNDF) {
        this._FitNDF = _FitNDF;
    }

    public Matrix get_CovMat() {
        return _CovMat;
    }

    public void set_CovMat(Matrix _CovMat) {
        this._CovMat = _CovMat;
    }

    public StateVec get_StateVecAtReg1MiddlePlane() {
        return _StateVecAtReg1MiddlePlane;
    }

    public void set_StateVecAtReg1MiddlePlane(StateVec _StateVecAtReg1MiddlePlane) {
        this._StateVecAtReg1MiddlePlane = _StateVecAtReg1MiddlePlane;
    }

    public void printInfo() {
        System.out.println("Track in sector " + this.get_Sector() + " p = " + this.get_P() + " --> " + " P = " + this.get_pAtOrig().mag() + "(" + this.get_pAtOrig().toString()
                + ") Vtx = " + "(" + this.get_Vtx0()
                + ") q = " + this.get_Q() + " pathLength = " + this.get_TotPathLen() + " chi^2 " + this.get_FitChi2());
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

        int return_val1 = idtrkSeg1 < idtrkSeg1a ? -1 : idtrkSeg1 == idtrkSeg1a ? 0 : 1;
        int return_val2 = idtrkSeg2 < idtrkSeg2a ? -1 : idtrkSeg2 == idtrkSeg2a ? 0 : 1;
        int return_val3 = idtrkSeg3 < idtrkSeg3a ? -1 : idtrkSeg3 == idtrkSeg3a ? 0 : 1;
        int return_val4 = idtrkSeg4 < idtrkSeg4a ? -1 : idtrkSeg4 == idtrkSeg4a ? 0 : 1;
        int return_val5 = idtrkSeg5 < idtrkSeg5a ? -1 : idtrkSeg5 == idtrkSeg5a ? 0 : 1;
        int return_val6 = idtrkSeg6 < idtrkSeg6a ? -1 : idtrkSeg6 == idtrkSeg6a ? 0 : 1;

        int return_val_a1 = ((return_val1 == 0) ? return_val2 : return_val1);
        int return_val_a2 = ((return_val2 == 0) ? return_val_a1 : return_val2);
        int return_val_a3 = ((return_val3 == 0) ? return_val_a2 : return_val3);
        int return_val_a4 = ((return_val4 == 0) ? return_val_a3 : return_val4);
        int return_val_a5 = ((return_val5 == 0) ? return_val_a4 : return_val5);
        int return_val_a6 = ((return_val6 == 0) ? return_val_a5 : return_val6);

        int returnSec = this.get_Sector() < arg.get_Sector() ? -1 : this.get_Sector() == arg.get_Sector() ? 0 : 1;

        return ((returnSec == 0) ? return_val_a6 : returnSec);
    }

}
