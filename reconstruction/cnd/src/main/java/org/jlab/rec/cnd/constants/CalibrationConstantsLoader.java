package org.jlab.rec.cnd.constants;

import java.util.List;
import org.jlab.utils.groups.IndexedTable;



/**
 * 
 * @author ziegler
 *
 */
public class CalibrationConstantsLoader {

	public CalibrationConstantsLoader() {
		// TODO Auto-generated constructor stub
	}
	public static boolean CSTLOADED = false;

	// Instantiating the constants arrays
	public static double[][] UTURNELOSS 		= new double[24][3];
	public static double[][] UTURNTLOSS 		= new double[24][3];
	public static double[][] TIMEOFFSETSLR 		= new double[24][3];	
	public static double[][][] TDCTOTIMESLOPE	= new double[24][3][2];
	public static double[][][] TDCTOTIMEOFFSET	= new double[24][3][2];	
	public static double[][] TIMEOFFSETSECT 	= new double[24][3];
	public static double[][][] EFFVEL 		= new double[24][3][2];
	public static double[][][] ATNLEN               = new double[24][3][2];
	public static double[][][] MIPDIRECT 		= new double[24][3][2];
	public static double[][][] MIPINDIRECT		= new double[24][3][2];
	public static int[][][] Status_LR 		= new int[24][3][2];
	public static double JITTER_PERIOD              = 0;
	public static int JITTER_PHASE                  = 0;
	public static int JITTER_CYCLES                 = 0;
	public static double[] LENGTH                   = new double[3];
	public static double[] ZOFFSET                  = new double[3];
	public static double[] THICKNESS                = new double[1];
	public static double[] INNERRADIUS              = new double[1];
	public static double[] ZTARGET                  = new double[1];
	//Calibration and geometry parameters from DB    

	public static boolean arEnergyibConstantsLoaded = false;
	public static synchronized void Load(List<IndexedTable> tabJs) {
            if(CSTLOADED)
                return;
            System.out.println(" LOADING CONSTANTS ");

            // load table reads entire table and makes an array of variables for each column in the table.
            //tabJs(0): ("/calibration/cnd/UturnEloss");
            //tabJs(1): ("/calibration/cnd/UturnTloss");
            //tabJs(2): ("/calibration/cnd/TimeOffsets_LR");
            //tabJs(3): ("/calibration/cnd/TDC_conv");
            //tabJs(4): ("/calibration/cnd/TimeOffsets_layer");
            //tabJs(5): ("/calibration/cnd/EffV");
            //tabJs(6): ("/calibration/cnd/Attenuation");
            //tabJs(7): ("/calibration/cnd/Status_LR");
            //tabJs(8): ("/calibration/cnd/Energy");
            //tabJs(9): ("/calibration/cnd/time_jitter");
            //tabJs(10): ("/geometry/cnd/layer");
            //tabJs(11): ("/geometry/cnd/cnd");

            for (int iSec = 1; iSec <=24; iSec++) {
                for(int iLay = 1; iLay <=3; iLay++) {
                    UTURNELOSS[iSec-1][iLay-1] = tabJs.get(0).getDoubleValue("uturn_eloss", iSec, iLay, 1); // component is 1
                    UTURNTLOSS[iSec-1][iLay-1] = tabJs.get(1).getDoubleValue("uturn_tloss", iSec, iLay, 1);
                    TIMEOFFSETSLR[iSec-1][iLay-1] = tabJs.get(2).getDoubleValue("time_offset_LR", iSec, iLay, 1);
                    TDCTOTIMESLOPE[iSec-1][iLay-1][0]  = tabJs.get(3).getDoubleValue("slope_L", iSec, iLay, 1);
                    TDCTOTIMEOFFSET[iSec-1][iLay-1][0] = tabJs.get(3).getDoubleValue("offset_L", iSec, iLay, 1);
                    TDCTOTIMESLOPE[iSec-1][iLay-1][1]  = tabJs.get(3).getDoubleValue("slope_R", iSec, iLay, 1);
                    TDCTOTIMEOFFSET[iSec-1][iLay-1][1] = tabJs.get(3).getDoubleValue("offset_R", iSec, iLay, 1);
                    TIMEOFFSETSECT[iSec-1][iLay-1] = tabJs.get(4).getDoubleValue("time_offset_layer", iSec, iLay, 1);
                    EFFVEL[iSec-1][iLay-1][0] = tabJs.get(5).getDoubleValue("veff_L", iSec, iLay, 1);
                    EFFVEL[iSec-1][iLay-1][1] = tabJs.get(5).getDoubleValue("veff_R", iSec, iLay, 1);
                    ATNLEN[iSec-1][iLay-1][0] = tabJs.get(6).getDoubleValue("attlen_L", iSec, iLay, 1);
                    ATNLEN[iSec-1][iLay-1][1] = tabJs.get(6).getDoubleValue("attlen_R", iSec, iLay, 1);
                    Status_LR[iSec-1][iLay-1][0] = tabJs.get(7).getIntValue("status_L", iSec, iLay, 1);
                    Status_LR[iSec-1][iLay-1][1] = tabJs.get(7).getIntValue("status_R", iSec, iLay, 1);
                    MIPDIRECT[iSec-1][iLay-1][0]    = tabJs.get(8).getDoubleValue("mip_dir_L", iSec, iLay, 1);
                    MIPINDIRECT[iSec-1][iLay-1][0]  = tabJs.get(8).getDoubleValue("mip_indir_L", iSec, iLay, 1);
                    MIPDIRECT[iSec-1][iLay-1][1]    = tabJs.get(8).getDoubleValue("mip_dir_R", iSec, iLay, 1);
                    MIPINDIRECT[iSec-1][iLay-1][1]  = tabJs.get(8).getDoubleValue("mip_indir_R", iSec, iLay, 1);
                }
            }
            JITTER_PERIOD = tabJs.get(9).getDoubleValue("period", 0, 0, 0);
            JITTER_PHASE  = tabJs.get(9).getIntValue("phase",  0, 0, 0);
            JITTER_CYCLES = tabJs.get(9).getIntValue("cycles", 0, 0, 0);

            for(int iLay = 1; iLay <=3; iLay++) {
                LENGTH[iLay-1]  = 10.*tabJs.get(10).getDoubleValue("Length", 1, iLay, 1);
                ZOFFSET[iLay-1] = 10.*tabJs.get(10).getDoubleValue("UpstreamZOffset", 1, iLay, 1);// not right structure for common tools
            }

            //LENGTH = new double[]{665.72, 700.0, 734.28};
            INNERRADIUS[0] = 10.*tabJs.get(10).getDoubleValue("InnerRadius", 1, 1, 1);            
            THICKNESS[0] = 10.*tabJs.get(10).getDoubleValue("Thickness", 1, 1, 1);  // not right structure for common tools
	    
	    ZTARGET[0] = tabJs.get(11).getDoubleValue("position", 0, 0, 0);  // not right structure for common tools
            //INNERRADIUS[0] = 290.0;            
            //THICKNESS[0] = 30.0;  

	System.out.println("Target Position "+ZTARGET[0]);
	System.out.println("Radius and thickness "+INNERRADIUS[0]+" "+THICKNESS[0]);
	 for(int iLay = 1; iLay <=3; iLay++) {
	System.out.println("Length and Zoff "+LENGTH[iLay-1]+" "+ZOFFSET[iLay-1]);
            }

            CSTLOADED = true;
            System.out.println("SUCCESSFULLY LOADED CND CALIBRATION CONSTANTS....");

	} 


}
