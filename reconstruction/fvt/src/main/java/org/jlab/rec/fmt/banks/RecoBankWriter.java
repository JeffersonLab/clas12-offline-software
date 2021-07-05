package org.jlab.rec.fmt.banks;

import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.cross.Cross;
import org.jlab.rec.fmt.hit.FittedHit;
import org.jlab.rec.fvt.track.Track;

/**
 *
 * @author ziegler
 */
public class RecoBankWriter {

        public DataBank fillFMTHitsBank(DataEvent event, List<FittedHit> hitlist) {
        DataBank bank = event.createBank("FMTRec::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            bank.setByte( "layer",        i, (byte)  hitlist.get(i).get_Layer());
            bank.setByte( "sector",       i, (byte)  hitlist.get(i).get_Sector());
            bank.setShort("strip",        i, (short) hitlist.get(i).get_Strip());
            bank.setFloat("energy",       i, (float) hitlist.get(i).get_Energy());
            bank.setFloat("time",         i, (float) hitlist.get(i).get_Time());
            bank.setFloat("localY",       i, (float) hitlist.get(i).get_StripLocalSegment().origin().y());
            bank.setFloat("residual",     i, (float) hitlist.get(i).get_Residual());
            bank.setShort("adcIndex",     i, (short) hitlist.get(i).get_Index());
            bank.setShort("clusterIndex", i, (short) hitlist.get(i).get_ClusterIndex());
            bank.setShort("trackIndex",   i, (short) hitlist.get(i).get_TrackIndex());
        }

        return bank;
    }

    /**
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public DataBank fillFMTClustersBank(DataEvent event, List<Cluster> cluslist) {

        DataBank bank = event.createBank("FMTRec::Clusters", cluslist.size());
        int[] hitIdxArray = new int[5];

        for (int i = 0; i < cluslist.size(); i++) {
            for (int j = 0; j < hitIdxArray.length; j++) hitIdxArray[j] = -1;

            bank.setShort("index",            i, (short) cluslist.get(i).get_Index());
            bank.setByte( "sector",           i, (byte)  cluslist.get(i).get_Sector());
            bank.setByte( "layer",            i, (byte)  cluslist.get(i).get_Layer());
            bank.setShort("size",             i, (short) cluslist.get(i).size());
            bank.setFloat("centroid",         i, (float) cluslist.get(i).get_Centroid());
            bank.setFloat("residual",         i, (float) cluslist.get(i).get_CentroidResidual());
            bank.setFloat("doca",             i, (float) cluslist.get(i).get_Doca());
            bank.setFloat("energy",           i, (float) cluslist.get(i).get_TotalEnergy());
            bank.setShort("seedStrip",        i, (short) cluslist.get(i).get_SeedStrip());
            bank.setShort("seedIndex",        i, (short) cluslist.get(i).getSeedIndex());
            bank.setShort("trackIndex",       i, (short) cluslist.get(i).get_TrackIndex());
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
            bank.setShort("index",       index, (short) crosses.get(j).get_Index());
            bank.setByte( "sector",      index, (byte)  crosses.get(j).get_Sector());
            bank.setByte( "region",      index, (byte)  crosses.get(j).get_Region());
            bank.setFloat("x",           index, (float) crosses.get(j).get_Point().x());
            bank.setFloat("y",           index, (float) crosses.get(j).get_Point().y());
            bank.setFloat("z",           index, (float) crosses.get(j).get_Point().z());
            bank.setShort("trkID",       index, (short) crosses.get(j).get_TrackIndex());
            bank.setShort("Cluster1_ID", index, (short) crosses.get(j).get_Cluster1().get_Index());

            index++;
        }

        return bank;
    }

    private DataBank fillFMTTracksBank(DataEvent event, Map<Integer,Track> candlist) {

        DataBank bank = event.createBank("FMTRec::Tracks", candlist.size());

        int i = 0;
        for (int id : candlist.keySet()) {
            bank.setShort("index",  i, (short) candlist.get(id).getIndex());
            bank.setShort("status", i, (short) candlist.get(id).status);
            bank.setByte( "sector", i, (byte)  candlist.get(id).getSector());
            bank.setByte( "q",      i, (byte)  candlist.get(id).getQ());
            bank.setFloat("chi2",   i, (float) candlist.get(id).getChi2());
            bank.setFloat("Vtx0_x", i, (float) candlist.get(id).getX());
            bank.setFloat("Vtx0_y", i, (float) candlist.get(id).getY());
            bank.setFloat("Vtx0_z", i, (float) candlist.get(id).getZ());
            bank.setFloat("p0_x",   i, (float) candlist.get(id).getPx());
            bank.setFloat("p0_y",   i, (float) candlist.get(id).getPy());
            bank.setFloat("p0_z",   i, (float) candlist.get(id).getPz());
            i++;
        }

        return bank;
    }

    public void appendFMTBanks(DataEvent event, List<FittedHit> fhits, List<Cluster> clusters,
                               Map<Integer,Track> tracks) {

        if (event == null) return;
        
        if (fhits != null) {
            event.appendBanks(this.fillFMTHitsBank(event, fhits));
        }        
        
        if (clusters != null && clusters.size()>0) {
            event.appendBanks(this.fillFMTClustersBank(event, clusters));
        }

//        if (crosses != null && crosses.size() > 0) {
//            event.appendBanks(this.fillFMTCrossesBank(event, crosses));
//        }
        
        if (tracks != null && tracks.size() > 0) {
            event.appendBanks(this.fillFMTTracksBank(event, tracks));
        }

    }
}
