package org.jlab.analysis.plotting;

import java.util.ArrayList;

/**
 * 
 * @author naharrison
 */
public class H1FCollection3D {
	public ArrayList<Double> binLimitsX;
	public ArrayList<Double> binLimitsY;
	public ArrayList<Double> binLimitsZ;
	public ArrayList<H1FCollection2D> histoCollections = new ArrayList<>();
	
	public H1FCollection3D(String name, int histBins, double histMin, double histMax, ArrayList<Double> binLimitsX, ArrayList<Double> binLimitsY, ArrayList<Double> binLimitsZ) {
		this.binLimitsX = binLimitsX;
		this.binLimitsY = binLimitsY;
		this.binLimitsZ = binLimitsZ;
		for(int k = 0; k < binLimitsZ.size() - 1; k++) {
			histoCollections.add(new H1FCollection2D(name+k, histBins, histMin, histMax, binLimitsX, binLimitsY));
		}
	}
	
	public ArrayList<H1FCollection2D> getListOfH1FCollection2Ds() {
		return histoCollections;
	}
	
	public void fill(double histValue, double binningValueX, double binningValueY, double binningValueZ) {
		int binZ = H1FCollection1D.getBinNumber(binningValueZ, binLimitsZ);
		if(binZ >= 0) histoCollections.get(binZ).fill(histValue, binningValueX, binningValueY);
	}
	
	public void setTitleX(String name) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setTitleX(name);
		}
	}

	public void setLineColor(int color) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setLineColor(color);
		}
	}
	
	public void setLineWidth(int width) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setLineWidth(width);
		}
	}
	
}
