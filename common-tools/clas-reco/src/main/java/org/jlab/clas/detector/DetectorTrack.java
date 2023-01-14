package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;

/**
 *
 * @author gavalian
 * @author baltzell
 */
public class DetectorTrack implements Comparable {

    public static class TrajectoryPoint {
        private int detId = -1;
        private int layId = -1;
        private Line3D traj=null;
        private float pathLength = -1;
        private float edge=-99;
        private float dx=-1;
        public TrajectoryPoint(DataBank bank, int row) {
            this.detId = bank.getInt("detector", row);
            this.layId = bank.getByte("layer", row);
            this.pathLength = bank.getFloat("path", row);
            this.edge = bank.getFloat("edge", row);
            this.dx  = bank.getFloat("dx", row);
            float xx = bank.getFloat("x", row);
            float yy = bank.getFloat("y", row);
            float zz = bank.getFloat("z", row);
            // cvt and dc have different trajectory banks:(
            boolean c = bank.getDescriptor().hasEntry("cx");
            this.traj = new Line3D(xx, yy, zz,
                    xx + MAX_LINE_LENGTH * bank.getFloat(c?"cx":"tx", row),
                    yy + MAX_LINE_LENGTH * bank.getFloat(c?"cy":"ty", row),
                    zz + MAX_LINE_LENGTH * bank.getFloat(c?"cz":"tz", row));
        }
        public TrajectoryPoint(int detId,int layId,Line3D traj,float pathLength,float edge,float dx) {
            this.detId=detId;
            this.layId=layId;
            this.traj=traj;
            this.pathLength=pathLength;
            this.edge=edge;
            this.dx=dx;
        }
        public int getDetectorId() { return detId; }
        public int getLayerId()  { return layId; }
        public Line3D getCross() { return traj; }
        public float getPathLength() { return pathLength; }
        public float getEdge() { return edge; }
        public float getDx() { return dx; }
    }

    public static class Trajectory {

        private int size=0;
        private Map < Integer, Map <Integer,TrajectoryPoint> > traj = new LinkedHashMap<>();
        
        public void add(TrajectoryPoint tp) {
            if (!this.traj.containsKey(tp.detId)) {
                this.traj.put(tp.detId,new LinkedHashMap<>());
            }
            if (this.traj.get(tp.detId).containsKey(tp.layId)) {
                throw new RuntimeException("Duplicate detector type/layer: "+tp.detId+"/"+tp.layId);
            }
            this.traj.get(tp.detId).put(tp.layId,tp);
            this.size++;
        }

        public Set<Integer> getDetectors() {
            return this.traj.keySet();
        }

        public Set<Integer> getLayers(int detId) {
            if (traj.containsKey(detId)) {
                return traj.get(detId).keySet();
            }
            return null;
        }
        
        public TrajectoryPoint get(int detId,int layId) {
            if (this.traj.containsKey(detId)) {
                if (this.traj.get(detId).containsKey(layId)) {
                    return this.traj.get(detId).get(layId);
                }
            }
            return null;
        }

        public TrajectoryPoint get(DetectorDescriptor dd) {
            return this.get(dd.getType().getDetectorId(),dd.getLayer());
        }

        public boolean contains(int detId) {
            return this.traj.containsKey(detId);
        }

        public boolean contains(int detId,int layId) {
            if (!this.contains(detId)) return false;
            return this.traj.get(detId).containsKey(layId);
        }

        public boolean contains(DetectorDescriptor dd) {
            return this.contains(dd.getType().getDetectorId(),dd.getLayer());
        }

        public boolean contains(DetectorResponse dr) {
            return this.contains(dr.getDescriptor());
        }

        public int size() {
            return this.size;
        }
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
 
    private Trajectory trajectory=new Trajectory();
    
    private static double MAX_LINE_LENGTH = 1500.0;

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

    public double getPathLength(DetectorType type,int layId) {
        if (!trajectory.contains(type.getDetectorId(),layId)) return -1.0;
        return trajectory.get(type.getDetectorId(),layId).getPathLength();
    }

    public TrajectoryPoint getTrajectoryPoint(int detId,int layId) {
        return this.trajectory.get(detId,layId);
    }

    public TrajectoryPoint getTrajectoryPoint(DetectorDescriptor dd) {
        return this.trajectory.get(dd);
    }
    
    public TrajectoryPoint getTrajectoryPoint(DetectorResponse dr) {
        return this.trajectory.get(dr.getDescriptor());
    }
    
    public Trajectory getTrajectory() {
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
   
    public DetectorTrack setVector(Vector3 v) {
        return this.setVector(v.x(),v.y(),v.z());
    }

    public DetectorTrack setVertex(double vx, double vy, double vz){
        this.trackVertex.setXYZ(vx, vy, vz);
        return this;
    }

    public DetectorTrack setVertex(Vector3 v) {
        return setVertex(v.x(),v.y(),v.z());
    }
    
    public DetectorTrack setPath(double path){
        this.trackPath = path;
        return this;
    }

    public DetectorTrack setNeutralPath(Vector3 vertex, Vector3D end) {
        this.setVertex(vertex);
        this.setTrackEnd(end);
        Vector3 path = new Vector3(end.x(),end.y(),end.z());
        path.sub(vertex);
        return this.setPath(path.mag());
    }
   
    public DetectorTrack setSector(int sector){
        this.trackSector = sector;
        return this;
    }
    
    public DetectorTrack setTrackEnd(double x, double y, double z){
        this.trackEnd.setXYZ(x, y, z);
        return this;
    }

    public DetectorTrack setTrackEnd(Vector3D v) {
        return this.setTrackEnd(v.x(),v.y(),v.z());
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
                x + ux*DetectorTrack.MAX_LINE_LENGTH,
                y + uy*DetectorTrack.MAX_LINE_LENGTH,
                z + uz*DetectorTrack.MAX_LINE_LENGTH
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
