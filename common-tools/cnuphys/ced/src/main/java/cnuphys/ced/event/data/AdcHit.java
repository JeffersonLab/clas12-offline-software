package cnuphys.ced.event.data;

import java.awt.Point;
import java.util.List;

import cnuphys.lund.DoubleFormat;

public class AdcHit implements Comparable<AdcHit> {
	
	//used for mouse over feedback
	private Point _screenLocation = new Point();

	//for feedback strings
	private static final String _fbColor = "$Light Blue$";
	
	public final byte sector;
	public final byte layer;
	public final short component;
	public int adcL = -1;
	public int adcR = -1;
	
	public short pedL = -1;
	public short pedR = -1;
	public float timeL = Float.NaN;
	public float timeR = Float.NaN;
	
	

	public AdcHit(byte sector, byte layer, short component) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.component = component;
	}

	@Override
	public int compareTo(AdcHit hit) {
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
	    AdcHit other = (AdcHit) obj;
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
	
	//make a sensible doca string
	private String timeString() {
		if (Float.isNaN(timeL) && Float.isNaN(timeR)) {
			return "";
		}
		
		if (Float.isNaN(timeL)) {
			return "time " + DoubleFormat.doubleFormat(timeR, 3);
		}
		else if (Float.isNaN(timeR)) {
			return "time " + DoubleFormat.doubleFormat(timeL, 3);
		}
		else {
			return "time [" + DoubleFormat.doubleFormat(timeL, 3) +
					", " + DoubleFormat.doubleFormat(timeL, 3) + "]";
		}
		
	}

	
	/**
	 * Get a string for just the tdc data
	 * @return a string for just the tdc data
	 */
	private String valString(int valL, int valR, String name) {
		if ((valL < 0) && (valR < 0)) {
			return "";
		}
		else if ((valL >= 0) && (valR >= 0)) {
			return name + " [" + valL + ", " + valR + "]";
		}
		else if (valL >= 0) {
			return name + " " + valL;
		}
		else {
			return name + " " + valR;
		}
	}

	
	/**
	 * Get a string for just the ped data
	 * @return a string for just the ped data
	 */
	public String pedString() {
		return valString(pedL, pedR, "ped");
	}

	
	/**
	 * Get a string for just the tdc data
	 * @return a string for just the tdc data
	 */
	public String adcString() {
		return valString(adcL, adcR, "adc");
	}
	
	@Override
	public String toString() {
		return "sector = " + sector + " layer " + layer + 
				" component: " + component + " " + adcString() + 
				" " + pedString() + " " + timeString();
	}
	
	/**
	 * Add this hit to the feedback list
	 * @param feedbackStrings the list of strings
	 */
	public void tdcAdcFeedback(List<String> feedbackStrings) {
		tdcAdcFeedback("layer " + layer, "component", feedbackStrings);
	}
	
	/**
	 * Add this hit to the feedback list
	 * @param layerName a nice name for the layer
	 * @param componentName a nice name for the component
	 * @param feedbackStrings
	 */
	public void tdcAdcFeedback(String layerName, String componentName,
			List<String> feedbackStrings) {
		
		feedbackStrings.add(_fbColor +
				"sector " + sector + 
				" " + layerName +
				"  " + componentName + " " + component);

		String adcStr = adcString();
		if (adcStr.length() > 3) {
			feedbackStrings.add(_fbColor + adcStr);
		}
		
		String pedStr = pedString();
		String timeStr = timeString();
		String data2Str = pedStr + " " + timeStr;
		
		if (data2Str.length() > 3) {
			feedbackStrings.add(_fbColor + data2Str);
		}

	}
	
	/**
	 * For feedback
	 * @param pp
	 */
	public void setLocation(Point pp) {
		_screenLocation.x = pp.x;
		_screenLocation.y = pp.y;
	}
	
	public boolean contains(Point pp) {
		return ((Math.abs(_screenLocation.x - pp.x) <= DataDrawSupport.HITHALF) &&
				(Math.abs(_screenLocation.y - pp.y) <= DataDrawSupport.HITHALF));
	}


}
