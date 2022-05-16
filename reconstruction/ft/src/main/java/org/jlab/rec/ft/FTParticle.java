package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.rec.ft.trk.FTTRKConstantsLoader;

public class FTParticle {

	private int _ID;                                          // track ID
	private int _Charge;		         	          // 0/1 for photon/electron
	private double _Time;      			          // time of the particle at the vertex
	private double _Energy;			                  // total energy of the cluster including correction
	private Vector3D _Vertex    = new Vector3D();             // vertex 
	private Vector3D _Position  = new Vector3D();             // position 
	private Vector3D _Direction = new Vector3D();             // direction 
         private int _Cluster;					  // track pointer to cluster information in FTCALRec::cluster bank
	private int _Signal;					  // track pointer to signal information in FTHODORec::cluster bank
        private int _Cross0;                                      // track pointer to cross information in FTTRKRec::cross bank (TRK0)
        private int _Cross1;                                      // track pointer to cross information in FTTRKRec::cross bank (TRK1)
        private double _field;
        	
	// constructor
	public FTParticle(int cid) {
		this.set_ID(cid);
	}

 	public FTParticle(int cid, int charge, double fieldScale, FTResponse response, double vx, double vy, double vz) {
            this.set_ID(cid);
            this.setCharge(0);
            this.setField(fieldScale);
            this.setEnergy(response.getEnergy());
            this.setPosition(response.getPosition());
            this.setVertex(vx,vy,vz);
            this.setDirection();
            this.setTime(response.getTime() - this.getPath() / PhysicsConstants.speedOfLight());
            this.setCalorimeterIndex(response.getId());
            this.setHodoscopeIndex(-1);
            this.setTrackerIndex(-1, 0);
            this.setTrackerIndex(-1, 1);
         }
        
	public int get_ID() {
		return _ID;
	}


	public final void set_ID(int _ID) {
		this._ID = _ID;
	}


	public int getCharge() {
		return _Charge;
	}


	public final void setCharge(int _Charge) {
		this._Charge = _Charge;
	}
        

	public double getTime() {
		return _Time;
	}


	public final void setTime(double _Time) {
		this._Time = _Time;
	}


	public double getEnergy() {
		return _Energy;
	}


	public final void setEnergy(double _Energy) {
		this._Energy = _Energy;
	}

        public Vector3D getPosition() {
            return _Position;
        }

        public final void setPosition(Vector3D _Position) {
            this._Position = _Position;
        }

        public double getField() {
            return _field;
        }

        public final void setField(double _field) {
            this._field = _field;
        }


        public Vector3D getDirection() {
            return this._Direction;
        }

        public final void setDirection() {
            Vector3D line = new Vector3D(this.getPosition());
            line.sub(this.getVertex());
            this._Direction = line.asUnit();            
        }
        
        public void setDirection(IndexedTable thetaTable, IndexedTable phiTable) {
            Vector3D direction = new Vector3D();
            Vector3D line = new Vector3D(this.getPosition());
            line.sub(this.getVertex());
            if(this._Charge==-1) {
                double energy    = this._Energy;
                double thetaCorr = Math.exp(thetaTable.getDoubleValue("thetacorr0", 1,1,0)+thetaTable.getDoubleValue("thetacorr1", 1,1,0)*energy)+
			     	   Math.exp(thetaTable.getDoubleValue("thetacorr2", 1,1,0)+thetaTable.getDoubleValue("thetacorr3", 1,1,0)*energy);
                thetaCorr        = Math.toRadians(thetaCorr * Math.abs(this._field));
		double phiCorr   = Math.exp(phiTable.getDoubleValue("phicorr0", 1,1,0)+phiTable.getDoubleValue("phicorr1", 1,1,0)*energy)+
			     	   Math.exp(phiTable.getDoubleValue("phicorr2", 1,1,0)+phiTable.getDoubleValue("phicorr3", 1,1,0)*energy)+
			     	   Math.exp(phiTable.getDoubleValue("phicorr4", 1,1,0)+phiTable.getDoubleValue("phicorr5", 1,1,0)*energy);
                phiCorr          = Math.toRadians(phiCorr * this._field);
                direction.setMagThetaPhi(1, line.theta()+thetaCorr, line.phi()-phiCorr);
            }
            else {
                direction = line.asUnit();
            }
            this._Direction = direction;
        }

        public Vector3D getVertex() {
            return _Vertex;
        }

        public final void setVertex(double x, double y, double z) {
            this._Vertex.setXYZ(x, y, z);
        }

        public final double getPath() {
            Vector3D path = new Vector3D(this.getPosition());
            path.sub(this.getVertex());
            return path.mag();
        }
        
