/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorLayer;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.ft.FTConstants;
import org.jlab.rec.ft.trk.FTTRKEngine;
import org.jlab.rec.ft.trk.FTTRKConstantsLoader;
import org.jlab.rec.ft.trk.FTTRKReconstruction;
import org.jlab.rec.ft.trk.FTTRKCross;
import org.jlab.rec.ft.trk.FTTRKCluster;
import org.jlab.rec.ft.trk.FTTRKHit;


/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKEngineTest extends ReconstructionEngine {

	public FTTRKEngineTest() {
		super("FTTRK", "devita", "1.0");
	}

	FTTRKReconstruction reco;
	
	@Override
	public boolean init() {

            String[]  tables = new String[]{ 
                "/calibration/ft/fthodo/charge_to_energy",
                "/calibration/ft/fthodo/time_offsets",
                "/calibration/ft/fthodo/status",
                "/geometry/ft/fthodo",
                "/geometry/ft/fttrk"
            };
            requireConstants(Arrays.asList(tables));
            this.getConstantsManager().setVariation("default");
            
            // use 11 provisionally as run number to download the basic FTTK geometry constants
            FTTRKConstantsLoader.Load(11, this.getConstantsManager().getVariation());
            reco = new FTTRKReconstruction();
	    reco.debugMode=0;       
            
            return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
            List<FTTRKHit> allHits      = new ArrayList();
            ArrayList<FTTRKCluster> clusters = new ArrayList();
            ArrayList<FTTRKCross>   crosses  = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits fron banks
                allHits = reco.initFTTRK(event,this.getConstantsManager(), run); 
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
            ArrayList<FTTRKCluster> clusters = new ArrayList();   // era ArrayList
            ArrayList<FTTRKCross>   crosses  = new ArrayList();
            
            // update calibration constants based on run number if changed
            int run = setRunConditionsParameters(event);
            
            if(run>=0) {
                // get hits from banks
                allHits = reco.initFTTRK(event,this.getConstantsManager(), run);
                if(allHits.size()>0){
                    // create clusters
                    clusters = reco.findClusters(allHits);
                    // create crosses
                    crosses = reco.findCrosses(clusters);
                    // update hit banks with associated clusters/crosses information
                    reco.updateAllHitsWithAssociatedIDs(allHits, clusters);
                    // write output banks
                    reco.writeBanks(event, allHits, clusters, crosses);
                }
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
        // insert input filename here
        String input = "/home/filippi/clas12/fttrkDev/clas12-offline-software-6.5.13-fttrkDev/filter_005418_newbanks.hipo";
//        String input = "/home/filippi/clas12/coatjava-devel/clas12-offline-software_v7.0.0/oneHit_126798.hipo";  // multihit evt
        System.out.println("input file " + input);
	HipoDataSource  reader = new HipoDataSource();
	reader.open(input);
		
	// initialize histos
        H2F h1 = new H2F("h1", "Layer vs. Component", 768, 0., 769., 4, 0.5, 4.5);
        h1.setTitleX("Component");
        h1.setTitleY("Layer");
        H1F h2 = new H1F("Energy",100, 0, 1000.);         
        h2.setOptStat(Integer.parseInt("1111")); h2.setTitleX("Energy"); h2.setTitleY("Counts");
        H1F h3 = new H1F("Time",100, 0., 500.); // was 1e-3
        h3.setOptStat(Integer.parseInt("1111")); h3.setTitleX("Time"); h3.setTitleY("Counts");
        H2F h333 = new H2F("energy vs time",100, 0., 410., 100, 0., 410.); 
        h333.setTitleX("strip Time"); h333.setTitleY("strip Energy");
        H2F h444 = new H2F("strip vs time",100, 0., 500., 768, 0., 769.); 
        h444.setTitleX("strip Time"); h444.setTitleY("strip number");
        H2F h445 = new H2F("strip lay 1 vs time",100, 0., 500., 768, 0., 769.); 
        h445.setTitleX("strip Time"); h445.setTitleY("strip number");
        H2F h446 = new H2F("strip lay 2 vs time",100, 0., 500., 768, 0., 769.); 
        h446.setTitleX("strip Time"); h446.setTitleY("strip number");
        H2F h447 = new H2F("strip lay 3 vs time",100, 0., 500., 768, 0., 769.); 
        h447.setTitleX("strip Time"); h447.setTitleY("strip number");
        H2F h448 = new H2F("strip lay 4 vs time",100, 0., 500., 768, 0., 769.); 
        h448.setTitleX("strip Time"); h448.setTitleY("strip number");
        H1F h1clEn = new H1F("Cluster energy", 100, 0., 1000.);
        h1clEn.setOptStat(Integer.parseInt("1111")); h1clEn.setTitleX("centroid energy of clusters"); 
        H1F hOccupancy1 = new H1F("hOccupancy1", 768, 0., 769.); hOccupancy1.setTitleX("Component layer 1"); 
        hOccupancy1.setLineColor(1); hOccupancy1.setFillColor(1); hOccupancy1.setOptStat(11);
        H1F hOccupancy2 = new H1F("hOccupancy2", 768, 0., 769.); hOccupancy2.setTitleX("Component layer 2"); 
        hOccupancy2.setLineColor(1); hOccupancy2.setFillColor(2); hOccupancy2.setOptStat(11);
        H1F hOccupancy3 = new H1F("hOccupancy3", 768, 0., 769.); hOccupancy3.setTitleX("Component layer 3"); 
        hOccupancy3.setLineColor(1); hOccupancy3.setFillColor(3); hOccupancy3.setOptStat(11);
        H1F hOccupancy4 = new H1F("hOccupancy4", 768, 0., 769.); hOccupancy4.setTitleX("Component layer 4"); 
        hOccupancy4.setLineColor(1); hOccupancy4.setFillColor(4); hOccupancy4.setOptStat(11);
        
        H1F hStripDiff1 = new H1F("hStripDiff1", 10, -5.5, 4.5); hStripDiff1.setTitleX("Strip difference layer 1"); 
        hStripDiff1.setOptStat(Integer.parseInt("1111")); hStripDiff1.setLineColor(1); hStripDiff1.setFillColor(1);
        H1F hStripDiff2 = new H1F("hStripDiff2", 10, -5.5, 4.5); hStripDiff2.setTitleX("Strip difference layer 2"); 
        hStripDiff2.setOptStat(Integer.parseInt("1111")); hStripDiff2.setLineColor(1); hStripDiff2.setFillColor(2);
        H1F hStripDiff3 = new H1F("hStripDiff3", 10, -5.5, 4.5); hStripDiff3.setTitleX("Strip difference layer 3"); 
        hStripDiff3.setOptStat(Integer.parseInt("1111")); hStripDiff3.setLineColor(1); hStripDiff3.setFillColor(3);
        H1F hStripDiff4 = new H1F("hStripDiff4", 10, -5.5, 4.5); hStripDiff4.setTitleX("Strip difference layer 4"); 
        hStripDiff4.setOptStat(Integer.parseInt("1111")); hStripDiff4.setLineColor(1); hStripDiff4.setFillColor(4);
        H1F hNumberOfStrips = new H1F("hNumberOfStrips", 10, -0.5, 9.5); hNumberOfStrips.setTitleX("number of strips per cluster"); 
        hNumberOfStrips.setOptStat(Integer.parseInt("1111")); hNumberOfStrips.setLineColor(1); hNumberOfStrips.setFillColor(5);
        H1F hNumberOfStripsInCrosses = new H1F("hNumberOfStripsInCrosses", 10, -0.5, 9.5); hNumberOfStripsInCrosses.setTitleX("number of strips per cluster in crosses"); 
        hNumberOfStripsInCrosses.setOptStat(Integer.parseInt("1111")); hNumberOfStripsInCrosses.setLineColor(1); hNumberOfStripsInCrosses.setFillColor(6);
        
        H1F hStripLay1 = new H1F("hStripLay1", 10, -0.5, 9.5); hStripLay1.setTitleX("Number of strips in clusters, layer 1"); 
        hStripLay1.setOptStat(Integer.parseInt("1111")); hStripLay1.setLineColor(1); hStripLay1.setFillColor(1);
        H1F hStripLay2 = new H1F("hStripLay2", 10, -0.5, 9.5); hStripLay2.setTitleX("Number of strips in clusters, layer 2"); 
        hStripLay2.setOptStat(Integer.parseInt("1111")); hStripLay2.setLineColor(1); hStripLay2.setFillColor(2);
        H1F hStripLay3 = new H1F("hStripLay3", 10, -0.5, 9.5); hStripLay3.setTitleX("Number of strips in clusters, layer 3"); 
        hStripLay3.setOptStat(Integer.parseInt("1111")); hStripLay3.setLineColor(1); hStripLay3.setFillColor(3);
        H1F hStripLay4 = new H1F("hStripLay4", 10, -0.5, 9.5); hStripLay4.setTitleX("Number of strips in clusters, layer 4"); 
        hStripLay4.setOptStat(Integer.parseInt("1111")); hStripLay4.setLineColor(1); hStripLay4.setFillColor(4);
        
        float lim = 15;
        H2F hHitL1 = new H2F("hHitL1","cross y vs x detector 1", 100, -lim, lim, 100, -lim, lim);
        H2F hHitL2 = new H2F("hHitL2","cross y vs x detector 2", 100, -lim, lim, 100, -lim, lim);
        H2F hHitMatch = new H2F("hHitL2","cross y vs x match", 100, -lim, lim, 100, -lim, lim);
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
        
        int nc1 = 0, nc2 = 0, ncmatch = 0;
        int nev=0;
        int TRK1 = DetectorLayer.FTTRK_MODULE1 - 1; 
        int TRK2 = DetectorLayer.FTTRK_MODULE2 - 1;
        while(reader.hasEvent()){
//        int nev1 = 0; int nev2 = 30000; for(nev=nev1; nev<nev2; nev++){   // debug only a set of events (uncomment while loop in case)
            DataEvent event = (DataEvent) reader.getNextEvent();
            if(debug>=1) System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ processing event ~~~~~~~~~~~ " + nev); 
//            if(nev != 8) continue;    // select one event only for debugging purposes
            
            ArrayList<FTTRKCluster> clusters = new ArrayList();
            clusters = trk.processDataEventAndGetClusters(event);
            int nStripsInClusters = 0;

            DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
            PhysicsEvent gen = detectorEvent.getGeneratedEvent();
            double maxcomp1=-100, maxcomp2=-100, maxcomp3=-100, maxcomp4=-100;
            int imax1 = -1, imax2 = -1, imax3 = -1, imax4 = -1;
            if(event.hasBank("FTTRK::hits")) {
                DataBank bank = event.getBank("FTTRK::hits");
                int nrows = bank.rows();
                for(int i=0; i<nrows;i++) {
                    int layer  = bank.getByte("layer",i);
                    int comp   = bank.getShort("component",i);
                    float energy = bank.getFloat("energy",i);
                    float time   = bank.getFloat("time",i);
                    
                    if(debug>=1) {
                        System.out.println("layer " + layer + " strip " + comp);
                        System.out.println("%%%%%%%%% layer " + layer + " strip " + comp + " sector " + FTTRKReconstruction.findSector(comp));
                    }
                    h1.fill(comp,layer);
                    h2.fill(energy);
                    h3.fill(time);
                    h333.fill(time, energy);
                    
                    h444.fill(time, comp);
                    if(layer==DetectorLayer.FTTRK_LAYER1){
                       h445.fill(time, comp);    
                    }else if(layer==DetectorLayer.FTTRK_LAYER2){
                       h446.fill(time, comp);
                    }else if(layer==DetectorLayer.FTTRK_LAYER3){
                       h447.fill(time, comp); 
                    }else if(layer==DetectorLayer.FTTRK_LAYER4){
                       h448.fill(time, comp); 
                    }
                    
                    if(layer==DetectorLayer.FTTRK_LAYER1){
                        if(debug>=1) System.out.println("component layer 1 " + comp + " event number " + nev + " n rows " + nrows);
                        hOccupancy1.fill(comp);
                        if(energy>maxcomp1){
                            maxcomp1 = energy;
                            imax1 = i;           
                        }
                    }else if(layer==DetectorLayer.FTTRK_LAYER2){
                        hOccupancy2.fill(comp);
                        if(energy>maxcomp2){
                            maxcomp2 = energy;
                            imax2 = i;           
                        }
                    }else if(layer==DetectorLayer.FTTRK_LAYER3){
                        hOccupancy3.fill(comp);
                        if(energy>maxcomp3){
                            maxcomp3 = energy;
                            imax3 = i;           
                        }
                    }else if(layer==DetectorLayer.FTTRK_LAYER4){
                        hOccupancy4.fill(comp);
                        if(energy>maxcomp4){
                            maxcomp4 = energy;
                            imax4 = i;           
                        }
                    }
            	}
                // strip number correposponding to the max energy release
                maxcomp1 = bank.getShort("component", imax1);
                maxcomp2 = bank.getShort("component", imax2);
                maxcomp3 = bank.getShort("component", imax3);
                maxcomp4 = bank.getShort("component", imax4);
            }            
          
            
            // iterate along the cluster list for every event
            if(debug>=1) System.out.println("clusters size --- " + clusters.size());
            if(clusters.size()!=0){
                // get one cluster and iterate over all the strips contained in it
                // draw dummy histograms to be superimposed with strips
                canvasCl.cd(1); canvasCl.draw(hHitMatch);
                for(int l=0; l<FTTRKConstantsLoader.Nlayers; l++){
                    canvasClSingleLay.cd(l); 
                    if(l==DetectorLayer.FTTRK_LAYER2 || l==DetectorLayer.FTTRK_LAYER3){canvasClSingleLay.draw(hHitL2);}else{canvasClSingleLay.draw(hHitL1);}
                }
                
                for(int i = 0; i < clusters.size(); i++){
                    // get a single cluster and count its strip, extract the information on extremal points of the segment
                    FTTRKCluster singleCluster = clusters.get(i);
                    if(singleCluster.get_AssociatedCrossID() >-1) hNumberOfStripsInCrosses.fill(singleCluster.size());
                    if(singleCluster.get_Layer()==DetectorLayer.FTTRK_LAYER1){
                        hStripDiff1.fill(maxcomp1 - singleCluster.get_Centroid());
                        hStripLay1.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==DetectorLayer.FTTRK_LAYER2){
                        hStripDiff2.fill(maxcomp2 - singleCluster.get_Centroid());
                        hStripLay2.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==DetectorLayer.FTTRK_LAYER3){
                        hStripDiff3.fill(maxcomp3 - singleCluster.get_Centroid());
                        hStripLay3.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==DetectorLayer.FTTRK_LAYER4){
                        hStripDiff4.fill(maxcomp4 - singleCluster.get_Centroid());
                        hStripLay4.fill(singleCluster.size());
                    }
                    int nst = singleCluster.size();
                    if(debug>=1) System.out.println("nst - " + nst);
                    for(int j=0; j<nst; j++){
                        if(singleCluster.get_TotalEnergy() < FTConstants.TRK_MIN_CLUS_ENERGY) continue;
                        Line3D seg = singleCluster.get(j).get_StripSegment();
                        if(debug>=1) System.out.println("total number of clusters " + clusters.size() + " - number of cluster " + i + " cluster size " + 
                                singleCluster.size() + " strip# " + j + " clusterId " + singleCluster.get_CId() + 
                                " layer " + singleCluster.get_Layer() + " strip number " + singleCluster.get(j).get_Strip() + 
                                " segment " + seg.origin().x() + " " + seg.origin().y() + " " + seg.end().x() + " " + seg.end().y() + 
                                " total mean energy of the cluster " + singleCluster.get_TotalEnergy());
                        segment[nStripsInClusters] = new DataLine(seg.origin().x(), seg.origin().y(), seg.end().x(), seg.end().y());
                        int lay = singleCluster.get_Layer();
                        segment[nStripsInClusters].setLineColor(lay);
                        if(debug>=1) System.out.println("nStripsInCluster " + nStripsInClusters);
                        canvasCl.draw(segment[nStripsInClusters]);
                        h1clEn.fill(singleCluster.get_TotalEnergy());
                        hNumberOfStrips.fill(singleCluster.size());
                        if(debug>=1) System.out.println("%%%%% drawn segment " + singleCluster.get(j).get_Strip() + " cluster " + singleCluster.get_CId() + " layer " + singleCluster.get_Layer());
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
                float z[];
                byte det[];
                x = new float[nrows];
                y = new float[nrows];
                z = new float[nrows];
                det = new byte[nrows];
                for(int i=0; i<nrows; i++){
                    det[i] = crossBank.getByte("detector",i);
                    x[i] = crossBank.getFloat("x", i);
                    y[i] = crossBank.getFloat("y", i);
                    z[i] = crossBank.getFloat("z", i);
                    if(debug>=1) System.out.println("number of crosses " + nrows + " detector " + det[i] + " x " + x[i] + " y " + y[i]);
                    
                    if(det[i]==TRK1) {hHitL1.fill(x[i], y[i]); nc1++;}
                    if(det[i]==TRK2) {hHitL2.fill(x[i], y[i]); nc2++;}
                }
                // loop on all crosses on detector 1 and 2 and find the one with better matching
                double minDistance = 1000;
                double minDiffPhi = 1000.;
                int iBest = -1, jBest = -1;
                if(debug>=1) System.out.println("number of rows " + nrows);
                if(nrows>=1){
                    double diffPhi = 1000;
                    for(int i=0; i<nrows; i++){
                        for(int j=nrows-1; j>i; j--){
                            if(det[i]!=det[j]){
                                double distance = Math.sqrt((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]));
                                // check det1 and det2 correspondence by comparing phi angles
                                // the radius is larger for the second detector (as it is more distant)
                                double phiCross1 = Math.atan2(y[i],x[i]);
                                double phiCross2 = Math.atan2(y[j],x[j]);
                                diffPhi = Math.abs(phiCross1-phiCross2);
                                if(Math.abs(phiCross1-phiCross2)<minDiffPhi){
                                    minDiffPhi = diffPhi;
                                    iBest = i; jBest = j;
                                }   
                            }
                        }
                    }
                    if(debug>=1) System.out.println("minimum distance " + minDistance);
                    // adjust tolerances if needed (some reference values are hardcoded in FTConstants.java
                    double diffRadTolerance = 0.5;
                    double diffPhiTolerance = 1.;
                    double thetaTolerance = 0.05;
                    if(iBest>-1 && jBest>-1){
                        double r1 = Math.sqrt(x[iBest]*x[iBest]+y[iBest]*y[iBest]);
                        double r2 = Math.sqrt(x[jBest]*x[jBest]+y[jBest]*y[jBest]);
                        double diffRadii = r1-r2;    
                        double diffTheta = Math.atan2(r1,z[iBest])- Math.atan2(r2,z[jBest]);
                        if(minDiffPhi < diffPhiTolerance &&
                            Math.abs(diffRadii)<diffRadTolerance && diffTheta<thetaTolerance){
                                if(debug>=1) System.out.println("phi differences on two detectors " + diffPhi + " number of cross " + ncmatch + 
                                    " diffTheta " + diffTheta + " diffRadii " + diffRadii + " " + det[iBest] + " " + det[jBest]);
                                hHitMatch.fill((x[iBest]+x[jBest])/2., (y[iBest]+y[jBest])/2.);
                                ncmatch++;
                        }   
                    }
                }
            }    
        }
    
        // output
        if(debug>=1) System.out.println("number of found crosses: module 1: " + nc1 + " module 2: " + nc2 + " matching crosses " + ncmatch);
        
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(1200,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(2,3);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        canvas.cd(3); canvas.draw(h333);
        canvas.cd(4); canvas.draw(h1clEn);
        canvas.cd(5); canvas.draw(h444);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); 
        
        JFrame framest = new JFrame("FT strip vs time in layers");
        framest.setSize(800,800);
        EmbeddedCanvas canvast = new EmbeddedCanvas();
        canvast.divide(2,2);
        canvast.cd(0); canvast.draw(h445);
        canvast.cd(1); canvast.draw(h446);
        canvast.cd(2); canvast.draw(h447);
        canvast.cd(3); canvast.draw(h448);
        framest.add(canvast);
        framest.setLocationRelativeTo(null);
        framest.setVisible(true); 
        
        JFrame frameDiff = new JFrame("Strip Difference");
        frameDiff.setSize(800,800);
        EmbeddedCanvas canvasDiff = new EmbeddedCanvas();
        canvasDiff.divide(2,3);
        canvasDiff.cd(0); canvasDiff.draw(hStripDiff1);
        canvasDiff.cd(1); canvasDiff.draw(hStripDiff2);
        canvasDiff.cd(2); canvasDiff.draw(hStripDiff3);
        canvasDiff.cd(3); canvasDiff.draw(hStripDiff4);
        canvasDiff.cd(4); canvasDiff.draw(hNumberOfStrips);
        canvasDiff.cd(5); canvasDiff.draw(hNumberOfStripsInCrosses);
        frameDiff.add(canvasDiff);
        frameDiff.setLocationRelativeTo(null);
        frameDiff.setVisible(true);
        
        JFrame frameStrip = new JFrame("Strips in clusters");
        frameStrip.setSize(800,800);
        EmbeddedCanvas canvasStrip = new EmbeddedCanvas();
        canvasStrip.divide(2,2);
        canvasStrip.cd(0); canvasStrip.draw(hStripLay1);
        canvasStrip.cd(1); canvasStrip.draw(hStripLay2);
        canvasStrip.cd(2); canvasStrip.draw(hStripLay3);
        canvasStrip.cd(3); canvasStrip.draw(hStripLay4);
        frameStrip.add(canvasStrip);
        frameStrip.setLocationRelativeTo(null);
        frameStrip.setVisible(true);
        
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
        
        JFrame frame3 = new JFrame("FT Occupancy single layers");
        frame3.setSize(1200,800);
        EmbeddedCanvas canvas3 = new EmbeddedCanvas();
        canvas3.divide(2,2);
        int ic=-1;
        double upl = 100.;
        DataLine l1 = new DataLine(64., 0., 64., upl);
        DataLine l2 = new DataLine(129., 0., 129., upl);
        DataLine l3 = new DataLine(162., 0., 162., upl);
        DataLine l4 = new DataLine(193., 0., 193., upl);
        DataLine l5 = new DataLine(224., 0., 224., upl); //
        DataLine l6 = new DataLine(256., 0., 256., upl); 
        DataLine l7 = new DataLine(288., 0., 288., upl); //
        DataLine l8 = new DataLine(321., 0., 321., upl); 
        DataLine l9 = new DataLine(353., 0., 353., upl); 
        DataLine l10 = new DataLine(385., 0., 385., upl); 
        DataLine l11 = new DataLine(417., 0., 417., upl); 
        DataLine l12 = new DataLine(449., 0., 449., upl);
        DataLine l13 = new DataLine(480., 0., 480., upl); //
        DataLine l14 = new DataLine(513., 0., 513., upl);
        DataLine l15 = new DataLine(544., 0., 544., upl); //
        DataLine l16 = new DataLine(578., 0., 578., upl);
        DataLine l17 = new DataLine(611., 0., 611., upl); 
        DataLine l18 = new DataLine(641., 0., 641., upl);
        DataLine l19 = new DataLine(705., 0., 705., upl); 
        DataLine l20 = new DataLine(768., 0., 768., upl);
        l1.setLineColor(6); l2.setLineColor(6); l3.setLineColor(6); 
        l4.setLineColor(6); l5.setLineColor(6); l6.setLineColor(6); 
        l7.setLineColor(6); l8.setLineColor(6); l9.setLineColor(6); l10.setLineColor(6);
        l11.setLineColor(6); l12.setLineColor(6); l13.setLineColor(6); l14.setLineColor(4); l15.setLineColor(6);
        l16.setLineColor(6); l17.setLineColor(6); l18.setLineColor(6); l19.setLineColor(6); l20.setLineColor(6);
        l1.setLineStyle(3); l2.setLineStyle(3); l3.setLineStyle(3); l4.setLineStyle(3); l5.setLineStyle(3);
        
        canvas3.cd(++ic); canvas3.draw(hOccupancy1);
        canvas3.draw(l1); canvas3.draw(l2); canvas3.draw(l3); 
        canvas3.draw(l4); 
        canvas3.draw(l6); 
        canvas3.draw(l8); canvas3.draw(l9); 
        canvas3.draw(l10); canvas3.draw(l11); 
        canvas3.draw(l12); 
        canvas3.draw(l14); 
        canvas3.draw(l16); canvas3.draw(l17); 
        canvas3.draw(l18); canvas3.draw(l19); canvas3.draw(l20);
        canvas3.cd(++ic); canvas3.draw(hOccupancy2);
        canvas3.draw(l1); canvas3.draw(l2); canvas3.draw(l3); 
        canvas3.draw(l4); 
        canvas3.draw(l6);  
        canvas3.draw(l8); canvas3.draw(l9); 
        canvas3.draw(l10); canvas3.draw(l11); 
        canvas3.draw(l12); 
        canvas3.draw(l14);  
        canvas3.draw(l16); canvas3.draw(l17); 
        canvas3.draw(l18); canvas3.draw(l19); canvas3.draw(l20);
        canvas3.cd(++ic); canvas3.draw(hOccupancy3);
        canvas3.draw(l1); canvas3.draw(l2); canvas3.draw(l3); 
        canvas3.draw(l4);  
        canvas3.draw(l6); 
        canvas3.draw(l8); canvas3.draw(l9); 
        canvas3.draw(l10); canvas3.draw(l11); 
        canvas3.draw(l12); 
        canvas3.draw(l14); 
        canvas3.draw(l16);  
        canvas3.draw(l18); canvas3.draw(l19); canvas3.draw(l20);
        canvas3.cd(++ic); canvas3.draw(hOccupancy4);
        canvas3.draw(l1); canvas3.draw(l2); canvas3.draw(l3); 
        canvas3.draw(l4); 
        canvas3.draw(l6); 
        canvas3.draw(l8); canvas3.draw(l9); 
        canvas3.draw(l10); canvas3.draw(l11); 
        canvas3.draw(l12); 
        canvas3.draw(l14); 
        canvas3.draw(l16); 
        canvas3.draw(l18); canvas3.draw(l19); canvas3.draw(l20);
        frame3.add(canvas3);
        frame3.setLocationRelativeTo(null);
        frame3.setVisible(true); 
        
    }
}

        
    
