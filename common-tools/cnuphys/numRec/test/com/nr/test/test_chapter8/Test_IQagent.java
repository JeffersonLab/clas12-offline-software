package com.nr.test.test_chapter8;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.Ran;
import com.nr.sort.IQagent;

public class Test_IQagent {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=1000,M=100;
    double var,sbeps;
    double uu[]={1.0,25.0,50.0,75.0,100.0};
    double[] u=buildVector(uu),x = new double[11],y=new double[11],z = new double[5];
    boolean localflag, globalflag=false;

    

    // Test IQagent
    System.out.println("Testing IQagent");
    IQagent iq = new IQagent();
    Ran myran = new Ran(17);
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) iq.add(myran.doub());
    }

    // Percentiles for Ran
    sbeps=3.e-3;
    for (j=0;j<11;j++) {
      y[j]=0.1*j;
      x[j]=iq.report(y[j]);
    }
    localflag = (maxel(vecsub(x,y)) > sbeps);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** IQagent: Percentiles of uniform distribution are suspiciously nonuniform");
      
    }

    // Artificial distribution
    IQagent iq2 = new IQagent();
    for (j=0;j<M;j++) {
      for (i=0;i<N;i++) iq2.add((double)(i%100+1));
    }
    for (j=0;j<5;j++) {
      var=0.25*j;
      z[j]=iq2.report(var);
    }
    localflag = (maxel(vecsub(u,z))!= 0.0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** IQagent: Distribution with perfectly known percentiles failed");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
