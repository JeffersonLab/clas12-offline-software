package cnuphys.magfield.converter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;

import cnuphys.magfield.FloatVect;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;

/**
 * Convert the new ascii maps to the old ascii format so that they can then be
 * converted to binary. Because reasons.
 * 
 * @author heddle
 *
 */
public class Converter {

	private static int PHI = 0;
	private static int RHO = 1;
	private static int Z = 2;

	private static final double TINY = 1.0e-12;

	static boolean firstLine = true;
	static int lineCount;

	private static String gemcName[] = { "\"azimuthal\"", "\"transverse\"", "\"longitudinal\"" };
	private static String ordinal[] = { "first", "second", "third" };
	private static String units[] = { "\"deg\"", "\"cm\"", "\"cm\"" };

	private static String _homeDir = System.getProperty("user.home");

	private static ArrayList<File> dataFiles(String dataDir) {

		ArrayList<File> files = new ArrayList<File>();

		File dir = new File(dataDir);
		if (dir.isDirectory()) {

			FileFilter filter = new FileFilter() {

				@Override
				public boolean accept(File file) {
					String name = file.getName();
					return name.contains("polarpatch");
				}

			};

			File[] fileArray = dir.listFiles(filter);

			if ((fileArray != null) && (fileArray.length > 0)) {
				System.out.println("has " + fileArray.length + " filtered files");

				for (File f : fileArray) {
					files.add(f);
				}
			}

			Comparator<File> cc = new Comparator<File>() {

				@Override
				public int compare(File o1, File o2) {
					Integer i1 = getNValue(o1.getName());
					Integer i2 = getNValue(o2.getName());
					return i1.compareTo(i2);
				}

			};

			// have to take them in order
			files.sort(cc);

			// for (File f : files) {
			// System.out.println(f.getName());
			// }

		}

		return files;
	}

	// get the number appended at the end of the file
	// we have to sort by this
	private static int getNValue(String name) {
		String vname = name.replaceAll("[^\\d]", "");
		// System.out.println("vname = [" + vname + "]");
		return Integer.parseInt(vname);
	}

	// get the dir with all the tables
	private static String getDataDir() {
		return _homeDir + "/newMaps";
	}

	private static GridData[] preProcessor(ArrayList<File> files) throws IOException {

		if (!files.isEmpty()) {
			System.out.println("Found " + files.size() + " files.");

			GridData gdata[] = new GridData[3];
			for (int i = 0; i < 3; i++) {
				gdata[i] = (new Converter()).new GridData(i);
			}

			gdata[Z].n = files.size();

			int zIndex = 0;
			for (File file : files) {

				System.out.println(" PRE-PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");
				firstLine = true;
				lineCount = 0;
				try {

					AsciiReader ar = new AsciiReader(file, zIndex) {

						@Override
						protected void processLine(String line) {
							String tokens[] = AsciiReadSupport.tokens(line);

							if (firstLine) {
								int nRho = Integer.parseInt(tokens[0]);
								int nPhi = Integer.parseInt(tokens[1]);

								if (gdata[PHI].n == 0) {
									gdata[PHI].n = nPhi;
									gdata[RHO].n = nRho;
								} else {
									if ((gdata[PHI].n != nPhi) || (gdata[RHO].n != nRho)) {
										System.err.println("Grid Inconsistency");
										System.exit(1);
									}
								}

								firstLine = false;
							} // firstLine

							if ((tokens != null) && (tokens.length == 7)) {
								int rhoIndex = lineCount % (gdata[RHO].n);
								int phiIndex = lineCount % (gdata[RHO].n);

								double newX = Double.parseDouble(tokens[0]) / 10;
								double newY = Double.parseDouble(tokens[1]) / 10;
								double newZ = Float.parseFloat(tokens[2]) / 10;
								double newRho = Math.hypot(newX, newY);
								double newPhi = Math.toDegrees(Math.atan2(newY, newX));

								if (phiIndex == 0) {
									newPhi = 0.;
								} else {
									if (newPhi < -.0001) {
										newPhi += 360;
									}
								}

								if (phiIndex == (gdata[PHI].n - 1)) {
									if (zeroAngle(newPhi)) {
										newPhi = 360.;
									}
								}

								gdata[PHI].min = Math.max(0, Math.min(gdata[PHI].min, newPhi));
								gdata[PHI].max = Math.min(360, Math.max(gdata[PHI].max, newPhi));
								gdata[RHO].min = Math.min(gdata[RHO].min, newRho);
								gdata[RHO].max = Math.max(gdata[RHO].max, newRho);
								gdata[Z].min = Math.min(gdata[Z].min, newZ);
								gdata[Z].max = Math.max(gdata[Z].max, newZ);

								lineCount++;
							}

						} // processLine

						@Override
						public void done() {
							System.out.println(" processed " + lineCount + " lines");
						}

					};
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				}

				zIndex++;
			}

			if ((gdata[PHI].max > 31) && (gdata[PHI].max > 31)) {
				System.err.println("Correcting PhiMax to 360 from " + gdata[PHI].max);
			}

			return gdata;
		}

		return null;
	}

