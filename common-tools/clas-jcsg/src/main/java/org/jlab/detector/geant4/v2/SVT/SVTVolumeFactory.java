package org.jlab.detector.geant4.v2.SVT;

import org.jlab.detector.volume.G4Operation;
import org.jlab.detector.volume.G4Pgon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.volume.G4Box;
import org.jlab.detector.volume.G4Tubs;
import org.jlab.detector.volume.G4World;
import org.jlab.detector.volume.Geant4Basic;
import org.jlab.geometry.prim.Triangle3d;

import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * <h1> Geometry for the SVT </h1>
 * 
 * length unit: mm (3D Primitives), cm (Geant4Basic volume positions) <br>
 * angle unit: deg <br>
 * 
 * Conventions:
 * <ul>
 * <li> svt = four concentric regions / superlayers </li>
 * <li> region / superlayer = ring of a variable number of sectors </li>
 * <li> sector = pair of sensor  modules and backing structure, connected and stabilised by copper and peek supports </li>
 * <li> module = triplet of sensors </li>
 * <li> sensor = silicon with etched strips in active region </li>
 * <li> layer = plane of sensitive strips, spanning active regions of module </li>
 * <li> strip = sensitive line </li>
 * </ul>
 * 
 * @author pdavies
 * @version 1.1.2
 */
public class SVTVolumeFactory
{
	private Geant4Basic motherVol;
	private final HashMap< String, String > parameters = new LinkedHashMap<>(); // store core parameters from CCDB and build switches for GEMC's perl scripts
	private final HashMap< String, String > properties = new LinkedHashMap<>(); // author, email, date
	
	private int regionMin, regionMax, moduleMin, moduleMax, layerMin, layerMax;
	private int[] sectorMin, sectorMax;
	
	private boolean bShift = false; // switch to select whether alignment shifts are applied
	private double scaleT = 1.0, scaleR = 1.0;
        
         private double motherVolumeLength;
         private double motherVolumeRmin;
         private double motherVolumeRmax;
	
	
	/**
	 * A switch to control whether debug information is displayed
	 * Default: false
	 */
	public boolean VERBOSE = false;
	
	/**
	 * A switch to control whether the sensor active and dead zones are made instead of the physical volume
	 * BUILDSENSORS=true must also be set.
	 * Default: false
	 */
	public boolean BUILDSENSORZONES = false;
	
	/**
	 * A switch to control whether the physical sensors are made inside the strip modules.
	 * Default: false
	 */
	public boolean BUILDSENSORS = false;
	
	/**
	 * A switch to control whether the sensor modules are made.
	 * Disable to see the high voltage rail assembly.
	 * Default: true
	 */
	public boolean BUILDMODULES = true;
	
	/**
	 * A switch to make only the sensors, without the passive materials
	 * Default: true
	 */
	public boolean BUILDPASSIVES = true;
	
	/**
	 * A parameter to add space between volumes to prevent overlaps
	 * Default: 0.0
	 */
	public double VOLSPACER = 0.0;
	
	
	/**
	 * A switch to halve the dimensions of all boxes for geant4 format.
	 * Default: false
	 * @deprecated
	 */
	public boolean HALFBOXES = false;
	
	
	
