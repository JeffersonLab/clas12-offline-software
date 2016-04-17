/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.fx.canvas;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.clas.utils.LatexString;

/**
 *
 * @author gavalian
 */
public class GraphCanvasApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane  root = new BorderPane();
        GraphCanvas  canvas = new GraphCanvas();

        root.setCenter(canvas);
        Scene scene = new Scene(root, 650, 420, Color.rgb(240,240,240));
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        // create a canvas node
       
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args){
        launch();
    }
}
