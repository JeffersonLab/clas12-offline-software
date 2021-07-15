package org.jlab.analysis.plotting;

import org.jlab.groot.ui.TCanvas;

/**
 * 
 * @author naharrison
 */
public class TCanvasP extends TCanvas {
	
	public int nColumns = 1;
	public int nRows = 1;

	public TCanvasP(String name, int xsize, int ysize) {
		super(name, xsize, ysize);
		setFontSizes(22);
	}
	
	public TCanvasP(String name, int xsize, int ysize, int nColumns, int nRows) {
		this(name, xsize, ysize);
		this.divide(nColumns, nRows);
	}
	
        @Override
	public TCanvasP divide(int nColumns, int nRows) {
		super.divide(nColumns, nRows);
		this.nColumns = nColumns;
		this.nRows = nRows;
		if(nColumns*nRows <= 9) setFontSizes(22);
		else if(nColumns*nRows > 9 && nColumns*nRows <= 16) setFontSizes(20);
		else if(nColumns*nRows > 16 && nColumns*nRows <= 25) setFontSizes(16);
		else setFontSizes(14);
            return this;
	}
	
	public void setFontSizes(int size) {
		this.getCanvas().setTitleSize(size);
		this.getCanvas().setAxisTitleSize(size);
		this.getCanvas().setAxisLabelSize(size);
	}
	
	public void cd(int column, int row) {
		super.cd(nColumns*(nRows - row - 1) + column);
	}
	
	public void draw(H1FCollection1D histos) {
		int n = histos.getListOfHistograms().size();
		int maxColumns = 8;
		int rows = (int) Math.ceil(((double) n)/((double) maxColumns));
		int columns;
		if(n <= maxColumns) columns = n;
		else columns = maxColumns;
		this.divide(columns, rows);
		for(int k = 0; k < n; k++) {
			this.cd(k);
			this.draw(histos.getListOfHistograms().get(k));
		}
	}

	public void draw(H1FCollection1D histos, String sameOpt) {
		int n = histos.getListOfHistograms().size();
		for(int k = 0; k < n; k++) {
			this.cd(k);
			this.draw(histos.getListOfHistograms().get(k), "same");
		}
	}
	
	public void draw(H1FCollection2D histos) {
		int nx = histos.getListOfH1FCollection1Ds().get(0).getListOfHistograms().size();
		int ny = histos.getListOfH1FCollection1Ds().size();
		this.divide(nx, ny);
		for(int x = 0; x < nx; x++) {
			for(int y = 0; y < ny; y++) {
				this.cd(x, y);
				this.draw(histos.getListOfH1FCollection1Ds().get(y).getListOfHistograms().get(x));
			}
		}
	}

	public void draw(H1FCollection2D histos, String sameOpt) {
		int nx = histos.getListOfH1FCollection1Ds().get(0).getListOfHistograms().size();
		int ny = histos.getListOfH1FCollection1Ds().size();
		for(int x = 0; x < nx; x++) {
			for(int y = 0; y < ny; y++) {
				this.cd(x, y);
				this.draw(histos.getListOfH1FCollection1Ds().get(y).getListOfHistograms().get(x), "same");
			}
		}
	}
	
	public void draw(H2FCollection1D histos) {
		int n = histos.getListOfHistograms().size();
		int maxColumns = 8;
		int rows = (int) Math.ceil(((double) n)/((double) maxColumns));
		int columns;
		if(n <= maxColumns) columns = n;
		else columns = maxColumns;
		this.divide(columns, rows);
		for(int k = 0; k < n; k++) {
			this.cd(k);
			this.draw(histos.getListOfHistograms().get(k));
		}
	}
	
		public void draw(H2FCollection2D histos) {
		int nx = histos.getListOfH2FCollection1Ds().get(0).getListOfHistograms().size();
		int ny = histos.getListOfH2FCollection1Ds().size();
		this.divide(nx, ny);
		for(int x = 0; x < nx; x++) {
			for(int y = 0; y < ny; y++) {
				this.cd(x, y);
				this.draw(histos.getListOfH2FCollection1Ds().get(y).getListOfHistograms().get(x));
			}
		}
	}
	
	public void update() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.getCanvas().update();
	}
}
