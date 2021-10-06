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

	private int     _clusID;		// cluster ID
         private boolean _clusStat=true;       // cluster status flag (true==good, false==bad)
	
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

	public void setStatus(IndexedTable clusterTable) {
            if(this.getSize()  > clusterTable.getDoubleValue("cluster_min_size", 1,1,0) &&
               this.getEnergy()> clusterTable.getDoubleValue("cluster_min_energy", 1,1,0)/1000) {
               this._clusStat=true;
            }
            else {
               this._clusStat=false;
            }
	}
        
         public boolean getStatus() {
             return this._clusStat;
         }
	
        private double weight(FTCALHit hit, double clusEnergy) {
            return Math.max(0., (3.45+Math.log(hit.get_Edep()/clusEnergy)));
        }
        
        public boolean containsHit(FTCALHit hit, IndexedTable thresholds, IndexedTable clusterTable) {
            boolean addFlag = false;
            if(hit.get_Edep()>thresholds.getDoubleValue("thresholdCluster",1,1,hit.get_COMPONENT())) {
                for(int j = 0; j< this.size(); j++) {
                    double tDiff = Math.abs(hit.get_Time() - this.get(j).get_Time());
                    double xDiff = Math.abs(hit.get_IDX()  - this.get(j).get_IDX());
                    double yDiff = Math.abs(hit.get_IDY()  - this.get(j).get_IDY());
                    if(tDiff <= clusterTable.getDoubleValue("time_window", 1,1,0) && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
                }                
            }
            return addFlag;
        }
        
        @Override
        public String  toString(){
            StringBuilder str = new StringBuilder();

            str.append(String.format("Cluster: %4d\n",   this.getID()));            
            str.append(String.format("\tStatus:  %b",    this.getStatus()));            
            str.append(String.format("\tSize: %4d",      this.getSize()));
            str.append(String.format("\tE: %7.3f",       this.getEnergy())); 
            str.append(String.format("\tTime: %7.3f",    this.getTime())); 
            str.append(String.format("\tTheta: %7.3f",   this.getTheta())); 
            str.append(String.format("\tPhi: %7.3f\n",   this.getPhi()));
            for(int j = 0; j< this.size(); j++) {
                str.append(String.format("\thit #%d\t%d\t%d\t%7.3f\n", j, this.get(j).get_IDX(), this.get(j).get_IDY(), this.get(j).get_Edep()));
            }
            return str.toString();
        }
    
        public void show() {
            System.out.println(this.toString());
        }
}   
	

	
