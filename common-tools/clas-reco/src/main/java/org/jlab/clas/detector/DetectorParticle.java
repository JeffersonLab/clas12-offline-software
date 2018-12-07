package org.jlab.clas.detector;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class DetectorParticle implements Comparable {
  
    public static final Double DEFAULTQUALITY=99.0;

    private boolean isTriggerParticle = false;
    private Integer particlePID       = 0;
    private Integer particleStatus    = 1;
    private Integer particleTrackIndex = -1;
    private Double  particleBeta      = 0.0;
    private Double  particleMass      = 0.0;
    private Double  particleIDQuality = DEFAULTQUALITY;
    private Double  particlePath      = 0.0; 
    private int     particleScore     = 0; // scores are assigned detector hits
    private double  particleScoreChi2 = 0.0; // chi2 for particle score 
    
    private Vector3 particleCrossPosition  = new Vector3();
    private Vector3 particleCrossDirection = new Vector3();
    
    private Line3D  driftChamberEnter = new Line3D();
    
    private List<DetectorResponse> responseStore = new ArrayList<DetectorResponse>();

    private DetectorTrack detectorTrack = null;
    
    public DetectorParticle(){
        detectorTrack = new DetectorTrack(-1);
    }
    
    public DetectorParticle(DetectorTrack track){
        detectorTrack = track;
    }
   
    public DetectorParticle(int charge, double px, double py, double pz){
        detectorTrack = new DetectorTrack(charge,px,py,pz);
    }
    
    public DetectorParticle(int id, int charge, double px, double py, double pz){
        detectorTrack = new DetectorTrack(id,charge,px,py,pz);
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
   
    public Map<Integer,DetectorTrack.TrajectoryPoint> getTrackTrajectory() {
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
            
            /*Vector3D vec = new Vector3D(
                    this.particleCrossPosition.x(),
                    particleCrossPosition.y(),
                    particleCrossPosition.z());
            */
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
    
    public void setLowerCross(double x, double y, double z, double ux, double uy, double uz){
        this.driftChamberEnter.set(x, y, z, x+1000.0*ux, y+1000.0*uy, z + 1000.0*uz);
    }
    
    public Line3D getLowerCross(){
        return this.driftChamberEnter;
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
    
    public double getBeta(){ return this.particleBeta;}
    public double getNDF() {return this.detectorTrack.getNDF();}
    public double getTrackChi2() {return this.detectorTrack.getchi2();}
    public int    getStatus(){ return this.particleStatus;}
    public int    getTrackDetector() {return this.detectorTrack.getDetectorID();}
    public int    getTrackSector() {return this.detectorTrack.getSector();}
    public int    getTrackDetectorID() {return this.detectorTrack.getDetectorID();}
    public int    getTrackStatus() {return this.detectorTrack.getStatus();}
    public Line3D getFirstCross() {return this.detectorTrack.getFirstCross();}
    public Line3D getLastCross() {return this.detectorTrack.getLastCross();}

    public double getMass(){ return this.particleMass;}
    public int    getPid(){ return this.particlePID;}
    public double getPidQuality() {return this.particleIDQuality;}
    public void   setPidQuality(double q) {this.particleIDQuality = q;}
    
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
    
    public Vector3  vector(){return detectorTrack.getVector();}    
    public Vector3  vertex(){return detectorTrack.getVertex();}
    
    public Vector3  getCross(){ return this.particleCrossPosition;}    
    public Vector3  getCrossDir(){ return this.particleCrossDirection;} 
    public double   getPathLength(){ return detectorTrack.getPath();}
    public int      getCharge(){ return detectorTrack.getCharge();}
    
    
    
    public double   getPathLength(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        return this.getPathLength(response.getPosition());
    }
    
    
    
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
    
    public double getBeta(DetectorType type, int layer, double startTime){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime() - startTime;
        double beta  = cpath/ctime/PhysicsConstants.speedOfLight();//30.0;
        return beta;
    }
    
    public double getBeta(DetectorType type, double startTime){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime() - startTime;
        double beta  = cpath/ctime/PhysicsConstants.speedOfLight();//30.0;
        if(type==DetectorType.CTOF){
            cpath = response.getPath();
            ctime = response.getTime()- startTime;
            beta = cpath/ctime/PhysicsConstants.speedOfLight(); 
        }
        return beta;
    }
    
    
    public double getBeta(DetectorType type){
        DetectorResponse response = this.getHit(type);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime();
        double beta  = cpath/ctime/PhysicsConstants.speedOfLight();//30.0;
        return beta;
    }
 

    
    public double getMass(DetectorType type,double startTime){
        double mass2 = this.getMass2(type,startTime);
        if(mass2<0) return Math.sqrt(-mass2);
        return Math.sqrt(mass2);
    }
    
    public double getMass(DetectorType type,int layer, double startTime){
        double mass2 = this.getMass2(type,layer,startTime);
        if(mass2<0) return Math.sqrt(-mass2);
        return Math.sqrt(mass2);
    }
    
    public double getMass(DetectorType type){
        double mass2 = this.getMass2(type);
        if(mass2<0) return Math.sqrt(-mass2);
        return Math.sqrt(mass2);
    }
    
    public double getMass2(DetectorType type,int layer, double startTime){
        double beta   = this.getBeta(type,layer,startTime);
        double energy = this.getEnergy(type);
        double mass2  = this.detectorTrack.getVector().mag2()/(beta*beta) 
                - this.detectorTrack.getVector().mag2();
        return mass2;
    }
    
    public double getMass2(DetectorType type,double startTime){
        double beta   = this.getBeta(type,startTime);
        double energy = this.getEnergy(type);
        double mass2  = this.detectorTrack.getVector().mag2()/(beta*beta) 
                - this.detectorTrack.getVector().mag2();
        return mass2;
    }
    
    public double getMass2(DetectorType type){
        double beta   = this.getBeta(type);
        double energy = this.getEnergy(type);
        double mass2  = this.detectorTrack.getVector().mag2()/(beta*beta) 
                - this.detectorTrack.getVector().mag2();
        return mass2;
    }
    
    public void setStatus(int status){this.particleStatus = status;}
    public void setBeta(double beta){ this.particleBeta = beta;}
    public void setMass(double mass){ this.particleMass = mass;}
    public void setPid(int pid){this.particlePID = pid;}
    public void setCharge(int charge) { this.detectorTrack.setCharge(charge);}
    
    public void setCross(double x, double y, double z,
            double ux, double uy, double uz){
        this.particleCrossPosition.setXYZ(x, y, z);
        this.particleCrossDirection.setXYZ(ux, uy, uz);
    }
    
    public int getDetectorHit(List<DetectorResponse>  hitList, DetectorType type,
            int detectorLayer,
            double distanceThreshold){
        
        Line3D   trajectory = this.detectorTrack.getLastCross();
        Point3D  hitPoint = new Point3D();
        double   minimumDistance = 500.0;
        int      bestIndex       = -1;

        for(int loop = 0; loop < hitList.size(); loop++){
           
            DetectorResponse response = hitList.get(loop);
            
            if(response.getDescriptor().getType()==type &&
               (detectorLayer<=0 || response.getDescriptor().getLayer()==detectorLayer) &&
               response.getAssociation()<0) {
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
    
    public double getDetectorHitQuality(List<DetectorResponse>  hitList, int index, Vector3D hitRes){
        
        Line3D   trajectory = this.detectorTrack.getLastCross();
        Point3D  hitPoint = new Point3D();
        double   chi2 = 0.0;

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
        
        chi2 = pow(dx,2)/pow(hitRes.x(),2) + pow(dy,2)/pow(hitRes.y(),2) + pow(dz,2)/pow(hitRes.z(),2);

        return chi2;
    }
    
    public Line3D  getDistance(DetectorResponse  response){
        Line3D cross = this.detectorTrack.getLastCross();
        Line3D  dist = cross.distanceRay(response.getPosition().toPoint3D());
        //Path3D trajectory = this.getTrajectory();
        //Point3D hitPoint = new Point3D(
        //response.getPosition().x(),response.getPosition().y(),response.getPosition().z());
        return dist;
    }

    public void setPath(double path){
        this.particlePath = path;
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

        // choose cross based on detector type:
        Line3D cross;
        if (type==DetectorType.HTCC) {
            cross=this.detectorTrack.getFirstCross();
            // 0 is detId for HTCC in TimeBasedTrkg::Trajectory bank!
            //cross=this.detectorTrack.getTrajectoryPoint(0);
            //if (cross==null) return -1;
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

    public double  getPathLength(DetectorType type, int layer){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        return this.getPathLength(response.getPosition());
    }  
    
    public int compareTo(Object o) {

        System.err.println("DetectorParticle:  Not ready for sorting!!!!!!!!!!!!!!!");

        DetectorParticle other = (DetectorParticle) o;

        // trigger particle takes highest priority:
        if (this.isTriggerParticle() && other.isTriggerParticle()) {
            throw new RuntimeException("Cannot have 2 trigger particles.");
        }
        else if (this.isTriggerParticle())  return -1;
        else if (other.isTriggerParticle()) return  1;

        // then charge ordering (-,+,0):
        else if (this.getCharge() != other.getCharge()) {
            if      (this.getCharge()  > 0) return -1;
            else if (other.getCharge() > 0) return  1;
            else if (this.getCharge()  < 0) return -1;
            else if (other.getCharge() < 0) return  1;
            else throw new RuntimeException("Impossible.");
        }

        // and then momentum ordering (largest to smallest):
        else if (this.vector().mag() == other.vector().mag()) return 0;
        else return this.vector().mag() > other.vector().mag() ? -1 : 1;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        /*
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
        */
        str.append(String.format("[particle] id = %5d, c = %2d, p = %6.2f , sf = %6.3f, htcc = %5d, beta = %6.3f, mass2 = %8.3f\n",                
                this.getPid(), this.getCharge(),this.vector().mag(),this.getEnergyFraction(DetectorType.ECAL),
                this.getNphe(DetectorType.HTCC),this.getBeta(),this.getMass()));
//        for(ScintillatorResponse res : this.scintillatorStore){
//            str.append(res.toString());
//            str.append("\n");
//        }
//        for(CalorimeterResponse res : this.calorimeterStore){
//            str.append(res.toString());
//            str.append("\n");
//        }
        for(DetectorResponse res : this.responseStore){
            str.append(res.toString());
            str.append("\n");
        }

        return str.toString();
    }

}
