/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc.viewer;

import org.jlab.groot.data.H1F;
import org.jlab.service.ltcc.LTCCCluster;


/**
 *
 * @author sly2j
 */
public class LTCCClusterHistos extends LTCCHistogrammer<LTCCCluster> {
    LTCCClusterHistos() {
        super();
        
        H1F hsector = new H1F("sector", "sector", "#", 10, 0, 10);
        hsector.setFillColor(33);
        this.add(hsector, cluster -> (double) cluster.getSector());
        
        H1F hsegment = new H1F("segment", "segment", "#", 20, 0, 20);
        hsegment.setFillColor(34);
        this.add(hsegment, cluster -> (double) cluster.getSegment());
        
        H1F htime = new H1F("time", "time [ns]", "#", 10, 0, 100);
        htime.setFillColor(38);
        this.add(htime, cluster -> (double) cluster.getTime());
        
        H1F hnphe = new H1F("nphe", "nphe", "#", 50, 0, 50);
        hnphe.setFillColor(36);
        this.add(hnphe, cluster -> (double) cluster.getNphe());
        
        H1F htheta = new H1F("theta", "theta [deg]", "#", 360, 0, 40);
        htheta.setFillColor(38);
        this.add(htheta, cluster -> Math.toDegrees(cluster.getPosition().theta()));
        
        H1F hphi = new H1F("phi", "phi [deg]", "#", 360, 0, 360);
        hphi.setFillColor(38);
        this.add(hphi, cluster -> {
            double phi = Math.toDegrees(cluster.getPosition().phi());
            return (phi > 0 ? phi : 360 + phi);
        });
        
        H1F hnHits = new H1F("nHits", "nHits", "#", 10, 0, 10);
        hnHits.setFillColor(35);
        this.add(hnHits, cluster -> cluster.getNHits());
    }
}
