package org.jlab.service.ltcc;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.hipo.HipoDataSource;

import javax.swing.JFrame;
import java.awt.Dimension;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.group.DataGroup;




/**
 * Make diagnostic histograms from the LTCC::clusters table.
 */
public class LTCCViewer {
    
    private final LTCCHistogrammer<LTCCHit> hitHistos;
    private final LTCCHistogrammer<LTCCCluster> clusterHistos;
    
    LTCCViewer() {
        hitHistos = new LTCCHitHistos();
        clusterHistos = new LTCCClusterHistos();
        setStyle();
    }
    
    public void process(DataEvent event) {
        if (event.hasBank("LTCC::adc")) {
            LTCCHit.loadHits(event).forEach(hit -> process(hit));
        }
        if (event.hasBank("LTCC::clusters")) {
            LTCCCluster.loadClusters(event, true).forEach(cluster -> process(cluster));
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
        canvas.divide(3, 2);
        DataGroup hgroup = new DataGroup(3,2);
        hgroup.addDataSet(clusterHistos.getH1F("sector"), 0);
        hgroup.addDataSet(clusterHistos.getH1F("segment"), 1);
        hgroup.addDataSet(clusterHistos.getH1F("nHits"), 3);
        hgroup.addDataSet(clusterHistos.getH1F("nphe"), 4);
        hgroup.addDataSet(clusterHistos.getH1F("theta"), 2);
        hgroup.addDataSet(clusterHistos.getH1F("phi"), 5);
        canvas.draw(hgroup);
    }
    private void drawHits(EmbeddedCanvas canvas) {
        canvas.divide(3, 2);
        DataGroup hgroup = new DataGroup(3,2);
        hgroup.addDataSet(hitHistos.getH1F("sector"), 0);
        hgroup.addDataSet(hitHistos.getH1F("segment"), 1);
        hgroup.addDataSet(hitHistos.getH1F("adc"), 3);
        hgroup.addDataSet(hitHistos.getH1F("nphe"), 4);
        hgroup.addDataSet(hitHistos.getH1F("theta"), 2);
        hgroup.addDataSet(hitHistos.getH1F("phi"), 5);
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
        String inputfile = "/Users/sly2j/Dropbox/Work/JLab-CLAS12/clas12-ltcc/sylvester/rec/20170123/coatjava/rec.hipo";

        DataSource reader = new HipoDataSource();
        reader.open(inputfile);
       
        LTCCViewer viewer = new LTCCViewer();
        
        while (reader.hasEvent()) {
            viewer.process(reader.getNextEvent());
        }
        
        viewer.show();
    }
}
