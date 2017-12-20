package cnuphys.ced.event.data;

import java.awt.Point;

public class Cross2 {

	public int sector;
	public int region;
	public int id;
	public float x;
	public float y;
	public float z;
	public float ux;
	public float uy;
	public float uz;
	public float err_x;
	public float err_y;
	public float err_z;
	
	//used for mouse over feedback
	private Point _screenLocation = new Point();
	
	public Cross2(int sector, int region, int id, float x, float y, float z, float ux, float uy, float uz,
			float err_x, float err_y, float err_z) {
		super();
		this.sector = sector;
		this.region = region;
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.ux = ux;
		this.uy = uy;
		this.uz = uz;
		this.err_x = err_x;
		this.err_y = err_y;
		this.err_z = err_z;
	}
	
	public boolean isFullLocationBad() {
		return Float.isNaN(x) || Float.isNaN(y) ||
				Float.isNaN(z);
	}
	
	public boolean isXYLocationBad() {
		return Float.isNaN(x) || Float.isNaN(y);
	}

	
	public boolean isDirectionBad() {
		return Float.isNaN(ux) ||
				Float.isNaN(uy) || Float.isNaN(uz);
	}

	public boolean isErrorBad() {
		return Float.isNaN(err_x) ||
				Float.isNaN(err_y) || Float.isNaN(err_z);
	}	
	
	public void setLocation(Point pp) {
		_screenLocation.x = pp.x;
		_screenLocation.y = pp.y;
	}
	
	public boolean contains(Point pp) {
		return ((Math.abs(_screenLocation.x - pp.x) <= DataDrawSupport.CROSSHALF) &&
				(Math.abs(_screenLocation.y - pp.y) <= DataDrawSupport.CROSSHALF));
	}

}
