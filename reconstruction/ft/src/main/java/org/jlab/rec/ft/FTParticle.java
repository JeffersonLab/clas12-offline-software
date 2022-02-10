package org.jlab.rec.ft;

import java.util.List;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.rec.ft.FTEBEngine;

public class FTParticle {

	
	private int _ID;                                          // track ID
	private int _Charge;		         	          // 0/1 for photon/electron
	private double _Time;      			          // time of the particle at the vertex
	private double _Energy;			                  // total energy of the cluster including correction
	private Vector3D _Position  = new Vector3D();             // position 
	private Vector3D _Direction = new Vector3D();             // direction 
        private int _Cluster;					  // track pointer to cluster information in FTCALRec::cluster bank
	private int _Signal;					  // track pointer to signal information in FTHODORec::cluster bank
	private int _Cross;					  // track pointer to cross information in FTTRKRec::cross bank
        private double _field;
        	
	// constructor
	public FTParticle(int cid) {
		this.set_ID(cid);
	}


	public int get_ID() {
		return _ID;
	}


	public void set_ID(int _ID) {
		this._ID = _ID;
	}


	public int getCharge() {
		return _Charge;
	}


	public void setCharge(int _Charge) {
		this._Charge = _Charge;
	}
        

	public double getTime() {
		return _Time;
	}


	public void setTime(double _Time) {
		this._Time = _Time;
	}


	public double getEnergy() {
		return _Energy;
	}


	public void setEnergy(double _Energy) {
		this._Energy = _Energy;
	}

        public Vector3D getPosition() {
            return _Position;
        }

        public void setPosition(Vector3D _Position) {
            this._Position = _Position;
        }

        public double getField() {
            return _field;
        }

        public void setField(double _field) {
            this._field = _field;
        }


        public Vector3D getDirection() {
            return this._Direction;
        }

        public void setDirection() {
            this._Direction = this.getPosition().asUnit();            
        }
        
        public void setDirection(IndexedTable thetaTable, IndexedTable phiTable) {
            Vector3D direction = new Vector3D();
            if(this._Charge==-1) {
                double energy    = this._Energy;
                double thetaCorr = Math.exp(thetaTable.getDoubleValue("thetacorr0", 1,1,0)+thetaTable.getDoubleValue("thetacorr1", 1,1,0)*energy)+
			     	   Math.exp(thetaTable.getDoubleValue("thetacorr2", 1,1,0)+thetaTable.getDoubleValue("thetacorr3", 1,1,0)*energy);
                thetaCorr        = Math.toRadians(thetaCorr * Math.abs(this._field));
		double phiCorr   = Math.exp(phiTable.getDoubleValue("phicorr0", 1,1,0)+phiTable.getDoubleValue("phicorr1", 1,1,0)*energy)+
			     	   Math.exp(phiTable.getDoubleValue("phicorr2", 1,1,0)+phiTable.getDoubleValue("phicorr3", 1,1,0)*energy)+
			     	   Math.exp(phiTable.getDoubleValue("phicorr4", 1,1,0)+phiTable.getDoubleValue("phicorr5", 1,1,0)*energy);
                phiCorr          = Math.toRadians(phiCorr * this._field);
                direction.setMagThetaPhi(1, this.getPosition().theta()+thetaCorr, this.getPosition().phi()-phiCorr);
            }
            else {
                direction = this.getPosition().asUnit();
            }
            this._Direction = direction;
        }

        public Line3D getLastCross() {
            Line3D track = new Line3D();
            track.set(this._Position.toPoint3D(), this._Position);
            return track;
        }
        
        public int getCalorimeterIndex() {
		return _Cluster;
	}


	public void setCalorimeterIndex(int _Cluster) {
		this._Cluster = _Cluster;
	}


	public int getHodoscopeIndex() {
		return _Signal;
	}


	public void setHodoscopeIndex(int _Signal) {
		this._Signal = _Signal;
	}


	public int getTrackerIndex() {
		return _Cross;
	}


	public void setTrackerIndex(int _Cross) {
		this._Cross = _Cross;
	}

        public int getDetectorHit(List<FTResponse>  hitList, String detectorType, double distanceThreshold, double timeThreshold){
        
            Line3D cross = this.getLastCross();
            double   minimumDistance = 500.0;
            int      bestIndex       = -1;
            for(int loop = 0; loop < hitList.size(); loop++){
                FTResponse response = hitList.get(loop);
                if(response.getAssociation()<0 && response.getType() == detectorType){
                    Line3D  dist = cross.distance(response.getPosition().toPoint3D());
                    double hitdistance  = dist.length();
                    double timedistance = Math.abs(this.getTime() - (response.getTime()-response.getPosition().mag()/PhysicsConstants.speedOfLight()));       
//                    FTEBEngine.h600.fill(timedistance);
//                    FTEBEngine.h601.fill(response.getTime());

                    if(detectorType=="FTTRK") {
                        double t=response.getTime();
                        System.out.println(this.getTime() + " to be compard to " + response.getPosition().mag()/PhysicsConstants.speedOfLight());
                        FTEBEngine.h600.fill(response.getPosition().mag()/PhysicsConstants.speedOfLight());
                        FTEBEngine.h601.fill(response.getCrTime(), response.getPosition().mag()/PhysicsConstants.speedOfLight());
                        if(response.getId()==0){
                            FTEBEngine.h602.fill(response.getCrTime());
                        }
                        if(response.getId()==1){
                            FTEBEngine.h603.fill(response.getCrTime());
                        }
                    }
                    
//                    if(detectorType=="FTHODO") FTEBEngine.h502.fill(timedistance);
//                    if((timedistanceTRK || timedistance<2.*timeThreshold) && hitdistance<distanceThreshold && hitdistance<minimumDistance){
                    if(hitdistance<distanceThreshold && hitdistance<minimumDistance){                    
                        if(detectorType=="FTTRK" && timedistance<timeThreshold){
                           minimumDistance = hitdistance;
                           bestIndex = loop;
                           System.out.println("best hit distance and time " + minimumDistance + " " + timedistance);
                           System.out.println("cross center coordinates x, y " + response.getPosition().toPoint3D().x() + " , " +
                                   response.getPosition().toPoint3D().y());
                        }else if(detectorType=="FTHODO" && timedistance<timeThreshold){
                           minimumDistance = hitdistance; 
                           bestIndex = loop;
                        } 
                    }
                }
            }
            if(bestIndex>-1) {
                // if bestHit is on hodo require at least two hits overall on HODO
                if(detectorType=="HODO"){if(hitList.get(bestIndex).getSize()<FTConstants.HODO_MIN_CLUSTER_SIZE) bestIndex=-1;}
                // if bestHit is on trk require at least two crosses overall in FTTRK
                if(detectorType=="FTTRK"){
                    if(hitList.get(bestIndex).getSize() >= FTConstants.TRK_MIN_CROSS_NUMBER){
                         ;
                    }else{
                        bestIndex=-1;
                    } 
                }
            }
            return bestIndex;
        }
        
        
        public void show() {            
            System.out.println( "FT Particle info " +
                                " Charge = "+ this.getCharge() +
                                " E = "     + this.getEnergy() +                    
                                " X = "     + this.getPosition().x() +
                                " Y = "     + this.getPosition().y() +
                                " Z = "     + this.getPosition().z() +
                                " Theta = " + Math.toDegrees(this.getDirection().theta()) +
                                " Phi = "   + Math.toDegrees(this.getDirection().phi()) +
                                " Time = "  + this.getTime());
        }
}
