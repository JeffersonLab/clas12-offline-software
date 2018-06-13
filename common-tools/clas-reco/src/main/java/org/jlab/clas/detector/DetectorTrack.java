package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class DetectorTrack {

    public class TrajectoryPoint {
        private int trackId = -1;
        private int detId = -1;
        private Line3D traj=null;
        private float bField = 0;
        private float pathLength = -1;
        public TrajectoryPoint(int trackId,int detId,Line3D traj,float bField,float pathLength) {
            this.trackId=trackId;
            this.detId=detId;
            this.traj=traj;
            this.bField=bField;
            this.pathLength=pathLength;
        }
        public TrajectoryPoint(int trackId,int detId,Line3D traj) {
            this.trackId=trackId;
            this.detId=detId;
            this.traj=traj;
        }
        public int getTrackId() { return trackId; }
        public int getDetId() { return detId; }
        public Line3D getCross() { return traj; }
        public float getBField() { return bField; }
        public float getPathLength() { return pathLength; }
    }
   
    private int     trackSector = 0;
    private int     trackAssociation = -1;
    private int     trackIndex = -1;
    private int     trackCharge = 0;
    private double  trackMom    = 0.0;
    private double  trackPath   = 0.0;
    private double  taggerTime  = 0.0;
    private int     taggerID    = 0;
    private double  trackchi2   = 0.0;
    private int     ndf         = 0;
    private int     trackStatus = 0;
    private int     trackDetectorID = -1;
    
    private Vector3D   trackEnd = new Vector3D();
    private Vector3      trackP = new Vector3();
    private Vector3 trackVertex = new Vector3();

    private float[][] covMatrix = new float[5][5];
    private List<Line3D> trackCrosses = new ArrayList<Line3D>();
   
    private List<TrajectoryPoint> trajectory = new ArrayList<TrajectoryPoint>();
    
    private double MAX_LINE_LENGTH = 1500.0;

    public void setCovMatrix(int ii,int jj,float val) {
        covMatrix[ii][jj] = val;
    }
    public void setCovMatrix(float[][] matrix) {
        covMatrix = matrix;
    }
    public float getCovMatrix(int ii, int jj) {
        return covMatrix[ii][jj];
    }
    public float[][] getCovMatrix() {
        return covMatrix;
    }

    public double getMaxLineLength() {
        return MAX_LINE_LENGTH;
    }
    
     public DetectorTrack(int charge){
        this.trackCharge = charge;
    }
    
    public DetectorTrack(int charge, double mom){
        this.trackMom = mom;
        this.trackCharge = charge;
    }

    public DetectorTrack(int charge, double mom, int index){
        this.trackMom = mom;
        this.trackCharge = charge;
        this.trackIndex = index;
    }
    
    public DetectorTrack(int charge, double px, double py, double pz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
    }
    
    public DetectorTrack(int id, int charge, double px, double py, double pz){
        // FIXME
        // Woah, DetectorTrack should not have a taggerID, or a taggerAnything!!!
        this.taggerID = id;
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
    }

    public DetectorTrack(int charge, double px, double py, double pz,
            double vx, double vy, double vz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
        this.trackVertex.setXYZ(vx, vy, vz);
    }

    public void addTrajectoryPoint(int trackId,int detId,Line3D traj,float bField,float pathLength) {
        this.trajectory.add(new TrajectoryPoint(trackId,detId,traj,bField,pathLength));
    }
    public void addTrajectoryPoint(int trackId,int detId,Line3D traj) {
        this.trajectory.add(new TrajectoryPoint(trackId,detId,traj));
    }

    public Line3D getTrajectoryPoint(int detId) {
        for (TrajectoryPoint tp : trajectory) {
            if (tp.getDetId() == detId) {
                return tp.getCross();
            }
        }
        return null;
    }
    
    public List<TrajectoryPoint> getTrajectory() {
        return this.trajectory;
    }
    
    public DetectorTrack setCharge(int charge){
        this.trackCharge = charge;
        return this;
    }
    
    public DetectorTrack setP(double p){
        this.trackMom = p;
        return this;
    }
    
    public DetectorTrack setVector(double px, double py, double pz){
        this.trackP.setXYZ(px, py, pz);
        return this;
    }
    
    public DetectorTrack setVertex(double vx, double vy, double vz){
        this.trackVertex.setXYZ(vx, vy, vz);
        return this;
    }
    
    public DetectorTrack setPath(double path){
        this.trackPath = path;
        return this;
    }
    
    public DetectorTrack setTime(double time){
        this.taggerTime = time;
        return this;
    }

    public DetectorTrack setSector(int sector){
        this.trackSector = sector;
        return this;
    }
    
    public DetectorTrack setID(int id){
        this.taggerID = id;
        return this;
    }
    
    public DetectorTrack setTrackEnd(double x, double y, double z){
        this.trackEnd.setXYZ(x, y, z);
        return this;
    }
    
    public void     setNDF(int x) {this.ndf = x;}
    public void     setStatus(int x) {this.trackStatus = x;}
    public void     setchi2(double x) {this.trackchi2 = x;}
    public void     setAssociation(int x) {this.trackAssociation = x;}
    public void     setDetectorID(int id) {this.trackDetectorID = id;}
    
    public int      getCharge()     {return this.trackCharge;}
    public int      getNDF()        {return this.ndf;}
    public int      getStatus()     {return this.trackStatus;}
    public double   getchi2()       {return this.trackchi2;}
    public double   getP()          {return this.trackP.mag();} 
    public int      getID()         {return this.taggerID;}
    public double   getPath()       {return this.trackPath;}
    public int      getSector()     {return this.trackSector;}
    public Vector3  getVector()     {return this.trackP;}
    public Vector3  getVertex()     {return this.trackVertex;}
    public Vector3D getTrackEnd()   {return this.trackEnd;}
    public int      getAssociation(){return this.trackAssociation;}
    public int      getDetectorID() {return this.trackDetectorID;}
    
    
    public void addCross(double x, double y, double z,
            double ux, double uy, double uz){
        Line3D line = new Line3D(x,y,z,
                x + ux*this.MAX_LINE_LENGTH,
                y + uy*this.MAX_LINE_LENGTH,
                z + uz*this.MAX_LINE_LENGTH
        );
        this.trackCrosses.add(line);
    }
    
    public int getCrossCount(){ return this.trackCrosses.size();}
    
    public Line3D getCross(int index){
        return this.trackCrosses.get(index);
    }
    
    public Line3D getFirstCross(){
        return trackCrosses.get(0);
    }
    
    public int getTrackIndex() {
        return this.trackIndex;
    }
    
    public Line3D getLastCross(){
        return this.trackCrosses.get(this.trackCrosses.size()-1);
    }
    
    
    
    @Override
    public String toString(){
       StringBuilder  str = new StringBuilder();
       str.append(String.format("[Track] >>>> c = %2d, p = %7.3f,", 
               getCharge(),getP()));
       str.append(String.format(" path = %7.2f", getPath()));
       return str.toString();
    }
}
