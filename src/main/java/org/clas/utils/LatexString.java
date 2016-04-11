/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 *
 * @author gavalian
 */
public class LatexString {
    
    Font  stringFont    = null;
    Font  subStringFont = null;
    List<String>   stringList = null;
    List<Integer>  stringAttributes = null;
    
    public static List<String>  formatStrings = LatexString.getFormatStrins();
    
    public LatexString(){
        setFont("Avenir",14);
        this.stringList = new ArrayList<String>();
        this.stringAttributes = new ArrayList<Integer>();
    }
    
    public static List<String>  getFormatStrins(){
        List<String> str = new ArrayList<String>();
        str.add("%.0f");
        str.add("%.1f");
        str.add("%.2f");
        str.add("%.3f");
        str.add("%.4f");
        return str;
    }
    
    public final void setFont(String name, int size){
        this.stringFont    = new Font(name,size);
        this.subStringFont = new Font(name,(int) (2*size/3));
    }
    
    public void addString(String str){
        this.stringList.add(str);
        this.stringAttributes.add(0);
    }
    
    public void addString(String str, int type){
        this.stringList.add(str);
        this.stringAttributes.add(type);
    }
    
    public float getHeight(){
        FontMetrics  fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.stringFont);
        return fm.getLineHeight();
    }
    
    public float getWidth(){
        FontMetrics  fm  = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.stringFont);
        FontMetrics  fms = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.subStringFont);
        float width = 0.0f;
        for(int k = 0; k < this.stringList.size(); k++){
            if(this.stringAttributes.get(k)==0){
                width += fm.computeStringWidth(this.stringList.get(k));
            } else {
                width += fms.computeStringWidth(this.stringList.get(k));
            }
        }
        return width;
    }
    
    public float getWidth(int index){
        if(this.stringAttributes.get(index)==0){
            FontMetrics  fm  = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.stringFont);
            return fm.computeStringWidth(this.stringList.get(index));
        }
        FontMetrics  fm  = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.subStringFont);
        return fm.computeStringWidth(this.stringList.get(index));
    }
    
    /**
     * Returns a number with maximum of n 4 spaces. everything larger gets converted to 
     * to exponent times the base.
     * @param number
     * @param places
     * @return 
     */
    public static LatexString  numberString(double number, int places){
        
        int   order = (int) Math.floor(Math.log(number) / Math.log(10));
        System.out.println(order);
        LatexString  ls = new LatexString();
        if(order>4){
            ls.addString(String.format(LatexString.formatStrings.get(places), number*Math.pow(10, -order)));
            ls.addString(" x 10");
            ls.addString(String.format("%d", order), 1);
            return ls;
        }
        if(order<-5){
            ls.addString(String.format(LatexString.formatStrings.get(places), number*Math.pow(10, -order)));
            ls.addString(" x 10");
            ls.addString(String.format("%d", -order), 1);
            return ls;
        }
        ls.addString(String.format(LatexString.formatStrings.get(places), number));
        return ls;
    }
    /**
     * Draw the text on graphics context
     * @param gc graphics context
     * @param x x position
     * @param y y position
     */
    public void draw(GraphicsContext gc, float x, float y){
        
        float position = x;

        
        float height = this.getHeight();        
        gc.strokeLine(x, y, x + 100, y);
        gc.strokeLine(x, y-height, x + 100, y-height);
        gc.setFont(stringFont);
        int previous = 0;
        for(int k = 0; k < this.stringList.size(); k++){
            int thisAttr = this.stringAttributes.get(k);
            if(this.stringAttributes.get(k)==1 || this.stringAttributes.get(k)==2){
                
                float yposition = (float) (y - height/3.0);
                if(this.stringAttributes.get(k)==2) yposition = (float) (y + height/3.0);
                gc.setFont(this.subStringFont);
                gc.fillText(this.stringList.get(k), position, yposition);
                gc.setFont(stringFont);
                if(k!=this.stringAttributes.size()-1){
                    int nextAttr = this.stringAttributes.get(k+1);
                    if(nextAttr!=0&&nextAttr!=thisAttr){
                        
                    } else {
                        position += this.getWidth(k);
                    }
                } else {
                    position += this.getWidth(k);
                }
                previous = this.stringAttributes.get(k);
            } else {
                gc.fillText(this.stringList.get(k), position, y);
                position += this.getWidth(k);
            }

        }
    }
    
    public static void main(String[] args){
        LatexString.numberString(1200000,1);
    }
}
