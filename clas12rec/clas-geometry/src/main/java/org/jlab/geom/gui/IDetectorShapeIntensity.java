/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Color;

/**
 *
 * @author gavalian
 */
public interface IDetectorShapeIntensity {
    public Color getColor(int sector, int layer, int component);
}
