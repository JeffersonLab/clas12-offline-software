package org.jlab.detector.geom.RICH;

import org.jlab.geom.prim.Vector3D;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import java.io.FileReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RICHGeoCalibration {

    // Default values of RICH Reconstruction parameters to be re-loaded from CCDB or TxT

    private RICHGeoParameters  geopar;

    private final static int NLAY   = RICHGeoConstants.NLAY;
    private final static int NCOMPO = RICHGeoConstants.NCOMPO;

    private final static int NALAY      = RICHGeoConstants.NAERLAY;
    private final static int NAMAX      = RICHGeoConstants.NAERMAX;
    
    public IndexedTable richTable               = new IndexedTable(3, "module/I");

    private ArrayList<IndexedTable> alignTables =   new ArrayList();
    private ArrayList<IndexedTable> aeroTables  =   new ArrayList();
    
    private IndexedTable alignConstants;


    // ----------------
    public RICHGeoCalibration() {
    // ----------------

    }


    //------------------------------
    public void load_CCDB(ConstantsManager manager, int run, int ncalls, RICHGeoParameters geopar){
    //------------------------------

        int debugMode = 0;

        this.geopar = geopar;

        if(RICHGeoConstants.RICH_TABLE_FROM_FILE==1){
            //init_RICHTableTxT();
            init_RICHTableDummy();
            if((debugMode>=1 || geopar.DEBUG_GEO_CONSTS>=1) && ncalls<Math.max(1,geopar.DEBUG_GEO_CONSTS)) dump_RICHSetup("TXT  ");
            
        }else{
            init_RICHTableCCDB( manager.getConstants(run, "/geometry/rich/setup") );
            if((debugMode>=1 || geopar.DEBUG_GEO_CONSTS>=1) && ncalls<Math.max(1,geopar.DEBUG_GEO_CONSTS)) {
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("RICH: Load GEO Setup from CCDB for run %6d \n", run);
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("Banks \n /geometry/rich/setup \n");

                dump_RICHSetup("CCDB ");
                }
        }

        for(int irich=1; irich<=nRICHes(); irich++){

            int isec = find_RICHSector(irich);

            if(isec==0) continue;
            String aero_bank = String.format("/geometry/rich/module%1d/aerogel",irich);  
            String alig_bank = String.format("/geometry/rich/module%1d/alignment",irich);
            init_GeoCalibrationCCDB( manager.getConstants(run, aero_bank),
                                     manager.getConstants(run, alig_bank) );

            if((debugMode>=1 || geopar.DEBUG_GEO_CONSTS>=1) && ncalls<Math.max(1,geopar.DEBUG_GEO_CONSTS)) {
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("RICH: Load GEO Calibration from CCDB for RICH %4d  sector %4d  run %6d \n", irich, isec, run);
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("Banks \n %s \n %s \n",aero_bank,alig_bank);

                dump_GeoCalibration(isec, "CCDB ");
            }

            if(RICHGeoConstants.ALIGN_TABLE_FROM_FILE==1 && ncalls==0){

                init_AliCalibrationTxT(irich, ncalls);

                if((debugMode>=1 || geopar.DEBUG_GEO_CONSTS>=1) && ncalls<Math.max(1,geopar.DEBUG_GEO_CONSTS)) {
                        System.out.format("------------------------------------------------------------- \n");
                        System.out.format("RICH: Load ALI Calibration from local TxT file for RICH 4d  sector %4d  run %6d \n", irich, isec, run);
                        System.out.format("------------------------------------------------------------- \n");

                        dump_AliCalibration(isec, "TXT  ");
                }
            }

            if(RICHGeoConstants.AERO_OPTICS_FROM_FILE==1 && ncalls==0){

                init_AerCalibrationTxT(ncalls);

                if((debugMode>=1 || geopar.DEBUG_GEO_CONSTS>=1) && ncalls<=Math.max(1,geopar.DEBUG_GEO_CONSTS)) {
                        System.out.format("------------------------------------------------------------- \n");
                        System.out.format("RICH: Load AER Calibration from local TxT file for RICH 4d  sector %4d  run %6d \n", irich, isec, run);
                        System.out.format("------------------------------------------------------------- \n");

                        dump_AerCalibration(isec, "TXT  ");
                }
            }
        }
    }


    //------------------------------
    public void init_GeoCalibrationCCDB(IndexedTable aeroConstants, IndexedTable alignConstants){
    //------------------------------

        int debugMode = 0;

        alignTables.add(alignConstants);
        aeroTables.add(aeroConstants);

    }


    //------------------------------
    public void init_AerCalibrationTxT(int ncalls){
    //------------------------------

    }


    //------------------------------
    public void init_AliCalibrationTxT(int irich, int ncalls){
    //------------------------------

        int debugMode = 0;

        double sscale = geopar.ALIGN_SHIFT_SCALE;
        double ascale = geopar.ALIGN_ANGLE_SCALE / RICHGeoConstants.MRAD;  // to convert in rad

        String ali_filename = "calibration/rich/module"+irich+"/alignment.txt";

        try {

            BufferedReader bf = new BufferedReader(new FileReader(ali_filename));
            String currentLine = null;

            while ( (currentLine = bf.readLine()) != null) {    

                String[] array = currentLine.split(" ");
                int isec = Integer.parseInt(array[0]);
                int lla = Integer.parseInt(array[1]);
                int cco = Integer.parseInt(array[2]);

                float  dx  = Float.parseFloat(array[3]);
                float  dy  = Float.parseFloat(array[4]);
                float  dz  = Float.parseFloat(array[5]);
                float  thx = Float.parseFloat(array[6]);
                float  thy = Float.parseFloat(array[7]);
                float  thz = Float.parseFloat(array[8]);

                int check = find_RICHSector(irich);

                int[] ind = {0,0};
                if(check==isec && convert_indexes(lla, cco, ind)){

                    int ila=ind[0];
                    int ico=ind[1];
                    if(debugMode>=1){
                        System.out.format("ALIGN conversion %4d %3d --> %4d %3d \n",lla,cco,ila,ico);
                        System.out.format(" --> %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f  %7.3f %7.3f\n",dx,dy,dz,thx,thy,thz,sscale,ascale);
                        int tla = get_MisaIla(ila, ico);
                        int tco = get_MisaIco(ila, ico);
                        System.out.format("test conversion rich %3d %3d --> ccdb %3d %3d \n",ila,ico,tla,tco);
                    }

                    // the rotation is assumed to be in the component local ref system

                    add_AlignShift(isec, ila, ico, new Vector3D(dx, dy, dz) );
                    add_AlignAngle(isec, ila, ico, new Vector3D(thx, thy, thz) );

                }else{
                    System.out.format("Unsupported alignment for layer %3d %3d \n",lla,cco);
                }
            }

        } catch (Exception e) {

            System.err.format("Exception occurred trying to read '%s' \n", ali_filename);
            e.printStackTrace();
        }

    }


    //------------------------------
    public void init_RICHTableCCDB(IndexedTable richTable){
    //------------------------------

        this.richTable = richTable;

    }


    //------------------------------
    public void init_RICHTableDummy(){
    //------------------------------

        int debugMode = 0;

        int irich[] = {0, 0, 0, 1, 0, 0};
        int ila = 0;
        int ico = 0;
        
        for (int isec=1; isec<=6; isec++) {
                richTable.addEntry(isec, ila, ico);
                richTable.setIntValue(irich[isec-1], "module", isec, ila, ico);

                if(debugMode>=1)System.out.format("Found RICH module %4d in sector %4d \n",irich[isec-1], isec);

        }

    }
  

    //------------------------------
    public void init_RICHTableTxT(){
    //------------------------------

        int debugMode = 0;

        String tt_filename = new String("geometry/rich/tt.txt");
        if(debugMode>=1)System.out.format(" Reading RICH Modules from file %s \n", tt_filename);

        try {

            BufferedReader bf = new BufferedReader(new FileReader(tt_filename));
            String currentLine = null;

            while ( (currentLine = bf.readLine()) != null) {

                String[] array = currentLine.split(" ");
                int    isec    = Integer.parseInt(array[0]);
                int    ila     = Integer.parseInt(array[1]);
                int    ico     = Integer.parseInt(array[2]);
                int    imod    = Integer.parseInt(array[3]);

                richTable.addEntry(isec, ila, ico);
                richTable.setIntValue(imod, "module", isec, ila, ico);

                 if(debugMode>=1){
                     int irich = richTable.getIntValue("module", isec, ila, ico);
                     System.out.format(" TT table  sector %4d %4d %4d -->  module %6d \n",isec,ila,ico,irich);
                 }
            }

        } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", tt_filename);
                e.printStackTrace();
        }

        if(debugMode>=1){
            System.out.format("\n");
            for(int k=1; k<=6; k++)if(richTable.hasEntry(k,0,0))
                 System.out.format(" Table entry %4d %6d \n",k,richTable.getIntValue("module", k, 0, 0));
            System.out.format("\n");
            for(RICHLayerType lay: RICHLayerType.values()){
                System.out.format(" LAY %18s %10s %4d %4d %4d %10s \n", lay.name(), lay.label(), lay.id(), lay.ccdb_ila(), lay.type(), lay.vers());
            }
            System.out.format("\n");
        }
    }


    //------------------------------
    public void init_GeoParametersTxT(int ifile){
    //------------------------------
    // To be moved to CCDB

       int debugMode = 0;

        if(ifile==1){

            /*
            * DC_OFFSETs
            */
            String dcoff_filename = new String("CALIB_DATA/DC_offsets_4013.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(dcoff_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {    

                    String[] array = currentLine.split(" ");
                    int idc    = Integer.parseInt(array[0]);
                    int imatch = Integer.parseInt(array[1]);
                    int iref   = Integer.parseInt(array[2]);
                    int ipiv   = Integer.parseInt(array[3]);
                    int isur   = Integer.parseInt(array[4]);

                    float  ss  = Float.parseFloat(array[5]);
                    float  sa  = Float.parseFloat(array[6]);

                    int inp    = Integer.parseInt(array[7]);
                    
                    float  hr  = Float.parseFloat(array[8]);

                    geopar.DO_ALIGNMENT    = idc;
                    geopar.ALIGN_PMT_PIVOT  = ipiv;
                    geopar.APPLY_SURVEY    = isur;

                    geopar.ALIGN_SHIFT_SCALE     = (double) ss ;
                    geopar.ALIGN_ANGLE_SCALE     = (double) sa;

                    if(debugMode>=1 || geopar.DEBUG_GEO_CONSTS>0){

                        System.out.format("TEXT PARA    DO_ALIGNMENT                 %7d \n", geopar.DO_ALIGNMENT);
                        System.out.format("TEXT PARA    ALIGN_PMT_PIVOT              %7d \n", geopar.ALIGN_PMT_PIVOT);
                        System.out.format("TEXT PARA    APPLY_SURVEY                 %7d \n", geopar.APPLY_SURVEY);

                        System.out.format("TEXT PARA    ALIGN_SHIFT_SCALE            %7.3f \n", geopar.ALIGN_SHIFT_SCALE);
                        System.out.format("TEXT PARA    ALIGN_ANGLE_SCALE            %7.3f \n", geopar.ALIGN_ANGLE_SCALE);

                    }

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", dcoff_filename);
                e.printStackTrace();
            }

        }



        if(ifile==3){

            /*
            * AEROGEL NOMINAL OPTCIS
            */

            String aero_filename = new String("CALIB_DATA/aerogel_passports.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(aero_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {    

                    String[] array = currentLine.split(" ");
                    int idlay = Integer.parseInt(array[1]);
                    int iaer = Integer.parseInt(array[2]);
                    
                    if(debugMode>=1)System.out.format("Read optics for AERO lay %3d  compo %3d", idlay, iaer); 
                    float refi = Float.parseFloat(array[5]);
                    float plana = Float.parseFloat(array[11]);
                    //aero_refi[idlay-201][iaer-1] = refi;
                    //aero_plan[idlay-201][iaer-1] = plana;
                    //aero_refi[idlay-201][iaer-1] = (float) RICHConstants.RICH_AEROGEL_INDEX;
                    //if(debugMode>=1)System.out.format(" n = %8.5f   pla = %8.2f \n", aero_refi[idlay-201][iaer-1], aero_plan[idlay-201][iaer-1]);
                    
                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", aero_filename);
                e.printStackTrace();
            }

            if(debugMode>=1)System.out.format("initConstants: DONE \n");
            
        }

    }


    //------------------------------
    public boolean convert_indexes(int lla, int cco, int[] ind){
    //------------------------------

        //int[] lateral_compo = {11,5,6,9,10,7,8};
        int[] lateral_compo = {5,6,7,8,9,10,11};

        /*
        *  Aerogel
        */
        if(lla==0 && cco==0){ind[0]=lla; ind[1]=cco; return true;}
        if(lla>=201 && lla<=204 && cco==0){ind[0]=lla-200; ind[1]=cco; return true;}
        if(lla==301 && cco>0 && cco<=7) {ind[0]=lateral_compo[cco-1]; ind[1]=0; return true;}
        if(lla==302){
            if(cco==0){ind[0]=12; ind[1]=0; return true;}
            if(cco>0 && cco<11){ind[0]=12; ind[1]=cco; return true;}
        }
        if(lla==401 && cco==0){ind[0]=13; ind[1]=cco; return true;}

        return false;
    }


    //------------------------------
    public int nRICHes(){
    //------------------------------
        return richTable.getRowCount();
    }


    //------------------------------
    public int find_RICHSector(int irich){
    //------------------------------

        int debugMode = 0;

        for (int isec=1; isec<=RICHGeoConstants.NSEC; isec++){
            if(richTable.hasEntry(isec,0,0)){
                if(debugMode>=1)System.out.format(" found %4d %4d <--> %4d \n",irich,isec, richTable.getIntValue("module", isec, 0, 0));
                if(richTable.getIntValue("module", isec, 0, 0) == irich) return isec;
            }
        }
        return 0;
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
    public void dump_RICHSetup(String head) {
    //------------------------------

        System.out.format(" RICH SETUP \n");
        for (int isec=1; isec<=RICHGeoConstants.NSEC; isec++){
            if(richTable.hasEntry(isec,0,0)){
                System.out.format(" sector %4d <--> module %4d \n",isec, richTable.getIntValue("module", isec, 0, 0));
            }
        }
    }


    //------------------------------
    public void dump_GeoCalibration(int isec, String head) {
    //------------------------------

        dump_AliCalibration(isec, head);
        dump_AerCalibration(isec, head);

    }


    //------------------------------
    public void dump_AliCalibration(int isec, String head){
    //------------------------------

        System.out.format(" \n");

        int found = 0;
        for(int ila=0; ila<=RICHLayerType.values().length; ila++){
            int ncompo = 0;
            String label = "GLOBAL";
            if(ila>0){
                ncompo = RICHLayerType.get_Type(ila-1).ncompo();
                label  = RICHLayerType.get_Type(ila-1).label();
            }
            //System.out.format("NCOMPO %d %d \n",ila, ncompo);
            for(int ico=0; ico<=ncompo; ico++){
            
                int lla = get_MisaIla(ila, ico);
                int cco = get_MisaIco(ila, ico);
                //System.out.format("NCOMPO ila %d %d --> lla %d %d \n",ila, ico, lla, cco);
                if(lla==-1 || cco==-1) continue;

                if(get_AlignShift(isec, ila, ico).multiply(10.).mag()>1.e-3 || get_AlignAngle(isec, ila, ico).multiply(1000.).mag()>1.e-3){
                    System.out.format("%s ALIGN  %s %4d %3d  (%4d %3d)  shift (mm) %s  angle (mrad) %s \n", head, label, lla,cco, ila,ico,
                        get_AlignShift(isec,ila,ico).multiply(10.).toStringBrief(2), get_AlignAngle(isec,ila,ico).multiply(1000.).toStringBrief(2));
                    found++;
                }
            }
        }
        System.out.format("Misalignments with non-zero values %3d \n \n",found);

    }

    //------------------------------
    public void dump_AerCalibration(int isec, String head){
    //------------------------------

        System.out.format("Aerogel Nominal Constants:\n");
        for (int ila=0; ila<NALAY; ila++){
            for (int ico=0; ico<RICHGeoConstants.NAERCO[ila]; ico++){
                if(ico>=2 && ico<RICHGeoConstants.NAERCO[ila]-2) continue;   // to limit redout
                System.out.format("%s AERO   isec %4d layer %4d ico %3d  n = %8.5f  pla = %8.2f\n", head, isec, 201+ila, ico+1, 
                             get_AeroNomIndex(isec, ila, ico), get_AeroNomPlanarity(isec, ila, ico) );
            }
        }
        System.out.format("\n");
    }


    //------------------------------
    public double get_AeroNomIndex(int isec, int ila, int ico){
    //------------------------------
        
        int irich = find_RICHModule(isec);
        if(irich==0) return 1.0;

        return aeroTables.get(irich-1).getDoubleValue("n400", isec, 201+ila, ico+1);

    }


    //------------------------------
    public double get_AeroNomPlanarity(int isec, int ila, int ico){
    //------------------------------
        
        int irich = find_RICHModule(isec);
        if(irich==0) return 0.0;

        return aeroTables.get(irich-1).getDoubleValue("planarity", isec, 201+ila, ico+1);

    }


    //------------------------------
    public Vector3D get_AlignShift(int isec, int ila, int ico){
    //------------------------------

        int irich = find_RICHModule(isec);
        if(irich==0) return new Vector3D(0., 0., 0.);

        int lla = get_MisaIla(ila, ico);
        int cco = get_MisaIco(ila, ico);
        if(lla==-1 || cco==-1) return new Vector3D(0., 0., 0.);

        double dx =  alignTables.get(irich-1).getDoubleValue("dx", isec, lla, cco);
        double dy =  alignTables.get(irich-1).getDoubleValue("dy", isec, lla, cco);
        double dz =  alignTables.get(irich-1).getDoubleValue("dz", isec, lla, cco);
    
        double sscale = geopar.ALIGN_SHIFT_SCALE;
        return new Vector3D( dx*sscale,  dy*sscale,  dz*sscale);

    }


    //------------------------------
    public void add_AlignShift(int isec, int ila, int ico, Vector3D shift){
    //------------------------------

        int debugMode = 0;
        
        int irich = find_RICHModule(isec);
        if(debugMode==1)System.out.format("add_AlignShift for irich %4d isec %4d ila %4d ico %4d %s \n",
                    irich, isec, ila, ico, shift.toStringBrief(2));

        if(irich==0) return;

        int lla = get_MisaIla(ila, ico);
        int cco = get_MisaIco(ila, ico);
        if(lla==-1 || cco==-1) return;

        double dx = shift.x();
        double dy = shift.y();
        double dz = shift.z();

        if( alignTables.get(irich-1).hasEntry(isec, lla, cco)) {

            if(debugMode==1){
                Vector3D val = new Vector3D( alignTables.get(irich-1).getDoubleValue("dx", isec, lla, cco),
                                             alignTables.get(irich-1).getDoubleValue("dy", isec, lla, cco),
                                             alignTables.get(irich-1).getDoubleValue("dz", isec, lla, cco) );
                System.out.format("table exists with %s \n", val.toStringBrief(2));
            }

            dx += alignTables.get(irich-1).getDoubleValue("dx", isec, lla, cco);
            dy += alignTables.get(irich-1).getDoubleValue("dy", isec, lla, cco);
            dz += alignTables.get(irich-1).getDoubleValue("dz", isec, lla, cco);

        }else{

            if(debugMode==1)System.out.format("table does not exist --> create entry \n");
            alignTables.get(irich-1).addEntry(isec, lla, cco);

        }

        if(debugMode==1)System.out.format("table fill with %7.2f %7.2f %7.2f \n", dx, dy, dz); 
        alignTables.get(irich-1).setDoubleValue(dx, "dx", isec, lla, cco);
        alignTables.get(irich-1).setDoubleValue(dy, "dy", isec, lla, cco);
        alignTables.get(irich-1).setDoubleValue(dz, "dz", isec, lla, cco);

    }


    //------------------------------
    public Vector3D get_AlignAngle(int isec, int ila, int ico){
    //------------------------------
        
        int irich = find_RICHModule(isec);
        if(irich==0) return new Vector3D(0., 0., 0.);

        int lla = get_MisaIla(ila, ico);
        int cco = get_MisaIco(ila, ico);
        if(lla==-1 || cco==-1) return new Vector3D(0., 0., 0.);

        double dthx = (double) alignTables.get(irich-1).getDoubleValue("dthx", isec, lla, cco);
        double dthy = (double) alignTables.get(irich-1).getDoubleValue("dthy", isec, lla, cco);
        double dthz = (double) alignTables.get(irich-1).getDoubleValue("dthz", isec, lla, cco);

        double ascale = geopar.ALIGN_ANGLE_SCALE / RICHGeoConstants.MRAD;  
        return new Vector3D(dthx*ascale, dthy*ascale, dthz*ascale);

    }


    //------------------------------
    public void add_AlignAngle(int isec, int ila, int ico, Vector3D angle){
    //------------------------------

        int debugMode = 0;
        
        int irich = find_RICHModule(isec);
        if(debugMode==1)System.out.format("add_Alignangle for irich %4d isec %4d ila %4d ico %4d %s\n",
                    irich, isec, ila, ico, angle.toStringBrief(2));

        if(irich==0) return;

        int lla = get_MisaIla(ila, ico);
        int cco = get_MisaIco(ila, ico);
        if(lla==-1 || cco==-1) return;

        if(debugMode==1)System.out.format(" --> lla %4d cco %4d \n", lla, cco);

        double dthx = angle.x();
        double dthy = angle.y();
        double dthz = angle.z();

        if( alignTables.get(irich-1).hasEntry(isec, lla, cco)) {

            if(debugMode==1){
                Vector3D val = new Vector3D( alignTables.get(irich-1).getDoubleValue("dthx", isec, lla, cco),
                                             alignTables.get(irich-1).getDoubleValue("dthy", isec, lla, cco),
                                             alignTables.get(irich-1).getDoubleValue("dthz", isec, lla, cco) );
                System.out.format("table exists with %s \n", val.toStringBrief(2));
            }

            dthx += alignTables.get(irich-1).getDoubleValue("dthx", isec, lla, cco);
            dthy += alignTables.get(irich-1).getDoubleValue("dthy", isec, lla, cco);
            dthz += alignTables.get(irich-1).getDoubleValue("dthz", isec, lla, cco);

        }else{

            if(debugMode==1)System.out.format("table does not exist --> create entry \n");
            alignTables.get(irich-1).addEntry(isec, lla, cco);

        }

        if(debugMode==1)System.out.format("table fill with %7.2f %7.2f %7.2f \n", dthx, dthy, dthz); 
        alignTables.get(irich-1).setDoubleValue(dthx, "dthx", isec, lla, cco);
        alignTables.get(irich-1).setDoubleValue(dthy, "dthy", isec, lla, cco);
        alignTables.get(irich-1).setDoubleValue(dthz, "dthz", isec, lla, cco);

    }


    //------------------------------
    public int get_MisaIla(int ila, int ico){
    //------------------------------

        if(ila<0 || ila>=RICHLayerType.values().length) return -1;
        int ncompo = 0;
        if(ila>0)ncompo = RICHLayerType.get_Type(ila-1).ncompo();
        if(ico<0 || ico>ncompo) return -1;

        // global RICH
        if(ila>0){
            for(RICHLayerType lay: RICHLayerType.values())
                if (lay.id() == ila-1)
                    return lay.ccdb_ila();
        }
        return 0;

    }


    //------------------------------
    public int get_MisaIco(int ila, int ico) {
    //------------------------------

        if(ila<0 || ila>=RICHLayerType.values().length) return -1;
        int ncompo = 0;
        if(ila>0)ncompo = RICHLayerType.get_Type(ila-1).ncompo();
        if(ico<0 || ico>ncompo) return -1;

        // CCDB uses 0 as global rich
        if(ila>0){
            for(RICHLayerType lay: RICHLayerType.values()){
                if (lay.id() == ila-1){

                    if(lay.ccdb_ila()==301){
                        return lay.ccdb_ico();
                    }else{
                        return ico;
                    }
                }
            }
        }
        return 0;
    }

}
