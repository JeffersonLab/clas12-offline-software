/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.tools.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

/**
 *
 * @author gavalian
 */
public class CoatUtilsButtonPane extends FlowPane {
    private List<Button>  buttons = new ArrayList<Button>();
    
    public CoatUtilsButtonPane(int size, String[] btn){
        super();        
        this.init(size,btn);
    }
    
    
    private void init(int length, String[] btn){
        this.setPadding(new Insets(10,10,10,10));
        this.setVgap(10);
        this.setHgap(10);
        
        for(String lb : btn){
            Button b = new Button(lb);
            b.setPrefWidth(length);
            this.buttons.add(b);
            this.getChildren().add(b);
        }
    }
    
    public List<Button>  getButtons(){
        return buttons;
    }
}
