/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.ft.FTConstants;

/**
 *
 * @author devita
 * @author filippi
 */
    public class FTTRKCluster extends ArrayList<FTTRKHit> implements Comparable<FTTRKCluster>{

	private static final long serialVersionUID = 9153980362683755204L;

	private int _Sector;
        private int _Layer;
        private int _CId;
        private double _TotalEnergy;
        private double _Centroid;
	private double _CentroidError;
        private Line3D _StripSegment ;
	
	/**
	 * 
	 * @param sector the sector 
	 * @param layer the layer 
	 * @param cid the cluster ID, an incremental integer corresponding to the cluster formed in the series of clusters
	 */
	public FTTRKCluster(int sector, int layer, int cid) {
		this._Sector = sector;
		this._Layer = layer;
		this._CId = cid;	
	}
	/**
	 * 
	 * @param hit  the first hit in the list of hits composing the cluster
	 * @param cid  the id of the cluster
	 * @return an array list of hits characterized by its sector, layer and id number.
	 */
	public FTTRKCluster newCluster(FTTRKHit hit, int cid) {
			return new FTTRKCluster(hit.get_Sector(), hit.get_Layer(), cid);
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
	 * @param _Sector  sector of the cluster (1)
	 */
	public void set_Sector(int _Sector) {
		this._Sector = _Sector;
	}
	
	/**
	 * 
	 * @return the layer of the cluster (1...4)
	 */
	public int get_Layer() {
		return _Layer;
	}
	
	/**
	 * 
	 * @param set the layer of the cluster (1...4)
	 */
	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}
	
	/**
	 * 
	 * @return the id of the cluster
	 */
	public int get_CId() {
		return _CId;
	}
	
	/**
	 * 
	 * @param _Id  the id of the cluster
	 */
	public void set_CId(int _Id) {
		this._CId = _Id;
                for(int i=0; i<this.size(); i++){
                    FTTRKHit thehit = this.get(i);
                    thehit.set_ClusterIndex(_Id);
                } 
	}

       
	/**
	 * 
	 * @return region (1,2) // 
	 */
        public int get_Region() {
               return (int) (this._Layer+1)/2;
        }
    
        /**
         * 
         * @return cluster info. about location and number of hits contained in it
         */
	public String printInfo() {
		String s = "FTTRK cluster: ID "+this.get_CId()+" Seed "+this.get_SeedStrip()+" Layer "+this.get_Layer()+" Size "+this.size();
		return s;
	}
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
	
	public void calc_CentroidParams() {
            // instantiation of variables
            double stripNumCent = 0;					// cluster Lorentz-angle-corrected energy-weighted strip = centroid
            double stripYCent = 0;                                          // ebergy-weighted y coordinate of the centroid strip

            double xCentEndPoint1 = 0;					// cluster energy-weighted Centroid x coordinate of the first end-point
            double yCentEndPoint1 = 0;					// cluster energy-weighted Centroid y coordinate of the first end-point
            double zCentEndPoint1 = 0;					// cluster energy-weighted Centroid z coordinate of the first end-point
            double xCentEndPoint2 = 0;					// cluster energy-weighted Centroid x coordinate of the second end-point
            double yCentEndPoint2 = 0;					// cluster energy-weighted Centroid y coordinate of the second end-point
            double zCentEndPoint2 = 0;					// cluster energy-weighted Centroid z coordinate of the second end-point

            double totEn = 0.;					        // cluster total energy
            double totEnergy = 0.;
            double totEnSq = 0.;                                            // sum of energies squared
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
                int Slay = -1;
                // find the number of two two strips with the highest energy deposited (to be zeroed in case of a truncated mean which excludes the two highest signals)
                // sort the FTTRKHIT list to find the maximum
                double maxEn1 = -1;
                double maxEn2 = -1;
                int maxI = -1;
                int maxI2nd = -1;
                int maxStripsForTM = FTConstants.TRK_MAX_STRIPS_FOR_TRUNCATED_MEAN; // truncated mean: minimum energy  HARDCODED 
                if(nbhits>maxStripsForTM){
                    // max energy strip
                    for(int i=0; i< nbhits; i++){
                        FTTRKHit oneHit = this.get(i);
                        double stripEn = oneHit.get_Edep();
                        if(stripEn> maxEn1){
                        maxEn1 = stripEn;
                        maxI = i;
                        }
                    }
                    // 2nd to max energy strip
                    for(int i=0; i < nbhits; i++){
                        FTTRKHit oneHit = this.get(i);
                        double stripEn = oneHit.get_Edep();
                        if(i!=maxI && stripEn> maxEn2){
                        maxEn2 = stripEn;
                        maxI2nd = i;
                        }
                    }
                }

                for(int i=0;i<nbhits;i++) {
                    FTTRKHit thehit = this.get(i);
                    // gets the energy value of the strip
                    double strpEn = thehit.get_Edep();
                    totEnergy += strpEn;
                    strpEn = 1.; // fix to 1 if no energy weighted mean is required - seems to have better efficiency
                    // if truncated mean on the maximun energy
                    if(i==maxI || i==maxI2nd && Math.random()>0.5) {continue;}
                    // get strip informations
                    int strpNb = thehit.get_Strip();
                    double x1 = thehit.get_StripSegment().origin().x();
                    double y1 = thehit.get_StripSegment().origin().y();
                    double z1 = thehit.get_StripSegment().origin().z();
                    double x2 = thehit.get_StripSegment().end().x();
                    double y2 = thehit.get_StripSegment().end().y();
                    double z2 = thehit.get_StripSegment().end().z();

                    totEn += strpEn;
                    totEnSq += strpEn*strpEn;
                    int layer = thehit.get_Layer();
                    int Slayer = thehit.get_HalfLayer(layer);
                    double y = (double)FTTRKConstantsLoader.stripsYloc[Slayer][strpNb-1][0];
                    weightedStrp+= strpEn*y;	
                    weightedStripEndPoint1X+= strpEn*x1;
                    weightedStripEndPoint1Y+= strpEn*y1;
                    weightedStripEndPoint1Z+= strpEn*z1;
                    weightedStripEndPoint2X+= strpEn*x2;
                    weightedStripEndPoint2Y+= strpEn*y2;
                    weightedStripEndPoint2Z+= strpEn*z2;
                    // getting the max and min strip number in the cluster
                    if(strpNb<=min) min = strpNb;
                    if(strpNb>=max) max = strpNb;	
                    // getting the seed strip which is defined as the strip with the largest deposited energy
                    if(strpEn>=Emax) {
                        Emax = strpEn;
                        seed = strpNb; // seed: hit with largest energy release
                    }
                    Slay = 2*Slayer;
                }
                if(totEn==0) {
                    System.err.println(" Cluster energy is null .... exit");
                    return;
                }
                this.set_MinStrip(min);
                this.set_MaxStrip(max);
                this.set_SeedStrip(seed);
                this.set_SeedEnergy(Emax);
                // calculates the centroid values and associated positions (in local RF)
                stripYCent = weightedStrp/totEn;       
                // extreme points of the strip in global RF
                xCentEndPoint1 = weightedStripEndPoint1X/totEn;
                yCentEndPoint1 = weightedStripEndPoint1Y/totEn;
                zCentEndPoint1 = weightedStripEndPoint1Z/totEn;
                xCentEndPoint2 = weightedStripEndPoint2X/totEn;
                yCentEndPoint2 = weightedStripEndPoint2Y/totEn;
                zCentEndPoint2 = weightedStripEndPoint2Z/totEn;
            }

            _TotalEnergy = totEnergy;
            double xmeanCent = (xCentEndPoint1+xCentEndPoint2)/2.;
            stripNumCent = this.get(0).get_StripNumberFromLocalY(xmeanCent, stripYCent, this.get(0).get_Layer());
            //centroid: centroid strip number in the measurement direction (local y of each layer)  
            _Centroid = stripNumCent;
            _CentroidError = FTTRKConstantsLoader.Pitch/Math.sqrt(12)*Math.sqrt(totEnSq);
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
            // set the _AssociatedCrossID index to all hits belonging to the cluster    
            for(int i=0; i<this.size(); i++){
                FTTRKHit thehit = this.get(i);
                thehit.set_CrossIndex(_AssociatedCrossID);
                this.set(i, thehit);
            }
        }

        public int get_AssociatedTrackID() {
            return _AssociatedTrackID;
        }

        public void set_AssociatedTrackID(int _AssociatedTrackID) {
            this._AssociatedTrackID = _AssociatedTrackID;
        }

        @Override
        public int compareTo(FTTRKCluster arg) {    
            return this.get_Layer() < arg.get_Layer() ? -1 : this.get_Layer() == arg.get_Layer() ? 0 : 1;
        }  
        
}
