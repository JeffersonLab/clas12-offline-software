package org.jlab.rec.rich;

import org.jlab.detector.geant4.v2.RICHGeant4Factory;
import eu.mihosoft.vrl.v3d.Vector3d;

// ----------------
public class RICHHit implements Comparable<RICHHit>{
// ----------------
// class implements Comparable interface to allow for sorting a collection of hits by Time values

    private int debugMode = 0;

    private int id;                                   //         id
    private int sector;                               //         Sector
    private int tile;                                 //         Front-End TILE ID
    private int channel;                              //         MAROC channel
    private int pmt;                                  //         MA-PMT ID
    private int anode;                                //         MA-PMT anode
    private int idx;                                  //         anode local  idx (within PMT)
    private int idy;                                  //         anode local  idy (within PMT)
    private int glx;                                  //         anode global idx (onto RICH plane)
    private int gly;                                  //         anode global idy (onto RICH plane)
    private float x;                                  //         anode global x (within CLAS)
    private float y;                                  //         anode global y (within CLAS)
    private float z;                                  //         anode global z (within CLAS)
    private float time;                               //         Hit time
    private float rawtime;                            //         Hit rawtime
    private int duration;                             //         Hit duration
    private int cluster;                              //         parent cluster ID
    private int xtalk;                                //         xtalk type


    // ----------------
     public RICHHit(){
    // ----------------
    }

    // ----------------
    public RICHHit(int hid, RICHTool tool, int phase, RICHEdge lead, RICHEdge trail) {
    // ----------------


	// ATT: Edge channel runs [1:192], Hit channel runs [0:191]
        this.id        = hid;
        this.sector    = lead.get_sector();
        this.tile      = lead.get_tile();
        this.pmt       = tool.Tile2PMT(tile, lead.get_channel());  // run from1  to 64
        this.channel   = (lead.get_channel()-1)%64;     
        this.anode     = tool.Maroc2Anode(channel);                // run from 1 to 64
        this.idx       = tool.Anode2idx(anode);
        this.idy       = tool.Anode2idy(anode);
        this.glx       = tool.get_Globalidx(pmt, anode);
        this.gly       = tool.get_Globalidy(pmt, anode);
        this.duration  = trail.get_tdc()-lead.get_tdc();

        /*float twalk_corr = 0;
        if(this.duration < 59){
              twalk_corr = (float) this.duration * tool.getPMTtimewalk(pmt, 2) + tool.getPMTtimewalk(pmt, 1);
        }else{
              twalk_corr = (float) this.duration * tool.getPMTtimewalk(pmt, 4) + tool.getPMTtimewalk(pmt, 3);
        }*/

        double twalk_corr = 0;
        double D0 = tool.getPMTtimewalk(pmt, 1);
        double T0 = tool.getPMTtimewalk(pmt, 2);
        double m1 = tool.getPMTtimewalk(pmt, 3);
        double m2 = tool.getPMTtimewalk(pmt, 4);
        double f1 = m1 * this.duration + T0;
        double f1T = m1 * D0 + T0;

        double f2 = m2 * (this.duration - D0) + f1T;
        twalk_corr = f1;
        if (this.duration > D0) twalk_corr = f2;

        this.rawtime   = (float) lead.get_tdc();
        this.time      = (float) (lead.get_tdc() + phase*4 + tool.getPMTtimeoff(pmt, anode) - twalk_corr);
        //this.time      = (float) (lead.get_tdc() + tool.getFTOFphase()*4 + tool.getPMTtimeoff(pmt, anode));
        this.cluster   = 0;
        this.xtalk     = 0;
            
        if(debugMode>=1)System.out.format("Correzione time  pmt %3d  anode %4d  dur %5d  raw %7.2f  toff %7.2f  twalk (%7.2f %7.2f %7.3f %7.3f --> %7.3f %7.3f) %7.3f  --> time %7.2f \n",
                                           this.pmt,this.anode,this.duration, this.rawtime, tool.getPMTtimeoff(pmt, anode), 
                                           D0, T0, m1, m2, f1, f2, twalk_corr, this.time);

	if(debugMode>=1)System.out.format(" Hittime %4d %4d %8d %7.2f %7d %7.2f %7.2f %7.2f \n", hid, pmt, this.duration, this.rawtime, 
                                           phase*4, tool.getPMTtimeoff(pmt, anode), -twalk_corr, this.time);

        //Vector3d CenPos = tool.GetPixelCenter(pmt,anode);
        Vector3d CesPos = tool.get_Pixel_Center(pmt,anode);
        this.x         = (float) CesPos.x;
        this.y         = (float) CesPos.y;
        this.z         = (float) CesPos.z;

	if(debugMode>=1)System.out.format(" Hit pmt %4d  anode %3d -->  %8.2f %8.2f %8.2f \n", pmt, anode, this.x, this.y, this.z);

      }


    // ----------------
    public int get_id() {
    // ----------------
        return id;
    }

    // ----------------
    public void set_id(int id) {
    // ----------------
        this.id = id;
    }
          
