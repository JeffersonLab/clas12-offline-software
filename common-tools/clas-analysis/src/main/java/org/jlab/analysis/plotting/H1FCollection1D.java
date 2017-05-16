package org.jlab.analysis.plotting;

import java.util.ArrayList;

import org.jlab.groot.data.H1F;

/**
 * 
 * @author naharrison
 */
public class H1FCollection1D {
	public ArrayList<Double> binLimits;
	public ArrayList<H1F> histos = new ArrayList<>();
	
	public H1FCollection1D(String name, int histBins, double histMin, double histMax, ArrayList<Double> binLimits) {
		this.binLimits = binLimits;
		for(int k = 0; k < binLimits.size() - 1; k++) {
			histos.add(new H1F(name+k, histBins, histMin, histMax));
			histos.get(k).setTitle(name + " (" + binLimits.get(k) + "-" + binLimits.get(k+1) + ")");
			histos.get(k).setLineWidth(2);
		}
	}
	
	public ArrayList<H1F> getListOfHistograms() {
		return histos;
	}
	
	public void fill(double histValue, double binningValue) {
		int bin = getBinNumber(binningValue, binLimits);
		if(bin >= 0) histos.get(bin).fill(histValue);
	}
	
	public void setTitleX(String title) {
		for(int k = 0; k < histos.size(); k++) {
			histos.get(k).setTitleX(title);
		}
	}

	public void setLineColor(int color) {
		for(int k = 0; k < histos.size(); k++) {
			histos.get(k).setLineColor(color);
		}
	}

	public void setLineWidth(int width) {
		for(int k = 0; k < histos.size(); k++) {
			histos.get(k).setLineWidth(width);
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
