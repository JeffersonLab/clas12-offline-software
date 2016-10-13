/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 *
 * @author kenjo
 */
public class LineSegment3d extends Straight{
    public LineSegment3d(Vector3d origin, Vector3d end){
        super(origin, end);
    }
    
    @Override
    public boolean contains(double parametricT){
        return parametricT>=0 && parametricT<=1;
    }
}
