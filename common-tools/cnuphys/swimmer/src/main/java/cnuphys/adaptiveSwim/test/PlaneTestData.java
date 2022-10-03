package cnuphys.adaptiveSwim.test;

import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Sphere;
import cnuphys.lund.AsciiReadSupport;

public class PlaneTestData {
	
	//usual swim parameters
	int charge;
	public double xo;
	public double yo;
	public double zo;
	public double p;
	public double theta;
	public double phi;
	
	//for making plane
	public double nx;
	public double ny;
	public double nz;
	public double px;
	public double py;
	public double pz;

	
	public double accuracy;
	public double sMax;
	public double stepSize;
	
	public Plane plane;
	
	/**
	 * Will tokenize a string from the csv file
	 * @param s
	 */
	public PlaneTestData(String s) {
		String tokens[] = AsciiReadSupport.tokens(s, ",");

		int index = 0;

		try {
			charge = Integer.parseInt(tokens[index++]);

			xo = Double.parseDouble(tokens[index++]);
			yo = Double.parseDouble(tokens[index++]);
			zo = Double.parseDouble(tokens[index++]);
			p = Double.parseDouble(tokens[index++]);
			theta = Double.parseDouble(tokens[index++]);
			phi = Double.parseDouble(tokens[index++]);

			nx = Double.parseDouble(tokens[index++]);
			ny = Double.parseDouble(tokens[index++]);
			nz = Double.parseDouble(tokens[index++]);
			px = Double.parseDouble(tokens[index++]);
			py = Double.parseDouble(tokens[index++]);
			pz = Double.parseDouble(tokens[index++]);

			accuracy = Double.parseDouble(tokens[index++]);
			sMax = Double.parseDouble(tokens[index++]);
			stepSize = Double.parseDouble(tokens[index++]);

			plane = new Plane(nx, ny, nz, px, py, pz);
		} catch (ArrayIndexOutOfBoundsException e) {
			
			System.out.println("OOB Bad Line in planedata: [" + s + "]");
			System.exit(1);

		}catch (NumberFormatException e) {
			
			System.out.println("NFE Bad Line in planedata: [" + s + "]");
			System.exit(1);

		}

	}
	

}