	private static boolean zeroAngle(double angDeg) {
		if ((Math.abs(angDeg) < TINY) || (Math.abs(angDeg - 360.) < TINY)) {
			return true;
		}

		return false;
	}

	// process all the files
	private static void processAllFiles(ArrayList<File> files, GridData gdata[]) throws IOException {
		if (!files.isEmpty()) {

			File bfile = new File(getDataDir(), "torus.dat");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(bfile));

			int nPhi = gdata[PHI].n;
			int nRho = gdata[RHO].n;
			int nZ = gdata[Z].n;

			float phimin = (float) gdata[PHI].min;
			float phimax = (float) gdata[PHI].max;
			float rhomin = (float) gdata[RHO].min;
			float rhomax = (float) gdata[RHO].max;
			float zmin = (float) gdata[Z].min;
			float zmax = (float) gdata[Z].max;

			dos.writeInt(0xced);
			dos.writeInt(0);
			dos.writeInt(1);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeFloat(phimin);
			dos.writeFloat(phimax);
			dos.writeInt(nPhi);
			dos.writeFloat(rhomin);
			dos.writeFloat(rhomax);
			dos.writeInt(nRho);
			dos.writeFloat(zmin);
			dos.writeFloat(zmax);
			dos.writeInt(nZ);

			long unixTime = System.currentTimeMillis();

			int high = (int) (unixTime >> 32);
			int low = (int) unixTime;

			// write reserved
			dos.writeInt(high); // first word of unix time
			dos.writeInt(low); // second word of unix time
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);

			int size = 3 * 4 * nPhi * nRho * nZ;
			System.err.println("FILE SIZE = " + size + " bytes");

			int zIndex = 0;

			FloatVect[][][] bvals = new FloatVect[nPhi][nRho][nZ];

			for (File file : files) {

				System.out.print(" PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");

				try {

					AsciiReader ar = new AsciiReader(file, zIndex) {
						int count = 0;

						@Override
						protected void processLine(String line) {
							String tokens[] = AsciiReadSupport.tokens(line);
							if ((tokens != null) && (tokens.length == 7)) {

								int rhoIndex = count % nRho;
								int phiIndex = count / nRho;

								// note t to kG
								float Bx = Float.parseFloat(tokens[3]) * 10;
								float By = Float.parseFloat(tokens[4]) * 10;
								float Bz = Float.parseFloat(tokens[5]) * 10;

								try {
									bvals[phiIndex][rhoIndex][iVal] = new FloatVect(Bx, By, Bz);
								} catch (ArrayIndexOutOfBoundsException e) {
									e.printStackTrace();
									System.err.println(
											"PHIINDX: " + phiIndex + "  RHOINDX: " + rhoIndex + "  ZINDX:  " + iVal);

									double x = Double.parseDouble(tokens[0]) / 10;
									double y = Double.parseDouble(tokens[1]) / 10;
									double z = Double.parseDouble(tokens[2]) / 10;
									double phi = Math.toDegrees(Math.atan2(y, x));
									double rho = Math.hypot(x, y);
									System.err.println("PHI: " + phi + "   rho: " + rho + "   z: " + z);

									System.exit(1);
								}

								count++;
							}
						}

						@Override
						public void done() {
							System.out.println(" processed " + count + " lines");
						}

					};
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				}

				zIndex++;

			} // end for loop

			for (int iPHI = 0; iPHI < nPhi; iPHI++) {
				System.out.println("iPHI = " + iPHI);
				for (int iRHO = 0; iRHO < nRho; iRHO++) {
					for (int iZ = 0; iZ < nZ; iZ++) {
						FloatVect fcv = bvals[iPHI][iRHO][iZ];
						dos.writeFloat(fcv.x);
						dos.writeFloat(fcv.y);
						dos.writeFloat(fcv.z);
					}
				}
			}

