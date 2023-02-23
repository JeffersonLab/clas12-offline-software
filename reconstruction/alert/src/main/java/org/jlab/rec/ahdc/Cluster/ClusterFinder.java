package org.jlab.rec.ahdc.Cluster;

import org.jlab.rec.ahdc.PreCluster.PreCluster;

import java.util.ArrayList;
import java.util.List;

public class ClusterFinder {

	private final ArrayList<Cluster> _AHDCClusters                 = new ArrayList<>();
	private final ArrayList<Cluster> _list_with_maybe_same_cluster = new ArrayList<>();

	public ClusterFinder() {}

	private void find_associate_cluster(PreCluster precluster, List<PreCluster> AHDC_precluster_list, int window, int minimal_distance, int super_layer, int layer, int associate_super_layer) {
		if (precluster.get_Super_layer() == super_layer && precluster.get_Layer() == layer && !precluster.is_Used()) {
			ArrayList<PreCluster> possible_precluster_list = new ArrayList<>();

			double phi_mean = precluster.get_Phi() + 0.1 * Math.pow(-1, precluster.get_Super_layer());
			double x        = -precluster.get_Radius() * Math.sin(phi_mean);
			double y        = -precluster.get_Radius() * Math.cos(phi_mean);
			for (PreCluster other_precluster : AHDC_precluster_list) {
				if (other_precluster.get_Super_layer() == associate_super_layer && other_precluster.get_Layer() == 0 && !other_precluster.is_Used()) {
					double x_start = x - window;
					double x_end   = x + window;
					double y_start = y - window;
					double y_end   = y + window;
					if (other_precluster.get_X() > x_start && other_precluster.get_X() < x_end && other_precluster.get_Y() > y_start && other_precluster.get_Y() < y_end) {
						possible_precluster_list.add(other_precluster);
					}
				}
			}

			if (possible_precluster_list.size() > 0) {
				double     distance_min    = Double.MAX_VALUE;
				PreCluster best_precluster = null;
				for (PreCluster possible_precluster : possible_precluster_list) {
					double distance      = Math.sqrt((x - possible_precluster.get_X()) * (x - possible_precluster.get_X()) + (y - possible_precluster.get_Y()) * (y - possible_precluster.get_Y()));
					double distance_real = Math.sqrt((precluster.get_X() - possible_precluster.get_X()) * (precluster.get_X() - possible_precluster.get_X()) + (precluster.get_Y() - possible_precluster.get_Y()) * (precluster.get_Y() - possible_precluster.get_Y()));
					if (distance < distance_min && distance_real > minimal_distance) {
						distance_min    = distance;
						best_precluster = possible_precluster;
					}
				}
				if (best_precluster != null) {
					precluster.set_Used(true);
					best_precluster.set_Used(true);
					Cluster new_Cluster = new Cluster(precluster, best_precluster);
					_list_with_maybe_same_cluster.add(new_Cluster);
				}
			}
		}
	}

	public void findCluster(List<PreCluster> AHDC_precluster_list) {
		int window           = 30;
		int minimal_distance = 10;

		for (PreCluster precluster : AHDC_precluster_list) {
			for (PreCluster other_precluster : AHDC_precluster_list) {
				if (precluster.get_Phi() == 0.0 && 6.0 < other_precluster.get_Phi() && other_precluster.get_Phi() < 2 * Math.PI) {
					precluster.set_Phi(2 * Math.PI);
				}
			}
		}

		// Collections.sort(AHDC_precluster_list);

		for (PreCluster precluster : AHDC_precluster_list) {
			find_associate_cluster(precluster, AHDC_precluster_list, window, minimal_distance, 0, 0, 1);
			find_associate_cluster(precluster, AHDC_precluster_list, window, minimal_distance, 1, 1, 2);
			find_associate_cluster(precluster, AHDC_precluster_list, window, minimal_distance, 2, 1, 3);
			find_associate_cluster(precluster, AHDC_precluster_list, window, minimal_distance, 3, 1, 4);
		}

		for (Cluster cluster : _list_with_maybe_same_cluster) {
			if (!containsCluster(_AHDCClusters, cluster.get_Phi(), cluster.get_Radius())) {
				_AHDCClusters.add(cluster);
			}
		}
	}

	public boolean containsCluster(final List<Cluster> list, double phi, double radius) {
		return list.stream().anyMatch(o -> o.get_Radius() == (radius) && o.get_Phi() == phi);
	}

	public ArrayList<Cluster> get_AHDCClusters() {
		return _AHDCClusters;
	}

}



