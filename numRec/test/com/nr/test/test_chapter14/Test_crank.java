package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.crank;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_crank {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    doubleW s = new doubleW(0);
    double adata[]={0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0};
    double bdata[]={0.0,1.0,1.0,1.0,4.0,5.0,5.0,7.0,8.0,9.0};
    double cdata[]={1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
    double ddata[]={1.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0};
    double edata[]={1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,2.0};
    // Expected midranking
    double aexp[]={1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
    double bexp[]={1.0,3.0,3.0,3.0,5.0,6.5,6.5,8.0,9.0,10.0};
    double cexp[]={5.5,5.5,5.5,5.5,5.5,5.5,5.5,5.5,5.5,5.5};
    double dexp[]={1.0,6.0,6.0,6.0,6.0,6.0,6.0,6.0,6.0,6.0};
    double eexp[]={5.0,5.0,5.0,5.0,5.0,5.0,5.0,5.0,5.0,10.0};
    //Expected sums
    double as=0.0,bs=((27-3)+(8-2)),cs=(1000-10),ds=(729-9),es=(729-9);
    double[] a=buildVector(adata),b=buildVector(bdata),c=buildVector(cdata),d=buildVector(ddata),e=buildVector(edata);
    double[] ae=buildVector(aexp),be=buildVector(bexp),ce=buildVector(cexp),de=buildVector(dexp),ee=buildVector(eexp);
    boolean localflag=false,globalflag=false;

    

    // Test crank
    System.out.println("Testing crank");

    crank(a,s);
    localflag = localflag || maxel(vecsub(a,ae)) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect ranking of data set a[]");
      
    }

    localflag = localflag || (s.val-as) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect sum over ties for data set a[]");
      
    }


    crank(b,s);
    localflag = localflag || maxel(vecsub(b,be)) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect ranking of data set b[]");
      
    }

    localflag = localflag || (s.val-bs) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect sum over ties for data set b[]");
      
    }

    crank(c,s);
    localflag = localflag || maxel(vecsub(c,ce)) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect ranking of data set c[]");
      
    }

    localflag = localflag || (s.val-cs) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect sum over ties for data set c[]");
      
    }

    crank(d,s);
    localflag = localflag || maxel(vecsub(d,de)) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect ranking of data set d[]");
      
    }

    localflag = localflag || (s.val-ds) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect sum over ties for data set d[]");
      
    }

    crank(e,s);
    localflag = localflag || maxel(vecsub(e,ee)) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect ranking of data set e[]");
      
    }

    localflag = localflag || (s.val-es) > 0.0;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** crank: Incorrect sum over ties for data set e[]");
      
    }
    
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
