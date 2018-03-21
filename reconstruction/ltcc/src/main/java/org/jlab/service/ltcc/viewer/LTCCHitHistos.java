/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc.viewer;
import org.jlab.service.ltcc.viewer.LTCCHistogrammer;
import org.jlab.groot.data.H1F;
import org.jlab.service.ltcc.LTCCHit;


/**
 *
 * @author sly2j
 */
public class LTCCHitHistos extends LTCCHistogrammer<LTCCHit> {
    LTCCHitHistos() {
        super();
        
        H1F hsector = new H1F("sector", "sector", "#", 10, 0, 10);
        hsector.setFillColor(33);
        this.add(hsector, hit -> (double) hit.getSector());
        
        H1F hsegment = new H1F("segment", "segment", "#", 20, 0, 20);
        hsegment.setFillColor(34);
        this.add(hsegment, hit -> (double) hit.getSegment());
        
        H1F hside = new H1F("side", "side", "#", 5, 0, 5);
        hside.setFillColor(37);
        this.add(hside, hit -> (double) hit.getSide());
        
        H1F hadc = new H1F("adc", "adc", "#", 50, 0, 5000);
        hadc.setFillColor(35);
        this.add(hadc, hit -> (double) hit.getADC());
        
        H1F hrawTime = new H1F("rawTime", "rawTime [ns]", "#", 10, 0, 100);
        hrawTime.setFillColor(38);
        this.add(hrawTime, hit -> (double) hit.getRawTime());
        
        H1F htime = new H1F("time", "time [ns]", "#", 10, 0, 100);
        htime.setFillColor(38);
        this.add(htime, hit -> (double) hit.getTime());
        
        H1F hnphe = new H1F("nphe", "nphe", "#", 50, 0, 50);
        hnphe.setFillColor(36);
        this.add(hnphe, hit -> (double) hit.getNphe());
        
        H1F htheta = new H1F("theta", "theta [deg]", "#", 360, 0, 40);
        htheta.setFillColor(38);
        this.add(htheta, hit -> Math.toDegrees(hit.getPosition().theta()));
        
        H1F hphi = new H1F("phi", "phi [deg]", "#", 360, 0, 360);
        hphi.setFillColor(38);
        this.add(hphi, hit -> {
            double phi = Math.toDegrees(hit.getPosition().phi());
            return (phi > 0 ? phi : 360 + phi);
        });
    }
}