    // ----------------
    public int get_sector() {
    // ----------------
        return sector;
    }

    // ----------------
    public void set_sector(int sector) {
    // ----------------
        this.sector = sector;
    }

    // ----------------
    public int get_channel() {
    // ----------------
        return channel;
    }

    // ----------------
    public void set_channel(int channel) {
    // ----------------
        this.channel = channel;
    }

    // ----------------
    public int get_tile() {
    // ----------------
        return tile;
    }


    // ----------------
    public void set_tile(int tile) {
    // ----------------
        this.tile = tile;
    }

    // ----------------
    public int get_pmt() {
    // ----------------
        return pmt;
    }


    // ----------------
    public void set_pmt(int pmt) {
    // ----------------
        this.pmt = pmt;
    }
    
    // ----------------
    public int get_anode() {
    // ----------------
        return anode;
    }


    // ----------------
    public void set_anode(int anode) {
    // ----------------
        this.anode = anode;
    }

    
    // ----------------
    public int get_idx() {
    // ----------------
        return idx;
    }


    // ----------------
    public void set_idx(int idx) {
    // ----------------
        this.idx = idx;
    }


    // ----------------
    public int get_idy() {
    // ----------------
        return idy;
    }


    // ----------------
    public void set_idy(int idy) {
    // ----------------
        this.idy = idy;
    }


    // ----------------
    public int get_glx() {
    // ----------------
        return glx;
    }


    // ----------------
    public void set_glx(int glx) {
    // ----------------
        this.glx = glx;
    }


    // ----------------
    public int get_gly() {
    // ----------------
        return gly;
    }


    // ----------------
    public void set_gly(int gly) {
    // ----------------
        this.gly=gly;
    }

    // ----------------
    public float get_x() {
    // ----------------
        return x;
    }


    // ----------------
    public void set_x(float x) {
    // ----------------
        this.x = x;
    }

    // ----------------
    public float get_y() {
    // ----------------
        return y;
    }


    // ----------------
    public void set_y(float y) {
    // ----------------
        this.y = y;
    }

    // ----------------
    public float get_z() {
    // ----------------
        return z;
    }

    // ----------------
    public void set_z(float z) {
    // ----------------
        this.z = z;
    }


    // ----------------
    public int get_duration() {
    // ----------------
        return duration;
    }


    // ----------------
    public void set_duration(int duration) {
    // ----------------
        this.duration = duration;
    }


    // ----------------
    public float get_time() {
    // ----------------
        return time;
    }


    // ----------------
    public void set_time(int time) {
    // ----------------
        this.time = time;
    }

    // ----------------
    public float get_rawtime() {
    // ----------------
        return rawtime;
    }


    // ----------------
    public void set_rawtime(int rawtime) {
    // ----------------
        this.rawtime = rawtime;
    }


    // ----------------
    public int get_cluster() {
    // ----------------
        return cluster;
    }

    // ----------------
    public void set_cluster(int cluster) {
    // ----------------
        this.cluster = cluster;
    }
    
    // ----------------
    public int get_xtalk() {
    // ----------------
        return xtalk;
    }

    // ----------------
    public void set_xtalk(int xtalk) {
    // ----------------
        this.xtalk = xtalk;
    }
    
    // ----------------
    public boolean passHitSelection(RICHHit hit) {
    // ----------------
        // a selection cut to pass the edge
        if(hit.get_time() > 0) {
            return true;
        } else {
            return false;
        }            
    }

    
    // ----------------
    public int compareTo(RICHHit ohit) {
    // ----------------
        if(ohit.get_duration()==this.get_duration()) return 0;
        if(ohit.get_duration() > this.get_duration()){
            return 1;
        }else{
            return -1;
        } 
    }


    // ----------------
    public boolean IsinNonet(RICHHit hit, RICHHit nonet) {
    // ----------------
        boolean addFlag = false;
        float tDiff = Math.abs(hit.get_time() - nonet.get_time());
        int xDiff = Math.abs(hit.get_idx()  - nonet.get_idx());
        int yDiff = Math.abs(hit.get_idy()  - nonet.get_idy());
        if(tDiff <= RICHConstants.CLUSTER_TIME_WINDOW && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
        return addFlag;
    }


    // ----------------
    public void showHit() {
    // ----------------
        System.out.format("Hit ID %3d Sec %2d Til %4d PMT %4d Ch %4d And %3d idx %2d idy %2d glx %3d gly %3d xyz %7.2f %7.2f %7.2f clu %3d xtk %5d tim %7.1f %7.1f dur %3d \n",
            this.get_id(),
            this.get_sector(),
            this.get_tile(),  
            this.get_pmt(),     
            this.get_channel(),  
            this.get_anode(),   
            this.get_idx(),     
            this.get_idy(),     
            this.get_glx(),     
            this.get_gly(),     
            this.get_x(),     
            this.get_y(),     
            this.get_z(),     
            this.get_cluster(), 
            this.get_xtalk(), 
            this.get_time(),    
            this.get_rawtime(),    
            this.get_duration());
    }
            
}
