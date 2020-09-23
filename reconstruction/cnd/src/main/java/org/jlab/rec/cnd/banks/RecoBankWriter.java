package org.jlab.rec.cnd.banks;

import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.hit.CndHit;
import org.jlab.rec.cnd.cluster.CNDCluster;

public class RecoBankWriter {
    
    
    // write useful information in the bank
    public static DataBank fillCndHitBanks(DataEvent event, ArrayList<CndHit> hitlist) {
        
        DataBank bank =  event.createBank("CND::hits", hitlist.size());
        
        if (bank == null) {
            System.err.println("COULD NOT CREATE A CND::Hits BANK!!!!!!");
            return null;
        }
        
        // the bank use cm as distance units -> need to convert from mm
        for(int i =0; i< hitlist.size(); i++) {
            bank.setShort("id",i, (short)(i+1));
            bank.setByte("sector",i, (byte) hitlist.get(i).Sector());
            bank.setByte("layer",i, (byte) hitlist.get(i).Layer());
            bank.setShort("component",i, (short) hitlist.get(i).Component());
            bank.setShort("trkID",i, (short) hitlist.get(i).get_AssociatedTrkId());
            bank.setShort("clusterid", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setFloat("time",i, (float) hitlist.get(i).Time());
            bank.setFloat("x",i, (float) (hitlist.get(i).X()/10.));
            bank.setFloat("y",i, (float) (hitlist.get(i).Y()/10.));
            bank.setFloat("z",i, (float) (hitlist.get(i).Z()/10.));
            bank.setFloat("x_unc",i, (float) (hitlist.get(i).get_uX()/10.));
            bank.setFloat("y_unc",i, (float) (hitlist.get(i).get_uY()/10.));
            bank.setFloat("z_unc",i, (float) (hitlist.get(i).get_uZ()/10.));
            bank.setFloat("tx",i, (float) (hitlist.get(i).get_tX()/10.));
            bank.setFloat("ty",i, (float) (hitlist.get(i).get_tY()/10.));
            bank.setFloat("tz",i, (float) (hitlist.get(i).get_tZ()/10.));
            bank.setFloat("energy",i, (float) hitlist.get(i).Edep());
            bank.setFloat("tlength",i, (float) (hitlist.get(i).tLength()/10.)); // units is cm
            bank.setFloat("pathlength",i, (float) (hitlist.get(i).pathLength()/10.)); // units is cm
            bank.setShort("indexLadc",i, (short) hitlist.get(i).indexLadc());
            bank.setShort("indexRadc",i, (short) hitlist.get(i).indexRadc());
            bank.setShort("indexLtdc",i, (short) hitlist.get(i).indexLtdc());
            bank.setShort("indexRtdc",i, (short) hitlist.get(i).indexRtdc());
            
        }
        return bank;
        
    }
    
    
    
    
    
    
    public DataBank fillClustersBank(DataEvent event, ArrayList<CNDCluster> cluslist) {
        
        if (cluslist == null) {
            return null;
        }
        if (cluslist.size() == 0) {
            return null;
        }
        
        
        DataBank bank =  event.createBank("CND::clusters", cluslist.size());
        if (bank == null) {
            System.err.println("COULD NOT CREATE A CND::clusters BANK!!!!!!");
            return null;
        }
        for(int i =0; i< cluslist.size(); i++) {
            bank.setShort("id",i, (short) cluslist.get(i).get_id() );
            bank.setShort("size",i, (short) cluslist.get(i).get_nhits() );
            bank.setByte("sector",i,  (byte)(1* cluslist.get(i).get_sector()) );
            bank.setByte("layer",i,  (byte)(1*  cluslist.get(i).get_layer()) );
            bank.setShort("component",i, (short) cluslist.get(i).get_component() );
            bank.setFloat("energy",i,   (float)(1.0* cluslist.get(i).get_energysum()) );
            bank.setFloat("x",i,   (float)(1.0* cluslist.get(i).get_x()) );
            bank.setFloat("y",i,   (float)(1.0* cluslist.get(i).get_y()) );
            bank.setFloat("z",i,   (float)(1.0* cluslist.get(i).get_z()) );
            bank.setFloat("time",i,   (float)(1.0*  cluslist.get(i).get_time()) );
            bank.setShort("status",i, (short)  cluslist.get(i).get_status());
            bank.setByte("layermult",i,  (byte) cluslist.get(i).get_layermultip() );
            bank.setByte("layer1",i,  (byte) cluslist.get(i).get_layer1() );
            bank.setByte("layer2",i,  (byte) cluslist.get(i).get_layer2() );
            bank.setByte("layer3",i,  (byte) cluslist.get(i).get_layer3() );
            bank.setFloat("pathLengthThruBar",i, (float)  cluslist.get(i).get_pathLengthThruBar());
        }
        
        return bank;
    }
    
    
    
    
    
    
    
    public void appendCNDBanks(DataEvent event,ArrayList<CndHit> hitlist, ArrayList<CNDCluster> cluslist) {
        if(hitlist.size()!=0){
            DataBank bank = this.fillCndHitBanks((DataEvent) event, hitlist);
            //bank.show();
            event.appendBanks(bank);
            //event.show();
        }
        
        
        if(cluslist.size()!=0){
            DataBank bank1 = this.fillClustersBank((DataEvent) event, cluslist);
            event.appendBanks(bank1);
        }
    }
    
}


