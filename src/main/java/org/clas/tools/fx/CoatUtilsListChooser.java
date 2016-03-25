/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools.fx;

import org.clas.tools.fx.CoatUtilsButtonPane;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author gavalian
 */
public class CoatUtilsListChooser {
    
    private Stage              stage    = null;
    private List<String>  chooserList   = null;
    private int         chooseAction    = 0;
    private List<String> selectedList  = new ArrayList<String>();
    private ListView     listView   = null;
    private CoatUtilsButtonPane  controls = null;
    
    public CoatUtilsListChooser(List<String> list){
        this.initStage(list);
    }
    
    private void initStage(List<String> list){
        this.chooserList = list;
        BorderPane  root = new BorderPane();
        Scene scene = new Scene(root,500,350);        
        this.listView = new ListView();
        this.listView.setItems(FXCollections.observableArrayList(chooserList));
        root.setPadding(new Insets(10,10,10,10));
        
        controls = new CoatUtilsButtonPane(100,new String[]{"OK","Cancel"});
        root.setCenter(listView);
        root.setBottom(controls);
        
        controls.getButtons().get(0).setOnAction(e -> {
            int index = listView.getSelectionModel().getSelectedIndex();
            if(index<0||index>=chooserList.size()){
                selectedList.clear();
                chooseAction = 0;
            } else {
                chooseAction = 1;
                selectedList.add(chooserList.get(index));
            }
            stage.close();
        });        
        
        controls.getButtons().get(1).setOnAction(e -> {
            chooseAction = 0;
            selectedList.clear();
            stage.close();
        });
        
        this.stage  = new Stage();
        this.stage.setTitle("List Selector");
        this.stage.setMinWidth(500);
        this.stage.setMinHeight(350);
        this.stage.setScene(scene);
    }
    
    public List<String>  getSelection(){
        return this.selectedList;
    }
    
    public int show(){
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.showAndWait();
        return this.chooseAction;
    }
    
}
