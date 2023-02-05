package org.jlab.detector.geom.RICH;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Line3D;

public class RICHPixelMap {

    private int debugMode = 0;

    private final static int pfirst[] = {1, 7,14,22,31,41,52,64,77, 91,106,122,139,157,176,196,217,239,262,286,311,337,364,392,395};
    private final static int plast[]  = {6,13,21,30,40,51,63,76,90,105,121,138,156,175,195,216,238,261,285,310,336,363,391,394,397};

    private int nxp[] = new int[397]; // X coordinate of pixel 1 of each mapmt
    private int nyp[] = new int[397]; // Y coordinate of pixel 1 of each mapmt


    //------------------------------
    public RICHPixelMap(){
    //------------------------------
    }

    //------------------------------
    public void init_GlobalPixelGeo(){
    //------------------------------

        int debugMode = 0;
        for(int irow=0; irow<=RICHGeoConstants.NROW; irow++){ // loop on rows

            for(int ipmt=pfirst[irow];ipmt<=plast[irow];ipmt++){ // loop on pmts

                // pixel 1 coordinate
                if(irow<23){
                   nyp[ipmt-1]=16+irow*8;
                   nxp[ipmt-1]=15+(28+(plast[irow]-pfirst[irow]+1)-(ipmt-pfirst[irow])*2)*4;
                }else{
                   int yoff = (int) (23*8+(32-20*25.4)/6.5);
                   nyp[ipmt-1]=16+yoff-(irow-24)*8;
                   nxp[ipmt-1]=15+(28-(plast[irow]-pfirst[irow]+1)+(ipmt-pfirst[irow])*2)*4+8;
                }

                if(debugMode>=1) System.out.println("PMT "+ipmt+" Nx "+nxp[ipmt-1]+" Ny "+nyp[ipmt-1]);

            }
        }

     }


    //------------------------------
    public int Anode2idx(int anode) {
    //------------------------------

        // return anode idx position within the pmt
        return (anode-1)%8+1;
    }


    //------------------------------
    public int Anode2idy(int anode) {
    //------------------------------

        // return anode idy position within the pmt
        return 8-(anode-1)/8;
    }


    //------------------------------
    public int get_Globalidx(int pmt, int anode) {
    //------------------------------
    // return global idx on the RICH plane

        if(pmt>391)return nxp[pmt-1]-(Anode2idx(anode)-1); //obsolete for cosmics
        return nxp[pmt-1]+(Anode2idx(anode)-1);
    }


    //------------------------------
    public int get_Globalidy(int pmt, int anode) {
    //------------------------------
    // return global idy on the RICH plane

        if(pmt>391)return nyp[pmt-1]-(Anode2idy(anode)-1);  //obsolete for cosmics
        return nyp[pmt-1]+(Anode2idy(anode)-1);
    }

}