	/**
	 * Constructs a new geometry factory for detector volumes.
	 * Please run {@code SVTConstants.connect() } first.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the necessary tables
	 * @param applyAlignmentShifts a switch to set whether the alignment shifts from CCDB will be applied
	 */
	public SVTVolumeFactory( DatabaseConstantProvider cp, boolean applyAlignmentShifts )
	{
		SVTConstants.load( cp );
		setApplyAlignmentShifts( applyAlignmentShifts );
		if( bShift == true && SVTConstants.getLayerSectorAlignmentData() == null ){
			System.err.println("error: SVTVolumeFactory: no shifts loaded");
			System.exit(-1);
		}
		
		sectorMin = new int[SVTConstants.NREGIONS];
		sectorMax = new int[SVTConstants.NREGIONS];
		
		// default behaviour
		setRange( 1, SVTConstants.NREGIONS, new int[]{ 1, 1, 1, 1 }, SVTConstants.NSECTORS, 1, SVTConstants.NMODULES ); // all regions, sectors, and modules
		
		motherVolumeRmin   = SVTConstants.LAYERRADIUS[0][0]*0.87;
		motherVolumeRmax   = SVTConstants.LAYERRADIUS[regionMax-1][1]*1.13;
		motherVolumeLength = SVTConstants.SECTORLEN*1.404;
		
		motherVol = new G4Tubs("svt", motherVolumeRmin*0.1, motherVolumeRmax*0.1, motherVolumeLength/2.0*0.1, 0, 360 );
		
		Geant4Basic top = new G4World("none");
		motherVol.setMother( top );
		
		if( SVTConstants.VERBOSE ) VERBOSE = true;
	}
	
	
	/**
	 * Populates the HashMaps with constants.
	 */
	public void putParameters()
	{
		System.out.println("factory: populating HashMap with parameters");
		
		properties.put("unit_length", "mm");
		properties.put("unit_angle", "deg");
		properties.put("author", "P. Davies");
		properties.put("email", "pdavies@jlab.org");
		properties.put("date", "7/15/16");
		
		parameters.put("nregions", Integer.toString(SVTConstants.NREGIONS) );
		
		for( int r = 0; r < SVTConstants.NREGIONS; r++)
		{
			parameters.put("nsectors_r"+(r+1), Integer.toString( SVTConstants.NSECTORS[r] ) );
			//parameters.put("z_start_r"+(r+1),  Double.toString(  SVTConstants.Z0ACTIVE[r] ) );
		}
		parameters.put("nmodules", Integer.toString(SVTConstants.NMODULES) );
		parameters.put("nsensors", Integer.toString(SVTConstants.NSENSORS) );
		parameters.put("npads", Integer.toString(SVTConstants.NPADS) );
		
		parameters.put("bsensorzones", Integer.toString(BUILDSENSORZONES ? 1 : 0 ) );
		parameters.put("bsensors", Integer.toString(BUILDSENSORS ? 1 : 0 ) );
		
		//parameters.put("nlayers", Integer.toString(SVTConstants.NLAYERS) );
		//parameters.put("ntotalsectors", Integer.toString(SVTConstants.NTOTALSECTORS) );
		//parameters.put("silicon_thk", Double.toString(SVTConstants.SILICONTHK) );
		// ...
		
		/*for( Map.Entry< String, String > entry : parameters.entrySet() )
		{
			System.out.println( entry.getKey() +" "+ entry.getValue() );
		}
		System.exit(0);*/
	}
	
	
	/**
	 * Returns one specified box volume.
	 * 
	 * @param aName a key in the MATERIALDIMENSIONS HashMap
	 * @return Geant4Basic a box positioned at the origin
	 * @throws IllegalArgumentException unknown material
	 */
	public static Geant4Basic createNamedVolume( String aName ) throws IllegalArgumentException
	{
		double[] dims = SVTConstants.MATERIALDIMENSIONS.get( aName );
		if( dims == null )
			throw new IllegalArgumentException("unknown material: \""+ aName + "\"");
		
		switch( SVTConstants.MATERIALTYPES.get( aName ) )
		{
		case "box":
			return new G4Box( aName, dims[0]/2.0*0.1, dims[1]/2.0*0.1, dims[2]/2.0*0.1 ); // wid/2, thk/2, len/2
		case "tube":
			return new G4Tubs( aName, dims[0]*0.1, dims[1]*0.1, dims[2]/2.0*0.1, dims[3], dims[4] ); // rmin, rmax, zlen/2, phi0, dphi
		}
		return null;
	}
		
	
	/**
	 * Generates the geometry using the current range and stores it in the mother volume. 
	 */
	public void makeVolumes()
	{
		System.out.println("factory: generating geometry with the following parameters");
		
		if( bShift )
		{
			System.out.println("  variation: shifted" );
			if( !(scaleT - 1.0 < 1.0E-3 && scaleR - 1.0 < 1.0E-3) ){ System.out.println("  scale(T,R): "+ scaleT + " " + scaleR ); }
		}
		else
		{
			System.out.println("  variation: ideal");
		}
		System.out.println( "  "+showRange() );
		System.out.println("  build passive materials ? "+ BUILDPASSIVES );
		System.out.println("  include physical sensors ? "+ BUILDSENSORS );
		if( BUILDSENSORS ) System.out.println("  include sensor active and dead zones ? "+ BUILDSENSORZONES );
		//System.out.println("  halve dimensions of boxes ? "+ HALFBOXES );
		
                  this.makeCage();
                  
		for( int region = regionMin-1; region < regionMax; region++ ) // NREGIONS
		{
			//if( VERBOSE ) System.out.println("r "+region);			
			G4Tubs regionVol = (G4Tubs) createRegion( region );
			regionVol.setMother( motherVol );
			regionVol.setName( regionVol.getName() + (region+1) );
			Util.appendChildrenName( regionVol, "_r"+ (region+1) );
			
			double zStartPhysical = SVTConstants.Z0ACTIVE[region] - SVTConstants.DEADZNLEN; // Cu edge of hybrid sensor's physical volume
			double heatSinkCuZStart = 5.60; // CuStart from fidOriginZ
			regionVol.translate( 0, 0, (zStartPhysical - SVTConstants.FIDORIGINZ - heatSinkCuZStart )*0.1 + regionVol.getZHalfLength() ); // central dowel hole
			
			if( bShift )
			{
				for( int sector = sectorMin[region]-1; sector < sectorMax[region]; sector++ )
				{
					Geant4Basic sectorVol = regionVol.getChildren().get( sector );
					
					//System.out.println("N "+sectorVol.gemcString() );
					Vector3d[] fidPos3Ds = SVTAlignmentFactory.getIdealFiducials( region, sector );
					Triangle3d fidTri3D = new Triangle3d( fidPos3Ds[0], fidPos3Ds[1], fidPos3Ds[2] );
					
					//System.out.println("rs "+ convertRegionSector2SvtIndex( aRegion, sector ));
					//double[] shift = SVTConstants.getDataAlignmentSectorShift()[SVTConstants.convertRegionSector2Index( region, sector )].clone();
					
					/*if( VERBOSE )
					{
						Vector3d vec = new Vector3d( shift[3], shift[4], shift[5] ).normalized();
						vec.times( (shift[6] == 0) ? 0 : 10 ); // length in mm, but only if non-zero angle
						//vec.show();
						
						Geant4Basic vecVol = Util.createArrow("rotAxis"+sector, vec, 2.0, 1.0, true, true, false );
						vecVol.setPosition( fidTri3D.center().times(0.1) );
						vecVol.setMother( regionVol );
						//System.out.println( vecVol.gemcString() );
					}*/
					
					/*int n = 1; // number of steps to show (n=1 for final step only)
					double d = shift[6]/n;
					for( int i = 1; i < n; i++ )
					{
						//System.out.println( "vol "+ i );
						Geant4Basic stepVol = Util.clone( sectorVol );
						stepVol.setMother(regionVol);
						Util.appendName( stepVol, "_"+i );
						shift[6] = i*d;
						AlignmentFactory.applyShift( stepVol, shift, fidTri3D.center(), scaleT, scaleR );
						//System.out.println("  "+stepVol.gemcString() );
						//for( int j = 0; j < stepVol.getChildren().size(); j++ )
							//System.out.println( stepVol.getChildren().get(j).gemcString() );
					}*/
					
                                        // FIXME currently using shifts from bottom module of each region
					AlignmentFactory.applyShift( sectorVol, SVTConstants.getLayerSectorAlignmentData()[sector][region*2], fidTri3D.center(), scaleT, scaleR );
					//System.out.println("S "+sectorVol.gemcString() );
				}
			}
		}
		
		//if( HALFBOXES ) Util.scaleDimensions(motherVol, 0.5, true);
	}
	
	
	/**
	 * Returns one region, containing the required number of sector modules.
	 * 
	 * @param aRegion an index starting from 0
	 * @return Geant4Basic a dummy volume positioned at the origin
	 * @throws IllegalArgumentException index out of bounds
	 */
	public Geant4Basic createRegion( int aRegion ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		
		double rfit = 4.0; // scale factor to encompass the entire volume of the sectors
		double rcen = 0.5*(SVTConstants.LAYERRADIUS[aRegion][1] + SVTConstants.LAYERRADIUS[aRegion][0]);
		double rthk = rfit*0.5*(SVTConstants.LAYERRADIUS[aRegion][1] - SVTConstants.LAYERRADIUS[aRegion][0]);
		double rmin = SVTConstants.REGIONPEEKRMIN[aRegion]-1.0;
		double rmax = rcen + rthk;
		double zlen = SVTConstants.SECTORLEN; // same length as dummy sector volume
		
		if( VERBOSE )
		{
			System.out.printf("region= %d\n", aRegion );
			System.out.printf("rcen=% 8.3f\n", rcen );
			System.out.printf("rthk=% 8.3f\n", rthk );
			System.out.printf("rmin=% 8.3f\n", rmin );
			System.out.printf("rmax=% 8.3f\n", rmax );
			System.out.printf("len= % 8.3f\n", zlen );
			System.out.println();
		}
		
		Geant4Basic regionVol = new G4Tubs("region", rmin*0.1, rmax*0.1, zlen/2.0*0.1, 0, 360 );

                   // add the peek support
		this.createDownstreamSupport(aRegion, regionVol);
                
		for( int sector = sectorMin[aRegion]-1; sector < sectorMax[aRegion]; sector++ ) // NSECTORS[region]
		{
			//if( VERBOSE ) System.out.println(" s "+sector);
			Geant4Basic sectorVol = createSector();
			sectorVol.setMother( regionVol );
			
			sectorVol.setName( sectorVol.getName() + (sector+1) ); // append name
			Util.appendChildrenName( sectorVol, "_s"+ (sector+1) ); // append tag to end of name (GdmlFile's material replacement search fails if it's prepended)
			
			double phi = SVTConstants.getPhi( aRegion, sector ); // module rotation about target / origin
			double psi = phi - SVTConstants.SECTOR0 - Math.PI; // module rotation about centre of geometry, -180 deg to set zero volume rotation for sector 1
			
			//System.out.println("phi="+Math.toDegrees(phi));
			
			Vector3d pos = new Vector3d( rcen, 0.0, 0.0 );
			pos.transform( new Transform().rotZ( -phi ) ); // change of sign for active/alibi -> passive/alias rotation
			//System.out.printf("% 8.3f % 8.3f % 8.3f\n", pos.x, pos.y, pos.z );
			
			sectorVol.rotate("xyz", 0.0, 0.0, -psi ); // change of sign for active/alibi -> passive/alias rotation
			sectorVol.setPosition( pos.times(0.1) );
			//Util.moveChildrenToMother( sectorVol );
		}
		
		return regionVol;
	}
	
