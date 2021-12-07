package org.jlab.detector.geant4.v2.SVT;

import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Triangle3d;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * <h1> Geometry Alignment </h1>
 * 
 * Universal class for processing and applying alignment shifts to points and volumes.
 * 
 * @author pdavies
 * @version 1.0.11
 */
public class AlignmentFactory
{
	public static int NSHIFTDATARECLEN = 7;
	public static boolean VERBOSE = false;
	
	
	/*public static void adjustData( double[][] dataMeasured, double uncertainty, double[][] dataNominalDistances )
	{
		// Adjusts data to fit nominal-sized triangle within experimental uncertainties.
		
		for( int k = 0; k < dataMeasured.length/3; k+=3 ) // triangle
		{
			Vector3d[] measuredPos3Ds = new Vector3d[3];
			double[] measuredDistances = new double[3];
			double[] distanceDeltas = new double[3];
			Vector3d[] sides = new Vector3d[3];
			double[] pointDeltas = new double[2*3];
			Vector3d[] adjustments = new Vector3d[3];
			Vector3d[] adjustedPos3Ds = new Vector3d[3];
			
			double[] p = new double[]{ 0.5, 0.5, 0.5 };
			
			//System.out.printf("\n%3s", "");
			
			for( int j = 0; j < 3+2; j++ ) // point, with offset x2
			{
				if( j < 3 ){ measuredPos3Ds[j] =  new Vector3d(dataMeasured[k+j][0], dataMeasured[k+j][1], dataMeasured[k+j][2]); }
				if( j > 0 && j < 4 ) // offset to wait for first point to be defined
				{
					//System.out.printf("%4s(%d %d)", "", j-1, j%3 );
					sides[j-1] = new Vector3d( measuredPos3Ds[j-1].vectorTo( measuredPos3Ds[j%3] ) );
					measuredDistances[j-1] = sides[j-1].mag();
					distanceDeltas[j-1] = measuredDistances[j-1] - dataNominalDistances[k][j-1];
				}
				if( j > 1 ) // offset to wait for both distanceDeltas of each vertex to be defined
				{
					//System.out.printf("\n (%d %d)(%d %d)(% 8.3f % 8.3f)", j-2, (j-1)%3, (j-2)*2+0, (j-2)*2+1, distanceDeltas[j-2], distanceDeltas[(j-1)%3] );
					System.out.printf("\n jj(%d %d)", j-2, (j-1)%3 );
					
					pointDeltas[(j-2)*2+0] = p[j-2]*distanceDeltas[j-2];
					pointDeltas[(j-2)*2+1] = (1 - p[j-2])*distanceDeltas[(j-1)%3];
					System.out.printf(" PD(% 8.3f % 8.3f)", pointDeltas[(j-2)*2+0], pointDeltas[(j-2)*2+1] );
					
					Vector3d unitSideA = sides[j-2].normalized();
					Vector3d unitSideB = sides[(j-1)%3].normalized();
					unitSideA.scale( pointDeltas[(j-2)*2+0] );
					unitSideB.scale( pointDeltas[(j-2)*2+1] );
					//System.out.printf("(% 8.3f % 8.3f)", unitSideA.mag(), unitSideB.mag() );
					
					adjustments[j-2] = new Vector3d( unitSideA.add( unitSideB ) );
					adjustments[j-2].scale( 0.5 );
					System.out.printf("(% 8.3f)", adjustments[j-2].mag() );
					
					adjustedPos3Ds[j-2] = new Vector3d( measuredPos3Ds[j-2], adjustments[j-2] );
					
					dataMeasured[k+(j-2)][0] = adjustedPos3Ds[j-2].x;
					dataMeasured[k+(j-2)][1] = adjustedPos3Ds[j-2].y;
					dataMeasured[k+(j-2)][2] = adjustedPos3Ds[j-2].z;
				}
			}
			System.out.println();
			System.out.printf("\nMD%d % 8.3f % 8.3f % 8.3f", k, measuredDistances[0], measuredDistances[1], measuredDistances[2] );
			System.out.printf("    ND  % 8.3f % 8.3f % 8.3f", dataNominalDistances[k][0], dataNominalDistances[k][1], dataNominalDistances[k][2] );
			System.out.printf("\nDD  % 8.3f % 8.3f % 8.3f", distanceDeltas[0], distanceDeltas[1], distanceDeltas[2] );
			//System.out.printf("\nPD  % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f % 8.3f", pointDeltas[0], pointDeltas[1], pointDeltas[2], pointDeltas[3], pointDeltas[4], pointDeltas[5] );
			System.out.println();
		}
	}*/
	
	
	
