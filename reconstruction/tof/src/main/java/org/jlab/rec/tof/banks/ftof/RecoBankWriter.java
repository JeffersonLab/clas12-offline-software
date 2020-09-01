package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.data.HipoGroup;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.hit.ftof.Hit;

/**
 *
 * @author ziegler
 *
 */
public class RecoBankWriter {

    public RecoBankWriter() {
        // TODO Auto-generated constructor stub
    }
    public DataBank CreateOutputBank(DataEvent event, String bankName, int bankSize) {
        if(event.hasBank(bankName)) { // for second pass tracking
            HipoDataEvent de = (HipoDataEvent) event;
            //HipoEvent dde = de.getHipoEvent();
            //HipoGroup group = dde.getGroup(bankName);
            ////event.show();
            //group.show();
            //dde.removeGroup(bankName);
            return null;
        }
        DataBank bank = event.createBank(bankName, bankSize);
        return bank;
    }
    public DataBank fillRawHitsBank(DataEvent event, List<Hit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }
        if(event.hasBank("FTOF::rawhits"))
            return null; // don't save again for TB
        
        DataBank bank = event.createBank("FTOF::rawhits", hitlist.size());
        //DataBank bank = this.CreateOutputBank(event, "FTOF::rawhits", hitlist.size());
        if (bank == null) {
            System.err
                    .println("COULD NOT CREATE A BANK!!!!!! for hitlist of size "
                            + hitlist.size());
            return null;
        }
        for (int i = 0; i < hitlist.size(); i++) {
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Panel());
            bank.setShort("component", i, (short) hitlist.get(i).get_Paddle());
            int status = 0;
            if (Integer.parseInt(hitlist.get(i).get_StatusWord()) == 1111) {
                status = 1;
            }
            bank.setShort("status", i, (short) status);
            bank.setFloat("energy_left", i, (float) hitlist.get(i)
                    .get_Energy1());
            bank.setFloat("energy_right", i, (float) hitlist.get(i)
                    .get_Energy2());
            bank.setFloat("energy_left_unc", i, (float) hitlist.get(i)
                    .get_Energy1Unc());
            bank.setFloat("energy_right_unc", i, (float) hitlist.get(i)
                    .get_Energy2Unc());
            bank.setFloat("time_left", i, (float) hitlist.get(i).get_t1());
            bank.setFloat("time_right", i, (float) hitlist.get(i).get_t2());
            bank.setFloat("time_left_unc", i, (float) hitlist.get(i)
                    .get_t1Unc());
            bank.setFloat("time_right_unc", i, (float) hitlist.get(i)
                    .get_t2Unc());
        }

        return bank;

    }

    public DataBank fillRecHitsBank(DataEvent event, List<Hit> hitlist, String hitsType) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }
        String bankName = "FTOF::hits";
        if(hitsType.equalsIgnoreCase("FTOFHB"))
            bankName = "FTOF::hbhits";
        DataBank bank = event.createBank(bankName, hitlist.size());
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!"+bankName);
            return null;
        }
        for (int i = 0; i < hitlist.size(); i++) {
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Panel());
            bank.setShort("component", i, (short) hitlist.get(i).get_Paddle());
            int status = 0;
            if (Integer.parseInt(hitlist.get(i).get_StatusWord()) == 1111) {
                status = 1;
            }
            bank.setShort("status", i, (short) status);
            bank.setFloat("energy", i, (float) hitlist.get(i).get_Energy());
            bank.setFloat("energy_unc", i, (float) hitlist.get(i)
                    .get_EnergyUnc());
            bank.setFloat("time", i, (float) hitlist.get(i).get_t());
            bank.setFloat("time_unc", i, (float) hitlist.get(i).get_tUnc());
            bank.setFloat("x", i, (float) hitlist.get(i).get_Position().x());
            bank.setFloat("y", i, (float) hitlist.get(i).get_Position().y());
            bank.setFloat("z", i, (float) hitlist.get(i).get_Position().z());
            if (hitlist.get(i).get_TrkPosition() != null
                    && hitlist.get(i).get_TrkPosition().z() != 0) {
                bank.setFloat("tx", i, (float) hitlist.get(i).get_TrkPosition()
                        .x());
                bank.setFloat("ty", i, (float) hitlist.get(i).get_TrkPosition()
                        .y());
                bank.setFloat("tz", i, (float) hitlist.get(i).get_TrkPosition()
                        .z());
                bank.setShort("trackid", i,
                        (short) hitlist.get(i).get_TrkId());
            } else {
                bank.setShort("trackid", i, (short) -1);
            }
            bank.setFloat("x_unc", i, 5);
            bank.setFloat("y_unc", i, (float) hitlist.get(i).get_yUnc());
            bank.setFloat("z_unc", i, 10);
            bank.setShort("adc_idx1", i, (short) hitlist.get(i)
                    .get_ADCbankHitIdx1());
            bank.setShort("adc_idx2", i, (short) hitlist.get(i)
                    .get_ADCbankHitIdx2());
            bank.setShort("tdc_idx1", i, (short) hitlist.get(i)
                    .get_TDCbankHitIdx1());
            bank.setShort("tdc_idx2", i, (short) hitlist.get(i)
                    .get_TDCbankHitIdx2());
            bank.setShort("clusterid", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setFloat("pathLength", i, (float) hitlist.get(i)
                    .get_TrkPathLen());
            bank.setFloat("pathLengthThruBar", i, (float) hitlist.get(i)
                    .get_TrkPathLenThruBar());
        }
        // bank.show();
        return bank;

    }

    public DataBank fillClustersBank(DataEvent event, List<Cluster> cluslist, String hitsType) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.size() == 0) {
            return null;
        }
        if(hitsType.equalsIgnoreCase("FTOFHB")) {
            return null;
        } else {
            // save clusters only for TB
            DataBank bank = event.createBank("FTOF::clusters", cluslist.size());

            //DataBank bank = this.CreateOutputBank(event, "FTOF::clusters", cluslist.size());
            if (bank == null) {
                System.err.println("COULD NOT CREATE A BANK!!!!!!");
                return null;
            }
            for (int i = 0; i < cluslist.size(); i++) {
                bank.setShort("id", i, (short) cluslist.get(i).get_Id());
                bank.setShort("trackid", i, (short) cluslist.get(i).get(0).get_TrkId());
                bank.setShort("size", i, (short) cluslist.get(i).size());
                bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());
                bank.setByte("layer", i, (byte) cluslist.get(i).get_Panel());
                bank.setShort("component", i, (short) cluslist.get(i).get(0)
                        .get_Paddle()); // paddle id of the cluster seed
                int status = 0;
                if (Integer.parseInt(cluslist.get(i).get_StatusWord()) == 1111) {
                    status = 1;
                }
                bank.setShort("status", i, (short) status);
                bank.setFloat("energy", i, (float) cluslist.get(i).get_Energy());
                bank.setFloat("energy_unc", i, (float) cluslist.get(i)
                        .get_EnergyUnc());
                bank.setFloat("time", i, (float) cluslist.get(i).get_t());
                bank.setFloat("time_unc", i, (float) cluslist.get(i).get_tUnc());
                bank.setFloat("x", i, (float) cluslist.get(i).get_x());
                bank.setFloat("y", i, (float) cluslist.get(i).get_y());
                bank.setFloat("z", i, (float) cluslist.get(i).get_z());
                bank.setFloat("pathLengthThruBar", i, (float) cluslist.get(i).get_PathLengthThruBar());										
            }

            return bank;
            }
    }

    private DataBank fillClustersBank(DataEvent event,
            ArrayList<ArrayList<Cluster>> matchedClusters, String hitsType) {
        if (matchedClusters == null) {
            return null;
        }
        if (matchedClusters.size() == 0) {
            return null;
        }
        if(hitsType.equalsIgnoreCase("FTOFHB")) {
            return null;
        } else {
            // save clusters only for TB
            DataBank bank = event.createBank("FTOF::matchedclusters",
                matchedClusters.size());
            if (bank == null) {
                System.err.println("COULD NOT CREATE A BANK!!!!!!");
                return null;
            }
            for (int i = 0; i < matchedClusters.size(); i++) {
                if (matchedClusters.get(i) == null) {
                    continue;
                }
                bank.setByte("sector", i, (byte) matchedClusters.get(i).get(0)
                        .get_Sector());
                bank.setShort("paddle_id1A", i,
                        (short) matchedClusters.get(i).get(0).get(0).get_Paddle()); // paddle
                // id
                // of
                // hit
                // with
                // lowest
                // paddle
                // id
                // in
                // cluster
                // [Check
                // the
                // sorting!!!]
                bank.setShort("paddle_id1B", i,
                        (short) matchedClusters.get(i).get(1).get(0).get_Paddle()); // paddle
                // id
                // of
                // hit
                // with
                // lowest
                // paddle
                // id
                // in
                // cluster
                // [Check
                // the
                // sorting!!!]
                bank.setShort("clusSize_1A", i,
                        (short) matchedClusters.get(i).get(0).size()); // size of
                // cluster
                // in 1a
                bank.setShort("clusSize_1B", i,
                        (short) matchedClusters.get(i).get(1).size()); // size of
                // cluster
                // in 1b
                bank.setShort("clus_1Aid", i, (short) matchedClusters.get(i).get(0)
                        .get_Id()); // id of cluster in 1a
                bank.setShort("clus_1Bid", i, (short) matchedClusters.get(i).get(1)
                        .get_Id()); // id of cluster in 1b
                bank.setFloat("tminAlgo_1B_tCorr", i, (float) matchedClusters
                        .get(i).get(1).get_tCorr()[0]); // uses tmin algorithm to
                // compute the path length
                // between counters
                bank.setFloat("midbarAlgo_1B_tCorr", i, (float) matchedClusters
                        .get(i).get(1).get_tCorr()[1]); // uses middle of bar
                // algorithm to compute the
                // path length between
                // counters
                bank.setFloat("EmaxAlgo_1B_tCorr", i, (float) matchedClusters
                        .get(i).get(1).get_tCorr()[2]); // uses Emax algorithm to
                // compute the path length
                // between counters
            }
            return bank;
        }
    }

    public void appendFTOFBanks(DataEvent event, List<Hit> hits,
            List<Cluster> clusters,
            ArrayList<ArrayList<Cluster>> matchedClusters, String hitsType) {
        List<DataBank> fTOFBanks = new ArrayList<DataBank>();

        DataBank bank1 = this.fillRawHitsBank((DataEvent) event, hits);
        if (bank1 != null) {
            fTOFBanks.add(bank1);
        }

        DataBank bank2 = this.fillRecHitsBank((DataEvent) event, hits, hitsType);
        if (bank2 != null) {
            fTOFBanks.add(bank2);
        }

        DataBank bank3 = this.fillClustersBank((DataEvent) event, clusters, hitsType);
        if (bank3 != null) {
            fTOFBanks.add(bank3);
        }

        DataBank bank4 = this.fillClustersBank((DataEvent) event,
                matchedClusters, hitsType);
        if (bank4 != null) {
            fTOFBanks.add(bank4);
        }

        if (fTOFBanks.size() == 4) {
            event.appendBanks(fTOFBanks.get(0), fTOFBanks.get(1),
                    fTOFBanks.get(2), fTOFBanks.get(3));
        }
        if (fTOFBanks.size() == 3) {
            event.appendBanks(fTOFBanks.get(0), fTOFBanks.get(1),
                    fTOFBanks.get(2));
        }
        if (fTOFBanks.size() == 2) {
            event.appendBanks(fTOFBanks.get(0), fTOFBanks.get(1));
        }
        if (fTOFBanks.size() == 1) {
            event.appendBanks(fTOFBanks.get(0));
        }

    }

}
