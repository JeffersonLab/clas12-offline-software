package org.jlab.rec.fmt.cluster;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;

/**
 *  A cluster in the fmt consists of an array of hits that are grouped together according to the algorithm of the ClusterFinder class
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<FittedHit> implements Comparable<Cluster>{

	private static final long serialVersionUID = 9153980362683755204L;


	private int _Sector;      							
	private int _Layer;    	 							
	private int _Id;									
	private Line3D _StripSegment ;
	
	/**
	 * 
	 * @param sector the sector 
	 * @param layer the layer 
	 * @param cid the cluster ID, an incremental integer corresponding to the cluster formed in the series of clusters
	 */
	public Cluster(int sector, int layer, int cid) {
		this._Sector = sector;
		this._Layer = layer;
		this._Id = cid;
		
		
	}
	/**
	 * 
	 * @param hit  the first hit in the list of hits composing the cluster
	 * @param cid  the id of the cluster
	 * @return an array list of hits characterized by its sector, layer and id number.
	 */
	public Cluster newCluster(Hit hit, int cid) {
			return new Cluster(hit.get_Sector(), hit.get_Layer(), cid);
	}

	/**
	 * 
	 * @return the sector of the cluster (1...24)
	 */
	public int get_Sector() {
		return _Sector;
	}
	
	/**
	 * 
	 * @param _Sector  sector of the cluster (1...24)
	 */
	public void set_Sector(int _Sector) {
		this._Sector = _Sector;
	}
	
	/**
	 * 
	 * @return the layer of the cluster (1...8)
	 */
	public int get_Layer() {
		return _Layer;
	}
	
	/**
	 * 
	 * @param _Superlayer  the layer of the cluster (1...6)
	 */
	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}
	
	/**
	 * 
	 * @return the id of the cluster
	 */
	public int get_Id() {
		return _Id;
	}
	
	/**
	 * 
	 * @param _Id  the id of the cluster
	 */
	public void set_Id(int _Id) {
		this._Id = _Id;
	}

	/**
	 * 
	 * @return region (1...4)
	 */
    public int get_Region() {
     	return (int) (this._Layer+1)/2;
     }
    
    /**
     * 
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int get_RegionSlayer() {
    	return (this._Layer+1)%2+1;
	}
    
    /**
     * 
     * @return cluster info. about location and number of hits contained in it
     */
	public String printInfo() {
		String s = "fmt cluster: ID "+this.get_Id()+" Seed "+this.get_SeedStrip()+" Layer "+this.get_Layer()+" Size "+this.size();
		return s;
	}
	
	
	
	private double _TotalEnergy;
        private double _Centroid;
	private double _CentroidError;

	public double get_Centroid() {
		return _Centroid;
	}
	public void set_Centroid(double _Centroid) {
		this._Centroid = _Centroid;
	}
	public double get_CentroidError() {
		return _CentroidError;
	}
	public void set_CentroidError(double _CentroidError) {
		this._CentroidError = _CentroidError;
	}
	public double get_TotalEnergy() {
		return _TotalEnergy;
	}
	public void set_TotalEnergy(double _TotalEnergy) {
		this._TotalEnergy = _TotalEnergy;
	}
	/**
	 * sets energy-weighted  parameters; these are the strip centroid (energy-weighted) value, the energy-weighted phi for Z detectors and the energy-weighted z for C detectors
	 */
	public void calc_CentroidParams() {
		// instantiation of variables
		double stripNumCent = 0;					// cluster Lorentz-angle-corrected energy-weighted strip = centroid
		
		double xCentEndPoint1 = 0;					// cluster energy-weighted Centroid x coordinate of the first end-point
		double yCentEndPoint1 = 0;					// cluster energy-weighted Centroid y coordinate of the first end-point
		double zCentEndPoint1 = 0;					// cluster energy-weighted Centroid z coordinate of the first end-point
		double xCentEndPoint2 = 0;					// cluster energy-weighted Centroid x coordinate of the second end-point
		double yCentEndPoint2 = 0;					// cluster energy-weighted Centroid y coordinate of the second end-point
		double zCentEndPoint2 = 0;					// cluster energy-weighted Centroid z coordinate of the second end-point
		
		double totEn = 0.;							// cluster total energy
		double weightedStrp = 0;					// energy-weighted strip 
		
		double weightedStripEndPoint1X = 0;			// Energy-weighted x of the strip first end point
		double weightedStripEndPoint1Y = 0;			// Energy-weighted y of the strip first end point
		double weightedStripEndPoint1Z = 0;			// Energy-weighted z of the strip first end point
		double weightedStripEndPoint2X = 0;			// Energy-weighted x of the strip second end point
		double weightedStripEndPoint2Y = 0;			// Energy-weighted y of the strip second end point
		double weightedStripEndPoint2Z = 0;			// Energy-weighted z of the strip second end point
		
		int nbhits = this.size();
		
		if(nbhits != 0) {
			int min = 1000000;
			int max = -1;
			int seed = -1;
			double Emax = -1;
			// looping over the number of hits in the cluster
			for(int i=0;i<nbhits;i++) {
                            FittedHit thehit = this.get(i);
                            // gets the energy value of the strip
                            double strpEn = thehit.get_Edep();
                            // get strip informations
                            int strpNb = thehit.get_Strip();
                            double x1 = thehit.get_StripSegment().origin().x();
                            double y1 = thehit.get_StripSegment().origin().y();
                            double z1 = thehit.get_StripSegment().origin().z();
                            double x2 = thehit.get_StripSegment().end().x();
                            double y2 = thehit.get_StripSegment().end().y();
                            double z2 = thehit.get_StripSegment().end().z();
				
			    totEn += strpEn;
			    weightedStrp+= strpEn*(double)Constants.FVT_stripsYloc[strpNb-1][0];	
			    weightedStripEndPoint1X+= strpEn*x1;
			    weightedStripEndPoint1Y+= strpEn*y1;
			    weightedStripEndPoint1Z+= strpEn*z1;
			    weightedStripEndPoint2X+= strpEn*x2;
			    weightedStripEndPoint2Y+= strpEn*y2;
			    weightedStripEndPoint2Z+= strpEn*z2;
			    //System.out.println(" making a cluster with strip "+strpNb+" ref var "+Constants.FVT_stripsYloc[strpNb-1][0]);
			    // getting the max and min strip number in the cluster
			    if(strpNb<=min) 
					min = strpNb;
			    if(strpNb>=max) 
					max = strpNb;	
			    // getting the seed strip which is defined as the strip with the largest deposited energy
			    if(strpEn>=Emax) {
					Emax = strpEn;
					seed = strpNb;
				}
			    
			}
			if(totEn==0) {
				System.err.println(" Cluster energy is null .... exit");
				return;
			}

			this.set_MinStrip(min);
			this.set_MaxStrip(max);
			this.set_SeedStrip(seed);
			this.set_SeedEnergy(Emax);
			// calculates the centroid values and associated positions
			stripNumCent = weightedStrp/totEn; //System.out.println("  --> centroid "+stripNumCent);
			xCentEndPoint1 = weightedStripEndPoint1X/totEn;
			yCentEndPoint1 = weightedStripEndPoint1Y/totEn;
			zCentEndPoint1 = weightedStripEndPoint1Z/totEn;
			xCentEndPoint2 = weightedStripEndPoint2X/totEn;
			yCentEndPoint2 = weightedStripEndPoint2Y/totEn;
			zCentEndPoint2 = weightedStripEndPoint2Z/totEn;
			
		}
		
		_TotalEnergy = totEn;
		_Centroid = stripNumCent;
                _CentroidError = Math.sqrt(this.size())*Constants.FVT_Pitch/Math.sqrt(12);
		_StripSegment = new Line3D();
		_StripSegment.setOrigin(xCentEndPoint1, yCentEndPoint1, zCentEndPoint1);
		_StripSegment.setEnd(xCentEndPoint2, yCentEndPoint2, zCentEndPoint2);
	}

	
	private int _MinStrip;
	private int _MaxStrip;
	private int _SeedStrip;
	private double _SeedEnergy;
	
	
	public int get_MinStrip() {
		return _MinStrip;
	}
	public void set_MinStrip(int _MinStrip) {
		this._MinStrip = _MinStrip;
	}
	public int get_MaxStrip() {
		return _MaxStrip;
	}
	public void set_MaxStrip(int _MaxStrip) {
		this._MaxStrip = _MaxStrip;
	}
	
	
	public int get_SeedStrip() {
		return _SeedStrip;
	}
	public void set_SeedStrip(int _SeedStrip) {
		this._SeedStrip = _SeedStrip;
	}
	public double get_SeedEnergy() {
		return _SeedEnergy;
	}
	public void set_SeedEnergy(double _SeedEnergy) {
		this._SeedEnergy = _SeedEnergy;
	}
	public Line3D get_StripSegment() {
		return _StripSegment;
	}
	public void set_StripSegment(Line3D _StripSegment) {
		this._StripSegment = _StripSegment;
	}
	
	
	
	/**
	 * 
	 * @param Z  z-coordinate of a point in the local coordinate system of a module
	 * @return the average resolution for a group of strips in a cluster
	 * 
	 */
	
	
    private int _AssociatedCrossID;
    private int _AssociatedTrackID;
	
	public int get_AssociatedCrossID() {
		return _AssociatedCrossID;
	}

	public void set_AssociatedCrossID(int _AssociatedCrossID) {
		this._AssociatedCrossID = _AssociatedCrossID;
	}

	public int get_AssociatedTrackID() {
		return _AssociatedTrackID;
	}

	public void set_AssociatedTrackID(int _AssociatedTrackID) {
		this._AssociatedTrackID = _AssociatedTrackID;
	}

    @Override
    public int compareTo(Cluster arg) {
        
        return this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
        
    }
}

	

