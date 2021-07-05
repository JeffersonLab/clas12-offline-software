package org.jlab.rec.fvt.track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.fmt.Constants;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fvt.track.fit.StateVecs;

/**
 *
 * @author ziegler
 */
public class Track {

    /**
     *  The status variable explains the number of tracks and the quality of the reconstruction.
     *
     *  Its last digit is the number of FMT layers used in FVT tracking, so it can be any number
     *  from 0 to 3. If it's 0, it means that no FMT layers were used and the FVT track should be
     *  the same as the DC track.
     *
     *  If there was an error in swimming due to an odd track shape or anything, a 100 is added to
     *  the variable to denote that.
     */
    public int status = 0;

    private int _index;
    private int _sector;
    private int _q;
    private double _chi2;
    private double _x;
    private double _y;
    private double _z;
    private double _px;
    private double _py;
    private double _pz;

    private Trajectory[] _DCtrajs  = new Trajectory[Constants.FVT_Nlayers];
    private Cluster[]    _clusters = new Cluster[Constants.FVT_Nlayers];
    private Trajectory[] _FMTtrajs = new Trajectory[Constants.FVT_Nlayers];

    public Track() {
    }


    public Track(int _index, int _sector, int _q, double _x, double _y, double _z, 
                 double _px, double _py, double _pz, List<Cluster> clusters) {
        this._index = _index;
        this._sector = _sector;
        this._q = _q;
        this._x = _x;
        this._y = _y;
        this._z = _z;
        this._px = _px;
        this._py = _py;
        this._pz = _pz;
        for(Cluster cluster : clusters) this.setCluster(cluster);
    }

    
    
    /**
     * @return the _traj
     */
    public Trajectory getDCTraj(int layer) {
        if(layer<=0 || layer>Constants.FVT_Nlayers) return null;
        else return _DCtrajs[layer-1];
    }

    /**
     * @param _traj the _traj to set
     */
    public void setDCTraj(Trajectory trj) {
        this._DCtrajs[trj.getLayer()-1] = trj;
    }

    public List<Cluster> getClusters() {
        List<Cluster> clusters = new ArrayList<>();
        for(int i=0; i<Constants.FVT_Nlayers; i++) {
            if(_clusters[i]!=null) clusters.add(_clusters[i]);
        }
        return clusters;
    }

    public Cluster getCluster(int layer) {
        if(layer<=0 || layer>Constants.FVT_Nlayers) return null;
        else return _clusters[layer-1];
    }

    public void setCluster(Cluster cluster) {
        this._clusters[cluster.get_Layer()-1] = cluster;
    }

    public Trajectory getFMTtraj(int layer) {
        if(layer<=0 || layer>Constants.FVT_Nlayers) return null;
        return _FMTtrajs[layer-1];
    }

    public void setFMTtraj(Trajectory trj) {
        this._FMTtrajs[trj.getLayer()-1] = trj;
    }

    /**
     * @return the _id
     */
    public int getIndex() {
        return _index;
    }

    /**
     * @param _id the _id to set
     */
    public void getIndex(int _id) {
        this._index = _id;
    }

    /**
     * @return the sector
     */
    public int getSector() {
        return _sector;
    }

    /**
     * @param _sector the sector to set
     */
    public void setSector(int _sector) {
        this._sector = _sector;
    }

    /**
     * @return the _q
     */
    public int getQ() {
        return _q;
    }

    /**
     * @param _q the _q to set
     */
    public void setQ(int _q) {
        this._q = _q;
    }

    /**
     * @return the _chi^2.
     */
    public double getChi2() {
        return _chi2;
    }

    /**
     * @param _chi2 the _chi2 to set
     */
    public void setChi2(double _chi2) {
        this._chi2 = _chi2;
    }

    /**
     * @return the _x
     */
    public double getX() {
        return _x;
    }

    /**
     * @param _x the _x to set
     */
    public void setX(double _x) {
        this._x = _x;
    }

    /**
     * @return the _y
     */
    public double getY() {
        return _y;
    }

    /**
     * @param _y the _y to set
     */
    public void setY(double _y) {
        this._y = _y;
    }

    /**
     * @return the _z
     */
    public double getZ() {
        return _z;
    }

    /**
     * @param _z the _z to set
     */
    public void setZ(double _z) {
        this._z = _z;
    }

