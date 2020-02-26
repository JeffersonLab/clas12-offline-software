package cnuphys.ced.geometry;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;

import eu.mihosoft.vrl.v3d.Vector3d;

import java.util.Optional;


public class DCGeometry2 {
	
	// the angle in degrees for rotating between tilted and sector CS
	private static final double _angle = 25.0;
	private static final double _sin25 = Math.sin(Math.toRadians(_angle));
	private static final double _cos25 = Math.cos(Math.toRadians(_angle));

	
	//wire positions. Indices are suplerlayer, layer, wire
	private static double _wireLeftX[][][] = new double[6][6][112]; 
	private static double _wireLeftY[][][] = new double[6][6][112]; 
	private static double _wireLeftZ[][][] = new double[6][6][112]; 
	private static double _wireRightX[][][] = new double[6][6][112]; 
	private static double _wireRightY[][][] = new double[6][6][112]; 
	private static double _wireRightZ[][][] = new double[6][6][112]; 
	
	private static double _wireMidX[][][] = new double[6][6][112]; 
	private static double _wireMidY[][][] = new double[6][6][112]; 
	private static double _wireMidZ[][][] = new double[6][6][112]; 


	public static DCGeant4Factory dcDetector;

	/**
	 * Initialize the DC Geometry by loading all the wires
	 */
	public static void initialize(String geomDBVar) {
		ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
		dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);
		
		//get the wire endpoints
		loadWirePositions();
	}
	
	//load the wire positions
	private static void loadWirePositions() {
		for  (int supl0 = 0; supl0 < 6; supl0++) {
			for  (int lay0 = 0; lay0 < 6; lay0++) {
				for  (int wire0 = 0; wire0 < 112; wire0++) {
					Vector3d v = dcDetector.getWireLeftend(supl0, lay0, wire0);
					tiltedToSector(v);
					
					_wireLeftX[supl0][lay0][wire0] = v.x;
					_wireLeftY[supl0][lay0][wire0] = v.y;
					_wireLeftZ[supl0][lay0][wire0] = v.z;
					
					v = dcDetector.getWireRightend(supl0, lay0, wire0);
					tiltedToSector(v);

					_wireRightX[supl0][lay0][wire0] = v.x;
					_wireRightY[supl0][lay0][wire0] = v.y;
					_wireRightZ[supl0][lay0][wire0] = v.z;
					
					v = dcDetector.getWireMidpoint(supl0, lay0, wire0);
					tiltedToSector(v);

					_wireMidX[supl0][lay0][wire0] = v.x;
					_wireMidY[supl0][lay0][wire0] = v.y;
					_wireMidZ[supl0][lay0][wire0] = v.z;
					

				}
			}
		}
	}
	
	//rotated tiltes cs to sector
	private static void tiltedToSector(Vector3d v) {
		double tx = v.x;
		double tz = v.z;
		
		v.x = tx * _cos25 + tz * _sin25;
		v.z = tz * _cos25 - tx * _sin25;

	}
	
	/**
	 * Convert tilted x and z to sector x and z
	 * @param tiltedX the tilted x coordinate
	 * @param tiltedZ the tilted z coordinate
	 * @return the sector coordinates, with v[0] = x and v[1] = z
	 */
	public static double[] tiltedToSector(double tiltedX, double tiltedZ) {
		double[] v = new double[2];
		v[0] = tiltedX * _cos25 + tiltedZ * _sin25;
		v[1] = tiltedZ * _cos25 - tiltedX * _sin25;

		return v;
	}
	
	//diagnostic to compare wite psotion with Gagik's package
	static void compareWire(int superlayer, int layer, int wire) {
		DCGeometry.printWire(superlayer, layer, wire);
		printWire(superlayer, layer, wire);
	}
	
	public static void printWire(int superlayer, int layer, int wire) {
		System.out.println(String.format(
				"NEW  end (%-4.1f, %-4.1f, %-4.1f) end (%-4.1f, %-4.1f, %-4.1f) mid (%-4.1f, %-4.1f, %-4.1f)",
				_wireLeftX[superlayer][layer][wire], _wireLeftY[superlayer][layer][wire], _wireLeftZ[superlayer][layer][wire],
				_wireRightX[superlayer][layer][wire], _wireRightY[superlayer][layer][wire], _wireRightZ[superlayer][layer][wire],
				_wireMidX[superlayer][layer][wire], _wireMidY[superlayer][layer][wire], _wireMidZ[superlayer][layer][wire]));
	}
	
	public static void main(String arg[]) {
		System.out.println("Testing the new DC geometry");
		
		initialize("default");
		
		//initialize Gagik's so I can compare
		DCGeometry.initialize();
		for (int superlayer = 0; superlayer < 6; superlayer++) {
			for (int layer = 0; layer < 6; layer++) {
				compareWire(superlayer,layer, 65);
				System.out.println();
			}
		}
		
	}
}
