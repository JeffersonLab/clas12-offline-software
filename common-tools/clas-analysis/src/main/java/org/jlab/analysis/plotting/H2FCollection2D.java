package org.jlab.analysis.plotting;

import java.util.ArrayList;

/**
 * 
 * @author naharrison
 */
public class H2FCollection2D {
	public ArrayList<Double> binLimitsX;
	public ArrayList<Double> binLimitsY;
	public ArrayList<H2FCollection1D> histoCollections = new ArrayList<>();

	public H2FCollection2D(String name, int histBinsX, double histMinX, double histMaxX, int histBinsY, double histMinY, double histMaxY, ArrayList<Double> binLimitsX, ArrayList<Double> binLimitsY) {
		this.binLimitsX = binLimitsX;
		this.binLimitsY = binLimitsY;
		for(int k = 0; k < binLimitsY.size() - 1; k++)
		{
			histoCollections.add(new H2FCollection1D(name+k, histBinsX, histMinX, histMaxX, histBinsY, histMinY, histMaxY, binLimitsX));
		}
	}

	public ArrayList<H2FCollection1D> getListOfH2FCollection1Ds() {
		return histoCollections;
	}

	public void fill(double histValueX, double histValueY, double binningValueX, double binningValueY) {
		int binY = H1FCollection1D.getBinNumber(binningValueY, binLimitsY);
		if(binY >= 0) histoCollections.get(binY).fill(histValueX, histValueY, binningValueX);
	}
	
	public void setTitleX(String title) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setTitleX(title);
		}
	}

	public void setTitleY(String title) {
		for(int k = 0; k < histoCollections.size(); k++) {
			histoCollections.get(k).setTitleY(title);
		}
	}
}
