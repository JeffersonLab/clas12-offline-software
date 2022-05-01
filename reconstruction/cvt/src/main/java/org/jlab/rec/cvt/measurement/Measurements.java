package org.jlab.rec.cvt.measurement;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.objects.Strip;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
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
                                         BMTGeometry.NPASSIVE +
                                         BMTGeometry.NLAYERS;
    private boolean cosmic   = false;
    private Surface[] cvtSurfaces;
    private boolean debug = false;
    
    public Measurements(double xbeam, double ybeam, boolean beamSpot) {
        this.initTargetSurfaces(xbeam, ybeam, beamSpot);
    }
    
    public Measurements(boolean cosmic) {
        this.cosmic  = cosmic;
        this.initCosmicSurfaces();
    }
    
    private void initTargetSurfaces(double xbeam, double ybeam, boolean beamSpot) {
        cvtSurfaces = new Surface[NSURFACES+2];
        this.add(MLayer.TARGET.getIndex(),       this.getTarget(xbeam, ybeam, beamSpot));
        this.add(MLayer.SCHAMBER.getIndex(),     this.getScatteringChamber());
        this.add(MLayer.SHIELD.getIndex(),       Constants.getInstance().SVTGEOMETRY.getShieldSurface());
        this.add(MLayer.INNERSVTCAGE.getIndex(), Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(0));
        this.add(MLayer.OUTERSVTCAGE.getIndex(), Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(1)); 
        this.add(MLayer.BMTINNERTUBE.getIndex(), Constants.getInstance().BMTGEOMETRY.getInnerTube()); 
        this.add(MLayer.BMTOUTERTUBE.getIndex(), Constants.getInstance().BMTGEOMETRY.getOuterTube()); 
    }
    
    private void initCosmicSurfaces() {
        cvtSurfaces = new Surface[NSURFACES*2+3];
        this.add(MLayer.COSMICPLANE.getIndex(1),   this.getCosmicPlane());
        this.add(MLayer.SCHAMBER.getIndex(1),      this.getScatteringChamber());
        this.add(MLayer.SHIELD.getIndex(1),        Constants.getInstance().SVTGEOMETRY.getShieldSurface());
        this.add(MLayer.INNERSVTCAGE.getIndex(1),  Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(0));
        this.add(MLayer.OUTERSVTCAGE.getIndex(1),  Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(1));       
        this.add(MLayer.BMTINNERTUBE.getIndex(1),  Constants.getInstance().BMTGEOMETRY.getInnerTube()); 
        this.add(MLayer.BMTOUTERTUBE.getIndex(1),  Constants.getInstance().BMTGEOMETRY.getOuterTube()); 
        this.add(MLayer.SCHAMBER.getIndex(-1),     this.getScatteringChamber());
        this.add(MLayer.SHIELD.getIndex(-1),       Constants.getInstance().SVTGEOMETRY.getShieldSurface(), -1);
        this.add(MLayer.INNERSVTCAGE.getIndex(-1), Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(0), -1);
        this.add(MLayer.OUTERSVTCAGE.getIndex(-1), Constants.getInstance().SVTGEOMETRY.getFaradayCageSurfaces(1), -1);       
        this.add(MLayer.BMTINNERTUBE.getIndex(-1), Constants.getInstance().BMTGEOMETRY.getInnerTube());        
        this.add(MLayer.BMTOUTERTUBE.getIndex(-1), Constants.getInstance().BMTGEOMETRY.getOuterTube()); 
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

    private Surface getTarget(double xbeam, double ybeam, boolean beamSpot) {
        Vector3D u = new Vector3D(0,0,1);
        Point3D  p = new Point3D(xbeam, ybeam, 0);
        Line3D   l = new Line3D(p, u);
        Surface target = new Surface(l.origin(), l.end(), Constants.getInstance().DEFAULTSWIMACC);
        target.addMaterial(Constants.getInstance().getTargetMaterial());
        target.addMaterial(Constants.TARGETKAPTON);
        target.setError(Constants.getInstance().getBeamRadius());
        if(beamSpot)
            target.passive = false;
        else
            target.passive = true;
        return target;
    }
    
    private Surface getScatteringChamber() {
        Point3D  center = new Point3D(0, 0, Constants.getInstance().getZoffset()-100);
        Point3D  origin = new Point3D(39.5, 0, Constants.getInstance().getZoffset()-100);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D chamber = new Cylindrical3D(base, 200);
        Surface scatteringChamber = new Surface(chamber, new Strip(0, 0, 0), Constants.DEFAULTSWIMACC);
        scatteringChamber.addMaterial(Constants.TARGETRHOACELL);
        scatteringChamber.passive=true;
        return scatteringChamber;
    }
    
    private static Surface getCTOF() {
        
        double radius    = Constants.getInstance().CTOFGEOMETRY.getRadius(1);
        double thickness = Constants.getInstance().CTOFGEOMETRY.getThickness(1);
        Line3d lineZ     = Constants.getInstance().CTOFGEOMETRY.getPaddle(1).getLineZ();
        
        Point3D  center = new Point3D(        0, 0, lineZ.origin().z);
        Point3D  origin = new Point3D(radius*10, 0, lineZ.origin().z);
        Vector3D axis   = new Vector3D(0,0,1);
        Arc3D base = new Arc3D(origin, center, axis, 2*Math.PI);
        Cylindrical3D barrel = new Cylindrical3D(base, lineZ.end().z -lineZ.origin().z);
        
        Surface ctof = new Surface(barrel, new Strip(0, 0, 0), Constants.DEFAULTSWIMACC);
        ctof.addMaterial(Constants.SCINTILLATOR.clone(thickness*10));
        ctof.setIndex(DetectorType.CTOF.getDetectorId());
        ctof.setLayer(1);
        ctof.setSector(1);
        ctof.passive=true;
        return ctof;
    }
    
    private static List<Surface> getCND() {
        List<Surface> surfaces = new ArrayList<>();
        
        Vector3D axis   = new Vector3D(0,0,1);
        
        for(int ilayer=0; ilayer<Constants.getInstance().CNDGEOMETRY.getSector(0).getSuperlayer(0).getNumLayers(); ilayer++) {
            
            Point3D paddle = Constants.getInstance().CNDGEOMETRY.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getMidpoint();
            
            double radius    = Math.sqrt(paddle.x()*paddle.x()+paddle.y()*paddle.y());
            double thickness = Constants.getInstance().CNDGEOMETRY.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getVolumeEdge(1).length();
            double length    = Constants.getInstance().CNDGEOMETRY.getSector(0).getSuperlayer(0).getLayer(ilayer).getComponent(0).getLength();
            Point3D       center = new Point3D(        0, 0, paddle.z() - length/2);
            Point3D       origin = new Point3D(radius*10, 0, paddle.z() - length/2);
            Arc3D           base = new Arc3D(origin, center, axis, 2*Math.PI);
            Cylindrical3D barrel = new Cylindrical3D(base, length);
            
            Surface cnd = new Surface(barrel, new Strip(0, 0, 0), Constants.DEFAULTSWIMACC);
            cnd.addMaterial(Constants.SCINTILLATOR.clone(thickness*10));
            cnd.setIndex(DetectorType.CND.getDetectorId());
            cnd.setLayer(ilayer+1);
            cnd.setSector(1);
            cnd.passive=true;
            surfaces.add(cnd);
        }
        return surfaces;
    }
    
    public static List<Surface> getOuters() {
        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(getCTOF());
        surfaces.addAll(getCND());
        return surfaces;
    }
    
    private Surface getCosmicPlane() {
        Point3D point = new Point3D(0,0,0);
        Point3D   ep1 = new Point3D(-300,0,0);
        Point3D   ep2 = new Point3D( 300,0,0);
        Vector3D  dir = new Vector3D(0,1,0); 
        Plane3D plane = new Plane3D(point, dir);
        Surface cosmic = new Surface(plane, point,ep1, ep2,Constants.DEFAULTSWIMACC);
        cosmic.addMaterial(Constants.VACUUM);
        cosmic.setError(1);
        cosmic.hemisphere = 1;
        cosmic.passive = true;
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

    
    public double getELoss(double p, double mass) {
        double pcorr = p;
        for(int i=0; i<cvtSurfaces.length; i++) {
            Surface surf = cvtSurfaces[i];
            if(surf==null) break;
            double E  = Math.sqrt(pcorr*pcorr + mass*mass);
            double dE = surf.getEloss(pcorr, mass);
            double Ecorr = E + dE;
            pcorr = Math.sqrt(Ecorr*Ecorr-mass*mass); 
            if(debug) System.out.println(p + " " + pcorr + "\n" + surf.toString());
        }
        return pcorr;
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
        this.addClusters(DetectorType.BST, this.getClusterSurfaces(DetectorType.BST, seed.getClusters()));
        this.addClusters(DetectorType.BMT, this.getClusterSurfaces(DetectorType.BMT, seed.getClusters()));
    }
    
    private void addClusters(StraightTrack cosmic) {
        this.addClusters(DetectorType.BST, this.getClusterSurfaces(DetectorType.BST, cosmic.getClusters()));
        this.addClusters(DetectorType.BMT, this.getClusterSurfaces(DetectorType.BMT, cosmic.getClusters(), cosmic.getRay()));
    }

    private void addClusters(DetectorType type, List<Surface> clusters) {
        for(Surface cluster : clusters) {
            int hemisphere = 0;
            if(cosmic) hemisphere = (int) cluster.hemisphere;
            int index = MLayer.getType(type, cluster.getLayer()).getIndex(hemisphere);
            this.add(index, cluster);
        }
    }
        
//    private List<Surface> getClusterSurfaces(DetectorType type, List<Cluster> clusters, int hemisphere) {
//        
//        List<Surface> surfaces = this.getClusterSurfaces(type, clusters);        
//        for(Surface surf : surfaces) {
//            surf.hemisphere = hemisphere;
//        }
//        return surfaces;
//    }
        
    private List<Surface> getClusterSurfaces(DetectorType type, List<Cluster> clusters, Ray ray) {
        
        List<Surface> surfaces = this.getClusterSurfaces(type, clusters);        
        for(Surface surf : surfaces) {
            surf.hemisphere = this.getHemisphere(ray, surf);
        }
        return surfaces;
    }
    
    private List<Surface> getClusterSurfaces(DetectorType type, List<Cluster> clusters) {
        List<Surface> surfaces = new ArrayList<>();
        
        for(Cluster cluster : clusters) {
            if(cluster.getDetector()!=type) continue;
            int layer = MLayer.getType(type, cluster.getLayer()).getCVTLayer();
            Surface measure = cluster.measurement();
            measure.hemisphere = Math.signum(cluster.center().y());
            if((int)Constants.getInstance().getUsedLayers().get(layer)<1)
                measure.passive=true;
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
            surface = Constants.getInstance().SVTGEOMETRY.getSurface(layer, sector, new Strip(0, 0, 0));
        else if(type==DetectorType.BMT)
            surface = Constants.getInstance().BMTGEOMETRY.getSurface(layer, sector, new Strip(0, 0, 0));
        return surface;
    }

    
    private int getSector(Seed seed, DetectorType type, int layer, int hemisphere) {
        Helix helix = seed.getHelix();
        if(type==DetectorType.BST) { 
            int twinLayer = Constants.getInstance().SVTGEOMETRY.getTwinLayer(layer);
            int twinIndex = MLayer.getType(DetectorType.BST, twinLayer).getIndex(hemisphere);
            if(cvtSurfaces[twinIndex]!=null) 
                return cvtSurfaces[twinIndex].getSector();
            Point3D traj = helix.getPointAtRadius(Constants.getInstance().SVTGEOMETRY.getLayerRadius(layer));
            if(traj!=null && !Double.isNaN(traj.z())) 
                return Constants.getInstance().SVTGEOMETRY.getSector(layer, traj);
        }
        else if(type==DetectorType.BMT) {
            Point3D traj = seed.getHelix().getPointAtRadius(Constants.getInstance().BMTGEOMETRY.getRadius(layer));
            if(traj!=null && !Double.isNaN(traj.z())) 
                return Constants.getInstance().BMTGEOMETRY.getSector(0, traj);
            else if(layer>1) {
                int twinIndex = MLayer.getType(DetectorType.BMT, layer-1).getIndex(hemisphere);
                if(cvtSurfaces[twinIndex]!=null)
                    return cvtSurfaces[twinIndex].getSector();
            }
        }
        return 0;
    }
    
    private int getSector(StraightTrack cosmic, DetectorType type, int layer, int hemisphere) {

        if(type==DetectorType.BST) {   
            int twinLayer = Constants.getInstance().SVTGEOMETRY.getTwinLayer(layer);
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
            return Constants.getInstance().BMTGEOMETRY.getSector(layer, Math.atan2(y, x));
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
    
    private int getHemisphere(Ray ray, Surface surface){
        if(surface.cylinder==null)
            return 0;
        List<Point3D> trajs = new ArrayList<>();
        Line3D line = ray.toLine();
        if(surface.cylinder.intersection(line, trajs)>= 1) {
            return (int) Math.signum(trajs.get(0).y());
        }
        else 
            return 0;
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