	/*public static double[][] calcShifts( int dataLen, double[][] dataNominal, double[][] dataMeasured, double uncertainty )
	{
		if( dataNominal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen < 1 ){ throw new IllegalArgumentException("no info"); }
		
		
		
		return null;
	}*/
	
	
	/**
	 * Calculates alignment shifts between two sets of fiducial data.
	 * 
	 * @param dataLen total number of data points (where each point is 3 Cartesian coordinates)
	 * @param dataIdeal first data set
	 * @param dataMeasured second data set
	 * @return double[][] alignment shifts relative to the first data set
	 */
	public static double[][] calcShifts( int dataLen, double[][] dataIdeal, double[][] dataMeasured )
	{
		if( dataIdeal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen < 1 ){ throw new IllegalArgumentException("no info"); }
		
		double[][] dataShifts = new double[dataLen][];
		
		for( int k = 0; k < dataLen; k++ )
		{
			Vector3d[] fidIdealPos3Ds = new Vector3d[3]; // 3 points in a triangle
			Vector3d[] fidMeasuredPos3Ds = new Vector3d[3]; // 3 points in a triangle
			Vector3d[] fidDeltas = new Vector3d[3];

			for( int f = 0; f < 3; f++ ) // 3 points in a triangle
			{
				fidIdealPos3Ds[f] = new Vector3d( dataIdeal[k*3+f][0], dataIdeal[k*3+f][1], dataIdeal[k*3+f][2] );
				fidMeasuredPos3Ds[f] = new Vector3d( dataMeasured[k*3+f][0], dataMeasured[k*3+f][1], dataMeasured[k*3+f][2] );
				fidDeltas[f] = fidMeasuredPos3Ds[f].minus( fidIdealPos3Ds[f] );
			}

			Triangle3d fidIdealTri3D = new Triangle3d( fidIdealPos3Ds[0], fidIdealPos3Ds[1], fidIdealPos3Ds[2] );
			Triangle3d fidMeasuredTri3D = new Triangle3d( fidMeasuredPos3Ds[0], fidMeasuredPos3Ds[1], fidMeasuredPos3Ds[2] );

			// find shift for position
			Vector3d fidIdealCenPos3D = fidIdealTri3D.center(); // average of 3 points
			Vector3d fidMeasuredCenPos3D = fidMeasuredTri3D.center();
			Vector3d fidDiffVec3D = fidMeasuredCenPos3D.minus( fidIdealCenPos3D );

			// find shift for rotation, about nominal triangle center
			Vector3d fidIdealVec3D = fidIdealTri3D.normal.normalized();
			Vector3d fidMeasuredVec3D = fidMeasuredTri3D.normal.normalized();
			double[] axisAngle = Util.convertVectorDiffToAxisAngle( fidIdealVec3D, fidMeasuredVec3D );
			boolean bUsedNormal = true;
			
			if( VERBOSE )
			{
				System.out.printf("\ncalculating shift for set %d", k );
				System.out.println();
				for( int f = 0; f < 3; f++ )
				{
					System.out.printf("IP%d % 8.3f % 8.3f % 8.3f", f, fidIdealPos3Ds[f].x, fidIdealPos3Ds[f].y, fidIdealPos3Ds[f].z );
					System.out.printf("    MP%d % 8.3f % 8.3f % 8.3f", f, fidMeasuredPos3Ds[f].x, fidMeasuredPos3Ds[f].y, fidMeasuredPos3Ds[f].z );
					System.out.printf("    D%d % 8.3f % 8.3f % 8.3f", f, fidDeltas[f].x, fidDeltas[f].y, fidDeltas[f].z );
					System.out.println();
				}
				System.out.printf("IC  % 8.3f % 8.3f % 8.3f", fidIdealCenPos3D.x, fidIdealCenPos3D.y, fidIdealCenPos3D.z );
				System.out.printf("    MC  % 8.3f % 8.3f % 8.3f", fidMeasuredCenPos3D.x, fidMeasuredCenPos3D.y, fidMeasuredCenPos3D.z );
				//System.out.printf("    DC  % 8.3f % 8.3f % 8.3f", fidDeltaCen.x, fidDeltaCen.y, fidDeltaCen.z );
				System.out.println();
				System.out.printf("IV  % 8.3f % 8.3f % 8.3f", fidIdealVec3D.x, fidIdealVec3D.y, fidIdealVec3D.z );
				System.out.printf("    MV  % 8.3f % 8.3f % 8.3f", fidMeasuredVec3D.x, fidMeasuredVec3D.y, fidMeasuredVec3D.z );
				System.out.println();
				System.out.printf("ST  % 8.3f % 8.3f % 8.3f", fidDiffVec3D.x, fidDiffVec3D.y, fidDiffVec3D.z );
				System.out.printf("    SR  % 8.3f % 8.3f % 8.3f % 8.3f", axisAngle[0], axisAngle[1], axisAngle[2], Math.toDegrees(axisAngle[3]) );
				System.out.println();
			}
			
			// if the normals are parallel, use vectors in the plane instead?
			if( axisAngle[3] < 1E-3 )
			{
				fidIdealVec3D =       fidIdealTri3D.point(0).midpoint(    fidIdealTri3D.point(1)  ).minus(   fidIdealTri3D.point(2) ).normalized();
				fidMeasuredVec3D = fidMeasuredTri3D.point(0).midpoint( fidMeasuredTri3D.point(1) ).minus( fidMeasuredTri3D.point(2) ).normalized();
				axisAngle = Util.convertVectorDiffToAxisAngle( fidIdealVec3D, fidMeasuredVec3D );
				bUsedNormal = false;
			}
			
			//Triangle3d fidDeltaTri3D = new Triangle3d( fidDeltas[0], fidDeltas[1], fidDeltas[2] );
			//Vector3d fidDeltaCen = fidDeltaTri3D.center();
			
			if( VERBOSE && !bUsedNormal )
			{
				System.out.printf("IV  % 8.3f % 8.3f % 8.3f", fidIdealVec3D.x, fidIdealVec3D.y, fidIdealVec3D.z );
				System.out.printf("    MV  % 8.3f % 8.3f % 8.3f", fidMeasuredVec3D.x, fidMeasuredVec3D.y, fidMeasuredVec3D.z );
				System.out.printf("    not using normal");
				System.out.println();
				System.out.printf("ST  % 8.3f % 8.3f % 8.3f", fidDiffVec3D.x, fidDiffVec3D.y, fidDiffVec3D.z );
				System.out.printf("    SR  % 8.3f % 8.3f % 8.3f % 8.3f", axisAngle[0], axisAngle[1], axisAngle[2], Math.toDegrees(axisAngle[3]) );
				System.out.println();
			}
			
			dataShifts[k] = new double[]{ fidDiffVec3D.x, fidDiffVec3D.y, fidDiffVec3D.z, axisAngle[0], axisAngle[1], axisAngle[2], axisAngle[3] };
		}
		return dataShifts;
	}
	
	
	/**
	 * Calculates the difference in coordinates between two sets of fiducial data.
	 * 
	 * @param dataLen number of data points
	 * @param dataWid number of elements in each data point
	 * @param dataIdeal first data set
	 * @param dataMeasured second data set
	 * @return dataDeltas point difference relative to the first data set 
	 */
	public static double[][] calcDeltas( int dataLen, int dataWid, double[][] dataIdeal, double[][] dataMeasured )
	{
		if( dataIdeal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen == 0 || dataWid == 0 ){ throw new IllegalArgumentException("no info"); }
		
		double[][] dataDeltas = new double[dataLen][dataWid];
		
		for( int j = 0; j < dataLen; j++ )
		{
			if( VERBOSE ) System.out.printf("calculating deltas for point %d\n", j );
			for( int i = 0; i < dataWid; i++ )
			{
				dataDeltas[j][i] = dataMeasured[j][i] - dataIdeal[j][i];
				if( VERBOSE ) System.out.printf("% 8.3f - % 8.3f = % 8.3f\n", dataMeasured[j][i], dataIdeal[j][i], dataDeltas[j][i] );
			}
		}
		
		return dataDeltas;
	}
	
	
	/**
	 * Calculates the difference in coordinates between two sets of fiducial data.
	 * 
	 * @param dataLen number of data points
	 * @param dataWid number of elements in each data point
	 * @param dataThk number of components in each element
	 * @param dataIdeal first data set
	 * @param dataMeasured second data set
	 * @return dataDeltas point difference relative to the first data set, with uncertainty
	 */
	public static double[][][] calcDeltas( int dataLen, int dataWid, int dataThk, double[][][] dataIdeal, double[][][] dataMeasured )
	{
		if( dataIdeal == null ){ throw new IllegalArgumentException("no data"); }
		if( dataMeasured == null ){ throw new IllegalArgumentException("no data"); }
		if( dataLen == 0 || dataWid == 0 ){ throw new IllegalArgumentException("no info"); }
		
		double[][][] dataDeltas = new double[dataLen][dataWid][dataThk];
		
		for( int j = 0; j < dataLen; j++ )
		{
			if( VERBOSE ) System.out.printf("calculating deltas for point %d\n", j );
			for( int i = 0; i < dataWid; i++ )
			{
				dataDeltas[j][i][0] = dataMeasured[j][i][0] - dataIdeal[j][i][0]; // calculate difference
				dataDeltas[j][i][1] = dataMeasured[j][i][1] - dataIdeal[j][i][1]; // sum uncertainties
				if( VERBOSE ) System.out.printf("% 8.3f:% 8.3f - % 8.3f:% 8.3f = % 8.3f:% 8.3f\n",
												dataMeasured[j][i][0], dataMeasured[j][i][1],
												dataIdeal[j][i][0], dataIdeal[j][i][1],
												dataDeltas[j][i][0], dataDeltas[j][i][1] );
			}
		}
		
		return dataDeltas;
	}
	
	
	/**
	 * Applies alignment shifts in bulk.
	 * 
	 * @param aData points in the lab frame
	 * @param aShift translations and axis-angle rotations of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aCenterData a point about which to rotate the first point (for example the midpoint of the ideal fiducials)
	 * @param aScaleT a scale factor for the translation shifts
	 * @param aScaleR a scale factor for the rotation shifts
	 * @return double[][] shifted points
	 */
	public static double[][] applyShift( double[][] aData, double[][] aShift, double[][] aCenterData, double aScaleT, double aScaleR )
	{
		double[][] aShiftedData = aData.clone();
		for( int j = 0; j < aData.length/3; j+=3 )
			for( int i = 0; i < 3; i++ )
			{
				Vector3d pos = new Vector3d( aData[j*3+i][0], aData[j*3+i][1], aData[j*3+i][2] );
				if( VERBOSE ) System.out.printf("applying shift to point %d\n", i );
				applyShift( pos, aShift[j], new Vector3d( aCenterData[j][0], aCenterData[j][1], aCenterData[j][2] ), aScaleT, aScaleR );
				aShiftedData[j*3+i][0] = pos.x;
				aShiftedData[j*3+i][1] = pos.y;
				aShiftedData[j*3+i][2] = pos.z;
			}
		return aShiftedData;
	}
	
	
	/**
	 * Applies the given alignment shift to the given point.
	 * 
	 * @param aPoint a point in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aCenter a point about which to rotate the first point (for example the midpoint of the ideal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	public static void applyShift( Vector3d aPoint, double[] aShift, Vector3d aCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
                if(aShift==null){ throw new IllegalArgumentException("shift array is null "+
                        Math.sqrt(aPoint.x*aPoint.x+aPoint.y*aPoint.y)); }
			
		if( aShift.length != NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+NSHIFTDATARECLEN+" elements");}
			
		// double[] tarray = SVTConstants.getDataAlignmentSectorShift()[0];
		// System.out.printf("Called applyShift. Shift[0][0]  = %8.4f ******************************************\n",tarray[0]);
		// System.out.printf("Called applyShift. Shift[0][1]  = %8.4f ******************************************\n",tarray[1]);
		// tarray = SVTConstants.getDataAlignmentSectorShift()[10];
		// System.out.printf("Called applyShift. Shift[0][10] = %8.4f ******************************************\n",tarray[0]);
		// tarray = SVTConstants.getDataAlignmentSectorShift()[65];
		// System.out.printf("Called applyShift. Shift[0][65] = %8.4f ******************************************\n",tarray[0]);
	
		double tx = aShift[0]; // The Java language has references but you cannot dereference the memory addresses like you can in C++.
		double ty = aShift[1]; // The Java runtime does have pointers, but they're not accessible to the programmer. (no pointer arithmetic)
		double tz = aShift[2];
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		tx *= aScaleT;
		ty *= aScaleT;
		tz *= aScaleT;
		ra *= aScaleR;
		
		if( VERBOSE )
		{
			System.out.printf("PN: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
			System.out.printf("ST: % 8.3f % 8.3f % 8.3f\n", tx, ty, tz );
			System.out.printf("SR: % 8.3f % 8.3f % 8.3f % 8.3f\n", rx, ry, rz, Math.toDegrees(ra) );
			System.out.printf("SC: % 8.3f % 8.3f % 8.3f\n", aCenter.x, aCenter.y, aCenter.z );
		}
				
		// do the rotation here.
		aCenter.times( -1 ); // reverse translation
                aPoint.set( aPoint.add( aCenter ) ); // move origin to center of rotation axis
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
                // if ra!=0, assume rx,ry,rz defines the rotation axis and ra the angle
                // if angle is non-zero but it is too small, don't do anything
		if(Math.abs(ra) >1E-5)
		{			
		    Vector3d vecAxis = new Vector3d( rx, ry, rz ).normalized();			
                    vecAxis.rotate( aPoint, ra );
                }
                // if ra==0, assume rx,ry,rz are the rotation angles around the 3 axis
                else if(ra==0)
		{		
                	aPoint.rotateZ(rz);
                        aPoint.rotateY(ry);
                        aPoint.rotateX(rx);
                }
			//System.out.printf("PR: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
			
                aCenter.times( -1 ); // reverse translation
                aPoint.set( aPoint.add( aCenter ) );

			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
		
		
		// do the translation here.
		Vector3d translationVec = new Vector3d( tx, ty, tz );
		aPoint.set( aPoint.add( translationVec ) );
		
		if( VERBOSE ) System.out.printf("PS: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
	}
	
	

	/**
	 * Applies the inverse of the given alignment shift to the given point.  gilfoyle 12/21/17
	 * 
	 * @param aPoint a point in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra }
	 * @param aCenter a point about which to rotate the first point (for example the midpoint of the ideal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	public static void applyInverseShift( Vector3d aPoint, double[] aShift, Vector3d aCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+NSHIFTDATARECLEN+" elements"); }
		
		double tx = aShift[0]; // The Java language has references but you cannot dereference the memory addresses like you can in C++.
		double ty = aShift[1]; // The Java runtime does have pointers, but they're not accessible to the programmer. (no pointer arithmetic)
		double tz = aShift[2];
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6];
		
		tx *= aScaleT;
		ty *= aScaleT;
		tz *= aScaleT;
		ra *= aScaleR;
		
		if( VERBOSE )
		{
			System.out.printf("iPN: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
			System.out.printf("iST: % 8.3f % 8.3f % 8.3f\n", tx, ty, tz );
			System.out.printf("iSR: % 8.3f % 8.3f % 8.3f % 8.3f\n", rx, ry, rz, Math.toDegrees(ra) );
			System.out.printf("iSC: % 8.3f % 8.3f % 8.3f\n", aCenter.x, aCenter.y, aCenter.z );
		}
			
		// undo the translation.
		Vector3d translationVec = new Vector3d( -tx, -ty, -tz );
		aPoint.set( aPoint.add( translationVec ) );

		// test size of rotation - too small creates errors.
		aCenter.times( -1 ); // reverse translation
		aPoint.set( aPoint.add( aCenter ) ); // move origin to center of rotation axis
			
			//System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
		// if ra!=0, assume rx,ry,rz defines the rotation axis and ra the angle
                // if angle is non-zero but it is too small, don't do anything
		if(Math.abs(ra) >1E-5)
		{			
		    Vector3d vecAxis = new Vector3d( rx, ry, rz ).normalized();			
                    vecAxis.rotate( aPoint, -ra );
                }
                // if ra==0, assume rx,ry,rz are the rotation angles around the 3 axis
                else if(ra==0)
		{
                        aPoint.rotateX(-rx);
                        aPoint.rotateY(-ry);
			aPoint.rotateZ(-rz);
                }
			
                //System.out.printf("PR: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );

                aCenter.times( -1 ); // reverse translation
                aPoint.set( aPoint.add( aCenter ) );

                //System.out.printf("PC: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
		
		
		if( VERBOSE ) System.out.printf("PS: % 8.3f % 8.3f % 8.3f\n", aPoint.x, aPoint.y, aPoint.z );
	}
	
	/**
	 * Applies the given alignment shift to the given volume.
	 * 
	 * @param aVol a volume in the lab frame
	 * @param aShift a translation and axis-angle rotation of the form { tx, ty, tz, rx, ry, rz, ra } (ra in radians)
	 * @param aNominalCenter a point about which to rotate the first point (for example the midpoint of the nominal fiducials)
	 * @param aScaleT a scale factor for the translation shift
	 * @param aScaleR a scale factor for the rotation shift
	 * @throws IllegalArgumentException incorrect number of elements in shift array
	 */
	public static void applyShift( Geant4Basic aVol, double[] aShift, Vector3d aNominalCenter, double aScaleT, double aScaleR ) throws IllegalArgumentException
	{
		if( aShift.length != AlignmentFactory.NSHIFTDATARECLEN ){ throw new IllegalArgumentException("shift array must have "+AlignmentFactory.NSHIFTDATARECLEN+" elements"); }
		
		double rx = aShift[3];
		double ry = aShift[4];
		double rz = aShift[5];
		double ra = aShift[6]*aScaleR; // must be in radians
		
		if( VERBOSE ) System.out.println( aVol.gemcString() );
		
		Vector3d pos = new Vector3d( aVol.getLocalPosition().x*10, aVol.getLocalPosition().y*10, aVol.getLocalPosition().z*10 ); // cm -> mm
		applyShift( pos, aShift, aNominalCenter, aScaleT, aScaleR );
		aVol.setPosition( pos.times(0.1) );
		
		double[] rot = aVol.getLocalRotation();
		Matrix rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		//double[] vec = rotMatrix.convertRotationToEulerInZYX_ExXYZ( rot[0], rot[1], rot[2] );
		
		if( VERBOSE ) System.out.printf("RI: % 8.3f % 8.3f % 8.3f\n", Math.toDegrees(-rot[0]), Math.toDegrees(-rot[1]), Math.toDegrees(-rot[2]) );
		//rotMatrix.show();
		
		/*Vector3d[] vr = new Vector3d[3];
		for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3d( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3d( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3d( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Util.toDoubleArray( vr[i] ) ) ).show();
		}*/
		
		Vector3d vrs = new Vector3d( rx, ry, rz );
		if( vrs.magnitude() != 0 ) vrs = vrs.normalized();
		Matrix shiftMatrix = Matrix.convertRotationAxisAngleToMatrix( new double[]{ vrs.x, vrs.y, vrs.z, ra } );
		rotMatrix = Matrix.matMul( shiftMatrix, rotMatrix );
		
		//rot = Matrix.convertRotationToEulerInXYZ_ExZYX( rotMatrix ); // this line causes major problems
		
		// these lines invert the rotation somehow with a zero shift
		aVol.rotate("xyz", aVol.getLocalRotation()[0], aVol.getLocalRotation()[1], aVol.getLocalRotation()[2] ); // reverse previous rotation
		aVol.rotate("xyz", -rot[0], -rot[1], -rot[2] );
		
		if( VERBOSE ) System.out.printf("RS: % 8.3f % 8.3f % 8.3f\n", Math.toDegrees(-rot[0]), Math.toDegrees(-rot[1]), Math.toDegrees(-rot[2]) );
		
		if( VERBOSE ) System.out.println( aVol.gemcString() );
		
		//rotMatrix = Matrix.convertRotationFromEulerInXYZ_ExZYX( -rot[0], -rot[1], -rot[2] ); // Geant = passive/alias, Java = active/alibi
		//rotMatrix.show();
		
		/*for( int i = 0; i < vr.length; i++ )
		{
			switch( i )
			{
			case 0:
				vr[i] = new Vector3d( 1, 0, 0 ); break;
			case 1:
				vr[i] = new Vector3d( 0, 1, 0 ); break;
			case 2:
				vr[i] = new Vector3d( 0, 0, 1 ); break;
			}
			System.out.println("vector "+i+" matrix rotated"); Matrix.matMul( rotMatrix, new Matrix(3, 1, Util.toDoubleArray( vr[i] ) ) ).show();
		}*/
	}
}
