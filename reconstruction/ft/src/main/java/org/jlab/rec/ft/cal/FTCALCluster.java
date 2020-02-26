package org.jlab.rec.ft.cal;

import java.util.ArrayList;
import org.jlab.geom.prim.Point3D;
import org.jlab.utils.groups.IndexedTable;



public class FTCALCluster extends ArrayList<FTCALHit> {

	/**
	 * A cluster in the calorimeter consists of an array of crystals that are grouped together according to 
	 * the algorithm of the CalClusterFinder class
	 */
	private static final long serialVersionUID = 1L;

	private int _clusID;			   			// cluster ID
//	private int _clusSize;						// number of crystals in the cluster
//	private double _clusX, _clusY;       		// <X>, <Y> moments of the cluster
//	private double _clusXX, _clusYY;     		// <XX>, <YY> moments of the cluster
//	private double _clusSigmaX, _clusSigmaY; 	// Sigma of the cluster in X, Y
//	private double _clusRadius; 				// cluster Radius 
//	private double _clusTime;      				// centroid Time value
//	private double _clusEnergy;			        // total energy of the cluster including correction
//	private double _clusRecEnergy;				// reconstructed cluster Energy
//	private double _clusMaxEnergy;				// cluster Max Energy
//	private double _clusTheta, _clusPhi;		// cluster Polar and Azimuth angles in the lab
	
	
	// constructor
	public FTCALCluster(int cid) {
            this.setID(cid);
	}

	public int getID() {
            return _clusID;
	}

	public void setID(int _clusID) {
            this._clusID = _clusID;
	}

	public int getSize() {
            // return number of crystals in cluster
            return this.size();
	}

	public double getEnergy() {
            // return measured energy
            double clusterEnergy = 0;
            for(int i=0; i<this.size(); i++) {
                clusterEnergy += this.get(i).get_Edep();
            }
            return clusterEnergy;
	}

        public double getFullEnergy(IndexedTable energyTable) {
            // return energy corrected for leakage and threshold effects
            double clusterEnergy  = this.getEnergy();
            int seedID = this.get(0).get_COMPONENT();
//            double energyCorr = FTCALConstantsLoader.energy_corr[0] 
//                              + FTCALConstantsLoader.energy_corr[1]*clusterEnergy 
//                              + FTCALConstantsLoader.energy_corr[2]*clusterEnergy*clusterEnergy
//                              + FTCALConstantsLoader.energy_corr[3]*clusterEnergy*clusterEnergy*clusterEnergy
//                              + FTCALConstantsLoader.energy_corr[4]*clusterEnergy*clusterEnergy*clusterEnergy*clusterEnergy;
            double  energyCorr = (energyTable.getDoubleValue("c0",1,1,seedID)
                               +  energyTable.getDoubleValue("c1",1,1,seedID)*clusterEnergy
                               +  energyTable.getDoubleValue("c2",1,1,seedID)*clusterEnergy*clusterEnergy
                               +  energyTable.getDoubleValue("c3",1,1,seedID)*clusterEnergy*clusterEnergy*clusterEnergy
                               +  energyTable.getDoubleValue("c4",1,1,seedID)*clusterEnergy*clusterEnergy*clusterEnergy*clusterEnergy
                                )/1000.;
            clusterEnergy+=energyCorr;
            return clusterEnergy;
	}

        public double getSeedEnergy() {
            // return max energy in cluster
            double seedEnergy = this.get(0).get_Edep();
            return seedEnergy;
	}
        
	public double getTime() {
            // returns energy weighted time 
            double clusterEnergy  = this.getEnergy();
            double clusterTime    = 0;
            for(int i=0; i<this.size(); i++) {
                FTCALHit hit = this.get(i);
                clusterTime += hit.get_Edep()*hit.get_Time();              
            }
            clusterTime /= clusterEnergy;
            return clusterTime;
	}
        
        public Point3D getCentroid() {
            double clusEnergy = this.getEnergy();
            double wtot       = 0;
            double clusterX   = 0;
            double clusterY   = 0;
            double clusterZ   = 0;
            for(int i=0; i<this.size(); i++) {
                FTCALHit hit = this.get(i);
                // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//				double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
                double wi = Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
                wtot     += wi;
                clusterX += wi*hit.get_Dx();
                clusterY += wi*hit.get_Dy();
                clusterZ += wi*hit.get_Dz();
            }
            clusterX /= wtot;
            clusterY /= wtot;
            clusterZ /= wtot;
            Point3D centroid  = new Point3D(clusterX,clusterY,clusterZ);
            return centroid;            
        }    

