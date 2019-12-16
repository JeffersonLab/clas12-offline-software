package org.jlab.service.fvt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;

import org.jlab.io.base.*;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.banks.HitReader;
import org.jlab.rec.fmt.banks.RecoBankWriter;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.cluster.ClusterFinder;
import org.jlab.rec.fmt.cross.Cross;
import org.jlab.rec.fmt.cross.CrossMaker;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.CCDBConstantsLoader;
import org.jlab.rec.fvt.track.Track;
import org.jlab.rec.fvt.track.TrackList;
import org.jlab.rec.fvt.track.fit.KFitter;

/**
 * Service to return reconstructed  track candidates- the output is in hipo
 * format
 *
 * @author ziegler
 *
 */
public class FVTEngine extends ReconstructionEngine {

    org.jlab.rec.fmt.Geometry FVTGeom;

    public FVTEngine() {
        super("FMTTracks", "ziegler", "4.0");
        
        FVTGeom = new org.jlab.rec.fmt.Geometry();
        
        //GeometryLoader.Load(10, "default");
        //GeometryLoader gl = new GeometryLoader();
        //gl.LoadSurfaces();
        CCDBConstantsLoader.Load(10);
    }

    String FieldsConfig = "";
    private int Run = -1;
  
 

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public String getFieldsConfig() {
        return FieldsConfig;
    }

    public void setFieldsConfig(String fieldsConfig) {
        FieldsConfig = fieldsConfig;
    }
    
    CrossMaker crossMake;
    ClusterFinder clusFinder;
    TrackList trkLister;
    double xB = 0;
    double yB = 0;
    @Override
    public boolean processDataEvent(DataEvent event) {

        List<Cluster> clusters = new ArrayList<Cluster>();     
        List<Cross> crosses = new ArrayList<Cross>();    
        
        this.FieldsConfig = this.getFieldsConfig();
        this.Run = this.getRun();
        // get Field
        Swim swimmer = new Swim();
        RecoBankWriter rbc = new RecoBankWriter();
        //I) get the hits
        HitReader hitRead = new HitReader();
        hitRead.fetch_FMTHits(event);
        List<Hit> hits = hitRead.get_FMTHits();
        List<Track> dcTracks = null;
        KFitter kf;
        dcTracks = trkLister.getDCTracks(event);
        //II) process the hits	
        //1) exit if hit list is empty
        if (hits.size() != 0) {
            //2) find the clusters from these hits
            clusters = clusFinder.findClusters(hits);
            
            List<FittedHit> FMThits =  new ArrayList<FittedHit>();
            if (clusters.size() != 0) {
                for (int i = 0; i < clusters.size(); i++) {
                    FMThits.addAll(clusters.get(i));
                }
                
                if(dcTracks==null) {
                    rbc.appendFMTBanks(event, FMThits, clusters, null, null);
                    return true;
                }
                //map of crosses associated with trk
                Map<Integer, List<Cross> >trj = new HashMap<Integer, List<Cross> >();
                for (int j = 0; j < clusters.size(); j++) {
                    for (int i = 0; i < dcTracks.size(); i++) {
                        List<Point3D> plist = dcTracks.get(i).getTraj();
                        if(plist==null)
                            continue;
                    
                        for (int k = 0; k < plist.size(); k++) {
                            double x = plist.get(k).x();
                            double y = plist.get(k).y();
                            double z = plist.get(k).z();
                            if(clusters.get(j).get_Layer()!=k+1) // match the layers from traj
                                continue;
                            double d = clusters.get(j).calcDoca(x, y, z);
                            if(d<Constants.CIRCLECONFUSION) {
                                Cross this_cross = new Cross(clusters.get(j).get_Sector(), clusters.get(j).get_Layer(),crosses.size()+1);
                                this_cross.set_Point(clusters.get(j).calcCross(x, y, z));
                                this_cross.set_AssociatedTrackID(dcTracks.get(i).getId());
                                this_cross.set_Cluster1(clusters.get(j));
                                crosses.add(this_cross);
                                // add to seed
                                if(trj.get(dcTracks.get(i).getId())==null) {
                                    trj.put(dcTracks.get(i).getId(), new ArrayList<Cross>());
                                    trj.get(dcTracks.get(i).getId()).add(this_cross);
                                } else {
                                    trj.get(dcTracks.get(i).getId()).add(this_cross);
                                }
                            }
                        }
                    }
                }
                
                double[] pars = new double[6];
                
                // iterate on hashmap to run the fit 
                 for (Integer id : trj.keySet()) { 
                    for(Track tr:dcTracks) {
                        for (int p = 0; p<6; p++)
                            pars[p]=0;
                        if(tr.getId()!=id) continue;
                        List<Cluster> cls = new ArrayList<Cluster>();
                        List<Cross> crs = trj.get(id);
                        for(Cross c : crs) {
                            cls.add(c.get_Cluster1());
                        }
                        kf = new KFitter( cls,
                        tr.getSector(), tr.getX(), tr.getY(), tr.getZ(), 
                        tr.getPx(), tr.getPy(), tr.getPz(),
                        tr.getQ(), swimmer, 0);
                        kf.runFitter(tr.getSector());
                        
                        
                        if(kf.finalStateVec!=null) { 
                            // set the track parameters
                            tr.setQ((int)Math.signum(kf.finalStateVec.Q));
                            pars = tr.getLabPars(kf.finalStateVec);
                            swimmer.SetSwimParameters(pars[0],pars[1],pars[2],
                                    -pars[3],-pars[4],-pars[5],
                                    -tr.getQ());
        
        
                            double[] Vt = swimmer.SwimToBeamLine(xB, yB);
                            if(Vt==null)
                                return true;
        
                            tr.setX(Vt[0]);
                            tr.setY(Vt[1]);
                            tr.setZ(Vt[2]);
                            tr.setPx(-Vt[3]);
                            tr.setPy(-Vt[4]);
                            tr.setPz(-Vt[5]);
                            
                            tr.status = 1;
                            
                        }
                    }
                } 
                rbc.appendFMTBanks(event, FMThits, clusters, crosses, dcTracks); 
            }
            
        }
        
        return true;
   }

    @Override
    public boolean init() {
       
       Constants.Load();
       clusFinder = new ClusterFinder();
       crossMake = new CrossMaker();
       trkLister = new TrackList();
       return true;
    }

     
    
}
