package org.jlab.clas.detector;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
 */
public class DetectorParticle implements Comparable {
    
    private Integer particlePID       = 0;
    private Integer particleStatus    = 1;
    private Integer particleTrackIndex = -1;
    private Double  particleBeta      = 0.0;
    private Double  particleMass      = 0.0;
    private Double  particleIDQuality = 0.0;
    private Double  particlePath      = 0.0; 
    private int     particleScore     = 0; // scores are assigned detector hits
    private double  particleScoreChi2 = 0.0; // chi2 for particle score 
    
    private Vector3 particleCrossPosition  = new Vector3();
    private Vector3 particleCrossDirection = new Vector3();
    
    private Line3D  driftChamberEnter = new Line3D();
    
//    private Point3D ctofIntersection = new Point3D();
    
    private double[]  covMAT = new double[15];
    
    private List<DetectorResponse>    responseStore = new ArrayList<DetectorResponse>();
    private List<CherenkovResponse>  cherenkovStore = new ArrayList<CherenkovResponse>();
    private List<TaggerResponse>     taggerStore = new ArrayList<TaggerResponse>();

//    private List<ScintillatorResponse>    scintillatorStore = new ArrayList<ScintillatorResponse>();
//    private List<CalorimeterResponse>  calorimeterStore = new ArrayList<CalorimeterResponse>();
   
    private TreeMap<DetectorType,Vector3>  projectedHit = 
            new  TreeMap<DetectorType,Vector3>();
    
    private DetectorTrack detectorTrack = null;
    private TaggerResponse taggerTrack = null;
    
    public DetectorParticle(){
        detectorTrack = new DetectorTrack(-1);
    }
    
    public DetectorParticle(DetectorTrack track){
        detectorTrack = track;
    }
   
