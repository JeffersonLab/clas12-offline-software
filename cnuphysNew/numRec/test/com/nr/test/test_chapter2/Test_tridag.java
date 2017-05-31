package com.nr.test.test_chapter2;

import static com.nr.test.NRTestUtil.*;
import static com.nr.la.Tridag.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_tridag {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps,diag=10.0;
    int i;
    double[] a=new double[50],b=new double[50],c=new double[50],r=new double[50],u=new double[50],rr=new double[50];
    boolean localflag, globalflag=false;
    ranvec(a);
    ranvec(b);
    ranvec(c);
    ranvec(r);
    for(i=0;i<50;i++) b[i] += diag;

    

    // Test tridag
    System.out.println("Testing tridag");
    tridag(a,b,c,r,u);
    // test solution u[]
    sbeps = 5.e-15;
    rr[0]=b[0]*u[0]+c[0]*u[1];
    rr[49]=a[49]*u[48]+b[49]*u[49];
    for(i=1;i<49;i++) rr[i]=a[i]*u[i-1]+b[i]*u[i]+c[i]*u[i+1];
    localflag = maxel(vecsub(rr,r)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** tridag: Inconsistant solution vector.\n");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
