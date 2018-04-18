package newMaps;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	static float oldZ = -99f;
	static float oldRho = -99f;
	static float oldPhi = -999f;

	private static void checkAllFiles(ArrayList<File> files) throws IOException {
		if (!files.isEmpty()) {
			System.out.println("Found " + files.size() + " files.");

			int zIndex = 0;
			oldZ = -99f;
			oldRho = -99f;
			oldPhi = -999f;
			float tiny = 0.1f;

			for (File file : files) {
				System.out.println(" PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");

				if (zIndex == 1) break;
				
				try {

					AsciiReader ar = new AsciiReader(file, zIndex) {
						int count = 0;

						@Override
						protected void processLine(String line) {
							String tokens[] = AsciiReadSupport.tokens(line);
							if ((tokens != null) && (tokens.length == 7)) {

								double newX = Double.parseDouble(tokens[0]);
								double newY = Double.parseDouble(tokens[1]);
								float newZ = Float.parseFloat(tokens[2]);
								
								float newRho = (float) Math.hypot(newX, newY);
								float newPhi = (float) Math.toDegrees(Math.atan2(newY, newX));
								if (newPhi < -.0001) newPhi += 360;
							
								if (oldPhi > 1) {
									if (newPhi < 1) {
										System.out.println("phi = " + newPhi + "  x = " + newX/10 + "  y = " + newY/10);
									}
								}
								

								if (Math.abs(newPhi - oldPhi) > tiny) {
									System.out.println("    new Phi = " + newPhi);
									oldPhi = newPhi;
								}
								
//								if (Math.abs(newRho - oldRho) > tiny) {
//									System.out.println("  new Rho = " + newRho/10); //mm to cm
//									oldRho = newRho;
//								}

								
								if (Math.abs(newZ - oldZ) > tiny) {
									System.out.println("new Z = " + newZ/10); //mm to cm
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

		} else {
			System.err.println("No files!");
		}
	}

	// process all the files
	private static void processAllFiles(ArrayList<File> files) throws IOException {
		if (!files.isEmpty()) {

			File bfile = new File(getDataDir(), "torus.binary");
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(bfile));

			int nPhi = 181;
			int nRho = 251;
			int nZ = 251;

			float phimin = 0.0f;
			float phimax = 360.0f;
			float rhomin = 0.0f;
			float rhomax = 500.0f;
			float zmin = 100.0f;
			float zmax = 600.0f;

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

			FloatVect[][][] bvals = new FloatVect[181][251][251];

			for (File file : files) {

				System.out.print(" PROCESSING FILE [" + file.getName() + "] zindex =  " + zIndex + "  ");

				try {

					AsciiReader ar = new AsciiReader(file, zIndex) {
						int count = 0;

						@Override
						protected void processLine(String line) {
							String tokens[] = AsciiReadSupport.tokens(line);
							if ((tokens != null) && (tokens.length == 7)) {

								int rhoIndex = count % 251;
								int phiIndex = count / 251;

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

			for (int iPHI = 0; iPHI < 181; iPHI++) {
				System.out.println("iPHI = " + iPHI);
				for (int iRHO = 0; iRHO < 251; iRHO++) {
					for (int iZ = 0; iZ < 251; iZ++) {
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

	public static void main(String arg[]) {
		String dataDir = getDataDir();
		System.out.println("data dir = [" + dataDir + "]");

//		try {
//			checkAllFiles(dataFiles(dataDir));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		 try {
		 processAllFiles(dataFiles(dataDir));
		 } catch (IOException e) {
		 e.printStackTrace();
		 }
		System.out.println("done");
	}

}
