/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKEngine extends ReconstructionEngine {

	public FTTRKEngine() {
		super("FTTRK", "devita", "1.0");
	}

	FTTRKReconstruction reco;
	
	@Override
	public boolean init() {
            
            FTTRKConstantsLoader.Load();
            
		reco = new FTTRKReconstruction();
		reco.debugMode=0;

            String[]  tables = new String[]{ 
                "/calibration/ft/fthodo/charge_to_energy",
                "/calibration/ft/fthodo/time_offsets",
                "/calibration/ft/fthodo/status",
                "/geometry/ft/fthodo"
            };
            requireConstants(Arrays.asList(tables));
            this.getConstantsManager().setVariation("default");

            return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            List<FTTRKHit> allHits      = new ArrayList();
            List<FTTRKHit> selectedHits = new ArrayList();
            List<FTTRKCluster> clusters = new ArrayList();
            List<FTTRKCross>   crosses  = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTTRK(event,this.getConstantsManager(), run);
//                // select good hits and order them by energy
//                selectedHits = reco.selectHits(allHits); 
                // create clusters
                clusters = reco.findClusters(allHits);
                // create crosses
                crosses = reco.findCrosses(clusters);
                // write output banks
                reco.writeBanks(event, allHits, clusters, crosses);
            }
            return true;
	}
        
        public ArrayList<FTTRKCluster> processDataEventAndGetClusters(DataEvent event) {
            List<FTTRKHit> allHits      = new ArrayList();
            List<FTTRKHit> selectedHits = new ArrayList();
            ArrayList<FTTRKCluster> clusters = new ArrayList();
            List<FTTRKCross>   crosses  = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTTRK(event,this.getConstantsManager(), run);
//                // select good hits and order them by energy
//                selectedHits = reco.selectHits(allHits); 
                // create clusters
                clusters = reco.findClusters(allHits);
                // create crosses
                crosses = reco.findCrosses(clusters);
                // write output banks
                reco.writeBanks(event, allHits, clusters, crosses);
            }
            return clusters;
	}      

    public int setRunConditionsParameters(DataEvent event) {
        int run = -1;
        if(event.hasBank("RUN::config")==false) {
                System.out.println("RUN CONDITIONS NOT READ!");
        }       
    
        DataBank bank = event.getBank("RUN::config");
            run = bank.getInt("run")[0];
        
	return run;	
    }

    
    public static void main (String arg[]) {
        int debug = FTTRKReconstruction.debugMode;
	FTTRKEngine trk = new FTTRKEngine();
	trk.init();
//	String input =  "/disk2/Clas/ForwardTrackerReconstruction/DATA/out.hipo";
//        String input =  "/disk2/Clas/ForwardTrackerReconstruction/DATA/out_allTracks.hipo";
        String input =  "/Users/devita/Work/clas12/simulations/clas12Tags/4.3.1/out.hipo";
//        String input =  "/home/filippi/clas/ForwardTracker/out_allTracks.hipo";
//        String input =  "/home/filippi/clas/ForwardTracker/out.hipo";
	HipoDataSource  reader = new HipoDataSource();
	reader.open(input);
		
	// initialize histos
        H2F h1 = new H2F("h1", "Layer vs. Component", 768, 0., 769., 4, 0.5, 4.5);
        h1.setTitleX("Component");
        h1.setTitleY("Layer");
        H1F h2 = new H1F("Energy",100, 0, 100);         
        h2.setOptStat(Integer.parseInt("1111")); h2.setTitleX("Energy"); h2.setTitleY("Counts");
        H1F h3 = new H1F("Time",100, -2, 2);         
        h3.setOptStat(Integer.parseInt("1111")); h3.setTitleX("Time"); h3.setTitleY("Counts");

        float lim = 15;
        H2F hHitL1 = new H2F("hHitL1","cross y vs x detector 1", 100, -lim, lim, 100, -lim, lim);
        H2F hHitL2 = new H2F("hHitL2","cross y vs x detector 2", 100, -lim, lim, 100, -lim, lim);
        H2F hHitMatch = new H2F("hHitL2","cross y vs x match", 100, -lim, lim, 100, -lim, lim);
//        H2F hHitL3 = new H2F("hHitL3","hit y vs x layer 3", 100, -13., 13., 100, -13., 13.);
//        H2F hHitL4 = new H2F("hHitL4","hit y vs x layer 4", 100, -13., 13., 100, -13., 13.);
        hHitL1.setTitleX("x cross detector 1"); hHitL1.setTitleY("y cross detector 1");
        hHitL2.setTitleX("x cross detector 2"); hHitL2.setTitleY("y cross detector 2");
        hHitMatch.setTitleX("x cross detector match"); hHitMatch.setTitleY("y cross detector match");                
        DataLine segment[] = new DataLine[FTTRKConstantsLoader.Nstrips*4];

        JFrame frameClusters = new JFrame("FT strips in clusters");
        frameClusters.setSize(800,800);
        EmbeddedCanvas canvasCl = new EmbeddedCanvas();
        JFrame frameClustersSingleLay = new JFrame("FT strips in clusters single layers");
        frameClustersSingleLay.setSize(800,800);
        EmbeddedCanvas canvasClSingleLay = new EmbeddedCanvas();
        canvasClSingleLay.divide(2,2);
        
//        hHitL3.setTitleX("x hit layer3"); hHitL1.setTitleY("y hit layer 3");
//        hHitL4.setTitleX("x hit layer4"); hHitL1.setTitleY("y hit layer 4");
        
        int nc1 = 0, nc2 = 0, ncmatch = 0;   
        while(reader.hasEvent()){
//        int nev1 = 0; int nev2 = nev1+1; for(int nev=nev1; nev<nev2; nev++){   // 1 event
//        int nev1 = 1; int nev2 = nev1+1; for(int nev=nev1; nev<50; nev++){    
//        int nev1 = 1; int nev2 = nev1+500; for(int nev=nev1; nev<nev2; nev++){  
            DataEvent event = (DataEvent) reader.getNextEvent();
//            if(nev!=49) continue;
            
            ArrayList<FTTRKCluster> clusters = new ArrayList();
            clusters = trk.processDataEventAndGetClusters(event);
            int nStripsInClusters = 0;

            DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
            PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
            if(event.hasBank("FTTRK::hits")) {
                DataBank bank = event.getBank("FTTRK::hits");
                int nrows = bank.rows();
                for(int i=0; i<nrows;i++) {
                    int layer  = bank.getByte("layer",i);
                    int comp   = bank.getShort("component",i);
                    float energy = bank.getFloat("energy",i);
                    float time   = bank.getFloat("time",i);
                    
                    if(debug>=1) System.out.println("layer " + layer + " strip " + comp);
                    h1.fill(comp,layer);
                    h2.fill(energy);
                    h3.fill(time);
            	}
            }            
          
            
            // iterate along the cluster list for every event
            if(debug>=1) System.out.println("clusters size --- " + clusters.size());
 //           DataLine segment[] = new DataLine[clusters.size()];
            if(clusters.size()!=0){
                // get one cluster and iterate over all the strips contained in it
                canvasCl.cd(1); canvasCl.draw(hHitL1);
                for(int l=0; l<4; l++){
                    canvasClSingleLay.cd(l); 
                    if(l==2 || l==3){canvasClSingleLay.draw(hHitL2);}else{canvasClSingleLay.draw(hHitL1);}
                }
                for(int i = 0; i < clusters.size(); i++){
                    // get a single cluster and count its strip, extract the information on extremal points of the segment
                    FTTRKCluster singleCluster = clusters.get(i);
                    int nst = singleCluster.size();
                    if(debug>=1) System.out.println("nst - " + nst);
                    for(int j=0; j<nst; j++){
                        Line3D seg = singleCluster.get(j).get_StripSegment();
                        if(debug>=1) System.out.println("total number of clusters " + clusters.size() + " - number of cluster " + i + " cluster size " + 
                                singleCluster.size() + " strip# " + j + " clusterId " + singleCluster.get_Id() + 
                                " layer " + singleCluster.get_Layer() + " seed strip number " + singleCluster.get_SeedStrip() + 
                                " segment -------------- " + seg.origin().x() + " " + seg.origin().y() + " " + seg.end().x() + " " + seg.end().y());
                        segment[nStripsInClusters] = new DataLine(seg.origin().x(), seg.origin().y(), seg.end().x(), seg.end().y());
                        int lay = singleCluster.get_Layer();
                        segment[nStripsInClusters].setLineColor(lay);
 //                       canvasCl.cd(1); canvasCl.draw(hHitL1);
                        if(debug>=1) System.out.println("nStripsInCluster " + nStripsInClusters);
                        canvasCl.draw(segment[nStripsInClusters]);
                        canvasClSingleLay.cd(lay-1); canvasClSingleLay.draw(segment[nStripsInClusters]);
                        
                        nStripsInClusters++;
                    }
                }
            }

        
            if(debug>=1) System.out.println("is there the crosses bank? " + event.hasBank("FTTRK::crosses"));
            if(event.hasBank("FTTRK::crosses")){
                DataBank crossBank = event.getBank("FTTRK::crosses");
                int nrows = crossBank.rows();
                float x[];
                float y[];
                byte det[];
                x = new float[nrows];
                y = new float[nrows];
                det = new byte[nrows];
                for(int i=0; i<nrows; i++){
                    det[i] = crossBank.getByte("detector",i);
                    x[i] = crossBank.getFloat("x", i);
                    y[i] = crossBank.getFloat("y", i);
                    if(debug>=2) System.out.println("number of crosses " + nrows + " detector " + det[i] + " x " + x[i] + " y " + y[i]);
                    
                    if(det[i]==1) {hHitL1.fill(x[i], y[i]); nc1++;}
                    if(det[i]==2) {hHitL2.fill(x[i], y[i]); nc2++;}
                }
                /// loop on all crosses on detector 1 and 2 and find the oone with better matching
                double minDistance = 1000;
                int iBest = -1, jBest = -1;
                if(debug>=1) System.out.println("number of rows " + nrows);
                if(nrows>1){
                    for(int i=0; i<nrows; i++){
                        for(int j=nrows-1; j>i; j--){
                            if(det[i]!=det[j]){
                                double distance = Math.sqrt((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]));
                                if(distance < minDistance){
                                    minDistance = distance;
                                    iBest = i; jBest = j;
                                }
                            }
                        }
                    }
                    if(debug>=1) System.out.println("minimum distance " + minDistance);
                    double distTolerance = 0.1;
                    distTolerance = 1000;
                    if(minDistance < distTolerance) {
                            hHitMatch.fill((x[iBest]+x[jBest])/2., (y[iBest]+y[jBest])/2.);
                            ncmatch++;
                    } 
                }
             }    
        }
    
        
        if(debug>=1) System.out.println("number of found crosses: module 1: " + nc1 + " module 2: " + nc2 + " matching crosses " + ncmatch);
        
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(1,3);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); 
        
        JFrame frame2 = new JFrame("FT crosses coordinates per module");
        frame2.setSize(800,800);
        EmbeddedCanvas canvas2 = new EmbeddedCanvas();
        canvas2.divide(2,2);
        canvas2.cd(0); canvas2.draw(hHitL1);
        canvas2.cd(1); canvas2.draw(hHitL2);
        canvas2.cd(2); canvas2.draw(hHitMatch);      
        frame2.add(canvas2);
        frame2.setLocationRelativeTo(null);
        frame2.setVisible(true); 

        frameClusters.add(canvasCl);    
        frameClusters.setLocationRelativeTo(null);
        frameClusters.setVisible(true);

        frameClustersSingleLay.add(canvasClSingleLay);    
        frameClustersSingleLay.setLocationRelativeTo(null);
        frameClustersSingleLay.setVisible(true);
        
        
    }
}

        
    