        public Line3D getLastCross() {
            Line3D track = new Line3D();
            track.set(this._Position.toPoint3D(), this._Position);
            return track;
        }
        
        public int getCalorimeterIndex() {
		return _Cluster;
	}


	public final void setCalorimeterIndex(int _Cluster) {
		this._Cluster = _Cluster;
	}


	public int getHodoscopeIndex() {
		return _Signal;
	}


	public final void setHodoscopeIndex(int _Signal) {
		this._Signal = _Signal;
	}
        
        public int getTrackerIndex(int ndet) {
		if(ndet==0){return _Cross0;}else{return _Cross1;}
	}
      
        public final void setTrackerIndex(int _Cross, int ndet) {
		if(ndet==0){this._Cross0 = _Cross;}else{this._Cross1 = _Cross;}
	}
        

        public int getDetectorHit(List<FTResponse>  hitList, DetectorType detectorType, double distanceThreshold, double timeThreshold){
            Line3D cross = this.getLastCross();
            double   minimumDistance = 500.0;
            int      bestIndex       = -1;
            for(int loop = 0; loop < hitList.size(); loop++){
                FTResponse response = hitList.get(loop);
                if(response.getAssociation()<0 && response.getType().equals(detectorType)){
                    Line3D  dist = cross.distance(response.getPosition().toPoint3D());
                    double hitdistance  = dist.length();
                    double timedistance = Math.abs(this.getTime() - (response.getTime()-response.getPosition().mag()/PhysicsConstants.speedOfLight()));       
                    if(hitdistance<distanceThreshold && hitdistance<minimumDistance){                    
                        if(detectorType.getName()=="FTTRK" && timedistance<timeThreshold){
                            minimumDistance = hitdistance;
                            bestIndex = loop;
                            if(FTEventBuilder.debugMode >=1){
                                System.out.println("best hit distance and time " + minimumDistance + " " + timedistance);
                                System.out.println("cross center coordinates x, y " + response.getPosition().toPoint3D().x() + " , " +
                                   response.getPosition().toPoint3D().y());
                            }
                        }else if(detectorType.getName()=="FTHODO" && timedistance<timeThreshold){
                            minimumDistance = hitdistance; 
                            bestIndex = loop;
                        } 
                    }
                }
            }
            if(bestIndex>-1) {
                // if bestHit is on hodo require at least two hits overall on HODO
                if(detectorType.getName()=="HODO"){if(hitList.get(bestIndex).getSize()<FTConstants.HODO_MIN_CLUSTER_SIZE) bestIndex=-1;}
                // if bestHit is on trk require at least two crosses overall in FTTRK
                if(detectorType.getName()=="FTTRK"){
                    if(hitList.get(bestIndex).getSize() >= FTConstants.TRK_MIN_CROSS_NUMBER){;}else{bestIndex=-1;}
                }
            }
            return bestIndex;
        }
        
        
        public int[] getTRKBestHits(List<FTResponse>  hitList, int it, double distanceThreshold, double timeThreshold){
            Line3D cross = this.getLastCross();
            double   minimumDistance = 500.0;
            int[] bestIndex = {-1, -1};
            for(int loop = 0; loop < hitList.size(); loop++){
                int bestidx = -1;
                FTResponse response = hitList.get(loop);
                if(response.getAssociation()<0 && response.getType().getName() == "FTTRK"){
                    Line3D  dist = cross.distance(response.getPosition().toPoint3D());
                    double hitdistance  = dist.length();
                    double timedistance = Math.abs(this.getTime() - (response.getTime()-response.getPosition().mag()/PhysicsConstants.speedOfLight()));       

                    double t=response.getTime();
                    int det = response.getTrkDet();
                    
                    if(timedistance<timeThreshold && hitdistance<distanceThreshold){
                        minimumDistance = hitdistance;
                        bestidx = loop;
                    }
                    // select index of the detector                    
                    
                    if(bestidx>-1){
                        if(hitList.get(bestidx).getSize() < FTConstants.TRK_MIN_CROSS_NUMBER) bestidx=-1;
                        bestIndex[det] = bestidx;
                        }
                    }   
                }
                return bestIndex;
        }
        
        
        public int [][] getTRKOrderedListOfHits(List<FTResponse>  hitList, int it, double distanceThreshold, double timeThreshold){
            
            int TRK1 = DetectorLayer.FTTRK_MODULE1 - 1;  // tracker id=0
            int TRK2 = DetectorLayer.FTTRK_MODULE2 -1;   // tracker id=1
            
            Line3D cross = this.getLastCross();
            int ndetectors = FTTRKConstantsLoader.NSupLayers;
            int hitsTRK = 0;
            for(int l=0; l<hitList.size(); l++){
                if(hitList.get(l).getType().getName() == "FTTRK") hitsTRK++;
            }
            int[][] bestIndices; 
            if(hitsTRK != 0){
                bestIndices = new int[hitsTRK][ndetectors];
                ArrayList<Double> hitDistancesDet0 = new ArrayList<Double>(hitsTRK);
                ArrayList<Integer> hitOrderDet0 = new ArrayList<Integer>(hitsTRK);
                ArrayList<Double> hitDistancesDet1 = new ArrayList<Double>(hitsTRK);
                ArrayList<Integer> hitOrderDet1 = new ArrayList<Integer>(hitsTRK);
                int lTRK = -1;
                //init
                for(int l=0; l<hitsTRK; l++){
                    bestIndices[l][TRK1] = bestIndices[l][TRK2] = -1;
                    hitDistancesDet0.add(l, -1.);
                    hitOrderDet0.add(l, 1);
                    hitDistancesDet1.add(l, -1.);
                    hitOrderDet1.add(l, 1);
                }
                for(int loop = 0; loop < hitList.size(); loop++){
                    int bestidx = -1;
                    FTResponse response = hitList.get(loop);
                    if(response.getAssociation()<0 && response.getType().getName() == "FTTRK"){
                        lTRK++;
                        hitDistancesDet0.add(lTRK, -1.);
                        hitDistancesDet1.add(lTRK, -1.);
                        hitOrderDet0.add(lTRK, -1);
                        hitOrderDet1.add(lTRK, -1);
                        Line3D  dist = cross.distance(response.getPosition().toPoint3D());
                        double hitdistance  = dist.length();
                        double timedistance = Math.abs(this.getTime() - (response.getTime()-response.getPosition().mag()/PhysicsConstants.speedOfLight()));       

                        double t=response.getTime();
                        int det = response.getTrkDet();
                        
                        if(timedistance<timeThreshold && hitdistance<distanceThreshold){
                            bestidx = loop;
                            if(det==TRK1) {
                                hitDistancesDet0.set(lTRK, hitdistance);
                                hitOrderDet0.set(lTRK, loop);
                            }
                            if(det==TRK2){
                                hitDistancesDet1.set(lTRK, hitdistance);
                                hitOrderDet1.set(lTRK, loop);
                            }
                        }                    
                    
                        if(bestidx>-1){
                            if(hitList.get(bestidx).getSize() < FTConstants.TRK_MIN_CROSS_NUMBER){
                                bestidx=-1;
                                if(det==TRK1){
                                    hitDistancesDet0.set(lTRK, -1.);
                                    hitOrderDet0.set(lTRK, -1);
                                }else if(det==TRK2){
                                    hitDistancesDet1.set(lTRK, -1.);
                                    hitOrderDet1.set(lTRK, -1);
                                }    
                            }
                        }
                    }   
                }
                // sort the two arrays as a function of the distance
                ArrayList<Double> iniHitDis0 = new ArrayList<Double>();
                ArrayList<Double> iniHitDis1 = new ArrayList<Double>(); 
                for(int l=0; l<hitsTRK; l++){
                    iniHitDis0.add(l, hitDistancesDet0.get(l));
                    iniHitDis1.add(l, hitDistancesDet1.get(l));
                }
                int[] orderedIndices0 = new int[hitsTRK];
                int[] orderedIndices1 = new int[hitsTRK];
                Collections.sort(hitDistancesDet0, Collections.reverseOrder());
                Collections.sort(hitDistancesDet1, Collections.reverseOrder());
                for(int l=0; l<hitsTRK; l++){
                    orderedIndices0[l] = orderedIndices1[l] = -1;
                    for(int k=0; k<hitsTRK; k++){
                        if(hitDistancesDet0.get(l) == iniHitDis0.get(k) && hitDistancesDet0.get(l)>0) orderedIndices0[l] = hitOrderDet0.get(k);
                        if(hitDistancesDet1.get(l) == iniHitDis1.get(k) && hitDistancesDet1.get(l)>0) orderedIndices1[l] = hitOrderDet1.get(k);
                    }
                }
                // compose the double arrays of indices ordered by distance
                for(int l=0; l < hitsTRK; l++){
                    bestIndices[l][TRK1] = orderedIndices0[l];
                    bestIndices[l][TRK2] = orderedIndices1[l];
                }
            }else{ 
                bestIndices = new int[1][2];
                bestIndices[0][TRK1] = bestIndices[0][TRK2] = -1;
            }
            return bestIndices;
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
