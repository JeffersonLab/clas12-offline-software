package org.jlab.rec.fvt.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class TrackList {

    public List<Track> getDCTracks(DataEvent event) {
        List<Track> trkList = new ArrayList<Track>();
        Map<Integer, List<Point3D> >trj = new HashMap<Integer, List<Point3D> >();

        DataBank trkbank = event.getBank("TimeBasedTrkg::TBTracks");
        if (trkbank == null || trkbank.rows() == 0) return null;

        DataBank trkbank2 = event.getBank("TimeBasedTrkg::Trajectory");
        if (trkbank2 == null || trkbank2.rows() == 0) return null;
        int trkrows2 = trkbank2.rows();
        for (int i = 0; i < trkrows2; i++) {
            if (trkbank2.getShort("detector", i) == 8) { // FMT detector
                int id = trkbank2.getShort("id", i);
                Point3D p = new Point3D(trkbank2.getFloat("x", i),
                                        trkbank2.getFloat("y", i),
                                        trkbank2.getFloat("z", i));

                if (trj.get(id) == null) {
                    trj.put(id, new ArrayList<Point3D>());
                    trj.get(id).add(p);
                } else {
                    trj.get(id).add(p);
                }
            }
        }
        int trkrows = trkbank.rows();
        for (int i = 0; i < trkrows; i++) {
            Track trk = new Track();
            int id = trkbank.getShort("id", i);
            trk.setId(id);
            trk.setSector(trkbank.getByte("sector", i));
            trk.setQ(trkbank.getByte("q", i));
            trk.setX(trkbank.getFloat("Vtx0_x", i));
            trk.setY(trkbank.getFloat("Vtx0_y", i));
            trk.setZ(trkbank.getFloat("Vtx0_z", i));
            trk.setPx(trkbank.getFloat("p0_x", i));
            trk.setPy(trkbank.getFloat("p0_y", i));
            trk.setPz(trkbank.getFloat("p0_z", i));
            if (trj.get(id) != null) trk.setTraj(trj.get(id));

            trkList.add(trk);
        }

        return trkList;
    }
}
