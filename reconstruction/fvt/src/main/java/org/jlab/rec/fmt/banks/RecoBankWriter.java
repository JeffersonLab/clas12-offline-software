package org.jlab.rec.fmt.banks;

import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.Constants;
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
        DataBank bank = event.createBank("FMT::Hits", hitlist.size());

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

        DataBank bank = event.createBank("FMT::Clusters", cluslist.size());

        for (int i = 0; i < cluslist.size(); i++) {

            bank.setShort("index",            i, (short) cluslist.get(i).get_Index());
            bank.setByte( "sector",           i, (byte)  cluslist.get(i).get_Sector());
            bank.setByte( "layer",            i, (byte)  cluslist.get(i).get_Layer());
            bank.setShort("size",             i, (short) cluslist.get(i).size());
            bank.setFloat("centroid",         i, (float) cluslist.get(i).get_Centroid());
            bank.setFloat("centroidError",    i, (float) cluslist.get(i).get_CentroidError());
            bank.setFloat("residual",         i, (float) cluslist.get(i).get_CentroidResidual());
            bank.setFloat("doca",             i, (float) cluslist.get(i).get_Doca());
            bank.setFloat("energy",           i, (float) cluslist.get(i).get_TotalEnergy());
            bank.setFloat("time",             i, (float) cluslist.get(i).get_Time());
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

        DataBank bank = event.createBank("FMT::Crosses", crosses.size());

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

    private DataBank fillFMTTracksBank(DataEvent event,List<Track> candlist) {

        DataBank bank = event.createBank("FMT::Tracks", candlist.size());

        for (int i=0; i<candlist.size(); i++) {
            bank.setShort("index",  i, (short) candlist.get(i).getIndex());
            bank.setByte( "status", i, (byte)  candlist.get(i).getStatus());
            bank.setByte( "sector", i, (byte)  candlist.get(i).getSector());
            bank.setByte( "q",      i, (byte)  candlist.get(i).getQ());
            bank.setFloat("chi2",   i, (float) candlist.get(i).getChi2());
            bank.setByte( "NDF",    i, (byte)  candlist.get(i).getNDF());
            bank.setFloat("Vtx0_x", i, (float) candlist.get(i).getX());
            bank.setFloat("Vtx0_y", i, (float) candlist.get(i).getY());
            bank.setFloat("Vtx0_z", i, (float) candlist.get(i).getZ());
            bank.setFloat("p0_x",   i, (float) candlist.get(i).getPx());
            bank.setFloat("p0_y",   i, (float) candlist.get(i).getPy());
            bank.setFloat("p0_z",   i, (float) candlist.get(i).getPz());
        }

        return bank;
    }

    private DataBank fillFMTTrajectoryBank(DataEvent event,List<Track> candlist) {

        DataBank bank = event.createBank("FMT::Trajectory", candlist.size()*Constants.FVT_Nlayers);

        int row = 0;
        for (int i=0; i<candlist.size(); i++) {
            Track track = candlist.get(i);
            for (int j=0; j<Constants.FVT_Nlayers; j++) {
                int layer = j+1;
                bank.setShort("index",    row, (short) track.getIndex());
                bank.setByte( "detector", row, (byte)  DetectorType.FMT.getDetectorId());
                bank.setByte( "layer",    row, (byte)  layer);
                if(track.getDCTraj(layer)!=null) {
                    bank.setFloat("dx",       row, (float) track.getDCTraj(layer).getLocalPosition().x());
                    bank.setFloat("dy",       row, (float) track.getDCTraj(layer).getLocalPosition().y());
                    bank.setFloat("dz",       row, (float) track.getDCTraj(layer).getLocalPosition().z());  
                }
//                else {
//                    System.out.println(layer + " " + track.toString());
//                    event.getBank("TimeBasedTrkg::Trajectory").show();
//                }
                if(track.getFMTTraj(layer)!=null) {
                    bank.setFloat("x",        row, (float) track.getFMTTraj(layer).getPosition().x());
                    bank.setFloat("y",        row, (float) track.getFMTTraj(layer).getPosition().y());
                    bank.setFloat("z",        row, (float) track.getFMTTraj(layer).getPosition().z());
                    bank.setFloat("tx",       row, (float) track.getFMTTraj(layer).getDirection().x());
                    bank.setFloat("ty",       row, (float) track.getFMTTraj(layer).getDirection().y());
                    bank.setFloat("tz",       row, (float) track.getFMTTraj(layer).getDirection().z());
                    bank.setFloat("lx",       row, (float) track.getFMTTraj(layer).getLocalPosition().x());
                    bank.setFloat("ly",       row, (float) track.getFMTTraj(layer).getLocalPosition().y());
                    bank.setFloat("lz",       row, (float) track.getFMTTraj(layer).getLocalPosition().z());
                    bank.setFloat("path",     row, (float) track.getFMTTraj(layer).getPath());
                }
                row++;
            }
        }
        return bank;
    }

    public void appendFMTBanks(DataEvent event, List<FittedHit> fhits, List<Cluster> clusters,
                               List<Track> tracks) {

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
            event.appendBanks(this.fillFMTTrajectoryBank(event, tracks));
        }

    }
}
