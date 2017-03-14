/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reactions;

import org.jlab.clas.physics.Vector3;

/**
 *
 * @author gagikgavalian
 */
public class TransMatrix {
    double[][] tMatrix;
    double[]   partialMatrix;
    
    Vector3[] LabV;
    Vector3[] PrimeV;
    
    Vector3 Lab1;
    Vector3 Lab2;
    Vector3 Lab3;
    Vector3 Prime1;
    Vector3 Prime2;
    Vector3 Prime3;
    
    public TransMatrix()
    {
        tMatrix = new double[3][3];
        partialMatrix = new double[4];
        
        LabV   = new Vector3[3];
        PrimeV = new Vector3[3];
        
        for(int loop = 0; loop < 3; loop++)
        {
            LabV[loop] = new Vector3();
            PrimeV[loop] = new Vector3();
        }
        
        LabV[0].setXYZ(1.0, 0.0, 0.0);
        LabV[1].setXYZ(0.0, 1.0, 0.0);
        LabV[2].setXYZ(0.0, 0.0, 1.0);
        
        PrimeV[0].setXYZ(1.0, 0.0, 0.0);
        PrimeV[1].setXYZ(0.0, 1.0, 0.0);
        PrimeV[2].setXYZ(0.0, 0.0, 1.0);
        
        Lab1 = new Vector3(1.0, 0.0, 0.0);
        Lab2 = new Vector3(0.0, 1.0, 0.0);
        Lab3 = new Vector3(0.0, 0.0, 1.0);
        Prime1 = new Vector3(1.0, 0.0, 0.0);
        Prime2 = new Vector3(0.0, 1.0, 0.0);
        Prime3 = new Vector3(0.0, 0.0, 1.0);        
    }
    
    public double get(int row, int col)
    {
        return tMatrix[row][col];
    }
    
    public void set(int row, int col, double val)
    {
        tMatrix[row][col] = val;
    }
    
    double partialDet(int exrow, int excol)
    {
        int index = 0;
        for(int ir = 0; ir < 3; ir++)
            for(int ic = 0; ic < 3; ic++)
            {
                if(ir!=exrow&&ic!=excol)
                {
                    partialMatrix[index] = get(ir,ic);
                    index++;
                }
            }
        return (partialMatrix[3]*partialMatrix[0]-partialMatrix[1]*partialMatrix[2]);
    }
    
    public double det()
    {
        double determ = 0.0;
        double factor = +1.0;
        for(int ir=0;ir<3;ir++)
        {
            factor = +1.0;
            if(ir==1) factor = -1.0;
            determ += get(ir,0)*factor*partialDet(ir,0);
        }
        return determ;
    }
    
    double epsilon(int i, int j)
    {
        double e = 0.0;
        if(i==0||i==2){
            if(j==1){
                e = -1.;
            } else {
                e = +1.;
            }
        }
        
        if(i==1){
            if(j==1){
                e = +1.;
            } else {
                e = -1.;
            }
        }
        return e;
    }
    public void unit()
    {
        for(int ir = 0; ir < 3; ir++)
            for(int ic = 0; ic < 3; ic++)
                set(ir,ic,0.0);
        set(0,0,1.0);
        set(1,1,1.0);
        set(2,2,1.0);
    }
    public void setRow(int row, double v1, double v2, double v3)
    {
        set(row,0,v1);
        set(row,1,v2);
        set(row,2,v3);
    }
    
    public void compose(Vector3 vec)
    {
        PrimeV[1] = LabV[2].cross(vec);
        if(PrimeV[1].mag()==0)
        {
            //System.out.println("set prime vector to lab direction");
            PrimeV[1].setXYZ(0.0, 1.0, 0.0);
        }
        //System.out.println(" Mag = " + PrimeV[1].mag());
        PrimeV[1].unit();
        //System.out.println(" Mag unit = " + PrimeV[1].mag());
        PrimeV[2].setXYZ(vec.x(), vec.y(), vec.z());
        PrimeV[2].unit();
        PrimeV[0] = PrimeV[1].cross(PrimeV[2]);
        
        //System.out.println("-------> prime vector");
        /*for(int j = 0; j < 3; j++)
            System.out.println(PrimeV[j].x() + "   " +  PrimeV[j].y() + "   " + PrimeV[j].z() + "  " + PrimeV[j].mag());
        System.out.println("-------> lab vector");
        for(int j = 0; j < 3; j++)
            System.out.println(LabV[j].x() + "   " +  LabV[j].y() + "   " + LabV[j].z());
        */
        for(int ir = 0; ir < 3; ir++)
            for(int ic=0;ic<3;ic++)
                this.set(ir, ic, PrimeV[ir].dot(LabV[ic]));
    }
    
    public Vector3 mult(Vector3 v)
    {
        Vector3 trvec = new Vector3();
        trvec.setXYZ(get(0,0)*v.x()+get(0,1)*v.y()+get(0,2)*v.z(),
                get(1,0)*v.x()+get(1,1)*v.y()+get(1,2)*v.z(),
                get(2,0)*v.x()+get(2,1)*v.y()+get(2,2)*v.z()
                );
        return trvec;
    }
    
    public void inverse()
    {
        double[][] matRes = new double[3][3];
        double determ = this.det();
        if(determ==0.0)
        {
            System.err.println("----> error in transmatrix deteminant is 0.0 and it can not be inversed.");
            return;
        }
        
        double rvalue;
        
        for(int ir = 0; ir < 3; ir++)
            for(int ic = 0; ic < 3; ic++)
            {
                // Check this later if the reversed index is correct for epsilon.
                rvalue = this.epsilon(ic, ir)*partialDet(ir,ic)/determ;
                //rvalue = partialDet(ir,ic)/determ;
                matRes[ir][ic] = rvalue;
            }
        
        for(int ir = 0; ir < 3; ir++)
            for(int ic = 0; ic < 3; ic++)
            {
                this.set(ir,ic,matRes[ic][ir]);
                //this.set(ir,ic,matRes[ir][ic]);
            }
    }
    
    public void print()
    {
        System.out.println("\n");
        for(int ir = 0; ir < 3; ir++)
                System.out.println(String.format("%8.5f,%8.5f,%8.5f", 
                        get(ir,0),get(ir,1),get(ir,2)));

    }
}
