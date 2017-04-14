package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

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

		if (event.hasBank("TimeBasedTrkg::TBTracks") == false) {
			// System.err.println("there is no DC bank ");
			_TrkLines = new ArrayList<Line3d>();

			return;
		}

		DataBank bankDC = event.getBank("TimeBasedTrkg::TBTracks");

		// double[] fitChisq = bankDC.getDouble("fitChisq"); // use this to
		// select good tracks
		int rows = bankDC.rows();

		double[] x = new double[rows]; // Region 3 cross x-position in the lab
										// (in cm = default unit)
		double[] y = new double[rows]; // Region 3 cross y-position in the lab
		double[] z = new double[rows]; // Region 3 cross z-position in the lab
		double[] ux = new double[rows]; // Region 3 cross x-unit-dir in the lab
		double[] uy = new double[rows]; // Region 3 cross y-unit-dir in the lab
		double[] uz = new double[rows]; // Region 3 cross z-unit-dir in the lab
		double[] p = new double[rows]; // pathlength of the track from origin to
										// DC R3
		int[] tid = new int[rows]; // track id in HB bank
		if (event.hasBank("HitBasedTrkg::HBTracks") == true) {
			// instanciates the list
			// each arraylist corresponds to the tracks for a given sector
			List<Line3d> trkLines = new ArrayList<Line3d>();
			// each array of paths likewise corresponds to the tracks for a
			// given sector
			double[] paths = new double[rows];

			for (int i = 0; i < rows; i++) {
				// if(fitChisq[i]>1)
				// continue; // check this
				tid[i] = bankDC.getShort("id", i);
				x[i] = bankDC.getFloat("c3_x", i);
				y[i] = bankDC.getFloat("c3_y", i);
				z[i] = bankDC.getFloat("c3_z", i);
				ux[i] = bankDC.getFloat("c3_ux", i);
				uy[i] = bankDC.getFloat("c3_uy", i);
				uz[i] = bankDC.getFloat("c3_uz", i);
				p[i] = bankDC.getFloat("pathlength", i);
				Line3d trk_path = new Line3d(new Vector3d(x[i], y[i], z[i]),
						new Vector3d(x[i] + 250 * ux[i], y[i] + 250 * uy[i],
								z[i] + 250 * uz[i]));

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
