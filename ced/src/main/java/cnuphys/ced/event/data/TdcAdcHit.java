package cnuphys.ced.event.data;

import java.util.List;

public class TdcAdcHit implements Comparable<TdcAdcHit> {
	
	//for feedback strings
	private static final String _fbColor = "$Orange Red$";
	
	public byte sector;
	public byte layer;
	public short component;
	public int tdcL = -1;
	public int tdcR = -1;
	public int adcL = -1;
	public int adcR = -1;
	

	public TdcAdcHit(byte sector, byte layer, short component) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.component = component;
	}

	@Override
	public int compareTo(TdcAdcHit hit) {
		int c = Integer.valueOf(sector).compareTo(Integer.valueOf(hit.sector));
		if (c == 0) {
			c = Integer.valueOf(layer).compareTo(Integer.valueOf(hit.layer));
			if (c == 0) {
				c = Integer.valueOf(component).compareTo(Integer.valueOf(hit.component));
			}
		}
		return c;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    TdcAdcHit other = (TdcAdcHit) obj;
	    if (sector != other.sector)
	        return false;
	    if (layer != other.layer)
	        return false;
	    if (component != other.component)
	        return false;
	    return true;
	}
	
	/**
	 * Get the average ADC value
	 * @return the average of left and right
	 */
	public int averageADC() {
		int count = 0;
		int sum = 0;
		if (adcL >= 0) {
			count++;
			sum += adcL;
		}
		if (adcR >= 0) {
			count++;
			sum += adcR;
		}
		if (count == 0) {
			return -1;
		}
		
		return sum/count;
	}
	
	/**
	 * Get a string for just the tdc data
	 * @return a string for just the tdc data
	 */
	public String tdcString() {
		if ((tdcL < 0) && (tdcR < 0)) {
			return "";
		}
		if ((tdcL >= 0) && (tdcR >= 0)) {
			return "tdc: [" + tdcL + ", " + tdcR + "]";
		}
		if ((tdcL >= 0) && (tdcR < 0)) {
			return "tdc: " + tdcL;
		}
		else {
			return "tdc: " + tdcR;
		}
	}

	
	/**
	 * Get a string for just the tdc data
	 * @return a string for just the tdc data
	 */
	public String adcString() {
		if ((adcL < 0) && (adcR < 0)) {
			return "";
		}
		if ((adcL >= 0) && (adcR >= 0)) {
			return "adc: [" + adcL + ", " + adcR + "]";
		}
		if ((adcL >= 0) && (adcR < 0)) {
			return "adc: " + adcL;
		}
		else {
			return "adc: " + adcR;
		}
	}
	
	@Override
	public String toString() {
		return "sector = " + sector + " layer " + layer + 
				" component: " + component + " " + tdcString() + " " + adcString();
	}
	
	public void tdcAdcFeedback(List<String> feedbackStrings) {
		tdcAdcFeedback("layer " + layer, "component", feedbackStrings);
	}
	
	public void tdcAdcFeedback(String layerName, String componentName,
			List<String> feedbackStrings) {
		
		feedbackStrings.add(_fbColor +
				"sector " + sector + 
				" " + layerName +
				"  " + componentName + " " + component);

		String tdcStr = tdcString();
		String adcStr = adcString();
		String dataStr = tdcStr + " " + adcStr;
		if (dataStr.length() > 3) {
			feedbackStrings.add(_fbColor + dataStr);
		}

	}
	

}
