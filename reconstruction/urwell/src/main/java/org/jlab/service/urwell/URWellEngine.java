package org.jlab.service.urwell;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;

/**
 *
 * URWell reconstruction engine
 * 
 * @author bondi, devita
 */
public class URWellEngine extends ReconstructionEngine {

    public static Logger LOGGER = Logger.getLogger(URWellEngine.class.getName());


    public URWellEngine() {
        super("URWell","bondi","1.0");
    }

    @Override
    public boolean init() {

        // init ConstantsManager to read constants from CCDB
        // load geometry
        // register output banks for drop option
        System.out.println("["+this.getName()+"] --> URWells are ready....");
        return true;
    }




    @Override
    public boolean processDataEvent(DataEvent event) {
        
        int run = -1;
        
        if(event.hasBank("RUN::config")){
            DataBank bank = event.getBank("RUN::config");
            run = bank.getInt("run", 0);
            if (run<=0) {
                LOGGER.log(Level.WARNING,"URwellEngine:  got run <= 0 in RUN::config, skipping event.");
                return false;
            }
        }
        
        List<URWellStrip> hits = URWellStrip.getStrips(event, this.getConstantsManager());
        List<URWellCluster> clusters = URWellCluster.createClusters(hits);
        
        this.writeHipoBanks(event, hits, clusters);
        
        return true;
    }

    
    private void writeHipoBanks(DataEvent de, 
                                List<URWellStrip>     hits, 
                                List<URWellCluster> clusters){
//	    
//        DataBank bankS = de.createBank("ECAL::hits", strips.size());
//        for(int h = 0; h < strips.size(); h++){
//            bankS.setByte("sector",     h,  (byte) strips.get(h).getDescriptor().getSector());
//            bankS.setByte("layer",      h,  (byte) strips.get(h).getDescriptor().getLayer());
//            bankS.setByte("strip",      h,  (byte) strips.get(h).getDescriptor().getComponent());
//            bankS.setByte("peakid",     h,  (byte) strips.get(h).getPeakId());
//            bankS.setShort("id",        h, (short) strips.get(h).getID());
//            bankS.setShort("clusterId", h, (short) strips.get(h).getClusterId());
//            bankS.setFloat("energy",    h, (float) strips.get(h).getEnergy());
//            bankS.setFloat("time",      h, (float) strips.get(h).getTime());                
//        }
//        
//        DataBank bankC = de.createBank("ECAL::clusters", clusters.size());        
//        for(int c = 0; c < clusters.size(); c++){
//            bankC.setByte("sector",  c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getSector());
//            bankC.setShort("status", c, (short) clusters.get(c).getStatus());
//            bankC.setByte("layer",   c,  (byte) clusters.get(c).clusterPeaks.get(0).getDescriptor().getLayer());
//            bankC.setFloat("energy", c, (float) clusters.get(c).getEnergy());
//            bankC.setFloat("time",   c, (float) clusters.get(c).getTime());
//            bankC.setByte("idU",     c,  (byte) clusters.get(c).UVIEW_ID);
//            bankC.setByte("idV",     c,  (byte) clusters.get(c).VVIEW_ID);
//            bankC.setByte("idW",     c,  (byte) clusters.get(c).WVIEW_ID);
//            bankC.setFloat("x",      c, (float) clusters.get(c).getHitPosition().x());
//            bankC.setFloat("y",      c, (float) clusters.get(c).getHitPosition().y());
//            bankC.setFloat("z",      c, (float) clusters.get(c).getHitPosition().z());
//            bankC.setFloat("widthU", c,         clusters.get(c).getPeak(0).getMultiplicity());
//            bankC.setFloat("widthV", c,         clusters.get(c).getPeak(1).getMultiplicity());
//            bankC.setFloat("widthW", c,         clusters.get(c).getPeak(2).getMultiplicity());
//            bankC.setInt("coordU",   c,         clusters.get(c).getPeak(0).getCoord());
//            bankC.setInt("coordV",   c,         clusters.get(c).getPeak(1).getCoord());
//            bankC.setInt("coordW",   c,         clusters.get(c).getPeak(2).getCoord());
//  
//        }       
//         de.appendBanks(bankS,bankC);
    }

}
