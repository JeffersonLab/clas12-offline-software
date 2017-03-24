package org.jlab.detector.geant4.v2.SVT;

import java.util.ArrayList;
import java.util.List;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;

/**
 * @author pdavies
 */
public class Pgon implements Primitive {
	
	private final PropertyStorage properties = new PropertyStorage();
	private int pN;
	private double pPhi, pDphi;
	
	public Pgon( int pN, double pPhi, double pDphi )
	{
		if( pN < 0 || pPhi < 0 || pDphi <= 0 ) {
            throw new IllegalArgumentException("Illegal arguments for Polyhedra Primitive!");
        }
		
		this.pN = pN;
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
		
		return null;
	}

}
