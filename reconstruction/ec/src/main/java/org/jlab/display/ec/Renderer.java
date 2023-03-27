/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.display.ec;

/**
 *
 * @author gavalian
 */
public class Renderer {
    public static Character[] block = new Character[]{
        '\u2581','\u2582','\u2583','\u2584',            
        '\u2585','\u2586' ,'\u2587' , '\u2588' };
    
    DataRow[] rows = null;
    private int xSize = 0;
    private int ySize = 0;
    public Renderer(int xsize, int ysize){
        this.initialize(xsize, ysize);
    }
    
    public Renderer(int height, double[] data){
        this.initialize(data.length, height);
        this.setData(data);
    }
    
    private double getMax(double[] data){
        double max = 0.0;
        for(int i = 0; i < data.length; i++) max = Math.max(max, data[i]);
        return max;
    }
    
    private void initialize(int xsize, int ysize){
        rows = new DataRow[xsize];
        for(int x = 0; x < xsize; x++) rows[x] = new DataRow(ysize);
        xSize = xsize; ySize = ysize;
    }
    
    public final void setData(double[] data){
        double max = getMax(data); 
        this.clear();
        for(int i = 0; i < rows.length; i++) rows[i].setHeight(data[i]/max);
    }
    
    public void clear(){
        for (DataRow row : rows) row.clear();        
    }
    
    @Override
    public String toString(){                        
        StringBuilder str = new StringBuilder();
        str.append(String.format("xsize = %3d, ysize = %d\n",xSize, ySize));
        for(int y = ySize-1; y >=0; y--){
            for(int x = 0; x < xSize; x++){
                str.append(rows[x].row[y]).append(" ");
            }
            str.append("\n");
        }
        for(int i = 0; i < xSize; i++) str.append("--");
        return str.toString();
    }
    
    public static class DataRow {
        Character[]  row = null;
        public DataRow(int size){
            row = new Character[size];
        }
        
        public void clear(){
            for(int r = 0; r < row.length; r++) row[r] = ' ';
        }
        
        public void setHeight(double fraction){

            double  height  = row.length*fraction;
            double    left  = height - Math.floor(height);
            int     nBlocks = (int) Math.floor(height);

            int      nOrder = (int) Math.floor(left*Renderer.block.length);
            //System.out.printf("--- setting fraction %f, nblocks = %d\n",fraction, nBlocks);
            for(int i = 0; i < nBlocks; i++) row[i] = Renderer.block[row.length-1];
            if(nBlocks<row.length) row[nBlocks] = Renderer.block[nOrder];
        }
        
    }
    
    public static void main(String[] args){
        //for(String b : block) System.out.println(b);
        Renderer r = new Renderer(8,new double[]{5,8,6,2,4,5,2,2});
        System.out.println(r.toString());
        
    }
    
    
}
