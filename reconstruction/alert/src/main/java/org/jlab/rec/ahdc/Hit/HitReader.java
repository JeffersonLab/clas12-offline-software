package org.jlab.rec.ahdc.Hit;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import java.util.ArrayList;

public class HitReader {

	private ArrayList<Hit>     _AHDCHits;
	private ArrayList<TrueHit> _TrueAHDCHits;

	public HitReader(DataEvent event, boolean simulation) {
		fetch_AHDCHits(event);
		if (simulation) fetch_TrueAHDCHits(event);
	}

	public void fetch_AHDCHits(DataEvent event) {
		ArrayList<Hit> hits = new ArrayList<>();

		DataBank bankDGTZ = event.getBank("ALRTDC::adc");

		if (event.hasBank("ALRTDC::adc")) {
			for (int i = 0; i < bankDGTZ.rows(); i++) {
				int    id         = i + 1;
				int    number     = bankDGTZ.getByte("layer", i);
				int    layer      = number % 10;
				int    superlayer = (int) (number % 100) / 10;
				int    wire       = bankDGTZ.getShort("component", i);
				double doca       = bankDGTZ.getShort("ped", i) / 1000.0;

				hits.add(new Hit(id, superlayer, layer, wire, doca));
			}
		}
		this.set_AHDCHits(hits);
	}

	public void fetch_TrueAHDCHits(DataEvent event) {
		ArrayList<TrueHit> truehits = new ArrayList<>();

		DataBank bankSIMU = event.getBank("MC::True");

		if (event.hasBank("MC::True")) {
			for (int i = 0; i < bankSIMU.rows(); i++) {
				int    pid    = bankSIMU.getInt("pid", i);
				double x_true = bankSIMU.getFloat("avgX", i);
				double y_true = bankSIMU.getFloat("avgY", i);
				double z_true = bankSIMU.getFloat("avgZ", i);
				double trackE = bankSIMU.getFloat("trackE", i);

				truehits.add(new TrueHit(pid, x_true, y_true, z_true, trackE));
			}
		}
		this.set_TrueAHDCHits(truehits);
	}

	public ArrayList<Hit> get_AHDCHits() {
		return _AHDCHits;
	}

	public void set_AHDCHits(ArrayList<Hit> _AHDCHits) {
		this._AHDCHits = _AHDCHits;
	}

	public ArrayList<TrueHit> get_TrueAHDCHits() {
		return _TrueAHDCHits;
	}

	public void set_TrueAHDCHits(ArrayList<TrueHit> _TrueAHDCHits) {
		this._TrueAHDCHits = _TrueAHDCHits;
	}

}