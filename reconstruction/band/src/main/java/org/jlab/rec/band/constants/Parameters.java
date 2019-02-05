package org.jlab.rec.band.constants;


/**
 * 
 * @author hauenstein
 *
 */
public class Parameters {

	Parameters(){

	}	

	public static final int lightspeed = 30;          // c (cm/ns)

	// SIMULATION FLAG
	public static boolean isSimulation = true;

	// GEOMETRY PARAMETERS
	public static final int sectNum = 5;									// Number of sectors (blocks)
	public static final int layNum = 6;                                     // Number of layer
	public static final int compNum = 7;             // Maximum Number of components in a sector
	public static final int[][] compNumSecLay = {{3,7,6,6,2},{3,7,6,6,2},{3,7,6,6,2},{3,7,6,6,2},{3,7,5,5,0},{3,7,6,6,2}};	// Number of components per layer and sector [layer][sector] index										
	
	public static final double thickness = 7.2;   							 // thickness of each bar (cm)
	public static final double[] layerGap = {7.94, 7.62, 7.94, 7.62, 7.3};   	 // gap between center of neighbouring layers (cm), 1-2, 2-3, 3-4, 4-5, 5-6
    public static final double distVetoLead = 17.46;											//distance veto to lead wall (cm)
	public static final double zOffset = 100;                                // distance from center first layer to target.
	//Maybe add here more geometry constants for x and y or read from ccdb
    public static final double[] barLengthSector = {164, 202, 51, 51, 202} ;           // Bar length in each layer (cm)

	
	//RECONSTRUCTION PARAMETERS

	public static final double[][][] pedestal = new double[sectNum][layNum][compNum];         // ADC pedestals, per ADC 
	public static final double[][][] adcConv = new double[sectNum][layNum][compNum]; 	      // conversion factors from ADC channels to MeVee
   
	public static final double[][][] tdcConv = new double[sectNum][layNum][compNum];         // conversion factors from TDC channels to time (ns).
	public static final double tdcOffset = 0;       										 // Global TDC offset
	public static final double[] tdcOffsetLayer = new double[layNum];                    // TDC offset per layer
	public static final double[][][] tdcOffsetLR = new double[sectNum][layNum][compNum];        // Time offset between the two components in each sector/layer	

	
	public static final double[][][] attL = new double[sectNum][layNum][compNum];              // Attenuation length 
	
	public static final double tRes = 0.2;	                                                  // Average time resolution for hit (ns)
	public static final double[] maxTime = new double[layNum];							      // Max time of particle hit in a bar for a good event	
	public static final double[] minTime = new double[layNum];							      // Min time of particle hit in a bar for a good event	

	public static double eThresh = 0.1 ;                                                     // Min reconstructed deposited energy threshold for a good event in MeVee


	public static void SetParameters() {

		for (int l=0; l<layNum; l++) {

		
			maxTime[l] = 250.;
			minTime[l] = 0.;
			tdcOffsetLayer[l] = 0.;

			for (int s=0; s<sectNum; s++) {

				for (int c=0; c<compNum; c++) {

					pedestal[s][l][c] = 0;
					adcConv[s][l][c] = 1./2000.; //assuming 1MeVee at channel 2000
					tdcConv[s][l][c] = 25./1000; 
					tdcOffsetLR[s][l][c] = 0.;
					attL[s][l][c] = 0.;
					

				}
			}
		}
	}
}


