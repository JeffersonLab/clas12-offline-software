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
	public static double eThresh = 0.1 ;                                                     // Min reconstructed deposited energy threshold for a good event in MeVee

}


