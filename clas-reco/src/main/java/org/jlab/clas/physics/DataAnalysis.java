/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.groot.data.H1F;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class DataAnalysis {
    public static void main(String[] args){
        OptionParser parser = new OptionParser();
        parser.addRequired("-i", "input file");
        parser.addOption("-b", "100", "number of bins in the histogram");
        parser.addRequired("-part", "particle string");
        parser.addRequired("-var", "variable of the particle");
        parser.addRequired("-min", "minimum value for histogram");
        parser.addRequired("-max", "maximum value for histogram");
        parser.addRequired("-filter", "event filter");
        
        parser.parse(args);
        
        H1F h1 = new H1F("h1",parser.getOption("-b").intValue(),parser.getOption("-min").doubleValue(),
                parser.getOption("-max").doubleValue());
        h1.setFillColor(43);
        HipoDataSource reader = new HipoDataSource();
        reader.open(parser.getOption("-i").stringValue());
        EventFilter filter = new EventFilter(parser.getOption("-filter").stringValue());
        
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            DetectorEvent detEvent = DetectorData.readDetectorEvent(event);
            if(filter.isValid(detEvent.getPhysicsEvent())==true){
                //System.out.println(detEvent.getPhysicsEvent().toLundString());
                Particle part = detEvent.getPhysicsEvent().getParticle(parser.getOption("-part").stringValue());
                double var = part.get(parser.getOption("-var").stringValue());
                //System.out.println(" getting particle " + parser.getOption("-part").stringValue());
                //System.out.println(" " + part.get(parser.getOption("-var").stringValue()));
                h1.fill(var);
            }
        }
        
        TCanvas c1 = new TCanvas("c1",500,500);
        c1.draw(h1);
    }
}
