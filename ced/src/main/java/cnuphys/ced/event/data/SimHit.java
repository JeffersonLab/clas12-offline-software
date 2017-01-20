package cnuphys.ced.event.data;

public class SimHit implements Comparable<SimHit> {
	
	public byte sector;
	public byte layer;
	public short component;
	public int tdcL = -1;
	public int tdcR = -1;
	public int adcL = -1;
	public int adcR = -1;
	

	public SimHit(byte sector, byte layer, short component) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.component = component;
	}

	@Override
	public int compareTo(SimHit hit) {
		int c = Integer.valueOf(sector).compareTo(Integer.valueOf(hit.sector));
		if (c == 0) {
			c = Integer.valueOf(layer).compareTo(Integer.valueOf(hit.layer));
			if (c == 0) {
				c = Integer.valueOf(component).compareTo(Integer.valueOf(hit.component));
			}
		}
		return c;
	}
	public String tdcString() {
		if ((tdcL < 0) && (tdcR < 0)) {
			return "";
		}
		if ((tdcL >= 0) && (tdcR >= 0)) {
			return " tdc: [" + tdcL + ", " + tdcR + "]";
		}
		if ((tdcL >= 0) && (tdcR < 0)) {
			return " tdc: " + tdcL;
		}
		else {
			return " tdc: " + tdcR;
		}
	}

	
	public String adcString() {
		if ((adcL < 0) && (adcR < 0)) {
			return "";
		}
		if ((adcL >= 0) && (adcR >= 0)) {
			return " adc: [" + adcL + ", " + adcR + "]";
		}
		if ((adcL >= 0) && (adcR < 0)) {
			return " adc: " + adcL;
		}
		else {
			return " adc: " + adcR;
		}
	}
	
	@Override
	public String toString() {
		return "sector = " + sector + " layer " + layer + 
				" component: " + component + tdcString() + adcString();
	}
}