	public void makeCage() {
            
              for(int i=0; i<SVTConstants.FARADAYCAGERMIN.length; i++) {
                   double zmin = Math.max(-SVTConstants.FARADAYCAGELENGTH[i]/2+SVTConstants.FARADAYCAGEZPOS[i], -this.motherVolumeLength/2);
                   double zmax = SVTConstants.FARADAYCAGELENGTH[i]/2+SVTConstants.FARADAYCAGEZPOS[i];                   
                   Geant4Basic cage = new G4Tubs("faradayCage" + SVTConstants.FARADAYCAGENAME[i], SVTConstants.FARADAYCAGERMIN[i]*0.1
                                                                                                , SVTConstants.FARADAYCAGERMAX[i]*0.1
                                                                                                , (zmax-zmin)/2*0.1
                                                                                                , 0, 360 );
                   cage.setMother( motherVol );
		cage.setPosition(0, 0, (zmax+zmin)/2*0.1); 
			
              }
         }
        
        
	/**
	 * Returns one sector module, containing a pair of sensor modules and backing structure.
	 * 
	 * @return Geant4Basic a dummy volume positioned in the lab frame
	 */
	public Geant4Basic createSector()
	{
		double heatSinkThk = SVTConstants.MATERIALDIMENSIONS.get("heatSinkCu")[1];
		double heatSinkRidgeThk = SVTConstants.MATERIALDIMENSIONS.get("heatSinkRidge")[1];
		double rohacellThk = SVTConstants.MATERIALDIMENSIONS.get("rohacell")[1];
		double carbonFiberThk = SVTConstants.MATERIALDIMENSIONS.get("carbonFiber")[1];
		double busCableThk = SVTConstants.MATERIALDIMENSIONS.get("busCable")[1];
		double epoxyThk = SVTConstants.MATERIALDIMENSIONS.get("epoxyAndRailAndPads")[1];
		double pitchAdaptorThk = SVTConstants.MATERIALDIMENSIONS.get("pitchAdaptor")[1];
		double pcBoardAndChipsThk = SVTConstants.MATERIALDIMENSIONS.get("pcBoardAndChips")[1];
		
		double heatSinkY = heatSinkThk/2 + heatSinkRidgeThk/2;
		
		double heatSinkLen = SVTConstants.MATERIALDIMENSIONS.get("heatSink")[2];
		double rohacellLen = SVTConstants.MATERIALDIMENSIONS.get("rohacell")[2];
		double heatSinkRidgeLen = SVTConstants.MATERIALDIMENSIONS.get("heatSinkRidge")[2];
		
		double rohacellZStart = 59.730; // CuEnd from fidOriginZ
		double heatSinkCuZStart = 5.60; // CuStart from fidOriginZ
		double carbonFiberZStart = 1.0; // from CuEnd of heatSinkRidge
		double pitchAdaptorZEnd = 61.43; // from fidOriginZ
		double pcBoardZEnd = 57.13; // from fidOriginZ
		
		double wid = SVTConstants.MODULEWID;
		//double thk = SVTConstants.LAYERGAPTHK + 2*SVTConstants.SILICONTHK;
		double thk = rohacellThk + 2*(SVTConstants.PASSIVETHK + pcBoardAndChipsThk);
		double len = SVTConstants.SECTORLEN;
		
		Geant4Basic sectorVol = new G4Box("sector", wid/2.0*0.1, thk/2.0*0.1, len/2.0*0.1 );
		
		// position components relative to fiducial layer
		
		// for each side (U,V)
		// 		create sensor module
		// 		create passive materials (carbon fibre, bus cable, epoxy)
		
		if( BUILDPASSIVES )
		{			
			Geant4Basic heatSinkVol = createHeatSink();
			heatSinkVol.setMother( sectorVol );
			heatSinkVol.setPosition( 0.0, heatSinkY*0.1, -heatSinkCuZStart*0.1 + heatSinkLen/2*0.1 );
			Util.moveChildrenToMother( heatSinkVol );
			
			Geant4Basic rohacellVol = createNamedVolume("rohacell");
			rohacellVol.setMother( sectorVol );
			rohacellVol.setPosition( 0.0, rohacellThk/2*0.1, rohacellZStart*0.1 + rohacellLen/2*0.1 );
		}
		
		for( int module = moduleMin-1; module < moduleMax; module++ ) // NMODULES
		{
			if( BUILDMODULES )
			{
				//if( VERBOSE ) System.out.println("  m "+ module );
				Geant4Basic moduleVol = createModule();
				moduleVol.setMother( sectorVol );
				moduleVol.setName( moduleVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( moduleVol, "_m" + (module+1) );
				
				double moduleY = 0.0;
				
				switch( module ) 
				{
				case 0: // U = inner
					moduleY = 0.0 + rohacellThk + SVTConstants.PASSIVETHK + SVTConstants.SILICONTHK/2;
					break;
					
				case 1: // V = outer
					moduleY = 0.0 - SVTConstants.PASSIVETHK - SVTConstants.SILICONTHK/2;
					break;
				}
				
				moduleVol.setPosition( 0.0, moduleY*0.1, (SVTConstants.FIDORIGINZ + SVTConstants.MODULELEN/2)*0.1 );
			}
			
			if( BUILDPASSIVES )
			{
				G4Box carbonFiberVol = (G4Box) createCarbonFiber();
				carbonFiberVol.setMother( sectorVol );
				carbonFiberVol.setName( carbonFiberVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( carbonFiberVol, "_m" + (module+1) );
				
				G4Box busCableVol = (G4Box) createBusCable();
				busCableVol.setMother( sectorVol );
				busCableVol.setName( busCableVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( busCableVol, "_m" + (module+1) );
				
				G4Box pitchAdaptorVol = (G4Box) createNamedVolume("pitchAdaptor");
				pitchAdaptorVol.setMother( sectorVol );
				pitchAdaptorVol.setName( pitchAdaptorVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( pitchAdaptorVol, "_m" + (module+1) );
				
				G4Box pcBoardAndChipsVol = (G4Box) createPcBoardAndChips();
				pcBoardAndChipsVol.setMother( sectorVol );
				pcBoardAndChipsVol.setName( pcBoardAndChipsVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( pcBoardAndChipsVol, "_m" + (module+1) );
				
				G4Box epoxyAndRailAndPadsVol = (G4Box) createEpoxyAndRailAndPads();
				epoxyAndRailAndPadsVol.setMother( sectorVol );
				epoxyAndRailAndPadsVol.setName( epoxyAndRailAndPadsVol.getName() + "_m" + (module+1) );
				Util.appendChildrenName( epoxyAndRailAndPadsVol, "_m" + (module+1) );
				
				double carbonFiberY = 0.0;
				double busCableY = 0.0;
				double pitchAdaptorY = 0.0;
				double pcBoardY = 0.0;
				double epoxyY = 0.0;
				
				switch( module ) 
				{
				case 0: // U = inner
					carbonFiberY  = 0.0 + rohacellThk + carbonFiberThk/2;
					busCableY     = 0.0 + rohacellThk + carbonFiberThk + busCableThk/2;
					epoxyY        = 0.0 + rohacellThk + carbonFiberThk + busCableThk + epoxyThk/2;
					pitchAdaptorY = 0.0 + rohacellThk + SVTConstants.PASSIVETHK + pitchAdaptorThk/2;
					pcBoardY      = 0.0 + rohacellThk + SVTConstants.PASSIVETHK + pcBoardAndChipsVol.getYHalfLength()*10;
					break;
					
				case 1: // V = outer
					
					carbonFiberY  = 0.0 - carbonFiberThk/2;
					busCableY     = 0.0 - carbonFiberThk - busCableThk/2;
					epoxyY        = 0.0 - carbonFiberThk - busCableThk - epoxyThk/2;
					pitchAdaptorY = 0.0 - SVTConstants.PASSIVETHK - pitchAdaptorThk/2;
					pcBoardY      = 0.0 - SVTConstants.PASSIVETHK - pcBoardAndChipsVol.getYHalfLength()*10;
					
					epoxyAndRailAndPadsVol.rotate("xyz", 0.0, 0.0, Math.PI );
					pcBoardAndChipsVol.rotate("xyz", 0.0, 0.0, Math.PI );
					break;
				}
			
				carbonFiberVol.setPosition(         0.0, carbonFiberY*0.1,  (0.0 - heatSinkCuZStart + heatSinkRidgeLen + carbonFiberZStart)*0.1 + carbonFiberVol.getZHalfLength() );
				busCableVol.setPosition(            0.0, busCableY*0.1,     (0.0 - heatSinkCuZStart + heatSinkRidgeLen + carbonFiberZStart)*0.1 + busCableVol.getZHalfLength() ); // same Z position as carbonFiber
				epoxyAndRailAndPadsVol.setPosition( 0.0, epoxyY*0.1,        (0.0 - heatSinkCuZStart + heatSinkRidgeLen + carbonFiberZStart)*0.1 + epoxyAndRailAndPadsVol.getZHalfLength() ); // same Z position as carbonFiber
				pitchAdaptorVol.setPosition(        0.0, pitchAdaptorY*0.1, pitchAdaptorZEnd*0.1 - pitchAdaptorVol.getZHalfLength() );
				pcBoardAndChipsVol.setPosition(     0.0, pcBoardY*0.1,      pcBoardZEnd*0.1 - pcBoardAndChipsVol.getZHalfLength() );
			}
			
			/*for( int kapton = 0; kapton < 1; kapton++ ) // left, right
			{
				
			}*/
		}
		
		for( Geant4Basic child : sectorVol.getChildren() )
			Util.shiftPosition( child, 0.0, -rohacellThk/2*0.1, (heatSinkCuZStart - len/2)*0.1 );
	
		return sectorVol;
	}
	
	
	/**
	 * Returns one sensor module, containing 3 physical sensors.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createModule()
	{		
		Geant4Basic moduleVol = new G4Box( "module", SVTConstants.PHYSSENWID/2.0*0.1, SVTConstants.SILICONTHK/2.0*0.1, SVTConstants.MODULELEN/2.0*0.1 );
		
		if( BUILDSENSORS ) // gemc handles the physical sensors itself in the hit process algorithm
		{
			for( int sensor = 0; sensor < SVTConstants.NSENSORS; sensor++ )
			{
				//if( VERBOSE ) System.out.println("   sp "+ sensor );
				Geant4Basic sensorVol = createSensorPhysical(); // includes active and dead zones as children
				sensorVol.setMother( moduleVol );
				sensorVol.setName( sensorVol.getName() + (sensor+1) ); // add switch for hybrid, intermediate and far labels?
				Util.appendChildrenName( sensorVol, "_sp"+ (sensor+1) );
				
				// module length = || DZ |  AL  | DZ |MG| DZ |  AL  | DZ |MG| DZ |  AL  | DZ ||
				//                  ^<-mid->^<-----stepLen----->^<-----stepLen----->^
				
				double deadZnLen   = SVTConstants.DEADZNLEN;
				double activeZnLen = SVTConstants.ACTIVESENLEN;
				double microGapLen = SVTConstants.MICROGAPLEN;
				double moduleLen   = SVTConstants.MODULELEN;
				double sensorZ = 0.0;
				double sensorPhysicalMidPos = deadZnLen + activeZnLen/2; // mid
				double stepLen = activeZnLen + deadZnLen + microGapLen + deadZnLen; // stepLen
				sensorZ = sensorPhysicalMidPos + sensor*stepLen - moduleLen/2;
				sensorVol.setPosition( 0.0, 0.0, sensorZ*0.1 );
			}
		}
		
		return moduleVol;
	}
	
	
	/**
	 * Returns one physical sensor, containing active zone and dead zones.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createSensorPhysical()
	{		
		Geant4Basic senPhysicalVol = new G4Box( "sensorPhysical", SVTConstants.PHYSSENWID/2.0*0.1, SVTConstants.SILICONTHK/2.0*0.1, SVTConstants.PHYSSENLEN/2.0*0.1 );
		
		if( BUILDSENSORZONES ) // gemc handles the active and dead zones itself in the hit process algorithm
		{
			Geant4Basic senActiveVol = createSensorActive();
			senActiveVol.setMother( senPhysicalVol );
			
			// physical sensor length = || DZ |  AL  | DZ || // dead zones either side of active zone
			//
			// + - - - - - - - - + - +
			// |           l1        |
			// | - + - - - - - - + - +
			// | w2| active zone | w3|
			// | - + - - - - - - + - +
			// |           l0        |
			// + - + - - - - - - + - +
			//
			// length, width
			
			List<Geant4Basic> deadZnVols = new ArrayList<>();
			for( int deadZn = 0; deadZn < 4; deadZn++ )
			{
				Geant4Basic deadZnVol = null;
				switch( deadZn )
				{
				case 0:
					deadZnVol = createDeadZone("l");
					deadZnVol.setPosition( -(SVTConstants.ACTIVESENWID + SVTConstants.DEADZNWID + VOLSPACER)/2.0*0.1, 0.0, 0.0 );
					break;
				case 1:
					deadZnVol = createDeadZone("l");
					deadZnVol.setPosition(  (SVTConstants.ACTIVESENWID + SVTConstants.DEADZNWID + VOLSPACER)/2.0*0.1, 0.0, 0.0 );
					break;
				case 2:
					deadZnVol = createDeadZone("w");
					deadZnVol.setPosition( 0.0, 0.0, -(SVTConstants.ACTIVESENLEN + SVTConstants.DEADZNLEN + VOLSPACER)/2.0*0.1 );
					break;
				case 3:
					deadZnVol = createDeadZone("w");
					deadZnVol.setPosition( 0.0, 0.0,  (SVTConstants.ACTIVESENLEN + SVTConstants.DEADZNLEN + VOLSPACER)/2.0*0.1 );
					break;
				}
				
				deadZnVol.setName( deadZnVol.getName() + (deadZn+1) );
				deadZnVol.setMother(senPhysicalVol);
				deadZnVols.add( deadZnVol );
			}
		}
		
		return senPhysicalVol;
	}
	
	
	/**
	 * Returns one active zone of a sensor.
	 * 
	 * @return Geant4Basic a volume positioned at the origin
	 */
	public Geant4Basic createSensorActive( )
	{	
		return new G4Box( "sensorActive", SVTConstants.ACTIVESENWID/2.0*0.1, SVTConstants.SILICONTHK/2.0*0.1, SVTConstants.ACTIVESENLEN/2.0*0.1 );
	}
	
	
	/**
	 * Returns one dead zone of a sensor.
	 * 
	 * @param aType one of two types of dead zone, extending along the length "l" or width "w" of the sensor
	 * @return Geant4Basic a volume positioned relative to a physical sensor
	 * @throws IllegalArgumentException unknown type
	 */
	public Geant4Basic createDeadZone( String aType ) throws IllegalArgumentException
	{
		Geant4Basic deadZnVol = null;
		
		switch( aType )
		{
		case "l":
			deadZnVol = new G4Box( "deadZoneLen", SVTConstants.DEADZNWID/2.0*0.1, SVTConstants.SILICONTHK/2.0*0.1, (SVTConstants.ACTIVESENLEN + 2*SVTConstants.DEADZNLEN)/2.0*0.1 );
			break;
		case "w":
			deadZnVol = new G4Box( "deadZoneWid", SVTConstants.ACTIVESENWID/2.0*0.1, SVTConstants.SILICONTHK/2.0*0.1, SVTConstants.DEADZNLEN/2.0*0.1 );
			break;
		default:
			throw new IllegalArgumentException("unknown dead zone type: "+ aType );
		}
		return deadZnVol;
	}
	
	
	
	public Geant4Basic createHeatSink()
	{
		G4Box mainVol = (G4Box) createNamedVolume("heatSink");
		
		G4Box cuVol = (G4Box) createNamedVolume("heatSinkCu");
		cuVol.setMother( mainVol );
		
		G4Box ridgeVol = (G4Box) createNamedVolume("heatSinkRidge"); // small protruding ridge
		ridgeVol.setMother( mainVol );
		
		cuVol.setPosition(    0.0, -ridgeVol.getYHalfLength(), 0.0 );
		ridgeVol.setPosition( 0.0, cuVol.getYHalfLength(), ridgeVol.getZHalfLength() - cuVol.getZHalfLength() );
		
		return mainVol;
	}
	
	
	
	public Geant4Basic createCarbonFiber()
	{
		G4Box mainVol = (G4Box) createNamedVolume("carbonFiber");
		
		G4Box cuVol = (G4Box) createNamedVolume("carbonFiberCu");
		cuVol.setMother( mainVol );
		
		G4Box pkVol = (G4Box) createNamedVolume("carbonFiberPk");
		pkVol.setMother( mainVol );
		
		cuVol.setPosition( 0.0, 0.0, cuVol.getZHalfLength() - mainVol.getZHalfLength() );
		pkVol.setPosition( 0.0, 0.0, pkVol.getZHalfLength() - mainVol.getZHalfLength() + cuVol.getZHalfLength()*2.0 );
		
		//     + - - +
		// + - +     |
		// |Cu | Pk  |
		// + - +     |
		//     + - - +
		
		return mainVol;
	}

	
	
	public Geant4Basic createBusCable()
	{
		G4Box mainVol = (G4Box) createNamedVolume("busCable");
		
		G4Box cuVol = (G4Box) createNamedVolume("busCableCu");
		cuVol.setMother( mainVol );
		
		G4Box pkVol = (G4Box) createNamedVolume("busCablePk");
		pkVol.setMother( mainVol );
		
		cuVol.setPosition( 0.0, 0.0, cuVol.getZHalfLength() - mainVol.getZHalfLength() );
		pkVol.setPosition( 0.0, 0.0, pkVol.getZHalfLength() - mainVol.getZHalfLength() + cuVol.getZHalfLength()*2.0 );
		
		return mainVol;
	}
	
	
	
	public Geant4Basic createPcBoardAndChips() // mmm, chips
	{
		G4Box mainVol = (G4Box) createNamedVolume("pcBoardAndChips");
		
		G4Box pcBoardVol = (G4Box) createNamedVolume("pcBoard");
		pcBoardVol.setMother( mainVol );
		
		G4Box chipLVol = (G4Box) createNamedVolume("chip");
		chipLVol.setMother( mainVol );
		chipLVol.setName("chipL");
		
		G4Box chipRVol = (G4Box) createNamedVolume("chip");
		chipRVol.setMother( mainVol );
		chipRVol.setName("chipR");
		
		double chipX = 13.74 - 7.50/2;
		double chipZ = pcBoardVol.getZHalfLength()*10.0 - 17.99 + 17.45 - chipLVol.getZHalfLength()*10.0;
				
		pcBoardVol.setPosition( 0.0, -pcBoardVol.getYHalfLength(), 0.0 );
		chipLVol.setPosition(  chipX*0.1, chipLVol.getYHalfLength(), chipZ*0.1 );
		chipRVol.setPosition( -chipX*0.1, chipRVol.getYHalfLength(), chipZ*0.1 );
		
		return mainVol;
	}
	
	
	
	public Geant4Basic createEpoxyAndRailAndPads()
	{
		G4Box mainVol = (G4Box) createNamedVolume("epoxyAndRailAndPads");
		
		double railXStart = 6.0;
		double railZStart = 19.320;
		double railZEnd = 30.655;
		double padZSpacingLong = 40.811;
		double padZSpacingShort = 30.110;
		
		G4Box epoxyMajorCuVol = (G4Box) createNamedVolume("epoxyMajorCu");
		G4Box epoxyMinorCuVol = (G4Box) createNamedVolume("epoxyMinorCu");
		G4Box epoxyMajorPkVol = (G4Box) createNamedVolume("epoxyMajorPk");
		G4Box epoxyMinorPkVol = (G4Box) createNamedVolume("epoxyMinorPk");
		
		epoxyMajorCuVol.setMother( mainVol );
		epoxyMinorCuVol.setMother( mainVol );
		epoxyMajorPkVol.setMother( mainVol );
		epoxyMinorPkVol.setMother( mainVol );
		
		epoxyMajorCuVol.setPosition(
				SVTConstants.MATERIALDIMENSIONS.get("carbonFiberCu")[0]/2.0*0.1 - epoxyMajorCuVol.getXHalfLength(),
				0.0,
				epoxyMajorCuVol.getZHalfLength() - mainVol.getZHalfLength() );
		
		epoxyMinorCuVol.setPosition(
				-(SVTConstants.MATERIALDIMENSIONS.get("carbonFiberCu")[0]/2.0*0.1 - epoxyMinorCuVol.getXHalfLength()),
				0.0,
				epoxyMinorCuVol.getZHalfLength() - mainVol.getZHalfLength() );
		
		epoxyMajorPkVol.setPosition( 
				SVTConstants.MATERIALDIMENSIONS.get("carbonFiberPk")[0]/2.0*0.1 - epoxyMajorPkVol.getXHalfLength(),
				0.0,
				epoxyMajorPkVol.getZHalfLength() - mainVol.getZHalfLength() + epoxyMajorCuVol.getZHalfLength()*2.0 );
		
		epoxyMinorPkVol.setPosition(
				-(SVTConstants.MATERIALDIMENSIONS.get("carbonFiberPk")[0]/2.0*0.1 - epoxyMinorPkVol.getXHalfLength()),
				0.0,
				epoxyMinorPkVol.getZHalfLength() - mainVol.getZHalfLength() + epoxyMinorCuVol.getZHalfLength()*2.0 );
		
		G4Box railVol = (G4Box) createNamedVolume("rail");
		railVol.setMother( mainVol );
		railVol.setPosition( 
				-railXStart*0.1, 
				railVol.getYHalfLength() - mainVol.getYHalfLength(), 
				railVol.getZHalfLength() - mainVol.getZHalfLength() );
		
		double[] padZSpacingFromEnd = new double[]{
				railZEnd,
				padZSpacingLong,
				padZSpacingLong,
				padZSpacingShort,
				padZSpacingLong,
				padZSpacingLong,
				padZSpacingShort,
				padZSpacingLong,
				padZSpacingLong,
				railZStart
			};
		
		for( int pad = 0; pad < SVTConstants.NPADS; pad++ )
		{
			G4Tubs padVol = (G4Tubs) createNamedVolume("pad");
			padVol.setMother( mainVol );
			padVol.setName("pad"+pad);
			padVol.rotate("xyz", -Math.PI/2, 0.0, 0.0 );
			
			double padPos = padZSpacingFromEnd[0];
			for( int p = 1; p < pad; p++ )
				padPos += padZSpacingFromEnd[p];
			//System.out.printf("padPos[%d]=% 8.3f\n", pad, padPos );
			
			padVol.setPosition( 
					-railXStart*0.1, 
					padVol.getZHalfLength() + railVol.getYHalfLength()*2.0 - mainVol.getYHalfLength(),
					mainVol.getZHalfLength() - padPos*0.1 );
		}
		
		return mainVol;
	}
	
	
	public void createDownstreamSupport(int aRegion, Geant4Basic regionVol)
	{
		/*
		 * GDML file needs to look like this
		 * 
			<polyhedra aunit="deg" deltaphi="360" lunit="mm" name="aBST_Downstream_SupportFrame_L1" numsides="10" startphi="0">
		      <zplane rmax="65.39" rmin="0" z="0"/>
		      <zplane rmax="65.39" rmin="0" z="14"/>
		    </polyhedra>
		    <tube aunit="deg" deltaphi="360" lunit="mm" name="bBST_Downstream_SupportHole_L1" rmax="62.39" rmin="0" startphi="0" z="28"/>
		    <subtraction name="downstreamSupport_L1">
		      <first ref="aBST_Downstream_SupportFrame_L1"/>
		      <second ref="bBST_Downstream_SupportHole_L1"/>
		      <position name="downstreamSupport_L1_pos" unit="mm" x="0" y="0" z="7"/>
		    </subtraction>
	    
	    */
		
		String pgonName = "peek_polyhedra" ;
		String tubeName = "peek_tube";
                   double[] zPlane = {-SVTConstants.REGIONPEEKLENGTH[aRegion]*0.1/2,SVTConstants.REGIONPEEKLENGTH[aRegion]*0.1/2};
		double[] rInner = {0.0, 0.0};
		double[] rOuter = {SVTConstants.REGIONPEEKRMAX[aRegion]*0.1,SVTConstants.REGIONPEEKRMAX[aRegion]*0.1};
		Geant4Basic polyhedra = new G4Pgon(pgonName, 0, Math.toRadians(360), SVTConstants.NSECTORS[aRegion], 2, zPlane, rInner, rOuter);
		Geant4Basic tube = new G4Tubs(tubeName, 0, SVTConstants.REGIONPEEKRMIN[aRegion]*0.1, SVTConstants.REGIONPEEKLENGTH[aRegion]/2*1.1*0.1, 0, 360 ); //add 10% more thickness for correct subtraction
		Geant4Basic op = new G4Operation("peek", "subtract", pgonName+"_r"+(aRegion+1), tubeName+"_r"+(aRegion+1));
//		tube.setMother(op);
//		polyhedra.setMother(op);
                   polyhedra.setMother(regionVol);
                   tube.setMother(regionVol);
                   op.setMother(regionVol);
                   op.setPosition(0, 0, (SVTConstants.SECTORLEN-SVTConstants.REGIONPEEKLENGTH[aRegion])/2.0*0.1);
	}
	
	
	/**
	 * Returns the mother volume to export the geometry to, for example, a GDML file.
	 * 
	 * @return Geant4Basic the mother volume
	 */
	public Geant4Basic getMotherVolume()
	{
		return motherVol;
	}
	
	
	/**
	 * Returns a value from the Properties HashMap.
	 * 
	 * @param aName name of a key
	 * @return String the value associated with the given key, or "none" if the key does not exist
	 */
	public String getProperty( String aName )
	{
		return properties.containsKey( aName ) ? properties.get( aName ) : "none";
	}
	
	
	/**
	 * Returns a value from the Parameters HashMap.
	 * 
	 * @param aName name of a key
	 * @return String the value associated with the given key, or "none" if the key does not exist.
	 */
	public String getParameter( String aName )
	{
		return parameters.containsKey( aName ) ? parameters.get( aName ) : "none";
	}
	
	
	/**
	 * Returns the "Parameters" HashMap. 
	 * Used by GEMC to interface with CCDB.
	 * 
	 * @return HashMap a HashMap containing named constants and core parameters.
	 */
	public HashMap<String, String> getParameters()
	{
		return parameters;
	}
	
	
	/**
	 * Appends a tag to the current volumes.
	 * Useful to avoid conflicts in a GDML file.
	 * 
	 * @param aTag something to add
	 */
	public void appendName( String aTag )
	{
		Util.appendName( motherVol, aTag );
	}
	
	
	/**
	 * Returns a list of all the volumes in the gemcString() format.
	 * 
	 * @return String multiple lines of text
	 */
	@Override
	public String toString()
	{
		return Util.gemcStringAll( motherVol );
	}
	
	
	/**
	 * Sets whether alignment shifts from CCDB should be applied to the geometry during generation.
	 * 
	 * @param b true/false
	 */
	public void setApplyAlignmentShifts( boolean b )
	{
		bShift = b;
	}
	
	
	/**
	 * Returns whether alignment shifts are applied.
	 * 
	 * @return boolean true/false
	 */
	public boolean isSetApplyAlignmentShifts()
	{
		return bShift;
	}
	
	
	/**
	 * Sets scale factors to amplify alignment shifts for visualisation purposes.
	 *  
	 * @param aScaleTranslation a scale factor for translation shifts
	 * @param aScaleRotation a scale factor for rotation shifts
	 */
	public void setAlignmentShiftScale( double aScaleTranslation, double aScaleRotation  )
	{
		scaleT = aScaleTranslation;
		scaleR = aScaleRotation;
	}
	
	
	/** 
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 * 
	 * @param aLayerMin an index starting from 1
	 * @param aLayerMax an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aLayerMin, int aLayerMax, int[] aSectorMin, int[] aSectorMax ) throws IllegalArgumentException
	{
		if( aLayerMin < 0 || aLayerMax > SVTConstants.NLAYERS ){ throw new IllegalArgumentException("layer out of bounds"); }
		if( aSectorMin.length != SVTConstants.NREGIONS || aSectorMax.length != SVTConstants.NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aLayerMin > aLayerMax ){ throw new IllegalArgumentException("invalid layer min/max"); }
		
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > SVTConstants.NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		
		// 0 means use default / previous value
		if( aLayerMin != 0 )
		{
			layerMin = aLayerMin;
			regionMin = SVTConstants.convertLayer2RegionModule( aLayerMin )[0]+1;
			moduleMin = SVTConstants.convertLayer2RegionModule( aLayerMin )[1]+1;
		}
		if( aLayerMax != 0 )
		{
			layerMax = aLayerMax;
			regionMax = SVTConstants.convertLayer2RegionModule( aLayerMax )[0]+1;
			moduleMax = SVTConstants.convertLayer2RegionModule( aLayerMax )[1]+1-1;
		}
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] != 0 ){ sectorMin[i] = aSectorMin[i]; }
			if( aSectorMax[i] != 0 ){ sectorMax[i] = aSectorMax[i]; }
		}
	}
	
	
	/** 
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 *  
	 * @param aRegionMin an index starting from 1
	 * @param aRegionMax an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @param aModuleMin an index starting from 1
	 * @param aModuleMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aRegionMin, int aRegionMax, int[] aSectorMin, int[] aSectorMax, int aModuleMin, int aModuleMax ) throws IllegalArgumentException
	{
		if( aRegionMin < 0 || aRegionMax > SVTConstants.NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin.length != SVTConstants.NREGIONS || aSectorMax.length != SVTConstants.NREGIONS ){ throw new IllegalArgumentException("invalid sector array"); }
		if( aRegionMin > aRegionMax ){ throw new IllegalArgumentException("invalid region min/max"); }
		
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] < 0 || aSectorMax[i] > SVTConstants.NSECTORS[i] )
				throw new IllegalArgumentException("sector out of bounds");
			if( aSectorMin[i] > aSectorMax[i] )
				throw new IllegalArgumentException("invalid sector min/max");
		}
		if( aModuleMin < 0 || aModuleMax > SVTConstants.NMODULES )
			throw new IllegalArgumentException("module out of bounds");
		
		// 0 means use default / previous value
		if( aRegionMin != 0 ){ regionMin = aRegionMin; }
		if( aRegionMax != 0 ){ regionMax = aRegionMax-1; }
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
		{
			if( aSectorMin[i] != 0 ){ sectorMin[i] = aSectorMin[i]; }
			if( aSectorMax[i] != 0 ){ sectorMax[i] = aSectorMax[i]; }
		}
		if( aModuleMin != 0 ){ moduleMin = aModuleMin; }
		if( aModuleMax != 0 ){ moduleMax = aModuleMax; }
		if( aRegionMin != 0 || aModuleMin != 0 ){ layerMin = SVTConstants.convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1; }
		if( aRegionMax != 0 || aModuleMax != 0 ){ layerMax = SVTConstants.convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
	}
	
	
	/**
	 * Sets the range of indices to cycle over when generating the geometry in makeVolumes().
	 * Enter 0 to use the previous/default value.
	 * 
	 * @param aRegion an index starting from 1
	 * @param aSectorMin an index starting from 1
	 * @param aSectorMax an index starting from 1
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public void setRange( int aRegion, int aSectorMin, int aSectorMax ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > SVTConstants.NREGIONS ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSectorMin < 0 || aSectorMin > SVTConstants.NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMin out of bounds"); }
		if( aSectorMax < 0 || aSectorMax > SVTConstants.NSECTORS[aRegion] ){ throw new IllegalArgumentException("sectorMax out of bounds"); }
		if( aSectorMin > aSectorMax ){ throw new IllegalArgumentException("invalid sector min/max"); }
		
		// 0 means use default / previous value
		if( aRegion != 0 ){ regionMin = aRegion; regionMax = aRegion; }
		if( aSectorMin != 0 ){ sectorMin[regionMin-1] = aSectorMin; }
		if( aSectorMax != 0 ){ sectorMax[regionMax-1] = aSectorMax; }
		if( aRegion != 0 ){ layerMin = SVTConstants.convertRegionModule2Layer( regionMin-1, moduleMin-1 )+1;
						    layerMax = SVTConstants.convertRegionModule2Layer( regionMax-1, moduleMax-1 )+1; }
	}
	
	
	/**
	 * Returns a string to display the current range of indices.
	 * 
	 * @return String a line of text
	 */
	public String showRange()
	{
		String range = ""; 
		range = range +"layer ["+layerMin+":"+layerMax+"]";
		range = range +" region ["+regionMin+":"+regionMax+"]";
		range = range +" module ["+moduleMin+":"+moduleMax+"]";
		range = range +" sector ";
		for( int i = 0; i < SVTConstants.NREGIONS; i++ )
			range = range +"["+sectorMin[i]+":"+sectorMax[i]+"]";
		return range;
	}
	
	
	/**
	 * Returns the first region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the region index
	 */
	public int getRegionMin()
	{
		return regionMin;
	}

	
	/**
	 * Returns the last region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NREGIONS
	 * 
	 * @return int an upper bound for the region index
	 */
	public int getRegionMax()
	{
		return regionMax;
	}

	
	/**
	 * Returns the first sector in each region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the sector index in each region
	 */
	public int[] getSectorMin()
	{
		return sectorMin;
	}

	
	/**
	 * Returns the last sector in each region to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NSECTORS[region]
	 * 
	 * @return int an upper bound for the sector index in each region
	 */
	public int[] getSectorMax()
	{
		return sectorMax;
	}

	
	/**
	 * Returns the first module in a sector to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the module index
	 */
	public int getModuleMin()
	{
		return moduleMin;
	}

	
	/**
	 * Returns the last module in a sector to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NMODULES
	 * 
	 * @return int an upper bound for the module index
	 */
	public int getModuleMax()
	{
		return moduleMax;
	}

	
	/**
	 * Returns the first layer to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: 1
	 * 
	 * @return int a lower bound for the layer index
	 */
	public int getLayerMin()
	{
		return layerMin;
	}

	
	/**
	 * Returns the last layer to be generated on makeVolumes().
	 * To use in a for loop, do {@code for( int i = min-1; i < max; i++)}
	 * Default: NLAYERS
	 * 
	 * @return int an upper bound for the layer index
	 */
	public int getLayerMax()
	{
		return layerMax;
	}
        
         
         public static void main(String[] args) {
              DatabaseConstantProvider cp = new DatabaseConstantProvider(11, "default");

              SVTConstants.connect(cp);

              SVTVolumeFactory factory = new SVTVolumeFactory( cp, false ); // ideal geometry
              factory.BUILDSENSORS = false; // include physical sensors (aka cards)
              factory.makeVolumes();

              System.out.println(factory.toString());
              
         }
    
}
