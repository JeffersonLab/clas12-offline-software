package org.jlab.rec.fmt.banks;

import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.cross.Cross;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.track.Track;

/**
 *
 * @author ziegler
 * @author benkel
 * @author devita
 */
public class RecoBankWriter {

        public static DataBank fillFMTHitsBank(DataEvent event, List<Hit> hitlist) {
        DataBank bank = event.createBank("FMT::Hits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            bank.setByte( "layer",        i, (byte)  hitlist.get(i).getLayer());
            bank.setShort("strip",        i, (short) hitlist.get(i).getStrip());
            bank.setFloat("energy",       i, (float) hitlist.get(i).getEnergy());
            bank.setFloat("time",         i, (float) hitlist.get(i).getTime());
            bank.setFloat("localY",       i, (float) hitlist.get(i).getStripLocalSegment().origin().y());
            bank.setFloat("residual",     i, (float) hitlist.get(i).getResidual());
            bank.setShort("adcIndex",     i, (short) hitlist.get(i).getIndex());
            bank.setShort("clusterIndex", i, (short) hitlist.get(i).getClusterIndex());
            bank.setShort("trackIndex",   i, (short) hitlist.get(i).getTrackIndex());
            bank.setByte( "status",       i, (byte)  hitlist.get(i).getStatus());
        }

        return bank;
    }

    /**
     * @param event
     * @param cluslist the reconstructed list of fitted clusters in the event
     * @return clusters bank
     */
    public static DataBank fillFMTClustersBank(DataEvent event, List<Cluster> cluslist) {

        DataBank bank = event.createBank("FMT::Clusters", cluslist.size());

        for (int i = 0; i < cluslist.size(); i++) {

            bank.setShort("index",            i, (short) cluslist.get(i).getIndex());
            bank.setByte( "layer",            i, (byte)  cluslist.get(i).getLayer());
            bank.setShort("size",             i, (short) cluslist.get(i).size());
            bank.setFloat("centroid",         i, (float) cluslist.get(i).getCentroid());
            bank.setFloat("centroidError",    i, (float) cluslist.get(i).getCentroidError());
            bank.setFloat("residual",         i, (float) cluslist.get(i).getCentroidResidual());
            bank.setFloat("doca",             i, (float) cluslist.get(i).getDoca());
            bank.setFloat("energy",           i, (float) cluslist.get(i).getTotalEnergy());
            bank.setFloat("time",             i, (float) cluslist.get(i).getTime());
            bank.setShort("seedStrip",        i, (short) cluslist.get(i).getSeedStrip());
            bank.setShort("seedIndex",        i, (short) cluslist.get(i).getSeedIndex());
            bank.setShort("trackIndex",       i, (short) cluslist.get(i).getTrackIndex());
        }

        return bank;
    }

    /**
     *
     * @param event
     * @param crosses the reconstructed list of crosses in the event
     * @return crosses bank
     */
    public static DataBank fillFMTCrossesBank(DataEvent event, List<Cross> crosses) {

        DataBank bank = event.createBank("FMT::Crosses", crosses.size());

        int index = 0;
        for (int j = 0; j < crosses.size(); j++) {
            bank.setShort("index",       index, (short) crosses.get(j).getIndex());
            bank.setByte( "region",      index, (byte)  crosses.get(j).getRegion());
            bank.setFloat("x",           index, (float) crosses.get(j).getPoint().x());
            bank.setFloat("y",           index, (float) crosses.get(j).getPoint().y());
            bank.setFloat("z",           index, (float) crosses.get(j).getPoint().z());
            bank.setShort("trkID",       index, (short) crosses.get(j).getTrackIndex());
            bank.setShort("Cluster1_ID", index, (short) crosses.get(j).getCluster1().getIndex());

            index++;
        }

        return bank;
    }

    private static DataBank fillFMTTracksBank(DataEvent event,List<Track> candlist) {

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

    private static DataBank fillFMTTrajectoryBank(DataEvent event,List<Track> candlist) {

        DataBank bank = event.createBank("FMT::Trajectory", candlist.size()*Constants.NLAYERS);

        int row = 0;
        for (int i=0; i<candlist.size(); i++) {
            Track track = candlist.get(i);
            for (int j=0; j<Constants.NLAYERS; j++) {
                int layer = j+1;
                bank.setShort("index",    row, (short) track.getIndex());
                bank.setByte( "detector", row, (byte)  DetectorType.FMT.getDetectorId());
                bank.setByte( "layer",    row, (byte)  layer);
                if(track.getDCTraj(layer)!=null) {
                    bank.setFloat("dx",       row, (float) track.getDCTraj(layer).getLocalPosition().x());
                    bank.setFloat("dy",       row, (float) track.getDCTraj(layer).getLocalPosition().y());
                    bank.setFloat("dz",       row, (float) track.getDCTraj(layer).getLocalPosition().z());  
                }
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

    public static void appendFMTBanks(DataEvent event, List<Hit> fhits, List<Cluster> clusters,
                               List<Track> tracks) {

        if (event == null) return;
        
        if (fhits != null) {
            event.appendBanks(fillFMTHitsBank(event, fhits));
        }        
        
        if (clusters != null && clusters.size()>0) {
            event.appendBanks(fillFMTClustersBank(event, clusters));
        }

//        if (crosses != null && crosses.size() > 0) {
//            event.appendBanks(this.fillFMTCrossesBank(event, crosses));
//        }
        
        if (tracks != null && tracks.size() > 0) {
            event.appendBanks(fillFMTTracksBank(event, tracks));
            event.appendBanks(fillFMTTrajectoryBank(event, tracks));
        }

    }
}
