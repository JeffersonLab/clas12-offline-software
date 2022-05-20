package org.jlab.rec.cvt.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.jlab.clas.tracking.kalmanfilter.AKFitter.HitOnTrack;
import org.jlab.clas.tracking.kalmanfilter.straight.KFitter;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.measurement.MLayer;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

public class StraightTrack extends Trajectory{

    private int _ndf;
    private double _chi2;
    private double[][] covmat;
    private int status;
    public Map<Integer, HitOnTrack> trajs = null;

    public StraightTrack(Ray ray) {
        super(ray);

    }
    
    public StraightTrack(StraightTrack seed, KFitter kf) {
        super(new Ray(kf.finalStateVec.tx, kf.finalStateVec.x0, kf.finalStateVec.tz, kf.finalStateVec.z0));
        this.addAll(seed);
        this.setId(seed.getId());
        this.update(kf);
    }
    
    public List<Cluster> getClusters() {
        List<Cluster> clusters = new ArrayList<>(); 
        for(Cross c : this) { 
	    if(c.getDetector()==DetectorType.BST) {
                if(c.getCluster1()!=null) clusters.add(c.getCluster1());
                if(c.getCluster2()!=null) clusters.add(c.getCluster2());
            } else {
                clusters.add(c.getCluster1());
            }
      	}
        Collections.sort(clusters);
        return clusters;
    }
    
    
    public final void update(KFitter kf) {
        Ray the_ray = new Ray(kf.finalStateVec.tx, kf.finalStateVec.x0, kf.finalStateVec.tz, kf.finalStateVec.z0);                
        this.setRay(the_ray);
        this.setChi2(kf.chi2);
        this.setNDF(kf.NDF);
        this.setStatus(kf.numIter*1000);
        this.setCovMat(kf.finalStateVec.covMat);
        this.trajs = kf.trajPoints;
        this.updateCrosses();
        this.updateClusters();
        this.findTrajectory();
    }
    
    
    public void updateCrosses(Ray ray) {
        this.setRay(ray);
        this.updateCrosses();
    }
    
    public void updateCrosses() {
        for (Cross c : this) {
            c.setAssociatedTrackID(this.getId());
            if(c.getDetector()==DetectorType.BST) 
                c.updateSVTCross(this.getRay().getDirVec());
            else {
                Cluster cluster = c.getCluster1();
                List<Point3D> trajs = new ArrayList<>();
                int nTraj = cluster.getTile().intersection(this.getRay().toLine(), trajs);
                if(nTraj>0) {
                    Point3D traj = null;
                    double  doca = Double.MAX_VALUE;
                    for(Point3D t : trajs) {
                        double d = Math.abs(cluster.residual(t));
                        if(d<doca) {
                            doca = d;
                            traj = t;
                        }
                    }
                    c.updateBMTCross(traj, this.getRay().getDirVec());
                }
            }
        }
    }
    
    public void updateClusters() {
        for(Cluster cluster: this.getClusters()) {
            int hemisphere = (int) Math.signum(cluster.center().y());
            int layer = cluster.getLayer();
            DetectorType type = cluster.getDetector();
            int index = MLayer.getType(type, layer).getIndex(hemisphere);
            if(this.trajs.containsKey(index)) 
                cluster.update(this.getId(), this.trajs.get(index));
        }
    }

    public int getNDF() {
        return _ndf;
    }

    public void setNDF(int _ndf) {
        this._ndf = _ndf;
    }

    public double getChi2() {
        return _chi2;
    }

    public void setChi2(double _chi2) {
        this._chi2 = _chi2;
    }

    public double[][] getCovMat() {
        return covmat;
    }

