package org.jlab.rec.dc.sacluster;

import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.service.dc.*;
import org.jlab.io.base.DataEvent;
/**
 * @author ziegler
 * 
 */
public class SAClustering extends DCEngine {
    public SAClustering() {
        super("DCSAC");
        
    }
    
    public static int[] p = new int[] {0}; 
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        int run = this.getRun(event);
        if(run==0) return true;
        Clusterer.getClusters(event);
        List<Hit> hits = Clusterer.hits;
        List<Cluster> clusters = Clusterer.clusters;
        
        if(hits!=null)
            event.appendBanks(this.fillAIHitsBank(event, hits));
        if(clusters!=null)
            event.appendBanks(this.fillClusteredHitsBank(event, clusters));
        return true;
    }

    private DataBank fillClusteredHitsBank(DataEvent event, List<Cluster> cluslist) {
        String name = "HitBasedTrkg::AIHits"; //a hack for CED display.  The clusters are shown in green
        int index = 0;
        for(Cluster cl : cluslist) {
            for(int i = 0; i<cl.size(); i++) {
                index++;
                
            }
        }
        
        DataBank bank = event.createBank(name, index);
        index = 0;
        
        for(Cluster cl : cluslist) {
            for(int i = 0; i<cl.size(); i++) {
                bank.setShort("id", index, (short) cl.get(i).getId());
                bank.setShort("status", index, (short) 0);
                bank.setByte("superlayer", index, (byte) cl.get(i).getSuperLayer());
                bank.setByte("layer", index, (byte) cl.get(i).getLocLayer());
                bank.setByte("sector", index, (byte) cl.get(i).getSector());
                bank.setShort("wire", index, (short) cl.get(i).getWire());
                bank.setInt("TDC",index,100);
                bank.setShort("clusterID", index, (short) cl.get(i).getCid());
                index++;
            }
        }

        return bank;
        
    }
    private DataBank fillAIHitsBank(DataEvent event, List<Hit> hitlist) {
        String name = "HitBasedTrkg::HBHits"; //a hack for CED display; the denoised hits are shown in yellow.
        
        int rejCnt = 0;
        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).getId()==0 ) {
                rejCnt++;
            }
        }
        DataBank bank = event.createBank(name, hitlist.size()-rejCnt);
        rejCnt=0;
        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).getId()==0 ) {
                rejCnt++;
                continue;
            }

            bank.setShort("id", i-rejCnt, (short) hitlist.get(i).getId());
            bank.setShort("status", i-rejCnt, (short) 0);
            bank.setByte("superlayer", i-rejCnt, (byte) hitlist.get(i).getSuperLayer());
            bank.setByte("layer", i-rejCnt, (byte) hitlist.get(i).getLocLayer());
            bank.setByte("sector", i-rejCnt, (byte) hitlist.get(i).getSector());
            bank.setShort("wire", i-rejCnt, (short) hitlist.get(i).getWire());
            bank.setInt("TDC",i-rejCnt,100);
            bank.setShort("clusterID", i-rejCnt, (short) hitlist.get(i).getCid());
        }

        return bank;
        
    }   
}
