package org.jlab.service.ltcc.viewer;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.hipo3.Hipo3DataSource;

import javax.swing.JFrame;
import java.awt.Dimension;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.data.H1F;
import org.jlab.service.ltcc.LTCCCluster;
import org.jlab.service.ltcc.LTCCHit;
import org.jlab.detector.calib.utils.ConstantsManager;
import java.util.Arrays;
import java.util.List;
import org.jlab.service.ltcc.LTCCClusterFinder;




/**
 * Make diagnostic histograms from the LTCC::clusters table.
 */
public class LTCCViewer {
    
    private final LTCCHistogrammer<LTCCHit> hitHistos;
    private final LTCCHistogrammer<LTCCCluster> clusterHistos;
    private final ConstantsManager ccdb;
    private final Boolean DEBUG = true;
    private final Boolean RECALC = true;
    
    LTCCViewer() {
        hitHistos = new LTCCHitHistos();
        clusterHistos = new LTCCClusterHistos();
        ccdb = new ConstantsManager();
        List<String> ccdb_paths = Arrays.asList("/calibration/ltcc/spe");
        ccdb.init(ccdb_paths);
        setStyle();
    }
    
    public void process(DataEvent event, Boolean debug) {
        if (debug) {
            //event.show();
        }
        if (event.hasBank("LTCC::adc")) {
            if (debug) {
                event.getBank("LTCC::adc").show();
            }
            if (!RECALC) {
                LTCCHit.loadHits(event, ccdb).forEach(hit -> process(hit));
            } else{  
                List<LTCCHit> hits = LTCCHit.loadHits(event, ccdb);
                hits.forEach(hit -> process(hit));
                List<LTCCCluster> clusters = LTCCClusterFinder.findClusters(hits);
                clusters.forEach(cluster -> process(cluster));
                if (debug) {
                    clusters.forEach(cluster -> cluster.print());
                }
            }
        }
        if (event.hasBank("LTCC::clusters")) {
            if (!RECALC) {
                LTCCCluster.loadClusters(event, true).forEach(cluster -> process(cluster));
                if (debug) {
                    event.getBank("LTCC::clusters").show();
                }
            }
        }
    }

    public void process(LTCCHit hit) {
        hitHistos.fill(hit);
    }

    public void process(LTCCCluster cluster) {
        clusterHistos.fill(cluster);
    }
    
    public void show() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("hits", "clusters");
        
        drawClusters(canvas.getCanvas("clusters"));
        drawHits(canvas.getCanvas("hits"));
   
        frame.add(canvas);
        frame.pack();
        frame.setMinimumSize(new Dimension(800, 450));
        frame.setVisible(true);
    }
    
     private void drawClusters(EmbeddedCanvas canvas) {
        canvas.divide(3, 3);
        DataGroup hgroup = new DataGroup(3, 3);
        int idx = 0;
        for(H1F h : clusterHistos.getH1Fs()) {
            hgroup.addDataSet(h, idx++);
        }
        canvas.draw(hgroup);
    }
    private void drawHits(EmbeddedCanvas canvas) {
        canvas.divide(3, 3);
        DataGroup hgroup = new DataGroup(3, 3);
        int idx = 0;
        for(H1F h : hitHistos.getH1Fs()) {
            hgroup.addDataSet(h, idx++);
        }
        canvas.draw(hgroup);
    }
    
    private void setStyle() {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
    }
    
    
    /**
     * Main routine for testing
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        String inputfile = "/Users/sjoosten/Data/CLAS12/006715/monitorclas_006715.evio.00041.hipo";

        DataSource reader = new Hipo3DataSource();
        reader.open(inputfile);
        
        LTCCViewer viewer = new LTCCViewer();
        
        // run first 20 events in optional debug mode
        for (int i = 0; i < 20; ++i) {
            viewer.process(reader.getNextEvent(), viewer.DEBUG);
        }
        // the rest in normal mode
        while (reader.hasEvent()) {
            viewer.process(reader.getNextEvent(), false);
        }
        
        viewer.show();
    }
}
