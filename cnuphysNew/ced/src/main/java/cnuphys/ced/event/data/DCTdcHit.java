package cnuphys.ced.event.data;

import java.util.List;

import cnuphys.lund.DoubleFormat;

public class DCTdcHit implements Comparable<DCTdcHit> {
	
	//for feedback strings
	private static final String _fbColor = "$Orange$";
	
	public byte sector;
	public byte layer; //1..36
	public short wire;
	public byte superlayer; //1..6
	public byte layer6; //1..6
	
	public int tdc = -1;
	
	//used to indicate that snr thinks it is noise
	public boolean noise;

//	bank name: [DC::doca] column name: [LR] full name: [DC::doca.LR] data type: byte
//	bank name: [DC::doca] column name: [doca] full name: [DC::doca.doca] data type: float
//	bank name: [DC::doca] column name: [sdoca] full name: [DC::doca.sdoca] data type: float
//	bank name: [DC::doca] column name: [time] full name: [DC::doca.time] data type: float
//	bank name: [DC::doca] column name: [stime] full name: [DC::doca.stime] data type: float

	//only in sim data
	public byte lr = -1;
	public float doca = Float.NaN;
	public float time = Float.NaN;
	public float sdoca = Float.NaN;
	public float stime = Float.NaN;
	

	public DCTdcHit(byte sector, byte layer, short wire, int tdc) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.wire = wire;
		this.tdc = tdc;
		
		//for convenience compute the 1..6 indices
		superlayer = (byte) (((layer - 1) / 6) + 1);
		layer6 = (byte) (((layer - 1) % 6) + 1);
	}
	
	/**
	 * Checks whether the indices are valid
	 * @return <code>true</code> if the hit has valid indices
	 */
	public boolean inRange() {
		return (valInRange(sector, 1, 6) && valInRange(layer, 1, 36) && valInRange(wire, 1, 112));
	}
	
	private boolean valInRange(int val, int min, int max) {
		return ((val >= min) && (val <= max));
	}
	
	public DCTdcHit(byte sector, byte layer, short wire, int tdc,
			byte lr, float doca, float sdoca, float time, float stime) {
		this(sector, layer, wire, tdc);
		this.doca = doca;
		this.sdoca = sdoca;
		this.time = time;
		this.stime = stime;
	}


	@Override
	public int compareTo(DCTdcHit hit) {
		int c = Integer.valueOf(sector).compareTo(Integer.valueOf(hit.sector));
		if (c == 0) {
			c = Integer.valueOf(layer).compareTo(Integer.valueOf(hit.layer));
			if (c == 0) {
				c = Integer.valueOf(wire).compareTo(Integer.valueOf(hit.wire));
			}
		}
		return c;
	}
	
	
	/**
	 * Get a string for just the tdc data
	 * @return a string for just the tdc data
	 */
	public String tdcString() {
		if (tdc < 0) {
			return "";
		}
		else {
			return "tdc " + tdc;
		}
	}
	
	
	@Override
	public String toString() {
		String s =  "sector = " + sector + " layer " + layer + 
				"  superlayer " + superlayer +
				"  layer6 " + layer6 +
				" wire: " + wire + " " + tdcString();
		
		String dStr = docaString(doca, time);
		if (dStr.length() > 3) {
			s += (" SIM (doca, time) " + dStr);
		}
		String sdStr = docaString(sdoca, stime);
		if (dStr.length() > 3) {
			s += (" SIM (sdoca, stime) " + sdStr);
		}
		
		return s;
	}
	
	//make a sensible doca string
	private String docaString(float d, float t) {
		if (Float.isNaN(d) || Float.isNaN(t)) {
			return "";
		}
		String dStr = DoubleFormat.doubleFormat(d, 3);
		String tStr = DoubleFormat.doubleFormat(t, 3);
		return "(" + dStr + " mm, " + tStr + ")";
	}
	
	/**
	 * Add to the feedback list
	 * @param showNoise if <code>true<code> add string for noise status
	 * @param showDoca if <code>true<code> add string for doca data (if present)
	 * @param feedbackStrings
	 */
	public void tdcAdcFeedback(boolean showNoise, boolean showDoca, List<String> feedbackStrings) {
		
		feedbackStrings.add(_fbColor  + "sector "
				+ sector + 
				" layer " + layer +
				" wire "  + wire);

		String tdcStr = tdcString();
		if (tdcStr.length() > 3) {
			feedbackStrings.add(_fbColor + tdcStr);
		}
		
		if (showNoise) {
			feedbackStrings.add(_fbColor + "Noise guess " + (noise ? "noise" : "not noise"));
		}
		
		if (showDoca) {
			String dstr = docaString(doca, time);
			if (dstr.length() > 3) {
				feedbackStrings.add(_fbColor + "SIM (doca, time) " + dstr);
			}
			String sdstr = docaString(sdoca, stime);
			if (dstr.length() > 3) {
				feedbackStrings.add(_fbColor + "SIM (sdoca, stime) " + sdstr);
			}
		}

	}
	

}