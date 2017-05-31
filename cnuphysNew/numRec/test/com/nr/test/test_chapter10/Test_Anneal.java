package com.nr.test.test_chapter10;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.min.Anneal;
import com.nr.ran.Ran;

public class Test_Anneal {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,k,N=100,M=10;
    double length,linit,rms,ave;
    int[] iorder= new int[N];
    double[] x= new double[N],y= new double[N],l=new double[M];
    boolean localflag, globalflag=false;

    

    // Test Anneal
    System.out.println("Testing Anneal");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      x[i]=myran.doub();
      y[i]=myran.doub();
      iorder[i]=i;
    }
    Anneal anl=new Anneal();

    // Initial length
    length=0;
    for (i=0;i<N-1;i++)
      length += anl.alen(x[iorder[i]],x[iorder[i+1]],y[iorder[i]],y[iorder[i+1]]);
    length += anl.alen(x[iorder[N-1]],x[iorder[0]],y[iorder[N-1]],y[iorder[0]]);
    linit=length;
//    System.out.printf(linit);
    for (k=0;k<M;k++) {
      anl.order(x,y,iorder);

      // Final length
      length=0;
      for (i=0;i<N-1;i++)
        length += anl.alen(x[iorder[i]],x[iorder[i+1]],y[iorder[i]],y[iorder[i+1]]);
      length += anl.alen(x[iorder[N-1]],x[iorder[0]],y[iorder[N-1]],y[iorder[0]]);
      l[k]=length;
//    System.out.printf(l[k]);
    }

    rms=0.0;
    ave=0.0;
    for (k=0;k<M;k++) ave += l[k];
    ave /= M;
    for (k=0;k<M;k++) rms += SQR(l[k]-ave);
    rms = sqrt(rms/M);
//    System.out.printf(ave << " " << rms);

    localflag = (linit/ave < 7.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Anneal: Did not achieve expected improvement in path length");
      
    }

    localflag = (rms/ave > 0.10);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Anneal: Successive runs did not result is substantially same path length");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
