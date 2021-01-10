package org.jlab.rec.rich;



public class RICHEdge implements Comparable<RICHEdge>{
    // class implements Comparable interface to allow for sorting a collection of hits by Time values

    // ----------------
    public RICHEdge(int i, int isector, int ilayer, int icomponent, int iorder, int itdc) {
    // ----------------

        this.sector    = isector;
        this.tile      = ilayer;
        this.channel   = icomponent;
        this.anode     = 0;
        this.idx       = 0;
        this.idy       = 0;
        this.polarity  = iorder;
        this.tdc       = itdc;
            
      }


    private int sector;                               //         Sector
    private int tile;                                 //         Front-End TILE ID
    private int channel;                              //         MAROC channel
    private int anode;                                //         MA-PMT anode
    private int idx;                                  //         MA-PMT idx
    private int idy;                                  //         MA-PMT idy
    private int polarity;                             //         Edge polarity
    private int tdc;                                  //         Edge TDC 
    private int hit;                                  //         Hit belonging to
          
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
    public int get_polarity() {
    // ----------------
        return polarity;
    }


    // ----------------
    public void set_polarity(int polarity) {
    // ----------------
        this.polarity = polarity;
    }


    // ----------------
    public int get_tdc() {
    // ----------------
        return tdc;
    }


    // ----------------
    public void set_tdc(int tdc) {
    // ----------------
        this.tdc = tdc;
    }

    // ----------------
    public int get_hit() {
    // ----------------
        return hit;
    }


    // ----------------
    public void set_hit(int hit) {
    // ----------------
        this.hit = hit;
    }

    
    // ----------------
    public boolean passEdgeSelection() {
    // ----------------
        // a selection cut to pass the edge
        if(this.get_tdc() > 0) {
            return true;
        } else {
            return false;
        }            
    }

    /*
    // ----------------
    public int compareTo(RICHEdge oedge) {
    // ----------------
        if(this.get_tile()*192+this.get_channel() ==  oedge.get_tile()*192+oedge.get_channel())return 0;
        if(this.get_tile()*192+this.get_channel() >  oedge.get_tile()*192+oedge.get_channel()){
            return 1;
      }else{
          return -1;
        } 
     }*/


    
    // ----------------
    public int compareTo(RICHEdge oedge) {
    // ----------------
        //System.out.println(" --> comp "+this.get_channel()+" "+this.get_tdc()+" "+oedge.get_channel()+" "+oedge.get_tdc());
        if(this.get_tdc() == oedge.get_tdc())return 0;
        if(this.get_tdc() > oedge.get_tdc()){
            return 1;
        }else{
            return -1;
        } 
    }


    // ----------------
    public void showEdge() {
    // ----------------
        System.out.println(
            + this.get_sector()      + "\t" 
            + this.get_tile()        + "\t" 
            + this.get_channel()     + "\t" 
            + this.get_polarity()    + "\t" 
            + this.get_tdc());
    }
            
}
