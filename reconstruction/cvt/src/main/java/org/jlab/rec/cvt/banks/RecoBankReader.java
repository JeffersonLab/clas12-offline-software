package org.jlab.rec.cvt.banks;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.hit.Strip;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;

/**
 *
 * @author devita
 */
public class RecoBankReader {
    
       
    public static List<Hit> readBSTHitBank(DataEvent event) {
        
        if(!event.hasBank("BST::Hits"))
            return null;
        else {
            List<Hit> hits = new ArrayList<>();        
            
            DataBank bank = event.getBank("BST::Hits");
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int sector = bank.getByte("sector", i);
                int layer  = bank.getByte("layer", i);
                int strip  = bank.getShort("strip", i);
                double energy      = bank.getFloat("energy", i);
                double time        = bank.getFloat("time", i);
                double fitResidual = bank.getFloat("fitResidual", i)*10;
                int clusterId  = bank.getShort("clusterID", i);
                int trackId    = bank.getShort("trkID", i);
                int trkStatus  = bank.getByte("trkingStat", i);
                int status     = bank.getByte("status", i);
                Hit hit = new Hit(DetectorType.BST, BMTType.UNDEFINED, sector, layer, new Strip(strip, energy, time));
                hit.getStrip().setLine(Constants.getInstance().SVTGEOMETRY.getStrip(layer, sector, strip));
                hit.getStrip().setModule(Constants.getInstance().SVTGEOMETRY.getModule(layer, sector));
                hit.getStrip().setNormal(Constants.getInstance().SVTGEOMETRY.getNormal(layer, sector));
                hit.getStrip().setPitch(SVTGeometry.getPitch());
                hit.getStrip().setStatus(status);
                hit.setId(id);
                hit.setAssociatedClusterID(clusterId);
                hit.setAssociatedTrackID(trackId);
                hit.setTrkgStatus(trkStatus);
                hit.setdocaToTrk(fitResidual);
                hits.add(hit);
            }
            return hits;
        }
    }
            
    public static List<Hit>  readBMTHitBank(DataEvent event) {
        
        if(!event.hasBank("BMT::Hits"))
            return null;
        else {
            List<Hit> hits = new ArrayList<>();        
            
            DataBank bank = event.getBank("BMT::Hits");
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int sector = bank.getByte("sector", i);
                int layer  = bank.getByte("layer", i);
                int strip  = bank.getShort("strip", i);
                double energy      = bank.getFloat("energy", i);
                double time        = bank.getFloat("time", i);
                double fitResidual = bank.getFloat("fitResidual", i)*10;
                int clusterId  = bank.getShort("clusterID", i);
                int trackId    = bank.getShort("trkID", i);
                int trkStatus  = bank.getByte("trkingStat", i);
                int status     = bank.getByte("status", i);
                Hit hit = new Hit(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, layer, new Strip(strip, energy, time));
                hit.getStrip().setStatus(status);
                hit.setId(id);
                hit.setAssociatedClusterID(clusterId);
                hit.setAssociatedTrackID(trackId);
                hit.setTrkgStatus(trkStatus);
                hit.setdocaToTrk(fitResidual);
                hits.add(hit);
            }
            return hits;
        }
    }
                        
    public static List<Cluster> readBSTClusterBank(DataEvent event) {
        
        if(!event.hasBank("BST::Clusters"))
            return null;
        else {
            List<Cluster> clusters = new ArrayList<>();        
            
            DataBank bank = event.getBank("BST::Clusters");
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int tid    = bank.getShort("trkID", i);
                int sector = bank.getByte("sector", i);
                int layer  = bank.getByte("layer", i);
                double etot          = bank.getFloat("ETot", i);
                double time          = bank.getFloat("time", i);
                double centroid      = bank.getFloat("centroid", i);
                double resolution    = bank.getFloat("e", i)*10;
                double x1 = bank.getFloat("x1",   i)*10;
                double y1 = bank.getFloat("y1",   i)*10;
                double z1 = bank.getFloat("z1",   i)*10;
                double x2 = bank.getFloat("x2",   i)*10;
                double y2 = bank.getFloat("y2",   i)*10;
                double z2 = bank.getFloat("z2",   i)*10;
                double cx = bank.getFloat("cx",   i)*10;
                double cy = bank.getFloat("cy",   i)*10;
                double cz = bank.getFloat("cz",   i)*10;
                double lx = bank.getFloat("lx",   i);
                double ly = bank.getFloat("ly",   i);
                double lz = bank.getFloat("lz",   i);
                double nx = bank.getFloat("nx",   i);
                double ny = bank.getFloat("ny",   i);
                double nz = bank.getFloat("nz",   i);
                double sx = bank.getFloat("sx",   i);
                double sy = bank.getFloat("sy",   i);
                double sz = bank.getFloat("sz",   i);

                Cluster cls = new Cluster(DetectorType.BST, BMTType.UNDEFINED, sector, layer, id);
                cls.setAssociatedTrackID(tid);         
                cls.setLine(new Line3D(x1,y1,z1,x2,y2,z2));
                cls.setTotalEnergy(etot);
                cls.setTime(time);
                cls.setCentroid(centroid);
                cls.setCentroidError(resolution/(SVTGeometry.getPitch()/Math.sqrt(12)));
                cls.setResolution(resolution);
                cls.setL(new Vector3D(lx,ly,lz));
                cls.setN(new Vector3D(nx,ny,nz));
                cls.setS(new Vector3D(sx,sy,sz));
                cls.setPhi(Math.atan2(cy,cx));
                cls.setPhi0(Math.atan2(cy,cx));
                clusters.add(cls);
            }
            return clusters;
        }
    }
        
        
    public static List<Cluster> readBMTClusterBank(DataEvent event) {
        
        if(!event.hasBank("BMT::Clusters"))
            return null;
        else {
            List<Cluster> clusters = new ArrayList<>();        
            
            DataBank bank = event.getBank("BMT::Clusters");
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int tid    = bank.getShort("trkID", i);
                int sector = bank.getByte("sector", i);
                int layer  = bank.getByte("layer", i);
                double etot          = bank.getFloat("ETot", i);
                double time          = bank.getFloat("time", i);
                double centroid      = bank.getFloat("centroid", i);
                double centroidValue = bank.getFloat("centroidValue", i);
                double centroidError = bank.getFloat("centroidError", i);
                double resolution    = bank.getFloat("e", i)*10;
                double x1 = bank.getFloat("x1",   i)*10;
                double y1 = bank.getFloat("y1",   i)*10;
                double z1 = bank.getFloat("z1",   i)*10;
                double x2 = bank.getFloat("x2",   i)*10;
                double y2 = bank.getFloat("y2",   i)*10;
                double z2 = bank.getFloat("z2",   i)*10;
                double cx = bank.getFloat("cx",   i)*10;
                double cy = bank.getFloat("cy",   i)*10;
                double ax1 = bank.getFloat("ax1",   i)*10;
                double ay1 = bank.getFloat("ay1",   i)*10;
                double az1 = bank.getFloat("az1",   i)*10;
                double ax2 = bank.getFloat("ax2",   i)*10;
                double ay2 = bank.getFloat("ay2",   i)*10;
                double az2 = bank.getFloat("az2",   i)*10;
                double cz = bank.getFloat("cz",   i)*10;
                double lx = bank.getFloat("lx",   i);
                double ly = bank.getFloat("ly",   i);
                double lz = bank.getFloat("lz",   i);
                double nx = bank.getFloat("nx",   i);
                double ny = bank.getFloat("ny",   i);
                double nz = bank.getFloat("nz",   i);
                double sx = bank.getFloat("sx",   i);
                double sy = bank.getFloat("sy",   i);
                double sz = bank.getFloat("sz",   i);   
                // cluster
                Cluster cls = new Cluster(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, layer, id);
                if(cls.getType()==BMTType.C) { 
                    double   theta  = bank.getFloat("theta",   i);
                    Line3D ln = new Line3D(ax1,ay1,az1, ax2,ay2,az2);
                    Point3D  origin = new Point3D(x1,y1,z1);
                    Point3D  center = ln.distance(origin).origin();
                    Vector3D normal = ln.direction();
                    Arc3D arc = new Arc3D(origin, center, normal, theta);
                    cls.setArc(arc);
                    cls.setCentroidValue(centroidValue*10);
                    cls.setCentroidError(centroidError*10);
                } else {
                    Line3D ln = new Line3D(x1,y1,z1, x2,y2,z2);
                    cls.setLine(ln);
                    cls.setCentroidValue(centroidValue);
                    cls.setCentroidError(centroidError);
                    cls.setPhi(Math.atan2(cy,cx));
                    cls.setPhi0(Math.atan2(cy,cx));
                }
                cls.setAssociatedTrackID(tid);
                cls.setTotalEnergy(etot);
                cls.setTime(time);
                cls.setCentroid(centroid);
                cls.setResolution(resolution);
                cls.setL(new Vector3D(lx,ly,lz));
                cls.setN(new Vector3D(nx,ny,nz));
                cls.setS(new Vector3D(sx,sy,sz));
                clusters.add(cls);
            }
            return clusters;
        }
    }

    
    
    public static List<Cross> readBSTCrossBank(DataEvent event) {
        
        if(!event.hasBank("BST::Crosses"))
            return null;
        else {
            List<Cross> crosses = new ArrayList<>();        
    
            DataBank bank = event.getBank("BST::Crosses");        
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int tid    = bank.getShort("trkID", i);
                int sector = bank.getByte("sector", i);
                int region = bank.getByte("region", i);
                double x   = bank.getFloat("x", i)*10;
                double y   = bank.getFloat("y", i)*10;
                double z   = bank.getFloat("z", i)*10;
                double err_x = bank.getFloat("err_x", i)*10;
                double err_y = bank.getFloat("err_y", i)*10;
                double err_z = bank.getFloat("err_z", i)*10;
                double x0  = bank.getFloat("x0", i)*10;
                double y0  = bank.getFloat("y0", i)*10;
                double z0  = bank.getFloat("z0", i)*10;
                double err_x0 = bank.getFloat("err_x0", i)*10;
                double err_y0 = bank.getFloat("err_y0", i)*10;
                double err_z0 = bank.getFloat("err_z0", i)*10;
                double ux = bank.getFloat("ux", i);
                double uy = bank.getFloat("uy", i);
                double uz = bank.getFloat("uz", i);
                int clid1 = bank.getShort("Cluster1_ID", i);
                int clid2 = bank.getShort("Cluster2_ID", i);
                Cross cr = new Cross(DetectorType.BST, BMTType.UNDEFINED, sector, region, id);
                cr.setAssociatedTrackID(tid); 
                cr.isInSeed=true;
                cr.setDir(new Vector3D(ux,uy,uz));
                cr.setPoint(new Point3D(x,y,z));
                cr.setPointErr(new Point3D(err_x,err_y,err_z));
                cr.setPoint0(new Point3D(x0,y0,z0));
                cr.setPointErr0(new Point3D(err_x0,err_y0,err_z0));
                cr.setOrderedRegion(region);
                cr.setCluster1(new Cluster(DetectorType.BST, BMTType.UNDEFINED, sector, 2*region-1, clid1));
                cr.setCluster2(new Cluster(DetectorType.BST, BMTType.UNDEFINED, sector, 2*region  , clid2));
                crosses.add(cr);
            }
            return crosses;
        }
    }
        
    public static List<Cross> readBMTCrossBank(DataEvent event) {
        
        if(!event.hasBank("BMT::Crosses"))
            return null;
        else {
            List<Cross> crosses = new ArrayList<>();        
    
            DataBank bank = event.getBank("BMT::Crosses");
            for(int i = 0; i < bank.rows(); i++) {
                int id     = bank.getShort("ID", i);
                int tid    = bank.getShort("trkID", i);
                int sector = bank.getByte("sector", i);
                int region = bank.getByte("region", i);
                int layer  = bank.getByte("layer", i);
                double x   = bank.getFloat("x", i)*10;
                double y   = bank.getFloat("y", i)*10;
                double z   = bank.getFloat("z", i)*10;
                double err_x = bank.getFloat("err_x", i)*10;
                double err_y = bank.getFloat("err_y", i)*10;
                double err_z = bank.getFloat("err_z", i)*10;
                double x0  = bank.getFloat("x0", i)*10;
                double y0  = bank.getFloat("y0", i)*10;
                double z0  = bank.getFloat("z0", i)*10;
                double err_x0 = bank.getFloat("err_x0", i)*10;
                double err_y0 = bank.getFloat("err_y0", i)*10;
                double err_z0 = bank.getFloat("err_z0", i)*10;
                double ux = bank.getFloat("ux", i);
                double uy = bank.getFloat("uy", i);
                double uz = bank.getFloat("uz", i);
                int clid1 = bank.getShort("Cluster1_ID", i);
                if(layer==0) continue;
                Cross cr = new Cross(DetectorType.BMT, BMTGeometry.getDetectorType(layer), sector, region, id);
                cr.setAssociatedTrackID(tid); 
                cr.isInSeed=true;
                cr.setDir(new Vector3D(ux,uy,uz));
                cr.setPoint(new Point3D(x,y,z));
                cr.setPointErr(new Point3D(err_x,err_y,err_z));
                cr.setPoint0(new Point3D(x0,y0,z0));
                cr.setPointErr0(new Point3D(err_x0,err_y0,err_z0));
                cr.setOrderedRegion(Constants.getInstance().BMTGEOMETRY.getLayer(region, cr.getType())+SVTGeometry.NREGIONS); // RDV check if is used and fix definition here and in CrossMaker
                cr.setCluster1(new Cluster(DetectorType.BMT, cr.getType(), sector, Constants.getInstance().BMTGEOMETRY.getLayer(region, cr.getType()), clid1));
                crosses.add(cr);
            }
            return crosses;
        }
    }
    
    public static List<Seed> readCVTSeedsBank(DataEvent event, double xb, double yb) {
        
        if(!event.hasBank("CVT::Seeds"))
            return null;
        else {
            List<Seed> seeds = new ArrayList<>();        
    
            DataBank bank = event.getBank("CVT::Seeds");
            for(int i = 0; i < bank.rows(); i++) {
                int    tid    = bank.getShort("ID", i);
                double pt     = bank.getFloat("pt", i);
                double phi0   = bank.getFloat("phi0", i);
                double tandip = bank.getFloat("tandip", i);
                double z0     = bank.getFloat("z0", i)*10;
                double d0     = bank.getFloat("d0", i)*10;
                int    q      = bank.getByte("q", i);
                int    type   = bank.getByte("fittingMethod", i);
//                double xb     = bank.getFloat("xb", i);
//                double yb     = bank.getFloat("yb", i);
                Helix helix = new Helix( pt, d0, phi0, z0, tandip, q, xb, yb, Constants.getSolenoidMagnitude());
                double[][] covmatrix = new double[5][5];
                covmatrix[0][0] = bank.getFloat("cov_d02", i)*10*10;
                covmatrix[0][1] = bank.getFloat("cov_d0phi0", i)*10 ;
                covmatrix[0][2] = bank.getFloat("cov_d0rho", i);
                covmatrix[1][0] = bank.getFloat("cov_d0phi0", i)*10 ;
                covmatrix[1][1] = bank.getFloat("cov_phi02", i);
                covmatrix[1][2] = bank.getFloat("cov_phi0rho", i)/10 ;
                covmatrix[2][0] = bank.getFloat("cov_d0rho", i);
                covmatrix[2][1] = bank.getFloat("cov_phi0rho", i)/10 ;
                covmatrix[2][2] = bank.getFloat("cov_rho2", i)/10/10;
                covmatrix[3][3] = bank.getFloat("cov_z02", i)*10*10;
                covmatrix[3][4] = bank.getFloat("cov_z0tandip", i)*10;
                covmatrix[4][3] = bank.getFloat("cov_z0tandip", i)*10;
                covmatrix[4][4] = bank.getFloat("cov_tandip2", i);
                double circleChi2 = bank.getFloat("circlefit_chi2_per_ndf", i);
                double lineChi2   = bank.getFloat("linefit_chi2_per_ndf", i);
                double chi2       = bank.getFloat("chi2", i);
                int    ndf        = bank.getShort("ndf", i);
                Seed seed = new Seed();
                seed.setId(tid);
                seed.setHelix(helix);
                seed.getHelix().setCovMatrix(covmatrix);
                seed.setStatus(type);
                seed.setCircleFitChi2PerNDF(circleChi2);
                seed.setLineFitChi2PerNDF(lineChi2);
                seed.setChi2(chi2);
                seed.setNDF(ndf);
                seeds.add(seed);
            }
            return seeds;
        }
    }    
    
    public static List<Track> readCVTTracksBank(DataEvent event, double xb, double yb) {
        
        if(!event.hasBank("CVT::Tracks"))
            return null;
        else {
            List<Track> tracks = new ArrayList<>();        
    
            DataBank bank = event.getBank("CVT::Tracks");
            for(int i = 0; i < bank.rows(); i++) {
                int    tid    = bank.getShort("ID", i);
                double pt     = bank.getFloat("pt", i);
                double phi0   = bank.getFloat("phi0", i);
                double tandip = bank.getFloat("tandip", i);
                double z0     = bank.getFloat("z0", i)*10;
                double d0     = bank.getFloat("d0", i)*10;
                int    q      = bank.getByte("q", i);
//                double xb     = bank.getFloat("xb", i);
//                double yb     = bank.getFloat("yb", i);
                Helix helix = new Helix( pt, d0, phi0, z0, tandip, q, xb, yb, Constants.getSolenoidMagnitude());
                double[][] covmatrix = new double[5][5];
                covmatrix[0][0] = bank.getFloat("cov_d02", i)*10*10;
                covmatrix[0][1] = bank.getFloat("cov_d0phi0", i)*10 ;
                covmatrix[0][2] = bank.getFloat("cov_d0rho", i);
                covmatrix[1][0] = bank.getFloat("cov_d0phi0", i)*10 ;
                covmatrix[1][1] = bank.getFloat("cov_phi02", i);
                covmatrix[1][2] = bank.getFloat("cov_phi0rho", i)/10 ;
                covmatrix[2][0] = bank.getFloat("cov_d0rho", i);
                covmatrix[2][1] = bank.getFloat("cov_phi0rho", i)/10 ;
                covmatrix[2][2] = bank.getFloat("cov_rho2", i)/10/10;
                covmatrix[3][3] = bank.getFloat("cov_z02", i)*10*10;
                covmatrix[3][4] = bank.getFloat("cov_z0tandip", i)*10;
                covmatrix[4][3] = bank.getFloat("cov_z0tandip", i)*10;
                covmatrix[4][4] = bank.getFloat("cov_tandip2", i);
                int    status   = bank.getShort("status", i);
                double chi2     = bank.getFloat("chi2", i);
                int    ndf      = bank.getShort("ndf", i);
                int    pid      = bank.getInt("pid", i);
                int    seedId   = bank.getShort("seedID", i);
                int    type   = bank.getByte("fittingMethod", i);
                Seed seed = new Seed();
                seed.setId(seedId);
                seed.setStatus(type);
                Track track = new Track(helix);
                track.setId(tid);
                track.getHelix().setCovMatrix(covmatrix);
                track.setChi2(chi2);
                track.setNDF(ndf);
                track.setPID(pid);
                track.setKFIterations((int) status/1000);
                track.setSeed(seed);
                tracks.add(track);
            }
            return tracks;
        }
    }
    
    
}
