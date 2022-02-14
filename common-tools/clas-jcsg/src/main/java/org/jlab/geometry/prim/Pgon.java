package org.jlab.geometry.prim;

import java.util.ArrayList;
import java.util.List;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;

/**
 * @author pdavies/devita
 */

// FIXME: currently support only polyheadra defintion to gemc geometry
public class Pgon implements Primitive {
	
	private final PropertyStorage properties = new PropertyStorage();
	private int numSides, numZPlanes;
	private double phiStart, phiTotal;
         double[] zPlane;
         double[] rInner;
         double[] rOuter;	
                     
	public Pgon(double   phiStart,
                     double   phiTotal,
                     int      numSides,
                     int      numZPlanes,
                     double[] zPlane,
                     double[] rInner,
                     double[] rOuter)
	{
              if( numSides < 0 || numZPlanes < 0 || phiStart < 0 || phiTotal <= 0 ) {
                   throw new IllegalArgumentException("Illegal arguments for Polyhedra Primitive!");
              }
              if( zPlane.length<2 || rInner.length<2 || rOuter.length<2) {
                   throw new IllegalArgumentException("Illegal arguments for Polyhedra Primitive!");
              }
              if( zPlane.length!=rInner.length || zPlane.length!=rOuter.length) {
                   throw new IllegalArgumentException("Illegal arguments for Polyhedra Primitive!");
              }
		
              this.numSides = numSides;
	     this.phiStart = phiStart;
	     this.phiTotal = phiTotal;
              this.zPlane = zPlane;
              this.rInner = rInner;
              this.rOuter = rOuter;
	}

	@Override
	public List<Polygon> toPolygons()
	{
		List<Polygon> polygons = new ArrayList<>();
        // just returns something to not cause a NullPointerException
        return polygons;
	}

	@Override
	public PropertyStorage getProperties()
	{		
		return null;
	}
        
}
