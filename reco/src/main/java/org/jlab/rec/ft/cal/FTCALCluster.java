package org.jlab.rec.ft.cal;

import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ft.cal.FTCALHit;


public class FTCALCluster extends ArrayList<FTCALHit> {

	/**
	 * A cluster in the calorimeter consists of an array of crystals that are grouped together according to 
	 * the algorithm of the CalClusterFinder class
	 */
	private static final long serialVersionUID = 1L;

	private int _clusID;			   			// cluster ID
	private int _clusSize;						// number of crystals in the cluster
	private double _clusX, _clusY;       		// <X>, <Y> moments of the cluster
	private double _clusXX, _clusYY;     		// <XX>, <YY> moments of the cluster
	private double _clusSigmaX, _clusSigmaY; 	// Sigma of the cluster in X, Y
	private double _clusRadius; 				// cluster Radius 
	private double _clusTime;      				// centroid Time value
	private double _clusEnergy;			        // total energy of the cluster including correction
	private double _clusRecEnergy;				// reconstructed cluster Energy
	private double _clusMaxEnergy;				// cluster Max Energy
	private double _clusTheta, _clusPhi;		// cluster Polar and Azimuth angles in the lab
	
	
	// constructor
	public FTCALCluster(int cid) {
		this.set_clusID(cid);
	}

	public int get_clusID() {
		return _clusID;
	}

	public void set_clusID(int _clusID) {
		this._clusID = _clusID;
	}

	public int get_clusSize() {
		return _clusSize;
	}

	public void set_clusSize(int _clusSize) {
		this._clusSize = _clusSize;
	}

	public double get_clusX() {
		return _clusX;
	}

	public void set_clusX(double clusX) {
		this._clusX = clusX;
	}

	public double get_clusY() {
		return _clusY;
	}

	public void set_clusY(double clusY) {
		this._clusY = clusY;
	}

	public double get_clusXX() {
		return _clusXX;
	}

	public void set_clusXX(double clusXX) {
		this._clusXX = clusXX;
	}

	public double get_clusYY() {
		return _clusYY;
	}

	public void set_clusYY(double clusYY) {
		this._clusYY = clusYY;
	}

	public double get_clusSigmaX() {
		return _clusSigmaX;
	}

	public void set_clusSigmaX(double clusSigmaX) {
		this._clusSigmaX = clusSigmaX;
	}

	public double get_clusSigmaY() {
		return _clusSigmaY;
	}

	public void set_clusSigmaY(double clusSigmaY) {
		this._clusSigmaY = clusSigmaY;
	}

	public double get_clusRadius() {
		return _clusRadius;
	}

	public void set_clusRadius(double clusRadius) {
		this._clusRadius = clusRadius;
	}

	public double get_clusTime() {
		return _clusTime;
	}

	public void set_clusTime(double clusTime) {
		this._clusTime = clusTime;
	}

	public double get_clusEnergy() {
		return _clusEnergy;
	}

	public void set_clusEnergy(double clusEnergy) {
		this._clusEnergy = clusEnergy;
	}

	public double get_clusRecEnergy() {
		return _clusRecEnergy;
	}

	public void set_clusRecEnergy(double _clusRecEnergy) {
		this._clusRecEnergy = _clusRecEnergy;
	}	
	
	public double get_clusMaxEnergy() {
		return _clusMaxEnergy;
	}

	public void set_clusMaxEnergy(double clusMaxEnergy) {
		this._clusMaxEnergy = clusMaxEnergy;
	}

	public double get_clusTheta() {
		return _clusTheta;
	}

	public void set_clusTheta(double clusTheta) {
		this._clusTheta = clusTheta;
	}

	public double get_clusPhi() {
		return _clusPhi;
	}

	public void set_clusPhi(double clusPhi) {
		this._clusPhi = clusPhi;
	}
	
	public boolean isgoodCluster() {
		if(_clusSize     >FTCALConstantsLoader.cluster_min_size &&
		   _clusRecEnergy>FTCALConstantsLoader.cluster_min_energy) {
			return true;
		}
		else {
			return false;
		}
	}	
	
	public static  ArrayList<FTCALCluster> getClusters(EvioDataBank bank) {

	     int[] clusID           = bank.getInt("clusID");
	     int[] clusSize         = bank.getInt("clusSize");
	     double[] clusX         = bank.getDouble("clusX");
	     double[] clusY         = bank.getDouble("clusY");
	     double[] clusXX        = bank.getDouble("clusXX");
	     double[] clusYY        = bank.getDouble("clusYY");
	     double[] clusSigmaX    = bank.getDouble("clusSigmaX");
	     double[] clusSigmaY    = bank.getDouble("clusSigmaY");
	     double[] clusRadius    = bank.getDouble("clusRadius");
	     double[] clusTime      = bank.getDouble("clusTime");
	     double[] clusEnergy    = bank.getDouble("clusEnergy");
	     double[] clusRecEnergy = bank.getDouble("clusRecEnergy");
	     double[] clusMaxEnergy = bank.getDouble("clusMaxEnergy");
	     double[] clusTheta     = bank.getDouble("clusTheta");
	     double[] clusPhi       = bank.getDouble("clusPhi");


	     int size = clusID.length;
	     ArrayList<FTCALCluster> clusters = new ArrayList<FTCALCluster>();
	      
	     for(int i = 0; i<size; i++){  
	    	 FTCALCluster cluster = new FTCALCluster(clusID[i]);	
	    	 cluster.set_clusSize(clusSize[i]);
	    	 cluster.set_clusX(clusX[i]);
	    	 cluster.set_clusY(clusY[i]);
	    	 cluster.set_clusXX(clusXX[i]);
	    	 cluster.set_clusYY(clusYY[i]);
	    	 cluster.set_clusSigmaX(clusSigmaX[i]);
	    	 cluster.set_clusSigmaY(clusSigmaY[i]);
	    	 cluster.set_clusRadius(clusRadius[i]);
	    	 cluster.set_clusTime(clusTime[i]);
	    	 cluster.set_clusEnergy(clusEnergy[i]);
	    	 cluster.set_clusRecEnergy(clusRecEnergy[i]);
	    	 cluster.set_clusMaxEnergy(clusMaxEnergy[i]);
	    	 cluster.set_clusTheta(clusTheta[i]);
	    	 cluster.set_clusPhi(clusPhi[i]);

             clusters.add(cluster); 
	          
	      }
	      return clusters;
	      
	}
	

	
}
	

	

