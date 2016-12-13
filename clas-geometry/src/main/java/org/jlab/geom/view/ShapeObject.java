/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.awt.Graphics2D;

/**
 *
 * @author gavalian
 */
public interface ShapeObject {
    void draw(Graphics2D g2d, UniverseCoordinateSystem csystem);
}
