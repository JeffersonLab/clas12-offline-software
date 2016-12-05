/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class DetectorParticle implements Comparable {
    
    private Vector3 particleMomenta = new Vector3();
    private Vector3 particleVertex  = new Vector3();
    private Integer particleCharge  = 0;
    private Integer particlePID     = 0;
    private Integer particleStatus  = 1;
    private Double  particleBeta    = 0.0;
    private Double  particleMass    = 0.0;
    private Double  particlePath    = 0.0;
    
    private int     particleScore     = 0; // scores are assigned detector hits
    private double  particleScoreChi2 = 0.0; // chi2 for particle score 
    
    private Vector3 particleCrossPosition  = new Vector3();
    private Vector3 particleCrossDirection = new Vector3();
    
    private Line3D  driftChamberEnter = new Line3D();
    
    private List<DetectorResponse>    responseStore = new ArrayList<DetectorResponse>();
    private List<CherenkovResponse>  cherenkovStore = new ArrayList<CherenkovResponse>();
    
    private TreeMap<DetectorType,Vector3>  projectedHit = 
            new  TreeMap<DetectorType,Vector3>();
            
    
    public DetectorParticle(){
        
    }
    
    public void clear(){
        this.responseStore.clear();
    }
    
    public List<CherenkovResponse> getCherenkovResponse(){
        return this.cherenkovStore;
    }
    
    public void addCherenkovResponse(CherenkovResponse res){
        this.cherenkovStore.add(res);
    }
    
    public void addResponse(DetectorResponse res, boolean match){
        this.responseStore.add(res);
        if(match==true){
            Line3D distance = res.getDistance(this);
            res.getMatchedPosition().setXYZ(distance.origin().x(),
                    distance.origin().y(),distance.origin().z());
            
            /*Vector3D vec = new Vector3D(
                    this.particleCrossPosition.x(),
                    particleCrossPosition.y(),
                    particleCrossPosition.z());
            */
            res.setPath(this.getPathLength(res.getPosition()));
        }
    }
    
    public Particle getPhysicsParticle(int pid){
        Particle  particle = new Particle(pid,
                this.vector().x(),this.vector().y(),this.vector().z(),
                this.vertex().x(),this.vertex().y(),this.vertex().z()
        );
        return particle;
    }
    
    public double compare(Vector3 vec){
        return this.vector().compare(vec);
    }
    
    public double compare(double x, double y, double z){
        return this.vector().compare(new Vector3(x,y,z));
    }
    
    public void setLowerCross(double x, double y, double z, double ux, double uy, double uz){
        this.driftChamberEnter.set(x, y, z, x+1000.0*ux, y+1000.0*uy, z + 1000.0*uz);
    }
    
    public Line3D getLowerCross(){
        return this.driftChamberEnter;
    }
    /**
     * Particle score combined number that represents which detectors were hit
     * HTCC - 1000, FTOF - 100, EC - 10
     * SCORE = HTCC + FTOF + EC
     * @param score 
     */
    public void setScore(int score){
        this.particleScore = score;
    }
    /**
     * Chi square of score determination.
     * @param chi2 
     */
    public void setChi2(double chi2){
        this.particleScoreChi2 = chi2;
    }
    /**
     * returns particle score.
     * @return 
     */
    public int getScore(){
        return this.particleScore;
    }
    /**
     * returns chi2 of score.
     * @return 
     */
    public double getChi2(){
        return this.particleScoreChi2;
    }
    /**
     * add detector response to the particle
     * @param res 
     */
    public void addResponse(DetectorResponse res){
        this.responseStore.add(res);
    }
    
    public DetectorResponse  getResponse(DetectorType type, int layer){
        for(DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer){
                return res;
            }
        }
        return null;
    }
    
    public boolean hasHit(DetectorType type){
        int hits = 0;
        for( DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type) hits++;
        }
        if(hits==0) return false;
        if(hits>1) System.out.println("[Warning] Too many hits for detector type = " + type);
        return true;
    }
    public boolean hasHit(DetectorType type, int layer){
        int hits = 0;
        for( DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer) hits++;
        }
        if(hits==0) return false;
        if(hits>1) System.out.println("[Warning] Too many hits for detector type = " + type);
        return true;
    }
    
    public List<DetectorResponse>  getDetectorResponses(){
        return this.responseStore;
    }
    
    public DetectorResponse getHit(DetectorType type){
        for(DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type) return res;
        }
        return null;
    }
    
    public DetectorResponse getHit(DetectorType type, int layer){
        for(DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer) return res;
        }
        return null;
    }
    
    public double getBeta(){ return this.particleBeta;}
    public int    getStatus(){ return this.particleStatus;}
    public double getMass(){ return this.particleMass;}
    public int    getPid(){ return this.particlePID;}
    
    public Path3D getTrajectory(){
        Path3D  path = new Path3D();
        //path.addPoint(this.particleCrossPosition.x(), 
        //        this.particleCrossPosition.y()
        //        , this.particleCrossPosition.z());
        path.generate(
                this.particleCrossPosition.x(),
                this.particleCrossPosition.y(),
                this.particleCrossPosition.z(),
                this.particleCrossDirection.x(), 
                this.particleCrossDirection.y(), 
                this.particleCrossDirection.z(),                               
                1500.0, 2);
        return path;
    }
    
    public Vector3  vector(){return this.particleMomenta;}    
    public Vector3  vertex(){return this.particleVertex;}    
    public Vector3  getCross(){ return this.particleCrossPosition;}    
    public Vector3  getCrossDir(){ return this.particleCrossDirection;}    
    public double   getPathLength(){ return this.particlePath;}
    public int      getCharge(){ return this.particleCharge;}
    
    
    
    public double   getPathLength(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        return this.getPathLength(response.getPosition());
    }
    
    
    
    public double   getPathLength(Vector3 vec){
        return this.getPathLength(vec.x(), vec.y(), vec.z());
    }
    
    public double   getPathLength(double x, double y, double z){
        double crosspath = Math.sqrt(
                (this.particleCrossPosition.x()-x)*(this.particleCrossPosition.x()-x)
                        + (this.particleCrossPosition.y()-y)*(this.particleCrossPosition.y()-y)
                        + (this.particleCrossPosition.z()-z)*(this.particleCrossPosition.z()-z)
        );
        return this.particlePath + crosspath;
    }
    
    public double getTime(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        return response.getTime();
    }
    
    public double getEnergy(DetectorType type){
        double energy = 0.0;
        for(DetectorResponse r : this.responseStore){
            if(r.getDescriptor().getType()==type){
                energy += r.getEnergy();
            }
        }
        /*
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        return response.getEnergy();*/
        return energy;
    }
    
    public double getBeta(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime();
        double beta  = cpath/ctime/30.0;
        return beta;
    }
    
    public double getMass(DetectorType type){
        double mass2 = this.getMass2(type);
        if(mass2<0) return Math.sqrt(-mass2);
        return Math.sqrt(mass2);
    }
    
    public double getMass2(DetectorType type){
        double beta   = this.getBeta(type);
        double energy = this.getEnergy(type);
        double mass2  = this.particleMomenta.mag2()/(beta*beta) - this.particleMomenta.mag2();
        return mass2;
    }
    
    public void setStatus(int status){this.particleStatus = status;}
    public void setBeta(double beta){ this.particleBeta = beta;}
    public void setMass(double mass){ this.particleMass = mass;}
    public void setPid(int pid){this.particlePID = pid;}
    public void setCharge(int charge) { this.particleCharge = charge;}
    
    public void setCross(double x, double y, double z,
            double ux, double uy, double uz){
        this.particleCrossPosition.setXYZ(x, y, z);
        this.particleCrossDirection.setXYZ(ux, uy, uz);
    }
    
    public int getDetectorHit(List<DetectorResponse>  hitList, DetectorType type,
            int detectorLayer,
            double distanceThreshold){
        
        Line3D   trajectory = new Line3D(
                this.particleCrossPosition.x(),
                this.particleCrossPosition.y(),
                this.particleCrossPosition.z(),
                this.particleCrossDirection.x()*1500.0,
                this.particleCrossDirection.y()*1500.0,
                this.particleCrossDirection.z()*1500.0
        );
        
        Point3D  hitPoint = new Point3D();
        double   minimumDistance = 500.0;
        int      bestIndex       = -1;
        for(int loop = 0; loop < hitList.size(); loop++){
            //for(DetectorResponse response : hitList){
            DetectorResponse response = hitList.get(loop);
            if(response.getDescriptor().getType()==type&&
                    response.getDescriptor().getLayer()==detectorLayer){
                hitPoint.set(
                        response.getPosition().x(),
                        response.getPosition().y(),
                        response.getPosition().z()
                        );
                double hitdistance = trajectory.distance(hitPoint).length();
                //System.out.println(" LOOP = " + loop + "   distance = " + hitdistance);
                if(hitdistance<distanceThreshold&&hitdistance<minimumDistance){
                    minimumDistance = hitdistance;
                    bestIndex       = loop;
                }
            }
        }
        return bestIndex;
    }
    /**
     * returns DetectorResponse that matches closely with the trajectory
     * @param responses
     * @return 
     */
    public DetectorResponse getDetectorResponse(List<DetectorResponse> responses){
        int index = this.getDetectorHitIndex(responses);
        return responses.get(index);
    }
    /**
     * Finds the index of the best matching detector response object from the list.
     * @param responses
     * @return 
     */
    public int  getDetectorHitIndex(List<DetectorResponse> responses){
        Path3D   trajectory = this.getTrajectory();
        int       bestIndex = 0;
        Line3D    bestLine     = new Line3D(0.,0.,0.,1000.0,0.0,0.0);
        Point3D   hitPosition  = new Point3D();
        int       index        = 0;
        for(DetectorResponse res : responses){
            hitPosition.set(res.getPosition().x(), 
                    res.getPosition().y(),res.getPosition().z());
            Line3D distance = trajectory.distance(hitPosition);
            if(distance.length()<bestLine.length()){
                bestLine.copy(distance);
                bestIndex = index;
            }
            index++;
        }
        return bestIndex;
    }
    
    public Line3D  getDistance(DetectorResponse  response){
        Path3D trajectory = this.getTrajectory();
        Point3D hitPoint = new Point3D(
                response.getPosition().x(),response.getPosition().y(),response.getPosition().z());
        return trajectory.distance(hitPoint);
    }
    
    public void setPath(double path){
        this.particlePath = path;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("status = %4d  charge = %3d [pid/beta/mass] %5d %8.4f %8.4f",                 
                this.particleStatus,
                this.particleCharge,
                this.particlePID,
                this.particleBeta,this.particleMass));
        str.append(String.format("  P [ %8.4f %8.4f %8.4f ]  V [ %8.4f %8.4f %8.4f ] ",
                this.particleMomenta.x(),this.particleMomenta.y(),
                this.particleMomenta.z(),
                this.particleVertex.x(),this.particleVertex.y(),
                this.particleVertex.z()));
        str.append("\n");
        str.append(String.format("\t\t\t CROSS [%8.4f %8.4f %8.4f]  DIRECTION [%8.4f %8.4f %8.4f]\n",
                this.particleCrossPosition.x(),this.particleCrossPosition.y(),
                this.particleCrossPosition.z(),this.particleCrossDirection.x(),
                this.particleCrossDirection.y(),this.particleCrossDirection.z()));
        for(DetectorResponse res : this.responseStore){
            str.append(res.toString());
            str.append("\n");
        }
        
        return str.toString();
    }

    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
