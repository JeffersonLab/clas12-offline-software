/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 *
 * @author gavalian
 */
public class CoatSVGUtils {
    
    List<String>   commandsHeader = new ArrayList<String>();
    List<String>   commands       = new ArrayList<String>();
    
    public CoatSVGUtils(int xsize, int ysize){
        commandsHeader.add(
                String.format("<svg viewBox='0 0 %d %d' xmlns='http://www.w3.org/2000/svg'>",
                        xsize,ysize));
        //commands.add("<desc><![CDATA[
        //PRODUCED By CLAS12 Graphing Software (Jlab)
        //]]></desc>"");
    }
    
    public static String drawLine(int x1, int y1,int x2,int y2, int width, String color){
        StringBuilder  str = new StringBuilder();
        str.append("<line ");
        str.append(String.format("x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"",
                x1,y1,x2,y2));
        str.append(String.format("stroke=\"%s\" stroke-width=\"%d\"/>", color,width));
        return str.toString();
    }
    
    public static String drawText(String text, int x, int y,String family, int size, String color){
        StringBuilder  str = new StringBuilder();
        str.append(String.format("<text x=\"%d\" y=\"%d\" ",x,y));
        str.append(String.format(" font-family=\"%s\" ",family ));
        str.append(String.format(" font-size=\"%d\"> %s </text>",
                size,text));
        return str.toString();
    }
    
    public static String drawPath(Path path, int lineWidth, String lineColor, String fillcolor,
            String clipPath){
        StringBuilder str = new StringBuilder();
        str.append("<path d=\"");
        for(PathElement  element : path.getElements()){
            if(element instanceof MoveTo){
                MoveTo move = (MoveTo) element;
                str.append(String.format("M %d %d ", (int) move.getX(), (int) move.getY()));
            }
            if(element instanceof LineTo){
                LineTo line = (LineTo) element;
                str.append(String.format("L %d %d ", (int) line.getX(), (int) line.getY()));
            }
        }
        str.append(String.format("\" stroke=\"%s\"",lineColor));
        str.append(String.format("\" stroke-width=\"%d\"",lineWidth));
        str.append(String.format("\" fill=\"%s\"",fillcolor));
        str.append(String.format(" clip-path=\"url(#%s)\"", clipPath));
        return str.toString();
    }
    
    public static String defineClipPath(String name, int x, int y, int width, int height){
        StringBuilder str = new StringBuilder();
        str.append(String.format("<defs><clipPath id=\"%s\">",name));
        str.append(String.format("<rect x=\"%d\" y=\"%d\" ",x,y));
        str.append(String.format("width=\"%d\" height=\"%d\" />",width,height));
        str.append(String.format("</clipPath></defs>",name));
        return str.toString();
    }
    
    public void addPath(Path path, int lineWidth, String lineColor, String fillcolor,
            String clipPath){
        this.commands.add(CoatSVGUtils.drawPath(path, lineWidth, lineColor, fillcolor, clipPath));
    }
    
    public void addClipPath(String name, int x, int y, int width, int height){
        this.commands.add(CoatSVGUtils.defineClipPath(name, x, y, width, height));
    }
    
    public void addText(String text, int x, int y,String family, int size){
        this.commands.add(CoatSVGUtils.drawText(text, x, y, family, size,"#ffffff"));
    }
    
    public void addLine(int x1, int y1,int x2,int y2, int width, String color){
        this.commands.add(CoatSVGUtils.drawLine(x1, y1, x2, y2, width, color));
    }
    
    public List<String>  getHeader(){
        return this.commandsHeader;
    }
    
    public void save(String file){
        List<String> content = new ArrayList<String>();
        content.addAll(this.commandsHeader);
        content.addAll(this.commands);
        content.add("</svg>");
        CoatUtilsFile.writeFile(file, content);
    }
}
