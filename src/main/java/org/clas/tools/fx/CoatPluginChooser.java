/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools.fx;

import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gavalian
 */
public class CoatPluginChooser {

    Stage   stage = new Stage();
    TreeView<String>  treeView      = null;
    Map<String, List<String> > map  = null;
    CoatUtilsButtonPane  buttonPane = null;
    int     chooserResult = 0;
    String  chooserClass  = "";
    Image   iconPlugin    = null;
    Image   iconJar       = null;
    
    public CoatPluginChooser(Map<String, List<String> > mapList){
        this.map = mapList;
        init();
    }
    
    private void init(){
        
        this.iconPlugin = new Image(getClass().getResourceAsStream("/icons/plugin-icon.png"));
        this.iconJar    = new Image(getClass().getResourceAsStream("/icons/jar-icon.png"));
        
        this.buttonPane = new CoatUtilsButtonPane(100,new String[]{"Load","Cancel"});
        this.treeView   = new TreeView();
        this.treeView.setStyle("-fx-font-size: 14");
        TreeItem<String>  rootTree = this.getTree();
        this.treeView.setRoot(rootTree);
        this.treeView.getRoot().setExpanded(true);
        BorderPane  root = new BorderPane();
        root.setPadding(new Insets(10,10,10,10));
        root.setCenter(this.treeView);
        root.setBottom(buttonPane);
        Scene scene = new Scene(root,600,500);
        this.stage = new Stage();
        this.stage.setScene(scene);
    }
    
    public void show(){
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.showAndWait();
    }
    
    private TreeItem<String>  getTree(){
        TreeItem<String> root = new TreeItem("root");
        for(Map.Entry<String, List<String> > jars : this.map.entrySet()){
            TreeItem<String>  jarItem = new TreeItem<String>(jars.getKey(), new ImageView(this.iconJar));
            for(String classes : jars.getValue()){
                jarItem.getChildren().add(new TreeItem<String>(classes,new ImageView(this.iconPlugin)));
            }
            root.getChildren().add(jarItem);
        }
        return root;
    }
}
