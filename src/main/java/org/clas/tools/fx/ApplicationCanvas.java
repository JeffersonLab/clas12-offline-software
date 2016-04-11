/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools.fx;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.clas.utils.LatexString;

/**
 *
 * @author gavalian
 */
public class ApplicationCanvas extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root, 650, 420, Color.rgb(240,240,240));
 
        // create a canvas node
        Canvas canvas = new Canvas();
 
        // bind the dimensions when the user resizes the window.
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        
        // obtain the GraphicsContext (drawing surface)
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.rgb(255, 0, 0));
        gc.setFill(Color.rgb(0, 255, 0));
        gc.fillRect(100, 100, 200, 200);
        gc.strokeRect(100, 100, 200, 200);
        
        //gc.rotate(-45.0);
        LatexString  string = new LatexString();
        string.setFont("Avenir", 12);
        string.addString("Lymber String");
        string.addString("2",1);
        
        string.addString("   M");
        string.addString("x",2);
        string.addString("15",1);
        string.addString("(p) [GeV");
        string.addString("2",1);
        string.addString("]");
        
        //string.addString(" Analyzing Results M");
        //string.addString("x",2);
        //string.addString("2",1);
        //string.addString("   10");
        //string.addString("-2",1);
        gc.setFill(Color.BLACK);
        LatexString str = LatexString.numberString(128445,2);
        string.draw(gc, 200, 200);
        str.setFont("Avenir", 12);
        str.draw(gc, 200, 300);
        
        
        LatexString small = LatexString.numberString(0.0056,4);
        small.setFont("Avenir", 12);
        small.draw(gc, 200, 350);
        root.getChildren().add(canvas);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    
    public static void main(String[] args){
        launch();
    }
}
