/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fx.canvas;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author gavalian
 */
public class GraphCanvas extends Canvas {
    
    private final List<IGraphCanvasObject>   canvasObjects = new ArrayList<IGraphCanvasObject>();
    double originalX, originalY;
    int    objectActive = -1;
    
    public GraphCanvas(){
        
        this.prefHeight(300);
        this.prefWidth(300);
        this.widthProperty().addListener(observable -> redraw());
        this.heightProperty().addListener(observable -> redraw());
        this.mouseEventsInit();
        AbsGraphCanvasObject ig = new AbsGraphCanvasObject(20,20,40,40);
        ig.setMovable(false);
        this.addObject(ig);
        this.addObject(new AbsGraphCanvasObject(120,120,60,60));
        /*
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, (final MouseEvent mouseEvent) -> {
            //System.out.println("Mouse moved too : "
            //        + mouseEvent.getX() + "  " + mouseEvent.getY());
            mouseClickedCallBack(mouseEvent);
        });*/
    }
    
    private void redraw(){
        //System.out.println("Redrawing");
        GraphicsContext gc = this.getGraphicsContext2D();
        double w = this.getWidth();
        double h = this.getHeight();
        gc.setFill(Color.rgb(255, 255, 255));
        gc.fillRect(0, 0, w, h);
        //gc.strokeRect(10, 10, w-20, h-20);
        for(IGraphCanvasObject gco : this.canvasObjects){
            //System.out.println("drawing object");
            gco.draw(gc);
        }
    }
    
    public void addObject(IGraphCanvasObject  obj){
        this.canvasObjects.add(obj);
    }
    
    
    public void update(){
        this.redraw();
    }
    
    
    private void mouseEventsInit(){
        
        EventHandler<MouseEvent> objectDraggedHandler = 
                new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent t) {
                        double x = t.getX();
                        double y = t.getY();
                        
                       // System.out.println("Dragged Event = " + x + "  " + y 
                        //         );
                        if(objectActive>=0){
                            double xshift = t.getX() - originalX;
                            double yshift = t.getY() - originalY;
                            canvasObjects.get(objectActive).mouseDragged(originalX, originalY,
                                    t.getX(),t.getY());
                            originalX = t.getX();
                            originalY = t.getY();
                            update();
                        }
                    }
                };
        EventHandler<MouseEvent> objectPressedHandler = 
                new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent t) {
                        int icounter = 0;
                        objectActive = -1;
                        for(IGraphCanvasObject gco : canvasObjects){
                            if(gco.bounds().contains(t.getX(), t.getY())==true){
                                //System.out.println("Object touched is : " + icounter);
                                objectActive = icounter;
                            }
                            icounter++;
                        }
                        
                        originalX = t.getX();
                        originalY = t.getY();
                    }
                };
        this.setOnMouseDragged(objectDraggedHandler);
        this.setOnMousePressed(objectPressedHandler);
        
    }
    
}
