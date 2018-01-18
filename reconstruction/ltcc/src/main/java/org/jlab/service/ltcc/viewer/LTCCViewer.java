package org.jlab.service.ltcc.viewer;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.hipo.HipoDataSource;

import javax.swing.JFrame;
import java.awt.Dimension;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.data.H1F;
import org.jlab.service.ltcc.LTCCCluster;
import org.jlab.service.ltcc.LTCCHit;




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
        String inputfile = "/Users/sly2j/Data/CLAS12/pass0_4/out_clas_002053.evio.1.hipo";

        DataSource reader = new HipoDataSource();
        reader.open(inputfile);
       
        LTCCViewer viewer = new LTCCViewer();
        
        // dump the first 10 events
        for (int i = 0; i < 1; ++i) {
            reader.getNextEvent().getBank("LTCC::adc").show();
        }
        while (reader.hasEvent()) {
            viewer.process(reader.getNextEvent());
        }
        
        viewer.show();
    }
}
