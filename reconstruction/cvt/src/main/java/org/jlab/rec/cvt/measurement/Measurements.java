package org.jlab.rec.cvt.measurement;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.objects.Strip;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;

/**
 *
 * @author devita
 */
public class Measurements {
    
    private static final int NSURFACES = SVTGeometry.NPASSIVE + 
                                         SVTGeometry.NLAYERS  +
                                         BMTGeometry.NLAYERS;
    private boolean cosmic = false;
    private Surface[] cvtSurfaces;
    private boolean debug = false;
    
    public Measurements(boolean cosmic, double xbeam, double ybeam) {
        this.cosmic  = cosmic;
        this.init(xbeam, ybeam);
    }
    
    private void init(double xbeam, double ybeam) {
        if(this.cosmic)
            this.initCosmicSurfaces();
        else
            this.initTargetSurfaces(xbeam, ybeam);
    }
    
    private void initTargetSurfaces(double xbeam, double ybeam) {
        cvtSurfaces = new Surface[NSURFACES+1];
        this.add(MLayer.TARGET.getIndex(),       this.getTarget(xbeam, ybeam));
        this.add(MLayer.SHIELD.getIndex(),       Constants.SVTGEOMETRY.getShieldSurface());
        this.add(MLayer.INNERSVTCAGE.getIndex(), Constants.SVTGEOMETRY.getFaradayCageSurfaces(0));
        this.add(MLayer.OUTERSVTCAGE.getIndex(), Constants.SVTGEOMETRY.getFaradayCageSurfaces(1)); 
    }
    
    private void initCosmicSurfaces() {
        cvtSurfaces = new Surface[NSURFACES*2+1];
        this.add(MLayer.COSMICPLANE.getIndex(1),   this.getCosmicPlane());
        this.add(MLayer.SHIELD.getIndex(1),        Constants.SVTGEOMETRY.getShieldSurface());
        this.add(MLayer.INNERSVTCAGE.getIndex(1),  Constants.SVTGEOMETRY.getFaradayCageSurfaces(0));
        this.add(MLayer.OUTERSVTCAGE.getIndex(1),  Constants.SVTGEOMETRY.getFaradayCageSurfaces(1));       
        this.add(MLayer.SHIELD.getIndex(-1),       Constants.SVTGEOMETRY.getShieldSurface(), -1);
        this.add(MLayer.INNERSVTCAGE.getIndex(-1), Constants.SVTGEOMETRY.getFaradayCageSurfaces(0), -1);
        this.add(MLayer.OUTERSVTCAGE.getIndex(-1), Constants.SVTGEOMETRY.getFaradayCageSurfaces(1), -1);       
       
    }
    
    private void add(int index, Surface surface) {
        if(!(0<=index && index<cvtSurfaces.length))
            throw new IllegalArgumentException("Error: invalid index ="+index);
        if(debug) System.out.println("adding at index " + index + " surface for layer/sector " + surface.getLayer() + "/" + surface.getSector() + " with type " + surface.type.name());
        surface.setIndex(index);
        cvtSurfaces[index] = surface;
    }
 
    private void add(int index, Surface surface, int hemisphere) {
        surface.hemisphere = hemisphere;
        this.add(index, surface);
    }
 
    private int getIndex(MLayer id, int hemisphere) {
        if(!cosmic)
            return id.getIndex(0);
        else
            return id.getIndex(hemisphere);
    }

    private Surface getTarget(double xbeam, double ybeam) {
        Vector3D u = new Vector3D(0,0,1);
        Point3D  p = new Point3D(xbeam, ybeam, 0);
        Line3D   l = new Line3D(p, u);
        Surface target = new Surface(l.origin(), l.end(), Constants.DEFAULTSWIMACC);
        target.addMaterial("LH2", 5, 0.0708E-3, 1, 630.4, 21.8);
        target.setError(Constants.getRbErr());
        if(Constants.kfBeamSpotConstraint())
            target.notUsedInFit = false;
        else
            target.notUsedInFit = true;
        return target;
    }
    
    private Surface getCosmicPlane() {
        Point3D point = new Point3D(0,0,0);
        Point3D   ep1 = new Point3D(-300,0,0);
        Point3D   ep2 = new Point3D( 300,0,0);
        Vector3D  dir = new Vector3D(0,1,0); 
        Plane3D plane = new Plane3D(point, dir);
        Surface cosmic = new Surface(plane, point,ep1, ep2,Constants.DEFAULTSWIMACC);
        cosmic.setError(1);
        cosmic.hemisphere = 1;
        return cosmic;
    }
    
    public List<Surface> getMeasurements(Seed seed) {
        this.reset();
        this.addClusters(seed);
        this.addMissing(seed);
        List<Surface> surfaces = new ArrayList<>();
        for(Surface surf : cvtSurfaces) {
            if(surf!=null) {
                if(debug) System.out.println(surf.toString());
                surfaces.add(surf);
            }
        }
        return surfaces;               
    }
    
