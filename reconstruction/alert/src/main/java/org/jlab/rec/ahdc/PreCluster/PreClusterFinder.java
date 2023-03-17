package org.jlab.rec.ahdc.PreCluster;

import org.jlab.rec.ahdc.Hit.Hit;

import java.util.ArrayList;
import java.util.List;

public class PreClusterFinder {

	private ArrayList<PreCluster> _AHDCPreClusters;

	public PreClusterFinder() {
		_AHDCPreClusters = new ArrayList<>();
	}

	private void fill_list(List<Hit> AHDC_hits, ArrayList<Hit> sxlx, int super_layer, int layer) {
		for (Hit hit : AHDC_hits) {
			if (hit.getSuperLayerId() == super_layer && hit.getLayerId() == layer) {
				sxlx.add(hit);
			}
		}
	}

	public void findPreCluster(List<Hit> AHDC_hits) {
		ArrayList<Hit> s0l0 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s0l0, 0, 0);
		ArrayList<Hit> s1l0 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s1l0, 1, 0);
		ArrayList<Hit> s1l1 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s1l1, 1, 1);
		ArrayList<Hit> s2l0 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s2l0, 2, 0);
		ArrayList<Hit> s2l1 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s2l1, 2, 1);
		ArrayList<Hit> s3l0 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s3l0, 3, 0);
		ArrayList<Hit> s3l1 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s3l1, 3, 1);
		ArrayList<Hit> s4l0 = new ArrayList<Hit>();
		fill_list(AHDC_hits, s4l0, 4, 0);

		ArrayList<ArrayList<Hit>> all_super_layer = new ArrayList<>();
		all_super_layer.add(s0l0);
		all_super_layer.add(s1l0);
		all_super_layer.add(s1l1);
		all_super_layer.add(s2l0);
		all_super_layer.add(s2l1);
		all_super_layer.add(s3l0);
		all_super_layer.add(s3l1);
		all_super_layer.add(s4l0);

		for (ArrayList<Hit> sxlx : all_super_layer) {
			for (Hit hit : sxlx) {
				if (hit.is_NoUsed()) {
					ArrayList<Hit> temp_list = new ArrayList<>();
					temp_list.add(hit);
					hit.setUse(true);
					int expected_wire_plus  = hit.getWireId() + 1;
					int expected_wire_minus = hit.getWireId() - 1;
					if (hit.getWireId() - 1 == 0) {
						expected_wire_minus = hit.getNbOfWires();
					}
					if (hit.getWireId() + 1 == hit.getNbOfWires() + 1) {
						expected_wire_plus = 1;
					}

					boolean already_use = false;
					for (Hit hit1 : sxlx) {
						if (hit1.getWireId() == hit.getWireId() && hit1.is_NoUsed()) {
							temp_list.add(hit1);
							hit1.setUse(false);
						}
						if (hit1.getDoca() < 0.6 || hit.getDoca() < 0.6) {continue;}

						if ((hit1.getWireId() == expected_wire_minus || hit1.getWireId() == expected_wire_plus) && ((hit.getDoca() > 1.7 && hit1.getDoca() > 1.7) || hit1.getDoca() > 2.6 || hit.getDoca() > 2.6) && hit1.is_NoUsed() && !already_use) {
							temp_list.add(hit1);
							already_use = true;
							hit1.setUse(true);
						}
					}
					if (!temp_list.isEmpty()) {
						_AHDCPreClusters.add(new PreCluster(temp_list));
					}
				}
			}
		}
	}


	public ArrayList<PreCluster> get_AHDCPreClusters() {
		return _AHDCPreClusters;
	}

	public void set_AHDCPreClusters(ArrayList<PreCluster> _AHDCPreClusters) {
		this._AHDCPreClusters = _AHDCPreClusters;
	}
}
