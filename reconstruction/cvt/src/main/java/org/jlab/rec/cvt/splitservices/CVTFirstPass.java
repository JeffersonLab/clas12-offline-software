package org.jlab.rec.cvt.splitservices;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
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
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track;
import org.jlab.utils.groups.IndexedTable;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTFirstPass extends CVTEngine {
    
    public CVTFirstPass() {
        super("CVTFirstPass");
        this.setOutputBankPrefix("FP");
    }

   
    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int run = this.getRun(event);
        
        Swim swimmer = new Swim();
        ADCConvertor  adcConv = new ADCConvertor();
 
        IndexedTable svtStatus = this.getConstantsManager().getConstants(run, "/calibration/svt/status");
        IndexedTable bmtStatus = this.getConstantsManager().getConstants(run, "/calibration/mvt/bmt_status");
        IndexedTable bmtTime   = this.getConstantsManager().getConstants(run, "/calibration/mvt/bmt_time");
        IndexedTable beamPos   = this.getConstantsManager().getConstants(run, "/geometry/beam/position");

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, svtStatus);
        if(Constants.SVTONLY==false)
          hitRead.fetch_BMTHits(event, adcConv, swimmer, bmtStatus, bmtTime);

        //I) get the hits
        List<Hit> SVThits = hitRead.getSVTHits();
        if(SVThits.size()>SVTParameters.MAXSVTHITS)
            return true;

        List<Hit> BMThits = hitRead.getBMTHits();
        if(BMThits.size()>BMTConstants.MAXBMTHITS)
             return true;
        
        //II) process the hits		
        //1) exit if hit list is empty
        if (SVThits.isEmpty() && BMThits.isEmpty()) {
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
            DataBank svtHitsBank = RecoBankWriter.fillSVTHitBank(event, SVThits, this.getSvtHitBank());
            DataBank bmtHitsBank = RecoBankWriter.fillBMTHitBank(event, BMThits, this.getBmtHitBank());
            event.appendBanks(svtHitsBank, bmtHitsBank);
            return true;
        }
        
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).getDetector() == DetectorType.BST) {
                SVTclusters.add(clusters.get(i));
            }
            if (clusters.get(i).getDetector() == DetectorType.BMT) {
                BMTclusters.add(clusters.get(i));
            }
        }
        

        //3) make crosses
        CrossMaker crossMake = new CrossMaker();
        List<ArrayList<Cross>> crosses = crossMake.findCrosses(clusters);
        if(crosses.get(0).size() > SVTParameters.MAXSVTCROSSES ) {
            DataBank svtHitsBank = RecoBankWriter.fillSVTHitBank(event, SVThits, this.getSvtHitBank());
            DataBank bmtHitsBank = RecoBankWriter.fillBMTHitBank(event, BMThits, this.getBmtHitBank());
            DataBank svtClustersBank = RecoBankWriter.fillSVTClusterBank(event, SVTclusters, this.getSvtClusterBank());
            DataBank bmtClustersBank = RecoBankWriter.fillBMTClusterBank(event, BMTclusters, this.getBmtClusterBank());
            event.appendBanks(svtHitsBank, bmtHitsBank, svtClustersBank, bmtClustersBank);
            return true; 
        }
        
        if(Constants.ISCOSMICDATA) {
            CosmicTracksRec strgtTrksRec = new CosmicTracksRec();
            strgtTrksRec.processEvent(event, SVThits, BMThits, SVTclusters, BMTclusters, 
                    crosses, swimmer);
        } else {
            double xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
            double yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
//            if(crosses.get(1)!=null) for(Cross c :crosses.get(1)) System.out.println(c.toString() + "\n" + c.getCluster1().toString());
            TracksFromTargetRec  trackFinder = new TracksFromTargetRec(xb, yb, swimmer, true, 0, this.getMass());
            List<Seed>   seeds = trackFinder.getSeeds(SVTclusters, BMTclusters, crosses);
            List<Track> tracks = trackFinder.getTracks(event);
            
            List<DataBank> banks = new ArrayList<>();
            banks.add(RecoBankWriter.fillSVTHitBank(event, SVThits, this.getSvtHitBank()));
            banks.add(RecoBankWriter.fillBMTHitBank(event, BMThits, this.getBmtHitBank()));
            banks.add(RecoBankWriter.fillSVTClusterBank(event, SVTclusters, this.getSvtClusterBank()));
            banks.add(RecoBankWriter.fillBMTClusterBank(event, BMTclusters, this.getBmtClusterBank()));
            banks.add(RecoBankWriter.fillSVTCrossBank(event, crosses.get(0), this.getSvtCrossBank()));
            banks.add(RecoBankWriter.fillBMTCrossBank(event, crosses.get(1), this.getBmtCrossBank()));
            if(seeds!=null) banks.add(RecoBankWriter.fillSeedBank(event, seeds, this.getSeedBank()));
            if(tracks!=null) {
                banks.add(RecoBankWriter.fillTrackBank(event, tracks, this.getTrackBank()));
//                banks.add(RecoBankWriter.fillTrackCovMatBank(event, tracks, this.getCovMat()));
                banks.add(RecoBankWriter.fillTrajectoryBank(event, tracks, this.getTrajectoryBank()));
            }
            event.appendBanks(banks.toArray(new DataBank[0]));
            
        }
        return true;
    }
     
}