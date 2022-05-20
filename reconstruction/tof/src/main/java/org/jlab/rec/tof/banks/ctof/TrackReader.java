package org.jlab.rec.tof.banks.ctof;

import java.util.ArrayList;

import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.base.DetectorType;
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

        if (event.hasBank("CVT::Trajectory") == false) {
            // System.err.println("there is no CVT bank ");
            return Tracks;
        }
        else {

            DataBank bank = event.getBank("CVT::Trajectory");
            int rows = bank.rows();
            for (int i = 0; i < rows; i++) {
                if(bank.getByte("detector", i)==DetectorType.CTOF.getDetectorId()) {
                    int id       = bank.getShort("id", i);
                    double x     = bank.getFloat("x", i);
                    double y     = bank.getFloat("y", i);
                    double z     = bank.getFloat("z", i);
                    double theta = bank.getFloat("theta", i);
                    double phi   = bank.getFloat("phi", i);                
                    double ux    = Math.sin(theta)*Math.cos(phi);
                    double uy    = Math.sin(theta)*Math.sin(phi);
                    double uz    = Math.cos(theta);
                    double path  = bank.getFloat("path", i);
                    Line3d line = new Line3d(new Vector3d(x-10*ux,y-10*uy,z-10*uz), new Vector3d(x+10*ux,y+10*uy,z+10*uz));
                    Track track = new Track(id,line,path);
                    Tracks.add(track);
                }
            }
        }
        return Tracks;
    }


}
