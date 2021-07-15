package org.jlab.rec.fmt;


/**
 *
 * @author defurne, ziegler
 */
public class Constants {

    public static final int FVT_Nlayers = 6;
    public static int FVT_Nstrips; // Number of strips: 1024
    public static int FVT_Halfstrips; // In the middle of the FMT, 320 strips are split in two.
    public static int FVT_Sidestrips; // 192

    // units = cm
    public static int MAX_NB_CROSSES = 30;

    public static double FVT_Pitch; // strip width 525 um
    public static double FVT_Interstrip; // inter strip
    public static double FVT_YCentral;
    public static double FVT_Rmax;

    public static double FVT_Beamhole; // Radius of the hole in the center for the beam.
    public static double FVT_SigmaS;

    public static double[] FVT_Zlayer; // Give z-coordinate of the layer
    public static double[] FVT_Alpha; // Give the rotation angle to apply

    public static double[][] FVT_stripsXloc; // Give the local end-points x-coordinates of the strip segment
    public static double[][] FVT_stripsYloc; // Give the local end-points y-coordinates of the strip segment
    public static double[] FVT_stripsXlocref; // Give the local ref-points x-coordinates of the strip segment
    public static double[] FVT_stripsYlocref; // Give the local ref-points y-coordinates of the strip segment
    public static double[][][] FVT_stripsX; // Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
    public static double[][][] FVT_stripsY; // Give the  end-points y-coordinates of the strip segment

    public static double[] FVT_stripslength; // Give the strip length
    public static double hDrift; // Thickness of the drift region in the micromegas

    public static double[] FVT_zShift; // z shift from alignment
    public static double[] FVT_zRot;   // alpha shift from alignment
    public static double[] FVT_xShift; // x shift from alignment
    public static double[] FVT_xRot;   // x rotation from alignment
    public static double[] FVT_yShift; // y shift from alignment
    public static double[] FVT_yRot;   // y rotation from alignment

    private static double[] EFF_Z_OVER_A; // for ELOSS
    private static double[] _X0;          // for M.Scat.
    private static double[] _REL_POS;     // relative postion of the material wrt to strip plane
    public static boolean areConstantsLoaded = false;

    // ----- cut based cand select
    public static  double phi12cut = 35.;
    public static  double phi13cut = 35.;
    public static  double drdzcut  =  1.5;

    // ----- end cut based cand select
    public static double CIRCLECONFUSION = 1.2; // cm

    // min path for final swimming to beamline to reject failed swimming
    public static double MIN_SWIM_PATH = 0.2; 
    
