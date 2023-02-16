package org.jlab.rec.rich;

import org.jlab.detector.geom.RICH.RICHGeoFactory;

import org.jlab.geom.prim.Point3D;

//import org.jlab.detector.geom.RICH.RICHGeoFactory;

// ----------------
public class RICHHit implements Comparable<RICHHit>{
// ----------------
// class implements Comparable interface to allow for sorting a collection of hits by Time values

    private int id = -1;                              //         id
    private int sector;                               //         Sector
    private int tile;                                 //         Front-End TILE ID
    private int channel;                              //         MAROC channel
    private int pmt;                                  //         MA-PMT ID
    private int anode;                                //         MA-PMT anode
    private int lead;                                 //         ID of the leading edge
    private int trail;                                //         ID of the trailing edge
    private int signal;                               //         pointer to the derived RICH signal

    private int idx;                                  //         anode local  idx (within PMT)
    private int idy;                                  //         anode local  idy (within PMT)
    private int glx;                                  //         anode global idx (onto RICH plane)
    private int gly;                                  //         anode global idy (onto RICH plane)

    private double x;                                 //         anode global x (within CLAS)
    private double y;                                 //         anode global y (within CLAS)
    private double z;                                 //         anode global z (within CLAS)
    private double time;                              //         Hit time
    private double rawtime;                           //         Hit rawtime

    private int duration;                             //         Hit duration
    private int cluster;                              //         parent cluster ID
    private int xtalk;                                //         xtalk type
    private int status;                               //         MA-PMT pixel status


    // ----------------
     public RICHHit(){
    // ----------------
    }


    // ----------------
    public RICHHit(int hid, int phase, RICHEdge lead, RICHEdge trail, RICHGeoFactory richgeo, RICHCalibration richcal) {
    // ----------------


        int debugMode = 0;

	// Edge channel runs [1:192], Hit channel runs [0:191]
        this.id        = hid;
        this.sector    = lead.get_sector();
        this.tile      = lead.get_tile();
        this.pmt       = richgeo.Tile2PMT(tile, lead.get_channel());  // run from 1  to 391
        this.channel   = (lead.get_channel()-1)%64;     
        this.anode     = richgeo.Maroc2Anode(channel);                // run from 1 to 64
        this.lead      = lead.get_id();
        this.trail     = trail.get_id();

        this.duration  = trail.get_tdc()-lead.get_tdc();

        double twalk_corr = richcal.get_PixelTimeWalk(sector, pmt, this.duration);
        double toff_corr  = richcal.get_PixelTimeOff(sector, pmt, anode);

        this.status    = richcal.get_PixelStatus(sector, pmt, anode);
        // to flag spurios hits (in not-existing sensors) coming from FPGA SEU indeuced by radiation
        if(this.pmt==0)this.status = 1;
        this.rawtime   = lead.get_tdc();
        this.time      = (lead.get_tdc() + phase*4 + toff_corr - twalk_corr);
        this.cluster   = 0;
        this.xtalk     = 0;
            
        if(debugMode>=2)System.out.format("Correzione time  til %3d ch %6d pmt %3d  anode %4d  dur %5d  raw %7.2f  toff %7.2f  twalk %7.3f  --> time %7.2f \n",
                                           this.tile, this.channel, this.pmt,this.anode,this.duration, this.rawtime, toff_corr, twalk_corr, this.time);

	if(debugMode>=2)System.out.format(" Hittime %4d %4d %8d %7.2f %7d %7.2f %7.2f %7.2f \n", hid, pmt, this.duration, this.rawtime, 
                                           phase*4, toff_corr, -twalk_corr, this.time);

        //this.glx       = richgeo.get_PixelMap().get_Globalidx(pmt, anode);
        //this.gly       = richgeo.get_PixelMap().get_Globalidy(pmt, anode);
        this.idx       = richgeo.get_PixelMap().Anode2idx(anode);
        this.idy       = richgeo.get_PixelMap().Anode2idy(anode);
        Point3D CesPos = richgeo.get_Pixel_Center(sector, pmt,anode);
        this.x         =  CesPos.x();
        this.y         =  CesPos.y();
        this.z         =  CesPos.z();

	if(debugMode>=1)System.out.format(" Hit pmt %4d  anode %3d -->  %8.2f %8.2f %8.2f \n", pmt, anode, this.x, this.y, this.z);

      }


