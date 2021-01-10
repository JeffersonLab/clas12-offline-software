package org.jlab.clas.detector;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.DetectorDescriptor;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class DetectorParticle implements Comparable {
  
    public static final Double DEFAULTQUALITY=9999.0;

    private boolean isTriggerParticle = false;
    private Integer particlePID       = 0;
    private Double  particleBeta      = 0.0;
    private Double  particleMass      = 0.0;
    private Double  particleIDQuality = DEFAULTQUALITY;
    private int     particleScore     = 0; // scores are assigned detector hits
    private double  particleScoreChi2 = 0.0; // chi2 for particle score 
    private double  startTime         = -1.0; // per-particle start-time
  
    public double getStartTime() { return this.startTime; }
    public void setStartTime(double time) {this.startTime=time; }
    
    private DetectorParticleStatus particleStatus = new DetectorParticleStatus();

    // let multiple particles share the same hit for these detectors:
    private final DetectorType[] sharedDetectors = {DetectorType.FTOF,DetectorType.CTOF};
    
    protected final List<DetectorResponse> responseStore = new ArrayList<>();

    protected DetectorTrack detectorTrack = null;
    
    public DetectorParticle(){
        detectorTrack = new DetectorTrack(-1);
    }
    
    public DetectorParticle(DetectorTrack track){
        detectorTrack = track;
    }
   
    public DetectorParticle(int charge, double px, double py, double pz){
        detectorTrack = new DetectorTrack(charge,px,py,pz);
    }
    
    public DetectorParticle(int charge, double px, double py, double pz,
            double vx, double vy, double vz){
        detectorTrack = new DetectorTrack(charge,px,py,pz,vx,vy,vz);
    }
    
    public static DetectorParticle createNeutral(
            double x,  double y,  double z,
            double vx, double vy, double vz){
        
        Vector3D dir = new Vector3D(x-vx,y-vy,z-vz);
        dir.unit();
        
        DetectorTrack track = new DetectorTrack(0,1.0);
        
        track.addCross(x, y, z, dir.x(),dir.y(),dir.z());
        
        track.setVector(dir.x(), dir.y(), dir.z());
        
        track.setVertex(vx,vy,vz);
        
        track.setPath(Math.sqrt(Math.pow(x-vx,2)+Math.pow(y-vy,2)+Math.pow(z-vz,2)));
        
        track.setTrackEnd(x, y, z);
        
        return new DetectorParticle(track);
    }
    
    public static DetectorParticle createNeutral(double x, double y, double z){
        return createNeutral(x,y,z,0,0,0);
    }
    
    public static DetectorParticle createNeutral(DetectorResponse resp){
        DetectorParticle particle = createNeutral(
                resp.getPosition().x(),
                resp.getPosition().y(),
                resp.getPosition().z());
        resp.setPath(resp.getPosition().mag());
        particle.addResponse(resp);
        return particle;
    }
    
    public static DetectorParticle createNeutral(DetectorResponse resp,Vector3 vertex){
        DetectorParticle particle = createNeutral(
                resp.getPosition().x(),
                resp.getPosition().y(),
                resp.getPosition().z(),
                vertex.x(),
                vertex.y(),
                vertex.z());
        // FIXME:  stop mixing Vector3 and Vector3D
        final double dx = resp.getPosition().x()-vertex.x();
        final double dy = resp.getPosition().y()-vertex.y();
        final double dz = resp.getPosition().z()-vertex.z();
        resp.setPath(Math.sqrt(dx*dx+dy*dy+dz*dz));
        particle.addResponse(resp);
        return particle;
    }
   
    public DetectorTrack.Trajectory getTrackTrajectory() {
        return detectorTrack.getTrajectory();
    }
    
    public void clear(){
        this.responseStore.clear();
    }
    
    public void addResponse(DetectorResponse res, boolean match){
        this.responseStore.add(res);
        if(match==true){
            Line3D distance = this.getDistance(res);
            res.getMatchedPosition().setXYZ(
                    distance.midpoint().x(),
                    distance.midpoint().y(),distance.midpoint().z());
            res.setPath(this.getPathLength(res.getPosition()));
        }
    }

    public int countResponses(DetectorType type,int layer) {
        int nResponses=0;
        for (int ii=0; ii<responseStore.size(); ii++) {
            DetectorDescriptor desc=responseStore.get(ii).getDescriptor();
            if (desc.getType()!=type) continue;
            if (desc.getLayer()!=layer) continue;
            nResponses++;
        }
        return nResponses;
    }
    
    public int countResponses(DetectorType type) {
        int nResponses=0;
        for (int ii=0; ii<responseStore.size(); ii++) {
            DetectorDescriptor desc=responseStore.get(ii).getDescriptor();
            if (desc.getType()!=type) continue;
            nResponses++;
        }
        return nResponses;
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
    
    public int getTrackIndex() {
        return this.detectorTrack.getTrackIndex();
    }

    public float[][] getCovMatrix() {
        return this.detectorTrack.getCovMatrix();
    }

    public float getCovMatrix(int ii, int jj) {
        return this.detectorTrack.getCovMatrix(ii,jj);
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
     * returns whether this is the trigger particle.
     */
    public boolean isTriggerParticle() {
        return isTriggerParticle;
    }

    /**
     *
     * set this as the trigger particle.
     *
     */
    public void setTriggerParticle(boolean val) {
        isTriggerParticle=val;
        particleStatus.setTriggerParticle(val);
    }
    
    public int getSector(DetectorType type,int layer) {
        DetectorResponse hit = this.getHit(type,layer);
        return hit==null ? 0 : hit.getSector();
    }

    public int getSector(DetectorType type) {
        return this.getSector(type,-1);
    }

    /**
     * @deprecated
     * Just for backward compatibility for any external usage
     */
    public int getSector(){
        return this.getSector(DetectorType.ECAL,1);
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

    public boolean hasHit(DetectorType type){
        int hits = 0;
        for( DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type) hits++;
        }
        if(hits==0) return false;
        if(hits>1 && type!=DetectorType.CTOF && type!=DetectorType.ECAL){
            // don't warn for CTOF, since it currently doesn't do clustering
            // don't warn for ECAL, since it has multiple layers
            System.out.println("[Warning] DetectorParticle.hasHit(type): Too many hits for detector type = " + type);
        }
        return true;
    }
    
    public boolean hasHit(DetectorType type, int layer){
        int hits = 0;
        for( DetectorResponse res : this.responseStore){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer) hits++;
        }
        if(hits==0) return false;
        if(hits>1 && type!=DetectorType.CTOF){
            // don't warn for CTOF, since it currently doesn't do clustering
            System.out.println("[Warning] DetectorParticle.hasHit(type,layer): Too many hits for detector type = " + type);
        }
        return true;
    }
    
    public List<DetectorResponse>  getDetectorResponses(){
        return this.responseStore;
    }
    
    public DetectorResponse getHit(DetectorType type){
        return getHit(type,-1);
    }
   
    public DetectorResponse getHit(DetectorType type, int layer) {
        for (DetectorResponse res : this.responseStore) {
            if (res.getDescriptor().getType() != type) continue;
            if (layer > 0 && res.getDescriptor().getLayer() != layer) continue;
            return res;
        }
        return null;
    }
    /**
     * @deprecated
     * Just for backward compatibility for any external usage
     */
    public DetectorResponse  getResponse(DetectorType type, int layer){
        return this.getHit(type,layer);
    }
   
    public DetectorTrack getTrack() {return this.detectorTrack; }
    public double getBeta(){ return this.particleBeta;}
    public double getNDF() {return this.detectorTrack.getNDF();}
    public double getTrackChi2() {return this.detectorTrack.getchi2();}
    public int    getTrackDetector() {return this.detectorTrack.getDetectorID();}
    public int    getTrackSector() {return this.detectorTrack.getSector();}
    public int    getTrackDetectorID() {return this.detectorTrack.getDetectorID();}
    public int    getTrackStatus() {return this.detectorTrack.getStatus();}
    public Line3D getFirstCross() {return this.detectorTrack.getFirstCross();}
    public Line3D getLastCross() {return this.detectorTrack.getLastCross();}

    public DetectorParticleStatus getStatus(){ return this.particleStatus;}
    
    public double getMass(){ return this.particleMass;}
    public int    getPid(){ return this.particlePID;}
    public double getPidQuality() {return this.particleIDQuality;}
    public void   setPidQuality(double q) {this.particleIDQuality = q;}
    
    public Vector3  vector(){return detectorTrack.getVector();}    
    public Vector3  vertex(){return detectorTrack.getVertex();}
    
    public double   getPathLength(){ return detectorTrack.getPath();}
    public int      getCharge(){ return detectorTrack.getCharge();}
 
    public double   getPathLength(Vector3D vec){
        return this.getPathLength(vec.x(), vec.y(), vec.z());
    }
    
    public double   getPathLength(double x, double y, double z){
        double crosspath = Math.sqrt(
                (this.detectorTrack.getLastCross().origin().x()-x)*
                (this.detectorTrack.getLastCross().origin().x()-x)
              + (this.detectorTrack.getLastCross().origin().y()-y)*
                (this.detectorTrack.getLastCross().origin().y()-y)
              + (this.detectorTrack.getLastCross().origin().z()-z)*
                (this.detectorTrack.getLastCross().origin().z()-z)
        );
        return this.detectorTrack.getPath() + crosspath;
    }
    
    public double getTime(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        return response.getTime();
    }
    
    public double getEnergyFraction(DetectorType type){
        double energy = this.getEnergy(type);
        if(this.vector().mag()<0.00001) return 0.0;
        return energy/this.vector().mag();
    }
    
    public double getEnergy(DetectorType type){
        double energy = 0.0;
        for(DetectorResponse r : this.responseStore){
            if(r.getDescriptor().getType()==type){
                energy += r.getEnergy();
            }
        }
        return energy;
    }
    
    public double getEnergy(DetectorType type, int layer){
        double energy = 0.0;
        for(DetectorResponse r : this.responseStore) {
            if (r.getDescriptor().getType()==type &&
               r.getDescriptor().getLayer()==layer) {
                energy += r.getEnergy();
            }
        }
        return energy;
    }
   
    public double getPathLength(DetectorType type, int layer){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        return this.getPathLength(response.getPosition());
    } 
    public double getBeta(DetectorType type, int layer, double startTime){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime() - startTime;
        double beta  = cpath/ctime/PhysicsConstants.speedOfLight();
        return beta;
    }

    public void setStatus(double minNpheHtcc,double minNpheLtcc) {
        this.particleStatus = DetectorParticleStatus.create(this,minNpheHtcc,minNpheLtcc);
    }

    public void setBeta(double beta){ this.particleBeta = beta;}
    public void setMass(double mass){ this.particleMass = mass;}
    public void setPid(int pid){this.particlePID = pid;}
    public void setCharge(int charge) { this.detectorTrack.setCharge(charge);}
    
    public int getDetectorHit(List<DetectorResponse>  hitList, DetectorType type,
            int detectorLayer,
            double distanceThreshold){
         
        Line3D   trajectory = this.detectorTrack.getLastCross();
        Point3D  hitPoint = new Point3D();
        double   minimumDistance = 500.0;
        int      bestIndex       = -1;

        boolean hitSharing=false;
        for (int ii=0; ii<sharedDetectors.length && this.getCharge()!=0; ii++) {
            if (type == sharedDetectors[ii]) {
                hitSharing=true;
                break;
            }
        }

        for(int loop = 0; loop < hitList.size(); loop++){
           
            DetectorResponse response = hitList.get(loop);
 
            // same-sector requirement between hit and track:
            if (response.getSector()>0 && this.detectorTrack.getSector()>0) {
              if (response.getSector() != this.detectorTrack.getSector()) {
                  continue;
              }
            }
            
            if(response.getDescriptor().getType()==type &&
               (detectorLayer<=0 || response.getDescriptor().getLayer()==detectorLayer) &&
               (hitSharing || response.getAssociation()<0)) {
                hitPoint.set(
                        response.getPosition().x(),
                        response.getPosition().y(),
                        response.getPosition().z()
                        );
                double hitdistance = trajectory.distance(hitPoint).length();
                if (hitdistance<distanceThreshold && hitdistance<minimumDistance) {
                    minimumDistance = hitdistance;
                    bestIndex       = loop;
                }
            }
        }
        return bestIndex;
    }
    
    public double getDetectorHitQuality(List<DetectorResponse>  hitList, int index, Vector3D hitRes){
        
        Line3D   trajectory = this.detectorTrack.getLastCross();
        Point3D  hitPoint = new Point3D();

        DetectorResponse response = hitList.get(index);
        hitPoint.set(
                response.getPosition().x(),
                response.getPosition().y(),
                response.getPosition().z()
        );
        Point3D poca = trajectory.distance(hitPoint).origin(); //Point of Closest Approach
        double dx = poca.x() - hitPoint.x();
        double dy = poca.y() - hitPoint.y();
        double dz = poca.z() - hitPoint.z();
        
        return pow(dx,2)/pow(hitRes.x(),2) + pow(dy,2)/pow(hitRes.y(),2) + pow(dz,2)/pow(hitRes.z(),2);
    }
    
    public Line3D  getDistance(DetectorResponse  response){
        Line3D cross = this.detectorTrack.getLastCross();
        Line3D  dist = cross.distanceRay(response.getPosition().toPoint3D());
        return dist;
    }

    public double getTheoryBeta(int id){
        final double p    = detectorTrack.getVector().mag();
        final double mass = PDGDatabase.getParticleById(id).mass();
        final double beta = p/sqrt(p*p + mass*mass);
        return beta;
    }   

    public double getNphe(DetectorType type){
        double nphe = 0;
        for(DetectorResponse c : this.responseStore){
            if(c.getDescriptor().getType() == type){
                nphe = c.getEnergy();
                // FIXME: this is choosing the last match 
                // should instead either be a += or a break
            }
        }
        return nphe;
    }

    public double getVertexTime(DetectorType type, int layer){
        double vertex_time = this.getTime(type,layer) -
                this.getPathLength(type, layer) /
                (this.getTheoryBeta(this.getPid())*PhysicsConstants.speedOfLight());
        return vertex_time;
    }
    
    public double getVertexTime(DetectorType type, int layer, int pid){
        double vertex_time = -9999;
        if(type==DetectorType.CTOF) {
            DetectorResponse res = this.getHit(type);
            vertex_time = this.getTime(type) - res.getPath()/
                    (this.getTheoryBeta(pid)*PhysicsConstants.speedOfLight());
        }
        else {
            vertex_time = this.getTime(type,layer) -
                    this.getPathLength(type, layer) /
                    (this.getTheoryBeta(pid)*PhysicsConstants.speedOfLight());
        }
        return vertex_time;
    }

    public int getCherenkovSignal(List<DetectorResponse> responses, DetectorType type){

        Line3D cross;
        if (type==DetectorType.HTCC) {
            cross=this.detectorTrack.getFirstCross();
        }
        else if (type==DetectorType.LTCC)
            cross=this.detectorTrack.getLastCross();
        else throw new RuntimeException(
                "DetectorParticle:getCheckr5noSignal:  invalid type:  "+type);

        // find the best match:
        int bestIndex = -1;
        double bestConeAngle = Double.POSITIVE_INFINITY;
        if(responses.size()>0){
            for(int loop = 0; loop < responses.size(); loop++) {
                if (responses.get(loop).getDescriptor().getType() != type) continue;
                if (responses.get(loop).getAssociation()>=0) continue;
                CherenkovResponse cher = (CherenkovResponse)responses.get(loop);
                // FIXME:  use normalized distance/angle instead of box cut?
                // unify with non-Cherenkov?
                CherenkovResponse.TrackResidual tres = cher.getTrackResidual(cross);
                if (Math.abs(tres.getDeltaTheta()) < cher.getDeltaTheta() &&
                    Math.abs(tres.getDeltaPhi())   < cher.getDeltaPhi()) {
                    if (tres.getConeAngle() < bestConeAngle) {
                        bestIndex = loop;
                        bestConeAngle = tres.getConeAngle();
                    }
                }
            }
        }
        return bestIndex;
    } 

    public double getTime(DetectorType type, int layer) {
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        return response.getTime();
    }
    
    /**
     * Calculate beta for given detector type/layer, prioritized by layer:
     */
    public double getNeutralBeta(DetectorType type, List<Integer> layers,double startTime) {
        double beta=-9999;
        for (int layer : layers) {
            DetectorResponse resp = getHit(type,layer);
            if (resp!=null) {
                Vector3D hit=resp.getPosition().clone();
                double path=hit.sub(new Vector3D(vertex().x(),vertex().y(),vertex().z())).mag();
                beta = path / (resp.getTime()-startTime) /
                    PhysicsConstants.speedOfLight();
                break;
            }
        }
        return beta;
    }


    @Override
    public int compareTo(Object o) {

        DetectorParticle other = (DetectorParticle) o;

        // trigger particle takes highest priority:
        if (this.isTriggerParticle() && other.isTriggerParticle()) {
            throw new RuntimeException("Cannot have 2 trigger particles.");
        }
        else if (this.isTriggerParticle())  return -1;
        else if (other.isTriggerParticle()) return  1;

        // then charge ordering (-,+,0):
        else if (this.getCharge() != other.getCharge()) {
            if      (this.getCharge()  < 0) return -1;
            else if (other.getCharge() < 0) return  1;
            else if (this.getCharge()  > 0) return -1;
            else if (other.getCharge() > 0) return  1;
            else throw new RuntimeException("Impossible.");
        }

        // and then momentum ordering (largest to smallest):
        else if (this.vector().mag() == other.vector().mag()) return 0;
        else return this.vector().mag() > other.vector().mag() ? -1 : 1;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("[particle] id = %5d, c = %2d, p = %6.2f , sf = %6.3f, htcc = %5d, beta = %6.3f, mass2 = %8.3f\n",                
                this.getPid(), this.getCharge(),this.vector().mag(),this.getEnergyFraction(DetectorType.ECAL),
                this.getNphe(DetectorType.HTCC),this.getBeta(),this.getMass()));
        for(DetectorResponse res : this.responseStore){
            str.append(res.toString());
            str.append("\n");
        }

        return str.toString();
    }

}
