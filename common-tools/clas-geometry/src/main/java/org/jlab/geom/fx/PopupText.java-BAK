/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author gavalian
 */
public class PopupText {

    List<String>   popUps = new ArrayList<String>();
    Font           popUpFont = new Font("Avenir",14);
    double         paddingX  = 10;
    double         paddingY  = 10;
    
    public PopupText(){
        
    }
    
    public void addText(String text){
        this.popUps.add(text);
    }
    
    public void clear(){
        this.popUps.clear();
    }
    
    public void draw(GraphicsContext gc, double x, double y){
        if(this.popUps.size()<=0) return;
        gc.setFill(Color.ANTIQUEWHITE);
        Rectangle2D  rect = this.getBounds(gc);
        gc.fillRect(x, y, rect.getWidth() + 2*paddingX,rect.getHeight()+2*paddingY);
        double step = rect.getHeight()/this.popUps.size();
        double counter = 0.7;
        gc.setFill(Color.BLACK);
        for(String text : this.popUps){
            gc.fillText(text, x + paddingX, y + paddingY + step*counter);
            counter +=1.0;
        }
    }
    
    public Rectangle2D  getBounds(GraphicsContext gc){
        FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(popUpFont);
        double w = 0.0;
        double h = 0.0;
        for(String text : this.popUps){
            double tw = fm.computeStringWidth(text);
            if(tw>w) w = tw;
            h += fm.getLineHeight();
        }
        
        paddingX = fm.getLineHeight()*0.5;
        paddingY = fm.getLineHeight()*0.5;
        return new Rectangle2D(0.0,0.0,w,h);
    }
}
