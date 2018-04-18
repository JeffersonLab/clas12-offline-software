package org.jlab.service.dc;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossListFinder;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.segment.SegmentFinder;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.track.TrackCandListFinder;
import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.rec.dc.trajectory.RoadFinder;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;
import java.util.Arrays;
import java.util.Random;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;
import org.jlab.rec.dc.trajectory.Road;
import org.jlab.rec.dc.trajectory.TrackDictionaryMaker; //import org.jlab.rec.dc.track.fit.StateVecs.StateVec;
import org.jlab.rec.dc.trajectory.TrackDictionaryMaker.DCTDC;

public class TrackingEff extends ReconstructionEngine {

    private boolean skipEvent = false;

    public TrackingEff() {
            super("DCHB","ziegler","4.0");
    }

    String FieldsConfig="";
    int Run = 0;
    DCGeant4Factory dcDetector;
    TrackDictionaryMaker trMk;    
    double[][][][] T0 ;
    double[][][][] T0ERR ;
        
    double TORSCALE;
    double SOLSCALE;
    
	@Override
	public boolean init() {
            Constants.Load();
            // Load the Fields 
            DCSwimmer.getMagneticFields();
            String[]  dcTables = new String[]{
                "/calibration/dc/signal_generation/doca_resolution",
               // "/calibration/dc/time_to_distance/t2d",
                "/calibration/dc/time_to_distance/time2dist",
              //  "/calibration/dc/time_corrections/T0_correction",
            };

            requireConstants(Arrays.asList(dcTables));
            // Get the constants for the correct variation
            this.getConstantsManager().setVariation("default");
            
            // Load the geometry
            ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, "default");
            dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);
            
            //T0s
            T0 = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
            T0ERR = new double[6][6][7][6]; //nSec*nSL*nSlots*nCables
            DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(800, "default");
            dbprovider.loadTable("/calibration/dc/time_corrections/T0Corrections");
            //disconnect from database. Important to do this after loading tables.
            dbprovider.disconnect();
            // T0-subtraction
           
            for (int i = 0; i < dbprovider.length("/calibration/dc/time_corrections/T0Corrections/Sector"); i++) {
                int iSec = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Sector", i);
                int iSly = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Superlayer", i);
                int iSlot = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Slot", i);
                int iCab = dbprovider.getInteger("/calibration/dc/time_corrections/T0Corrections/Cable", i);
                double t0 = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Correction", i);
                double t0Error = dbprovider.getDouble("/calibration/dc/time_corrections/T0Corrections/T0Error", i);

                T0[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0; 
                T0ERR[iSec - 1][iSly - 1][iSlot - 1][iCab - 1] = t0Error;
            }
            //Constants.setLAYEREFFS(true);
            trMk = new TrackDictionaryMaker();
            tde = new TimeToDistanceEstimator();
            return true;
        }

	private TimeToDistanceEstimator tde;
       
	
	@Override
	public boolean processDataEvent(DataEvent event) {
            //setRunConditionsParameters( event) ;
            if(event.hasBank("RUN::config")==false ) {
                System.err.println("RUN CONDITIONS NOT READ!");
                return true;
            }
            double passedCand=0;
            double genCand=0;

            //double p = 2.5+6*Math.random();
            //double theta = 10.+30*Math.random();
            //double phi = 360*Math.random();
            //double vz =0;

            double p = 2.0;
            double theta = 32.0;
            double phi = 300.0;
            double vz = 0;
            //p=3.170735;
            //theta=35.868;
            //phi=-166.516;

            DataBank bank = event.getBank("RUN::config");

            // Load the constants
            //-------------------
            int newRun = bank.getInt("run", 0);

            if(Run!=newRun) {
                //CCDBTables.add(this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_corrections/T0_correction"));
                TORSCALE = (double)bank.getFloat("torus", 0);
                SOLSCALE = (double)bank.getFloat("solenoid", 0);
                double shift =0;
               // if(Run>1890)
               //     shift = -1.9;
                DCSwimmer.setMagneticFieldsScales(SOLSCALE, TORSCALE, shift);

                System.out.println(" Got the correct geometry "+dcDetector.getWireMidpoint(0, 0, 0));
                Run = newRun;
            }

            DCSwimmer sw = new DCSwimmer();
            TrackDictionaryMaker.DCTDC TDCSignalTrk = trMk.ProcessTrack(-1, 
                    p*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi)), 
                    p*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi)), 
                    p*Math.cos(Math.toRadians(theta)), 0., 0., vz, dcDetector, trMk, sw);

            // init SNR
            Clas12NoiseResult results = new Clas12NoiseResult();
            Clas12NoiseAnalysis noiseAnalysis = new Clas12NoiseAnalysis();

            int[] rightShifts = Constants.SNR_RIGHTSHIFTS;		
            int[] leftShifts  = Constants.SNR_LEFTSHIFTS;
            NoiseReductionParameters parameters = new NoiseReductionParameters (
                            2,leftShifts,
                            rightShifts);
            //System.out.println("RUNING HITBASED_________________________________________");

            ClusterFitter cf = new ClusterFitter();
            ClusterCleanerUtilities ct = new ClusterCleanerUtilities();

            List<FittedHit> fhits = new ArrayList<FittedHit>();
            List<FittedCluster> clusters = new ArrayList<FittedCluster>();
            List<Segment> segments = new ArrayList<Segment>();
            List<Cross> crosses = new ArrayList<Cross>();

            List<Track> trkcands = new ArrayList<Track>();
            List<Track> trkcandsTM = new ArrayList<Track>(); // truth matched
            //instantiate bank writer
            RecoBankWriter rbc = new RecoBankWriter();

            //if(Constants.DEBUGCROSSES)
            //	event.appendBank(rbc.fillR3CrossfromMCTrack(event));

            HitReader hitRead = new HitReader();
            //hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results, T0, T0ERR, this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/t2d"), dcDetector);
            hitRead.fetch_DCHits(event, noiseAnalysis, parameters, results, T0, T0ERR, 
                    this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), 
                    this.getConstantsManager().getConstants(newRun,"/calibration/dc/time_corrections/timingcuts"), dcDetector);

            List<Hit> hits = new ArrayList<Hit>();
            List<Hit> sighits = new ArrayList<Hit>();
            //I) get the hits
            List<Hit> allhits = hitRead.get_DCHits();

            // add signal hits

            // use only hits in same sector as fastMC track
            for(Hit h : allhits) {
                if(h.get_Sector()==TDCSignalTrk.sector.get(0))
                    hits.add(h);
            }
            Random rng = new Random();

            //// miss 1 layer out of every superlayer
            //double[]excl = new double[6];
            //for(int e = 0; e<6; e++) {
            //    excl[e]=1+rng.nextInt(6);
            //}
            //int exclsL = 1+rng.nextInt(6);
            boolean isAccepted = true;
            for(int ii=0; ii<TDCSignalTrk.sector.size(); ii++) {
                if(TDCSignalTrk.component.get(ii)>=1 && TDCSignalTrk.component.get(ii)<=112) {
                    Hit hit = new Hit(TDCSignalTrk.sector.get(ii), TDCSignalTrk.superlayer.get(ii), TDCSignalTrk.layer.get(ii), TDCSignalTrk.component.get(ii), 100, (ii + hits.size()));

                    hit.calc_CellSize(dcDetector);
                    double posError = hit.get_CellSize() / Math.sqrt(12.);
                    hit.set_DocaErr(posError);
                    hit.set_Id(ii + hits.size());

                    if(hit.get_Wire()!=0 ) {
                        sighits.add(hit);
                        //System.out.println(" FastMC "+hit.printInfo());
                        hits.add(hit);
                    }
                } else {
                    isAccepted=false;
                }
            }

            /*
            if(sighits.size()<36) {
                this.skipEvent=true;
            } else {
                this.skipEvent=false;    
            }
            //II) process the hits
            //1) exit if hit list is empty
            if(hits.size()==0 ) {
                    return true;
            }
            */
            if(isAccepted==false) {
                return true;
            } else {
                genCand++;
            }
            fhits = rbc.createRawHitList(hits);


            //2) find the clusters from these hits
            ClusterFinder clusFinder = new ClusterFinder();
            clusters = clusFinder.FindHitBasedClusters(hits, ct, cf, dcDetector);


            rbc.updateListsListWithClusterInfo(fhits, clusters);

            //3) find the segments from the fitted clusters
            SegmentFinder segFinder = new SegmentFinder();
            segments =  segFinder.get_Segments(clusters, event, dcDetector);

            List<Segment> rmSegs = new ArrayList<Segment>();
            // clean up hit-based segments
            for(Segment se : segments) {
                double trkDocOverCellSize =0;

                for(FittedHit fh : se.get_fittedCluster()) {

                    trkDocOverCellSize+=fh.get_ClusFitDoca()/fh.get_CellSize();
                }

                if(trkDocOverCellSize/(float)se.size()>1.1)
                    rmSegs.add(se);
            }
            segments.removeAll(rmSegs);
            //
               
            CrossMaker crossMake = new CrossMaker();
            crosses = crossMake.find_Crosses(segments, dcDetector);

            CrossListFinder crossLister = new CrossListFinder();
            CrossList crosslist = crossLister.candCrossLists(crosses, false, this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), dcDetector, null);

            //6) find the list of  track candidates
            TrackCandListFinder trkcandFinder = new TrackCandListFinder("HitBased");
            trkcands = trkcandFinder.getTrackCands(crosslist, dcDetector, TORSCALE) ;


  // track found	
        int trkId = 1;
                
        if(trkcands.size()>0) {
            trkcandFinder.removeOverlappingTracks(trkcands);		// remove overlaps

            for(Track trk: trkcands) {
                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, dcDetector);
                for(Cross c : trk) { 
                    c.get_Segment1().isOnTrack=true;
                    c.get_Segment2().isOnTrack=true;

                    for(FittedHit h1 : c.get_Segment1()) { 
                            h1.set_AssociatedHBTrackID(trk.get_Id());

                    }
                    for(FittedHit h2 : c.get_Segment2()) {
                            h2.set_AssociatedHBTrackID(trk.get_Id());                              
                    }
                }
                trkId++;
            }
        }    
        
        List<Segment> crossSegsNotOnTrack = new ArrayList<Segment>();
        List<Segment> psegments = new ArrayList<Segment>();
        for(Cross c : crosses) { 
            if(c.get_Segment1().isOnTrack==false)
                crossSegsNotOnTrack.add(c.get_Segment1());
            if(c.get_Segment2().isOnTrack==false)
                crossSegsNotOnTrack.add(c.get_Segment2());
        }
        
        RoadFinder rf = new RoadFinder();
        List<Road> allRoads =rf.findRoads(segments, dcDetector);
        
        for(Road r : allRoads) {
            List<Segment> Segs2Road = new ArrayList<Segment>(); 
            int missingSL = -1;
            for(int ri = 0; ri<3; ri++) { 
                if(r.get(ri).associatedCrossId==-1) {
                    if(r.get(ri).get_Superlayer()%2==1) {
                        missingSL = r.get(ri).get_Superlayer()+1;
                    } else {
                        missingSL = r.get(ri).get_Superlayer()-1;
                    }
                }
            }
            for(int ri = 0; ri<3; ri++) { 
                for(Segment s : crossSegsNotOnTrack) { 
                    if(s.get_Sector()==r.get(ri).get_Sector() && s.get_Region()==r.get(ri).get_Region() 
                            && s.associatedCrossId==r.get(ri).associatedCrossId && r.get(ri).associatedCrossId!=-1) {
                        if(s.get_Superlayer()%2==missingSL%2)
                            Segs2Road.add(s);
                    }
                }
            }

            if(Segs2Road.size()==2) {
                Segment pSegment = rf.findRoadMissingSegment(Segs2Road, dcDetector, r.a) ;
                psegments.add(pSegment);
            }
        }
        
        
        segments.addAll(psegments);

        List<Cross> pcrosses = crossMake.find_Crosses(segments, dcDetector);

        //
        CrossList pcrosslist = crossLister.candCrossLists(pcrosses, false, this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/time2dist"), dcDetector, null);

        List<Track> mistrkcands =trkcandFinder.getTrackCands(pcrosslist, dcDetector, TORSCALE);
        if(mistrkcands.size()>0) {    
            trkcandFinder.removeOverlappingTracks(mistrkcands);		// remove overlaps

            for(Track trk: mistrkcands) {

                // reset the id
                trk.set_Id(trkId);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, dcDetector);
                for(Cross c : trk) { 
                    for(FittedHit h1 : c.get_Segment1()) { 
                            h1.set_AssociatedHBTrackID(trk.get_Id());
                    }
                    for(FittedHit h2 : c.get_Segment2()) {
                            h2.set_AssociatedHBTrackID(trk.get_Id());                              
                    }
                }
                trkId++;
            }
        }
        trkcands.addAll(mistrkcands) ;

            // track found	
            boolean foundTrk = false;
            for(Track trk: trkcands) { 
                int trkNHOTS =0;
                for(Cross c : trk) { 
                        for(FittedHit h1 : c.get_Segment1())
                            if(h1.get_TDC()==100)
                                trkNHOTS++;
                        for(FittedHit h2 : c.get_Segment2())
                                if(h2.get_TDC()==100)
                                    trkNHOTS++;
                }

                if(trkNHOTS>=30) {
                    //passedCand++;
                    trkcandsTM.add(trk);
                }
                double px = trk.get_pAtOrig().x();
                double py = trk.get_pAtOrig().y();
                double pz = trk.get_pAtOrig().z();
                double rec_p = Math.sqrt(px*px+py*py+pz*pz);
                double rec_theta = Math.toDegrees(Math.acos(pz/rec_p));
                double rec_phi = Math.toDegrees(Math.atan2(py,px));

                if( trkNHOTS>=30 && (Math.abs(rec_p-p)/p)<0.50 && Math.abs(rec_theta-theta)<10 && Math.abs(rec_phi-phi)<10) {

                   foundTrk=true;
                   //System.out.println(passedCand+" gen P "+p+" Theta "+theta+" Phi "+phi 
                //+" rec P "+rec_p+" Theta "+rec_theta+" Phi "+rec_phi +" gen nb "+genCand);
                }
            }
            int trkIdm=1;
            for(Track trk: trkcandsTM) {		
                // reset the id
                trk.set_Id(trkIdm);
                trkcandFinder.matchHits(trk.get_Trajectory(), trk, dcDetector);
                for(Cross c : trk) { 
                    for(FittedHit h1 : c.get_Segment1()) {
                            h1.set_AssociatedHBTrackID(trk.get_Id());

                    }
                    for(FittedHit h2 : c.get_Segment2()) {
                            h2.set_AssociatedHBTrackID(trk.get_Id());                              
                    }
                }
                trkIdm++;
            }

            //rbc.fillAllHBBanks(event, rbc, fhits, clusters, segments, crosses, trkcandsTM);
            DataBank bankMC = event.createBank("MC::Particle", 1);
            bankMC.setInt("pid", 0, (int) 11);
            bankMC.setFloat("px", 0, (float) ((float) p*Math.sin(Math.toRadians(theta))*Math.cos(Math.toRadians(phi))));
            bankMC.setFloat("py", 0, (float) ((float) p*Math.sin(Math.toRadians(theta))*Math.sin(Math.toRadians(phi))));
            bankMC.setFloat("pz", 0, (float) ((float) p*Math.cos(Math.toRadians(theta))));
            bankMC.setFloat("vx", 0, (float) 0);
            bankMC.setFloat("vy", 0, (float) 0);
            bankMC.setFloat("vz", 0, (float) vz);
            bankMC.setFloat("vt", 0, (float) 0); 

            DataBank bankHits = event.createBank("HitBasedTrkg::HBHits", fhits.size()) ;
            for(int i = 0; i<fhits.size(); i++) {
                if(fhits.get(i).get_AssociatedClusterID()==-1 && fhits.get(i).get_ClusFitDoca()< fhits.get(i).get_CellSize()*1.1)
                    continue;
                bankHits.setShort("id", i, (short) fhits.get(i).get_Id());
                bankHits.setShort("status", i, (short) 0);
                bankHits.setByte("superlayer", i, (byte) fhits.get(i).get_Superlayer());
                bankHits.setByte("layer", i, (byte) fhits.get(i).get_Layer());
                bankHits.setByte("sector", i, (byte) fhits.get(i).get_Sector());
                bankHits.setShort("wire", i, (short) fhits.get(i).get_Wire());
                //bank.setFloat("time", i, (float) hitlist.get(i).get_Time());
                bankHits.setFloat("docaError", i, (float) fhits.get(i).get_DocaErr());
                bankHits.setFloat("trkDoca", i, (float) fhits.get(i).get_ClusFitDoca());
                bankHits.setFloat("LocX", i, (float) fhits.get(i).get_lX());
                bankHits.setFloat("LocY", i, (float) fhits.get(i).get_lY());
                bankHits.setFloat("X", i, (float) fhits.get(i).get_X());
                bankHits.setFloat("Z", i, (float) fhits.get(i).get_Z());
                bankHits.setByte("LR", i, (byte) fhits.get(i).get_LeftRightAmb());
                bankHits.setShort("clusterID", i, (short) fhits.get(i).get_AssociatedClusterID());
                bankHits.setByte("trkID", i, (byte) fhits.get(i).get_AssociatedHBTrackID());
                bankHits.setFloat("B", i, (float) fhits.get(i).getB());
            } 

            DataBank bankRec = event.createBank("HitBasedTrkg::HBTracks", trkcandsTM.size());
            for (int i = 0; i < trkcandsTM.size(); i++) {
                bankRec.setShort("id", i, (short) trkcandsTM.get(i).get_Id());
                bankRec.setByte("sector", i, (byte) trkcandsTM.get(i).get_Sector());
                bankRec.setByte("q", i, (byte) trkcandsTM.get(i).get_Q());
                //bank.setFloat("p", i, (float) candlist.get(i).get_P());
                bankRec.setFloat("c1_x", i, (float) trkcandsTM.get(i).get_PreRegion1CrossPoint().x());
                bankRec.setFloat("c1_y", i, (float) trkcandsTM.get(i).get_PreRegion1CrossPoint().y());
                bankRec.setFloat("c1_z", i, (float) trkcandsTM.get(i).get_PreRegion1CrossPoint().z());
                bankRec.setFloat("c1_ux", i, (float) trkcandsTM.get(i).get_PreRegion1CrossDir().x());
                bankRec.setFloat("c1_uy", i, (float) trkcandsTM.get(i).get_PreRegion1CrossDir().y());
                bankRec.setFloat("c1_uz", i, (float) trkcandsTM.get(i).get_PreRegion1CrossDir().z());
                bankRec.setFloat("c3_x", i, (float) trkcandsTM.get(i).get_PostRegion3CrossPoint().x());
                bankRec.setFloat("c3_y", i, (float) trkcandsTM.get(i).get_PostRegion3CrossPoint().y());
                bankRec.setFloat("c3_z", i, (float) trkcandsTM.get(i).get_PostRegion3CrossPoint().z());
                bankRec.setFloat("c3_ux", i, (float) trkcandsTM.get(i).get_PostRegion3CrossDir().x());
                bankRec.setFloat("c3_uy", i, (float) trkcandsTM.get(i).get_PostRegion3CrossDir().y());
                bankRec.setFloat("c3_uz", i, (float) trkcandsTM.get(i).get_PostRegion3CrossDir().z());
                bankRec.setFloat("t1_x", i, (float) trkcandsTM.get(i).get_Region1TrackX().x());
                bankRec.setFloat("t1_y", i, (float) trkcandsTM.get(i).get_Region1TrackX().y());
                bankRec.setFloat("t1_z", i, (float) trkcandsTM.get(i).get_Region1TrackX().z());
                bankRec.setFloat("t1_px", i, (float) trkcandsTM.get(i).get_Region1TrackP().x());
                bankRec.setFloat("t1_py", i, (float) trkcandsTM.get(i).get_Region1TrackP().y());
                bankRec.setFloat("t1_pz", i, (float) trkcandsTM.get(i).get_Region1TrackP().z());
                bankRec.setFloat("pathlength", i, (float) trkcandsTM.get(i).get_TotPathLen());
                bankRec.setFloat("Vtx0_x", i, (float) trkcandsTM.get(i).get_Vtx0().x());
                bankRec.setFloat("Vtx0_y", i, (float) trkcandsTM.get(i).get_Vtx0().y());
                bankRec.setFloat("Vtx0_z", i, (float) trkcandsTM.get(i).get_Vtx0().z());
                bankRec.setFloat("p0_x", i, (float) trkcandsTM.get(i).get_pAtOrig().x());
                bankRec.setFloat("p0_y", i, (float) trkcandsTM.get(i).get_pAtOrig().y());
                bankRec.setFloat("p0_z", i, (float) trkcandsTM.get(i).get_pAtOrig().z());
                bankRec.setShort("Cross1_ID", i, (short) trkcandsTM.get(i).get(0).get_Id());
                bankRec.setShort("Cross2_ID", i, (short) trkcandsTM.get(i).get(1).get_Id());
                bankRec.setShort("Cross3_ID", i, (short) trkcandsTM.get(i).get(2).get_Id());
                bank.setShort("status", i, (short) (100+trkcandsTM.get(i).get_FitConvergenceStatus()*10+trkcandsTM.get(i).get_MissingSuperlayer()));
                bankRec.setFloat("chi2", i, (float) trkcandsTM.get(i).get_FitChi2());
                bankRec.setShort("ndf", i, (short) trkcandsTM.get(i).get_FitNDF());
            }
            event.appendBanks(bankMC, bankHits, bankRec);


            //this.vz = -0.25+0.5*Math.random();
            return true;
	}

       
        
        
	public static void main(String[] args)  {
        String inputFile="/Users/ziegler/Desktop/Work/Files/Data/DecodedData/OldSchema/decoded_2326.hipo";
        TrackingEff en = new TrackingEff();
        en.init();
        DCTBEngine en2 = new DCTBEngine();
        en2.init();
        int counter = 0;
        int num  = 0;
        int gen =0;
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);

        HipoDataSync writer = new HipoDataSync();
        //Writer 
        String outputFile="/Users/ziegler/Desktop/Work/Files/Data/DecodedData/OldSchema/decoded_2326_rec.hipo";
        writer.open(outputFile);


        
        
        long t1 = 0;
        while (reader.hasEvent()) {
             //en.p = 4+1*Math.random();
             //en.theta = Math.toRadians(15.+5*Math.random());
             //en.phi = -10+20*Math.random();
            DataEvent event = reader.getNextEvent();
            if (counter > 0) {
                t1 = System.currentTimeMillis();
            }
            
            counter++;
            //en.processDataEvent(event);
            //if(event.getBank("RUN::config").getInt("event",0)>=1150 && event.getBank("RUN::config").getInt("event",0)<=1166) {
                en.processDataEvent(event);
                //if(event.hasBank("HitBasedTrkg::HBTracks")==false) {
                event.show();
                writer.writeEvent(event);
                //}
            //}
            //en2.processDataEvent(event);
            
            if (counter > 34) {
                break;
            }
            //event.show();
            //if(counter%100==0)
            //System.out.println("*************************************************************run " + counter + " events");
         /*   if(event.hasBank("HitBasedTrkg::HBTracks")) {
                DataBank bank = event.getBank("HitBasedTrkg::HBTracks");
                
                for(int i = 0; i< bank.rows(); i++) {
                    double px = bank.getFloat("p0_x", i);
                    double py = bank.getFloat("p0_y", i);
                    double pz = bank.getFloat("p0_z", i);
                    double rec_p = Math.sqrt(px*px+py*py+pz*pz);
                    double rec_theta = Math.toDegrees(Math.acos(pz/rec_p));
                    double rec_phi = Math.toDegrees(Math.atan2(py,px));
                    
                    //if( (Math.abs(rec_p-en.p)/en.p)<0.25 && Math.abs(rec_theta-(en.theta))<2 && Math.abs(rec_phi-(en.phi))<10)
                    //   num++;     
                    //System.out.println("rec P "+rec_p+" Theta "+rec_theta+" Phi "+rec_phi);
                }
               // event.show();
                writer.writeEvent(event);
            } */
           // event.show();
            if(event.hasBank("MC::Particle")==true) {
                  
                  gen++;
                  if(event.hasBank("HitBasedTrkg::HBTracks")==true)
                      num++;
                  if(counter%10==0)
                  System.out.println(gen+" eff "+(float)num*100./(float)gen);
            }
            
            //TimeToDistanceEstimator tbl = new TimeToDistanceEstimator();
            //System.out.println(maxBinIdxT[1][0][0]+" "+maxBinIdxT[1][0][5]+" "+DISTFROMTIME[1][0][0][maxBinIdxT[1][0][0]]+ " "+DISTFROMTIME[1][0][5][maxBinIdxT[1][0][5]]);
            
              //  for(int a =0; a<1; a++)
              //      for(int t=0; t<12; t++)
              //      System.out.println("T2D .......B "+2.5+ " time " +t*100+" alpha "+(float)a*6.0+" : "+tbl.interpolateOnGrid((double)2.5, Math.toRadians((float)a*6.0), (float)100*t, 0, 2) );
      //  }
        }
       writer.close();
       // double t = System.currentTimeMillis() - t1;
       // System.out.println(t1 + " TOTAL  PROCESSING TIME = " + (t / (float) counter));
       // } 
        
        }
}