			dos.flush();
			dos.close();

			System.out.println("Binary file: " + bfile.getCanonicalPath());

		}

	}

	public static void convertToBinary(ArrayList<File> files, GridData gdata[]) {
		if (gdata != null) {
			try {
				processAllFiles(files, gdata);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void convertToGemc(ArrayList<File> files, GridData gdata[]) {
		if (gdata != null) {
			File afile = new File(getDataDir(), "gemc_torus.txt");

			boolean assumedSymmetric = gdata[PHI].max < 100;
			System.out.println("In GEMC Converter assumed symmetric: " + assumedSymmetric);

			if (afile.exists()) {
				afile.delete();
			}

			try {
				PrintWriter writer = new PrintWriter(new FileOutputStream(afile));

				// write the header

				int indentLevel = 0;
				writeln(writer, indentLevel, false, "<mfield>");
				indentLevel++;

				writeln(writer, indentLevel, true, "description name=\"" + afile.getName()
						+ "\" factory=\"ASCII\" comment=\"clas12 superconducting torus\"");

				writeln(writer, indentLevel, true,
						"symmetry type=\"" + (assumedSymmetric ? "phi-segmented\"" : "none\"")
								+ " format=\"map\" integration=\"ClassicalRK4\" minStep=\"1*mm\"");

				writeln(writer, indentLevel, false, "<map>");
				indentLevel++;

				writeln(writer, indentLevel, false, "<coordinate>");
				indentLevel++;

				for (GridData gd : gdata) {
					writeln(writer, indentLevel, true, gd.forGEMC());
				}

				indentLevel--;
				writeln(writer, indentLevel, false, "</coordinate>");

				writeln(writer, indentLevel, true, "field unit=\"kilogauss\"");
				writeln(writer, indentLevel, true, "interpolation type=\"none\"");

				indentLevel--;
				writeln(writer, indentLevel, false, "</map>");

				indentLevel--;
				writeln(writer, indentLevel, false, "</mfield>");

				// OK now the data

				int nPhi = gdata[PHI].n;
				int nRho = gdata[RHO].n;
				int nZ = gdata[Z].n;

				int zIndex = 0;
				FloatVect[][][] bvals = new FloatVect[nPhi][nRho][nZ];

				for (File file : files) {

					System.out.print(" PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");

					try {

						AsciiReader ar = new AsciiReader(file, zIndex) {
							int count = 0;

							@Override
							protected void processLine(String line) {
								String tokens[] = AsciiReadSupport.tokens(line);
								if ((tokens != null) && (tokens.length == 7)) {

									int rhoIndex = count % nRho;
									int phiIndex = count / nRho;

									// note T to kG
									float Bx = Float.parseFloat(tokens[3]) * 10;
									float By = Float.parseFloat(tokens[4]) * 10;
									float Bz = Float.parseFloat(tokens[5]) * 10;

									bvals[phiIndex][rhoIndex][iVal] = new FloatVect(Bx, By, Bz);

									count++;
								}
							}

							@Override
							public void done() {
								System.out.println(" processed " + count + " lines");
							}

						};
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						System.exit(1);
					}

					zIndex++;

				} // end for loop

				// now write them back out
				for (int iPhi = 0; iPhi < nPhi; iPhi++) {
					double phi = gdata[PHI].min + iPhi * gdata[PHI].del();
					for (int iRho = 0; iRho < nRho; iRho++) {
						double rho = gdata[RHO].min + iRho * gdata[RHO].del();
						for (int iZ = 0; iZ < nZ; iZ++) {
							double z = gdata[Z].min + iZ * gdata[Z].del();
							FloatVect fv = bvals[iPhi][iRho][iZ];

							writeln(writer, 0, false, String.format("%-5.1f %-5.1f %-5.1f %s %s %s", phi, rho, z,
									bstr(fv.x), bstr(fv.y), bstr(fv.z)));

							// if (iZ == 200) {
							// writer.flush();
							// writer.close();
							// return;
							// }
						}

					}

				}

				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String bstr(float b) {
		if (Math.abs(b) < 1.0e-3) {
			return "0";
		} else {
			return String.format("% 11.4E", b);
		}
	}

	private static String spaces = "    ";

	private static void writeln(PrintWriter writer, int indentLevel, boolean brackets, String line) {
		for (int i = 0; i < indentLevel; i++) {
			writer.print(spaces);
		}
		if (brackets) {
			writer.print("<");
			writer.print(line);
			writer.println("/>");
		} else {
			writer.println(line);
		}
	}

	public static void main(String arg[]) {
		String dataDir = getDataDir();

		ArrayList<File> files = dataFiles(dataDir);
		System.out.println("data dir = [" + dataDir + "]  file count: " + files.size());

		// preprocess to get the grid data
		GridData gdata[] = null;
		try {
			gdata = preProcessor(files);

			System.out.println("PHI: " + gdata[PHI]);
			System.out.println("RHO: " + gdata[RHO]);
			System.out.println("Z: " + gdata[Z]);

		} catch (Exception e) {
			e.printStackTrace();
		}

		convertToBinary(files, gdata);
		// convertToGemc(files, gdata);

		System.out.println("done");
	}

	class GridData {
		public int index = -1;
		public int n = 0;
		public double min = Double.POSITIVE_INFINITY;
		public double max = Double.NEGATIVE_INFINITY;

		public GridData(int index) {
			this.index = index;
		}

		public String forGEMC() {
			return String.format("%-6s name=%-14s npoints=\"%d\" min=\"%d\" max=\"%d\" units=%s", ordinal[index],
					gemcName[index], n, (int) min, (int) max, units[index]);
		}

		@Override
		public String toString() {
			return String.format("N = %d   min = %12.5f  max = %12.5f", n, min, max);
		}

		public double del() {
			return (max - min) / (n - 1);
		}

	}
	
	/**
	 * Convert the in memory solenoid to GEMC format
	 */
	public static void memoryToGEMCSolenoidConverter() {
		Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
		if (solenoid == null) {
			System.err.println("No in-map solenoid.");
			System.exit(-1);
		}
		
		String mapCreationDate = solenoid.getCreationDate();
		mapCreationDate=  mapCreationDate.replace("/", "_");
		mapCreationDate=  mapCreationDate.replace(":", "_");
		mapCreationDate=  mapCreationDate.replace(" ", "_");
		

		File afile = new File(getDataDir(), "gemc_solenoid_" + mapCreationDate + ".dat");
		System.out.println("GEMC file at [" + afile.getPath() + "]");

		if (afile.exists()) {
			afile.delete();
		}
		
		GridCoordinate gcR = solenoid.getQ2Coordinate();
		GridCoordinate gcZ = solenoid.getQ3Coordinate();
		
		int nR = gcR.getNumPoints();
		int nZ = gcZ.getNumPoints();
		
		double rMin = gcR.getMin()/100.;  //meters
		double rMax = gcR.getMax()/100.;  //meters
		double zMin = gcZ.getMin()/100.;  //meters
		double zMax = gcZ.getMax()/100.;  //meters

		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(afile));
			
			// write the header

			int indentLevel = 0;
			writeln(writer, indentLevel, false, "<mfield>");
			indentLevel++;

			writeln(writer, indentLevel, true, "description name=\"" + "clas12-newSolenoid"
					+ "\" factory=\"ASCII\" comment=\"clas12 superconducting solenoid\"");

			writeln(writer, indentLevel, true,
					"symmetry type=\"" + "cylindrical-z\""
							+ " format=\"map\" integration=\"ClassicalRK4\" minStep=\"0.01*mm\"");

			writeln(writer, indentLevel, false, "<map>");
			indentLevel++;

			writeln(writer, indentLevel, false, "<coordinate>");
			indentLevel++;
			
			
			String s1 = String.format("<%s name=\"%s\" npoints=\"%d\" min=\"%d\" max=\"%d\" units=\"%s\"/>", "first", "transverse", nR, (int)rMin, (int)rMax, "m");
			String s2 = String.format("<%s name=\"%s\" npoints=\"%d\" min=\"%d\" max=\"%d\" units=\"%s\"/>", "second", "longitudinal", nZ, (int)zMin, (int)zMax, "m");

			writeln(writer, indentLevel, false, s1);
			writeln(writer, indentLevel, false, s2);

			indentLevel--;
			writeln(writer, indentLevel, false, "</coordinate>");

			writeln(writer, indentLevel, true, "field unit=\"T\"");

			indentLevel--;
			writeln(writer, indentLevel, false, "</map>");

			indentLevel--;
			writeln(writer, indentLevel, false, "</mfield>");
			
			
			double valuesR[] = gcR.getValues();
			double valuesZ[] = gcZ.getValues();
			
			for (int iR = 0; iR < valuesR.length; iR++) {
				double r = valuesR[iR];
				for (int iZ = 0; iZ < valuesZ.length; iZ++) {
					double z = valuesZ[iZ];
					
					int compIndex = solenoid.getCompositeIndex(0, iR, iZ);
					
					double bR = solenoid.getB2(compIndex);
					double bZ = solenoid.getB3(compIndex);
					
					String s = String.format("%-6.3f %-6.3f %-11.5e %-11.5e", r/100, z/100, bR/10, bZ/10);
					writeln(writer, 0, false, s);
				}				
			}
						
			
			writer.flush();
			writer.close();


		}
		catch (Exception e) {
			
		}

	}
	
	
	/**
	 * Convert the in memory torus to GEMC format
	 */
	public static void memoryToGEMCTorusConverter() {
		Torus torus = MagneticFields.getInstance().getTorus();
		if (torus == null) {
			System.err.println("No in-map torus.");
			System.exit(-1);
		}
		
		String mapCreationDate = torus.getCreationDate();
		mapCreationDate=  mapCreationDate.replace("/", "_");
		mapCreationDate=  mapCreationDate.replace(":", "_");
		mapCreationDate=  mapCreationDate.replace(" ", "_");
		

		File afile = new File(getDataDir(), "gemc_torus_" + mapCreationDate + ".dat");
		System.out.println("GEMC file at [" + afile.getPath() + "]");

		if (afile.exists()) {
			afile.delete();
		}
		
		GridCoordinate gcP = torus.getQ1Coordinate();
		GridCoordinate gcR = torus.getQ2Coordinate();
		GridCoordinate gcZ = torus.getQ3Coordinate();
		
		int nR = gcR.getNumPoints();
		int nZ = gcZ.getNumPoints();
		
		double rMin = gcR.getMin()/100.;  //meters
		double rMax = gcR.getMax()/100.;  //meters
		double zMin = gcZ.getMin()/100.;  //meters
		double zMax = gcZ.getMax()/100.;  //meters

		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(afile));
			
			// write the header

			int indentLevel = 0;
			writeln(writer, indentLevel, false, "<mfield>");
			indentLevel++;

			writeln(writer, indentLevel, true, "description name=\"" + "clas12-newSolenoid"
					+ "\" factory=\"ASCII\" comment=\"clas12 superconducting solenoid\"");

			writeln(writer, indentLevel, true,
					"symmetry type=\"" + "cylindrical-z\""
							+ " format=\"map\" integration=\"ClassicalRK4\" minStep=\"0.01*mm\"");

			writeln(writer, indentLevel, false, "<map>");
			indentLevel++;

			writeln(writer, indentLevel, false, "<coordinate>");
			indentLevel++;
			
			
			String s1 = String.format("<%s name=\"%s\" npoints=\"%d\" min=\"%d\" max=\"%d\" units=\"%s\"/>", "first", "transverse", nR, (int)rMin, (int)rMax, "m");
			String s2 = String.format("<%s name=\"%s\" npoints=\"%d\" min=\"%d\" max=\"%d\" units=\"%s\"/>", "second", "longitudinal", nZ, (int)zMin, (int)zMax, "m");

			writeln(writer, indentLevel, false, s1);
			writeln(writer, indentLevel, false, s2);

			indentLevel--;
			writeln(writer, indentLevel, false, "</coordinate>");

			writeln(writer, indentLevel, true, "field unit=\"T\"");

			indentLevel--;
			writeln(writer, indentLevel, false, "</map>");

			indentLevel--;
			writeln(writer, indentLevel, false, "</mfield>");
			
//			
//			double valuesR[] = gcR.getValues();
//			double valuesZ[] = gcZ.getValues();
//			
//			for (int iR = 0; iR < valuesR.length; iR++) {
//				double r = valuesR[iR];
//				for (int iZ = 0; iZ < valuesZ.length; iZ++) {
//					double z = valuesZ[iZ];
//					
//					int compIndex = solenoid.getCompositeIndex(0, iR, iZ);
//					
//					double bR = solenoid.getB2(compIndex);
//					double bZ = solenoid.getB3(compIndex);
//					
//					String s = String.format("%-6.3f %-6.3f %-11.8f %-11.8f", r/100, z/100, bR/10, bZ/10);
//					writeln(writer, 0, false, s);
//				}				
//			}
//						
			
			writer.flush();
			writer.close();


		}
		catch (Exception e) {
			
		}

	}

}