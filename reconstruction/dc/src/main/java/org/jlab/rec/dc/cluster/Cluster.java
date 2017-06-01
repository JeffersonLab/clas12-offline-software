package org.jlab.rec.dc.cluster;

import java.util.ArrayList;

import org.jlab.rec.dc.hit.Hit;

/**
 * A cluster in the DC consists of an array of hits that are grouped together
 * according to the algorithm of the ClusterFinder class
 *
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<Hit> {

    private static final long serialVersionUID = 9153980362683755204L;

    private int _Sector;      							//	    sector[1...6]
    private int _Superlayer;    	 					//	    superlayer [1,...6]
    private int _Id;									//		cluster Id

    /**
     *
     * @param sector the sector (1...6)
     * @param superlayer the superlayer (1...6)
     * @param cid the cluster ID, an incremental integer corresponding to the
     * cluster formed in the series of clusters
     */
    public Cluster(int sector, int superlayer, int cid) {
        this._Sector = sector;
        this._Superlayer = superlayer;
        this._Id = cid;
    }

    /**
     *
     * @param hit the first hit in the list of hits composing the cluster
     * @param cid the id of the cluster
     * @return an array list of hits characterized by its sector, superlayer and
     * id number.
     */
    public Cluster newCluster(Hit hit, int cid) {
        return new Cluster(hit.get_Sector(), hit.get_Superlayer(), cid);
    }

    /**
     *
     * @return the sector of the cluster (1...6)
     */
    public int get_Sector() {
        return _Sector;
    }

    /**
     *
     * @param _Sector sector of the cluster (1...6)
     */
    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    /**
     *
     * @return the superlayer of the cluster (1...6)
     */
    public int get_Superlayer() {
        return _Superlayer;
    }

    /**
     *
     * @param _Superlayer the superlayer of the cluster (1...6)
     */
    public void set_Superlayer(int _Superlayer) {
        this._Superlayer = _Superlayer;
    }

    /**
     *
     * @return the id of the cluster
     */
    public int get_Id() {
        return _Id;
    }

    /**
     *
     * @param _Id the id of the cluster
     */
    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    /**
     *
     * @return region (1...3)
     */
    public int get_Region() {
        return (int) (this._Superlayer + 1) / 2;
    }

    /**
     *
     * @return superlayer 1 or 2 in region (1...3)
     */
    public int get_RegionSlayer() {
        return (this._Superlayer + 1) % 2 + 1;
    }

    /**
     *
     * @return cluster info. about location and number of hits contained in it
     */
    public String printInfo() {
        String s = "DC cluster: ID " + this.get_Id() + " Sector " + this.get_Sector() + " Superlayer " + this.get_Superlayer() + " Size " + this.size();
        return s;
    }

}
