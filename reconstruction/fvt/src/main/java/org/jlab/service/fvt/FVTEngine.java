package org.jlab.service.fvt;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
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
    
    @Override
    public boolean processDataEvent(DataEvent event) {

        List<Cluster> clusters = new ArrayList<Cluster>();    
        List<Cross> crosses = new ArrayList<Cross>();    
        
        this.FieldsConfig = this.getFieldsConfig();
        this.Run = this.getRun();
        
        RecoBankWriter rbc = new RecoBankWriter();
        //I) get the hits
        HitReader hitRead = new HitReader();
        hitRead.fetch_FMTHits(event);
        List<Hit> hits = hitRead.get_FMTHits();
        List<Track> dcTracks = null;
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
                dcTracks = trkLister.getDCTracks(event);
                if(dcTracks==null) {
                    rbc.appendFMTBanks(event, FMThits, clusters, null);
                    return true;
                }
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
                            }
                        }
                    }
                }
                    
                //crosses = crossMake.findCrosses(clusters);
                
            }
            
            rbc.appendFMTBanks(event, FMThits, clusters, crosses);
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
