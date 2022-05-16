package org.jlab.rec.ft.trk;

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

/*
            String[]  tables = new String[]{ 
                "/calibration/ft/fthodo/charge_to_energy",
                "/calibration/ft/fthodo/time_offsets",
                "/calibration/ft/fthodo/status",
                "/geometry/ft/fthodo",
                "/geometry/ft/fttrk"
            };
            requireConstants(Arrays.asList(tables));
            this.getConstantsManager().setVariation("default");
*/            
            

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
        
        H1F h1clEn = new H1F("Cluster energy", 100, 0., 1000.);
        h1clEn.setOptStat(Integer.parseInt("1111")); h1clEn.setTitleX("centroid energy of clusters"); 
        H1F hOccupancy1 = new H1F("hOccupancy1", 768, 0., 769.); hOccupancy1.setTitleX("Component layer 1"); 
        hOccupancy1.setLineColor(1); hOccupancy1.setFillColor(1);
        H1F hOccupancy2 = new H1F("hOccupancy2", 768, 0., 769.); hOccupancy2.setTitleX("Component layer 2"); 
        hOccupancy2.setLineColor(1); hOccupancy2.setFillColor(2);
        H1F hOccupancy3 = new H1F("hOccupancy3", 768, 0., 769.); hOccupancy3.setTitleX("Component layer 3"); 
        hOccupancy3.setLineColor(1); hOccupancy3.setFillColor(3);
        H1F hOccupancy4 = new H1F("hOccupancy4", 768, 0., 769.); hOccupancy4.setTitleX("Component layer 4"); 
        hOccupancy4.setLineColor(1); hOccupancy4.setFillColor(4);
        
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
        
        int nc1 = 0, nc2 = 0, ncmatch = 0;
        int nev=0;
        while(reader.hasEvent()){
//        int nev1 = 0; int nev2 = 20000; for(nev=nev1; nev<nev2; nev++){   // debug only a set of events (uncomment while loop in case)
            DataEvent event = (DataEvent) reader.getNextEvent();
            if(debug>=1) System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ processing event ~~~~~~~~~~~ " + nev); 
            
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
                    h1.fill(comp,layer);
                    h2.fill(energy);
                    h3.fill(time);
                    h333.fill(time, energy);
                    h444.fill(time, comp);
                    
                    switch(layer){
                        case DetectorLayer.FTTRK_LAYER1:
                            hOccupancy1.fill(comp);
                            if(energy>maxcomp1){
                                maxcomp1 = energy;
                                imax1 = i;           
                            }
                            break;
                        case DetectorLayer.FTTRK_LAYER2:
                            hOccupancy2.fill(comp);
                            if(energy>maxcomp2){
                                maxcomp2 = energy;
                                imax2 = i;           
                            }
                            break;
                        case DetectorLayer.FTTRK_LAYER3:    
                            hOccupancy3.fill(comp);
                            if(energy>maxcomp3){
                                maxcomp3 = energy;
                                imax3 = i;           
                            }
                            break;
                        case DetectorLayer.FTTRK_LAYER4:
                            hOccupancy4.fill(comp);
                            if(energy>maxcomp4){
                                maxcomp4 = energy;
                                imax4 = i;           
                            }
                            break;
                        default:    
                    }
            	}
            }            
          
            canvasCl.draw(hHitL1);  // dummy histogram
            // iterate along the cluster list for every event
            if(debug>=1) System.out.println("clusters size --- " + clusters.size());
            if(clusters.size()!=0){
                // get one cluster and iterate over all the strips contained in it
                for(int i = 0; i < clusters.size(); i++){
                    // get a single cluster and count its strip, extract the information on extremal points of the segment
                    FTTRKCluster singleCluster = clusters.get(i);
                    int nst = singleCluster.size();
                    for(int j=0; j<nst; j++){
                        if(singleCluster.get_TotalEnergy() < FTConstants.TRK_MIN_CLUS_ENERGY) continue;
                        Line3D seg = singleCluster.get(j).get_StripSegment();
                        segment[nStripsInClusters] = new DataLine(seg.origin().x(), seg.origin().y(), seg.end().x(), seg.end().y());
                        int lay = singleCluster.get_Layer();
                        segment[nStripsInClusters].setLineColor(lay);
                        if(debug>=1) System.out.println("nStripsInCluster " + nStripsInClusters);
                        canvasCl.draw(segment[nStripsInClusters]);
                        h1clEn.fill(singleCluster.get_TotalEnergy());
                        nStripsInClusters++;
                    }
                }
            }

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
                    
                    if(det[i]==(DetectorLayer.FTTRK_MODULE1 - 1)) {hHitL1.fill(x[i], y[i]); nc1++;}
                    if(det[i]==(DetectorLayer.FTTRK_MODULE2 - 1)) {hHitL2.fill(x[i], y[i]); nc2++;}
                }
                /// loop on all crosses on detector 1 and 2 and find the one with better matching
                double minDistance = 1000;
                double minDiffPhi = 1000.;
                int iBest = -1, jBest = -1;
                if(nrows>1){
                    double diffPhi = 1000;
                    for(int i=0; i<nrows; i++){
                        for(int j=nrows-1; j>i; j--){
                            if(det[i]!=det[j]){
                                double distance = Math.sqrt((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]));
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
                    // adjust tolerances if needed
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
                                hHitMatch.fill((x[iBest]+x[jBest])/2., (y[iBest]+y[jBest])/2.);
                                ncmatch++;
                        }   
                    }
                }
             }    
        }

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

        
    
