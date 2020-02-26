package org.jlab.rec.ft.hodo;


import java.util.ArrayList;
import org.jlab.geom.prim.Point3D;

import org.jlab.io.evio.EvioDataBank;



public class FTHODOCluster extends ArrayList<FTHODOHit> {

	/**
	 * A signal in the hodoscope is a pair of hits above threshold in the two layers. 
	 * The hit pair is define according o the algorithm in HodoSignalFinder
	 */
	private static final long serialVersionUID = 1L;

	private int _clusID;			   			// signal ID	
	
	
	// constructor
	public FTHODOCluster(int cid) {
		this.setID(cid);
	}

	public int getID() {
		return _clusID;
	}

	public void setID(int _clusterID) {
		this._clusID = _clusterID;
	}

	public int getSize() {
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
        
	public double getTime() {
            // returns energy weighted time 
            double clusterEnergy  = this.getEnergy();
            double clusterTime    = 0;
            for(int i=0; i<this.size(); i++) {
                FTHODOHit hit = this.get(i);
                clusterTime += hit.get_Edep()*hit.get_Time();              
            }
            clusterTime /= clusterEnergy;
            return clusterTime;
	}
        
        public Point3D getCentroid() {
            double clusterEnergy = this.getEnergy();
            double clusterX   = 0;
            double clusterY   = 0;
            double clusterZ   = 0;
            for(int i=0; i<this.size(); i++) {
                FTHODOHit hit = this.get(i);
                clusterX += hit.get_Edep()*hit.get_Dx();
                clusterY += hit.get_Edep()*hit.get_Dy();
                clusterZ += hit.get_Edep()*hit.get_Dz();
            }
            clusterX /= clusterEnergy;
            clusterY /= clusterEnergy;
            clusterZ /= clusterEnergy;
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
            double clusterXX   = 0;
            for(int i=0; i<this.size(); i++) {
                FTHODOHit hit = this.get(i);
                clusterXX += hit.get_Edep()*hit.get_Dx()*hit.get_Dx();
            }
            clusterXX /= clusEnergy;
            return clusterXX;
        }

	public double getY2() {
            double clusEnergy = this.getEnergy();
            double clusterYY   = 0;
            for(int i=0; i<this.size(); i++) {
                FTHODOHit hit = this.get(i);
                clusterYY += hit.get_Edep()*hit.get_Dy()*hit.get_Dy();
            }
            clusterYY /= clusEnergy;
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

	public double getLayerMultiplicity(int ilayer) {
            int nlayer = 0;
            for(int i=0; i<this.size(); i++) {
                if(this.get(i).get_Layer()==ilayer) nlayer++;
            }
            return nlayer;
	}

        public boolean isgoodCluster() {
            if(this.getSize()  > FTHODOConstantsLoader.cluster_min_size   &&
               this.getEnergy()> FTHODOConstantsLoader.cluster_min_energy && 
               this.getLayerMultiplicity(1)>0 && this.getLayerMultiplicity(2)>0) {
                    return true;
            }
            else {
                    return false;
            }
	}	
       
        public boolean containsHit(FTHODOHit hit) {
            boolean addFlag = false;
            for(int j = 0; j< this.size(); j++) {
		double tDiff = Math.abs(hit.get_Time() - this.get(j).get_Time());
		double xDiff = Math.abs(hit.get_Dx()   - this.get(j).get_Dx());
		double yDiff = Math.abs(hit.get_Dy()   - this.get(j).get_Dy());
//                System.out.println("DT: " + tDiff + "(" + FTHODOConstantsLoader.time_window 
//                             + ")\t DX: " + xDiff + "(" + FTHODOConstantsLoader.hit_distance 
//                             + ")\t DY: " + yDiff + "(" + FTHODOConstantsLoader.hit_distance + ")");
                if(tDiff <= FTHODOConstantsLoader.time_window && 
                   xDiff <= FTHODOConstantsLoader.hit_distance && 
                   yDiff <= FTHODOConstantsLoader.hit_distance) addFlag = true;
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
                System.out.println("hit # " + j + "\t" + this.get(j).get_Layer() + "\t" + this.get(j).get_Sector() + "\t" + this.get(j).get_ID() + "\t" + this.get(j).get_Edep());
            }
        }
	
	
	public static  ArrayList<FTHODOCluster> getSignals(EvioDataBank bank) {

	     int[] signalID           = bank.getInt("signalID");
	     int[] signalSize         = bank.getInt("signalSize");
	     double[] signalX         = bank.getDouble("signalX");
	     double[] signalY         = bank.getDouble("signalY");
	     double[] signalDX        = bank.getDouble("signalDX");
	     double[] signalDY        = bank.getDouble("signalDY");
	     double[] signalTime      = bank.getDouble("signalTime");
	     double[] signalEnergy    = bank.getDouble("signalEnergy");
	     double[] signalTheta     = bank.getDouble("signalTheta");
	     double[] signalPhi       = bank.getDouble("signalPhi");


	     int size = signalID.length;
	     ArrayList<FTHODOCluster> signals = new ArrayList<FTHODOCluster>();
	      
//	     for(int i = 0; i<size; i++){  
//	    	 FTHODOCluster signal = new FTHODOCluster(signalID[i]);	
//	    	 signal.set_signalSize(signalSize[i]);
//	    	 signal.set_signalX(signalX[i]);
//	    	 signal.set_signalY(signalY[i]);
//	    	 signal.set_signalDX(signalDX[i]);
//	    	 signal.set_signalDY(signalDY[i]);
//	    	 signal.set_signalTime(signalTime[i]);
//	    	 signal.set_signalEnergy(signalEnergy[i]);
//	    	 signal.set_signalTheta(signalTheta[i]);
//	    	 signal.set_signalPhi(signalPhi[i]);
//
//            signals.add(signal); 
//	          
//	      }
	      return signals;
	      
	}

	
}
	

	

