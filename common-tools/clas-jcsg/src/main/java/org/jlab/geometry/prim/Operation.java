package org.jlab.geometry.prim;

import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;

/**
 * @author pdavies
 */
public class Operation implements Primitive {
	
	private final PropertyStorage properties = new PropertyStorage();
	private String pOp;
	private String[] pOps;

	public Operation( String operation, String[] operands )
	{
		if( operation.isEmpty() || operands.length == 0 ) {
            throw new IllegalArgumentException("Illegal arguments for Operation Primitive!");
        }
		
		this.pOp = operation;
		this.pOps = operands;
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
