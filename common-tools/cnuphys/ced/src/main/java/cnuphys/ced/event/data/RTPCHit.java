package cnuphys.ced.event.data;

public class RTPCHit implements Comparable<RTPCHit> {

	public final byte layer;
	public final short component;
	public final int adc;

	public final short ped;
	public final float time;

	public RTPCHit(byte layer, short component, int adc, short ped, float time) {
		super();
		this.layer = layer;
		this.component = component;
		this.adc = adc;
		this.ped = ped;
		this.time = time;
	}
	
	@Override
	public int compareTo(RTPCHit hit) {
			int c = Integer.valueOf(layer).compareTo(Integer.valueOf(hit.layer));
			if (c == 0) {
				c = Integer.valueOf(component).compareTo(Integer.valueOf(hit.component));
			}
		return c;
	}


}
