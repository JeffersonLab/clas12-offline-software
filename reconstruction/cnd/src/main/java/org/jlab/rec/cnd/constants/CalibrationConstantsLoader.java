package org.jlab.rec.cnd.constants;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;



/**
 * 
 * @author ziegler
 *
 */
public class CalibrationConstantsLoader {

        private final static String[]  cndTables = new String[]{
                "/calibration/cnd/UturnEloss",
                "/calibration/cnd/UturnTloss",
                "/calibration/cnd/TimeOffsets_LR",
                "/calibration/cnd/TDC_conv",
                "/calibration/cnd/TimeOffsets_layer",
                "/calibration/cnd/EffV",
                "/calibration/cnd/Attenuation",
                "/calibration/cnd/Status_LR",
                "/calibration/cnd/Energy",
                "/calibration/cnd/time_jitter",
		"/geometry/cnd/cndgeom",
		"/geometry/target", "/calibration/cnd/cluster",
        "/calibration/cnd/double_hits"
            };

        public CalibrationConstantsLoader(int run, ConstantsManager ccdb) {
            this.Load(run, ccdb);
        }

        // Instantiating the constants arrays
	public double[][] UTURNELOSS 		= new double[24][3];
	public double[][] UTURNTLOSS 		= new double[24][3];
	public double[][] TIMEOFFSETSLR 		= new double[24][3];	
	public double[][][] TDCTOTIMESLOPE	= new double[24][3][2];
	public double[][][] TDCTOTIMEOFFSET	= new double[24][3][2];	
	public double[][] TIMEOFFSETSECT 	= new double[24][3];
	public double[][][] EFFVEL 		= new double[24][3][2];
	public double[][][] ATNLEN               = new double[24][3][2];
	public double[][][] MIPDIRECT 		= new double[24][3][2];
	public double[][][] MIPINDIRECT		= new double[24][3][2];
	public int[][][] Status_LR 		= new int[24][3][2];
	public double JITTER_PERIOD              = 0;
	public int JITTER_PHASE                  = 0;
	public int JITTER_CYCLES                 = 0;
	public double[] LENGTH                   = new double[3];
	public double[] ZOFFSET                  = new double[3];
	public double[] THICKNESS                = new double[1];
	public double[] INNERRADIUS              = new double[1];
	public double[] ZTARGET                  = new double[1];

    public double DX = 0;
    public double DY = 0;
    public double DZ = 0;
    public double DT = 0;

    public double[] DeltaZDH                 = new double[3];    // in cm, maximum absolute value of difference between hit z and double hit Z
    public double[] DeltaTDH                 = new double[3];    //in ns, maximum absolute value between direct hit time of left and right paddle, both should be equal for double hits

	//Calibration and geometry parameters from DB    

        public static String[] getCndTables() {
            return cndTables;
        }

        
	public final void Load(int run, ConstantsManager ccdb) {

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
            List<IndexedTable> tabJs = new ArrayList<IndexedTable>();
            for(String tbStg : cndTables) {
                tabJs.add(ccdb.getConstants(run, tbStg));
            }

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

                DeltaZDH[iLay-1] = tabJs.get(13).getDoubleValue("deltaZ", 0, iLay, 0);
                DeltaTDH[iLay-1] = tabJs.get(13).getDoubleValue("deltaT", 0, iLay, 0);

            }

            //LENGTH = new double[]{665.72, 700.0, 734.28};
            INNERRADIUS[0] = 10.*tabJs.get(10).getDoubleValue("InnerRadius", 1, 1, 1);            
            THICKNESS[0] = 10.*tabJs.get(10).getDoubleValue("Thickness", 1, 1, 1);  // not right structure for common tools
	    
	    ZTARGET[0] = tabJs.get(11).getDoubleValue("position", 0, 0, 0);  // not right structure for common tools
            //INNERRADIUS[0] = 290.0;            
            //THICKNESS[0] = 30.0;  

//            System.out.println("Target Position "+ZTARGET[0]);
//            System.out.println("Radius and thickness "+INNERRADIUS[0]+" "+THICKNESS[0]);
//            for(int iLay = 1; iLay <=3; iLay++) {
//              System.out.println("Length and Zoff "+LENGTH[iLay-1]+" "+ZOFFSET[iLay-1]);
//            }
             DX = tabJs.get(12).getDoubleValue("deltax", 0, 0, 0);
             DY = tabJs.get(12).getDoubleValue("deltay", 0, 0, 0);
             DZ = tabJs.get(12).getDoubleValue("deltaz", 0, 0, 0);
             DT = tabJs.get(12).getDoubleValue("deltat", 0, 0, 0);

    } 


}
