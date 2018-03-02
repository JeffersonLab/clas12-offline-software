package cnuphys.ced.event.data;

import java.awt.Point;
import java.util.List;

public class Cluster {

	public byte sector;  //the sector
	public byte layer;  //the layer
	public float energy;  //energy in MeV
	public float time;  //energy in MeV
	public float x;       //x coordinate (CLAS system)  in cm
	public float y;       //y coordinate (CLAS system)  in cm
	public float z;       //z coordinate (CLAS system)  in cm
	
	//used for mouse over feedback
	private Point _screenLocation = new Point();

	
	/**
	 * @param sector the 1-based sector
	 * @param layer the layer
	 * @param energy energy in MeV
	 * @param time time
	 * @param x x coordinate (CLAS system)  in cm
	 * @param y y coordinate (CLAS system)  in cm
	 * @param z z coordinate (CLAS system)  in cm
	 */
	public Cluster(byte sector, byte layer, float energy, float time, float x, float y, float z) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.energy = energy;
		this.time = time;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * Add to a list of feedback strings, no doubt
	 * because the mouse is ovwer this hit
	 * @param v the list to add to
	 */
	public void getFeedbackStrings(String prefix, List<String> v) {
		if (v == null) {
			return;
		}
		
		String hitStr1 = String.format(prefix + " Cluster sect %d  layer %d ", sector, layer);
		v.add("$red$" + hitStr1);
		
		String hitStr2 = String.format("x %7.3fcm  y %7.3fcm  z %7.3fcm", x, y, z);
		v.add("$red$" + hitStr2);
		
		String hitStr3 = String.format("energy %6.3f   time %6.3f", energy, time);
		v.add("$red$" + hitStr3);
		
	}

	/**
	 * For feedback
	 * @param pp
	 */
	public void setLocation(Point pp) {
		_screenLocation.x = pp.x;
		_screenLocation.y = pp.y;
	}
	
	public boolean contains(Point pp) {
		return ((Math.abs(_screenLocation.x - pp.x) <= DataDrawSupport.HITHALF) &&
				(Math.abs(_screenLocation.y - pp.y) <= DataDrawSupport.HITHALF));
	}

	/**
	 * Get the azimuthal value of this hit
	 * @return the azimuthal value (phi) of this hit in degrees
	 */
	public double phi() {
		return Math.toDegrees(Math.atan2(y, x));
	}
	
	
}
