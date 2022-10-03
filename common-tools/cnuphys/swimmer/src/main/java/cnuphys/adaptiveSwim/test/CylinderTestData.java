package cnuphys.adaptiveSwim.test;

import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.lund.AsciiReadSupport;

/**
 * Used to read swimCylinder test data from a csv file
 * all lengths in meters, all angles in degrees
 * @author heddle
 *
 */
public class CylinderTestData {
	
	//usual swim parameters
	int charge;
	public double xo;
	public double yo;
	public double zo;
	public double p;
	public double theta;
	public double phi;
	
	//centerline points
	public double CLP1[] = new double[3];
	public double CLP2[] = new double[3];
	
	//radius
	public double r;
	
	public double accuracy;
	public double sMax;
	public double stepSize;
	
	public Cylinder cylinder;
	
	/**
	 * Will tokenize a string from the csv file
	 * @param s
	 */
	public CylinderTestData(String s) {
		String tokens[] = AsciiReadSupport.tokens(s, ",");
		
		int index = 0;
		charge = Integer.parseInt(tokens[index++]);
		
		xo = Double.parseDouble(tokens[index++]);
		yo = Double.parseDouble(tokens[index++]);
		zo = Double.parseDouble(tokens[index++]);
		p = Double.parseDouble(tokens[index++]);
	    theta = Double.parseDouble(tokens[index++]);
		phi = Double.parseDouble(tokens[index++]);
		
		CLP1[0] = Double.parseDouble(tokens[index++]);
		CLP1[1] = Double.parseDouble(tokens[index++]);
		CLP1[2] = Double.parseDouble(tokens[index++]);
		
		CLP2[0] = Double.parseDouble(tokens[index++]);
		CLP2[1] = Double.parseDouble(tokens[index++]);
		CLP2[2] = Double.parseDouble(tokens[index++]);
		
		r = Double.parseDouble(tokens[index++]);
		
		accuracy = Double.parseDouble(tokens[index++]);
		sMax = Double.parseDouble(tokens[index++]);
		stepSize = Double.parseDouble(tokens[index++]);

        cylinder = new Cylinder(CLP1, CLP2, r);

	}
	


}
