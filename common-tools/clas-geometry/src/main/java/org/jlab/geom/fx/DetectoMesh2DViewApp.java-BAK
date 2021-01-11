/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class DetectoMesh2DViewApp extends Application {
    
    DetectorMesh2DCanvas  canvas = null;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane  root = new BorderPane();
        canvas = new DetectorMesh2DCanvas("CVT");
        this.addShapes();
        DetectorTabView view = new DetectorTabView(650,650);
        view.addView(canvas);
        root.setCenter(view);
        
        Scene scene = new Scene(root, 650, 650, Color.rgb(255,255,255));
        
//scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> canvas.keyPressed());
        //canvas.widthProperty().bind(scene.widthProperty());
        //canvas.heightProperty().bind(scene.heightProperty());
        // create a canvas node
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void addShapes(){
        DetectorMesh2DLayer layer = new DetectorMesh2DLayer("SVT");
        
        int nd = 18;
        for(int i = 0; i < nd; i++){
            double angle = i*2.0*Math.PI/nd;
            DetectorMesh2D      mesh1 = new DetectorMesh2D(1,1,1,i+1);
            mesh1.box(25, 12);
            mesh1.setLineWidth(0.4).setFillColor(90, 120, 110);
            mesh1.getPath().translateXYZ(0, 90, 0);
            mesh1.getPath().rotateZ(angle);
            layer.addMesh(mesh1);
        }
        nd = 24;
        DetectorMesh2DLayer layer2 = new DetectorMesh2DLayer("BMT");
        for(int i = 0; i < nd; i++){
            double angle = i*2.0*Math.PI/nd;
            DetectorMesh2D      mesh1 = new DetectorMesh2D(1,1,2,i+1);
            mesh1.box(30, 10);
            mesh1.setFillColor(90,110,125).setLineWidth(0.4);
            mesh1.getPath().translateXYZ(0, 120, 0);
            mesh1.getPath().rotateZ(angle);
            layer2.addMesh(mesh1);
        }
        
        DetectorMesh2DLayer layer3 = new DetectorMesh2DLayer("CND");
        for(int i = 0; i < nd; i++){
            double angle = i*2.0*Math.PI/nd;
            DetectorMesh2D      mesh1 = new DetectorMesh2D(1,1,2,i+1);
            mesh1.box(30, 10);
            mesh1.setFillColor(90,160,125).setLineWidth(0.4);
            mesh1.getPath().translateXYZ(0, 160, 0);
            mesh1.getPath().rotateZ(angle);
            layer3.addMesh(mesh1);
        }
        
        layer2.addHit(60,-85,0.0,String.format("%s : %3.5f", "Hit Position",125.568));
        layer2.addHit(60,85,0.0,
                String.format("%s : %3.5f", "Hit Position",125.568),
                String.format("%s : %3.5f", "Energy = ",6.34),
                String.format("%s : %3.5f", "Time = ",0.4567)
        );
        
        Path3D path = new Path3D();
        path.addPoint(0.0, 0.0, 0.0);
        path.addPoint(120.0, 120.0, 0.0);
        path.addPoint(160.0, 190.0, 0.0);
        layer2.addPath(path, 
                String.format("%s = %12.4f", "P",1.4567),
                String.format("%s = %12.4f", "Theta",25.67),
                String.format("%s = %12.4f", "Phi",-127.34)
        );
        //layer2.getPaths().add(path);
        //layer.getRegion().show();
        this.canvas.addLayer(layer);
        this.canvas.addLayer(layer2);
        this.canvas.addLayer(layer3);
        
        this.canvas.setOnSelect(new DetectorEventHandler<DetectorMesh2D>(){
            @Override
            public void handle(DetectorMesh2D event) {
                System.out.print(" guess what a click was detected --> " );
                event.show();
            }
        });
    }
    
    public static void main(String[] args){
        launch();
    }
}
