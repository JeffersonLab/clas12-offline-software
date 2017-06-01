package com.nr.test.test_chapter2;

import static com.nr.NRUtil.*;
import static com.nr.la.Tridag.cyclic;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;

public class Test_cyclic {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps,alpha,beta;
    int i,N=50;
    double[] a=new double[N],b=new double[N],c=new double[N],r=new double[N],x=new double[N],y=new double[N],u = buildVector(N,3.0);
    boolean localflag, globalflag=false;
    ranvec(a);
    ranvec(b);
    b=vecadd(b,u);
    ranvec(c);
    ranvec(r);
    Ran myran = new Ran(17);
    alpha=myran.doub();
    beta=myran.doub();

    

    // Test cyclic
    System.out.println("Testing cyclic");
    cyclic(a,b,c,alpha,beta,r,x);
    // test solution x[]
    sbeps = 5.e-14;
    y[0]=b[0]*x[0]+c[0]*x[1]+beta*x[N-1];
    y[N-1]=alpha*x[0]+a[N-1]*x[N-2]+b[N-1]*x[N-1];
    for(i=1;i<N-1;i++) y[i]=a[i]*x[i-1]+b[i]*x[i]+c[i]*x[i+1];
    localflag = maxel(vecsub(y,r)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cyclic: Inconsistant solution vector.\n");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
