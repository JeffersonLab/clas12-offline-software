/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.fvt.fmt;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author ziegler
 */
public class CCDBConstantsLoader {
    
    static boolean CSTLOADED = false;

    // static FTOFGeant4Factory geometry ;
    private static DatabaseConstantProvider DB;
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(
            10, "default");

    public static final synchronized void Load(int runNb) {
        double[] EFF_Z_OVER_A;
        double[] EFFX0  ;
        double[] REL_POS;
        // Load the tables
        
        
        //load material budget:
        dbprovider.loadTable("/test/mvt/fmt_mat");
        
        dbprovider.disconnect();

    //material budget
    //===============
        EFF_Z_OVER_A = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
        EFFX0 = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
        REL_POS = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
        for (int i = 0; i < dbprovider.length("/test/mvt/fmt_mat/thickness"); i++) {
            double RelPos = dbprovider.getDouble("/test/mvt/fmt_mat/relative_position", i);
            double Zeff =  dbprovider.getDouble("/test/mvt/fmt_mat/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/fmt_mat/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/fmt_mat/x0", i);
            EFF_Z_OVER_A[i] = Zeff/Aeff;      
            EFFX0[i] = X0;
            REL_POS[i] = RelPos; 
        }
        Constants.setEFF_Z_OVER_A(EFF_Z_OVER_A);
        Constants.set_X0(EFFX0);
        Constants.set_RELPOS(REL_POS);
        
        CSTLOADED = true;
        System.out
                .println("SUCCESSFULLY LOADED FMT material budget CONSTANTS....");
        setDB(dbprovider);
    }

    public static final synchronized DatabaseConstantProvider getDB() {
        return DB;
    }

    public static final synchronized void setDB(DatabaseConstantProvider dB) {
        DB = dB;
    }
}
