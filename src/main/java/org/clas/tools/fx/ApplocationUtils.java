/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools.fx;


import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.clas.utils.CoatUtilsFile;
import org.clas.utils.CoatUtilsJar;

/**
 *
 * @author gavalian
 */
public class ApplocationUtils extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane root = new GridPane();        
        /**
         * Testing the list viewer interface.
         */
        Button btnChooser = new Button("Chooser");
        btnChooser.setOnAction(e -> {
            List<String>  files   = CoatUtilsFile.getFileList("COATJAVA", "etc/bankdefs/clas12/");
            //List<String>  options = new ArrayList<String>(files));
            CoatUtilsListChooser chooser = new CoatUtilsListChooser(CoatUtilsFile.getFileNamesRelative(files));
            for(String item : files){
                System.out.println(item);
            }
            int status = chooser.show();
            if(status==0){
                System.out.println("nothing was selected");
            } else {
                for(String item : chooser.getSelection()){
                    System.out.println("selected : " + item);
                }
            }
        } );
        
        
        root.add(btnChooser, 0, 0);
        
        Button btnJarfile = new Button("Scan Jar");
        btnJarfile.setOnAction(e -> {
            List<String> jarList = CoatUtilsFile.getFileList("COATJAVA", "lib/tests");
            for(String jar : jarList){
                System.out.println("file : --> " + jar);
                List<String>  classes = CoatUtilsJar.scanJarFile(jar, "org.clas.plugins.ICLASPlugin");

                System.out.println("result = " + classes.size());
            }
            String directory = CoatUtilsFile.getResourceDir("COATJAVA", "lib/tests");
            Map<String, List<String> > classMap = CoatUtilsJar.scanDirectory(directory, "org.clas.plugins.ICLASPlugin");
            CoatPluginChooser  chooser = new CoatPluginChooser(classMap);
            chooser.show();
        });
        
        root.add(btnJarfile, 1, 0);
        Scene scene = new Scene(root,200,600);        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
