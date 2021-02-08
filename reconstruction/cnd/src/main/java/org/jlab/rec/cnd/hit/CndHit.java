package org.jlab.rec.cnd.hit;

import java.util.ArrayList;



public class CndHit extends ArrayList<HalfHit> implements Comparable<CndHit>{

	/**
	 * A CndHit consists of an array of particle hits in the CND reconstructed according to 
	 * the algorithm of the CndHitFinder class
	 */

	private static final long serialVersionUID = 1L;    // What is this??

	private int _pad_d;   				  // index, to the half-hit array list, of the "direct" half-hit 
	private int _pad_n;			          // index, to the half-hit array list, of the "indirect", neighbour half-hit 
	private double _T;       	          // reconstructed time of hit
	private double _Z;       	          // z co-ordinate of hit (wrt target center)
	private double _X;       	          // x co-ordinate of hit (wrt target center)
	private double _Y;       	          // y co-ordinate of hit (wrt target center)
	private double _tX;       	          // x co-ordinate of track wrt to middle of the counter	
	private double _tY;       	          // y co-ordinate of track wrt to middle of the counter
	private double _tZ;       	          // z co-ordinate of track wrt to middle of the counter
	private double _uX;					  // uncertainty in hit x coordinate
	private double _uY;					  // uncertainty in hit y coordinate
	private double _uZ;					  // uncertainty in hit z coordinate
	private double _pathlength;       	      // reconstructed path length from the vertex
	private double _tLength;       	      // reconstructed path length in the hit paddle
	private double _E;       	          // reconstructed energy of hit
	private double _phi;       	          // azimuthal angle of hit (assuming hit in center of paddle width)
	private double _theta;       	      // polar angle of hit (assuming hit in center of paddle thickness)
	private int _sector, _layer, _component; 
	private int _indexLadc;				  // index to match row adc and tdc to reconstructed hit (index L (resp.R) should be the same for adc and tdc)
	private int _indexRadc;
	private int _indexLtdc;				  // index to match row adc and tdc to reconstructed hit (index L (resp.R) should be the same for adc and tdc)
	private int _indexRtdc;


	// constructor
	public CndHit(int padd, int padn) {
		this._pad_d = padd;
		this._pad_n = padn;
	}

	public int index_d() {
		return _pad_d;
	}

	public int index_n() {
		return _pad_n;
	}

	public double Time() {
		return _T;
	}

	public void set_Time(double time) {
		this._T = time;
	}

	public double Z() {
		return _Z;
	}

	public double X() {
		return _X;
	}

	public double Y() {
		return _Y;
	}

	public double pathLength() {
		return _pathlength;
	}

	public double tLength() {
		return _tLength;
	}

	public void set_Z(double zpos) {
		this._Z = zpos;
	}

	public void set_X(double xpos) {
		this._X = xpos;
	}

	public void set_Y(double ypos) {
		this._Y = ypos;
	}

	public void set_pathlength(double path) {
		this._pathlength=path;
	}

	public void set_tLength(double dL) {
		this._tLength=dL;
	}

	public double Edep() {
		return _E;
	}

	public void set_Edep(double energy) {
		this._E = energy;
	}	

	public double Phi() {
		return _phi;
	}

	public void set_Phi(double phi) {
		this._phi = phi;
	}

	public double Theta() {
		return _theta;
	}

	public void set_Theta(double theta) {
		this._theta = theta;
	}

	public int Sector() {
		return _sector;
	}

	public void set_Sector(int sector) {
		this._sector = sector;
	}

	public int Layer() {
		return _layer;
	}

	public void set_Layer(int layer) {
		this._layer = layer;
	}

	public int Component() {
		return _component;
	}

	public void set_Component(int component) {
		this._component = component;
	}		

	public double indexLadc() {
		return _indexLadc;
	}

	public void set_indexLadc(int indexLadc) {
		this._indexLadc = indexLadc;
	}

	public double indexRadc() {
		return _indexRadc;
	}

	public void set_indexRadc(int indexRadc) {
		this._indexRadc = indexRadc;
	}
	
	public double indexLtdc() {
		return _indexLtdc;
	}

	public void set_indexLtdc(int indexLtdc) {
		this._indexLtdc = indexLtdc;
	}

	public double indexRtdc() {
		return _indexRtdc;
	}

	public void set_indexRtdc(int indexRtdc) {
		this._indexRtdc = indexRtdc;
	}

	public double get_tX() {
		return _tX;
	}

	public void set_tX(double _tX) {
		this._tX = _tX;
	}

	public double get_tY() {
		return _tY;
	}

	public void set_tY(double _tY) {
		this._tY = _tY;
	}

	public double get_tZ() {
		return _tZ;
	}

	public void set_tZ(double _tZ) {
		this._tZ = _tZ;
	}

	public double get_uX() {
		return _uX;
	}

	public void set_uX(double _uX) {
		this._uX = _uX;
	}

	public double get_uY() {
		return _uY;
	}

	public void set_uY(double _uY) {
		this._uY = _uY;
	}

	public double get_uZ() {
		return _uZ;
	}

	public void set_uZ(double _uZ) {
		this._uZ = _uZ;
	}

	public int compareTo(CndHit arg0) {  // Sorts into ascending time order
		if(this.Time()<arg0.Time()) {
			return 1;
		} else {
			return 0;
		}
	}

	private int _Id = -1;
	public void set_AssociatedTrkId(int id) {
		_Id = id;
	}	
	public int get_AssociatedTrkId() {
		return _Id ;
	}
    
    
    private int _AssociatedClusterID = -1;
    
    public int get_AssociatedClusterID() {
        return _AssociatedClusterID;
    }

    public void set_AssociatedClusterID(int _AssociatedClusterID) {
        this._AssociatedClusterID = _AssociatedClusterID;
    }
    
    
}
