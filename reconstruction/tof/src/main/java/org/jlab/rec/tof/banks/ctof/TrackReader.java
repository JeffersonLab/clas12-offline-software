package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;

import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.rec.tof.track.Track;

/**
 *
 * @author ziegler
 *
 */
public class TrackReader {

    public TrackReader() {
        // TODO Auto-generated constructor stub
    }

    
    public ArrayList<Track> fetch_Trks(DataEvent event) {

         ArrayList<Track> Tracks = new ArrayList<Track>();

        if (event.hasBank("CVTRec::Tracks") == false) {
            // System.err.println("there is no CVT bank ");
            return Tracks;
        }
        else {

            DataBank bank = event.getBank("CVTRec::Tracks");
            int rows = bank.rows();
            for (int i = 0; i < rows; i++) {
                int id      = bank.getShort("ID", i);
                double x    = bank.getFloat("c_x", i);
                double y    = bank.getFloat("c_y", i);
                double z    = bank.getFloat("c_z", i);
                double ux   = bank.getFloat("c_ux", i);
                double uy   = bank.getFloat("c_uy", i);
                double uz   = bank.getFloat("c_uz", i);
                double path = bank.getFloat("pathlength", i);

                Line3d line = new Line3d(new Vector3d(x,y,z), new Vector3d(x+5*ux,y+5*uy, z+5*uz));
                Track track = new Track(id,line,path);
                Tracks.add(track);
            }
        }
        return Tracks;
    }

}
