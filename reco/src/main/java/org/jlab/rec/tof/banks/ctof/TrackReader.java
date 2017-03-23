package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * 
 * @author ziegler
 *
 */
public class TrackReader {

	public TrackReader() {
		// TODO Auto-generated constructor stub
	}

	private List<Line3d> _TrkLines;
	private double[] _Paths;

	public List<Line3d> get_TrkLines() {
		return _TrkLines;
	}

	public void set_TrkLines(List<Line3d> trkLines) {
		this._TrkLines = trkLines;
	}

	public double[] get_Paths() {
		return _Paths;
	}

	public void set_Paths(double[] paths) {
		this._Paths = paths;
	}

	public void fetch_Trks(DataEvent event) {

		if (event.hasBank("CVTRec::Tracks") == false) {
			// System.err.println("there is no CVT bank ");

			_TrkLines = new ArrayList<Line3d>();

			return;
		}

		EvioDataBank bank = (EvioDataBank) event.getBank("CVTRec::Tracks");

		double[] fitChisq = bank.getDouble("circlefit_chi2_per_ndf"); // use
																		// this
																		// to
																		// select
																		// good
																		// tracks

		double[] x = bank.getDouble("c_x"); // cross x-position in the lab at
											// the CTOF face
		double[] y = bank.getDouble("c_y"); // cross y-position in the lab at
											// the CTOF face
		double[] z = bank.getDouble("c_z"); // cross z-position in the lab at
											// the CTOF face
		double[] ux = bank.getDouble("c_ux"); // cross x-unit-dir in the lab at
												// the CTOF face
		double[] uy = bank.getDouble("c_uy"); // cross y-unit-dir in the lab at
												// the CTOF face
		double[] uz = bank.getDouble("c_uz"); // cross z-unit-dir in the lab at
												// the CTOF face
		double[] p = bank.getDouble("pathlength"); // pathlength

		if (event.hasBank("CVTRec::Tracks") == true) {
			// instanciates the list
			// each arraylist corresponds to the tracks for a given sector
			// instanciates the list
			// each arraylist corresponds to the tracks for a given sector
			List<Line3d> trkLines = new ArrayList<Line3d>();
			// each array of paths likewise corresponds to the tracks for a
			// given sector
			double[] paths = new double[x.length];

			for (int i = 0; i < x.length; i++) {
				// if(fitChisq[i]>1)
				// continue; // check this

				Line3d trk_path = new Line3d(new Vector3d(x[i], y[i], z[i]),
						new Vector3d(x[i] + 5.0 * ux[i], y[i] + 5.0 * uy[i],
								z[i] + 5.0 * uz[i]));

				// add this hit
				trkLines.add(trk_path);
				paths[i] = p[i];
			}

			// fill the list of TOF hits
			this.set_TrkLines(trkLines);
			this.set_Paths(paths);
		}
	}

}
