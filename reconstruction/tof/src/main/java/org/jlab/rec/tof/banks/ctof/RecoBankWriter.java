package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.hit.ctof.Hit;

/**
 *
 * @author ziegler
 *
 */
public class RecoBankWriter {

    public RecoBankWriter() {
        // TODO Auto-generated constructor stub
    }

    public DataBank fillRawHitsBank(DataEvent event, List<Hit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("CTOF::rawhits", hitlist.size());
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!! for hitlist of size " + hitlist.size());
            return null;
        }

        for (int i = 0; i < hitlist.size(); i++) {
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setShort("component", i, (short) hitlist.get(i).get_Paddle());
            int status = 0;
            if (Integer.parseInt(hitlist.get(i).get_StatusWord()) == 1111) {
                status = 1;
            }
            bank.setShort("status", i, (short) status);
            bank.setFloat("energy_up", i, (float) hitlist.get(i).get_Energy1());
            bank.setFloat("energy_down", i, (float) hitlist.get(i).get_Energy2());
            bank.setFloat("energy_up_unc", i, (float) hitlist.get(i).get_Energy1Unc());
            bank.setFloat("energy_down_unc", i, (float) hitlist.get(i).get_Energy2Unc());
            bank.setFloat("time_up", i, (float) hitlist.get(i).get_t1());
            bank.setFloat("time_down", i, (float) hitlist.get(i).get_t2());
            bank.setFloat("time_up_unc", i, (float) hitlist.get(i).get_t1Unc());
            bank.setFloat("time_down_unc", i, (float) hitlist.get(i).get_t2Unc());
        }

        return bank;

    }

    public DataBank fillRecHitsBank(DataEvent event, List<Hit> hitlist) {
        if (hitlist == null) {
            return null;
        }
        if (hitlist.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("CTOF::hits", hitlist.size());
        if (bank == null) {
            System.err.println("COULD NOT CREATE A BANK!!!!!!");
            return null;
        }
        for (int i = 0; i < hitlist.size(); i++) {
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Panel());
            bank.setShort("component", i, (short) hitlist.get(i).get_Paddle());
            int status = 0;
//            if (Integer.parseInt(hitlist.get(i).get_StatusWord()) == 1111) {
//                status = 1;
//            }
            status = Integer.parseInt(hitlist.get(i).get_StatusWord());
            bank.setShort("status", i, (short) status);
            bank.setFloat("energy", i, (float) hitlist.get(i).get_Energy());
            bank.setFloat("energy_unc", i, (float) hitlist.get(i).get_EnergyUnc());
            bank.setFloat("time", i, (float) hitlist.get(i).get_t());
            bank.setFloat("time_unc", i, (float) hitlist.get(i).get_tUnc());
            bank.setFloat("x", i, (float) hitlist.get(i).get_Position().x());
            bank.setFloat("y", i, (float) hitlist.get(i).get_Position().y());
            bank.setFloat("z", i, (float) hitlist.get(i).get_Position().z());
            if (hitlist.get(i).get_TrkPosition() != null && hitlist.get(i).get_TrkPosition().z() != 0) {
                bank.setFloat("tx", i, (float) hitlist.get(i).get_TrkPosition().x());
                bank.setFloat("ty", i, (float) hitlist.get(i).get_TrkPosition().y());
                bank.setFloat("tz", i, (float) hitlist.get(i).get_TrkPosition().z());
                bank.setShort("trkID", i, (short) hitlist.get(i).get_TrkId());
            } else {
                bank.setShort("trkID", i, (short) -1);
            }
            bank.setFloat("x_unc", i, 5);
            bank.setFloat("y_unc", i, (float) hitlist.get(i).get_yUnc());
            bank.setFloat("z_unc", i, 10);
            bank.setShort("adc_idx1",i, (short) hitlist.get(i).get_ADCbankHitIdx1()); 		
            bank.setShort("adc_idx2",i, (short) hitlist.get(i).get_ADCbankHitIdx2()); 		
            bank.setShort("tdc_idx1",i, (short) hitlist.get(i).get_TDCbankHitIdx1()); 		
            bank.setShort("tdc_idx2",i, (short) hitlist.get(i).get_TDCbankHitIdx2()); 
            bank.setShort("clusterid", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setFloat("pathLength", i, (float) hitlist.get(i)
                    .get_TrkPathLen());
            bank.setFloat("pathLengthThruBar", i, (float) hitlist.get(i)
                    .get_TrkPathLenThruBar());
        }
        //bank.show();
        return bank;

    }

    public DataBank fillClustersBank(DataEvent event, List<Cluster> cluslist) {
        if (cluslist == null) {
            return null;
        }
        if (cluslist.size() == 0) {
            return null;
        }

        DataBank bank = event.createBank("CTOF::clusters", cluslist.size());
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
            bank.setShort("component", i, (short) cluslist.get(i).get(0).get_Paddle()); // paddle id of cluster seed
            int status = 0;
            if (Integer.parseInt(cluslist.get(i).get_StatusWord()) == 1111) {
                status = 1;
            }
            bank.setShort("status", i, (short) status);
            bank.setFloat("energy", i, (float) cluslist.get(i).get_Energy());
            bank.setFloat("energy_unc", i, (float) cluslist.get(i).get_EnergyUnc());
            bank.setFloat("time", i, (float) cluslist.get(i).get(0).get_t());
            bank.setFloat("time_unc", i, (float) cluslist.get(i).get(0).get_tUnc());
            bank.setFloat("x", i, (float) cluslist.get(i).get(0).get_Position().x());
            bank.setFloat("y", i, (float) cluslist.get(i).get(0).get_Position().y());
            bank.setFloat("z", i, (float) cluslist.get(i).get(0).get_Position().z());										
            bank.setFloat("pathLengthThruBar", i, (float) cluslist.get(i).get_PathLengthThruBar());										
        }

        return bank;

    }

    public void appendCTOFBanks(DataEvent event,
            List<Hit> hits, List<Cluster> clusters) {
        List<DataBank> cTOFBanks = new ArrayList<DataBank>();

        DataBank bank1 = this.fillRawHitsBank((DataEvent) event, hits);
        if (bank1 != null) {
            cTOFBanks.add(bank1);
        }

        DataBank bank2 = this.fillRecHitsBank((DataEvent) event, hits);
        if (bank2 != null) {
            cTOFBanks.add(bank2);
        }

        DataBank bank3 = this.fillClustersBank((DataEvent) event, clusters);
        if (bank3 != null) {
            cTOFBanks.add(bank3);
        }

        if (cTOFBanks.size() == 3) {
            event.appendBanks(cTOFBanks.get(0), cTOFBanks.get(1), cTOFBanks.get(2));
        }
        if (cTOFBanks.size() == 2) {
            event.appendBanks(cTOFBanks.get(0), cTOFBanks.get(1));
        }
        if (cTOFBanks.size() == 1) {
            event.appendBanks(cTOFBanks.get(0));
        }

    }
}