    /**
     * @return the _px
     */
    public double getPx() {
        return _px;
    }

    /**
     * @param _px the _px to set
     */
    public void setPx(double _px) {
        this._px = _px;
    }

    /**
     * @return the _py
     */
    public double getPy() {
        return _py;
    }

    /**
     * @param _py the _py to set
     */
    public void setPy(double _py) {
        this._py = _py;
    }

    /**
     * @return the _pz
     */
    public double getPz() {
        return _pz;
    }

    /**
     * @param _pz the _pz to set
     */
    public void setPz(double _pz) {
        this._pz = _pz;
    }

    public double[] getLabPars(StateVecs.StateVec sv) {
        double x = sv.x;
        double y = sv.y;
        double z = sv.z;
        double p = 1./Math.abs(sv.Q);
        double tx = sv.tx;
        double ty = sv.ty;
        double pz = p/Math.sqrt(tx*tx+ty*ty+1);
        double px = pz*tx;
        double py = pz*ty;
        return new double[] {x,y,z,px,py,pz};
    }
    
    
    public static Map<Integer, Track> getDCTracks(DataEvent event) {
        Map<Integer, Track> tracks = new LinkedHashMap<Integer, Track>();
                
        
        DataBank trackBank = null;
        DataBank trajBank  = null;
        if(event.hasBank("TimeBasedTrkg::TBTracks"))   trackBank = event.getBank("TimeBasedTrkg::TBTracks");
        if(event.hasBank("TimeBasedTrkg::Trajectory")) trajBank  = event.getBank("TimeBasedTrkg::Trajectory");
        if (trackBank!=null && trajBank!=null) {
        
            int trkrows = trackBank.rows();
            for (int i = 0; i < trkrows; i++) {
                Track trk = new Track();
                int id = trackBank.getShort("id", i);
                trk.getIndex(i);
                trk.setSector(trackBank.getByte("sector", i));
                trk.setQ(trackBank.getByte("q", i));
                trk.setX(trackBank.getFloat("Vtx0_x", i));
                trk.setY(trackBank.getFloat("Vtx0_y", i));
                trk.setZ(trackBank.getFloat("Vtx0_z", i));
                trk.setPx(trackBank.getFloat("p0_x", i));
                trk.setPy(trackBank.getFloat("p0_y", i));
                trk.setPz(trackBank.getFloat("p0_z", i));
                tracks.put(id,trk);
            }

            int trkrows2 = trajBank.rows();
            for (int i = 0; i < trkrows2; i++) {
                if (trajBank.getShort("detector", i) == DetectorType.FMT.getDetectorId()) { 
                    int id    = trajBank.getShort("id", i);
                    int layer = trajBank.getByte("layer", i);
                    Trajectory trj = new Trajectory(layer,
                                                    trajBank.getFloat("x", i),
                                                    trajBank.getFloat("y", i),
                                                    trajBank.getFloat("z", i),
                                                    trajBank.getFloat("tx", i),
                                                    trajBank.getFloat("ty", i),
                                                    trajBank.getFloat("tz", i),
                                                    trajBank.getFloat("path", i));
                    tracks.get(id).setDCTraj(trj);                
                }
            }
        }
        return tracks;
    }
    
    @Override
    public String toString() {
        String str = "FMT track :" + " Index "  + this._index
                                   + " Q  "     + this._q
                                   + String.format(" P (%.4f,%.4f,%.4f)", this._px, this._py, this._pz)
                                   + String.format(" D (%.4f,%.4f,%.4f)", this._x, this._y, this._z);
        for(int i=0; i<Constants.FVT_Nlayers; i++) {
            if(_DCtrajs[i]!=null) str = str + "\n\t" +_DCtrajs[i].toString() + String.format(" LocY %.4f",_DCtrajs[i].getLocalY());           
        }
        for(int i=0; i<Constants.FVT_Nlayers; i++) {
            if(_clusters[i]!=null) str = str + "\n\t" + _clusters[i].toStringBrief();           
        }
        for(int i=0; i<Constants.FVT_Nlayers; i++) {
            if(_FMTtrajs[i]!=null) str = str + "\n\t" + _FMTtrajs[i].toString() + String.format(" LocY %.4f",_FMTtrajs[i].getLocalY());           
        }
        return str;                           
    }

}
