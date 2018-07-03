/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fmt.banks;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.cross.Cross;
import org.jlab.rec.fmt.hit.FittedHit;
/**
 *
 * @author ziegler
 */
public class RecoBankWriter {
    
        public DataBank fillFMTHitsBank(DataEvent event, List<FittedHit> hitlist) {
        
        DataBank bank
                = event.createBank("FMTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {

            bank.setShort("ID", i, (short) hitlist.get(i).get_Id());

            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setInt("strip", i, hitlist.get(i).get_Strip());

            bank.setFloat("fitResidual", i, (float) hitlist.get(i).get_Residual());
            bank.setInt("trkingStat", i, hitlist.get(i).get_TrkgStatus());

            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setShort("trkID", i, (short) hitlist.get(i).get_AssociatedTrackID());

        }

        return bank;

    }

    /**
     *
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillFMTClustersBank(DataEvent event, List<Cluster> cluslist) {

        DataBank bank = event.createBank("FMTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            bank.setShort("ID", i, (short) cluslist.get(i).get_Id());
            bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) cluslist.get(i).get_Layer());
            bank.setShort("size", i, (short) cluslist.get(i).size());
            bank.setFloat("ETot", i, (float) cluslist.get(i).get_TotalEnergy());
            bank.setInt("seedStrip", i, cluslist.get(i).get_SeedStrip());
            bank.setFloat("centroid", i, (float) cluslist.get(i).get_Centroid());
            bank.setFloat("seedE", i, (float) cluslist.get(i).get_SeedEnergy());
           // bank.setFloat("centroidResidual", i, (float) cluslist.get(i).get_CentroidResidual());
           // bank.setFloat("seedResidual", i, (float) cluslist.get(i).get_SeedResidual()); 
            bank.setShort("trkID", i, (short) cluslist.get(i).get_AssociatedTrackID());

            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
                }
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }

        }

        return bank;

    }

    /**
     *
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public DataBank fillFMTCrossesBank(DataEvent event, List<Cross> crosses) {
        
        DataBank bank = event.createBank("FMTRec::Crosses", crosses.size());

        int index = 0;
        
        for (int j = 0; j < crosses.size(); j++) {
            bank.setShort("ID", index, (short) crosses.get(j).get_Id());
            bank.setByte("sector", index, (byte) crosses.get(j).get_Sector());
            bank.setByte("region", index, (byte) crosses.get(j).get_Region());
            bank.setFloat("x", index, (float) crosses.get(j).get_Point().x());
            bank.setFloat("y", index, (float) crosses.get(j).get_Point().y());
            bank.setFloat("z", index, (float) crosses.get(j).get_Point().z());
            bank.setFloat("err_x", index, (float) crosses.get(j).get_PointErr().x());
            bank.setFloat("err_y", index, (float) crosses.get(j).get_PointErr().y());
            bank.setFloat("err_z", index, (float) crosses.get(j).get_PointErr().z());
            bank.setShort("trkID", index, (short) crosses.get(j).get_AssociatedTrackID());

            if (crosses.get(j).get_Dir() != null) {
                bank.setFloat("ux", index, (float) crosses.get(j).get_Dir().x());
                bank.setFloat("uy", index, (float) crosses.get(j).get_Dir().y());
                bank.setFloat("uz", index, (float) crosses.get(j).get_Dir().z());
            } else {
                bank.setFloat("ux", index, 0);
                bank.setFloat("uy", index, 0);
                bank.setFloat("uz", index, 0);
            }
            if (crosses.get(j).get_Cluster1() != null) {
                bank.setShort("Cluster1_ID", index, (short) crosses.get(j).get_Cluster1().get_Id());
            }
            if (crosses.get(j).get_Cluster2() != null) {
                bank.setShort("Cluster2_ID", index, (short) crosses.get(j).get_Cluster2().get_Id());
            }
            index++;
        }

        //bank.show();
        return bank;

    }
    
	
    public void appendFMTBanks(DataEvent event, List<FittedHit> fhits, List<Cluster> clusters,
            List<Cross> crosses) {

        if (event == null) {
            return;
        }

        if (crosses != null && crosses.size()>0) {
            event.appendBanks(this.fillFMTHitsBank(event, fhits),
                    this.fillFMTClustersBank(event, clusters),
                    this.fillFMTCrossesBank(event, crosses));
        }
        if (crosses == null || crosses.size()==0 && (clusters != null && clusters.size()>0)) {
            event.appendBanks(this.fillFMTHitsBank(event, fhits),
                    this.fillFMTClustersBank(event, clusters));
        }

        if (fhits != null && (clusters == null || clusters.size()==0) ) {
            event.appendBanks(this.fillFMTHitsBank(event, fhits));
        }
    }

	
}
