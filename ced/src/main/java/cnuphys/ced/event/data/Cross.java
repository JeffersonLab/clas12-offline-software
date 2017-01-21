package cnuphys.ced.event.data;

public class Cross {

	public byte sector;
	public byte region;
	public short id;
	public float x;
	public float y;
	public float z;
	public float ux;
	public float uy;
	public float uz;
	public float err_x;
	public float err_y;
	public float err_z;
	public float err_ux;
	public float err_uy;
	public float err_uz;
	
	public Cross(byte sector, byte region, short id, float x, float y, float z, float ux, float uy, float uz,
			float err_x, float err_y, float err_z, float err_ux, float err_uy, float err_uz) {
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
		this.err_ux = err_ux;
		this.err_uy = err_uy;
		this.err_uz = err_uz;
	}

}
