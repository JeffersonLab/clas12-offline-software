package org.jlab.geometry.prim;

import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;

/**
 * @author pdavies
 */

public class Tube implements Primitive {

	private final PropertyStorage properties = new PropertyStorage();
	private double pDr1, pDr2, pDz, pPhi, pDphi;
	
	public Tube( double pDr1, double pDr2, double pDz, double pPhi, double pDphi )
	{
		if( pDr1 < 0 || pDr2 <= 0 || pDz <= 0 || pPhi < 0 || pDphi <= 0 ) {
            throw new IllegalArgumentException("Illegal arguments for Tube Primitive!");
        }
		
		this.pDr1 = pDr1;
		this.pDr2 = pDr2;
		this.pDz = pDz;
		this.pPhi = pPhi;
		this.pDphi = pDphi;
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
		return properties;
	}
	
	

}
