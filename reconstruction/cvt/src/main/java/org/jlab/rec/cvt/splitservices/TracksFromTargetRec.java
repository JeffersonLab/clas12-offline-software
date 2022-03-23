package org.jlab.rec.cvt.splitservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.services.RecUtilities;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrackSeeder;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.TrackSeederCA;

/**
 *
 * @author ziegler
 */
public class TracksFromTargetRec {
    private final StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
    private final RecUtilities recUtil = new RecUtilities();
    
    private List<Seed> seeds = new ArrayList<>();
    private List<Track> trkcands = new ArrayList<>();
    private List<Hit> SVThits = new ArrayList<>();;
    private List<Hit> BMThits = new ArrayList<>();;
    private List<Cluster> SVTclusters = new ArrayList<>();;
    private List<Cluster> BMTclusters = new ArrayList<>();;
    private List<ArrayList<Cross>> crosses = new ArrayList<ArrayList<Cross>>();
    private double xb; 
    private double yb;
    private RecoBankWriter rbc;
    private Swim swimmer;
       
    public boolean getSeeds(DataEvent event,  
            List<Hit> SVThits, List<Hit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            double xb, double yb,
            RecoBankWriter rbc,
            Swim swimmer) {
        this.SVThits = SVThits;
        this.BMThits = BMThits;
        this.SVTclusters = SVTclusters;
        this.BMTclusters = BMTclusters;
        this.crosses = crosses;
        this.xb = xb;
        this.yb = yb;
        this.rbc = rbc;
        this.swimmer = swimmer;
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        seeds.clear();
        
        // make list of crosses consistent with a track candidate
        List<Seed> seeds = null;
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder(xb, yb);
            seeds = trseed.findSeed(crosses.get(0), crosses.get(1), Constants.SVTONLY);
            // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//            if(Constants.EXCLUDELAYERS==true) {
//                seeds = recUtil.reFit(seeds, swimmer, trseed); // RDV can we juts refit?
//            }
        } else {
            if(Constants.SVTONLY) {
                TrackSeeder trseed = new TrackSeeder(swimmer, xb, yb);
                trseed.unUsedHitsOnly = true;
                seeds = trseed.findSeed(crosses.get(0), null);
            } else {
                TrackSeederCA trseed = new TrackSeederCA(swimmer, xb, yb);  // cellular automaton seeder
                seeds = trseed.findSeed(crosses.get(0), crosses.get(1));
                
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                if(Constants.SVTSEEDING) {
                    TrackSeeder trseed2 = new TrackSeeder(swimmer, xb, yb);
                    trseed2.unUsedHitsOnly = true;
                    seeds.addAll( trseed2.findSeed(crosses.get(0), crosses.get(1)));
                    // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//                    if(Constants.EXCLUDELAYERS==true) {
//                        seeds = recUtil.reFit(seeds, swimmer, trseed, trseed2);
//                    }
                }
                if(!Constants.seedBeamSpotConstraint()) {
                    List<Seed> failed = new ArrayList<>();
                    for(Seed s : seeds) {
                        if(!recUtil.reFitCircle(s, Constants.SEEDFITITERATIONS, xb, yb))
                            failed.add(s);
                    }
                    seeds.removeAll(failed);
                }
            }
        }
        if(seeds ==null || seeds.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, null, 0);
            return false;
        }   
        
        // create track candidate list, set seed IDs and do a final update to crosses
        List<Track> trkcands = new ArrayList<>();        
        for(Seed seed : seeds) {
            int id = trkcands.size()+1;
            seed.setId(id);
            Track track = new Track(seed);
            track.update_Crosses(id);
            trkcands.add(track);   
        }
        // Got seeds;
        this.seeds.addAll(seeds);
        return true;
    }
    
    public boolean getTracks(DataEvent event, boolean searchMissingCls, int pass) {
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        trkcands.clear();
        KFitter kf = new KFitter(Constants.KFFILTERON, Constants.KFITERATIONS, Constants.kfBeamSpotConstraint(), swimmer, Constants.KFMATLIB);
        Measurements surfaces = new Measurements(false, xb, yb);
        for (Seed seed : seeds) { 
            Point3D  v = seed.getHelix().getVertex();
            Vector3D p = seed.getHelix().getPXYZ(solenoidValue);
            int charge = (int) (Math.signum(solenoidScale)*seed.getHelix().getCharge());
            if(solenoidValue<0.001)
                charge = 1;
            double[] pars = recUtil.mcTrackPars(event);
            if(Constants.INITFROMMC) {
                v = new Point3D(pars[0],pars[1],pars[2]);
                p = new Vector3D(pars[3],pars[4],pars[5]);
                if(solenoidValue<0.001) p.scale(100/p.mag());
            }
            Helix hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                            solenoidValue, xb , yb, Helix.Units.MM);
            double[][] cov = seed.getHelix().getCovMatrix();

            if(solenoidValue>0.001 && Constants.LIGHTVEL * seed.getHelix().radius() *solenoidValue<Constants.PTCUT)
                continue;
            double mass = this.getPartMass(event, seed.getId());
            kf.init(hlx, cov, xb, yb, 0, surfaces.getMeasurements(seed), mass) ;
            kf.runFitter();
            if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null) { 
                Track fittedTrack = new Track(seed, kf);
                for(Cross c : fittedTrack) { 
                    if(c.getDetector()==DetectorType.BST) {
                        c.getCluster1().setAssociatedTrackID(0);
                        c.getCluster2().setAssociatedTrackID(0);
                    }
                }
                if(searchMissingCls) {
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.findClustersOnTrk(SVTclusters, seed.getClusters(), fittedTrack.getHelix(), 
                            fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                    List<Cluster> bmtclsOnTrack = recUtil.findBMTClustersOnTrk(BMTclusters, seed.getCrosses(), fittedTrack.getHelix(), 
                            fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                    CrossMaker cm = new CrossMaker();
                    List<Cross> bmtcrsOnTrack = recUtil.findCrossesOnBMTTrack(bmtclsOnTrack, cm, crosses.get(1).size()+2000);

                    if(clsOnTrack.size()>0 || bmtcrsOnTrack.size()>0) { 
                        if(clsOnTrack.size()>0) 
                            seed.getClusters().addAll(clsOnTrack); //VZ check for additional clusters, and only then re-run KF adding new clusters                    
                        if(bmtcrsOnTrack.size()>0) {
                            seed.getCrosses().addAll(bmtcrsOnTrack); //VZ check for additional crosses, and only then re-run KF adding new clusters                    
                            crosses.get(1).addAll(bmtcrsOnTrack);
                            for(Cross c : bmtcrsOnTrack) {
                                seed.getClusters().add(c.getCluster1());
                            }
                        }
                        //reset pars
                        v = fittedTrack.getHelix().getVertex();
                        p = fittedTrack.getHelix().getPXYZ(solenoidValue);
                        charge = (int) (Math.signum(solenoidScale)*fittedTrack.getHelix().getCharge());
                        if(solenoidValue<0.001)
                            charge = 1;
                        hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                                        solenoidValue, xb, yb, Helix.Units.MM);

                        kf.init(hlx, cov, xb, yb, 0, surfaces.getMeasurements(seed), mass) ;
                        kf.runFitter();

                        // RDV get rid of added clusters if not true
                        if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null)
                            fittedTrack = new Track(seed, kf);
                    }
                }
                trkcands.add(fittedTrack);
            }
        }
    

        // reset cross and cluster IDs
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
                c.setAssociatedTrackID(-1);
            }
        }
        for(Cluster c : SVTclusters) {
            c.setAssociatedTrackID(-1);
        }
        for(Cluster c : BMTclusters) {
            c.setAssociatedTrackID(-1);
        }
        
        List<Track> tracks = null;
        if(!trkcands.isEmpty()) {
            // do a final cleanup
            TrackListFinder.removeOverlappingTracks(trkcands); 
            if(trkcands.isEmpty()) System.out.println("Error: no tracks left after overlap remover");
            
            tracks = TrackListFinder.getTracks(trkcands, swimmer);
            // update crosses and clusters on track
            for(int it = 0; it < tracks.size(); it++) {
                int id = it + 1;
                tracks.get(it).setId(id); 
                tracks.get(it).update_Crosses(id);
                tracks.get(it).update_Clusters(id);
                tracks.get(it).setTrackCovMat(recUtil.getCovMatInTrackRep(tracks.get(it)));
            }
        }
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
                if(c.getAssociatedTrackID()==-1) {
                    c.reset();
                    if(det==1 && c.getId()>2000) { //if matched cross failed tracking resol requirements, reset its id
                        c.setId(c.getId()-1000);
                    } 
                }
                
            }
        }


