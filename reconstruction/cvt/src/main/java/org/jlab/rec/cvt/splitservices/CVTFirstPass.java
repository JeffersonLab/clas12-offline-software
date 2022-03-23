package org.jlab.rec.cvt.splitservices;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTConstants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.services.CosmicTracksRec;
import org.jlab.rec.cvt.svt.SVTParameters;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTFirstPass extends CVTEngine {

    private CosmicTracksRec   strgtTrksRec = null;
    private TracksFromTargetRec trksFromTargetRec = null;
    private int Run = -1;
    
    public CVTFirstPass() {
        super("CVTFirstPass");
        strgtTrksRec      = new CosmicTracksRec();
        trksFromTargetRec = new TracksFromTargetRec();
    }

    
    @Override
    public boolean init() {
        
        this.loadConfiguration();
        this.initConstantsTables();
        this.loadGeometries();
        this.registerBanks();
        return true;
    }
    private void registerBanks() {
        super.registerOutputBank("BMTRec::Hits");
        super.registerOutputBank("BMTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("BSTRec::Hits");
        super.registerOutputBank("BSTRec::Clusters");
        super.registerOutputBank("BSTRec::Crosses");
        super.registerOutputBank("CVTRec::Seeds");
        super.registerOutputBank("CVTRec::Tracks");
        super.registerOutputBank("CVTRec::Trajectory");        
    }
   
    @Override
    public boolean processDataEvent(DataEvent event) {
        this.setRunConditionsParameters(event, Run, false, "");
        
        Swim swimmer = new Swim();
        ADCConvertor adcConv = new ADCConvertor();

        RecoBankWriter rbc = new RecoBankWriter();

        IndexedTable svtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/svt/status");
        IndexedTable bmtStatus = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_status");
        IndexedTable bmtTime   = this.getConstantsManager().getConstants(this.getRun(), "/calibration/mvt/bmt_time");
        IndexedTable beamPos   = this.getConstantsManager().getConstants(this.getRun(), "/geometry/beam/position");

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, svtStatus);
        if(Constants.SVTONLY==false)
          hitRead.fetch_BMTHits(event, adcConv, swimmer, bmtStatus, bmtTime);

        List<Hit> hits = new ArrayList<>();
        //I) get the hits
        List<Hit> SVThits = hitRead.getSVTHits();
        if(SVThits.size()>SVTParameters.MAXSVTHITS)
            return true;
        if (SVThits != null && !SVThits.isEmpty()) {
            hits.addAll(SVThits);
        }

        List<Hit> BMThits = hitRead.getBMTHits();
        if (BMThits != null && BMThits.size() > 0) {
            hits.addAll(BMThits);

            if(BMThits.size()>BMTConstants.MAXBMTHITS)
                 return true;
        }

        //II) process the hits		
        //1) exit if hit list is empty
        if (hits.isEmpty()) {
            return true;
        }
       
        List<Cluster> clusters = new ArrayList<>();
        List<Cluster> SVTclusters = new ArrayList<>();
        List<Cluster> BMTclusters = new ArrayList<>();

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters.addAll(clusFinder.findClusters(SVThits));     
        if(BMThits != null && BMThits.size() > 0) {
            clusters.addAll(clusFinder.findClusters(BMThits)); 
        }
        if (clusters.isEmpty()) {
            rbc.appendCVTBanks(event, SVThits, BMThits, null, null, null, null, null,0);
            return true;
        }
        
        if (!clusters.isEmpty()) {
            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).getDetector() == DetectorType.BST) {
                    SVTclusters.add(clusters.get(i));
                }
                if (clusters.get(i).getDetector() == DetectorType.BMT) {
                    BMTclusters.add(clusters.get(i));
                }
            }
        }

        CrossMaker crossMake = new CrossMaker();
        List<ArrayList<Cross>> crosses = crossMake.findCrosses(clusters);
        if(crosses.get(0).size() > SVTParameters.MAXSVTCROSSES ) {
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, null, null, null,0);
            return true; 
        }
        
        if(Constants.ISCOSMICDATA) {
            strgtTrksRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                    crosses, rbc, swimmer);
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
            trksFromTargetRec.getSeeds(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                crosses, xb , yb, rbc, swimmer);
            trksFromTargetRec.getTracks(event, true, 1);
        }
        return true;
    }
     
}