package org.jlab.rec.urwell.reader;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author Tongtong Cao
 */
public class URWellReader{
   
    private final List<URWellHit>       urHits     = new ArrayList<>();
    private final List<URWellCluster>   urClusters = new ArrayList<>();
    private final List<URWellCross>     urCrosses  = new ArrayList<>(); 
      
    
    public URWellReader(DataEvent event) {
        
        if(event.hasBank("URWELL::hits"))
            this.readHits(event.getBank("URWELL::hits"));
        if(event.hasBank("URWELL::clusters"))
            this.readClusters(event.getBank("URWELL::clusters"));
        if(event.hasBank("URWELL::crosses"))
            this.readCrosses(event.getBank("URWELL::crosses"));
    }
    
    public URWellReader(DataEvent event, String type) {
        if(type == "HB"){            
            if(event.hasBank("URWELL::clusters"))
                this.readClusters(event.getBank("URWELL::clusters"));
            if(event.hasBank("URWELL::crosses"))
                this.readCrosses(event.getBank("URWELL::crosses"));
        }
        else if(type == "TB"){ 
            if(event.hasBank("HitBasedTrkg::HBURWellCrosses"))
                this.readHBCrosses(event.getBank("HitBasedTrkg::HBURWellCrosses"));
        }
        else if(type == "AI"){   
            if(event.hasBank("HitBasedTrkg::AIURWellCrosses"))
                this.readHBCrosses(event.getBank("HitBasedTrkg::AIURWellCrosses"));
        }
    }
    
    public List<URWellHit> getUrwellHits() {
        return urHits;
    }

    public List<URWellCluster> getUrwellClusters() {
        return urClusters;
    }

    public List<URWellCross> getUrwellCrosses() {
        return urCrosses;
    }    
    
    public final void readHits(DataBank bank) {

        for(int i=0; i<bank.rows(); i++) {
            int    sector = bank.getByte("sector", i);
            int    layer  = bank.getByte("layer", i);
            int    strip  = bank.getShort("strip", i);
            double energy = bank.getFloat("energy", i);
            double time   = bank.getFloat("time", i);
            URWellHit hit = new URWellHit(sector, layer, strip, energy, time);
            urHits.add(hit);
        }
    }
    
    public final void readClusters(DataBank bank) {

        for(int i=0; i<bank.rows(); i++) {
            int    sector = bank.getByte("sector", i);
            int    layer  = bank.getByte("layer", i);
            int    strip  = bank.getShort("strip", i);
            int    size   = bank.getShort("size", i);
            double energy = bank.getFloat("energy", i);
            double time   = bank.getFloat("time", i);
            URWellCluster cluster = new URWellCluster(sector, layer, strip, size, energy, time);
            urClusters.add(cluster);
        }
    }
    
    // Crosses with energy and time coincidence for their clusters are stored
    public final void readCrosses(DataBank bank) {

        for(int i=0; i<bank.rows(); i++) {
            int id = bank.getShort("id", i);
            int    sector = bank.getByte("sector", i);
            double x      = bank.getFloat("x", i);
            double y      = bank.getFloat("y", i);
            double z      = bank.getFloat("z", i);                        
            double energy = bank.getFloat("energy", i);
            double time   = bank.getFloat("time", i);
            int  cluster1 = bank.getShort("cluster1", i);
            int  cluster2 = bank.getShort("cluster2", i); 
            int status = bank.getShort("status", i); 
            URWellCross cross = new URWellCross(id, sector, x, y, z, energy, time, cluster1, cluster2, status);
            cross.setClusterIndex1(cluster1);
            cross.setClusterIndex2(cluster2);
            if(cluster1<=urClusters.size()) urClusters.get(cluster1-1).setCrossIndex(i);
            if(cluster2<=urClusters.size()) urClusters.get(cluster2-1).setCrossIndex(i);           
            if(cross.isGood(urClusters) && cross.isInTime())            
                urCrosses.add(cross);
        }
    } 
    
        // Crosses with energy and time coincidence for their clusters are stored
    public final void readHBCrosses(DataBank bank) {

        for(int i=0; i<bank.rows(); i++) {
            int id = bank.getShort("id", i);
            int tid = bank.getShort("tid", i);    
            int    sector = bank.getByte("sector", i);
            double x_local = bank.getFloat("x", i);
            double y_local = bank.getFloat("y", i);
            double z_local = bank.getFloat("z", i);  
            double energy = bank.getFloat("energy", i);
            double time   = bank.getFloat("time", i);
            int  cluster1 = bank.getShort("cluster1", i);
            int  cluster2 = bank.getShort("cluster2", i); 
            int status = bank.getShort("status", i); 
            URWellCross cross = new URWellCross(id, tid, sector, x_local, y_local, z_local, energy, time, cluster1, cluster2, status);
            urCrosses.add(cross);
        }
    }
}
