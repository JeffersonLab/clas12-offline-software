package cnuphys.ced.event.data;

public class DCHit implements Comparable<DCHit> {

	public byte sector;
	public byte superlayer;
	public byte layer6;
	public short wire;
	public short id;
	public short status;
	public float time;
	public float doca;
	
	
	
	public DCHit(byte sector, byte superlayer, byte layer6, short wire, short id, short status, float time, float doca) {
		super();
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer6 = layer6;
		this.wire = wire;
		this.id = id;
		this.status = status;
		this.time = time;
		this.doca = doca;
	}
	
	@Override
	public int compareTo(DCHit hit) {
		int c = Integer.valueOf(sector).compareTo(Integer.valueOf(hit.sector));
		if (c == 0) {
			c = Integer.valueOf(superlayer).compareTo(Integer.valueOf(hit.superlayer));
			if (c == 0) {
				c = Integer.valueOf(layer6).compareTo(Integer.valueOf(hit.layer6));
				if (c == 0) {
					c = Integer.valueOf(wire).compareTo(Integer.valueOf(hit.wire));
				}
			}
		}
		return c;
	}


}
