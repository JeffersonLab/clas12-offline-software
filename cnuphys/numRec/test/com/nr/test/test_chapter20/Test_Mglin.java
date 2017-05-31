package com.nr.test.test_chapter20;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.pde.Mglin;

public class Test_Mglin {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int JMAX=33;
    int i,j,midl=JMAX/2;
    double sbeps;
    double[][] f=new double[JMAX][JMAX], u=new double[JMAX][JMAX];
    boolean localflag, globalflag=false;

    

    // Test Mglin
    System.out.println("Testing Mglin");

    u[midl][midl]=2.0;
    Mglin mglin = new Mglin(u,20);u=mglin.u;
    
//    System.out.printf(endl << " Test that solution satisfies difference equations:";
//    System.out.printf(endl;
//    System.out.printf(setprecision(2);
//    for (i=NSTEP;i<JMAX-1;i+=NSTEP) {
//      for (j=NSTEP;j<JMAX-1;j+=NSTEP)
//        f[i][j]=(u[i+1][j]+u[i-1][j]+u[i][j+1]+u[i][j-1]
//          -4.0*u[i][j])*SQR(JMAX-1.0);
//      for (j=NSTEP;j<JMAX-1;j+=NSTEP)
//        System.out.printf(setw(10) << f[i][j];
//      System.out.printf(endl;
//    }

    // Test that solution satisfies difference equations
    sbeps=1.e-15;
    localflag = false;
    for (i=1;i<JMAX-1;i++) {
      for (j=1;j<JMAX-1;j++) {
        f[i][j]=(u[i+1][j]+u[i-1][j]+u[i][j+1]+u[i][j-1]
          -4.0*u[i][j])*SQR(JMAX-1.0);
        if (i != midl || j != midl)
          localflag = localflag || abs(f[i][j]) > sbeps;
        else
          localflag = localflag || abs(f[i][j]-2.0) > sbeps;
      }
    }

    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mglin: Solution u[i][j] does not satisfy the original difference equations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
