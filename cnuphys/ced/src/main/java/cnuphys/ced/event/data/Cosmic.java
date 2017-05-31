package cnuphys.ced.event.data;

public class Cosmic {

	
	public int ID;
	public float chi2;
	public float phi;
	public float theta;
	
	public float trkline_yx_interc;
	public float trkline_yx_slope;
	public float trkline_yz_interc;
	public float trkline_yz_slope;
	
	public Cosmic(int id, float chi2, float phi, float theta, float trkline_yx_interc, float trkline_yx_slope,
			float trkline_yz_interc, float trkline_yz_slope) {
		super();
		ID = id;
		this.chi2 = chi2;
		this.phi = phi;
		this.theta = theta;
		this.trkline_yx_interc = trkline_yx_interc;
		this.trkline_yx_slope = trkline_yx_slope;
		this.trkline_yz_interc = trkline_yz_interc;
		this.trkline_yz_slope = trkline_yz_slope;
	}


}
