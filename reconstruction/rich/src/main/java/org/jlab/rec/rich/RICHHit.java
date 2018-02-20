package org.jlab.rec.rich;



public class RICHHit implements Comparable<RICHHit>{
    // class implements Comparable interface to allow for sorting a collection of hits by Time values

    RICHTool tool = new RICHTool();

    // ----------------
    public RICHHit(){
    // ----------------
    }

    // ----------------
    public RICHHit(int hid, RICHTool tool, RICHEdge lead, RICHEdge trail) {
    // ----------------

	// ATT: Edge channel runs [1:192], Hit channel runs [0:191]
        this.id        = hid;
        this.sector    = lead.get_sector();
        this.tile      = lead.get_tile();
        this.pmt       = tool.Tile2PMT(tile, lead.get_channel());
        this.channel   = (lead.get_channel()-1)%64;
        this.anode     = tool.Maroc2Anode(channel);
        this.idx       = tool.Anode2idx(anode);
        this.idy       = tool.Anode2idy(anode);
        this.glx       = tool.Globalidx(pmt, anode);
        this.gly       = tool.Globalidy(pmt, anode);
        this.time      = lead.get_tdc();
        this.duration  = trail.get_tdc()-lead.get_tdc();
        this.cluster   = 0;
        this.xtalk     = 0;
            
      }

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
    private int time;                                 //         Hit time
    private int duration;                             //         Hit duration
    private int cluster;                              //         parent cluster ID
    private int xtalk;                                //         xtalk type

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
    public int get_time() {
    // ----------------
        return time;
    }


    // ----------------
    public void set_time(int time) {
    // ----------------
        this.time = time;
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
    public static boolean passHitSelection(RICHHit hit) {
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
        double tDiff = Math.abs(hit.get_time() - nonet.get_time());
        double xDiff = Math.abs(hit.get_idx()  - nonet.get_idx());
        double yDiff = Math.abs(hit.get_idy()  - nonet.get_idy());
        if(tDiff <= RICHConstants.CLUSTER_TIME_WINDOW && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
        return addFlag;
    }


    // ----------------
    public void showHit() {
    // ----------------
        System.out.println("Hit "  
            +" ID  "  + this.get_id()
            +" Sec "  + this.get_sector()
            +" Til "  + this.get_tile()  
            +" PMT "  + this.get_pmt()     
            +" Ch  "  + this.get_channel()  
            +" And "  + this.get_anode()   
            +" idx "  + this.get_idx()     
            +" idy "  + this.get_idy()     
            +" glx "  + this.get_glx()     
            +" gly "  + this.get_gly()     
            +" clu "  + this.get_cluster() 
            +" xtk "  + this.get_xtalk() 
            +" tim "  + this.get_time()    
            +" dur "  + this.get_duration());
    }
            
}
