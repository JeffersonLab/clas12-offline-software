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
import org.clas.utils.Configuration;
import org.clas.utils.ConfigurationGroup;

/**
 *
 * @author gavalian
 */
public class ApplicationUtils extends Application {

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
        
        
        root.add(btnChooser, 4, 3);
        
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
        
        root.add(btnJarfile, 3, 3);
        
        ConfigurationGroup group = new ConfigurationGroup("DCHB");
        group.addItem("torus", new String[]{"-1.0","-0.5","0.0","0.5","1.0"});
        group.addItem("solenoid", 0.5);
        group.addItem("kalman", new String[]{"true","false"});
        group.addItem("mcdata", new String[]{"true","false"});
        group.addItem("method", new String[]{"normalized","chi2","Log Likelihood"});
        group.addItem("debug", 15);
        ConfigurationGroup groupSEB = new ConfigurationGroup("H100");
        groupSEB.addItem("vX", new String[]{"K0","Phi0","MxE"});
        groupSEB.addItem("vY", new String[]{"K0","Phi0","MxE"});
        groupSEB.addItem("X min", 0.5);
        groupSEB.addItem("X max", 0.5);        
        groupSEB.addItem("X bins", 120);
        
        groupSEB.addItem("Y min", 0.5);
        groupSEB.addItem("Y max", 0.5);        
        groupSEB.addItem("Y bins", 120);
        
        ConfigurationGroup groupB = new ConfigurationGroup("K0mass");
        groupB.addItem("property", new String[]{"p","mass","theta","phi","px","py","pz"});
        groupB.addItem("particles", new String[]{"[b]","[t]","[2212]","[211]"});

        Configuration config = new Configuration();
        config.addGroup(group);
        config.addGroup(groupSEB);
        config.addGroup(groupB);
        root.add(config.getConfigPane(), 1, 0,4,2);
        
        Scene scene = new Scene(root,500,600);
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(600);
        
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
