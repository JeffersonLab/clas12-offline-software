package cnuphys.magfield;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ToAscii {

	/**
	 * Write the Torus to Ascii
	 */
	public static void torusToAscii(Torus torus, String path) {
		
		
		System.out.println("Converting Torus To Ascii");
		File asciiFile = new File(path);
		
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(asciiFile));
			writeAsciiHeader(dos, torus);
			writeData(dos, torus);
			dos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}
	
	private static void writeData(DataOutputStream dos, Torus torus) {
		
		//needed from torus
		GridCoordinate pCoord = torus.getPhiCoordinate();
		GridCoordinate rCoord = torus.getRCoordinate();
		GridCoordinate zCoord = torus.getZCoordinate();

		
		for (int pidx = 0; pidx < pCoord.getNumPoints(); pidx++) {			
			double p = pCoord.getValue(pidx);
			
			System.out.println("phi = " + p);
			
			for (int ridx = 0; ridx < rCoord.getNumPoints(); ridx++) {
				double r = rCoord.getValue(ridx);

				for (int zidx = 0; zidx < zCoord.getNumPoints(); zidx++) {
					double z = zCoord.getValue(zidx);
					
					int compositeIndex = torus.getCompositeIndex(pidx, ridx, zidx);
					
					float bp = torus.getB1(compositeIndex);
					float br = torus.getB2(compositeIndex);
					float bz = torus.getB3(compositeIndex);
					
					
					String s = String.format("%3.0f %3.0f %3.0f %12.5E %12.5E %12.5E", 
							p, r, z, bp, br, bz);
					stringLn(dos, 0, s);
				}
			}
		}
		
	}
	
	private static void writeAsciiHeader(DataOutputStream dos, Torus torus) {
		
		//needed from torus
		GridCoordinate pCoord = torus.getPhiCoordinate();
		GridCoordinate rCoord = torus.getRCoordinate();
		GridCoordinate zCoord = torus.getZCoordinate();
		
		
		stringLn(dos, 0, "<mfield>");
		stringLn(dos, 2, "<description name=\"torus_version3.0\" factory=\"ASCII\" comment=\"clas12 superconducting torus\"/>");
		stringLn(dos, 2, "<symmetry type=\"cylindrical_3d\" format=\"map\" integration=\"ClassicalRK4\" minStep=\"1*mm\"/>");

		stringLn(dos, 2, "<map>");
		stringLn(dos, 4, "<coordinate>");

		writeCoord(dos, 6, pCoord, "first", "azimuthal", "deg");
		writeCoord(dos, 6, rCoord, "second", "transverse", "cm");
		writeCoord(dos, 6, zCoord, "third", "longitudinal", "cm");
		
		stringLn(dos, 4, "</coordinate>");
		stringLn(dos, 4, "<field unit=\"kilogauss\"/>");
		stringLn(dos, 4, "<interpolation type=\"none\"/>");
		
		
		stringLn(dos, 2, "</map>");
		stringLn(dos, 0, "</mfield>");
		
	}
		
	private static void writeCoord(DataOutputStream dos, int leadingSpace, GridCoordinate coord, String ordinal, String name, String unit) {
		int np = coord.getNumPoints();
		int min = (int)(coord.getMin());
		int max = (int)(coord.getMax());
		
		String s = String.format("<%s name=\"%s\" npoints=\"%d\" min=\"%d.0\" max=\"%d.0\"/>", 
				ordinal, name, np, min, max);
		
		stringLn(dos, leadingSpace, s);
	}
	
	private static void stringLn(DataOutputStream dos, int leadingSpace, String str) {
		
		try {
			for (int i = 0; i < leadingSpace; i++) {
				dos.writeChars(" ");
			}
			dos.writeChars(str);
			
			dos.writeChars("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
//	<mfield>
//    <description name="torus_version3.0" factory="ASCII" comment="clas12 superconducting torus"/>
//    <symmetry type="cylindrical_3d" format="map" integration="ClassicalRK4" minStep="1*mm"/>
//    <map>
//        <coordinate>
//            <first  name="azimuthal"    npoints="0" min="0" max="360" units="deg"/>
//            <second name="transverse"   npoints="10" min="0" max="500" units="cm"/>
//            <third  name="longitudinal" npoints="10" min="100" max="600" units="cm"/>
//        </coordinate>
//        <field unit="kilogauss"/>
//        <interpolation type="none"/>
//    </map>
//</mfield>

}
