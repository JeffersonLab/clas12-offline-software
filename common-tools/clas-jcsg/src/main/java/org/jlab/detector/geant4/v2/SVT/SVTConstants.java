package org.jlab.detector.geant4.v2.SVT;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jlab.detector.calib.utils.DatabaseConstantProvider; // coatjava-3.0
import org.jlab.geom.base.ConstantProvider;

import eu.mihosoft.vrl.v3d.Transform;

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
 * @version 1.1.1
 */
public class SVTConstants
{
	private static String ccdbPath = "/geometry/cvt/svt/";
	private static boolean bLoadedConstants = false; // only load constants once
	
	// data for alignment shifts
	private static double[][] SECTORSHIFTDATA = null;
	private static String filenameSectorShiftData = null;
        private static double[][][] LAYERSHIFTDATA = null;
	
	//private static double[][] LAYERSHIFTDATA = null;
	//private static String filenameLayerShiftData = null;
	
	public static boolean VERBOSE = false;
	//public static String[] VARIATION = new String[]{ "ideal", "shifted" };
	
	// SVT GEOMETRY PARAMETERS
	// fundamentals
	public static int NREGIONS; // number of regions
	public static int[] NSECTORS; // number of sectors in a region
	public static int NFIDUCIALS; // number of survey fiducials on a sector module
	public static int NMODULES; // number of modules in a sector
	public static int NSENSORS; // number of sensors in a module
	public static int NSTRIPS; // number of strips in a layer
	public static int NPADS; // number of pads on High Voltage Rail
	public static double STRIPOFFSETWID; // offset of first intermediate sensor strip from edge of active zone
	public static double READOUTPITCH; // distance between start of strips along front of hybrid sensor
	public static double STEREOANGLE; // total angle swept by sensor strips
	public static int[] STATUS; // whether a region is used in Reconstruction
	//
        public static int[][] RSI;
	// position and orientation of layers
	public static double PHI0;
	public static double SECTOR0;
	public static double[] REFRADIUS; // outer side of U (inner) module
	public static double LAYERPOSFAC; // location of strip layer within sensor volume
	public static double[] Z0ACTIVE; // Cu edge of hybrid sensor's active volume
	// fiducials
	public static double[] SUPPORTRADIUS; // from MechEng drawings, to inner side of wide copper part
	public static double FIDCUX;
	public static double FIDPKX;
	public static double FIDORIGINZ;
	public static double FIDCUZ;
	public static double FIDPKZ0;
	public static double FIDPKZ1;
	//
	// dimensions of sensors
	public static double ACTIVESENWID;
	public static double PHYSSENWID;
	public static double DEADZNWID;
	//
	public static double SILICONTHK;
	//
	public static double PHYSSENLEN;
	public static double ACTIVESENLEN;
	public static double DEADZNLEN;
	public static double MICROGAPLEN; // spacing between sensors
	//
	// dimensions of passive materials
	public static int NMATERIALS;
	public static HashMap<String, String> MATERIALTYPES = new LinkedHashMap<>();
	public static HashMap<String, double[]> MATERIALDIMENSIONS = new LinkedHashMap<>();
	public static double PASSIVETHK;
	//
	// calculated on load()
	public static int NLAYERS; // total number of layers in a sector
	public static int NTOTALSECTORS; // total number of sectors for all regions
	public static int NTOTALFIDUCIALS; // total number of fiducials for all sectors and regions
	public static double[][] LAYERRADIUS; // radius to strip planes
	public static double LAYERGAPTHK; // distance between pairs of layers within a sector
	public static double MODULELEN;    // || DZ |  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  || DZ ||
	public static double STRIPLENMAX;  //      ||  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  ||
	public static double MODULEWID; // || DZ | AZ | DZ ||
	public static double SECTORLEN;
	
