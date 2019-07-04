package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class DetectorTrack implements Comparable {

    public class TrajectoryPoint {
        private int detId = -1;
        private int layId = -1;
        private Line3D traj=null;
        private float bField = 0;
        private float pathLength = -1;
        public TrajectoryPoint(int detId,int layId,Line3D traj,float bField,float pathLength) {
            this.detId=detId;
            this.layId=layId;
            this.traj=traj;
            this.bField=bField;
            this.pathLength=pathLength;
        }
        public TrajectoryPoint(int detId,int layId,Line3D traj) {
            this.layId=layId;
            this.detId=detId;
            this.traj=traj;
        }
        public int getDetectorId() { return detId; }
        public int getLayerId()  { return layId; }
        public Line3D getCross() { return traj; }
        public float getBField() { return bField; }
        public float getPathLength() { return pathLength; }
    }
   
    private int     trackSector = 0;
    private int     trackAssociation = -1;
    private int     trackIndex = -1;
    private int     trackCharge = 0;
    private double  trackPath   = 0.0;
    private double  trackchi2   = 0.0;
    private int     ndf         = 0;
    private int     trackStatus = -1;
    private int     trackDetectorID = -1;
    
    private Vector3D   trackEnd = new Vector3D();
    private Vector3      trackP = new Vector3();
    private Vector3 trackVertex = new Vector3();

    private float[][] covMatrix = new float[5][5];
    private List<Line3D> trackCrosses = new ArrayList<>();
  
    private List<TrajectoryPoint> trajectory = new ArrayList<>();
    
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
        this.trackCharge = charge;
    }

    public DetectorTrack(int charge, double mom, int index){
        this.trackCharge = charge;
        this.trackIndex = index;
    }
    
    public DetectorTrack(int charge, double px, double py, double pz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
    }
    
    public DetectorTrack(int charge, double px, double py, double pz,
            double vx, double vy, double vz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
        this.trackVertex.setXYZ(vx, vy, vz);
    }

    public void addTrajectoryPoint(int detId,int layId,Line3D traj,float bField,float pathLength) {
        this.trajectory.add(new TrajectoryPoint(detId,layId,traj,bField,pathLength));
    }
    public void addTrajectoryPoint(int detId,int layId,Line3D traj) {
        this.trajectory.add(new TrajectoryPoint(detId,layId,traj));
    }

    public Line3D getTrajectoryPoint(int detId,int layId) {
        for (int ii=0; ii<this.trajectory.size(); ii++) {
            if (this.trajectory.get(ii).getDetectorId()==detId &&
                this.trajectory.get(ii).getLayerId()==layId) {
                return this.trajectory.get(ii).getCross();
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
    
    public DetectorTrack setSector(int sector){
        this.trackSector = sector;
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
    public int compareTo(Object o) {
        DetectorTrack other = (DetectorTrack) o;
        if (this.getVector().mag() == other.getVector().mag()) return 0;
        else return this.getVector().mag() > other.getVector().mag() ? -1 : 1;
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
