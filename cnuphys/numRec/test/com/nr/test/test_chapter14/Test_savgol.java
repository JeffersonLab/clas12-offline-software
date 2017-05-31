package com.nr.test.test_chapter14;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.stat.SavitzkyGolayFilter;

public class Test_savgol {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int NMAX=1000,NTEST=6;
    int mtest[]={2,2,2,2,4,4};
    int nltest[]={2,3,4,5,4,5};
    int nrtest[]={2,1,0,5,4,5};
    String ans[]={
      "                      -0.086  0.343  0.486  0.343 -0.086",
      "               -0.143  0.171  0.343  0.371  0.257",
      "         0.086 -0.143 -0.086  0.257  0.886",
      " -0.084  0.021  0.103  0.161  0.196  0.207  0.196  0.161  0.103  0.021 -0.084",
      "         0.035 -0.128  0.070  0.315  0.417  0.315  0.070 -0.128  0.035",
      "  0.042 -0.105 -0.023  0.140  0.280  0.333  0.280  0.140 -0.023 -0.105  0.042"};
    int i,j,m,nl,np,nr;
    double sum,sbeps;
    double[] c=new double[NMAX];
    boolean localflag,globalflag=false;

    

    // Test savgol
    System.out.println("Testing savgol");

    // Test against table from the book
    for (i=0;i<NTEST;i++) {
      m=mtest[i];
      nl=nltest[i];
      nr=nrtest[i];
      np=nl+nr+1;
      SavitzkyGolayFilter.savgol(c,np,nl,nr,0,m);

      sum=0.0;
      sbeps=1.e-15;
      for (j=0;j<np;j++) sum += c[j];
      localflag = abs(sum-1.0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** savgol: Sum of coefficients was not 1.0 for case "+ i);
        
      }

      Scanner sc = new Scanner(ans[i]);
      localflag=false;
      sbeps=1.e-3;
      double r;
      for (j=nl;j>=0;j--) {
        r = sc.nextDouble();
//        System.out.printf(c[j] << " %f\n", w << " %f\n", abs(c[j]-atof(w.c_str())));
        localflag = localflag || abs(c[j]-r) > sbeps;
      }
      for (j=0;j<nr;j++) {
        r = sc.nextDouble();
//        System.out.printf(c[np-1-j] << " %f\n", w << " %f\n", w.c_str());
        localflag = localflag || abs(c[np-1-j]-r) > sbeps;
      }
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** savgol: Computed coefficients do not agree with book for case "+ i);
        
      }

    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
