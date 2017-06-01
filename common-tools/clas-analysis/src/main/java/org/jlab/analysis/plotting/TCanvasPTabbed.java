package org.jlab.analysis.plotting;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;

/**
*
* @author naharrison
*/
public class TCanvasPTabbed extends JFrame {
	
    JPanel framePane = null;
    EmbeddedCanvasTabbed emCanvasT = null; 


    public TCanvasPTabbed(String name, int xsize, int ysize) {
    	super();
        this.setTitle(name);
        this.setSize(xsize, ysize);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        emCanvasT = new EmbeddedCanvasTabbed(true);
        
        framePane = new JPanel();
        framePane.setLayout(new BorderLayout());
        framePane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(4, 4, 4, 4), new EtchedBorder()));
        
        framePane.add(emCanvasT, BorderLayout.CENTER);
        
        this.add(framePane);
        this.setVisible(true);
    }


    public EmbeddedCanvasTabbed getEmbeddedCanvasTabbed() {
    	return emCanvasT;
    }
    
    
    public void addTab(String name) {
    	emCanvasT.addCanvas(name);
    }


    public EmbeddedCanvas getTab(String name) {
    	return emCanvasT.getCanvas(name);
    }

    
    public void draw(H1FCollection3D histos) {
    	int nx = histos.getListOfH1FCollection2Ds().get(0).getListOfH1FCollection1Ds().get(0).getListOfHistograms().size();
    	int ny = histos.getListOfH1FCollection2Ds().get(0).getListOfH1FCollection1Ds().size();
    	int nz = histos.getListOfH1FCollection2Ds().size();
    	
    	for(int iz = 0; iz < nz; iz++) {
    		emCanvasT.addCanvas(String.format("tab%d", iz));
    		emCanvasT.getCanvas(String.format("tab%d", iz)).divide(nx, ny);
    		emCanvasT.getCanvas(String.format("tab%d", iz)).setTitleSize(16);
    		emCanvasT.getCanvas(String.format("tab%d", iz)).setAxisTitleSize(16);
    		emCanvasT.getCanvas(String.format("tab%d", iz)).setAxisLabelSize(16);
    		for(int iy = 0; iy < ny; iy++) {
    			for(int ix = 0; ix < nx; ix++) {
    				H1F hh = histos.getListOfH1FCollection2Ds().get(iz).getListOfH1FCollection1Ds().get(iy).getListOfHistograms().get(ix);
    				emCanvasT.getCanvas(String.format("tab%d", iz)).cd(nx*(ny - iy - 1) + ix);
    				emCanvasT.getCanvas(String.format("tab%d", iz)).draw(hh);
    			}
    		}
    	}
    }


    public void draw(H1FCollection3D histos, String sameOpt) {
    	int nx = histos.getListOfH1FCollection2Ds().get(0).getListOfH1FCollection1Ds().get(0).getListOfHistograms().size();
    	int ny = histos.getListOfH1FCollection2Ds().get(0).getListOfH1FCollection1Ds().size();
    	int nz = histos.getListOfH1FCollection2Ds().size();
    	
    	for(int iz = 0; iz < nz; iz++) {
    		for(int iy = 0; iy < ny; iy++) {
    			for(int ix = 0; ix < nx; ix++) {
    				H1F hh = histos.getListOfH1FCollection2Ds().get(iz).getListOfH1FCollection1Ds().get(iy).getListOfHistograms().get(ix);
    				emCanvasT.getCanvas(String.format("tab%d", iz)).cd(nx*(ny - iy - 1) + ix);
    				emCanvasT.getCanvas(String.format("tab%d", iz)).draw(hh, "same");
    			}
    		}
    	}
    }
    
}
