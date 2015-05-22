package com.nr.test.test_chapter14;

import static com.nr.NRUtil.buildVector;
import static com.nr.stat.Stattests.spear;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_spear {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    doubleW d1=new doubleW(0),zd1 = new doubleW(0),probd1 = new doubleW(0);
    doubleW rs1=new doubleW(0),probrs1 = new doubleW(0);
    doubleW d2=new doubleW(0),zd2 = new doubleW(0),probd2 = new doubleW(0);
    doubleW rs2=new doubleW(0),probrs2 = new doubleW(0);
    
    double sbeps=1.e-6;
    double adata[]={0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0};
    double bdata[]={9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.0,0.0};
    double cdata[]={1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,0.0}; // Note 0.0 at end
    double edata[]={1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0};
    double fdata[]={2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0,2.0,1.0};
    // Expected results for each test case
    double ae[]={0.0,-3.0,0.0026998,1.0,0.0};
    double be[]={330.0,3.0,0.0026998,-1.0,0.0};
    double ce[]={90.0,-1.363636,0.172682,0.454545,0.186905};
    double ee[]={250.0,3.0,0.0026998,-1.0,0.0};
    double[] a=buildVector(adata),b=buildVector(bdata),c=buildVector(cdata);
    double[] e=buildVector(edata),f=buildVector(fdata);
    boolean localflag=false,globalflag=false;

    

    // Test spear
    System.out.println("Testing spear");

    // Test 1
    spear(a,a,d1,zd1,probd1,rs1,probrs1);
//    System.out.printf(d1 << " %f\n", zd1 << " %f\n", probd1 << " %f\n", rs1 << " %f\n", probrs1);
    localflag = localflag || (d1.val != ae[0]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Squared difference of ranks not zero for identical distributions");
      
    }

    localflag = localflag || (zd1.val != ae[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected standard deviation (should be -3) for special case");
      
    }

    localflag = localflag || abs(probd1.val-ae[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected probability for d, given the standard deviation");
      
    }

    localflag = localflag || (rs1.val != ae[3]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Spearman's rank correlation should be 1 for identical distributions");
      
    }

    localflag = localflag || (probrs1.val != ae[4]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Probrs should be zero for identical distributions");
      
    }

    // Test 2
    spear(a,b,d1,zd1,probd1,rs1,probrs1);
//    System.out.printf(d1 << " %f\n", zd1 << " %f\n", probd1 << " %f\n", rs1 << " %f\n", probrs1);
    localflag = localflag || (d1.val != be[0]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Sum squared difference of ranks should be 2*(81+49+25+9+1)=330");
      
    }

    localflag = localflag || (zd1.val != be[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Standard deviation should be (330/55)-3=3");
      
    }

    localflag = localflag || abs(probd1.val-be[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected probability for d, given the standard deviation");
      
    }

    localflag = localflag || (rs1.val != be[3]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Spearman's rank correlation should be -1 for perfect anticorrelation");
      
    }

    localflag = localflag || abs(probrs1.val - be[4])>1.e-16;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Probrs should be zero for perfect anticorrelation");
      
    }

    // Test 3
    spear(b,a,d2,zd2,probd2,rs2,probrs2);
//    System.out.printf(d2 << " %f\n", zd2 << " %f\n", probd2 << " %f\n", rs2 << " %f\n", probrs2);
    localflag = localflag || (d1.val != d2.val) || (zd1.val != zd2.val) || 
        (probd1.val != probd2.val) || (rs1.val != rs2.val) || (probrs1.val != probrs2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Results changed when two arrays were swapped (case 1)");
      
    }

    // Test 4
    spear(a,c,d1,zd1,probd1,rs1,probrs1);
//    System.out.printf(d1 << " %f\n", zd1 << " %f\n", probd1 << " %f\n", rs1 << " %f\n", probrs1);
    localflag = localflag || (d1.val != ce[0]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Sum squared difference of ranks should be 9*(1^2)+1*(9^2)=90");
      
    }

    localflag = localflag || abs(zd1.val-ce[1]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Standard deviation should be (90/55)-3=-1.363636");
      
    }

    localflag = localflag || abs(probd1.val-ce[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected probability for d, given the standard deviation");
      
    }

    localflag = localflag || abs(rs1.val-ce[3]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Spearman's rank correlation should be 1-6*90/990=0.454545");
      
    }

    localflag = localflag || abs(probrs1.val-ce[4]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected probability for rs, given the rank correlation");
      
    }

    // Test 5
    spear(c,a,d2,zd2,probd2,rs2,probrs2);
//    System.out.printf(d2 << " %f\n", zd2 << " %f\n", probd2 << " %f\n", rs2 << " %f\n", probrs2);
    localflag = localflag || (d1.val != d2.val) || (zd1.val != zd2.val) ||
        (probd1.val != probd2.val) || (rs1.val != rs2.val) || (probrs1.val != probrs2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Results changed when two arrays were swapped (case 2)");
      
    }

    // Test 6
    spear(e,f,d1,zd1,probd1,rs1,probrs1);
//    System.out.printf(d1 << " %f\n", zd1 << " %f\n", probd1 << " %f\n", rs1 << " %f\n", probrs1);
    localflag = localflag || (d1.val != ee[0]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Sum squared difference of ranks should be 10*(8-3)^2=250");
      
    }

    localflag = localflag || (zd1.val != ee[1]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Standard deviation should be (250-125)/55/(1-240/990)=3");
      
    }

    localflag = localflag || abs(probd1.val-ee[2]) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Unexpected probability for d, given the standard deviation");
      
    }

    localflag = localflag || (rs1.val != ee[3]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Spearman's rank correlation should be -1 for perfect anticorrelation (case 2)");
      
    }

    localflag = localflag || (probrs1.val != ee[4]);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Probrs should be zero for perfect anticorrelation (case 2)");
      
    }

    // Test 7
    spear(f,e,d2,zd2,probd2,rs2,probrs2);
//    System.out.printf(d2 << " %f\n", zd2 << " %f\n", probd2 << " %f\n", rs2 << " %f\n", probrs2);
    localflag = localflag || (d1.val != d2.val) || (zd1.val != zd2.val) ||
        (probd1.val != probd2.val) || (rs1.val != rs2.val) || (probrs1.val != probrs2.val);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** spear: Results changed when two arrays were swapped (case 3)");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
