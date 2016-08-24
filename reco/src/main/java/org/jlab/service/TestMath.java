package org.jlab.service;

import java.io.FileNotFoundException;
import java.util.Random;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jlab.coda.jevio.EvioException;

import Jama.Matrix;

public class TestMath {

	public TestMath() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws FileNotFoundException, EvioException{
		 
		Random rand = new Random();
		//System.out.print("Current Time in milliseconds = ");
		
	    double[][] matrixData = { {rand.nextInt(50),rand.nextInt(50),rand.nextInt(50),-rand.nextInt(50),rand.nextInt(50)}, {rand.nextInt(50),rand.nextInt(50),rand.nextInt(50),-rand.nextInt(50),4},{1,2,rand.nextInt(50),-3,4}, {2,5,rand.nextInt(50),4.7,4}, {9.8,-5,7,1,4} };
	    long t1 = System.currentTimeMillis();
	   // System.out.println(System.currentTimeMillis());
		for(int i = 0; i<10000000; i++) {
			 
		     Matrix mat1 = new Matrix( matrixData);
		     Matrix imat1 = mat1.inverse();
		     
		}		
		long t = System.currentTimeMillis()-t1;
		System.out.println(" JAMA PROCESSING TIME = "+t);
		t1 = System.currentTimeMillis();
	     
		for(int i = 0; i<10000000; i++) {
			   
		     
		     RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		     RealMatrix pInverse = new LUDecomposition(m).getSolver().getInverse();
		     
		}		
		t = System.currentTimeMillis()-t1;
		System.out.println(" APACHE PROCESSING TIME = "+t);
	}
}