    public List<Surface> getActiveMeasurements(Seed seed) {
        List<Surface> surfaces = this.getMeasurements(seed);
        List<Surface> active = new ArrayList<>();
        for(Surface surf : surfaces) {
            if(surf.passive && surf.getIndex()!=0) continue;
            active.add(surf);
            if(debug) System.out.println(surf.toString());
        }
        return active;
    }

    public List<Surface> getMeasurements(StraightTrack cosmic) {
        this.reset();
        this.addClusters(cosmic);
        this.addMissing(cosmic);
        List<Surface> surfaces = new ArrayList<>();
        for(Surface surf : cvtSurfaces) {
            if(surf!=null) {
                if(debug) System.out.println(surf.toString());
                if(surf.passive && surf.getIndex()!=0 && !this.isCrossed(cosmic.getRay(), surf)) {
                    if(debug) System.out.println("Removing surface " + surf.passive + " " + this.isCrossed(cosmic.getRay(), surf));
                    continue;
                }
                surfaces.add(surf);
            }
        }
        return surfaces;            
    }
    
    public List<Surface> getActiveMeasurements(StraightTrack cosmic) {
        List<Surface> surfaces = this.getMeasurements(cosmic);
        List<Surface> active = new ArrayList<>();
        for(Surface surf : surfaces) {
            if(surf.passive && surf.getIndex()!=0) continue;
            active.add(surf);
            if(debug) System.out.println(surf.toString());
        }
        return active;
    }

    private void addClusters(Seed seed) {
        int hemisp = (int) Math.signum(seed.getHelix().getPointAtRadius(300).y());    
        this.addClusters(DetectorType.BST, this.getClusterSurfaces(DetectorType.BST, seed.getClusters()));
        this.addClusters(DetectorType.BMT, this.getClusterSurfaces(DetectorType.BMT, seed.getClusters(), hemisp));
    }
    
    private void addClusters(StraightTrack cosmic) {
        this.addClusters(DetectorType.BST, this.getClusterSurfaces(DetectorType.BST, cosmic.getClusters()));
        this.addClusters(DetectorType.BMT, this.getClusterSurfaces(DetectorType.BMT, cosmic.getClusters()));
    }

    private void addClusters(DetectorType type, List<Surface> clusters) {
        for(Surface cluster : clusters) {
            int hemisphere = 0;
            if(cosmic) hemisphere = (int) cluster.hemisphere;
            int index = MLayer.getType(type, cluster.getLayer()).getIndex(hemisphere);
            this.add(index, cluster);
        }
    }
        
    private List<Surface> getClusterSurfaces(DetectorType type, List<Cluster> clusters, int hemisphere) {
        
        List<Surface> surfaces = this.getClusterSurfaces(type, clusters);        
        for(Surface surf : surfaces) {
            surf.hemisphere = hemisphere;
        }
        return surfaces;
    }
    
    private List<Surface> getClusterSurfaces(DetectorType type, List<Cluster> clusters) {
        List<Surface> surfaces = new ArrayList<>();
        
        for(Cluster cluster : clusters) {
            if(cluster.getDetector()!=type) continue;
            int layer = cluster.getLayer();
            if(type==DetectorType.BMT) layer += SVTGeometry.NLAYERS;
            Surface measure = cluster.measurement();
            measure.hemisphere = Math.signum(cluster.center().y());
            if((int)Constants.getUsedLayers().get(layer)<1)
                measure.notUsedInFit=true;
            surfaces.add(measure);
        }
        return surfaces;
    }
    
    private void addMissing(Seed seed) {
        for(int i=0; i<cvtSurfaces.length; i++) {
            if(cvtSurfaces[i]==null) {
                int id = MLayer.getId(i, 0);
                int layer = MLayer.getType(id).getLayer();
                if(layer>0) {
                    DetectorType type = MLayer.getDetectorType(id);
                    Surface surface = this.getDetectorSurface(seed, type, layer, 0);
                    if(surface == null) continue;
                    surface.notUsedInFit=true;
                    surface.passive=true;
                    if(debug) System.out.println("Generating surface for missing index " + i + " detector " + type.getName() + " layer " + layer + " sector " + surface.getSector());
                    this.add(i, surface);
                }
            }
        }
    }
                
