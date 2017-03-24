/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Intersection;
import eu.mihosoft.vrl.v3d.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author kenjo
 */
public class Triangle3d {

    private final Vector3d[] vertices = new Vector3d[3];
    private final Vector3d side1, side2;

    public final Vector3d normal;

    public Triangle3d(Vector3d vertex0, Vector3d vertex1, Vector3d vertex2) {
        vertices[0] = vertex0.clone();
        vertices[1] = vertex1.clone();
        vertices[2] = vertex2.clone();
        side1 = vertices[1].minus(vertices[0]);
        side2 = vertices[2].minus(vertices[0]);
        normal = side1.cross(side2).normalized();
    }

    public boolean contains(Vector3d point) {
        Vector3d pvec = point.minus(vertices[0]);
        double d00 = side1.dot(side1);
        double d01 = side1.dot(side2);
        double d11 = side2.dot(side2);
        double d20 = pvec.dot(side1);
        double d21 = pvec.dot(side2);
        double denom = d00 * d11 - d01 * d01;
        double vv = (d11 * d20 - d01 * d21) / denom;
        double ww = (d00 * d21 - d01 * d20) / denom;
        //added 1e-10 to account for error related to precision
        return (vv >= -1e-10 && ww >= -1e-10 && (vv + ww) <= 1+1e-10);
    }

    public Intersection getIntersection(Straight line) {
        Intersection intersect = new Intersection();

        double tt = vertices[0].minus(line.origin()).dot(normal);
        double denom = line.diff().dot(normal);
        if (denom != 0) {
            tt /= denom;
            if (line.contains(tt)) {
                Vector3d p0 = line.origin().plus(line.diff().times(tt));
                if (this.contains(p0)) {
                    intersect.setPosition(p0, tt);
                }
            }

        }
        return intersect;
    }

    public Optional<Line3d> getIntersection(Vector3d planePoint, Vector3d planeNormal) {
        Optional<Line3d> line = Optional.empty();
        List<Vector3d> interpoints = new ArrayList<>();
                
        for (int ivert = 0; ivert < 3; ivert++) {
            LineSegment3d side = new LineSegment3d(vertices[ivert], vertices[ivert<2 ? ivert+1 : 0]);
            double tt = planePoint.minus(side.origin()).dot(planeNormal);
            double denom = side.diff().dot(planeNormal);
            if (denom != 0) {
                tt /= denom;
                if (side.contains(tt)) {
                    interpoints.add(side.origin().plus(side.diff().times(tt)));
                }
            }
        }
        
        if(interpoints.size()==2) {
            line = Optional.of(new Line3d(interpoints.get(0), interpoints.get(1)));
        }
        else if(!interpoints.isEmpty()) {
            System.err.println("Intersection of planes produced "+interpoints.size()+"points!!!!");
            System.exit(1111);
        }

        return line;
    }

    /**
     * @author pdavies
     */
	public Vector3d center() {
		double x = 0;
		double y = 0;
		double z = 0;
		for( Vector3d v : vertices )
		{
			x += v.x;
			y += v.y;
			z += v.z;
		}
		return new Vector3d( x/3, y/3, z/3 );
	}

	/**
	 * @author pdavies
	 */
	public Vector3d point(int index) {
		if( index < 0 || index > 2 )
		{
			System.err.println("Warning: Triangle3d point(int index): invalid index=" + index );
			return null;
		}
		return vertices[index];
	}
}
