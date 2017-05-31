package cnuphys.ced.event.data;

public class Segment {

	//add more columns from the bank as needed
	public byte sector;
	public byte superlayer; //1..6
	public float x1;
	public float x2;
	public float z1;
	public float z2;
	
	public Segment(byte sector, byte superlayer, float x1, float z1, float x2, float z2) {
		super();
		this.sector = sector;
		this.superlayer = superlayer;
		this.x1 = x1;
		this.x2 = x2;
		this.z1 = z1;
		this.z2 = z2;
	}

}
