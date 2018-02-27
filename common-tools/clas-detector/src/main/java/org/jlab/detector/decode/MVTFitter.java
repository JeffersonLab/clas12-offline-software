package org.jlab.detector.decode;

/**
 * Fit BMT / FMT Pulse
 * @author guillaum
 * @version 1
 */
public class MVTFitter {

	public int    binMax;   //Bin of the max ADC over the pulse
  public int binOffset;   //Offset due to sparse sample
	public double adcMax;   //Max value of ADC over the pulse (fitted)
	public double timeMax;  //Time of the max ADC over the pulse (fitted)
	public double integral; //Sum of ADCs over the pulse (not fitted)
	public long timestamp;
	
	private short[] pulseArrayCorr; //Pulse after offset correction
	private int binNumber;  //Number of bins in one pulse
	
	/**
	 * Fit takes an ADC pulse and DAQ parameters and compute interesting properties of the pulse
	 * @param adcOffset : hardware offset used to avoid signal to be negative after (hardware) common mode node suppression 
	 * @param fineTimeStampResolution : precision of dream clock (usually 8)
	 * @param samplingTime : time between 2 ADC bins
	 * @param pulseArray : pulse = array containing the ADC values
	 * @param timeStamp : timing informations (used to make fine corrections)
	 */
    public void fit(short adcOffset, double fineTimeStampResolution, double samplingTime, short[] pulseArray, long timeStamp, int sparseSample) {
    pulseCorrection(adcOffset, samplingTime, pulseArray, sparseSample);
		fitParabolic(samplingTime);
		fineTimeStampCorrection(timeStamp, fineTimeStampResolution);
	}
	
	/**
	 * This method subtract offset from ADCs, compute a first value for the max ADC, bin of max, time of max and integral of pulse
	 * @param adcOffset : hardware offset used to avoid signal to be negative after (hardware) common mode node suppression 
	 * @param samplingTime : time between 2 ADC bins
	 * @param pulseArray : pulse = array containing the ADC values
	 */
    private void pulseCorrection(short adcOffset, double samplingTime, short[] pulseArray, int sparseSample) {
		binNumber = pulseArray.length;
		binMax = 0;
		adcMax = (short) (pulseArray[0]-adcOffset);
		integral = 0;
		pulseArrayCorr = new short[binNumber];
		for (int bin = 0 ; bin < binNumber ; bin ++){
			pulseArrayCorr[bin] = (short) (pulseArray[bin] - adcOffset);
			integral = integral + pulseArrayCorr[bin];
			if (pulseArrayCorr[bin]>adcMax){
				adcMax = pulseArrayCorr[bin];
				binMax = bin;
			}
		}
    binOffset = sparseSample * binMax;
    timeMax = (binMax+binOffset) * samplingTime;
	}
	
	/**
	 * Fit the max of the pulse using parabolic fit, this method updates the timeMax and adcMax values
	 * @param samplingTime : time between 2 ADC bins
	 */
	private void fitParabolic(double samplingTime) {
		if (binMax < (binNumber-1) && binMax > 0) {
			int y1 = pulseArrayCorr[binMax - 1];
			int y2 = pulseArrayCorr[binMax];
			int y3 = pulseArrayCorr[binMax + 1];
			int x1 = binMax - 1 + binOffset - 1;
			int x2 = binMax + binOffset;
			int x3 = binMax + 1 + binOffset + 1;
			double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
			double A = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
			double B = (x3 * x3 * (y1 - y2) + x2 * x2 * (y3 - y1) + x1 * x1 * (y2 - y3)) / denom;
			double C = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
			double xv = -B / (2 * A);
			double yv = C - B * B / (4 * A);
			timeMax = xv * samplingTime;
			adcMax = yv;
		}
	}
	
	/**
	 * Make fine timestamp correction (using dream (=electronic chip) clock)
	 * @param timeStamp : timing informations (used to make fine corrections)
	 * @param fineTimeStampResolution : precision of dream clock (usually 8)
	 */
	private void fineTimeStampCorrection (long timeStamp, double fineTimeStampResolution) {
		this.timestamp = timeStamp;
		String binaryTimeStamp = Long.toBinaryString(timeStamp); //get 64 bit timestamp in binary format
            //byte fineTimeStamp = Byte.parseByte(binaryTimeStamp.substring(binaryTimeStamp.length()-3,binaryTimeStamp.length()),2); //fineTimeStamp : keep and convert last 3 bits of binary timestamp
            //timeMax += (double) ((fineTimeStamp+0.5) * fineTimeStampResolution); //fineTimeStampCorrection
            //added condition
            if (binaryTimeStamp.length()>=3){
                byte fineTimeStamp = Byte.parseByte(binaryTimeStamp.substring(binaryTimeStamp.length()-3,binaryTimeStamp.length()),2); //fineTimeStamp : keep and convert last 3 bits of binary timestamp
                timeMax += (double) ((fineTimeStamp+0.5) * fineTimeStampResolution); //fineTimeStampCorrection
            }
        }
	
}
