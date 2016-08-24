package org.jlab.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jlab.geom.base.Component;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

public class GetHits {

	public GetHits() {
		// TODO Auto-generated constructor stub
	}
     
	 public static List<DCWire>  initWires(DataEvent event, 
	            Detector detector, HashMap<String, double[][][]> calibConstants){
	                
	        List<DCWire>  wires = new ArrayList<DCWire>();
	        
	        if(event.hasBank("DC::dgtz")==true){
	            EvioDataBank dcBank = (EvioDataBank) event.getBank("DC::dgtz");
	            int nrows = dcBank.rows();
	            //System.out.println(" BANK DC loaded with ROWS = " + nrows);
	            for(int row = 0; row < nrows; row++){
	                
	                int sector = dcBank.getInt("sector", row);
	                int superlayer  = dcBank.getInt("superlayer", row);
	                int layer   = dcBank.getInt("layer", row);
	                int component  = dcBank.getInt("wire", row);
	                
	                DCWire wire = new DCWire(
	                        sector, superlayer, layer, component
	                );
	                
	                wire.set_Id(row+1);
	                wire.setTDC(dcBank.getInt("TDC", row));
	                if(dcBank.getDouble("stime", row)>0) { 
	                	wire.setGEMCTime(dcBank.getDouble("stime", row));
	                }
	                // get the midpoint of the wire from geometry
	                Component wireComp = detector.getSector(0).getSuperlayer(superlayer-1).getLayer(layer-1).getComponent(component-1);	                
	                Point3D midPointWire = wireComp.getMidpoint();
	                wire.setMidPoint(midPointWire);
	                // set the wireLine
	                double stereoAngle = -6.0;
	                if(superlayer%2==1)
	                	stereoAngle = 6.0;
	                Vector3D wireDir = new Vector3D(Math.cos(Math.toDegrees(stereoAngle)), Math.sin(Math.toDegrees(stereoAngle)), 0);
	                Line3D wireLine = new Line3D(midPointWire, wireDir);
	                wire.setWireLine(wireLine);
	                // get the cell size 
	        		double layerDiffAtMPln  = detector.getSector(0).getSuperlayer(superlayer-1).getLayer(0).getComponent(0).getMidpoint().x()
	        	                     - detector.getSector(0).getSuperlayer(superlayer-1).getLayer(0).getComponent(1).getMidpoint().x();
	        		
	        		double cellSize = 0.5*Math.abs(layerDiffAtMPln);
	        		wire.set_CellSize(cellSize);
	                // get the resolution parameters for the doca uncertainty calculation
	                if(calibConstants!=null){
	                	wire.setResolutionsPars(calibConstants.get("resolution"));
	                	
	                }
	                wires.add(wire);
	            }
	        }
	        
	        return wires;
	    }
	 
}
