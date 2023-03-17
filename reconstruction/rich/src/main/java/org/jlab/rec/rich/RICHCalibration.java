package org.jlab.rec.rich;

import org.jlab.detector.geom.RICH.RICHGeoConstants;
import org.jlab.detector.geom.RICH.RICHGeoFactory;
import org.jlab.detector.geom.RICH.RICHLayerType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import org.jlab.geom.prim.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.FileReader;
import java.io.BufferedReader;

/**
 *
 * @author mcontalb
 */
public class RICHCalibration{

    private final static RICHGeoConstants geocost = new RICHGeoConstants();

    private final static int NSEC   =  geocost.NSEC;
    private final static int NLAY   =  geocost.NLAY;
    private final static int NPMT   =  geocost.NPMT;
    private final static int NPIX   =  geocost.NPIX;
    private final static int NCOMPO =  geocost.NCOMPO;
    private final static int NWALK  =  geocost.NWALK;

    private final static int NAERMAX  = RICHGeoConstants.NAERMAX;

    private static IndexedTable richTable;
    private ArrayList<IndexedTable> timewalkTables  = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> timeoffTables   = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> anglerefTables  = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> pixelTables     = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> pixstatusTables = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> aerstatusTables = new ArrayList<IndexedTable>();
    private ArrayList<IndexedTable> mirstatusTables = new ArrayList<IndexedTable>();

    private double D0[] = new double[NPMT];
    private double T0[] = new double[NPMT];
    private double m1[] = new double[NPMT];
    private double m2[] = new double[NPMT];

    private RICHParameters richpar;

    // just for TxT file
    private double pmt_timeoff[][] = new double[NPMT][NPIX];
    private double pmt_timewalk[][] = new double[NPMT][4];

    private double aero_chele_dir[][][] = new double[4][NAERMAX][225];
    private double aero_chele_lat[][][] = new double[4][NAERMAX][225];
    private double aero_chele_spe[][][] = new double[4][NAERMAX][225];
    private double aero_schele_dir[][][] = new double[4][NAERMAX][225];
    private double aero_schele_lat[][][] = new double[4][NAERMAX][225];
    private double aero_schele_spe[][][] = new double[4][NAERMAX][225];


    //------------------------------
    public RICHCalibration() {
    //------------------------------
    }


    //------------------------------
    public void load_CCDB(ConstantsManager manager, int run, int ncalls, RICHGeoFactory richgeo, RICHParameters richpar){
    //------------------------------

        int debugMode = 0;

        this.richpar = richpar;
        this.richTable = richgeo.get_richTable();

        for(int irich=1; irich<=richgeo.nRICHes(); irich++){

            int isec = find_RICHSector(irich);
            if(isec==0) continue;

            String time_walk = String.format("/calibration/rich/module%1d/time_walk",irich);
            String time_offs = String.format("/calibration/rich/module%1d/time_offset",irich);
            String cher_angs = String.format("/calibration/rich/module%1d/cherenkov_angle",irich);
            String stat_aero = String.format("/calibration/rich/module%1d/status_aerogel",irich);
            String pmts_pixe = String.format("/calibration/rich/module%1d/mapmt_pixel",irich);
            String stat_pmts = String.format("/calibration/rich/module%1d/status_mapmt",irich);
            String stat_mirr = String.format("/calibration/rich/module%1d/status_mirror",irich);

            init_CalConstantsCCDB( manager.getConstants(run, time_walk),
                                   manager.getConstants(run, time_offs),
                                   manager.getConstants(run, cher_angs),
                                   manager.getConstants(run, stat_aero),
                                   manager.getConstants(run, pmts_pixe),
                                   manager.getConstants(run, stat_pmts),
                                   manager.getConstants(run, stat_mirr) );

            /*if(irich==1){
                // first RICH module
                init_CalConstantsCCDB( manager.getConstants(run, "/calibration/rich/module1/time_walk"),
                                       manager.getConstants(run, "/calibration/rich/module1/time_offset"),
                                       manager.getConstants(run, "/calibration/rich/module1/cherenkov_angle"),
                                       manager.getConstants(run, "/calibration/rich/module1/status_aerogel"), 
                                       manager.getConstants(run, "/calibration/rich/module1/mapmt_pixel"), 
                                       manager.getConstants(run, "/calibration/rich/module1/status_mapmt"), 
                                       manager.getConstants(run, "/calibration/rich/module1/status_mirror"), irich );
            }else{
                // second RICH module
                init_CalConstantsCCDB( manager.getConstants(run, "/calibration/rich/module2/time_walk"),
                                       manager.getConstants(run, "/calibration/rich/module2/time_offset"),
                                       manager.getConstants(run, "/calibration/rich/module2/cherenkov_angle"),
                                       manager.getConstants(run, "/calibration/rich/module2/status_aerogel"), 
                                       manager.getConstants(run, "/calibration/rich/module2/mapmt_pixel"), 
                                       manager.getConstants(run, "/calibration/rich/module2/status_mapmt"), 
                                       manager.getConstants(run, "/calibration/rich/module2/status_mirror"), irich );
            }*/

            if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST)) {
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("RICH: Load RECO Calibration from CCDB for RICH %4d  sector %4d  run %6d (ncalls %3d) \n", irich, isec, run, ncalls);
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("Banks \n %s \n %s \n %s \n %s \n %s \n %s \n %s \n \n",time_walk,time_offs,cher_angs,stat_aero,pmts_pixe,stat_pmts,stat_mirr);

                dump_CalConstants(irich);
            }

