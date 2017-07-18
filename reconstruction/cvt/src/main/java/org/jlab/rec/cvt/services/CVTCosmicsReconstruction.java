package org.jlab.rec.cvt.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cvt.banks.HitReader;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cluster.ClusterFinder;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
import org.jlab.rec.cvt.trajectory.TrkSwimmer;

/**
 * Service to return reconstructed BST track candidates- the output is in Evio
 * format
 *
 * @author ziegler
 *
 */
public class CVTCosmicsReconstruction extends ReconstructionEngine {

    org.jlab.rec.cvt.svt.Geometry SVTGeom;
    org.jlab.rec.cvt.bmt.Geometry BMTGeom;

    public CVTCosmicsReconstruction() {
        super("CVTTracks", "ziegler", "4.0");

        SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
        BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();

    }

    String FieldsConfig = "";
    int Run = -1;
    CVTRecConfig config;

    @Override
    public boolean processDataEvent(DataEvent event) {
        config.setRunConditionsParameters(event, FieldsConfig, Run, false, "");

        this.FieldsConfig = config.getFieldsConfig();
        this.Run = config.getRun();

        ADCConvertor adcConv = new ADCConvertor();

        RecoBankWriter rbc = new RecoBankWriter();

        HitReader hitRead = new HitReader();
        hitRead.fetch_SVTHits(event, adcConv, -1, -1, SVTGeom);
        hitRead.fetch_BMTHits(event, adcConv, BMTGeom);

        List<Hit> hits = new ArrayList<Hit>();
        //I) get the hits
        List<Hit> svt_hits = hitRead.get_SVTHits();
        if (svt_hits != null && svt_hits.size() > 0) {
            hits.addAll(svt_hits);
        }

        List<Hit> bmt_hits = hitRead.get_BMTHits();
        if (bmt_hits != null && bmt_hits.size() > 0) {
            hits.addAll(bmt_hits);
        }

        //II) process the hits		
        List<FittedHit> SVThits = new ArrayList<FittedHit>();
        List<FittedHit> BMThits = new ArrayList<FittedHit>();
        //1) exit if hit list is empty
        if (hits.size() == 0) {
            return true;
        }

        List<Cluster> clusters = new ArrayList<Cluster>();
        List<Cluster> SVTclusters = new ArrayList<Cluster>();
        List<Cluster> BMTclusters = new ArrayList<Cluster>();

        //2) find the clusters from these hits
        ClusterFinder clusFinder = new ClusterFinder();
        clusters = clusFinder.findClusters(hits);

        // test
        /*	for(Hit h0 : hits) {
			FittedHit h = new FittedHit(h0.get_Detector(), h0.get_DetectorType(), h0.get_Sector(),h0.get_Layer(),h0.get_Strip());
			Cluster c = new Cluster(h.get_Detector(), h.get_DetectorType(), h.get_Sector(),h.get_Layer(), 0); 					
			c.set_Id(0);
			// add hits to the cluster
			c.add(h);
			c.calc_CentroidParams();
			clusters.add(c);
		
		} */
        if (clusters.size() == 0) {
            return true;
        }

        // fill the fitted hits list.
        if (clusters.size() != 0) {
            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).get_Detector().equalsIgnoreCase("SVT")) {
                    SVTclusters.add(clusters.get(i));
                    SVThits.addAll(clusters.get(i));
                }
                if (clusters.get(i).get_Detector().equalsIgnoreCase("BMT")) {
                    BMTclusters.add(clusters.get(i));
                    BMThits.addAll(clusters.get(i));
                }
            }
        }

        List<ArrayList<Cross>> crosses = new ArrayList<ArrayList<Cross>>();
        CrossMaker crossMake = new CrossMaker();
        crosses = crossMake.findCrosses(clusters, SVTGeom);

        if (crosses.size() == 0) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, null, null);
            return true; //exiting
        }
        //4) make list of crosses consistent with a track candidate
        StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom,
                BMTGeom, 3);
        if (crosslist == null || crosslist.size() == 0) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);

            return true;
        }

        List<StraightTrack> cosmics = new ArrayList<StraightTrack>();

        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);
        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        if (cosmics.size() == 0) {
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null);

            return true;
        }

        if (cosmics.size() > 0) {
            for (int k1 = 0; k1 < cosmics.size(); k1++) {
                cosmics.get(k1).set_Id(k1 + 1);
                for (int k2 = 0; k2 < cosmics.get(k1).size(); k2++) {
                    cosmics.get(k1).get(k2).set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate crosses
                    if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
                        cosmics.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster1 in cross
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
                        cosmics.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster2 in cross	
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
                        for (int k3 = 0; k3 < cosmics.get(k1).get(k2).get_Cluster1().size(); k3++) { //associate hits
                            cosmics.get(k1).get(k2).get_Cluster1().get(k3).set_AssociatedTrackID(cosmics.get(k1).get_Id());
                        }
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
                        for (int k4 = 0; k4 < cosmics.get(k1).get(k2).get_Cluster2().size(); k4++) { //associate hits
                            cosmics.get(k1).get(k2).get_Cluster2().get(k4).set_AssociatedTrackID(cosmics.get(k1).get_Id());
                        }
                    }
                }
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), SVTGeom, BMTGeom, true,
                        cosmics.get(k1).get_Trajectory(), k1 + 1);
            }

            //4)  ---  write out the banks			
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics);
        }

        return true;

    }

    public boolean init() {

        TrkSwimmer.getMagneticFields();
        TrkSwimmer.setMagneticFieldScale(0.0);
        config = new CVTRecConfig();
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException, EvioException {

        //String inputFile = "/Users/ziegler/Workdir/Files/GEMC/CVT/cosmics_cvt_skim.hipo";
        String inputFile = "/Users/ziegler/Workdir/Files/GEMC/CVT/cvt_cosmics.1.hipo";
        //String inputFile = "/Users/ziegler/Workdir//Files/Data/DecodedData/CVT/474deco.hipo";
        System.err.println(" \n[PROCESSING FILE] : " + inputFile);

        CVTCosmicsReconstruction en = new CVTCosmicsReconstruction();
        en.init();

        int counter = 0;

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer
        String outputFile="/Users/ziegler/Workdir/Files/GEMC/CVT/cvt_cosmics.1rec.hipo";
        //String outputFile = "/Users/ziegler/Workdir//Files/Data/CosmicRun474Rec.hipo";
        writer.open(outputFile);

        long t1 = 0;
        while (reader.hasEvent()) {
            counter++;

            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }

            // Processing    
            en.processDataEvent(event);
            if(event.hasBank("CVTRec::Cosmics"))
                writer.writeEvent(event);

            System.out.println("  EVENT " + counter);
            /*
			 * event.show();
			if(event.hasBank("CVTRec::Tracks")) {
				 HipoDataEvent de = (HipoDataEvent) event;
				 HipoEvent dde = de.getHipoEvent();
				 HipoGroup group = dde.getGroup("CVTRec::Tracks");
				 dde.show();
				 dde.removeGroup("CVTRec::Tracks");
				 dde.show();
				 dde.writeGroup(group);
				 dde.show();
			}
             */
            //if (counter > 200) {
            //    break;
            //}
            //event.show();
            //if(counter%100==0)
            //System.out.println("run "+counter+" events");

        }
        writer.close();
        double t = System.currentTimeMillis() - t1;
        System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
    }

}
