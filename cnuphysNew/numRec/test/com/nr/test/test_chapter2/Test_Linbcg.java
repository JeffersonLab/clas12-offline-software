package com.nr.test.test_chapter2;

import static com.nr.NRUtil.*;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;
import org.netlib.util.intW;

import com.nr.la.Linbcg;
import com.nr.la.NRsparseMat;

public class Test_Linbcg {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double sbeps;
    int i,j;
    boolean localflag, globalflag=false;

    

    // Test Linbcg (and NRsparseMat)
    System.out.println("Testing Linbcg (and NRsparseMat)");
    final int NP=20;
    final int NSIZE=58;
    NRsparseMat sa = new NRsparseMat(NP,NP,NSIZE);
    sa.col_ptr[1]=2;
    for (i=1;i<NP-1;i++)
      sa.col_ptr[i+1]=sa.col_ptr[i]+3;
    sa.col_ptr[NP]=sa.col_ptr[NP-1]+2;
    sa.row_ind[0]=0;
    sa.row_ind[1]=1;
    sa.val[0]=3.0;
    sa.val[1]=-2.0;
    int k=1;
    for (j=1;j<NP-1;j++) {
      i=j-1;
      sa.row_ind[++k]=i;
      sa.val[k]=2.0;
      sa.row_ind[++k]=++i;
      sa.val[k]=3.0;
      sa.row_ind[++k]=++i;
      sa.val[k]=-2.0;
    }
    sa.row_ind[++k]=NP-2;
    sa.val[k]=2.0;
    sa.row_ind[++k]=NP-1;
    sa.val[k]=3.0;
    double[] bbb = buildVector(NP,1.0),xx = new double[NP];
    bbb[0]=3.0;
    bbb[NP-1] = -1.0;
    final int ITOL=1,ITMAX=75;
    final double TOL=1.0e-9;
    doubleW err = new doubleW(0);
    intW iter = new intW(0);

    TestLinbcg test = new TestLinbcg(sa);
    test.solve(bbb,xx,ITOL,TOL,ITMAX,iter,err);

    sbeps = 5.e-15;
    localflag = maxel(vecsub(sa.ax(xx),bbb)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Linbcg: Derived class gives inconsistent solution vector");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

  
  class TestLinbcg extends Linbcg {
    NRsparseMat a;
    
    TestLinbcg(NRsparseMat sa) {
      a = sa;
    }
    
    public void asolve(final double[] b, final double[] x, final int itrnsp) {
      for (int i=0;i<b.length;i++) {
        double diag=0.0;
        for (int j=a.col_ptr[i];j<a.col_ptr[i+1];j++)
          if (a.row_ind[j] == i) {
            diag=a.val[j];
            break;
          }
        x[i]=(diag != 0.0 ? b[i]/diag : b[i]);
      }
    }
    
    public void atimes(final double[] x, final double[] r, final int itrnsp) {
      double[] rr = null;
      if (itrnsp!=0)
        rr=a.atx(x);
      else
        rr=a.ax(x);
      System.arraycopy(rr, 0, r, 0, rr.length);
    }
  }

}