        public double getX() {
            // returns X coordinate of centroild
            return this.getCentroid().x();
	}

	public double getY() {
            // returns Y coordinate of centroild
            return this.getCentroid().y();           
	}
       
	public double getZ() {
            // returns Z coordinate of centroild
            return this.getCentroid().z();           
	}
       
	public double getX2() {
            double clusEnergy = this.getEnergy();
            double wtot       = 0;
            double clusterXX   = 0;
            for(int i=0; i<this.size(); i++) {
                FTCALHit hit = this.get(i);
                // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//				double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
                double wi = Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
                wtot     += wi;
                clusterXX += wi*hit.get_Dx()*hit.get_Dx();
            }
            clusterXX /= wtot;
            return clusterXX;
        }

	public double getY2() {
            double clusEnergy = this.getEnergy();
            double wtot       = 0;
            double clusterYY   = 0;
            for(int i=0; i<this.size(); i++) {
                FTCALHit hit = this.get(i);
                // the moments: this are calculated in a second loop because log weighting requires clusEnergy to be known
//				double wi = hit_in_clus.get_Edep();    // de-comment for arithmetic weighting
                double wi = Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
                wtot     += wi;
                clusterYY += wi*hit.get_Dy()*hit.get_Dy();
            }
            clusterYY /= wtot;
            return clusterYY;
	}

	public double getWidthX() {
            double sigmaX2 = (this.getX2() - Math.pow(this.getX(),2.)); 
            if(sigmaX2<0) sigmaX2=0;
            double sigmaX = Math.sqrt(sigmaX2);
            return sigmaX;
	}

	public double getWidthY() {
            double sigmaY2 = (this.getY2() - Math.pow(this.getY(),2.)); 
            if(sigmaY2<0) sigmaY2=0;
            double sigmaY = Math.sqrt(sigmaY2); 
            return sigmaY;
	}

	public double getRadius() {
            double radius2 = (this.getX2() - Math.pow(this.getX(),2.) + this.getY2() - Math.pow(this.getY(),2.));
            if(radius2<0) radius2=0;
            double radius = Math.sqrt(radius2);
            return radius;
	}

	public double getTheta() {
            double theta = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(this.getX(),2.)+Math.pow(this.getY(),2.))/this.getZ()));
            return theta;
	}

	public double getPhi() {
            double phi = Math.toDegrees(Math.atan2(this.getY(),this.getX()));
            return phi;
	}

	public boolean isgoodCluster(IndexedTable clusterTable) {
            if(this.getSize()  > clusterTable.getDoubleValue("cluster_min_size", 1,1,0) &&
               this.getEnergy()> clusterTable.getDoubleValue("cluster_min_energy", 1,1,0)/1000) {
                    return true;
            }
            else {
                    return false;
            }
	}	
	
        private double weight(FTCALHit hit, double clusEnergy) {
            return Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
        }
        
        public boolean containsHit(FTCALHit hit, IndexedTable clusterTable) {
            boolean addFlag = false;
            for(int j = 0; j< this.size(); j++) {
		double tDiff = Math.abs(hit.get_Time() - this.get(j).get_Time());
		double xDiff = Math.abs(hit.get_IDX()  - this.get(j).get_IDX());
		double yDiff = Math.abs(hit.get_IDY()  - this.get(j).get_IDY());
                if(tDiff <= clusterTable.getDoubleValue("time_window", 1,1,0) && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
            }
            return addFlag;
        }
        
        public void showCluster() {
            System.out.println("\nCluster = "  + this._clusID); 
            System.out.println("Size = "    + this.getSize() + 
                               "\tE = "     + this.getEnergy() + 
                               "\tTime = "  + this.getTime() + 
                               "\tTheta = " + this.getTheta()  + 
                               "\tPhi = "   + this.getPhi());
            for(int j = 0; j< this.size(); j++) {
                System.out.println("hit # " + j + "\t" + this.get(j).get_IDX() + "\t" + this.get(j).get_IDY() + "\t" + this.get(j).get_Edep());
            }
        }
    
}
	

	

