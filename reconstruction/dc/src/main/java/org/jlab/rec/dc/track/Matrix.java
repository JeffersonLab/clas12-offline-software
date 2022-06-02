/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.track;

/**
 *
 * @author gavalian
 */
public class Matrix {

    public double M[][] = new double[5][5];

    public Matrix(){

    }

    public double get(int i, int j){
        return M[i][j];
    }

    public void reset(){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                M[i][j] = 0.0;
            }
        }
    }


    public void set(double A[][]){
       for(int i = 0; i < 5; i++){
           for(int j = 0; j < 5; j++){
               M[i][j] = A[i][j];
           }
       }
    }

    public void set(double... values){
        if(values.length<25){
            System.out.println("[jnp::matrix] error *** number of arguments = " + values.length + " , expected 25" );
        } else {
            int count = 0;
            for(int j = 0; j < 5; j++){
                for(int i = 0; i < 5; i++){
                    M[i][j] = values[count]; count++;
                }
            }
        }
    }
}
