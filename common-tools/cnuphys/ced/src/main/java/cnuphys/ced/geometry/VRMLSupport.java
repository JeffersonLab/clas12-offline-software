package cnuphys.ced.geometry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class VRMLSupport {
	
	private static String _lineColors[] = {
			
			"0.0 0.0 0.0,",
			"0.5 0.5 0.5,",
			
			"1.0 0.0 0.0,",
			"0.5 0.0 0.0,",
			
			"0.0 1.0 0.0,",
			"0.0 0.5 0.0,",
			
			"0.0 0.0 1.0,",
			"0.0 0.0 0.5,",
			"0.0 0.0 0.25,",	
			
			"1.0 1.0 0.0,",
			"0.0 1.0 1.0,",
			"1.0 0.0 1.0"
	};
	
	private static int INDENT = 2;

	public static PrintStream openForWriting(String path) {
		File file = new File(path);
		PrintStream ps = null;
		try {
			ps = new PrintStream(file);
			header(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return ps;
	}
	
	private static void header(PrintStream ps) {
		ps.println("#VRML V2.0 utf8");
		ps.println("WorldInfo {");
		indentPrint(ps, 1, "title \"CLAS Geometry\"");
		indentPrint(ps, 1, "info [\"This content created from ced\"]");
		ps.println("}");
	}
	
	public static void close(PrintStream ps) {
		ps.flush();
		ps.close();
	}
	
	public static void indexedFaceSet(PrintStream ps, float red, float green, float blue, float transparency, float coords[], int[][] indices) {
		ps.println("Shape {");
		
		//appearance
		indentPrint(ps, 1, "appearance Appearance {");
		indentPrint(ps, 2, "material Material {");
		
		String cs = String.format("diffuseColor %4.2f %4.2f %4.2f", red, green, blue);
		indentPrint(ps, 3, cs);
		
		String ts = String.format("transparency %4.2f", transparency);
		indentPrint(ps, 3, ts);
		indentPrint(ps, 2, "}"); //material
		indentPrint(ps, 1, "}"); //appearance

		indentPrint(ps, 1, "geometry IndexedFaceSet {");
		
		//coordinate
		indentPrint(ps, 2, "coord Coordinate {");
		indentPrint(ps, 3, "point ["); //indexed face set

		indentArray(ps, 4, coords);
		
		indentPrint(ps, 3, "]"); //point
		indentPrint(ps, 2, "}"); //coord coordinate
		
		//coordinate index
		indentPrint(ps, 2, "coordIndex [");
		int len = indices.length;
		for (int face = 0; face < len; face++) {
			int idx[] = indices[face];
			
			String s = intArrayString(idx);
			indentPrint(ps, 3, s);
		}

		
		indentPrint(ps, 2, "]"); //coord index
		
		indentPrint(ps, 1, "}"); //geometry indexed face set
		
		ps.println("}"); //shape

	}
	
	
	private static void indentArray(PrintStream ps, int level, float[] coords) {
		int len = coords.length;
		int numPoints = len/3;
		
		for (int i = 0; i < numPoints; i++) {
			int j = 3 * i;

			String s = String.format("%8.1f %8.1f %8.1f", coords[j], coords[j + 1], coords[j + 2]);
			
			if (i != (numPoints - 1)) {
				s += ",";
			}
			
			indentPrint(ps, level, s);
		}
	}
	
	
	private static void indentPrint(PrintStream ps, int level, String message) {
		spaces(ps, level);
		ps.println(message);
	}
	
	private static void spaces(PrintStream ps, int level) {
		int spaces = level*INDENT;
		for (int i = 0; i < spaces; i++) {
			ps.print(" ");
		}
	}
	
	private static String intArrayString(int[] ia) {
		String s= "";
		for (int i : ia) {
			s += (i + ", ");
		}
		s += "-1,";
		
		return s;
	}
	
}
