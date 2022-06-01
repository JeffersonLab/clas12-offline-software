package org.jlab.rec.band.constants;

import java.util.HashMap;
import java.util.Map;
import java.lang.Integer;
import java.lang.Double;
/**
 * 
 * @author Efrain Segarra, Florian Hauenstein
 *
 */
public class Parameters {

	Parameters(){

	}	
	
	// Physics constants
	public static final double lightspeed = 29.9792458;

	// GEOMETRY PARAMETERS
	public static final int sectNum = 5;	// Number of sectors (blocks)
	public static final int layNum = 6; 	// Number of layer
	public static final int compNum = 7;    // Maximum Number of components in a sector
		// Number of components per layer and sector [layer][sector] index
	public static final int[][] compNumSecLay = { {3,7,6,6,2}, {3,7,6,6,2}, {3,7,6,6,2}, 
							{3,7,6,6,2}, {3,7,5,5,0}, {3,7,6,6,2} };

	public static final double distVetoLead = 17.463;			// distance from veto to lead wall (cm)
	
	public static final double thickness = 7.2;				// thickness of each bar (cm)
	public static final double[] layerGap = {7.94, 7.62, 7.94, 7.62, 7.3};  // gap between center of neighbouring layers (cm), 1-2, 2-3, 3-4, 4-5, 5-6
	public static final double zOffset = 100;                               // distance from center first layer to target.

	// Constants from survey box. They measured x,y,z of the end of the PMTs on the inside hole of BAND (where the 
	// beam pipe goes through). [0][:] = beam right bottom (x,y,z) ; [1][:] = beam right top (x,y,z) ;
	// [2][:] = beam left bottom (x,y,z) ; [3][:] = beam left top (x,y,z) -- all in cm
	// 	The box is defined by using the end of PMT 1-11AR and end of PMT 1-11BL to measure in x, 
	// 		and the bottom of bar 1-10 and top of bar 1-17 to measure in y.
	// 		In z, the position is distance from target to middle of PMTs on 1-11AR, 1-11BL, 1-16AR, 1-16BL.
	public static final double[][] surveyBox = {  {-24.05,-21.10,-302.69},
						      {-24.05, 22.81,-302.69},
						      { 24.10,-21.06,-302.57},
						      { 24.37, 22.81,-302.64}  };
	public static final double lenLG = 8.9; // [cm] -- length of LG
	public static final double lenET = 16.; // [cm] -- length of PMT tube for ET PMTs
	public static final double lenHam = 13.3; // [cm] -- length of PMT tube for Hamamatsu PMTs

	public static double avgX = ( (surveyBox[0][0] + surveyBox[2][0]) + (surveyBox[1][0] + surveyBox[3][0]) )/2.;
	public static double avgY = ( (surveyBox[0][1] + thickness*3.) + (surveyBox[1][1] - thickness*3.) 
					+ (surveyBox[2][1] + thickness*3.) + (surveyBox[3][1] - thickness*3.) )/4.;
	public static double avgZ = ( surveyBox[0][2] + surveyBox[1][2] + surveyBox[2][2] + surveyBox[3][2] )/4.;

	public static final double[] globPt = {avgX,avgY,avgZ}; // single global position

	public static final double barRes = 0.3; // needs to be read in actually from CCDB but temp holder at the moment
	
	//Maybe add here more geometry constants for x and y or read from ccdb
	public static final double[] barLengthSector = {163.7, 201.9, 51.2, 51.2, 201.9} ;           // Bar length in each layer (cm)

	public static Map<Integer, Double[]> barGeo = new HashMap<Integer, Double[]>();
	public static synchronized void CreateGeometry() {
		for( int layer = 1 ; layer < layNum + 1 ; layer++){
			double localZ = 0.;
			localZ += (layerGap[1])/2.; // taking this thickness because wrapping material
						    // isn't 'squeezed' by the weight of the detector
			for( int i = 1 ; i < layer ; i++ ){
				localZ += layerGap[i-1];
			}

			for( int sector = 1 ; sector < sectNum + 1 ; sector++){
				int nBars = compNumSecLay[layer-1][sector-1];
				for( int bar = 1 ; bar < nBars + 1 ; bar++){
					int key = sector*100+layer*10+bar;
					double localY = 666666.;				
					double localX = 666666.;
					
					double secYOff = 666666.;
					
					if( sector == 1){
						secYOff = 10.;
						localX = 0.;
					}
					else if( sector == 2){
						secYOff = 3.;
						localX = 0.;
					}
					else if( sector == 3 || sector == 4){
						secYOff = -3.;
						if( sector == 3){
							localX = (surveyBox[2][0]+surveyBox[3][0])/2. + lenLG + lenET + barLengthSector[sector-1]/2.;
						}
						else if( sector == 4){
							localX = (surveyBox[0][0]+surveyBox[1][0])/2. - lenLG - lenET - barLengthSector[sector-1]/2.;
						}
					}
					else if( sector == 5){
						secYOff = -5.;
						localX = 0.;
					}
					localY = secYOff*thickness + (nBars - (bar-1) )*thickness - thickness/2.;


					//System.out.println("Bar ID: "+key+" x,y,z: "+localX+" "+localY+" "+localZ);
					barGeo.put( Integer.valueOf(key), new Double[] { localX + globPt[0],
										  	 localY + globPt[1],
											 localZ + globPt[2]  } );
					//System.out.println("Bar ID: "+key+" x,y,z: "+(localX+globPt[0])+" "+(localY+globPt[1])+" "+(localZ+globPt[2]));


				}
				

			}
	

		}
		

		return;
	}
}


