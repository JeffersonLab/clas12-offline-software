/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fx.canvas;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author gavalian
 */
public interface IGraphCanvasObject {
    
    public Rectangle2D  bounds();
    public void mousePressed(double x, double y, int button);
    public void mouseDragged(double x1, double y1,double x2, double y2);
    public void mouseReleased(double x, double y);
    public void mouseZoom(double x1, double y1,double x2, double y2);
    public void draw(GraphicsContext gc);
}
