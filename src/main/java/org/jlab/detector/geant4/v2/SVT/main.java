package org.jlab.detector.geant4.v2.SVT;

import java.io.Writer;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.volume.G4Box;
import org.jlab.detector.volume.G4Tubs;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Line3d;
import eu.mihosoft.vrl.v3d.Vector3d;

public class main {

	public static void main(String[] args) {
		
		/*Geant4Basic topVol = new G4Box("top", 1, 1, 2 );
		topVol.setMother( new G4Box("none", 1, 1, 1 ) );
		
		Geant4Basic myVol = new G4Box("myVol", 2, 2, 4 );
		myVol.setMother( topVol );
		myVol.setPosition( 0, 5, 0 );
		myVol.rotate("xyz", 0, 0, -Math.toRadians(90) );
		//myVol.rotate("xyz", 0, -Math.toRadians(45), 0 );
		
		Geant4Basic yourVol = new G4Box("yourVol", 2, 2, 4 );
		yourVol.setMother( myVol );
		yourVol.setPosition( 0, 5, 0 );
		yourVol.rotate("xyz", 0, -Math.toRadians(45), 0 );
		//yourVol.setRotation("xyz", 0, 0, -Math.toRadians(45) );
		
		Geant4Basic theirVol = new G4Box("theirVol", 2, 2, 2 );
		theirVol.setMother( yourVol );
		theirVol.rotate("xyz", 0, -Math.toRadians(90), 0 );
		theirVol.setPosition( 2, 0, 0 );
		
		System.out.println( Util.gemcStringAll( topVol ) );
		
		Geant4Basic myOtherVol = new G4Box("myOtherVol", 2, 2, 4 ); // same as myVol
		myOtherVol.setMother( topVol );
		myOtherVol.setPosition( 0, 5, 0 );
		myOtherVol.rotate("xyz", 0, 0, -Math.toRadians(90) );
		//myOtherVol.rotate("xyz", 0, -Math.toRadians(45), 0 );
		
		Util.moveChildrenToMother( myVol );
		
		System.out.println( Util.gemcStringAll( topVol ) );
		
		GdmlExporter gdmlTest = VolumeExporterFactory.createGdmlFactory();
		gdmlTest.addTopVolume( topVol );
		gdmlTest.writeFile("test_mother");*/
		//System.exit(0);
		
		//double[][] nominalData = new double[][]{ new double[]{ 1,0,-1 }, new double[]{ -1,0,-1 }, new double[]{ 0,0,2 } };
		//double[][] measuredData = new double[][]{ new double[]{ 1,0,1 }, new double[]{ 1,0,-1 }, new double[]{ -2,0,0 } };
		
		/*double[][] nominalData = new double[][]{ new double[] { -17.350, -68.283, -286.541 }, new double[]{ 17.350, -68.283, -286.541 }, new double[]{ 3.500, -68.283, 122.333 } };
		double[][] measuredData = new double[][]{ new double[]{ -17.295, -68.028, -286.365 }, new double[]{ 17.385, -67.934, -286.308 }, new double[]{ 3.942, -68.334, 122.473 } };
		
		for( int k = 0; k < nominalData.length/3; k+=3 )
		{
			System.out.println();
			for( int j = 0; j < 3; j++ )
			{
				System.out.printf("NP%d % 8.3f % 8.3f % 8.3f", j, nominalData[k+j][0], nominalData[k+j][1], nominalData[k+j][2] );
				//System.out.printf("    UP%d % 8.3f % 8.3f % 8.3f", j, measuredData[k+j][0], measuredData[k+j][1], measuredData[k+j][2] );
				//for( int i = 0; i < 3; i++ ) measuredData[k+j][i] = measuredData[k+j][i] + uncertainData[k+j][i];
				System.out.printf("    MP%d % 8.3f % 8.3f % 8.3f", j, measuredData[k+j][0], measuredData[k+j][1], measuredData[k+j][2] );
				System.out.println();
			}
		}
		
		double[][] nominalDistances = new double[nominalData.length/3][3];
		double[][] centerData = new double[nominalData.length/3][3];
		
		for( int k = 0; k < nominalData.length/3; k+=3 ) // triangle
		{
			Vector3d[] nominalPos3Ds = new Vector3d[3];
			
			System.out.printf("\n%3s", " ");
			for( int j = 0; j < 3+1; j++ ) // point, with offset
			{
				if( j < 3 )	nominalPos3Ds[j] =  new Vector3d(nominalData[k+j][0], nominalData[k+j][1], nominalData[k+j][2]);
				if( j > 0 ) // offset to wait for first point to be defined
				{
					if( k == 0) System.out.printf("%4s(%d %d)", " ", j-1, j%3 );
					nominalDistances[k][j-1] = nominalPos3Ds[j-1].distance( nominalPos3Ds[j%3] );
				}
			}
			
			Triangle3d centerTri = new Triangle3d( nominalPos3Ds[0], nominalPos3Ds[1], nominalPos3Ds[2] );
			centerData[k] = Util.toDoubleArray( centerTri.center() );
			
			System.out.printf("\nND%d % 8.3f % 8.3f % 8.3f", k, nominalDistances[k][0], nominalDistances[k][1], nominalDistances[k][2] );
			System.out.printf("    NC%d % 8.3f % 8.3f % 8.3f", k, centerData[k][0], centerData[k][1], centerData[k][2] );
			System.out.println();
		}
		
		AlignmentFactory.VERBOSE = true;
		double[][] shiftData = AlignmentFactory.calcShifts( 1, nominalData, measuredData );
		//double[][] shiftData = AlignmentFactory.calcShifts( 1, nominalData, measuredData, uncertainty );
		
		double[][] shiftedData = AlignmentFactory.applyShift( nominalData, shiftData, centerData, 1, 1 );
		
		double[][] deltasData = AlignmentFactory.calcDeltas( 3, 3, measuredData, shiftedData );
		
		System.exit(0);*/
		
		/*Vector3d originPos = new Vector3d( 0.0, 1.0, 0.0 );

		Vector3d[] fidPln0 = new Vector3d[]{ originPos.clone(), new Vector3d( 0.0, 1.0, 0.0 ) };
		Vector3d[] fidPln1 = new Vector3d[]{ new Vector3d( fidPln0[0].x + 1.0, fidPln0[0].y, fidPln0[0].z ), new Vector3d( 0.0, 0.0, 1.0 ) };
		double[] translationShift = Util.toDoubleArray( fidPln1[0].minus( fidPln0[0] ) );
		double[] rotationShift = Util.convertVectorDiffToAxisAngle( fidPln0[1], fidPln1[1] );
		
		System.out.printf(" O: %5.1f %5.1f %5.1f\n", originPos.x, originPos.y, originPos.z );
		System.out.printf("F0: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln0[0].x, fidPln0[0].y, fidPln0[0].z, fidPln0[1].x, fidPln0[1].y, fidPln0[1].z );
		System.out.printf("F1: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f\n", fidPln1[0].x, fidPln1[0].y, fidPln1[0].z, fidPln1[1].x, fidPln1[1].y, fidPln1[1].z );
		System.out.printf(" S: %5.1f %5.1f %5.1f | %5.1f %5.1f %5.1f | %5.1f\n", translationShift[0], translationShift[1], translationShift[2], rotationShift[0], rotationShift[1], rotationShift[2], Math.toDegrees(rotationShift[3]) );
		
		Vector3d arbPos0 = new Vector3d( originPos.x, originPos.y + 1.0, originPos.z + 1.0 );
		Vector3d arbPos1 = arbPos0.clone();
		
		Vector3d translationVec = Util.toVector3D( translationShift );
		Vector3d centerVec = fidPln0[0];
		Vector3d rotationVec = new Vector3d( rotationShift[0], rotationShift[1], rotationShift[2] );
		System.out.printf("A0: %5.1f %5.1f %5.1f\n", arbPos0.x, arbPos0.y, arbPos0.z );
		
		centerVec.times( -1 );
		arbPos1 = arbPos1.add( centerVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f centered\n", arbPos1.x, arbPos1.y, arbPos1.z );
		
		rotationVec.rotate( arbPos1, rotationShift[3] );
		System.out.printf("A1: %5.1f %5.1f %5.1f rotated\n", arbPos1.x, arbPos1.y, arbPos1.z );
		
		centerVec.times( -1 );
		arbPos1 = arbPos1.add( centerVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f centered back\n", arbPos1.x, arbPos1.y, arbPos1.z );
		
		arbPos1 = arbPos1.add( translationVec );
		System.out.printf("A1: %5.1f %5.1f %5.1f translated\n", arbPos1.x, arbPos1.y, arbPos1.z );
		
		Geant4Basic topVol = new G4Box("top", 1,1,1 );
		
		double orbR = 0.02, arrowR = 0.01;
		
		Geant4Basic zeroVol = new G4Box("zero", orbR, orbR, orbR );
		zeroVol.setMother( topVol );
		
		Geant4Basic originVol = new G4Box("origin", orbR, orbR, orbR );
		originVol.setPosition( originPos.times(0.1) );
		originVol.setMother( topVol );
		
		Geant4Basic fidPln0Vol = Util.createArrow("fidPln0", fidPln0[1], orbR*10, arrowR*10, true, true, false );
		fidPln0Vol.setPosition( fidPln0[0].times(0.1) );
		fidPln0Vol.setMother( topVol );
		
		Geant4Basic fidPln1Vol = Util.createArrow("fidPln1", fidPln1[1], orbR*10, arrowR*10, true, true, false );
		fidPln1Vol.setPosition( fidPln1[0].times(0.1) );
		fidPln1Vol.setMother( topVol );
		
		Geant4Basic arbPos0Vol = new G4Box("arbPos0", orbR, orbR, orbR );
		arbPos0Vol.setPosition( arbPos0.times(0.1) );
		//arbPos0Vol.setMother( topVol );
		
		Geant4Basic arbPos1Vol = new G4Box("arbPos1", orbR, orbR, orbR );
		arbPos1Vol.setPosition( arbPos1.times(0.1) );
		//arbPos1Vol.setMother( topVol );
		
		System.exit(0);*/
		
		SVTConstants.VERBOSE = true;
		DatabaseConstantProvider cp = SVTConstants.connect();
		
		SVTAlignmentFactory.setup( cp, "survey_ideals_reformat.dat", "survey_measured_reformat.dat" );
		double[][] dataFactoryIdeal = SVTAlignmentFactory.getFactoryIdealFiducialData();
		
		//AlignmentFactory.VERBOSE = true;
		SVTAlignmentFactory.calcShifts( dataFactoryIdeal, SVTAlignmentFactory.getDataSurveyMeasured(), "shifts_survey_measured_from_factory_ideal.dat" );
		//SVTAlignmentFactory.calcDeltas( dataFactoryIdeal, SVTAlignmentFactory.getDataSurveyMeasured(), "deltas_survey_measured_from_factory_ideal.dat" );
		AlignmentFactory.VERBOSE = false;
		
		SVTAlignmentFactory.calcDeltas( dataFactoryIdeal, SVTAlignmentFactory.getDataSurveyIdeal(), "deltas_survey_ideal_from_factory_ideal.dat");
		//SVTAlignmentFactory.calcDeltas( SVTAlignmentFactory.getDataSurveyIdeal(), SVTAlignmentFactory.getDataSurveyMeasured(), "deltas_survey_measured_from_survey_ideals.dat");
		
		double[][][] dataSideIdeals = SVTAlignmentFactory.calcTriangleSides( dataFactoryIdeal, 0, "sides_factory_ideal.dat");
		double[][][] dataSideMeasureds = SVTAlignmentFactory.calcTriangleSides( SVTAlignmentFactory.getDataSurveyMeasured(), 0.020, "sides_survey_measured.dat");
		SVTAlignmentFactory.calcDistanceDeltas( dataSideIdeals, dataSideMeasureds, "sides_survey_measured_from_factory_ideal.dat");
		
		//System.exit(0);
		
		int regionSelector = 1, sectorSelector = 1;
		double fidBallRadius = 0.1, // cm
				fidArrowCapRadius = 2.0, // mm
				fidArrowPointerRadius = 1.0, // mm
				stripArrowCapRadius = 0.5, // mm
				stripArrowPointerRadius = 0.25, // mm
				cornerDiscRadius = 0.075; // cm
		
		SVTConstants.loadAlignmentSectorShifts("shifts_custom.dat");
		AlignmentFactory.VERBOSE = true;
		SVTVolumeFactory svtIdealVolumeFactory = new SVTVolumeFactory( cp, true );
		
		//svtIdealVolumeFactory.setRange( regionSelector, sectorSelector, sectorSelector );
		//svtIdealVolumeFactory.setRange( regionSelector, 0, 0 );
		svtIdealVolumeFactory.setRange( regionSelector, 1, 2 );
		
		//svtIdealVolumeFactory.VERBOSE = true;
		svtIdealVolumeFactory.BUILDSENSORS = true;
		svtIdealVolumeFactory.BUILDSENSORZONES = true;
		//svtIdealVolumeFactory.BUILDPASSIVES = false;
		svtIdealVolumeFactory.BUILDMODULES = false;
		
		svtIdealVolumeFactory.makeVolumes();
		//Geant4Basic sectorVol = svtIdealVolumeFactory.createSector(); sectorVol.setMother( svtIdealVolumeFactory.getMotherVolume() );
		//Geant4Basic pcBoardVol = svtIdealVolumeFactory.createPcBoard(); pcBoardVol.setMother( svtIdealVolumeFactory.getMotherVolume() );
		
		SVTStripFactory svtIdealStripFactory = new SVTStripFactory( cp, false );
		
		String fileNameIdealFiducials = "factory_fiducials_ideal.dat";
		Writer fileIdealFiducials = Util.openOutputDataFile( fileNameIdealFiducials );
		
		for( int region = svtIdealVolumeFactory.getRegionMin()-1; region < svtIdealVolumeFactory.getRegionMax(); region++ )
			for( int sector = svtIdealVolumeFactory.getSectorMin()[region]-1; sector < svtIdealVolumeFactory.getSectorMax()[region]; sector++ )
			{
				for( int module = svtIdealVolumeFactory.getModuleMin()-1; module < svtIdealVolumeFactory.getModuleMax(); module++ )
				{
					for( int strip = 0; strip < 1; strip+=16 ) // SVTConstants.NSTRIPS
					{
						Line3d stripLine = svtIdealStripFactory.getStrip( region, sector, module, strip );
						//System.out.printf("\nr%ds%dm%ds%d %s\n", region, sector, module, strip, stripLine.toString() );
						Geant4Basic stripVol = Util.createArrow("strip"+strip+"_m"+module+"_s"+sector+"_r"+region, stripLine, stripArrowCapRadius, stripArrowPointerRadius, false, true, false ); // mm
						stripVol.setMother( svtIdealVolumeFactory.getMotherVolume() );
						//System.out.println( stripVol.gemcString() );
						//for( int c = 0; c < stripVol.getChildren().size(); c++ )
							//System.out.println( stripVol.getChildren().get(c).gemcString() );
					}
					
					Vector3d[] layerCorners = svtIdealStripFactory.getLayerCorners( region, sector, module );
					for( int i = 0; i < layerCorners.length; i++ )
					{
						Geant4Basic cornerDisc = new G4Tubs("cornerBall"+i+"_m"+module+"_s"+sector+"_r"+region, 0, cornerDiscRadius, cornerDiscRadius/2.0, 0, 360 ); // cm
						cornerDisc.setPosition( layerCorners[i].times(0.1) ); // mm -> cm
						//cornerDisc.setMother( svtIdealVolumeFactory.getMotherVolume() );
					}
				}
				
				Vector3d fidPos3Ds[] = SVTAlignmentFactory.getIdealFiducials( region, sector );
				
				for( int fid = 0; fid < SVTConstants.NFIDUCIALS; fid++ )
				{
					Util.writeLine( fileIdealFiducials, String.format("R%dS%02dF%d % 8.3f % 8.3f % 8.3f\n", region+1, sector+1, fid+1, fidPos3Ds[fid].x, fidPos3Ds[fid].y, fidPos3Ds[fid].z ) );
					
					Geant4Basic fidBox = new G4Box("fiducialBall"+fid+"_s"+sector+"_r"+region, 0.1, 0.1, 0.1 ); // cm
					fidBox.setPosition( fidPos3Ds[fid].times(0.1) ); // mm->cm
					fidBox.setMother( svtIdealVolumeFactory.getMotherVolume() );
				}
				
				/*Triangle3d fidTri3D = new Triangle3d( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
				Vector3d fidVec3D = fidTri3D.normal.normalized();
				fidVec3D.times( 10 ); // length of arrow in mm
				Geant4Basic fidCen = Util.createArrow( "fiducialCenter_s"+sector+"_r"+region, fidVec3D, fidArrowCapRadius, fidArrowPointerRadius, true, true, false ); // mm
				fidCen.setPosition( fidTri3D.center().times(0.1) ); // mm->cm
				fidCen.setMother( svtIdealVolumeFactory.getMotherVolume() );*/
			}
		
		Util.closeOutputDataFile( fileNameIdealFiducials, fileIdealFiducials );
		
		//System.out.println( svtIdealVolumeFactory.toString() );

	}

}