    // ----------------
    public int get_id() { return id; }
    // ----------------

    // ----------------
    public void set_id(int id) { this.id = id; }
    // ----------------
          
    // ----------------
    public int get_sector() { return sector; }
    // ----------------

    // ----------------
    public int get_status() { return status; }
    // ----------------

    // ----------------
    public void set_sector(int sector) { this.sector = sector; }
    // ----------------

    // ----------------
    public int get_channel() { return channel; }
    // ----------------

    // ----------------
    public void set_channel(int channel) { this.channel = channel; }
    // ----------------

    // ----------------
    public int get_tile() { return tile; }
    // ----------------

    // ----------------
    public void set_tile(int tile) { this.tile = tile; }
    // ----------------

    // ----------------
    public int get_pmt() { return pmt; }
    // ----------------


    // ----------------
    public void set_pmt(int pmt) { this.pmt = pmt; }
    // ----------------
    
    // ----------------
    public int get_anode() { return anode; }
    // ----------------

    // ----------------
    public void set_anode(int anode) { this.anode = anode; }
    // ----------------

    // ----------------
    public int get_lead() { return lead; }
    // ----------------

    // ----------------
    public void set_lead(int lead) { this.lead = lead; }
    // ----------------

    // ----------------
    public int get_trail() { return trail; }
    // ----------------

    // ----------------
    public void set_trail(int trail) { this.trail = trail; }
    // ----------------

    // ----------------
    public int get_signal() { return signal; }
    // ----------------

    // ----------------
    public void set_signal(int signal) { this.signal = signal; }
    // ----------------


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
    public Point3D get_Position() {
    // ----------------
        return new Point3D(x,y,z);
    }


    // ----------------
    public void set_Position(Point3D pos){
    // ----------------
        set_Position( pos.x(), pos.y(), pos.z() );
    }


    // ----------------
    public void set_Position(double x, double y, double z) {
    // ----------------
        this.x = x;
        this.y = y;
        this.z = z;
    }


    // ----------------
    public double get_x() {
    // ----------------
        return x;
    }


    // ----------------
    public void set_x(double x) {
    // ----------------
        this.x = x;
    }

    // ----------------
    public double get_y() {
    // ----------------
        return y;
    }


    // ----------------
    public void set_y(double y) {
    // ----------------
        this.y = y;
    }

    // ----------------
    public double get_z() {
    // ----------------
        return z;
    }

    // ----------------
    public void set_z(double z) {
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
    public double get_Time() {
    // ----------------
        return time;
    }


    // ----------------
    public void set_Time(double time) {
    // ----------------
        this.time = time;
    }

    // ----------------
    public double get_rawtime() {
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
        if(hit.get_Time() > 0) {
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
        double tDiff = Math.abs(hit.get_Time() - nonet.get_Time());
        int xDiff = Math.abs(hit.get_idx()  - nonet.get_idx());
        int yDiff = Math.abs(hit.get_idy()  - nonet.get_idy());
        if(tDiff <= RICHConstants.CLUSTER_TIME_WINDOW && xDiff <= 1 && yDiff <= 1 && (xDiff + yDiff) >0) addFlag = true;
        return addFlag;
    }


    // ----------------
    public void showHit() {
    // ----------------
        System.out.format("Hit ID %3d (%3d, %3d) Sec %2d Til %4d PMT %4d Ch %4d And %3d idx %2d idy %2d glx %3d gly %3d xyz %7.2f %7.2f %7.2f clu %3d xtk %5d tim %7.1f %7.1f dur %3d \n",
            this.get_id(),
            this.get_lead(),
            this.get_trail(),
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
            this.get_Time(),    
            this.get_rawtime(),    
            this.get_duration());
    }
            
}
