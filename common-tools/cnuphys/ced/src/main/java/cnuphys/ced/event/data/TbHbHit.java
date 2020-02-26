package cnuphys.ced.event.data;

/**
 * This is not the basic TdcAdc hit, this is the base for Hit Based and Time based hits.
 * @author heddle
 *
 */
public class TbHbHit implements Comparable<TbHbHit> {

	public byte sector;
	public byte superlayer;
	public byte layer6;
	public short wire;
	public short id;
	public short status;
	public int TDC;
	public float doca;
	public float trkDoca;
	
	
	
	public TbHbHit(byte sector, byte superlayer, byte layer6, short wire, short id, short status, int tdc, float doca, float trkDoca) {
		super();
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer6 = layer6;
		this.wire = wire;
		this.id = id;
		this.status = status;
		this.TDC = tdc;
//		this.time = time;
		this.doca = doca;
		this.trkDoca = trkDoca;
	}
	
	//used for indexing and binary search
	public TbHbHit(byte sector, byte superlayer, byte layer6, short wire) {
		this(sector, superlayer, layer6, wire, (short)(-1), (short)(-1), -1, -1f, -1f);
	}

	
	@Override
	public int compareTo(TbHbHit hit) {
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