    public void setCovMat(double[][] covmat) {
        this.covmat = covmat;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<Integer, HitOnTrack> getTrajectories() {
        return trajs;
    }
    
    public boolean containsCross(Cross cross) {
        StraightTrack cand = this;
        boolean isInTrack = false;

        for (int i = 0; i < cand.size(); i++) {
            if (cand.get(i).getId() == cross.getId()) {
                isInTrack = true;
            }

        }

        return isInTrack;
    }

    
    public void findTrajectory() {
        Ray ray = this.getRay();
        ArrayList<StateVec> stateVecs = new ArrayList<>();

        double[][][] SVTIntersections = TrajectoryFinder.calcTrackIntersSVT(ray);

        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
            for (int s = 0; s < SVTGeometry.NSECTORS[l]; s++) {

                if (SVTIntersections[l][s][0] != -999) {

                    int LayerTrackIntersPlane = (l + 1);
                    int SectorTrackIntersPlane = (s + 1);
                    double XtrackIntersPlane = SVTIntersections[l][s][0];
                    double YtrackIntersPlane = SVTIntersections[l][s][1];
                    double ZtrackIntersPlane = SVTIntersections[l][s][2];
                    double PhiTrackIntersPlane = SVTIntersections[l][s][3];
                    double ThetaTrackIntersPlane = SVTIntersections[l][s][4];
                    double trkToMPlnAngl = SVTIntersections[l][s][5];
                    double CalcCentroidStrip = SVTIntersections[l][s][6];

                    StateVec stVec = new StateVec(XtrackIntersPlane, YtrackIntersPlane, ZtrackIntersPlane, ray.getDirVec().x(), ray.getDirVec().y(), ray.getDirVec().z());
                    stVec.setID(this.getId());
                    stVec.setSurfaceLayer(LayerTrackIntersPlane);
                    stVec.setSurfaceSector(SectorTrackIntersPlane);
                    stVec.setTrkPhiAtSurface(PhiTrackIntersPlane);
                    stVec.setTrkThetaAtSurface(ThetaTrackIntersPlane);
                    stVec.setTrkToModuleAngle(trkToMPlnAngl);
                    stVec.setCalcCentroidStrip(CalcCentroidStrip);
                    if(stateVecs.size()>0 
                            && stateVecs.get(stateVecs.size()-1).x()==stVec.x()
                            && stateVecs.get(stateVecs.size()-1).y()==stVec.y()
                            && stateVecs.get(stateVecs.size()-1).z()==stVec.z()) {
                    } else {
                        stateVecs.add(stVec);
                    }
                }
            }
        }
        
        double[][][] BMTIntersections = TrajectoryFinder.calcTrackIntersBMT(ray, BMTConstants.STARTINGLAYR);

        for (int l = BMTConstants.STARTINGLAYR - 1; l < 6; l++) {
            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {

                if (BMTIntersections[l][h][0] != -999) {

                    int LayerTrackIntersSurf = (l + 1);
                    double XtrackIntersSurf = BMTIntersections[l][h][0];
                    double YtrackIntersSurf = BMTIntersections[l][h][1];
                    double ZtrackIntersSurf = BMTIntersections[l][h][2];
                    //int SectorTrackIntersSurf = bmt_geo.isInSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf), Math.toRadians(BMTConstants.ISINSECTORJITTER));
                    int SectorTrackIntersSurf = Constants.getInstance().BMTGEOMETRY.getSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf));
                    double PhiTrackIntersSurf = BMTIntersections[l][h][3];
                    double ThetaTrackIntersSurf = BMTIntersections[l][h][4];
                    double trkToMPlnAngl = BMTIntersections[l][h][5];
                    double CalcCentroidStrip = BMTIntersections[l][h][6];

                    StateVec stVec = new StateVec(XtrackIntersSurf, YtrackIntersSurf, ZtrackIntersSurf, ray.getDirVec().x(), ray.getDirVec().y(), ray.getDirVec().z());

                    stVec.setID(this.getId());
                    stVec.setSurfaceLayer(LayerTrackIntersSurf);
                    stVec.setSurfaceSector(SectorTrackIntersSurf);
                    stVec.setTrkPhiAtSurface(PhiTrackIntersSurf);
                    stVec.setTrkThetaAtSurface(ThetaTrackIntersSurf);
                    stVec.setTrkToModuleAngle(trkToMPlnAngl);
                    stVec.setCalcCentroidStrip(CalcCentroidStrip); 
                     if(stateVecs.size()>0 
                            && stateVecs.get(stateVecs.size()-1).x()==stVec.x()
                            && stateVecs.get(stateVecs.size()-1).y()==stVec.y()
                            && stateVecs.get(stateVecs.size()-1).z()==stVec.z()) {
                    } else {
                        stateVecs.add(stVec);
                    }
                }
            }
        }

        //Collections.sort(stateVecs);
        
        stateVecs.sort(Comparator.comparing(StateVec::y));
        for (int l = 0; l < stateVecs.size(); l++) {
            stateVecs.get(l).setPlaneIdx(l);
        }
        this.setTrajectory(stateVecs);
        this.setSVTIntersections(SVTIntersections);
        this.setBMTIntersections(BMTIntersections);
    }

    
    /**
     *
     * @return the chi^2 for the straight track fit
     */
    public double calcStraightTrkChi2() {

        double chi2 = 0;

        double yxSl = this.getRay().getYXSlope();
        double yzSl = this.getRay().getYZSlope();
        double yxIt = this.getRay().getYXInterc();
        double yzIt = this.getRay().getYZInterc(); 
        
        for (Cross c : this) {
            double errSq = c.getPointErr().x() * c.getPointErr().x() + c.getPointErr().z() * c.getPointErr().z();
            double y = c.getPoint().y();
            double x = c.getPoint().x();
            double z = c.getPoint().z();

            double x_fit = yxSl * y + yxIt;
            double z_fit = yzSl * y + yzIt;

            double delta = (x - x_fit) * (x - x_fit) + (z - z_fit) * (z - z_fit);

            chi2 += delta / errSq;
        }
        return chi2;
    }

}