            if(RICHConstants.TIMECAL_FROM_FILE==1 && ncalls==0){

                init_CalConstantsTxT(1, isec, ncalls);

                if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST)) {
                        System.out.format("------------------------------------------------------------- \n");
                        System.out.format("RICH: Load TIME calib constants from local TxT file for RICH 4d  sector %4d  run %6d \n", irich, isec, run);
                        System.out.format("------------------------------------------------------------- \n");

                        dump_TimeConstants(irich);
                }
            }

            if(RICHConstants.AEROCAL_FROM_FILE==1){

                init_CalConstantsTxT(2, isec, ncalls);

                if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST)) {
                        System.out.format("------------------------------------------------------------- \n");
                        System.out.format("RICH: Load AERO calib constants from local TxT file for RICH 4d  sector %4d  run %6d \n", irich, isec, run);
                        System.out.format("------------------------------------------------------------- \n");

                        dump_AeroConstants(irich);
                }
            }

            if(RICHConstants.PIXECAL_FROM_FILE==1){

                init_CalConstantsTxT(3, isec, ncalls);

                if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST)) {
                        System.out.format("------------------------------------------------------------- \n");
                        System.out.format("RICH: Load PIXEL calib constants from local TxT file for RICH 4d  sector %4d  run %6d \n", irich, isec, run);
                        System.out.format("------------------------------------------------------------- \n");

                        dump_PixelConstants(irich);
                }
            }
        }

    }


    //------------------------------
    public void init_CalConstantsCCDB(IndexedTable timewalkConstants, IndexedTable timeoffConstants,  
                                      IndexedTable cheleConstants, IndexedTable aerstatusConstants,
                                      IndexedTable pixelConstants, IndexedTable pixstatusConstants,
                                      IndexedTable mirstatusConstants ){
    //------------------------------

        int debugMode = 0;

        this.timeoffTables.add( timeoffConstants );
        this.timewalkTables.add( timewalkConstants );
        this.anglerefTables.add( cheleConstants );
        this.pixelTables.add( pixelConstants );
        this.aerstatusTables.add( aerstatusConstants );
        this.mirstatusTables.add( mirstatusConstants );
        this.pixstatusTables.add( pixstatusConstants );

    }


    //------------------------------
    public void init_CalConstantsTxT(int ifile, int isec, int ncalls){
    //------------------------------
    // To be moved to CCDB

        int debugMode = 0;

       if(ifile==1){
           /*
            * TIME_OFFSETs
            */
           /* String off_filename = new String("CALIB_DATA/MIRA/richTimeOffsets.out");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(off_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    String[] array = currentLine.split(" ");
                    int ipmt = Integer.parseInt(array[0]);
                    int ich  = Integer.parseInt(array[1]);
                    float off = Float.parseFloat(array[4]);
                    pmt_timeoff[ipmt-1][ich-1] = off;

                    if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<richpar.DEBUG_CAL_COST)if(ich==1 || ich==64)
                              System.out.format("TXT RICH TOFF   pmt %4d (ich=%3d: %8.2f) \n", ipmt, ich, pmt_timeoff[ipmt-1][ich-1]);

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", off_filename);
                e.printStackTrace();

            }*/


            /*
            *  TIME_WALKs
            */
            String walk_filename = new String("calibration/rich/richModule1/time_walk.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(walk_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    if(currentLine.substring(0,1).equals("#")) continue;
                    if(currentLine.substring(0,1).equals(" ")) continue;

                    String[] array = currentLine.split(" ");
                    //int isec = Integer.parseInt(array[0]);
                    int ipmt = Integer.parseInt(array[1]);
                    if(ipmt<1 || ipmt>391) System.err.format("Exception occurred trying to pmt from '%s' \n", walk_filename);

                    D0[ipmt-1] = Double.parseDouble(array[3]);
                    T0[ipmt-1] = Double.parseDouble(array[4]);
                    m1[ipmt-1] = Double.parseDouble(array[5]);
                    m2[ipmt-1] = Double.parseDouble(array[6]);

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", walk_filename);
                e.printStackTrace();

            }
       }


        if(ifile==2){

           /*
            * AEROGEL CALIBRATED OPTICS
            */

            String chele_filename = new String("CALIB_DATA/aerogel_chele.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(chele_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    String[] array = currentLine.split(" ");
                    int idlay = Integer.parseInt(array[1]);
                    int iaer  = Integer.parseInt(array[2]);
                    int iqua  = Integer.parseInt(array[3]);

                    if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST))
                        System.out.format("Read chele for AERO lay %3d  compo %3d quadrant  %3d", idlay, iaer, iqua);

                    int ndir     = Integer.parseInt(array[4]);
                    float chdir  = Float.parseFloat(array[5]);
                    float sdir   = Float.parseFloat(array[6]);

                    int nlat     = Integer.parseInt(array[7]);
                    float chlat  = Float.parseFloat(array[8]);
                    float slat   = Float.parseFloat(array[9]);

                    int nspe     = Integer.parseInt(array[10]);
                    float chspe  = Float.parseFloat(array[11]);
                    float sspe   = Float.parseFloat(array[12]);

                    aero_chele_dir[idlay-201][iaer-1][iqua] = chdir;
                    aero_chele_lat[idlay-201][iaer-1][iqua] = chlat;
                    aero_chele_spe[idlay-201][iaer-1][iqua] = chspe;

                    aero_schele_dir[idlay-201][iaer-1][iqua] = sdir;
                    aero_schele_lat[idlay-201][iaer-1][iqua] = slat;
                    aero_schele_spe[idlay-201][iaer-1][iqua] = sspe;

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", chele_filename);
                e.printStackTrace();
            }

            if((debugMode>=1 || richpar.DEBUG_CAL_COST>=1) && ncalls<Math.max(1,richpar.DEBUG_CAL_COST))System.out.format("initConstants: DONE \n");

        }

    }

    //------------------------------
    public void dump_CalConstants(int irich) { 
    //------------------------------


        dump_TimeConstants(irich);
        dump_AeroConstants(irich);
        dump_MirrorConstants(irich);
        dump_PixelConstants(irich);

    }


    //------------------------------
    public void dump_TimeConstants(int irich) { 
    //------------------------------

        int isec = find_RICHSector(irich);
        if(isec==0) return;

        for(int ipmt=1; ipmt<=NPMT; ipmt++){

            if(ipmt<=10 || ipmt>=382)System.out.format("CCDB RICH TOFF    ipmt %4d  %8.3f (ch1)  %8.3f (ch2)  %8.3f (ch63)  %8.3f (ch64) \n", ipmt,
               -get_PixelTimeOff(isec, ipmt, 1), -get_PixelTimeOff(isec, ipmt, 2), -get_PixelTimeOff(isec, ipmt, 63), -get_PixelTimeOff(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB RICH TOFF     ....... \n");
            if(ipmt==391)System.out.format("  \n");
        }

        for(int ipmt=1; ipmt<=NPMT; ipmt++){
            if(ipmt<=10 || ipmt>=382)System.out.format("CCDB RICH TWALK   ipmt %4d  D0 = %8.3f  T0 = %8.3f  m1 = %8.4f  m2 = %8.4f\n", ipmt,
                     timewalkTables.get(irich-1).getDoubleValue("D0", isec, ipmt, 0), timewalkTables.get(irich-1).getDoubleValue("m1", isec, ipmt, 0),
                     timewalkTables.get(irich-1).getDoubleValue("m2", isec, ipmt, 0), timewalkTables.get(irich-1).getDoubleValue("T0", isec, ipmt, 0));
                     

            if(ipmt==10)System.out.format("CCDB RICH TWALK    ....... \n");
            if(ipmt==391)System.out.format("  \n");
        }

    }


    //------------------------------
    public void dump_PixelConstants(int irich) { 
    //------------------------------

        int isec = find_RICHSector(irich);
        if(isec==0) return;

        for(int ipmt=1; ipmt<=NPMT; ipmt++){

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL GAIN    ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt,
               get_PixelGain(isec, ipmt, 1), get_PixelGain(isec, ipmt, 2), get_PixelGain(isec, ipmt, 63), get_PixelGain(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL GAIN     ....... \n");

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL EFF     ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt,
               get_PixelEff(isec, ipmt, 1), get_PixelEff(isec, ipmt, 2), get_PixelEff(isec, ipmt, 63), get_PixelEff(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL EFF      ....... \n");

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL DARKRT  ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt,
               get_PixelDarkRate(isec, ipmt, 1), get_PixelDarkRate(isec, ipmt, 2), get_PixelDarkRate(isec, ipmt, 63), get_PixelDarkRate(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL DARKRATE ....... \n");

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL MTIME   ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt,
               get_PixelMeanTime(isec, ipmt, 1), get_PixelMeanTime(isec, ipmt, 2), get_PixelMeanTime(isec, ipmt, 63), get_PixelMeanTime(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL MTIME    ....... \n");

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL STIME   ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt,
               get_PixelRMSTime(isec, ipmt, 1), get_PixelRMSTime(isec, ipmt, 2), get_PixelRMSTime(isec, ipmt, 63), get_PixelRMSTime(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL STIME    ....... \n");

            if(ipmt<=2 || ipmt>=390)System.out.format("CCDB PIXEL STATUS  ipmt %4d  %8d (ch1)  %8d (ch2)  %8d (ch63)  %8d (ch64) \n", ipmt,
               get_PixelStatus(isec, ipmt, 1), get_PixelStatus(isec, ipmt, 2), get_PixelStatus(isec, ipmt, 63), get_PixelStatus(isec, ipmt, 64));
            if(ipmt==10)System.out.format("CCDB PIXEL STATUS   ....... \n");

        }
        System.out.format("  \n");


        int p_dead = 0;
        for(int ipmt=1; ipmt<=NPMT; ipmt++){
            for(int ianode=1; ianode<=NPIX; ianode++){
                if(get_PixelStatus(isec, ipmt, ianode)==2){
                    System.out.format("CCDB PIXEL DEAD Sec %4d  PMT %4d  Anode %6d  Status %6d \n",isec, ipmt, ianode, get_PixelStatus(isec, ipmt, ianode));  
                    p_dead++;
                }
            }
        }

        int p_hot = 0;
        System.out.format("  \n");
        for(int ipmt=1; ipmt<=NPMT; ipmt++){
            for(int ianode=1; ianode<=NPIX; ianode++){
                if(get_PixelStatus(isec, ipmt, ianode)==5){
                    System.out.format("CCDB PIXEL HOT  Sec %4d  PMT %4d  Anode %6d  Status %6d \n",isec, ipmt, ianode, get_PixelStatus(isec, ipmt, ianode));  
                    p_hot++;
                }
            }
        }
        System.out.format("Pixels with bad status flag: %4d dead   %4d hot \n \n",p_dead,p_hot);

    }



    //------------------------------
    public void dump_AeroConstants(int irich) { 
    //------------------------------

        double mrad = RICHConstants.MRAD;
        int isec = find_RICHSector(irich);
        if(isec==0) return;

        for (int ila=0; ila<4; ila++){
            for(int ico=0; ico<geocost.NAERCO[ila]; ico++){
                if(ico<2 || ico>geocost.NAERCO[ila]-3){
                    for (int iqua=0; iqua<225; iqua+=224){
                    
                        System.out.format("CCDB RICH CHELE   ila %4d itile %3d iq %4d ", 201+ila, ico+1, iqua+1);
                        System.out.format(" [+]  %5.2f %6.2f %4.2f   %5.2f %6.2f %4.2f   %5.2f %6.2f %4.2f \n", 
                        get_NElectron(isec, ila, ico, iqua, 0, 1), 
                        get_ChElectron(isec, ila, ico, iqua, 0, 1)*mrad, get_SChElectron(isec, ila, ico, iqua, 0, 1)*mrad,
                        get_NElectron(isec, ila, ico, iqua, 1, 1), 
                        get_ChElectron(isec, ila, ico, iqua, 1, 1)*mrad, get_SChElectron(isec, ila, ico, iqua, 1, 1)*mrad,
                        get_NElectron(isec, ila, ico, iqua, 2, 1), 
                        get_ChElectron(isec, ila, ico, iqua, 2, 1)*mrad, get_SChElectron(isec, ila, ico, iqua, 2, 1)*mrad);
                        System.out.format("                                             ");
                        System.out.format(" [-]  %5.2f %6.2f %4.2f   %5.2f %6.2f %4.2f   %5.2f %6.2f %4.2f \n", 
                        get_NElectron(isec, ila, ico, iqua, 0, -1), 
                        get_ChElectron(isec, ila, ico, iqua, 0, -1)*mrad, get_SChElectron(isec, ila, ico, iqua, 0, -1)*mrad,
                        get_NElectron(isec, ila, ico, iqua, 1, -1), 
                        get_ChElectron(isec, ila, ico, iqua, 1, -1)*mrad, get_SChElectron(isec, ila, ico, iqua, 1, -1)*mrad,
                        get_NElectron(isec, ila, ico, iqua, 2, -1), 
                        get_ChElectron(isec, ila, ico, iqua, 2, -1)*mrad, get_SChElectron(isec, ila, ico, iqua, 2, -1)*mrad);
                    }
                }
                /*for (int iqua=0; iqua<225; iqua+=224){
                    double nspe = get_NElectron(isec, ila, ico, iqua, 2, -1);
                    double ndir = get_NElectron(isec, ila, ico, iqua, 0, -1);
                    if(nspe>0 && nspe==ndir){
                        System.out.format("ECCOLO ila %4d itile %3d iq %4d  [%7.2f %7.2f]\n", 201+ila, ico+1, iqua+1, nspe, ndir);
                    }
                }*/
            }
        }
        System.out.format("  \n");

        int found = 0;
        for (int ila=0; ila<4; ila++){
            for(int ico=0; ico<geocost.NAERCO[ila]; ico++){
                if(ico<2 || ico>geocost.NAERCO[ila]-3){
                    if(get_AeroStatus(isec, ila, ico)>0){
                        System.out.format("CCDB BAD  AERO   Sec %4d  Lay %4d  Compo %6d  Status %6d \n",isec, ila, ico, get_AeroStatus(isec, ila, ico));
                        found++;
                    }
                }
            }
        }
        System.out.format("Aerogels with bad status flag: %4d  \n \n",found);

    }


    //------------------------------
    public void dump_MirrorConstants(int irich) { 
    //------------------------------

        int isec = find_RICHSector(irich);
        if(isec==0) return;
        System.out.format(" \n");

        int found = 0;
        for(RICHLayerType lay: RICHLayerType.values()){
            if(lay.ccdb_ila()==301){
                int ila = lay.id();
                int ico = 0;
                if(get_MirrorStatus(isec, ila, ico)>0){
                    System.out.format("CCDB BAD MIRROR LAT Sec %4d  Lay %4d  Compo %6d  Status %6d \n",isec, ila, ico, get_MirrorStatus(isec, ila, ico));
                    found++;
                }
            }
            if(lay.ccdb_ila()==302){
                int ila = lay.id();
                // ico==0 is for global layer
                for(int ico=1; ico<11; ico++){
                    if(get_MirrorStatus(isec, ila, ico)>0){
                        System.out.format("CCDB BAD MIRROR SPE Sec %4d  Lay %4d  Compo %6d  Status %6d \n",isec, ila, ico, get_MirrorStatus(isec, ila, ico));
                        found++;
                    }
                }
            }
        }
        System.out.format("Mirrors with non-zero status flag %3d  \n \n", found);
    }


    //------------------------------
    public double get_SChElectron(int isec, int ila, int ico, int iqua, int irefle, int icharge) {
    //------------------------------


        if(ico<0 || ico>=geocost.NAERCO[ila]) return 0.0;
        int irich = find_RICHModule(isec);
        if(irich==0) return 0.0;
 
        int irow = ico*225+iqua+1;

        double dir  = 0.0;
        double lat  = 0.0;
        double sphe = 0.0;
        if(icharge==1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hp_sigma_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hp_sigma_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hp_sigma_sphe", isec, 201+ila, irow);
        }
        if(icharge==-1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hm_sigma_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hm_sigma_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hm_sigma_sphe", isec, 201+ila, irow);
        }
   
        if(irefle==0){
            if(dir>0)  return dir;
            if(lat>0)  return lat;
            if(sphe>0) return sphe;
        }
        if(irefle==1){
            if(lat>0)  return lat;
            if(dir>0)  return dir;
            if(sphe>0) return sphe;
        }
        if(irefle==2){
            if(sphe>0) return sphe;
            if(dir>0)  return dir;
            if(lat>0)  return lat;
        }
        return 0.0;
    }


    //------------------------------
    public double get_PixelGain(int isec, int ipmt, int ich) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
        return pixelTables.get(irich-1).getDoubleValue("gain", isec, ipmt, ich);
    }


    //------------------------------
    public double get_PixelEff(int isec, int ipmt, int ich) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
        return pixelTables.get(irich-1).getDoubleValue("efficiency", isec, ipmt, ich);
    }


    //------------------------------
    public double get_PixelDarkRate(int isec, int ipmt, int ich) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0;
        return pixelTables.get(irich-1).getDoubleValue("darkrate", isec, ipmt, ich);
    }


    //------------------------------
    public double get_PixelMeanTime(int isec, int ipmt, int ich) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
        return pixelTables.get(irich-1).getDoubleValue("mean_t", isec, ipmt, ich);
    }


    //------------------------------
    public double get_PixelRMSTime(int isec, int ipmt, int ich) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
        return pixelTables.get(irich-1).getDoubleValue("sigma_t", isec, ipmt, ich);
    }


    //------------------------------
    public int get_PixelStatus(int isec, int ipmt, int ianode) { 
    //------------------------------
        int irich = find_RICHModule(isec);
        if(irich==0)return 0;
        return pixstatusTables.get(irich-1).getIntValue("status", isec, ipmt, ianode);
    }


    //------------------------------
    public int get_AeroStatus(int isec, int ila, int ico) {
    //------------------------------

        int iref_quadrant = 1;
        return get_AeroStatus(isec, ila, ico, iref_quadrant);

    }


    //------------------------------
    public int get_AeroStatus(int isec, int ila, int ico, int iqua) {
    //------------------------------

        if(ico<0 || ico>=geocost.NAERCO[ila]) return 0;
        int irich = find_RICHModule(isec);
        if(irich==0)return 0;
 
        int irow = ico*225+iqua+1;
   
        return aerstatusTables.get(irich-1).getIntValue("status", isec, 201+ila, irow);
    
    }


    //------------------------------
    public int get_MirrorStatus(int isec, int ila, int ico) {
    //------------------------------

        int irich = find_RICHModule(isec);
        if(irich==0) return 0;

        int lla = get_MisaIla(ila, ico);
        int cco = get_MisaIco(ila, ico);
        if(lla==-1 || cco==-1) return 0;

        return mirstatusTables.get(irich-1).getIntValue("status", isec, lla, cco);

    }

 
    //------------------------------
    public double get_ChElectron(int isec, int ila, int ico, int iqua, int irefle, int icharge) {
    //------------------------------


        if(ico<0 || ico>=geocost.NAERCO[ila]) return 0.0;
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;

        int irow = ico*225+iqua+1;
   
        double dir  = 0.0;
        double lat  = 0.0;
        double sphe = 0.0;
        if(icharge==1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hp_mean_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hp_mean_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hp_mean_sphe", isec, 201+ila, irow);
        }
        if(icharge==-1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hm_mean_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hm_mean_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hm_mean_sphe", isec, 201+ila, irow);
        }
   
        if(irefle==0){
            if(dir>0)  return dir;
            if(lat>0)  return lat;
            if(sphe>0) return sphe;
        }
        if(irefle==1){
            if(lat>0)  return lat;
            if(dir>0)  return dir;
            if(sphe>0) return sphe;
        }
        if(irefle==2){
            if(sphe>0) return sphe;
            if(dir>0)  return dir;
            if(lat>0)  return lat;
        }
        return 0.0;
    }


    //------------------------------
    public double get_NElectron(int isec, int ila, int ico, int iqua, int irefle, int icharge) {
    //------------------------------


        if(ico<0 || ico>=geocost.NAERCO[ila]) return 0.0;
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
 
        int irow = ico*225+iqua+1;
   
        double dir  = 0.0;
        double lat  = 0.0;
        double sphe = 0.0;
        if(icharge==1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hp_npe_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hp_npe_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hp_npe_sphe", isec, 201+ila, irow);
        }
        if(icharge==-1){
            dir  = anglerefTables.get(irich-1).getDoubleValue("hm_npe_dir", isec, 201+ila, irow);
            lat  = anglerefTables.get(irich-1).getDoubleValue("hm_npe_lat", isec, 201+ila, irow);
            sphe = anglerefTables.get(irich-1).getDoubleValue("hm_npe_sphe", isec, 201+ila, irow);
        }
   
        if(irefle==0) return dir;
        if(irefle==1) return lat;
        if(irefle==2) return sphe;
        return 0.0;
    }


    //------------------------------
    public double get_PixelTimeOff(int isec, int ipmt, int ich){
    //------------------------------
 
        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;

        return -1*timeoffTables.get(irich-1).getDoubleValue("offset", isec, ipmt, ich) + richpar.OFFSET_TIME;

    }


    //------------------------------
    public double get_PixelTimeWalk(int isec, int ipmt, int duration){
    //------------------------------

        int irich = find_RICHModule(isec);
        if(irich==0)return 0.0;
 
        double twalk_corr = 0;
        double D0 = timewalkTables.get(irich-1).getDoubleValue("D0", isec, ipmt, 0);
        double T0 = timewalkTables.get(irich-1).getDoubleValue("m1", isec, ipmt, 0);
        double m1 = timewalkTables.get(irich-1).getDoubleValue("m2", isec, ipmt, 0);
        double m2 = timewalkTables.get(irich-1).getDoubleValue("T0", isec, ipmt, 0);

        double f1 = m1 * duration + T0;
        double f1T = m1 * D0 + T0;

        double f2 = m2 * (duration - D0) + f1T;
        twalk_corr = f1;
        if (duration > D0) twalk_corr = f2;

        return twalk_corr;
    }


    //------------------------------
    public int find_RICHModule(int isec){
    //------------------------------

        if( richTable.hasEntry(isec,0,0)){
            return richTable.getIntValue("module", isec, 0, 0);
        }
        return 0;
    }


    //------------------------------
    public int find_RICHSector(int irich){
    //------------------------------
        int debugMode = 0;

        for (int isec=1; isec<=RICHGeoConstants.NSEC; isec++){
            if(richTable.hasEntry(isec,0,0)){
                if(debugMode>=1)System.out.format(" trovo %4d <--> %4d \n",irich, richTable.getIntValue("module", isec, 0, 0));
                if(richTable.getIntValue("module", isec, 0, 0) == irich)  return isec;
            }
        }
        return 0;
    }


    //------------------------------
    public int get_MisaIla(int ila, int ico){
    //------------------------------

        // 0 = global layer
        if(ila<0 || ila>RICHLayerType.values().length) return -1;
        int ncompo = 0;
        ncompo = RICHLayerType.get_Type(ila).ncompo();
        if(ico<0 || ico>ncompo) return -1;

        // global RICH
        for(RICHLayerType lay: RICHLayerType.values())
            if (lay.id() == ila)
                return lay.ccdb_ila();
        return 0;

    }


    //------------------------------
    public int get_MisaIco(int ila, int ico) {
    //------------------------------

        if(ila<0 || ila>RICHLayerType.values().length) return -1;
        int ncompo = 0;
        ncompo = RICHLayerType.get_Type(ila).ncompo();
        if(ico<0 || ico>ncompo) return -1;

        for(RICHLayerType lay: RICHLayerType.values()){
            if (lay.id() == ila){

                if(lay.ccdb_ila()==301){
                        return lay.ccdb_ico();
                }else{
                        return ico;
                }
            }
        }
        return 0;
    }

}
