package org.jlab.rec.cvt.track;

import java.util.ArrayList;
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
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.StateVec;
import org.jlab.rec.cvt.trajectory.Trajectory;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

public class StraightTrack extends Trajectory{

    private double _ndf;
    private double _chi2;
    public Map<Integer, Cluster> clsMap;
    public Map<Integer, HitOnTrack> trajs = null;

    public StraightTrack(Ray ray) {
        super(ray);

    }

    public void update(KFitter kf) {
        Ray the_ray = new Ray(kf.finalStateVec.tx, kf.finalStateVec.x0, kf.finalStateVec.tz, kf.finalStateVec.z0);                
        this.set_ray(the_ray);
        this.set_chi2(kf.chi2);
        this.trajs = kf.TrjPoints;
        this.updateCrosses();
        this.updateClusters();
        this.findTrajectory();
    }
    
    
    public void updateCrosses(Ray ray) {
        this.set_ray(ray);
        this.updateCrosses();
    }
    
    public void updateCrosses() {
        for (Cross c : this) {
            c.set_AssociatedTrackID(this.get_Id());
            if(c.get_Detector()==DetectorType.BST) 
                c.updateSVTCross(this.get_ray().get_dirVec());
            else {
                Cluster cluster = c.get_Cluster1();
                List<Point3D> trajs = new ArrayList<>();
                int nTraj = cluster.getTile().intersection(this.get_ray().toLine(), trajs);
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
                    c.updateBMTCross(traj, this.get_ray().get_dirVec());
                }
            }
        }
    }
    
    public void updateClusters() {
        for(int key : this.trajs.keySet()) {
            this.clsMap.get(key).update(this.get_Id(), this.trajs.get(key));
        }        
    }

    public double get_ndf() {
        return _ndf;
    }

    public void set_ndf(double _ndf) {
        this._ndf = _ndf;
    }

    public double get_chi2() {
        return _chi2;
    }

    public void set_chi2(double _chi2) {
        this._chi2 = _chi2;
    }

    public boolean containsCross(Cross cross) {
        StraightTrack cand = this;
        boolean isInTrack = false;

        for (int i = 0; i < cand.size(); i++) {
            if (cand.get(i).get_Id() == cross.get_Id()) {
                isInTrack = true;
            }

        }

        return isInTrack;
    }

    
    public void findTrajectory() {
        Ray ray = this.get_ray();
        ArrayList<StateVec> stateVecs = new ArrayList<>();

        double[][][] SVTIntersections = TrajectoryFinder.calc_trackIntersSVT(ray);

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

                    StateVec stVec = new StateVec(XtrackIntersPlane, YtrackIntersPlane, ZtrackIntersPlane, ray.get_dirVec().x(), ray.get_dirVec().y(), ray.get_dirVec().z());
                    stVec.set_ID(this.get_Id());
                    stVec.set_SurfaceLayer(LayerTrackIntersPlane);
                    stVec.set_SurfaceSector(SectorTrackIntersPlane);
                    stVec.set_TrkPhiAtSurface(PhiTrackIntersPlane);
                    stVec.set_TrkThetaAtSurface(ThetaTrackIntersPlane);
                    stVec.set_TrkToModuleAngle(trkToMPlnAngl);
                    stVec.set_CalcCentroidStrip(CalcCentroidStrip);
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
        
        double[][][] BMTIntersections = TrajectoryFinder.calc_trackIntersBMT(ray, BMTConstants.STARTINGLAYR);

        for (int l = BMTConstants.STARTINGLAYR - 1; l < 6; l++) {
            //hemisphere 1-2
            for (int h = 0; h < 2; h++) {

                if (BMTIntersections[l][h][0] != -999) {

                    int LayerTrackIntersSurf = (l + 1);
                    double XtrackIntersSurf = BMTIntersections[l][h][0];
                    double YtrackIntersSurf = BMTIntersections[l][h][1];
                    double ZtrackIntersSurf = BMTIntersections[l][h][2];
                    //int SectorTrackIntersSurf = bmt_geo.isInSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf), Math.toRadians(BMTConstants.isInSectorJitter));
                    int SectorTrackIntersSurf = Constants.BMTGEOMETRY.getSector(LayerTrackIntersSurf, Math.atan2(YtrackIntersSurf, XtrackIntersSurf));
                    double PhiTrackIntersSurf = BMTIntersections[l][h][3];
                    double ThetaTrackIntersSurf = BMTIntersections[l][h][4];
                    double trkToMPlnAngl = BMTIntersections[l][h][5];
                    double CalcCentroidStrip = BMTIntersections[l][h][6];

                    StateVec stVec = new StateVec(XtrackIntersSurf, YtrackIntersSurf, ZtrackIntersSurf, ray.get_dirVec().x(), ray.get_dirVec().y(), ray.get_dirVec().z());

                    stVec.set_ID(this.get_Id());
                    stVec.set_SurfaceLayer(LayerTrackIntersSurf);
                    stVec.set_SurfaceSector(SectorTrackIntersSurf);
                    stVec.set_TrkPhiAtSurface(PhiTrackIntersSurf);
                    stVec.set_TrkThetaAtSurface(ThetaTrackIntersSurf);
                    stVec.set_TrkToModuleAngle(trkToMPlnAngl);
                    stVec.set_CalcCentroidStrip(CalcCentroidStrip); 
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
            stateVecs.get(l).set_planeIdx(l);
        }
        this.set_Trajectory(stateVecs);
        this.set_SVTIntersections(SVTIntersections);
        this.set_BMTIntersections(BMTIntersections);
    }

    
    /**
     *
     * @return the chi^2 for the straight track fit
     */
    public double calc_straightTrkChi2() {

        double chi2 = 0;

        double yxSl = this.get_ray().get_yxslope();
        double yzSl = this.get_ray().get_yzslope();
        double yxIt = this.get_ray().get_yxinterc();
        double yzIt = this.get_ray().get_yzinterc(); 
        
        for (Cross c : this) {
            double errSq = c.get_PointErr().x() * c.get_PointErr().x() + c.get_PointErr().z() * c.get_PointErr().z();
            double y = c.get_Point().y();
            double x = c.get_Point().x();
            double z = c.get_Point().z();

            double x_fit = yxSl * y + yxIt;
            double z_fit = yzSl * y + yzIt;

            double delta = (x - x_fit) * (x - x_fit) + (z - z_fit) * (z - z_fit);

            chi2 += delta / errSq;
        }
        return chi2;
    }

}
