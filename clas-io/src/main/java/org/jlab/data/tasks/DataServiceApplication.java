/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.tasks;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

/**
 *
 * @author gavalian
 */
public class DataServiceApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DataSourceService service = new DataSourceService();

        
        Group  root = new Group();
        ProgressBar bar = new ProgressBar();
        root.getChildren().add(bar);
        //bar.progressProperty().bind(service.progressProperty());
        //bar.progressProperty().set(0.0);
        primaryStage.setScene(new Scene(root, 500, 100));
        primaryStage.show();
        service.start();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
