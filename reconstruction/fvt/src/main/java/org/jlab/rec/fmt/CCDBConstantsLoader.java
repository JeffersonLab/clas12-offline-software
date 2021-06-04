package org.jlab.rec.fmt;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler
 */
public class CCDBConstantsLoader {


    public static final synchronized void Load(int runNb, String variation) {

        double[] EFF_Z_OVER_A;
        double[] EFFX0;
        double[] REL_POS;

        // Load the tables
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(10, variation);

        // Load material budget:
        dbprovider.loadTable("/test/mvt/fmt_mat");

        // Material budget
        // ===============
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

        // Get common variables for all disks and apply shifts
        // ===============
	    dbprovider.loadTable("/geometry/fmt/fmt_global");
	    Constants.hDrift            = dbprovider.getDouble("/geometry/fmt/fmt_global/hDrift", 0)/10.;
	    Constants.FVT_Pitch         = dbprovider.getDouble("/geometry/fmt/fmt_global/Pitch", 0)/10.;
	    Constants.FVT_Interstrip    = dbprovider.getDouble("/geometry/fmt/fmt_global/Interstrip", 0)/10.;
        Constants.FVT_Beamhole      = dbprovider.getDouble("/geometry/fmt/fmt_global/R_min", 0)/10.;
	    Constants.FVT_Nstrips       = dbprovider.getInteger("/geometry/fmt/fmt_global/N_strip", 0);
	    Constants.FVT_Halfstrips    = dbprovider.getInteger("/geometry/fmt/fmt_global/N_halfstr", 0);
	    Constants.FVT_stripsXloc    = new double[Constants.FVT_Nstrips][2]; // Give the local end-points x-coordinates of the strip segment
        Constants.FVT_stripsYloc    = new double[Constants.FVT_Nstrips][2]; // Give the local end-points y-coordinates of the strip segment
        Constants.FVT_stripsXlocref = new double[Constants.FVT_Nstrips]; // Give the local ref-points x-coordinates of the strip segment
        Constants.FVT_stripsYlocref = new double[Constants.FVT_Nstrips]; // Give the local ref-points y-coordinates of the strip segment
        Constants.FVT_stripsX       = new double[Constants.FVT_Nlayers][Constants.FVT_Nstrips][2]; // Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
        Constants.FVT_stripsY       = new double[Constants.FVT_Nlayers][Constants.FVT_Nstrips][2]; // Give the  end-points y-coordinates of the strip segment
        Constants.FVT_stripslength  = new double[Constants.FVT_Nstrips]; // Give the strip length

        // Position and strip orientation of each disks
        // ===============
        dbprovider.loadTable("/geometry/fmt/fmt_layer_noshim");

        Constants.FVT_Zlayer = new double[dbprovider.length("/geometry/fmt/fmt_layer_noshim/Z")];
        Constants.FVT_Alpha  = new double[dbprovider.length("/geometry/fmt/fmt_layer_noshim/Z")];
        for (int i = 0; i < dbprovider.length("/geometry/fmt/fmt_layer_noshim/Z"); i++) {
            Constants.FVT_Zlayer[i] = dbprovider.getDouble("/geometry/fmt/fmt_layer_noshim/Z", i)/10.;
            Constants.FVT_Alpha[i]  = Math.toRadians(dbprovider.getDouble("/geometry/fmt/fmt_layer_noshim/Angle", i));
        }

        dbprovider.disconnect();

        System.out.println("SUCCESSFULLY LOADED FMT material budget CONSTANTS....");
    }

    /**
     * Get alignment information from CCDB.
     * @param run       : run number.
     * @param constMngr : constants manager, where FMT shifts are obtained.
     * @return a two-dimensional array storing alignment information. Rows define the 6 FMT layers,
     *         and columns are [deltaX, deltaY, deltaZ, rotX, rotY, rotZ].
     */
    public static final synchronized double[][] loadAlignmentTable(int run, ConstantsManager constMngr) {
        IndexedTable fmtShifts = constMngr.getConstants(run, "/geometry/fmt/alignment");
        String[] sn = new String[] {"deltaX", "deltaY", "deltaZ", "rotX", "rotY", "rotZ"};
        double[][] shArr = new double[Constants.FVT_Nlayers][6];
        for (int li = 0; li < Constants.FVT_Nlayers; ++li) {
            // Get shifts and convert them to centimeters.
            for (int si = 0; si < 3; ++si)
                shArr[li][si] = fmtShifts.getDoubleValue(sn[si], 0,li+1,0) / 10.;
            // Get rotations and convert to radians.
            for (int si = 3; si < 6; ++si)
                shArr[li][si] = Math.toRadians(fmtShifts.getDoubleValue(sn[si], 0,li+1,0));
        }

        return shArr;
    }
}