    public static void Load() {

        FVT_Sidestrips    = (FVT_Nstrips - 2*FVT_Halfstrips)/2;                  // 192
        FVT_YCentral      = (double) FVT_Halfstrips*FVT_Pitch/2.;
        FVT_Rmax          = FVT_Pitch * (FVT_Halfstrips + 2*FVT_Sidestrips)/2.; // 184.8 mm
        FVT_SigmaS        = FVT_Pitch/Math.sqrt(12);
        FVT_stripsXloc    = new double[FVT_Nstrips][2];
        FVT_stripsYloc    = new double[FVT_Nstrips][2];
        FVT_stripsXlocref = new double[FVT_Nstrips];
        FVT_stripsYlocref = new double[FVT_Nstrips];
        FVT_stripsX       = new double[FVT_Nlayers][FVT_Nstrips][2];
        FVT_stripsY       = new double[FVT_Nlayers][FVT_Nstrips][2];
        FVT_stripslength  = new double[FVT_Nstrips];

        for (int i = 0; i < FVT_Nstrips; i++) {
            // Give the Y of the middle of the strip
            if (i < 512){
                FVT_stripsYloc[i][0] = -FVT_Rmax + (511-i+0.5)*FVT_Pitch;
                FVT_stripsYloc[i][1] = -FVT_Rmax + (511-i+0.5)*FVT_Pitch;
            } else {
                FVT_stripsYloc[i][0] =  FVT_Rmax - (1023-i+0.5)*FVT_Pitch;
                FVT_stripsYloc[i][1] =  FVT_Rmax - (1023-i+0.5)*FVT_Pitch;
            }
            FVT_stripsYlocref[i] = FVT_stripsYloc[i][0];

            int localRegion = getLocalRegion(i);
            switch(localRegion) {
            case 2: case 4:
                FVT_stripslength[i] = 2*FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
                FVT_stripsXloc[i][0] = -FVT_stripslength[i]/2.;
                FVT_stripsXloc[i][1] =  FVT_stripslength[i]/2.;
                FVT_stripsXlocref[i] = 0;
                break;
            case 1:
                FVT_stripslength[i] = FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
                FVT_stripsXloc[i][1] = 0;
                FVT_stripsXloc[i][0] = -FVT_stripslength[i];
                FVT_stripsXlocref[i] = -FVT_stripslength[i]/2;
                if (Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole < 1) {
                    FVT_stripslength[i] = FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax))
                            -FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                    FVT_stripsXloc[i][1] = -FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                    FVT_stripsXloc[i][0] = -FVT_stripslength[i];
                    FVT_stripsXlocref[i] = -FVT_stripslength[i]/2-FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                }
                break;
            case 3:
                FVT_stripslength[i] = FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
                FVT_stripsXloc[i][0] = 0;
                FVT_stripsXloc[i][1] = FVT_stripslength[i];
                FVT_stripsXlocref[i] = FVT_stripslength[i]/2;
                if (Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole < 1) {
                    FVT_stripslength[i]= FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax))
                            -FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                    FVT_stripsXloc[i][0] = FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                    FVT_stripsXloc[i][1] = FVT_stripslength[i];
                    FVT_stripsXlocref[i] = FVT_stripslength[i]/2+FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
                }
                break;
            }
            for (int j = 0; j < FVT_Nlayers; j++) { // x sign flipgit s
                FVT_stripsX[j][i][0] = -(FVT_stripsXloc[i][0]*Math.cos(FVT_Alpha[j]) + FVT_stripsYloc[i][0]*Math.sin(FVT_Alpha[j]));
                FVT_stripsY[j][i][0] =  -FVT_stripsXloc[i][0]*Math.sin(FVT_Alpha[j]) + FVT_stripsYloc[i][0]*Math.cos(FVT_Alpha[j]);
                FVT_stripsX[j][i][1] = -(FVT_stripsXloc[i][1]*Math.cos(FVT_Alpha[j]) + FVT_stripsYloc[i][1]*Math.sin(FVT_Alpha[j]));
                FVT_stripsY[j][i][1] =  -FVT_stripsXloc[i][1]*Math.sin(FVT_Alpha[j]) + FVT_stripsYloc[i][1]*Math.cos(FVT_Alpha[j]);
            }
        }

        System.out.println("*****   FMT constants loaded!");
    }

    /**
     * Save loaded alignment table to constants.
     * @param shArr : Two-dimensional array storing alignment information. Rows define the 6 FMT
     *                layers, and columns are [deltaX, deltaY, deltaZ, rotX, rotY, rotZ].
     */
    public static void saveAlignmentTable(double[][] shArr) {
        FVT_xShift = new double[FVT_Nlayers];
        FVT_yShift = new double[FVT_Nlayers];
        FVT_zShift = new double[FVT_Nlayers];
        FVT_xRot   = new double[FVT_Nlayers];
        FVT_yRot   = new double[FVT_Nlayers];
        FVT_zRot   = new double[FVT_Nlayers];

        for (int li=0; li<FVT_Nlayers; ++li) {
            FVT_xShift[li] = shArr[li][0];
            FVT_yShift[li] = shArr[li][1];
            FVT_zShift[li] = shArr[li][2];
            FVT_xRot[li]   = shArr[li][3];
            FVT_yRot[li]   = shArr[li][4];
            FVT_zRot[li]   = shArr[li][5];
        }
    }

    /**
     * Apply z shifts and rotations to FMT Constants.
     */
    public static void applyZShifts() {
        for (int li = 0; li < FVT_Nlayers; ++li) {
            FVT_Zlayer[li] += FVT_zShift[li];
            FVT_Alpha[li]  += FVT_zRot[li];
        }
    }

    /**
     * Apply z shifts and rotations to FMT Constants.
     */
    public static void applyXYShifts() {
        // TODO: Only deltaX and deltaY are implemented. rotX and rotY pending!
        for (int li = 0; li < FVT_Nlayers; ++li) { // layers
            for (int si = 0; si < FVT_Nstrips; ++si) { // strips
                for (int ei = 0; ei < 2; ++ei) { // endpoints
                    FVT_stripsX[li][si][ei] += FVT_xShift[li];
                    FVT_stripsY[li][si][ei] += FVT_yShift[li];
                }
            }
        }
    }

    private static int getLocalRegion(int i) {
            // To represent the geometry we divide the barrel micromega disk into 4 regions according to the strip numbering system.
            // Here i = strip_number -1;
            // Region 1 is the region in the negative x part of inner region: the strips range is from   1 to 320  (   0 <= i < 320)
            // Region 2 is the region in the negative y part of outer region: the strips range is from 321 to 512  ( 320 <= i < 512)
            // Region 3 is the region in the positive x part of inner region: the strips range is from 513 to 832  ( 512 <= i < 832)
            // Region 4 is the region in the positive y part of outer region: the strips range is from 833 to 1024 ( 832 <= i < 1024)
            if (i>=  0 && i< 320) return 1;
            if (i>=320 && i< 512) return 2;
            if (i>=512 && i< 832) return 3;
            if (i>=832 && i<1024) return 4;
            return 0;
    }
    
    public static synchronized double[] getEFF_Z_OVER_A() {
        return EFF_Z_OVER_A;
    }
    public static synchronized void setEFF_Z_OVER_A(double[] eFF_Z_OVER_A) {
        EFF_Z_OVER_A = eFF_Z_OVER_A;
    }

    public static synchronized double[] get_X0() {
        return _X0;
    }
    public static synchronized void set_X0(double[] X0) {
       _X0 = X0;
    }

    public static synchronized double[] get_RELPOS() {
        return _REL_POS;
    }
    public static synchronized void set_RELPOS(double[] REL_POS) {
       _REL_POS = REL_POS;
    }
}
