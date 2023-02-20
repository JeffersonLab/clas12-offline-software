package org.jlab.rec.cnd.constants;

public class Parameters {

	Parameters(){

	}	

	public static final int lightspeed = 30;          // c (cm/ns)

	// SIMULATION FLAG
	public static boolean isSimulation = true;

	// GEOMETRY PARAMETERS
	public static final int SectNum = 24;													  // Number of sectors (blocks)
	public static final int LayNum = 3;                                                       // Number of layer
	public static final int CompNum = 2;                                                      // Number of components (paddles) in each layer, sector

	public static final double R0 = 28.92;   	// not used											  // radius of inner-most surface of CND paddle (cm)
	public static final double Thickness = 3.08;   	// not used										  // thickness of each paddle (cm)
	public static final double LayerGap = 1.0;	// IN MM										  // radial gap between surfaces of paddles in neighbouring layers (cm)
	public static final double BlockSlice = 360./SectNum;									  // azimuthal angle subtended by each sector

	public static final double[] PLength = new double[LayNum] ;                               // Paddle length in each layer (cm)

	//RECONSTRUCTION PARAMETERS

	public static final int[][][] pedestal = new int[SectNum][LayNum][CompNum];               // ADC pedestals, per ADC 
	public static final double[][][] ADCslope = new double[SectNum][LayNum][CompNum]; 	      // conversion factors from ADC channels to energy (MeV^-1) at the upstream end of paddle
	public static final double[][][] ADCoffset = new double[SectNum][LayNum][CompNum];        // ADC offset

	public static final int NullTDC = 4096;													  // Max number of channels in the TDC	
	public static final double[][][] TDCslope = new double[SectNum][LayNum][CompNum];         // conversion factors from TDC channels to time (ns).
	public static final double[][][] TDCoffset = new double[SectNum][LayNum][CompNum];        // TDC offset
	public static final double[][][] ToffsetSector = new double[SectNum][LayNum][CompNum];    // Global time offset
	public static final double[][][] ToffsetLR = new double[SectNum][LayNum][CompNum];        // Time offset between the two components in each sector/layer	

	//	public static final double[][][] Veff = new double[SectNum][LayNum][CompNum];             // Effective velocity in each paddle; 
	public static final double[][][] uturn_tloss = new double[SectNum][LayNum][CompNum];      // Time of propagation around the u-turn 

	public static final double[][][] AttL = new double[SectNum][LayNum][CompNum];              // Attenuation length 
	public static final double[][][] uturn_Eloss = new double[SectNum][LayNum][CompNum];       // Fractional energy loss in u-turn 	

	public static final double[] Zres = new double[LayNum] ;								  // expected resolution in z for each layer (cm)
	public static final double[] POffset = new double[LayNum] ;								  // offset of paddle wrt Central Detector center (cm)		

	public static final double TarZ = 0.;												      // position of the target wrt Central Detector centre

	public static final double Tres = 0.2;	                                                  // Average time resolution for hit
	public static final double[] MaxTime = new double[LayNum];							      // Max time of particle hit in paddle for a good event	
	public static final double[] MinTime = new double[LayNum];							      // Min time of particle hit in paddle for a good event	

	public static double EThresh = 0.1 ;                                                       // Min reconstructed deposited energy threshold for a good event

	public static double DeltaZDH = 2;                                                         // in cm, maximum absolute value of difference between hit z and double hit Z
	public static double DeltaTDH = 0.5;                                                         //in ns, maximum absolute value between direct hit time of left and right paddle, both should be equal for double hits

	public static void SetParameters() {

		for (int l=0; l<LayNum; l++) {

			PLength[l] = 66. ;
			POffset[l] = 0. ;
			Zres[l] = 1.5 ;
			MaxTime[l] = 250.;
			MinTime[l] = 0.;

			for (int s=0; s<SectNum; s++) {

				for (int c=0; c<CompNum; c++) {

					pedestal[s][l][c] = 0;
					ADCslope[s][l][c] = 50./1000.;
					ADCoffset[s][l][c] = 0.;
					TDCslope[s][l][c] = 25./1000.;
					TDCoffset[s][l][c] = 0.;
					ToffsetSector[s][l][c] = 0.;
					ToffsetLR[s][l][c] = 0.;
					//					Veff[s][l][c] = 16. ;
					uturn_tloss[s][l][c] = 0.5;
					AttL[s][l][c] = 1.5*1000;
					uturn_Eloss[s][l][c] = 0.5;

				}
			}
		}
	}
}


