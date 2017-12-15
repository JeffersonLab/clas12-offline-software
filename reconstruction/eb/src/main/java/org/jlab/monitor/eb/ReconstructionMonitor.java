package org.jlab.monitor.eb;

import java.util.ArrayList;
import java.util.List;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.ui.TBrowser;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class ReconstructionMonitor {
    public static void main(String[] args){
        String inputFile  = args[0];
        String outputFile = args[1];
        
        TDirectory  dir = new TDirectory();
        
        List<ParticleReconstruction>  recModules = new ArrayList<ParticleReconstruction>();
        recModules.add(new ParticleReconstruction(dir,11));
        recModules.add(new ParticleReconstruction(dir,2212));
        recModules.add(new ParticleReconstruction(dir,211));
        recModules.add(new ParticleReconstruction(dir,-211));
        
        
        ReactionAnalysis  analysisPi0 = new ReactionAnalysis(dir);
        
        List<String> objects = dir.getCompositeObjectList(dir);
        System.out.println("OBJECTS");
        for(String o : objects){
            System.out.println(o);
        }
        
        System.out.println("OBJECTS END");
        HipoDataSource reader = new HipoDataSource();  
        reader.open(inputFile);
        
        int events = 0;
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            for(ParticleReconstruction m : recModules){
                m.process(event, dir);
            }
            analysisPi0.process(event, dir);
            events++;
        }
        System.out.println(" processed events # " + events);
        dir.writeFile(outputFile);
        
        //TBrowser t = new TBrowser(dir);
    }
}
