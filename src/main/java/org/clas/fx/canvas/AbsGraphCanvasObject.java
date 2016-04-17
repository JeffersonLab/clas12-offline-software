/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fx.canvas;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author gavalian
 */
public class AbsGraphCanvasObject implements IGraphCanvasObject {
    private Rectangle2D   objectBounds = null;
    
    public AbsGraphCanvasObject(double width, double height){
        this.objectBounds = new Rectangle2D(0.0,0.0,width,height);
    }
    public AbsGraphCanvasObject(double x, double y,double width, double height){
        this.objectBounds = new Rectangle2D(x,y,width,height);
    }
    
    @Override
    public Rectangle2D bounds() {
        return this.objectBounds;
    }

    
    @Override
    public void mouseClick(double x, double y, int button) {
        System.out.println("[AbsGraphCanvasObject] abstract method. does not do anything");
    }

    @Override
    public void mouseDrag(double x1, double y1, double x2, double y2) {
        //System.out.println("[AbsGraphCanvasObject] abstract method. does not do anything");
        //System.out.println("Dragged from : " + x1 + " x " + y1 + "  to " + x2 
        //+ " x " + y2);
        double w = this.objectBounds.getWidth();
        double h = this.objectBounds.getHeight();
        this.objectBounds = new Rectangle2D(x2,y2,w,h);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.rgb(0, 0, 0));
        gc.strokeRect(this.bounds().getMinX(), this.bounds().getMinY(), 
                this.bounds().getWidth(), this.bounds().getHeight());
    }
    
}