    public DetectorParticle(DetectorTrack track, double[] covMat) {
        detectorTrack = track;
        covMAT = covMat;
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
    
    public static DetectorParticle createNeutral(double x, double y, double z){
        Vector3D dir = new Vector3D(x,y,z);
        dir.unit();
        DetectorTrack track = new DetectorTrack(0,1.0);
        track.addCross(x, y, z, dir.x(),dir.y(),dir.z());
        track.setVector(dir.x(), dir.y(), dir.z());
        track.setPath(Math.sqrt(x*x+y*y+z*z));
        track.setTrackEnd(x, y, z);
        return new DetectorParticle(track);
    }
    
    public static DetectorParticle createNeutral(DetectorResponse resp){
        Vector3D dir = new Vector3D(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z());
        dir.unit();
        DetectorTrack track = new DetectorTrack(0,1.0);
        track.addCross(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z(),
                dir.x(),dir.y(),dir.z());
        track.setVector(dir.x(), dir.y(), dir.z());
        track.setVertex(0.0, 0.0, 0.0);
        track.setPath(resp.getPosition().mag());
        track.setTrackEnd(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z());
        DetectorParticle particle = new DetectorParticle(track);
        particle.addResponse(resp);
        return particle;
    }

    public static DetectorParticle createNeutral(CalorimeterResponse resp){
        Vector3D dir = new Vector3D(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z());
        dir.unit();
        DetectorTrack track = new DetectorTrack(0,1.0);
        track.addCross(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z(),
                dir.x(),dir.y(),dir.z());
        track.setVector(dir.x(), dir.y(), dir.z());
        track.setVertex(0.0, 0.0, 0.0);
        track.setPath(resp.getPosition().mag());
        track.setTrackEnd(resp.getPosition().x(),
                resp.getPosition().y(),resp.getPosition().z());
        DetectorParticle particle = new DetectorParticle(track);
//        particle.addCalorimeterResponse(resp);
        particle.addResponse(resp);
        return particle;
    }
    
    public static DetectorParticle createFTparticle(TaggerResponse tagger) {

        // FIXME:
        //
        // This "taggerTrack" naming is not good:
        // Any "track" should be a DetectorTrack (or at least inherit from it).
        //
        // TaggerResponse should be based on FT::particle, not just FT::cluster
       
        Point3D xyz = tagger.getPosition();
        Vector3D mom = tagger.getMomentum();
        Vector3D dir=new Vector3D(mom);
        dir.unit();

        // copied from createNeutral / processNeutralTracks:
        DetectorTrack track = new DetectorTrack(0,1.0);
        track.addCross(xyz.x(), xyz.y(), xyz.z(), dir.x(),dir.y(),dir.z());
        track.setVector(dir.x(),dir.y(),dir.z());
        track.setVertex(0.0, 0.0, 0.0);
        track.setPath(xyz.distance(new Point3D(0,0,0)));
        track.setTrackEnd(xyz.x(),xyz.y(),xyz.z());
        track.setVector(mom.x(),mom.y(),mom.z());
        track.setP(mom.r());
        
        DetectorParticle particle = new DetectorParticle(track);
        particle.taggerTrack=tagger;

        // FIXME: Use FT::particle instead of FT::cluster, then stop assuming charge=0, pid=22
        particle.setPid(22);

        return particle;
    }
    
    
    public void clear(){
        this.responseStore.clear();
    }
    
    public List<CherenkovResponse> getCherenkovResponse(){
        return this.cherenkovStore;
    }
    
    
    
//    public List<CalorimeterResponse> getCalorimeterResponse(){
//        return this.calorimeterStore;
//    }
    
//    public List<ScintillatorResponse> getScintillatorResponse(){
//        return this.scintillatorStore;
//    }
    
    public void addCherenkovResponse(CherenkovResponse res){
        this.cherenkovStore.add(res);
    }
    
    public void addTaggerResponse(TaggerResponse res) {
        this.taggerStore.add(res);
    }
    
//        public void addCalorimeterResponse(CalorimeterResponse res){
//        this.calorimeterStore.add(res);
//    }
    
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
    
    public double[] getTBCovariantMatrix() {
        return this.covMAT;
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
    
    public int getSector(){
        if(this.hasHit(DetectorType.ECAL, 1)==true){
            return getHit(DetectorType.ECAL, 1).getDescriptor().getSector();
        }
        return 0;
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
        if(hits>1 && type!=DetectorType.CTOF){
            // don't warn for CTOF, since it currently doesn't do clustering
            System.out.println("[Warning] Too many hits for detector type = " + type);
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
            System.out.println("[Warning] Too many hits for detector type = " + type);
        }
        return true;
    }
    
    public List<DetectorResponse>  getDetectorResponses(){
        return this.responseStore;
    }
    
    public List<CherenkovResponse> getCherenkovResponses(){
        return this.cherenkovStore;
    }
    
    public List<TaggerResponse> getTaggerResponses() {
        return this.taggerStore;
    }
 
//    public List<CalorimeterResponse>  getCalorimeterResponses(){
//        return this.calorimeterStore;
//    }
//    
//    public List<ScintillatorResponse> getScintillatorResponses(){
//        return this.scintillatorStore;
//    }    
    
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
    public double getNDF() {return this.detectorTrack.getNDF();}
    public double getTrackChi2() {return this.detectorTrack.getchi2();}
    public int    getStatus(){ return this.particleStatus;}
    public int    getTrackDetector() {return this.detectorTrack.getDetectorID();}
    public double getMass(){ return this.particleMass;}
    public int    getPid(){ return this.particlePID;}
    public double getPidQuality() {return this.particleIDQuality;}
    public void   setPidQuality(double q) {this.particleIDQuality = q;}

    public TaggerResponse getTaggerResponse(){ return this.taggerTrack; }
    public Point3D getTaggerPosition() {return this.taggerTrack.getPosition();}
    public Point3D getTaggerPositionWidth() {return this.taggerTrack.getPositionWidth();}
    public double  getTaggerRadius() {return this.taggerTrack.getRadius();}
    public double  getTaggerSize() {return this.taggerTrack.getSize();}
    public double  getTaggerIndex() {return this.taggerTrack.getHitIndex();}
    public double  getTaggerTime() {return this.taggerTrack.getTime();}
    public double  getTaggerEnergy() {return this.taggerTrack.getEnergy();}

    
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
    
    public void setStatus() {

        final int centralStat=4000;
        final int forwardStat=2000;
        final int taggerStat=1000;

        int status = 0;
        int trackType = -1;
        if (this.detectorTrack!=null) trackType = this.detectorTrack.getDetectorID();

        // central:
        if (this.hasHit(DetectorType.BMT)  ||
            this.hasHit(DetectorType.BST)  ||
            this.hasHit(DetectorType.CVT)  ||
            this.hasHit(DetectorType.CTOF) ||
            this.hasHit(DetectorType.CND)  ||
            this.hasHit(DetectorType.RTPC)) {
                status += centralStat;
        }
        else if (this.detectorTrack!=null &&
                DetectorType.getType(trackType)==DetectorType.CVT) {
                status += centralStat;
        }

        // forward:
        if (this.hasHit(DetectorType.DC)     ||
            this.hasHit(DetectorType.FMT)    ||
            this.hasHit(DetectorType.ECAL,1) ||
            this.hasHit(DetectorType.ECAL,4) ||
            this.hasHit(DetectorType.ECAL,7) ||
            this.hasHit(DetectorType.FTOF,1) ||
            this.hasHit(DetectorType.FTOF,2) ||
            this.hasHit(DetectorType.FTOF,3) ||
            this.hasHit(DetectorType.HTCC)   ||
            this.hasHit(DetectorType.LTCC)   ||
            this.hasHit(DetectorType.RICH)) {
            status += forwardStat;
        }
        else if (this.detectorTrack!=null && 
                DetectorType.getType(trackType)==DetectorType.DC) {
            status += forwardStat;
        }

        // tagger:
        if (this.hasHit(DetectorType.FT)   ||
            this.hasHit(DetectorType.FTCAL)  ||
            this.hasHit(DetectorType.FTHODO) ||
            this.hasHit(DetectorType.FTTRK)) {
            status += taggerStat;
        }
        this.particleStatus = status;
    }
    
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
               response.getDescriptor().getLayer()==detectorLayer &&
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
        for(CherenkovResponse c : this.cherenkovStore){
            if(c.getCherenkovType()==type){
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

    public int getCherenkovSignal(List<CherenkovResponse> cherenkovs, DetectorType type){

        // choose cross based on detector type:
        Line3D cross;
        if (type==DetectorType.HTCC) 
            cross=this.detectorTrack.getFirstCross();
        else if (type==DetectorType.LTCC)
            cross=this.detectorTrack.getLastCross();
        else throw new RuntimeException(
                "DetectorParticle:getCheckr5noSignal:  invalid type:  "+type);

        // find the best match:
        int bestIndex = -1;
        if(cherenkovs.size()>0){
            for(int loop = 0; loop < cherenkovs.size(); loop++) {
                if(cherenkovs.get(loop).getCherenkovType()==type){
                    boolean matchtruth = cherenkovs.get(loop).match(cross);
                    if(matchtruth==true){
                        bestIndex = loop;
                        // FIXME keep the first match!
                        break;
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
        throw new UnsupportedOperationException("Not supported yet.");
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