//        //------------------------ RDV check with Veronique
//        // set index associations
//        if (tracks.size() > 0) {
//            recUtil.CleanupSpuriousCrosses(crosses, tracks, SVTGeom) ;
//        }
        rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, seeds, tracks, pass);

        return true;

    }

    
    Map<Integer, Seed>   trkMap = new HashMap<Integer, Seed>();
    Map<Integer, HashMap<Integer, Cross>>   svtCrsMap = new HashMap<Integer, HashMap<Integer, Cross>>();
    Map<Integer, HashMap<Integer, Cross>>   bmtCrsMap = new HashMap<Integer, HashMap<Integer, Cross>>();
    HashMap<Integer, Cross>   svtCrs = new HashMap<Integer, Cross>();
    HashMap<Integer, Cross>   bmtCrs = new HashMap<Integer, Cross>();
     Map<Integer, HashMap<Integer, Cluster>>   svtClsMap = new HashMap<Integer, HashMap<Integer, Cluster>>();
    Map<Integer, HashMap<Integer, Cluster>>   bmtClsMap = new HashMap<Integer, HashMap<Integer, Cluster>>();
    HashMap<Integer, Cluster>   svtCls = new HashMap<Integer, Cluster>();
    HashMap<Integer, Cluster>   bmtCls = new HashMap<Integer, Cluster>();
    BMTGeometry bGeom = new BMTGeometry();
    
    void getSeedsFromBanks(DataEvent event, double xb, double yb) {
        trkMap.clear();
        svtCrsMap.clear();
        bmtCrsMap.clear();
        svtClsMap.clear();
        bmtClsMap.clear();
        System.out.println("GET SEEDS FROM BANKS....");
        if(event.getBank("CVTRec::Tracks")==null) return;
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        DataBank tbank = event.getBank("CVTRec::Tracks");
        for(int i = 0; i < tbank.rows(); i++) {
            svtCrs.clear();
            bmtCrs.clear();
            double pt = (double) tbank.getFloat("pt", i);
            double phi0 = (double) tbank.getFloat("phi0", i);
            double tandip = (double) tbank.getFloat("tandip", i);
            double z0 = (double) tbank.getFloat("z0", i);
            double d0 = (double) tbank.getFloat("d0", i);
            int q = (int) (double) tbank.getByte("q", i);
            //double xb = tbank.getDouble("xb", i);
            //double yb = tbank.getDouble("yb", i);
            org.jlab.rec.cvt.trajectory.Helix he = 
                    new org.jlab.rec.cvt.trajectory.Helix( pt, d0, phi0, z0, tandip, q, xb, yb, solenoidValue);
            Seed seed = new Seed();
            seed.setHelix(he);
            double[][] covmatrix = new double[5][5];
            covmatrix[0][0] = (double)tbank.getFloat("cov_d02", i)*10*10 ;
            covmatrix[0][1] = (double)tbank.getFloat("cov_d0phi0", i)*10 ;
            covmatrix[0][2] = (double)tbank.getFloat("cov_d0rho", i);
            covmatrix[1][1] = (double)tbank.getFloat("cov_phi02", i);
            covmatrix[1][2] = (double)tbank.getFloat("cov_phi0rho", i)/10 ;
            covmatrix[2][2] = (double)tbank.getFloat("cov_rho2", i)/10/10 ;
            covmatrix[3][3] = (double)tbank.getFloat("cov_z02", i)*10*10 ;
            covmatrix[4][4] = (double)tbank.getFloat("cov_tandip2", i);
            seed.getHelix().setCovMatrix(covmatrix);
            int tid = tbank.getShort("ID", i);
            seed.setId(tid);
            trkMap.put((int) tid, seed);
            for (int j = 0; j < 9; j++) {
                String hitStrg = "Cross";
                hitStrg += (j + 1);
                hitStrg += "_ID";  
                int cid = (int) tbank.getShort(hitStrg, j);
                Cross cr = null;
                if(cid==-1) continue;
                if(cid>1000) {
                    cr = new Cross(DetectorType.BMT, BMTType.UNDEFINED, 0, 0, cid);
                    bmtCrs.put(cid, cr);
                } else {
                    cr = new Cross(DetectorType.BST, BMTType.UNDEFINED, 0, 0, cid);
                    svtCrs.put(cid, cr);
                    
                }
                svtCrsMap.put((int) tid, svtCrs);
                bmtCrsMap.put((int) tid, bmtCrs);
            }
        }
        System.out.println("Got seeds....");
        //Look on Cross bank
        DataBank cbank = event.getBank("BSTRec::Crosses");
        
        for(int i = 0; i < cbank.rows(); i++) {
            int id = (int) cbank.getShort("ID", i);
            int sec = (int) cbank.getByte("sector", i);
            int region = (int) (int) cbank.getByte("region", i);
            double x = (double) cbank.getFloat("x", i);
            double y = (double) cbank.getFloat("y", i);
            double z = (double) cbank.getFloat("z", i);
            double err_x = (double) cbank.getFloat("err_x", i);
            double err_y = (double) cbank.getFloat("err_y", i);
            double err_z = (double) cbank.getFloat("err_z", i);
            double ux = (double) cbank.getFloat("ux", i);
            double uy = (double) cbank.getFloat("uy", i);
            double uz = (double) cbank.getFloat("uz", i);
            int tid = (int) cbank.getShort("trkID", i);
            int clid1 = (int) cbank.getShort("Cluster1_ID", i);
            int clid2 = (int) cbank.getShort("Cluster2_ID", i);
            System.out.println("Cross id from bank "+id);
            Cross cr = svtCrsMap.get(tid).get(id);
            cr.setCluster1(new Cluster(DetectorType.BST, BMTType.UNDEFINED, sec, 2*region-1, clid1));
            cr.setCluster2(new Cluster(DetectorType.BST, BMTType.UNDEFINED, sec, 2*region, clid2));
            
            cr.setAssociatedTrackID(tid); 
            cr.isInSeed=true;
            cr.setDir(new Vector3D(ux,uy,uz));
            cr.setPoint(new Point3D(x,y,z));
            cr.setPointErr(new Point3D(err_x,err_y,err_z));
            cr.setSector(sec);
            cr.setRegion(region);
            System.out.println("Reco "+cr.printInfo());
        }
        cbank = event.getBank("BMTRec::Crosses");
        for(int i = 0; i < cbank.rows(); i++) {
            int id = (int) cbank.getShort("ID", i);
            int sec = (int) cbank.getByte("sector", i);
            int region = (int) (int) cbank.getByte("region", i);
            double x = (double) cbank.getFloat("x", i);
            double y = (double) cbank.getFloat("y", i);
            double z = (double) cbank.getFloat("z", i);
            double err_x = (double) cbank.getFloat("err_x", i);
            double err_y = (double) cbank.getFloat("err_y", i);
            double err_z = (double) cbank.getFloat("err_z", i);
            double ux = (double) cbank.getFloat("ux", i);
            double uy = (double) cbank.getFloat("uy", i);
            double uz = (double) cbank.getFloat("uz", i);
            int tid = (int) cbank.getShort("trkID", i);
            int clid1 = (int) cbank.getShort("Cluster1_ID", i);
            
            Cross cr = bmtCrsMap.get(tid).get(id);
            if(err_z==0 && err_x!=0) {
                cr.setType(BMTType.Z);
                cr.setCluster1(new Cluster(DetectorType.BMT, BMTType.Z, sec, BMTGeometry.getlZ()[region-1], clid1));
            } else {
                cr.setType(BMTType.C);
                cr.setCluster1(new Cluster(DetectorType.BMT, BMTType.C, sec, BMTGeometry.getlC()[region-1], clid1));
            }
            cr.setAssociatedTrackID(tid); 
            cr.isInSeed=true;
            cr.setDir(new Vector3D(ux,uy,uz));
            cr.setPoint(new Point3D(x,y,z));
            cr.setPointErr(new Point3D(err_x,err_y,err_z));
            cr.setSector(sec);
            cr.setRegion(region);
            System.out.println("Reco "+cr.printInfo());
        }
        
        //Look on clusters banks
        cbank = event.getBank("BSTRec::Clusters");
        svtCls.clear();
        for(int i = 0; i < cbank.rows(); i++) {
            int id = (int) cbank.getShort("ID", i);
            int tid = (int) cbank.getShort("trkID", i);
            int sec = (int) cbank.getByte("sector", i);
            int layer = (int) (int) cbank.getByte("layer", i);
            double centroid = (double) cbank.getFloat("centroid", i);
            double err = cbank.getFloat("e", i);
            double x1 = cbank.getFloat("x1",   i)*10;
            double y1 = cbank.getFloat("y1",   i)*10;
            double z1 = cbank.getFloat("z1",   i)*10;
            double x2 = cbank.getFloat("x2",   i)*10;
            double y2 = cbank.getFloat("y2",   i)*10;
            double z2 = cbank.getFloat("z2",   i)*10;
            double lx = cbank.getFloat("lx",   i);
            double ly = cbank.getFloat("ly",   i);
            double lz = cbank.getFloat("lz",   i);
            double nx = cbank.getFloat("nx",   i);
            double ny = cbank.getFloat("ny",   i);
            double nz = cbank.getFloat("nz",   i);
            double sx = cbank.getFloat("sx",   i);
            double sy = cbank.getFloat("sy",   i);
            double sz = cbank.getFloat("sz",   i);
            
            Cluster cls = new Cluster(DetectorType.BST, BMTType.UNDEFINED, sec, layer, id);
            cls.setAssociatedTrackID(tid);
            cls.setLine(new Line3D(x1,y1,z1,x2,y2,z2));
            cls.setCentroid(centroid);
            cls.setCentroidError(err);
            cls.setL(new Vector3D(lx,ly,lz));
            cls.setN(new Vector3D(nx,ny,nz));
            cls.setS(new Vector3D(sx,sy,sz));
            System.out.println("Reco ");cls.printInfo();
            svtCls.put(id, cls);
            if(svtClsMap.get(tid).isEmpty()) {
                svtClsMap.put(tid, svtCls);
            } else {
                svtClsMap.replace(tid, svtCls);
            }
        }
        cbank = event.getBank("BMTRec::Clusters");
        bmtCls.clear();
        for(int i = 0; i < cbank.rows(); i++) {
            int id = (int) cbank.getShort("ID", i);
            int tid = (int) cbank.getShort("trkID", i);
            int sec = (int) cbank.getByte("sector", i);
            int layer = (int) (int) cbank.getByte("layer", i);
            double centroid = (double) cbank.getFloat("centroid", i);
            double err = cbank.getFloat("e", i);
            double x1 = cbank.getFloat("x1",   i)*10;
            double y1 = cbank.getFloat("y1",   i)*10;
            double z1 = cbank.getFloat("z1",   i)*10;
            double x2 = cbank.getFloat("x2",   i)*10;
            double y2 = cbank.getFloat("y2",   i)*10;
            double z2 = cbank.getFloat("z2",   i)*10;
            double cx = cbank.getFloat("cx",   i)*10;
            double cy = cbank.getFloat("cy",   i)*10;
            double cz = cbank.getFloat("cz",   i)*10;
            double ax2 = cbank.getFloat("ax2",   i)*10;
            double ay2 = cbank.getFloat("ay2",   i)*10;
            double az2 = cbank.getFloat("az2",   i)*10;
            double lx = cbank.getFloat("lx",   i);
            double ly = cbank.getFloat("ly",   i);
            double lz = cbank.getFloat("lz",   i);
            double nx = cbank.getFloat("nx",   i);
            double ny = cbank.getFloat("ny",   i);
            double nz = cbank.getFloat("nz",   i);
            double sx = cbank.getFloat("sx",   i);
            double sy = cbank.getFloat("sy",   i);
            double sz = cbank.getFloat("sz",   i);
           
            Cluster cls = null;
            if(layer==1 || layer == 4 || layer == 6) { 
                cls = new Cluster(DetectorType.BMT, BMTType.C, sec, layer, id);
                double theta = cbank.getFloat("theta",   i);
                Point3D origin = new Point3D(x1,y1,z1);
                Vector3D normal = new Vector3D(0,0,1);
                bGeom.toGlobal(layer, sec).apply(normal);
                Point3D center = new Point3D(cx,cy,cz);
                Arc3D arc = new Arc3D(origin,center,normal,theta);
                cls.setArc(arc);
                cls.setCentroidValue(cbank.getFloat("centroidValue", i)*10);
                cls.setCentroidError(cbank.getFloat("centroidValue", i)*10);
            } else {
                cls = new Cluster(DetectorType.BMT, BMTType.Z, sec, layer, id);
                Line3D ln = new Line3D(x1,y1,z1, x2,y2,z2);
                cls.setLine(ln);
                cls.setCentroidValue(cbank.getFloat("centroidValue", i));
                cls.setCentroidError(cbank.getFloat("centroidValue", i));
            }
            
            cls.setAssociatedTrackID(tid);
            cls.setLine(new Line3D(x1,y1,z1,x2,y2,z2));
            cls.setCentroid(centroid);
            cls.setCentroidError(err);
            cls.setL(new Vector3D(lx,ly,lz));
            cls.setN(new Vector3D(nx,ny,nz));
            cls.setS(new Vector3D(sx,sy,sz));
            System.out.println("Reco ");cls.printInfo();
            bmtCls.put(id, cls);
            if(bmtClsMap.get(tid).isEmpty()) {
                bmtClsMap.put(tid, bmtCls);
            } else {
                bmtClsMap.replace(tid, bmtCls);
            }
        }
        
        // using iterators 
        Iterator<Map.Entry<Integer, Seed>> itr = trkMap.entrySet().iterator(); 
        while(itr.hasNext()) {
            Map.Entry<Integer, Seed> entry = itr.next(); 
            Seed seed = entry.getValue();
            seed.add_Clusters(new ArrayList<>(svtClsMap.get(entry.getKey()).values()));
            seed.add_Clusters(new ArrayList<>(bmtClsMap.get(entry.getKey()).values()));
            List<Cross> crosses = new ArrayList<>();
            crosses.addAll(new ArrayList<>(svtCrsMap.get(entry.getKey()).values()));
            crosses.addAll(new ArrayList<>(bmtCrsMap.get(entry.getKey()).values()));
            seed.setCrosses(crosses);
        }
    }

    private double getPartMass(DataEvent event, int trkId) {
        double mass = PhysicsConstants.massPionCharged();
        int pid = 0;
        if (!event.hasBank("RECHB::Particle") || !event.hasBank("RECHB::Track"))
            return mass;
        DataBank bank = event.getBank("RECHB::Track");

        int rows = bank.rows();
        for (int i = 0; i < rows; i++) {
            if (bank.getByte("detector", i) == 5 &&
                    bank.getShort("index", i) == trkId - 1) {
                pid = event.getBank("RECHB::Particle").getInt("pid",
                        bank.getShort("pindex", i));
            }
        }
        if(pid==0) return mass;
        
        mass = this.getMassFromPID(pid);
        System.out.println("mass "+mass);
        return mass;
    }

    private double getMassFromPID(int pid) {
        
        double mass = PhysicsConstants.massPionCharged();
        switch(pid) {
            case (11): 
                mass = PhysicsConstants.massElectron();
                break;
            case (-11): 
                mass = PhysicsConstants.massElectron();
                break;
            case (211): 
                mass = PhysicsConstants.massPionCharged();
                break;
            case (-211): 
                mass = PhysicsConstants.massPionCharged();
                break;
            case (321): 
                mass = PhysicsConstants.massKaonCharged();
                break;
            case (-321): 
                mass = PhysicsConstants.massKaonCharged();
                break;
            case (2212): 
                mass = PhysicsConstants.massProton();
                break;
            case (-2212): 
                mass = PhysicsConstants.massProton();
                break;
            default:
                mass = PhysicsConstants.massPionCharged();
        }
        return mass;
    }

}
