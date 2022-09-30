package org.jlab.rec.ahdc.Banks;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.ahdc.Cluster.Cluster;
import org.jlab.rec.ahdc.Hit.Hit;
import org.jlab.rec.ahdc.PreCluster.PreCluster;
import org.jlab.rec.ahdc.Track.Track;

import java.util.ArrayList;

public class RecoBankWriter {

	public DataBank fillAHDCHitsBank(DataEvent event, ArrayList<Hit> hitList) {
		if (hitList == null || hitList.size() == 0) return null;

		DataBank bank = event.createBank("AHDC::Hits", hitList.size());

		for (int i = 0; i < hitList.size(); i++) {

			bank.setShort("ID", i, (short) hitList.get(i).getId());
			bank.setByte("layer", i, (byte) hitList.get(i).getLayerId());
			bank.setByte("superlayer", i, (byte) hitList.get(i).getSuperLayerId());
			bank.setInt("wire", i, hitList.get(i).getWireId());
			bank.setDouble("Doca", i, hitList.get(i).getDoca());
		}

		return bank;
	}

	public DataBank fillPreClustersBank(DataEvent event, ArrayList<PreCluster> preClusters) {
		if (preClusters == null || preClusters.size() == 0) return null;

		DataBank bank = event.createBank("AHDC::PreClusters", preClusters.size());

		for (int i = 0; i < preClusters.size(); i++) {
			bank.setFloat("X", i, (float) preClusters.get(i).get_X());
			bank.setFloat("Y", i, (float) preClusters.get(i).get_Y());
		}

		return bank;
	}

	public DataBank fillClustersBank(DataEvent event, ArrayList<Cluster> clusters) {
		if (clusters == null || clusters.size() == 0) return null;

		DataBank bank = event.createBank("AHDC::Clusters", clusters.size());

		for (int i = 0; i < clusters.size(); i++) {
			bank.setFloat("X", i, (float) clusters.get(i).get_X());
			bank.setFloat("Y", i, (float) clusters.get(i).get_Y());
			bank.setFloat("Z", i, (float) clusters.get(i).get_Z());
		}

		return bank;
	}


	public DataBank fillAHDCMCTrackBank(DataEvent event) {

		DataBank particle = event.getBank("MC::Particle");
		double   x_mc     = particle.getFloat("vx", 0);
		double   y_mc     = particle.getFloat("vy", 0);
		double   z_mc     = particle.getFloat("vz", 0);
		double   px_mc    = particle.getFloat("px", 0) * 1000;
		double   py_mc    = particle.getFloat("py", 0) * 1000;
		double   pz_mc    = particle.getFloat("pz", 0) * 1000;

		int      row  = 0;
		DataBank bank = event.createBank("AHDC::MC", row + 1);
		bank.setFloat("x", row, (float) x_mc);
		bank.setFloat("y", row, (float) y_mc);
		bank.setFloat("z", row, (float) z_mc);
		bank.setFloat("px", row, (float) px_mc);
		bank.setFloat("py", row, (float) py_mc);
		bank.setFloat("pz", row, (float) pz_mc);

		return bank;
	}

	public DataBank fillAHDCTrackBank(DataEvent event, ArrayList<Track> tracks) {

		DataBank bank = event.createBank("AHDC::Track", tracks.size());

		int row = 0;

		for (Track track : tracks) {
			if (track == null) continue;
			double x  = track.get_X0();
			double y  = track.get_Y0();
			double z  = track.get_Z0();
			double px = track.get_px();
			double py = track.get_py();
			double pz = track.get_pz();

			bank.setFloat("x", row, (float) x);
			bank.setFloat("y", row, (float) y);
			bank.setFloat("z", row, (float) z);
			bank.setFloat("px", row, (float) px);
			bank.setFloat("py", row, (float) py);
			bank.setFloat("pz", row, (float) pz);

			row++;
		}

		return bank;
	}

	public DataBank fillAHDCKFTrackBank(DataEvent event, ArrayList<Track> tracks) {

		DataBank bank = event.createBank("AHDC::KFTrack", tracks.size());

		int row = 0;

		for (Track track : tracks) {
			if (track == null) continue;
			double x  = track.getX0_kf();
			double y  = track.getY0_kf();
			double z  = track.getZ0_kf();
			double px = track.getPx0_kf();
			double py = track.getPy0_kf();
			double pz = track.getPz0_kf();

			bank.setFloat("x", row, (float) x);
			bank.setFloat("y", row, (float) y);
			bank.setFloat("z", row, (float) z);
			bank.setFloat("px", row, (float) px);
			bank.setFloat("py", row, (float) py);
			bank.setFloat("pz", row, (float) pz);

			row++;
		}

		return bank;
	}
}
