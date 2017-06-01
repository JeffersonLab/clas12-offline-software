package org.jlab.rec.tof.banks.ctof;

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

        if (event.hasBank("CVTRec::Tracks") == false) {
            // System.err.println("there is no CVT bank ");

            _TrkLines = new ArrayList<Line3d>();

            return;
        }

        DataBank bank = event.getBank("CVTRec::Tracks");
        int rows = bank.rows();

        double[] x = new double[rows]; // cross x-position in the lab at
        // the CTOF face
        double[] y = new double[rows]; // cross y-position in the lab at
        // the CTOF face
        double[] z = new double[rows]; // cross z-position in the lab at
        // the CTOF face
        double[] ux = new double[rows]; // cross x-unit-dir in the lab at
        // the CTOF face
        double[] uy = new double[rows]; // cross y-unit-dir in the lab at
        // the CTOF face
        double[] uz = new double[rows]; // cross z-unit-dir in the lab at
        // the CTOF face
        double[] p = new double[rows]; // pathlength

        if (event.hasBank("CVTRec::Tracks") == true) {
            // instanciates the list
            // each arraylist corresponds to the tracks for a given sector
            // instanciates the list
            // each arraylist corresponds to the tracks for a given sector
            List<Line3d> trkLines = new ArrayList<Line3d>();
            // each array of paths likewise corresponds to the tracks for a
            // given sector
            double[] paths = new double[rows];

            for (int i = 0; i < rows; i++) {

                x[i] = bank.getFloat("c_x", i);
                y[i] = bank.getFloat("c_y", i);
                z[i] = bank.getFloat("c_z", i);
                ux[i] = bank.getFloat("c_ux", i);
                uy[i] = bank.getFloat("c_uy", i);
                uz[i] = bank.getFloat("c_uz", i);
                p[i] = bank.getFloat("pathlength", i);

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
