package org.jlab.analysis.plotting;

import java.util.ArrayList;

/**
 * 
 * @author naharrison
 */
public class H1FCollection2D {
	public ArrayList<Double> binLimitsX;
	public ArrayList<Double> binLimitsY;
	public ArrayList<H1FCollection1D> histoCollections = new ArrayList<>();

	public H1FCollection2D(String name, int histBins, double histMin, double histMax, ArrayList<Double> binLimitsX, ArrayList<Double> binLimitsY) {
		this.binLimitsX = binLimitsX;
		this.binLimitsY = binLimitsY;
		for(int k = 0; k < binLimitsY.size() - 1; k++)
		{
			histoCollections.add(new H1FCollection1D(name+k, histBins, histMin, histMax, binLimitsX));
		}
	}

	public ArrayList<H1FCollection1D> getListOfH1FCollection1Ds() {
		return histoCollections;
	}

	public void fill(double histValue, double binningValueX, double binningValueY) {
		int binY = H1FCollection1D.getBinNumber(binningValueY, binLimitsY);
		if(binY >= 0) histoCollections.get(binY).fill(histValue, binningValueX);
	}
	
	public void setTitleX(String title) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setTitleX(title);
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
