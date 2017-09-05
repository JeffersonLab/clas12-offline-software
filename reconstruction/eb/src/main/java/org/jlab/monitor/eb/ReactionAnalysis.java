package org.jlab.monitor.eb;

import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.RecEvent;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class ReactionAnalysis {
    
    String directoryName = "";
    EventFilter   filter = new EventFilter("11:2212:22:22");
    EventFilter filterEP = new EventFilter("11:2212:Xn:X+:X-");
    public GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
                
    public ReactionAnalysis(TDirectory dir){
        directoryName = "/physics/eppi0";
        init(dir);
    }
    
    public void init(TDirectory dir){
        dir.cd();
        dir.mkdir(directoryName);
        dir.cd(directoryName);
        
        H1F h1a = new H1F("hQ2",100,0.0,5.0);
        H1F h1b = new H1F("hMxEP",100,0.0,0.5);
        H1F h1c = new H1F("hMPi0",100,0.0,0.5);
        
        dir.addDataSet(h1a,h1b,h1c);
        dir.cd();
        
    }
    
    public void process(DataEvent event, TDirectory directory){
         RecEvent recEvent = fitter.getRecEvent(event);
         recEvent.doPidMatch();
         
         if(filter.isValid(recEvent.getGenerated())==true){
             if(filterEP.isValid(recEvent.getReconstructed())==true){
                 Particle epmx = recEvent.getReconstructed().getParticle("[b]+[t]-[11]-[2212]");
                 
                 IDataSet dsP = directory.getObject(directoryName+"/hMxEP");
                 if(dsP != null) ( (H1F) dsP).fill(epmx.vector().mass());
             }
             if(recEvent.getReconstructed().countByPid(22)==2){
                 Particle ggm = recEvent.getReconstructed().getParticle("[22,0]+[22,1]");                 
                 IDataSet dsP = directory.getObject(directoryName+"/hMPi0");
                 if(dsP != null) ( (H1F) dsP).fill(ggm.vector().mass());
             }
         }
    }
}
