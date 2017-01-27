package org.jlab.service.ltcc;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

import javax.swing.JFrame;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jMath.Vector.threeVec;



/**
 * Make diagnostic histograms from the LTCC::clusters table.
 */
public class LTCCCLusterHistogrammer {

    /**
     * Main routine for testing
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        String inputfile = "/Users/sly2j/Dropbox/Work/JLab-CLAS12/clas12-ltcc/sylvester/rec/20170123/coatjava/rec-large-e+.hipo";

        HipoDataSource reader = new HipoDataSource();
        reader.open(inputfile);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);
        
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(3,2);
        canvas.setAxisFontSize(14);
        H1F hsector = new H1F("sector", "sector", "#", 10, 0, 10);
        H1F hsegment = new H1F("segment", "segment", "#", 20, 0, 20);
        H1F hnphe = new H1F("nphe", "nphe", "#", 50, 0, 50);
        H1F hnhit = new H1F("nHit", "nHit", "#", 10, 0, 10);
        H1F htime = new H1F("time", "time [ns]", "#", 10, 0, 100);
        H2F hloc = new H2F("x-y location", 50, -300., 300, 50, -300., 300);  
        H1F hx = new H1F("x", "x [cm]", "#", 100, -400., 400.);
        H1F hy = new H1F("y", "y [cm]", "#", 100, -400., 400.);
        H1F htheta = new H1F("#theta", "#theta [deg]", "#", 360, 0, 40);
        H1F hphi = new H1F("#phi", "#phi [deg]", "#", 360, 0, 360);

        
        hsector.setFillColor(33);
        hsegment.setFillColor(34);
        hnphe.setFillColor(36);
        hnhit.setFillColor(35);
        htime.setFillColor(38);
        hx.setFillColor(38);
        hy.setFillColor(38);
        htheta.setFillColor(38);
        hphi.setFillColor(38);
        
        int sector;
        int segment;
        float nphe;
        int nHit;
        float time;
        float x; 
        float y;
        double theta;
        double phi;
        
        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            if (event.hasBank("LTCC::clusters")) {
                DataBank bank = event.getBank("LTCC::clusters");
                for (int i = 0; i < bank.rows(); ++i) {
                    sector = bank.getByte("sector", i);
                    segment = bank.getShort("segment", i);
                    nphe = bank.getFloat("nphe", i);
                    nHit = bank.getShort("nHits", i);
                    time = bank.getFloat("time", i) * 1e+9f;
                    x = bank.getFloat("x", i);
                    y = bank.getFloat("y", i); 
                    float z = bank.getFloat("z", i);
                    hsector.fill(sector);
                    hsegment.fill(segment);
                    hnphe.fill(nphe);
                    hnhit.fill(nHit);
                    htime.fill(time);
                    hloc.fill(x, y);
                    hx.fill(x);
                    hy.fill(y);
                    threeVec pos = new threeVec(x, y, z);
                    phi = pos.phi();
                    theta = pos.theta();
                    htheta.fill(Math.toDegrees(theta));
                    if (phi < 0) {
                        phi = Math.PI * 2 + phi;
                    }
                    hphi.fill(Math.toDegrees(phi));
                }
                
            }
        }
        
        DataGroup hgroup = new DataGroup(3,2);
        hgroup.addDataSet(hsector, 0);
        hgroup.addDataSet(hsegment, 1);
        hgroup.addDataSet(hnphe, 3);
        hgroup.addDataSet(hnhit, 4);
        //hgroup.addDataSet(htime, 4);
        //hgroup.addDataSet(hloc, 5);
        //hgroup.addDataSet(hx,2);
        //hgroup.addDataSet(hy,5);
        hgroup.addDataSet(htheta,2);
        hgroup.addDataSet(hphi,5);

        canvas.draw(hgroup);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }
}
