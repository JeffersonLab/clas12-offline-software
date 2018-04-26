package newMaps;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;

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
	
	private static String gemcName[] = {"\"azimuthal\"", "\"transverse\"", "\"longitudinal\""};
	private static String ordinal[] = {"first", "second", "third"};
	private static String units[] = {"\"deg\"", "\"cm\"", "\"cm\""};

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

	private static final double TINY = 1.0e-12;

	// test that the grid sems to be right
	static double oldZ = -99f;
	static double oldRho = -99f;
	static double oldPhi = -999f;

	static boolean checkrho = true;
	private static GridData[] preProcess(ArrayList<File> files) throws IOException {
		
		if (!files.isEmpty()) {
			System.out.println("Found " + files.size() + " files.");
			GridData gdata[] = new GridData[3];
			for (int i = 0; i < 3; i++) {
				gdata[i] = (new Converter()).new GridData(i);
			}

			int zIndex = 0;
			oldZ = -99f;
			oldRho = -99f;
			oldPhi = -999f;
			float tiny = 0.1f;

			for (File file : files) {
				System.out.println(" PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");

				boolean first = (zIndex == 0);
				
//				boolean last = (zIndex == (files.size()-1));
				
				gdata[Z].n = files.size();
				
	//			if (zIndex == 1) break;
				
				try {

					AsciiReader ar = new AsciiReader(file, zIndex) {
						int count = 0;

						@Override
						protected void processLine(String line) {
							String tokens[] = AsciiReadSupport.tokens(line);
							if ((tokens != null) && (tokens.length == 7)) {

								double newX = Double.parseDouble(tokens[0])/10;
								double newY = Double.parseDouble(tokens[1])/10;
								double newZ = Float.parseFloat(tokens[2])/10;
								double newRho = Math.hypot(newX, newY);
								double newPhi = Math.toDegrees(Math.atan2(newY, newX));
								if (newPhi < -.0001) newPhi += 360;
							
								gdata[PHI].min = Math.min(gdata[PHI].min, newPhi);
								gdata[PHI].max = Math.max(gdata[PHI].max, newPhi);
								gdata[RHO].min = Math.min(gdata[RHO].min, newRho);
								gdata[RHO].max = Math.max(gdata[RHO].max, newRho);
								gdata[Z].min = Math.min(gdata[Z].min, newZ);
								gdata[Z].max = Math.max(gdata[Z].max, newZ);
								
							
//								if (oldPhi > 1) {
//									if (newPhi < 1) {
//										System.out.println("phi = " + newPhi + "  x = " + newX/10 + "  y = " + newY/10);
//									}
//								}
								

								if (Math.abs(newPhi - oldPhi) > tiny) {
									if (first && (newPhi > oldPhi)) {
										System.out.println("    new Phi = " + newPhi);
										gdata[PHI].n += 1;
									}
									oldPhi = newPhi;
									
								}
								
								if (Math.abs(newRho - oldRho) > tiny) {
	//								System.out.println("  new Rho = " + newRho/10); //mm to cm
									
									if (checkrho && (newRho < oldRho)) {
										checkrho = false;
									}
									if (checkrho &&  (newRho > oldRho)) {
										gdata[RHO].n += 1;
									}
			     					oldRho = newRho;
									

								}

								
								if (Math.abs(newZ - oldZ) > tiny) {
	//								System.out.println("new Z = " + newZ/10); //mm to cm
									oldZ = newZ;
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
			} // end file loop

			return gdata;
		} else {
			System.err.println("No files!");
			return null;
		}
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
			
			int high = (int)(unixTime >> 32);
			int low = (int)unixTime;
			
			
			// write reserved
			dos.writeInt(high);  //first word of unix time
			dos.writeInt(low);  //second word of unix time
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
				
				//write the header
				
				int indentLevel = 0;
				writeln(writer, indentLevel, false, "<mfield>");
				indentLevel++;
				
				writeln(writer, indentLevel, true, "description name=\""
						+ afile.getName() + "\" factory=\"ASCII\" comment=\"clas12 superconducting torus\"");
				
				writeln(writer, indentLevel, true, "symmetry type=\"" + 
						(assumedSymmetric ? "phi-segmented\"" : "none\"") +
						" format=\"map\" integration=\"ClassicalRK4\" minStep=\"1*mm\"");
				
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
		    	
		    	//OK now the data
		    	
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
				
				
				//now write them back out
				for (int iPhi= 0; iPhi < nPhi; iPhi++) {
					double phi = gdata[PHI].min + iPhi*gdata[PHI].del();
					for (int iRho= 0; iRho < nRho; iRho++) {
						double rho = gdata[RHO].min + iRho*gdata[RHO].del();
						for (int iZ= 0; iZ < nZ; iZ++) {
							double z = gdata[Z].min + iZ*gdata[Z].del();
							FloatVect fv = bvals[iPhi][iRho][iZ];
														
							writeln(writer, 0, false, String.format("%-5.1f %-5.1f %-5.1f %s %s %s", 
									phi, rho, z, bstr(fv.x), bstr(fv.y), bstr(fv.z)));
							
//							if (iZ == 200) {
//								writer.flush();
//								writer.close();
//								return;
//							}
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
		}
		else {
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

		//preprocess to get the grid data
		GridData gdata[] = null;
		try {
			gdata = preProcess(files);
			System.out.println("PHI: " + gdata[PHI]);
			System.out.println("RHO: " + gdata[RHO]);
			System.out.println("Z: " + gdata[Z]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	//	convertToBinary(files, gdata);
		convertToGemc(files, gdata);

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
			return String.format("%-6s name=%-14s npoints=\"%d\" min=\"%d\" max=\"%d\" units=%s", ordinal[index], gemcName[index], n, (int)min, (int)max, units[index]);
		}
		
		@Override
		public String toString() {
			return String.format("N = %d   min = %12.5f  max = %12.5f", n, min, max);
		}
		
		public double del() {
			return (max-min)/(n-1);
		}
		
	}

}
