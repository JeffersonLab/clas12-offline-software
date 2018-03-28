package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.List;


/**
 * This class selects the best track candidate if there are multiple candidates
 * TODO
 * @author ziegler
 *
 */
public class TrackListFinder {

    public TrackListFinder() {
            // TODO Auto-generated constructor stub
    }

    public List<Track> getTracks(List<Track> cands) {
        List<Track> tracks = new ArrayList<Track>();
        if(cands.size()==0) {
            System.err.print("Error no tracks found");
            return cands;
        }
        int index = 0;

        for(Track trk : cands) {
            if(cands.size()>1) {
                System.out.print(" trk "+index+"  ");trk.printInfo();
            }
            index++;
        }
        return tracks;
    }

}
