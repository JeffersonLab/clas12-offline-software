package org.jlab.rec.dc.track;


import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.trajectory.StateVec;
import org.jlab.rec.dc.trajectory.Trajectory;

import Jama.Matrix;

/**
 * A class representing track candidates in the DC.  A track has a trajectory represented by an ensemble of geometrical state vectors along its path, 
 * a charge and a momentum
 * @author ziegler
 *
 */
public class Track extends Trajectory implements Comparable<Track>{

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
	
	private StateVec _StateVecAtReg1MiddlePlane;
	
	private  List<Point3D> _MicroMegasPointsList;
	
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
	public Point3D get_Region3CrossPoint() {
		return _Region3CrossPoint;
	}
	public void set_Region3CrossPoint() {
		Point3D point = this.get(2).get_Point();
		_Region3CrossPoint = this.get(2).getCoordsInLab(point.x(),point.y(),point.z());
	}

	public Point3D get_Region3CrossDir() {
		return _Region3CrossDir;
	}
	public void set_Region3CrossDir() {
		Point3D dir = this.get(2).get_Dir();
		
		_Region3CrossDir = this.get(2).getCoordsInLab(dir.x(),dir.y(),dir.z());
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

	private String _trking;
	/**
	 * Method to assign a string indicated if the stage of tracking is hi-based or time-based
	 * @param trking
	 */
	public void set_TrackingInfoString(String trking) {
		_trking = trking;
	}
	
	public String get_TrackingInfoString() {
		return _trking;
	}
	
	
	
	private double _fitChisq;
	public boolean fit_Successful;
	
	
	
	public void set_FitChi2(double fitChisq) {
		_fitChisq = fitChisq;		
	}
	
	public double  get_FitChi2() {
		return _fitChisq;		
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
		System.out.println("Track in sector "+this.get_Sector()+" p = "+this.get_P()+" --> "+" P = "+this.get_pAtOrig().mag()+ "("+this.get_pAtOrig().toString()+
				") Vtx = "+ "("+this.get_Vtx0()+
				") q = "+this.get_Q()+" pathLength = "+this.get_TotPathLen()+" chi^2 "+this.get_FitChi2());
	}
	@Override
	public int compareTo(Track arg) {
		// Sort by cross in R1, R2, R3
		int idtrk = this.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
		int idtrk0 = arg.get(0).get_Segment1().get(0).get_AssociatedHBTrackID();
		 
		int return_val = idtrk < idtrk0   ? -1 : idtrk  == idtrk0   ? 0 : 1;		
		
		return return_val;
	}
	
}
