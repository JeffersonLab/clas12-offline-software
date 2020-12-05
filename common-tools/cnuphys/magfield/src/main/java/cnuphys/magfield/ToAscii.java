package cnuphys.magfield;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import cnuphys.magfield.converter.AsciiReader;

public class ToAscii {
	
	private static float MINVAL = (float)(1.0e-6);
	
	private static boolean _csv;

	/**
	 * Write the Torus to Ascii
	 */
	public static void torusToAscii(Torus torus, String path, boolean csv) {
		
		_csv = csv;
		
		System.out.println("Converting Torus To Ascii. CSV: " + csv);
		
		if (_csv) {
			MINVAL = (float)(1.0e-4);
		}
		
		File asciiFile = new File(path);

		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(asciiFile));

			if (!_csv) {
				writeAsciiHeader(dos, torus);
			}
			writeTorusData(dos, torus);
			dos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}
	

	/**
	 * Write the Solenoid to Ascii
	 */
	public static void solenoidToAscii(SolenoidProbe solenoid, String path) {
		
		_csv = true;
		
		System.out.println("Converting Solenoid To CSV");
		
		MINVAL = (float)(1.0e-4);
		
		File asciiFile = new File(path);

		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(asciiFile));

			writeSolenoidData(dos, solenoid);
			dos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}

	
	private static void writeTorusData(DataOutputStream dos, Torus torus) {
				
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
					
					float bx = torus.getB1(compositeIndex);
					float by = torus.getB2(compositeIndex);
					float bz = torus.getB3(compositeIndex);
					
					String s = null;
					
					if (_csv) {
						s = String.format("%-3.0f,%-3.0f,%-3.0f,%s,%s,%s", 
								p, r, z, vStr(bx), vStr(by), vStr(bz));
					}
					else {
						s = String.format("%-3.0f %-3.0f %-3.0f %s %s %s", 
								p, r, z, vStr(bx), vStr(by), vStr(bz));
					}
					
					s = s.replace("   ", " ");
					s = s.replace("  ", " ");		
					s = s.replace("0E", "E");		
					s = s.replace("0E", "E");		
					s = s.replace("0E", "E");		
					s = s.replace("0E", "E");		
					s = s.replace("E+00", "");		
					
					if (_csv) {
						s = s.replace(", ", ",");		
						s = s.replace(" ,", ",");		
					}
					stringLn(dos, 0, s);
				}
			}
		}
		
	}
	
	private static void writeSolenoidData(DataOutputStream dos, SolenoidProbe solenoid) {
		
		//needed from torus
		GridCoordinate pCoord = solenoid.getPhiCoordinate();
		GridCoordinate rCoord = solenoid.getRCoordinate();
		GridCoordinate zCoord = solenoid.getZCoordinate();

		
		for (int ip = 0;  ip <= 360; ip += 2) {			
			double p = ip;

			System.out.println("phi = " + p);

			double rphi = Math.toRadians(p);
			double cos = Math.cos(rphi);
			double sin = Math.sin(rphi);

			for (int ridx = 0; ridx < rCoord.getNumPoints(); ridx++) {

				if ((ridx % 2) == 0) {
					double r = rCoord.getValue(ridx);

					if (r < 150.1) {

						for (int zidx = 0; zidx < zCoord.getNumPoints(); zidx++) {

							if ((zidx % 2) == 0) {

								double z = zCoord.getValue(zidx);
								if ((z > -200.1) && (z < 200.1)) {

									int compositeIndex = solenoid.getCompositeIndex(0, ridx, zidx);

									// the solenoid map is in cylindrical coordinates
									float bphi = solenoid.getB1(compositeIndex);
									float brho = solenoid.getB2(compositeIndex);
									float bz = solenoid.getB3(compositeIndex);

									float bx = (float) (brho * cos - bphi * sin);
									float by = (float) (brho * sin + bphi * cos);

									String s = null;

									s = String.format("%-3.0f,%-3.0f,%-3.0f,%s,%s,%s", p, r, z, vStr(bx), vStr(by),
											vStr(bz));

									s = s.replace("   ", " ");
									s = s.replace("  ", " ");
									s = s.replace("0E", "E");
									s = s.replace("0E", "E");
									s = s.replace("0E", "E");
									s = s.replace("0E", "E");
									s = s.replace("E+00", "");

									s = s.replace(", ", ",");
									stringLn(dos, 0, s);
								} // z in range
							} // even z index
						} // z loop
					} // rho < 150.1
				} // even rho index
			} // rho loop
		}

	}

	
	private static String vStr(float val) {
		if (Math.abs(val) < MINVAL) {
			return "0";
		}
		
		return String.format("%11.4E", val);
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
				dos.writeBytes(" ");
			}
			
			dos.writeBytes(str);
			
			dos.writeBytes("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static int _lineCount;
	private static int _dataCount;
	private static boolean _headerRead = false;
	
	public static void readAsciiTorus(String path) {
		
		final File file = new File(path);
		_lineCount = 0;
		_dataCount = 0;
		
		try {
			new AsciiReader(file) {

				@Override
				protected void processLine(String line) {
					
					//arggh. For some unknown reason the string has a null \0
					//character between every real character
					line = line.replace("\0", "");
					_lineCount++;
					
					if (!_headerRead) {
						if (line.contains("</mfield>")) {
							System.out.println("Done reading header");
							_headerRead = true;
						}
					}
					else { //data line
						_dataCount++;
					}
				}

				@Override
				public void done() {
					System.out.println("Read " + _lineCount + " lines. Data lines: " + _dataCount);
				}
				
			};
		} catch (FileNotFoundException e) {
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
