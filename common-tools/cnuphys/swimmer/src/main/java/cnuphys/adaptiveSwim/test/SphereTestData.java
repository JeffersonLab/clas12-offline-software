package cnuphys.adaptiveSwim.test;

import cnuphys.adaptiveSwim.geometry.Sphere;
import cnuphys.lund.AsciiReadSupport;

public class SphereTestData {

	
	//usual swim parameters
	int charge;
	public double xo;
	public double yo;
	public double zo;
	public double p;
	public double theta;
	public double phi;
	
	//radius
	public double r;
	
	public double accuracy;
	public double sMax;
	public double stepSize;
	public double cycleSize;
	
	public Sphere sphere;
	
	/**
	 * Will tokenize a string from the csv file
	 * @param s
	 */
	public SphereTestData(String s) {
		String tokens[] = AsciiReadSupport.tokens(s, ",");
		
		int index = 0;
		charge = Integer.parseInt(tokens[index++]);
		
		xo = Double.parseDouble(tokens[index++]);
		yo = Double.parseDouble(tokens[index++]);
		zo = Double.parseDouble(tokens[index++]);
		p = Double.parseDouble(tokens[index++]);
	    theta = Double.parseDouble(tokens[index++]);
		phi = Double.parseDouble(tokens[index++]);
		
		sMax = Double.parseDouble(tokens[index++]);
		stepSize = Double.parseDouble(tokens[index++]);
		cycleSize = Double.parseDouble(tokens[index++]);
		r = Double.parseDouble(tokens[index++]);
		
		//make accuracy same as uniform step size
		accuracy = stepSize;
		sphere = new Sphere(r);

	}
	

}
