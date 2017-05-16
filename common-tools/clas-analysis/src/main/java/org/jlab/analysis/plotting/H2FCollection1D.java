package org.jlab.analysis.plotting;

import java.util.ArrayList;

import org.jlab.groot.data.H2F;

/**
 * 
 * @author naharrison
 */
public class H2FCollection1D {
	public ArrayList<Double> binLimits;
	public ArrayList<H2F> histos = new ArrayList<>();
	
	public H2FCollection1D(String name, int histBinsX, double histMinX, double histMaxX, int histBinsY, double histMinY, double histMaxY, ArrayList<Double> binLimits) {
		this.binLimits = binLimits;
		for(int k = 0; k < binLimits.size() - 1; k++) {
			histos.add(new H2F(name+k, histBinsX, histMinX, histMaxX, histBinsY, histMinY, histMaxY));
			histos.get(k).setTitle(name + " (" + binLimits.get(k) + "-" + binLimits.get(k+1) + ")");
		}
	}
	
	public ArrayList<H2F> getListOfHistograms() {
		return histos;
	}
	
	public void fill(double histValueX, double histValueY, double binningValue) {
		int bin = getBinNumber(binningValue, binLimits);
		if(bin >= 0) histos.get(bin).fill(histValueX, histValueY);
	}
	
	public void setTitleX(String title) {
		for(int k = 0; k < histos.size(); k++) {
			histos.get(k).setTitleX(title);
		}
	}

	public void setTitleY(String title) {
		for(int k = 0; k < histos.size(); k++) {
			histos.get(k).setTitleY(title);
		}
	}
	
	public static int getBinNumber(double value, ArrayList<Double> limits) {
		if(limits.size() < 2) return -1;
		if(value < limits.get(0)) return -2;
		if(value >= limits.get(limits.size() - 1)) return -3;
		
		int result = 0;
		for(int k = 1; k < limits.size() - 1; k++) {
			if(value > limits.get(k)) result++;
			else break;
		}
		return result;
	}
}
