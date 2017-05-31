package com.nr.test.test_chapter20;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildMatrix;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.pde.Relaxation;

public class Test_sor {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,midl,JMAX=33;
    double rjac,sbeps,PI=acos(-1.0);
    double[][] a=buildMatrix(JMAX,JMAX,1.0),b=buildMatrix(JMAX,JMAX,1.0),c=buildMatrix(JMAX,JMAX,1.0);
    double[][] d=buildMatrix(JMAX,JMAX,1.0),e=buildMatrix(JMAX,JMAX,-4.0),f=buildMatrix(JMAX,JMAX,0.0);
    double[][] u=buildMatrix(JMAX,JMAX,0.0);
    boolean localflag, globalflag=false;

    

    // Test sor
    System.out.println("Testing sor");

    midl=JMAX/2;
    f[midl][midl]=2000.0/SQR(JMAX-1.0);
    rjac=cos(PI/JMAX);
    Relaxation.sor(a,b,c,d,e,f,u,rjac);

//    System.out.printf(endl << " Test that solution satisfies difference equations:";
//    System.out.printf(endl;
//    System.out.printf(setprecision(2);
//    for (i=NSTEP;i<JMAX-1;i+=NSTEP) {
//      for (j=NSTEP;j<JMAX-1;j+=NSTEP)
//        f[i][j]=u[i+1][j]+u[i-1][j]+u[i][j+1]
//          +u[i][j-1]-4.0*u[i][j];
//      for (j=NSTEP;j<JMAX-1;j+=NSTEP) System.out.printf(setw(10) << f[i][j];
//      System.out.printf(endl;
//    }

    // Test that solution satisfies difference equations
    sbeps=1.e-14;
    localflag=false;
    for (i=1;i<JMAX-1;i++) {
      for (j=1;j<JMAX-1;j++) {
        f[i][j]=u[i+1][j]+u[i-1][j]+u[i][j+1]
          +u[i][j-1]-4.0*u[i][j];
        if (i != midl || j != midl)
          localflag = localflag || abs(f[i][j]) > sbeps;
        else
          localflag = localflag || abs(f[i][j]-2000.0/SQR(JMAX-1.0)) > sbeps;
      }
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** sor: Solution u[i][j] does not satisfy the original difference equations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