	/**
	 * Loads the the necessary tables for the SVT geometry for a given DatabaseConstantProvider.
	 * 
	 * @return DatabaseConstantProvider the same thing
	 */
	public static DatabaseConstantProvider connect( DatabaseConstantProvider cp )
	{
		cp.loadTable( ccdbPath +"svt");
		cp.loadTable( ccdbPath +"region");
		cp.loadTable( ccdbPath +"support");
		cp.loadTable( ccdbPath +"fiducial");
		cp.loadTable( ccdbPath +"material/box");
		cp.loadTable( ccdbPath +"material/tube");
		cp.loadTable( ccdbPath +"alignment");
		cp.loadTable( ccdbPath +"layeralignment");
                //shift by target
                cp.loadTable("/geometry/target");
                
		//if( loadAlignmentTables ) cp.loadTable( ccdbPath +"alignment/sector"); // possible future tables
		//if( loadAlignmentTables ) cp.loadTable( ccdbPath +"alignment/layer");
		
		load( cp );
		return cp;
	}
	
	
	/**
	 * Returns the path to the database directory that contains the core parameters and constants for the SVT.<br>
	 * To use with DatabaseConstantProvider:<ul>
	 * <li>Access tables with {@code getCcdbPath() +"table"}.</li>
	 * <li>Access constants with {@code getCcdbPath() +"table/constant"}.</li></ul>
	 * 
	 * @return String a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
	public static String getCcdbPath()
	{
		return ccdbPath;
	}
	
	
	/**
	 * Sets the path to the database directory that contains the core parameters and constants for the SVT.<br>
	 * 
	 * @param aPath a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
	public static void setCcdbPath( String aPath )
	{
		ccdbPath = aPath;
	}
	
	
	/**
	 * Reads all the necessary constants from CCDB into static variables.
	 * Please use a DatabaseConstantProvider to access CCDB and load the following tables:
	 * svt, region, support, fiducial, material, alignment.
	 *  
	 * @param cp a ConstantProvider that has loaded the necessary tables
	 */
	public static synchronized void load( ConstantProvider cp )
	{
		if( !bLoadedConstants )
		{			
			// read constants from svt table
			NREGIONS = cp.getInteger( ccdbPath+"svt/nRegions", 0 );
			NMODULES = cp.getInteger( ccdbPath+"svt/nModules", 0 );
			NSENSORS = cp.getInteger( ccdbPath+"svt/nSensors", 0 );
			NSTRIPS = cp.getInteger( ccdbPath+"svt/nStrips", 0 );
			NFIDUCIALS = cp.getInteger( ccdbPath+"svt/nFiducials", 0 );
			NPADS = cp.getInteger( ccdbPath+"svt/nPads", 0 );
			
			READOUTPITCH = cp.getDouble( ccdbPath+"svt/readoutPitch", 0 );
			STEREOANGLE = Math.toRadians(cp.getDouble( ccdbPath+"svt/stereoAngle", 0 ));
			PHI0 = Math.toRadians(cp.getDouble( ccdbPath+"svt/phiStart", 0 ));
			SECTOR0 = Math.toRadians(cp.getDouble( ccdbPath+"svt/zRotationStart", 0 ));
			LAYERPOSFAC = cp.getDouble( ccdbPath+"svt/modulePosFac", 0 );
			
			SILICONTHK = cp.getDouble( ccdbPath+"svt/siliconThk", 0 );
			PHYSSENLEN = cp.getDouble( ccdbPath+"svt/physSenLen", 0 );
			PHYSSENWID = cp.getDouble( ccdbPath+"svt/physSenWid", 0 );
			ACTIVESENLEN = cp.getDouble( ccdbPath+"svt/activeSenLen", 0 );
			ACTIVESENWID = cp.getDouble( ccdbPath+"svt/activeSenWid", 0 );
			DEADZNLEN = cp.getDouble( ccdbPath+"svt/deadZnLen", 0 );
			DEADZNWID = cp.getDouble( ccdbPath+"svt/deadZnWid", 0 );
			MICROGAPLEN = cp.getDouble( ccdbPath+"svt/microGapLen", 0 );
			
			FIDCUX = cp.getDouble( ccdbPath+"fiducial/CuX", 0 );
			FIDPKX = cp.getDouble( ccdbPath+"fiducial/PkX", 0 );
			FIDORIGINZ = cp.getDouble( ccdbPath+"fiducial/OriginZ", 0 );
			FIDCUZ = cp.getDouble( ccdbPath+"fiducial/CuZ", 0 );
			FIDPKZ0 = cp.getDouble( ccdbPath+"fiducial/PkZ0", 0 );
			FIDPKZ1 = cp.getDouble( ccdbPath+"fiducial/PkZ1", 0 );
			
			// read constants from materials table
			NMATERIALS = 14; // number of unique materials, not length of materialNames
					
			// cannot read String variables from CCDB, so put them here in the correct order
			MATERIALTYPES.put("heatSink", 			"box");
			MATERIALTYPES.put("heatSinkCu", 		"box");
			MATERIALTYPES.put("heatSinkRidge", 		"box");
			MATERIALTYPES.put("rohacell", 			"box");
			MATERIALTYPES.put("rohacellCu",			"box");
			MATERIALTYPES.put("plastic", 			"box");
			MATERIALTYPES.put("plasticPk", 			"box");
			MATERIALTYPES.put("carbonFiber", 		"box");
			MATERIALTYPES.put("carbonFiberCu", 		"box");
			MATERIALTYPES.put("carbonFiberPk", 		"box");
			MATERIALTYPES.put("busCable", 			"box");
			MATERIALTYPES.put("busCableCu", 		"box");
			MATERIALTYPES.put("busCablePk", 		"box");
			MATERIALTYPES.put("pitchAdaptor", 		"box");
			MATERIALTYPES.put("pcBoardAndChips", 	"box");
			MATERIALTYPES.put("pcBoard", 			"box");
			MATERIALTYPES.put("chip", 				"box");
			MATERIALTYPES.put("epoxyAndRailAndPads","box");
			MATERIALTYPES.put("epoxyMajorCu",       "box");
			MATERIALTYPES.put("epoxyMinorCu",		"box");
			MATERIALTYPES.put("epoxyMajorPk",		"box");
			MATERIALTYPES.put("epoxyMinorPk",		"box");
			MATERIALTYPES.put("rail",				"box");
			MATERIALTYPES.put("wirebond",			"box");
			MATERIALTYPES.put("kaptonWrapTapeSide", "box");
			MATERIALTYPES.put("kaptonWrapTapeCap", 	"box");
			MATERIALTYPES.put("kaptonWrapGlueSide",	"box");
			MATERIALTYPES.put("kaptonWrapGlueCap", 	"box");
			
			MATERIALTYPES.put("pad", "tube");
			
			int boxNum = 0; // number of box types
			int mat = 0;
			for( Entry<String, String> entry : MATERIALTYPES.entrySet() )
			{
				String key = entry.getKey();
				String value = entry.getValue();
				double[] dimensions = null;
				
				if( value == "box" )
				{
					boxNum++;
					dimensions = new double[]{
							cp.getDouble( ccdbPath+"material/box/wid", mat ),
							cp.getDouble( ccdbPath+"material/box/thk", mat ),
							cp.getDouble( ccdbPath+"material/box/len", mat )
							};
				}
				else if( value == "tube" ) // offset by boxNum to reset row for CCDB table
				{
					dimensions = new double[]{
							cp.getDouble( ccdbPath+"material/tube/rmin", mat - boxNum ),
							cp.getDouble( ccdbPath+"material/tube/rmax", mat - boxNum ),
							cp.getDouble( ccdbPath+"material/tube/zlen", mat - boxNum ),
							cp.getDouble( ccdbPath+"material/tube/phi0", mat - boxNum ),
							cp.getDouble( ccdbPath+"material/tube/dphi", mat - boxNum )
							};
				}
				MATERIALDIMENSIONS.put( key, dimensions );
				mat++;
			}
			
			
			// calculate derived constants
			NLAYERS = NMODULES*NREGIONS;
			MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
			STRIPLENMAX = MODULELEN - 2*DEADZNLEN;
			MODULEWID = ACTIVESENWID + 2*DEADZNWID;
			STRIPOFFSETWID = cp.getDouble(ccdbPath+"svt/stripStart", 0 );
			LAYERGAPTHK = cp.getDouble(ccdbPath+"svt/layerGapThk", 0 );
			PASSIVETHK = MATERIALDIMENSIONS.get("carbonFiber")[1] + MATERIALDIMENSIONS.get("busCable")[1] + MATERIALDIMENSIONS.get("epoxyAndRailAndPads")[1];
			SECTORLEN = MATERIALDIMENSIONS.get("heatSink")[2] + MATERIALDIMENSIONS.get("rohacell")[2];
			double layerGapThk = MATERIALDIMENSIONS.get("rohacell")[1] + 2*PASSIVETHK; // construct from material thicknesses instead
			
			
			if( VERBOSE ) System.out.printf("LAYERGAPTHK (CCDB)      = % 8.3f\n", LAYERGAPTHK );
			if( VERBOSE ) System.out.printf("layerGapThk (MATERIALS) = % 8.3f\n", layerGapThk );
			LAYERGAPTHK = layerGapThk; if( VERBOSE ) System.out.println("set LAYERGAPTHK to layerGapThk");
			
			if( VERBOSE )
			{
				System.out.printf("NREGIONS        %4d\n", NREGIONS );
				System.out.printf("NMODULES        %4d\n", NMODULES );
				System.out.printf("NLAYERS         %4d\n", NLAYERS );
				System.out.printf("NSENSORS        %4d\n", NSENSORS );
				System.out.printf("NSTRIPS         %4d\n", NSTRIPS );
				System.out.printf("NFIDUCIALS      %4d\n", NFIDUCIALS );
				System.out.printf("NPADS           %4d\n", NPADS );
				System.out.println();
			}
			
			// read constants from region and support table
			NSECTORS = new int[NREGIONS];
			STATUS = new int[NREGIONS];
			Z0ACTIVE = new double[NREGIONS];
			REFRADIUS = new double[NREGIONS]; // used to derive LAYERRADIUS
			SUPPORTRADIUS = new double[NREGIONS]; // used to build volumes
			LAYERRADIUS = new double[NREGIONS][NMODULES]; // used to build strips
			
			// Consider Region 1 Sector 6 (not to scale)
			//
			// y (vertical)                                    .------------------^-------------
			// ^         			   						   | V (outer)		  |
			// |                                               | sensor layer	  | 0.32 silicon thickness
			// |                                               |				  |
			// |                                      .--------+--------------^---v------------- module radius 1
			// |                                      | passiveThk 			  |
			// |------^-----------------^-------------+-----------------------|----------------- fiducial layer
			// |      |   				|		|							  |
			// |	  |		heatSink	|		|							  |
			// |      |   				| 2.50	|		rohacell			  | 3.236 layer gap
			// |	  |	2.88			|		|							  |
			// |      |         	    |		|							  |
			// |      |              +--v-------------+-----------------------|------------------ module radius 0
			// |  	  |				 |                | passiveThk			  |
			// |      |				 |				  '---^----+--------------v-----^------------ radius CCDB
			// |------v-------^------'					  |	   | 				    |						radius MechEng
			// |              |                           |    | U (inner)			| 0.32 silicon thickness
			// |              |                           |    | sensor layer		|
			// |              |                           |    '--------------------v-----------
			// |			  | support					  | reference 
			// |			  | radius					  | radius
			// |			  | 						  |
			// o==============v===========================v===================================-> z (beamline)
			System.out.println("SVT READ Z SHIFT VALUE "+cp.getDouble("/geometry/target/position", 0));
			// LAYERRADIUS and ZSTARTACTIVE are used primarily by the Reconstruction and getStrip()
			for( int region = 0; region < NREGIONS; region++ )
			{
				NSECTORS[region] = cp.getInteger(ccdbPath+"region/nSectors", region );
                                
				STATUS[region] = cp.getInteger(ccdbPath+"region/status", region );
				Z0ACTIVE[region] = cp.getDouble(ccdbPath+"region/zStart", region ); // Cu edge of hybrid sensor's active volume
				REFRADIUS[region] = cp.getDouble(ccdbPath+"region/UlayerOuterRadius", region); // radius to outer side of U (inner) module
				SUPPORTRADIUS[region] = cp.getDouble(ccdbPath+"region/CuSupportInnerRadius", region); // radius to inner side of heatSinkRidge
				
				for( int m = 0; m < NMODULES; m++ )
				{
					switch( m ) 
					{
					case 0: // U = lower / inner
						LAYERRADIUS[region][m] = REFRADIUS[region] - LAYERPOSFAC*SILICONTHK;
						break;
					case 1: // V = upper / outer
						LAYERRADIUS[region][m] = REFRADIUS[region] + LAYERGAPTHK + LAYERPOSFAC*SILICONTHK;
						break;
					}
					//System.out.println("LAYERRADIUS "+ LAYERRADIUS[region][m]);
				}
			}
                        
                        NTOTALSECTORS = convertRegionSector2Index( NREGIONS-1, NSECTORS[NREGIONS-1]-1 )+1;
			NTOTALFIDUCIALS = convertRegionSectorFiducial2Index(NREGIONS-1, NSECTORS[NREGIONS-1]-1, NFIDUCIALS-1  )+1;
			
                        RSI = new int[NREGIONS][NTOTALSECTORS];
                        for( int aRegion = 0; aRegion < NREGIONS; aRegion++ )
			{
                            for( int aSector = 0; aSector < NSECTORS[aRegion]; aSector++ )
                            {
                                RSI[aRegion][aSector] = convertRegionSector2Index( aRegion, aSector );
                                System.out.println(" a Region "+aRegion +" aSector "+aSector+" RSI "+RSI[aRegion][aSector] );
                            }
                        }
			System.out.println("Reading alignment shifts from database");
		
                        SECTORSHIFTDATA = new double[NTOTALSECTORS][];
                        
                        for( int i = 0; i < NTOTALSECTORS; i++ )
                        {
                                double tx = cp.getDouble(ccdbPath+"alignment/tx", i );
                                double ty = cp.getDouble(ccdbPath+"alignment/ty", i );
                                double tz = cp.getDouble(ccdbPath+"alignment/tz", i );
                                double rx = cp.getDouble(ccdbPath+"alignment/rx", i );
                                double ry = cp.getDouble(ccdbPath+"alignment/ry", i );
                                double rz = cp.getDouble(ccdbPath+"alignment/rz", i );
                                double ra = cp.getDouble(ccdbPath+"alignment/ra", i );

                                SECTORSHIFTDATA[i] = new double[]{ tx, ty, tz, rx, ry, rz, Math.toRadians(ra) };

                        }
                        
                        LAYERSHIFTDATA = new double[NSECTORS[3]][NLAYERS][];
                        for( int i = 0; i < (NTOTALSECTORS-NSECTORS[3])*2; i++ )    // layeralignment tables doesn't cover region 4
                        {
                                int sector = cp.getInteger(ccdbPath+"layeralignment/sector", i );
                                int layer  = cp.getInteger(ccdbPath+"layeralignment/layer", i );
                                double tx  = cp.getDouble(ccdbPath+"layeralignment/deltaX", i );
                                double ty  = cp.getDouble(ccdbPath+"layeralignment/deltaY", i );
                                double tz  = cp.getDouble(ccdbPath+"layeralignment/deltaZ", i );
                                double rx  = cp.getDouble(ccdbPath+"layeralignment/rotX", i );
                                double ry  = cp.getDouble(ccdbPath+"layeralignment/rotY", i );
                                double rz  = cp.getDouble(ccdbPath+"layeralignment/rotZ", i );
                                double ra  = cp.getDouble(ccdbPath+"layeralignment/rotA", i );
                                LAYERSHIFTDATA[sector-1][layer-1] = new double[]{ tx, ty, tz, rx, ry, rz, ra };
                        }
                        
                        
			if( VERBOSE )
			{
				System.out.println("NSECTORS STATUS Z0ACTIVE REFRADIUS SUPPORTRADIUS LAYERRADIUS (U,V)");
				for(int r = 0; r < NREGIONS; r++ )
				{
					System.out.printf("%6s%2d","", NSECTORS[r] );
					System.out.printf("%6s%1d","", STATUS[r] );
					System.out.printf("%1s%8.3f","", Z0ACTIVE[r] );
					System.out.printf("%2s%8.3f","", REFRADIUS[r] );
					System.out.printf("%6s%8.3f","", SUPPORTRADIUS[r] );
					System.out.printf("%1s%8.3f %8.3f","", LAYERRADIUS[r][0], LAYERRADIUS[r][1] );
					System.out.println();
				}
			}
			
			
                         
			// check one constant from each table
			//if( NREGIONS == 0 || NSECTORS[0] == 0 || FIDCUX == 0 || MATERIALS[0][0] == 0 || SUPPORTRADIUS[0] == 0 )
				//throw new NullPointerException("please load the following tables from CCDB in "+ccdbPath+"\n svt\n region\n support\n fiducial\n material\n");
			
			bLoadedConstants = true;
			
			if( VERBOSE )
			{
				System.out.println();
				System.out.printf("NTOTALSECTORS   %4d\n", NTOTALSECTORS );
				System.out.printf("NTOTALFIDUCIALS %4d\n", NTOTALFIDUCIALS );
				System.out.printf("PHI0            %8.3f\n", Math.toDegrees(PHI0) );
				System.out.printf("SECTOR0         %8.3f\n", Math.toDegrees(SECTOR0) );
				System.out.printf("STEREOANGLE     %8.3f\n", Math.toDegrees(STEREOANGLE) );
				System.out.printf("READOUTPITCH    %8.3f\n", READOUTPITCH );
				System.out.printf("STRIPOFFSETWID  %8.3f\n", STRIPOFFSETWID );
				System.out.printf("STRIPLENMAX     %8.3f\n", STRIPLENMAX );
				System.out.printf("LAYERPOSFAC     %8.3f\n", LAYERPOSFAC );
				System.out.printf("PHYSSENLEN      %8.3f\n", PHYSSENLEN );
				System.out.printf("SILICONTHK      %8.3f\n", SILICONTHK );
				System.out.printf("PHYSSENWID      %8.3f\n", PHYSSENWID );
				System.out.printf("ACTIVESENLEN    %8.3f\n", ACTIVESENLEN );
				System.out.printf("ACTIVESENWID    %8.3f\n", ACTIVESENWID );
				System.out.printf("DEADZNLEN       %8.3f\n", DEADZNLEN );
				System.out.printf("DEADZNWID       %8.3f\n", DEADZNWID );
				System.out.printf("MICROGAPLEN     %8.3f\n", MICROGAPLEN );
				System.out.printf("MODULEWID       %8.3f\n", MODULEWID );
				System.out.printf("MODULELEN       %8.3f\n", MODULELEN );
				System.out.printf("LAYERGAPTHK     %8.3f\n", LAYERGAPTHK );
				System.out.printf("PASSIVETHK      %8.3f\n", PASSIVETHK );
				System.out.printf("SECTORLEN       %8.3f\n", SECTORLEN );
				System.out.printf("FIDCUX          %8.3f\n", FIDCUX );
				System.out.printf("FIDPKX          %8.3f\n", FIDPKX );
				System.out.printf("FIDORIGINZ      %8.3f\n", FIDORIGINZ );
				System.out.printf("FIDCUZ          %8.3f\n", FIDCUZ );
				System.out.printf("FIDPKZ0         %8.3f\n", FIDPKZ0 );
				System.out.printf("FIDPKZ1         %8.3f\n", FIDPKZ1 );
				
				double fidXDist = 2*SVTConstants.FIDCUX;
				double fidZDist = SVTConstants.FIDCUZ + SVTConstants.FIDPKZ0 + SVTConstants.FIDPKZ1;
				double fidZDist0 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTConstants.FIDCUX + SVTConstants.FIDPKX, 2) );
				double fidZDist1 = Math.sqrt( Math.pow(fidZDist,2) + Math.pow(SVTConstants.FIDCUX - SVTConstants.FIDPKX, 2) );
				
				System.out.printf("fidXDist  %8.3f\n", fidXDist );
				System.out.printf("fidZDist  %8.3f\n", fidZDist );
				System.out.printf("fidZDist0 %8.3f\n", fidZDist0 );
				System.out.printf("fidZDist1 %8.3f\n", fidZDist1 );
				
				int maxStrLenName = 32;
				int maxStrLenType = 5;
				for( Map.Entry<String, String> entryType : MATERIALTYPES.entrySet() )
				{
					String name = entryType.getKey();
					String type = entryType.getValue();
					double[] dimensions = MATERIALDIMENSIONS.get( name );
					String fmt = "%s %"+(maxStrLenName - name.length())+"s";
					System.out.printf(fmt, name, "" );
					fmt = "%s %"+(maxStrLenType - type.length())+"s";
					System.out.printf(fmt, type, "" );
					for( int i = 0; i < dimensions.length; i++ )
						System.out.printf("%8.3f ", dimensions[i] );
					System.out.println();
				}
			}
                       
		}
	}
	
	
	/**
	 * Reads alignment data from CCDB.
	 * 
	 * @param cp a DatabaseConstantProvider that has loaded the "alignment" table
	 */
	public static synchronized void loadAlignmentShifts( ConstantProvider cp )
	{
		System.out.println("reading alignment shifts from database");
		
		SECTORSHIFTDATA = new double[NTOTALSECTORS][];
		//LAYERSHIFTDATA = new double[NTOTALSECTORS*NMODULES][];
		
		for( int i = 0; i < NTOTALSECTORS; i++ )
		{
			double tx = cp.getDouble(ccdbPath+"alignment/tx", i );
			double ty = cp.getDouble(ccdbPath+"alignment/ty", i );
			double tz = cp.getDouble(ccdbPath+"alignment/tz", i );
			double rx = cp.getDouble(ccdbPath+"alignment/rx", i );
			double ry = cp.getDouble(ccdbPath+"alignment/ry", i );
			double rz = cp.getDouble(ccdbPath+"alignment/rz", i );
			double ra = cp.getDouble(ccdbPath+"alignment/ra", i );
			
			SECTORSHIFTDATA[i] = new double[]{ tx, ty, tz, rx, ry, rz, Math.toRadians(ra) };
			
			/*double stx = cp.getDouble(ccdbPath+"alignment/sector/tx", i );
			double sty = cp.getDouble(ccdbPath+"alignment/sector/ty", i );
			double stz = cp.getDouble(ccdbPath+"alignment/sector/tz", i );
			double srx = cp.getDouble(ccdbPath+"alignment/sector/rx", i );
			double sry = cp.getDouble(ccdbPath+"alignment/sector/ry", i );
			double srz = cp.getDouble(ccdbPath+"alignment/sector/rz", i );
			double sra = cp.getDouble(ccdbPath+"alignment/sector/ra", i );
			
			SECTORSHIFTDATA[i] = new double[]{ tx, ty, tz, rx, ry, rz, Math.toRadians(ra) };
			
			for( int j = 0; j < NMODULES; j++ )
			{
				double ltx = cp.getDouble(ccdbPath+"alignment/layer/tx", i );
				double lty = cp.getDouble(ccdbPath+"alignment/layer/ty", i );
				double ltz = cp.getDouble(ccdbPath+"alignment/layer/tz", i );
				double lrx = cp.getDouble(ccdbPath+"alignment/layer/rx", i );
				double lry = cp.getDouble(ccdbPath+"alignment/layer/ry", i );
				double lrz = cp.getDouble(ccdbPath+"alignment/layer/rz", i );
				double lra = cp.getDouble(ccdbPath+"alignment/layer/ra", i );
				
				SECTORSHIFTDATA[i] = new double[]{ ltx, lty, ltz, lrx, lry, lrz, Math.toRadians(lra) };
			}*/
		}
		//if( VERBOSE ) 
                    showSectorShiftData();
	}
	
	
	/**
	 * Reads alignment data for sectors from the given file.
	 * The translation and axis-angle rotation data should be of the form { tx, ty, tz, rx, ry, rz, ra }.
	 * 
	 * @param aFilename a filename
	 */
	public static void loadAlignmentSectorShifts( String aFilename )
	{
		filenameSectorShiftData = aFilename;
		
		try
		{
			SECTORSHIFTDATA = Util.inputTaggedData( filenameSectorShiftData, AlignmentFactory.NSHIFTDATARECLEN ); // 3 translation(x,y,z), 4 rotation(x,y,z,a)
		}
		catch( Exception e )
		{ 
			e.printStackTrace();
			System.exit(-1); // trigger fatal error
		}
		
		if( SECTORSHIFTDATA == null )
		{
			System.err.println("stop: SHIFTDATA is null after reading file \""+filenameSectorShiftData+"\"");
			System.exit(-1); 
		}
		
		for( int k = 0; k < NTOTALSECTORS; k++ )
		{
			SECTORSHIFTDATA[k][6] = Math.toRadians(SECTORSHIFTDATA[k][6]); // convert shift angle to radians 
		}
		
		if( VERBOSE ) showSectorShiftData();
	}
	
	
	/*
	 * Reads alignment data for layers from the given file.
	 * The translation and axis-angle rotation data should be of the form { tx, ty, tz, rx, ry, rz, ra }.
	 * 
	 * @param aFilename a filename
	 */
	/*public static void loadLayerAlignmentShifts( String aFilename )
	{
		filenameLayerShiftData = aFilename;
		
		try
		{
			LAYERSHIFTDATA = Util.inputTaggedData( filenameLayerShiftData, AlignmentFactory.NSHIFTDATARECLEN ); // 3 translation(x,y,z), 4 rotation(x,y,z,a)
		}
		catch( Exception e )
		{ 
			e.printStackTrace();
			System.exit(-1); // trigger fatal error
		}
		
		if( LAYERSHIFTDATA == null )
		{
			System.err.println("stop: SHIFTDATA is null after reading file \""+filenameLayerShiftData+"\"");
			System.exit(-1); 
		}
		
		for( int k = 0; k < NTOTALSECTORS; k++ )
		{
			LAYERSHIFTDATA[k][6] = Math.toRadians(LAYERSHIFTDATA[k][6]); // convert shift angle to radians 
		}
		
		if( VERBOSE ) showLayerShiftData();
	}*/
	
	
	/**
	 * Prints alignment shift data for sectors to screen.
	 */
	public static void showSectorShiftData()
	{
		//System.out.printf("i%8stx%7sty%7stz%7srx%7sry%7srz%7sra\n","","","","","","","");
		System.out.printf(" i%9stranslation(x,y,z)%19srotation(x,y,z,a)\n","","");
		for( int i = 0; i < NTOTALSECTORS; i++ )
		{
			System.out.printf("%2d", i+1 );
			for( int d = 0; d < AlignmentFactory.NSHIFTDATARECLEN-1; d++ )
				System.out.printf(" %8.3f", SECTORSHIFTDATA[i][d] );
			System.out.printf(" %8.3f", Math.toDegrees(SECTORSHIFTDATA[i][AlignmentFactory.NSHIFTDATARECLEN-1]) );
			System.out.println();
		}
	}
	
	
	/*
	 * Prints alignment shift data for layers to screen.
	 */
	/*public static void showLayersShiftData()
	{
		//System.out.printf("i%8stx%7sty%7stz%7srx%7sry%7srz%7sra\n","","","","","","","");
		System.out.printf(" i%9stranslation(x,y,z)%19srotation(x,y,z,a)\n","","");
		for( int i = 0; i < NTOTALSECTORS; i++ )
		{
			for( int j = 0; j < NMODULES; j++ )
			{
				System.out.printf("%2d", i+1 );
				for( int d = 0; d < AlignmentFactory.NSHIFTDATARECLEN-1; d++ )
					System.out.printf(" %8.3f", LAYERSHIFTDATA[i][d] );
				System.out.printf(" %8.3f", Math.toDegrees(LAYERSHIFTDATA[i][AlignmentFactory.NSHIFTDATARECLEN-1]) );
				System.out.println();
			}
		}
	}*/
	
	
	/**
	 * Converts RSF indices to linear index.
	 * Useful for writing data files.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aFiducial an index starting from 0
	 * @return int an index used for fiducial survey data
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionSectorFiducial2Index( int aRegion, int aSector, int aFiducial ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		if( aFiducial < 0 || aFiducial > NREGIONS-1 ){ throw new IllegalArgumentException("fiducial out of bounds"); }
		return convertRegionSector2Index( aRegion, aSector )*NFIDUCIALS + aFiducial;
	}
	
	
	/**
	 * Converts linear index to Region, Sector, and Fiducial indices.
	 * For use with data files.
	 * 
	 * @param aSurveyIndex an index used for fiducial survey data
	 * @return int[] an array containing RSF indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertIndex2RegionSectorFiducial( int aSurveyIndex ) throws IllegalArgumentException
	{
		if( aSurveyIndex < 0 || aSurveyIndex > NTOTALSECTORS*NFIDUCIALS-1 ){ throw new IllegalArgumentException("survey index out of bounds"); }
		int region = -1, sector = -1, fiducial = -1;
		for( int i = 0; i < NREGIONS; i++ )
		{
			int l0 = Util.subArraySum( NSECTORS, i   )*NFIDUCIALS;
			int l1 = Util.subArraySum( NSECTORS, i+1 )*NFIDUCIALS;
			if( i == NREGIONS-1 ){ l1 = l0 + NSECTORS[NREGIONS-1]*NFIDUCIALS; }
			if( l0 <= aSurveyIndex && aSurveyIndex <= l1-1 ){ region = i; break; }
		}		
		sector = aSurveyIndex / NFIDUCIALS - Util.subArraySum( NSECTORS, region );
		fiducial = aSurveyIndex % NFIDUCIALS;
		return new int[]{ region, sector, fiducial };
	}
	
	
	/**
	 * Converts RS indices to linear index.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return int an index used for sector modules
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionSector2Index( int aRegion, int aSector ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		return Util.subArraySum( NSECTORS, aRegion ) + aSector;
                
	}
	
	
	/**
	 * Converts linear index to Region and Sector indices.
	 * For use with data files.
	 * 
	 * @param aSvtIndex an index starting from 0
	 * @return int[] an array containing RS indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertIndex2RegionSector( int aSvtIndex )
	{
		if( aSvtIndex < 0 || aSvtIndex > NTOTALSECTORS-1 ){ throw new IllegalArgumentException("svt index out of bounds"); }
		
		int region = -1, sector = -1;
		for( int i = 0; i < NREGIONS; i++ )
		{
			int l0 = Util.subArraySum( NSECTORS, i   );
			int l1 = Util.subArraySum( NSECTORS, i+1 );
			if( i == NREGIONS-1 ){ l1 = l0 + NSECTORS[NREGIONS-1]; }
			if( l0 <= aSvtIndex && aSvtIndex <= l1-1 ){ region = i; break; }
		}
		sector = aSvtIndex - Util.subArraySum( NSECTORS, region );
		return new int[]{ region, sector };
	}
	
	
	/**
	 * Converts Layer index to Region, Module indices.
	 * 
	 * @param aLayer an index starting from 0
	 * @return int[] an array containing RM indices
	 * @throws IllegalArgumentException index out of bounds
	 */
	public static int[] convertLayer2RegionModule( int aLayer ) throws IllegalArgumentException // l=[0:7], NMODULES = 2
	{
		if( aLayer < 0 || aLayer > NLAYERS-1 ){ throw new IllegalArgumentException("layer out of bounds"); }
		return new int[]{ aLayer/NMODULES, aLayer%NMODULES }; // r=[0:3], m=[0:1]
	}
	
	
	/**
	 * Converts Region, Module indices to Layer index.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aModule an index starting from 0
	 * @return int layer index starting from 0
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static int convertRegionModule2Layer( int aRegion, int aModule ) throws IllegalArgumentException // U/inner(m=0) V/outer(m=1) 
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aModule < 0 || aModule > NMODULES-1 ){ throw new IllegalArgumentException("module out of bounds"); }
		return aRegion*NMODULES + aModule; // zero-based indices
	}
	
	
	/**
	 * Returns the azimuth angle from the x-axis for a given sector module.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @return double an angle in radians
	 */
	public static double getPhi( int aRegion, int aSector )
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		return -2.0*Math.PI/NSECTORS[aRegion]*aSector + PHI0;
	}
	
	
	/**
	 * Returns a transformation from the local frame to the lab frame, for a given sector module.
	 * 
	 * @param aRegion an index starting from 0
	 * @param aSector an index starting from 0
	 * @param aRadius transverse distance from beamline
	 * @param aZ longitudinal distance along beamline
	 * @return Transformation3D a sequence of transformations
	 * @throws IllegalArgumentException indices out of bounds
	 */
	public static Transform getLabFrame( int aRegion, int aSector, double aRadius, double aZ ) throws IllegalArgumentException
	{
		if( aRegion < 0 || aRegion > NREGIONS-1 ){ throw new IllegalArgumentException("region out of bounds"); }
		if( aSector < 0 || aSector > NSECTORS[aRegion]-1 ){ throw new IllegalArgumentException("sector out of bounds"); }
		
		// step 1: create point in XZ plane
		// step 2: rotate 90 deg
		// step 3: shift along x axis by radius
		// step 4: rotate to correct sector about target
		//
		//								y
		//				  4 			^
		//			   **.^ 			|
		//			**_./   			|
		//		 **_./ 		    		|
		//		  /      				|
		//		 ' 						|
		//	   3 <--------------------- 2 <-._
		//	   *						*     '. 
		//	   *					    *       \
		//	   *				        *       '
		// x <-*-------------------***********--1--------------------
		//	   *						*   
		//	   *						*
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		//								|
		
		double phi = getPhi( aRegion, aSector );
		Transform labFrame = new Transform();
		labFrame.rotZ( -phi ).translate( aRadius, 0, aZ ).rotZ( -SECTOR0 ); // change of sign for active/alibi -> passive/alias rotation
		return labFrame;
	}
	
	
	/**
	 * Returns a transformation for the strip frame to the local frame. 
	 * 
	 * @param aFlip whether the transformation should append a rotation of 180 deg about the z axis
	 * @return Transformation3D a sequence of transformations
	 */
	public static Transform getStripFrame( boolean aFlip )
	{
		Transform stripFrame = new Transform();
		if( aFlip ) { stripFrame.rotZ( Math.PI ); } // flip for U layer
		stripFrame.translate( -SVTConstants.ACTIVESENWID/2, 0, 0 ); // move to centre along x
		return stripFrame;
	}
	
	
	/**
	 * Returns the sector alignment data.
	 * 
	 * @return double[][] an array of translations and axis-angle rotations of the form { tx, ty, tz, rx, ry, rz, ra }
	 */
	public static double[][] getDataAlignmentSectorShift()
	{
		if( SECTORSHIFTDATA == null ){ System.err.println("error: SVTConstants.getDataAlignmentSectorShift: SECTORSHIFTDATA requested is null"); } // System.exit(-1);
		return SECTORSHIFTDATA;
	}

        /**
         * Returns the layer/sector alignment data
         * @return
         */
        public static double[][][] getLayerSectorAlignmentData() {
                if(LAYERSHIFTDATA == null ) { System.err.println("error: SVTConstants.getLayerSectorAlignmentData: LAYERSHIFTDATA requested is null"); }
                return LAYERSHIFTDATA;
        }
        
        
}
