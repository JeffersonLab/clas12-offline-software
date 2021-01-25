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
import org.jlab.utils.system.ClasUtilsFile;
import org.jlab.rec.ft.FTConstants;

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
//		reco.debugMode=0;

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
        String input = "/home/filippi/clas12/fttrkDev/clas12-offline-software-6.5.13-fttrkDev/gemc_singleEle_nofields_big_-30.60.120.30.hipo";
        System.out.println("input file " + input);
	HipoDataSource  reader = new HipoDataSource();
	reader.open(input);
		
	// initialize histos
        H2F h1 = new H2F("h1", "Layer vs. Component", 768, 0., 769., 4, 0.5, 4.5);
        h1.setTitleX("Component");
        h1.setTitleY("Layer");
        H1F h2 = new H1F("Energy",100, 0, 100);         
        h2.setOptStat(Integer.parseInt("1111")); h2.setTitleX("Energy"); h2.setTitleY("Counts");
        H1F h3 = new H1F("Time",100, 0., 1.e-3);
        h3.setOptStat(Integer.parseInt("1111")); h3.setTitleX("Time"); h3.setTitleY("Counts");
        H1F h1clEn = new H1F("Cluster energy", 100, 0., 100.);
        h1clEn.setOptStat(Integer.parseInt("1111")); h1clEn.setTitleX("centroid energy of clusters"); 
        H1F hOccupancy1 = new H1F("hOccupancy1", 768, 0., 769.); hOccupancy1.setTitleX("Component layer 1"); 
        hOccupancy1.setLineColor(1); hOccupancy1.setFillColor(1);
        H1F hOccupancy2 = new H1F("hOccupancy2", 768, 0., 769.); hOccupancy2.setTitleX("Component layer 2"); 
        hOccupancy2.setLineColor(1); hOccupancy2.setFillColor(2);
        H1F hOccupancy3 = new H1F("hOccupancy3", 768, 0., 769.); hOccupancy3.setTitleX("Component layer 3"); 
        hOccupancy3.setLineColor(1); hOccupancy3.setFillColor(3);
        H1F hOccupancy4 = new H1F("hOccupancy4", 768, 0., 769.); hOccupancy4.setTitleX("Component layer 4"); 
        hOccupancy4.setLineColor(1); hOccupancy4.setFillColor(4);
        
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
        int nev=-1;
        while(reader.hasEvent()){
//        int nev1 = 0; int nev2 = nev1+6150; for(nev=nev1; nev<nev2; nev++){   // debug only a set of events (uncomment while loop in case)
           DataEvent event = (DataEvent) reader.getNextEvent();
            if(debug>=1) System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ processing event ~~~~~~~~~~~ " + ++nev); 
//            if(nev!=6146) continue;    // select one event only for debugging purposes
            
            ArrayList<FTTRKCluster> clusters = new ArrayList();
            
            clusters = trk.processDataEventAndGetClusters(event);
            int nStripsInClusters = 0;

            DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
            PhysicsEvent            gen = detectorEvent.getGeneratedEvent();
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
                    
                    if(debug>=1) System.out.println("layer " + layer + " strip " + comp);
                    h1.fill(comp,layer);
                    h2.fill(energy);
                    h3.fill(time);
                    if(layer==1){
                        if(debug>=1) System.out.println("component layer 1 " + comp + " event number " + nev + " n rows " + nrows);
                        hOccupancy1.fill(comp);
                        if(energy>maxcomp1){
                            maxcomp1 = energy;
                            imax1 = i;           
                        }
                    }else if(layer==2){
                        hOccupancy2.fill(comp);
                        if(energy>maxcomp2){
                            maxcomp2 = energy;
                            imax2 = i;           
                        }
                    }else if(layer==3){
                        hOccupancy3.fill(comp);
                        if(energy>maxcomp3){
                            maxcomp3 = energy;
                            imax3 = i;           
                        }
                    }else if(layer==4){
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
                canvasCl.cd(1); canvasCl.draw(hHitL1);
                for(int l=0; l<4; l++){
                    canvasClSingleLay.cd(l); 
                    if(l==2 || l==3){canvasClSingleLay.draw(hHitL2);}else{canvasClSingleLay.draw(hHitL1);}
                }
                for(int i = 0; i < clusters.size(); i++){
                    // get a single cluster and count its strip, extract the information on extremal points of the segment
                    FTTRKCluster singleCluster = clusters.get(i);
                    if(singleCluster.get_AssociatedCrossID() >-1) hNumberOfStripsInCrosses.fill(singleCluster.size());
                    if(singleCluster.get_Layer()==1){
                        hStripDiff1.fill(maxcomp1 - singleCluster.get_Centroid());
                        hStripLay1.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==2){
                        hStripDiff2.fill(maxcomp2 - singleCluster.get_Centroid());
                        hStripLay2.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==3){
                        hStripDiff3.fill(maxcomp3 - singleCluster.get_Centroid());
                        hStripLay3.fill(singleCluster.size());
                    }else if(singleCluster.get_Layer()==4){
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
                    
                    if(det[i]==0) {hHitL1.fill(x[i], y[i]); nc1++;}
                    if(det[i]==1) {hHitL2.fill(x[i], y[i]); nc2++;}
                }
                /// loop on all crosses on detector 1 and 2 and find the one with better matching
                double minDistance = 1000;
                double minDiffPhi = 1000.;
                int iBest = -1, jBest = -1;
                if(debug>=1) System.out.println("number of rows " + nrows);
                if(nrows>1){
                    double diffPhi = 1000;
                    for(int i=0; i<nrows; i++){
                        for(int j=nrows-1; j>i; j--){
                            if(det[i]!=det[j]){
                                double distance = Math.sqrt((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]));
//                                if(distance < minDistance){
//                                    minDistance = distance;
//                                    iBest = i; jBest = j;
//                  check det1 and det2 correspondence by comparing phi angles
//                  the radius is larger for the second detector (as it is more distant)
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
                    // adjust tolerances if needed
                    double diffRadTolerance = 0.5;
                    double diffPhiTolerance = 1.;
                    double thetaTolerance = 0.05;
                    if(iBest>-1 && jBest>-1){
                    double r1 = Math.sqrt(x[iBest]*x[iBest]+y[iBest]*y[iBest]);
                    double r2 = Math.sqrt(x[jBest]*x[jBest]+y[jBest]*y[jBest]);
                    double diffRadii = r1-r2;    
                    double diffTheta = Math.atan2(r1,z[iBest])- Math.atan2(r2,z[jBest]);
 //                   if(minDiffPhi < diffPhiTolerance && diffRadii<0 && Math.abs(diffRadii)<diffRadTolerance && 
 //                           diffTheta<thetaTolerance){
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
    
        
        if(debug>=1) System.out.println("number of found crosses: module 1: " + nc1 + " module 2: " + nc2 + " matching crosses " + ncmatch);
        
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(800,800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(2,2);
        canvas.cd(0); canvas.draw(h1);
        canvas.cd(1); canvas.draw(h2);
        canvas.cd(2); canvas.draw(h3);
        canvas.cd(3); canvas.draw(h1clEn);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true); 
        
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
        canvas3.cd(++ic); canvas3.draw(hOccupancy1);
        canvas3.cd(++ic); canvas3.draw(hOccupancy2);
        canvas3.cd(++ic); canvas3.draw(hOccupancy3);
        canvas3.cd(++ic); canvas3.draw(hOccupancy4);
        frame3.add(canvas3);
        frame3.setLocationRelativeTo(null);
        frame3.setVisible(true); 
        
    }
}

        
    