    private void addMissing(StraightTrack ray) {
        for(int i=0; i<cvtSurfaces.length; i++) {
            if(cvtSurfaces[i]==null) {
                int hemisphere = MLayer.getHemisphere(i);
                int id = MLayer.getId(i, hemisphere);
                int layer = MLayer.getType(id).getLayer();
                if(layer>0) {
                    DetectorType type = MLayer.getDetectorType(id);
                    Surface surface = this.getDetectorSurface(ray, type, layer, hemisphere);
                    if(surface == null) continue;
                    surface.hemisphere=hemisphere;
                    surface.notUsedInFit=true;
                    surface.passive=true;
                    if(debug) System.out.println("Generating surface for missing index " + i + " detector " + type.getName() + " layer " + layer + " sector " + surface.getSector());
                    this.add(i, surface);
                }
            }
        }
    }
                
    private Surface getDetectorSurface(Seed seed, DetectorType type, int layer, int hemisphere) {
        int sector = this.getSector(seed, type, layer, hemisphere);
        if(sector>0)
            return this.getDetectorSurface(type, layer, sector);
        return null;
    }

    
    private Surface getDetectorSurface(StraightTrack ray, DetectorType type, int layer, int hemisphere) {
        int sector = this.getSector(ray, type, layer, hemisphere);
        if(sector>0)
            return this.getDetectorSurface(type, layer, sector);
        return null;
    }

    private Surface getDetectorSurface(DetectorType type, int layer, int sector) {
        Surface surface = null;
        if(type==DetectorType.BST)
            surface = Constants.SVTGEOMETRY.getSurface(layer, sector, new Strip(0, 0, 0));
        else if(type==DetectorType.BMT)
            surface = Constants.BMTGEOMETRY.getSurface(layer, sector, new Strip(0, 0, 0));
        return surface;
    }

    
    private int getSector(Seed seed, DetectorType type, int layer, int hemisphere) {
        Helix helix = seed.getHelix();
        if(type==DetectorType.BST) { 
            int twinLayer = Constants.SVTGEOMETRY.getTwinLayer(layer);
            int twinIndex = MLayer.getType(DetectorType.BST, twinLayer).getIndex(hemisphere);
            if(cvtSurfaces[twinIndex]!=null) 
                return cvtSurfaces[twinIndex].getSector();
            Point3D traj = helix.getPointAtRadius(Constants.SVTGEOMETRY.getLayerRadius(layer));
            if(traj!=null && !Double.isNaN(traj.z())) 
                return Constants.SVTGEOMETRY.getSector(layer, traj);
        }
        else if(type==DetectorType.BMT) {
            Point3D traj = seed.getHelix().getPointAtRadius(Constants.BMTGEOMETRY.getRadius(layer));
            if(traj!=null && !Double.isNaN(traj.z())) 
                return Constants.BMTGEOMETRY.getSector(0, traj);
        }
        return 0;
    }
    
    private int getSector(StraightTrack cosmic, DetectorType type, int layer, int hemisphere) {

        if(type==DetectorType.BST) {   
            int twinLayer = Constants.SVTGEOMETRY.getTwinLayer(layer);
            int twinIndex = MLayer.getType(DetectorType.BST, twinLayer).getIndex(hemisphere);
            if(cvtSurfaces[twinIndex]!=null)
                return cvtSurfaces[twinIndex].getSector();
           
            double[][][] trajs = TrajectoryFinder.calcTrackIntersSVT(cosmic.getRay());
            for(int i=0; i<SVTGeometry.NSECTORS[layer-1]; i++) {
                if(trajs[layer-1][i][0]!=-999 && hemisphere==(int) Math.signum(trajs[layer-1][i][1])) {                       
                    return i+1;
                }
            }
        }
        else if(type==DetectorType.BMT) {
            double[][][] trajs = TrajectoryFinder.calcTrackIntersBMT(cosmic.getRay(), 1);
            double x = trajs[layer-1][(hemisphere+1)/2][0];
            double y = trajs[layer-1][(hemisphere+1)/2][1];
            double z = trajs[layer-1][(hemisphere+1)/2][2];
            return Constants.BMTGEOMETRY.getSector(layer, Math.atan2(y, x));
        }
        return 0;
        
    }
    
    private boolean isCrossed(Ray ray, Surface surface){
        if(surface.cylinder==null)
            return true;
        List<Point3D> trajs = new ArrayList<>();
        Line3D line = ray.toLine();
        return surface.cylinder.intersection(line, trajs) > 1;
    }
    
    private void reset() {
        for(int i=0; i<cvtSurfaces.length; i++) {
            int hemisphere = MLayer.getHemisphere(i);
            int id = MLayer.getId(i, hemisphere);
            if(!cosmic) id= MLayer.getId(i, 0);
            DetectorType type = MLayer.getDetectorType(id);
            if(type==DetectorType.BST || type==DetectorType.BMT) {
                cvtSurfaces[i] = null;
                if(debug) System.out.println("Resetting surface with index " + i + " in hemisphere " + hemisphere + " with id " + id + " and DetectorType " + type.getName());
            }
        }
    }
}
