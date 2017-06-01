package com.nr.test.test_chapter6;

import static com.nr.sf.Gamma.bico;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_bico {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=50;
    double sbeps=1.e-12;
    double y[]={1.0,5.0,10.0,10.0,5.0,1.0};
    double[] x= new double[N],z= new double[N];
    boolean localflag, globalflag=false;

    

    // Test bico
    System.out.println("Testing bico");

    // Test some small values
    localflag = bico(0,0) != 1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bico: Incorrect value for special case bico(0,0)=1");
      
    } 
    
    localflag = bico(1,1) != 1.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bico: Incorrect value for special case bico(1,1)=1");
      
    }

    for (i=0;i<6;i++) {
      localflag = localflag || bico(5,i) != y[i];
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** bico: Test of bico with small arguments failed");
        
      }
    }

    // Test recursion in k, for n < 171
    for (i=0;i<N;i++) {
      j=i+20;
      x[i]=bico(100,j)/bico(100,j+1);
      z[i]=(j+1.0)/(100.0-j);
    }
//    System.out.printf(maxel(vecsub(x,z)));
    localflag = localflag || maxel(vecsub(x,z)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bico: Test of recursion in k for n < 171 failed");
      
    }

    // Test recursion in k, for n > 171
    for (i=0;i<N;i++) {
      j=i+20;
      x[i]=bico(200,j)/bico(200,j+1);
      z[i]=(j+1.0)/(200.0-j);
    }
//    System.out.printf(maxel(vecsub(x,z)));
    localflag = localflag || maxel(vecsub(x,z)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bico: Test of recursion in k for n > 171 failed");
      
    }

    // Test recursion in n
    for (i=0;i<N;i++) {
      j=i+150;
      x[i]=bico(j,50)/bico(j+1,50);
      z[i]=1.0-50.0/(j+1);
    }
//    System.out.printf(maxel(vecsub(x,z)));
    localflag = localflag || maxel(vecsub(x,z)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bico: Test of recursion in n failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
