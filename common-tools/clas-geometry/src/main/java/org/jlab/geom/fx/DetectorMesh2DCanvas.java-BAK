/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class DetectorMesh2DCanvas extends Canvas {
    
    private Map<String,DetectorMesh2DLayer>  meshLayers = new HashMap<String,DetectorMesh2DLayer>();
    private Color   background = Color.rgb(58, 70, 80);//Color.ANTIQUEWHITE;
    private double  canvasScale = 1.0;
    
    private  final Point3D   mousePositionLocal = new Point3D();
    private  final Point3D   mousePositionWorld = new Point3D();
    private  final Point3D  mousePositionClicked = new Point3D();
    private  final Point3D  mousePositionDragged = new Point3D();
    private  boolean  isInDragMode = false;
    
    private boolean  autoScaling   = true;
    
    private double   canvasCenterOffsetX = 0.0;
    private double   canvasCenterOffsetY = 0.0;
    
    private double  mousePositionX = 0.0;
    private double  mousePositionY = 0.0;
    //private final ArrayList<EventHandler<DetectorMesh2D> >   detectorEventHandlers 
    //        = new ArrayList<EventHandler<DetectorMesh2D> >();
    private  List<DetectorEventHandler<DetectorMesh2D> >  onClickHandler = 
            new ArrayList<DetectorEventHandler<DetectorMesh2D>>();
    private  ToolBar   toolBar = new ToolBar();
    private  ComboBox  comboBox = new ComboBox();
    
    public String canvasName = "canvas";
    
    public DetectorMesh2DCanvas(String name){
        this.canvasName = name;
        this.prefWidth(500);
        this.prefHeight(500);
        this.widthProperty().addListener(observable -> update());
        this.heightProperty().addListener(observable -> update());
        this.initMouseListeners();
        this.initToolBar();
    }
    
    public String getName(){return this.canvasName;}
    
    public void addLayer(DetectorMesh2DLayer layer){
        this.meshLayers.put(layer.getName(), layer);
         List<String>  layers = new ArrayList<String>();
        
        for(Map.Entry<String,DetectorMesh2DLayer> entry : this.meshLayers.entrySet()){
            layers.add(entry.getKey());
        }

        ObservableList<String>  options = FXCollections.observableArrayList(
                layers
        );
        comboBox.getItems().clear();
        comboBox.getItems().addAll(options);
        comboBox.getSelectionModel().select(0);
    }
    
    public void createLayer(String name){
        this.meshLayers.put(name, new DetectorMesh2DLayer(name));
    }
    
    public DetectorMesh2DLayer getLayer(String name){
        return this.meshLayers.get(name);
    }
    public void addMesh(String name, DetectorMesh2D mesh){
        if(this.meshLayers.containsKey(name)==true){
            this.meshLayers.get(name).addMesh(mesh);
        }
    }
    
    public void setOnSelect(DetectorEventHandler<DetectorMesh2D> handler){
        this.onClickHandler.add(handler);
    }
    
    public void update(){
        //System.out.println("updating");
        GraphicsContext gc = this.getGraphicsContext2D();
        double w = this.getWidth();
        double h = this.getHeight();
        if(this.autoScaling==true) this.updateScale();
        gc.setFill(background);
        gc.fillRect(0, 0, w, h);
        
        for(Map.Entry<String,DetectorMesh2DLayer> entry : this.meshLayers.entrySet()){
            entry.getValue().draw(gc, w, h, 
                    this.canvasCenterOffsetX, this.canvasCenterOffsetY, 
                    this.mousePositionLocal.x(), this.mousePositionLocal.y(),
                    this.canvasScale);
        }
                
        //this.drawZoomButtons(gc, w, h);
        this.drawStatString(gc, w, h);
        if(this.isInDragMode==true){
            gc.setStroke(Color.YELLOW);
            gc.strokeRect(this.mousePositionClicked.x(), mousePositionClicked.y(), 
                    this.mousePositionDragged.x()-this.mousePositionClicked.x(),
                    this.mousePositionDragged.y()-this.mousePositionClicked.y()
                    );
        }
    }
    
    public void updateScale(){
        double w = this.getWidth();
        double h = this.getHeight();
        double niceScale = 1.0;
        this.canvasCenterOffsetX = 0.0;
        this.canvasCenterOffsetY = 0.0;
        for(Map.Entry<String,DetectorMesh2DLayer> entry : this.meshLayers.entrySet()){
            double scaleX = w/entry.getValue().getRegion().getDimension().x();
            double scaleY = h/entry.getValue().getRegion().getDimension().y();
            if(scaleX<scaleY){
                niceScale = scaleX;
            } else {
                niceScale = scaleY;
            }
        }
        this.canvasScale = niceScale*0.8;
    }
    
    public void zoomRegion(Point3D p1, Point3D p2){
        this.autoScaling = false;
        double w = getWidth();
        double h = getHeight();
        double centerX = p1.x() + (p2.x() - p1.x())/2.0;
        double centerY = p1.y() + (p2.y() - p1.y())/2.0;
        double rcx = w/2.0 - centerX;
        double rcy = h/2.0 - centerY;
        double factor = 0.1*w/(p2.x()-p1.x());
        System.out.println("Scale factor = " + factor);
        this.canvasScale = this.canvasScale + this.canvasScale*factor;
        this.canvasCenterOffsetX += rcx;
        this.canvasCenterOffsetY += rcy;        
    }
    
    public void zoomRegion(){
        this.zoomRegion(mousePositionClicked, mousePositionDragged);
    }
    
    public void drawZoomButtons(GraphicsContext gc, double w, double h){
        
        gc.setFill(Color.rgb(20,20,120,0.4));
        gc.fillRoundRect(0, 0, 30, 180, 30, 30);        
        gc.setStroke(Color.WHITE);     
        gc.setLineWidth(2);
        gc.strokeOval(5, 5, 20, 20);
        gc.strokeOval(5, 30, 20, 20);
        gc.strokeLine(10,40,20,40);
        gc.strokeLine(10,15,20,15);
        gc.strokeLine(15,10,15,20);
        gc.strokeOval(5, 55, 20, 20);        
        gc.strokeOval(5, 80, 20, 20);        
        gc.strokeLine(18, 90, 12, 87);
        gc.strokeLine(18, 90, 12, 93);
        gc.strokeOval(5, 105, 20, 20);
        gc.strokeLine(10, 115, 18, 112);
        gc.strokeLine(10, 115, 18, 118);
        gc.strokeOval(5, 130, 20, 20);
        gc.strokeOval(5, 155, 20, 20);
        

        
        
    }
    
    public void drawStatString(GraphicsContext gc, double w, double h){
        gc.setFill(Color.WHEAT);
        gc.fillText(String.format("SCALE = %.2f, MOUSE (%.0f x %.0f), WORLD (%.1f x %.1f)", 
                this.canvasScale,this.mousePositionLocal.x(),this.mousePositionLocal.y(),
                this.mousePositionWorld.x(),this.mousePositionWorld.y())
                , 10, 15);
    }
    
    public void  updateSelectionList(double worldX, double worldY){
        
        DetectorMesh2D  dMesh = null;
        for(Map.Entry<String,DetectorMesh2DLayer> entry : this.meshLayers.entrySet()){
            for(DetectorMesh2D mesh : entry.getValue().getMeshList()){
                if(mesh.isContained(worldX, worldY)){
                    //System.out.println("yep ! you clicked on a shape");
                    dMesh = mesh;
                }
            }
        }
        
        if(dMesh!=null){
            dMesh.show();
            if(this.onClickHandler.size()>0){
                for(DetectorEventHandler<DetectorMesh2D> h : this.onClickHandler){
                    h.handle(dMesh);
                }
            }
            //System.out.println("selected detector component");
        }
    }
    
    public void initMouseListeners(){
        
        EventHandler<MouseEvent> mouseClickedHandler = 
                new EventHandler<MouseEvent>() {                    
                    @Override
                    public void handle(MouseEvent t) {
                        double rzi = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-15)*(t.getY()-15));
                        double rzo = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-40)*(t.getY()-40));
                        double rzn = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-65)*(t.getY()-65));
                        double rmr = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-90)*(t.getY()-90));
                        double rml = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-115)*(t.getY()-115));
                        double rmu = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-140)*(t.getY()-140));
                        double rmd = Math.sqrt((t.getX()-15)*(t.getX()-15) + (t.getY()-165)*(t.getY()-165));
                        //System.out.println("CLICKED " 
                        //        + t.getX() + " " + t.getY() 
                         //       + "  " + rzi + "  " + rzo);
                        if(rzo<10.0){ autoScaling = false; canvasScale -= 0.1; update();}
                        if(rzi<10.0){ autoScaling = false; canvasScale += 0.1; update();}
                        if(rzn<10.0){ autoScaling  = true; update();}
                        if(rmr<10.0){ autoScaling  = false; canvasCenterOffsetX += 25; update();}
                        if(rml<10.0){ autoScaling  = false; canvasCenterOffsetX -= 25; update();}
                        if(rmu<10.0){ autoScaling  = false; canvasCenterOffsetY += 25; update();}
                        if(rmd<10.0){ autoScaling  = false; canvasCenterOffsetY -= 25; update();}
                        //mousePositionLocal.set(t.getX(),t.getY(),0.0);
                        //mousePositionWorld.set((t.getX()-0.5*getWidth())/canvasScale,
                        //        (t.getY()-0.5*getHeight())/canvasScale,0.0);
                        updateSelectionList(mousePositionWorld.x(),mousePositionWorld.y());
                        
                        update();
                        System.out.println("changed the dragg mode = " + isInDragMode);
                    }
                };
        EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {                    
                    @Override
                    public void handle(MouseEvent t) {
                        mousePositionLocal.set(t.getX(),t.getY(),0.0);
                        mousePositionWorld.set((t.getX()-0.5*getWidth()-canvasCenterOffsetX)/canvasScale,
                                (t.getY()-0.5*getHeight()-canvasCenterOffsetY)/canvasScale,0.0);
                        update();
                    }
        };
        
        
        
        this.setOnMouseMoved(mouseMoveHandler);
        this.setOnMouseClicked(mouseClickedHandler);
        EventHandler<MouseEvent> mouseReleasedHandler = 
                new EventHandler<MouseEvent>() {                    
                    @Override
                    public void handle(MouseEvent event) {
                        //System.out.println("---> dragging finished");
                        if(isInDragMode==true){
                            isInDragMode = false;
                            zoomRegion();
                            update();
                        }
                    }

                };
        EventHandler<MouseEvent> mouseDragHandler = 
                new EventHandler<MouseEvent>() {                    
                    @Override
                    public void handle(MouseEvent event) {
                        System.out.println("---> dragging moude " + isInDragMode);
                        mousePositionDragged.set(event.getX(), event.getY(), 0.0);
                        isInDragMode = true;
                        update();
                    }
                    
                };
        EventHandler<MouseEvent> mousePressedHandler = 
                new EventHandler<MouseEvent>() {                    
                    @Override
                    public void handle(MouseEvent t) {
                        System.out.println("---> dragging moude " + isInDragMode);
                        mousePositionClicked.set(t.getX(), t.getY(), 0.0);
                        mousePositionDragged.set(t.getX(), t.getY(), 0.0);
                        //isInDragMode = true;
                        update();
                    }
                    
                };
        //this.setOnMouseClicked(mouseClickedHandler);
        this.setOnMousePressed(mousePressedHandler);
        this.setOnMouseDragged(mouseDragHandler);
        this.setOnMouseReleased(mouseReleasedHandler);
    }
    
    public ToolBar getToolBar(){ return toolBar; }
    
    public void initToolBar(){
        Button  btZoomIn     = new Button("+"); 
        Button  btZoomOut    = new Button("-"); 
        Button  btZoomReset  = new Button("\u293E");
        Button  btMoveLeft   = new Button("<");
        Button  btMoveRight  = new Button(">");
        Button  btMoveUp     = new Button("\u21E7");
        Button  btMoveDown   = new Button("\u21E9");

        btZoomIn.setOnAction( event -> { autoScaling  = false;canvasScale += 0.1;update();});
        btZoomOut.setOnAction( event -> { autoScaling  = false;canvasScale -= 0.1;update();});
        btZoomReset.setOnAction( event -> { autoScaling  = true;update();});
        btMoveRight.setOnAction(event -> { autoScaling  = false; canvasCenterOffsetX += 25; update();});
        btMoveLeft.setOnAction(event -> { autoScaling  = false; canvasCenterOffsetX -= 25; update();});
        btMoveUp.setOnAction(event -> { autoScaling  = false; canvasCenterOffsetY -= 25; update();});
        btMoveDown.setOnAction( event -> { autoScaling  = false; canvasCenterOffsetY += 25; update();});
        
        comboBox = new ComboBox();
        
        toolBar.getItems().addAll(btZoomIn,btZoomOut,btZoomReset);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().addAll(btMoveLeft,btMoveRight,btMoveUp,btMoveDown);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(comboBox);
        Slider slider = new Slider(0, 1.0, 1.0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(0.25f);
        slider.setBlockIncrement(0.1f);
        
        toolBar.getItems().addAll(new Label("Opacity"),slider);
        
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    System.out.println(new_val.doubleValue());
                    String layerName = (String) comboBox.getSelectionModel().getSelectedItem();
                    meshLayers.get(layerName).setOpacity(new_val.doubleValue());
                    update();
            }
        });
        
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                System.out.println(ov);
                System.out.println(t);
                System.out.println(t1);
                if(meshLayers.containsKey(t1)==true){
                    double opacity = meshLayers.get(t1).getOpacity();
                    slider.setValue(opacity);
                }
            }    
        });
    }
}
