package com.nr.test.test_chapter14;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildMatrix;
import static com.nr.stat.Stattests.cntab;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.sf.Gamma;

public class Test_cntab {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,j,ntotal,NTYPE=9,NMON=12;
    double sbeps;
    doubleW chisq=new doubleW(0),prob = new  doubleW(0),df =new doubleW(0);
    doubleW ccc=new doubleW(0),cramrv = new doubleW(0);
    
    double mydf,mychisq,mycramerv,myconting,myprob;
    String title="Accidental Deaths by Month and Type";
    String month[]={"jan","feb","mar","apr","may","jun",
      "jul","aug","sep","oct","nov","dec"};
    String accident[]={"Motor Vehicle","Falls","Drowning","Fires",
      "Choking","Fire-arms","Poisons","Gas-Poison","Other"};
    int incidence[]={
      3298,3304,4241,4291,4594,4710,4914,4942,4861,4914,4563,4892,
      1150,1034,1089,1126,1142,1100,1112,1099,1114,1079,999,1181,
      180,190,370,530,800,1130,1320,990,580,320,250,212,
      874,768,630,516,385,324,277,272,271,381,533,760,
      299,264,258,247,273,269,251,269,271,279,297,266,
      168,142,122,140,153,142,147,160,162,172,266,230,
      298,277,346,263,253,239,268,228,240,260,252,241,
      267,193,144,127,70,63,55,53,60,118,150,172,
      1264,1234,1172,1220,1547,1339,1419,1453,1359,1308,1264,1246};
    double[] rowsum=new double[NTYPE],colsum=new double[NMON];
    int[][] table = buildMatrix(NTYPE,NMON,incidence);
    double[][] mynij=new double[NTYPE][NMON];
    boolean localflag,globalflag=false;

    

    // Test cntab
    System.out.println("Testing cntab");

  /*
    System.out.printf(title);
    System.out.println("               ";
    for (i=0;i<NMON;i++)
      System.out.printf(setw(5) << month[i];
    System.out.printf(endl;
    for (i=0;i<NTYPE;i++) {
      System.out.printf(setw(15) << left << accident[i] << right;
      for (j=0;j<12;j++) System.out.printf(setw(5) << table[i][j];
      System.out.printf(endl;
    }
  */

    for (i=0;i<NTYPE;i++) {
      rowsum[i]=0;
      for (j=0;j<NMON;j++)
        rowsum[i] += table[i][j];
//      System.out.printf(rowsum[i]);
    }
//    System.out.printf(endl;

    for (j=0;j<NMON;j++) {
      colsum[j]=0.0;
      for (i=0;i<NTYPE;i++)
        colsum[j] += table[i][j];
//      System.out.printf(colsum[j]);
    }
//    System.out.printf(endl;

    ntotal=0;
    for (i=0;i<NTYPE;i++)
      ntotal += (int)(rowsum[i]);
//    System.out.printf(ntotal);

    mydf=NMON*NTYPE-NMON-NTYPE+1;
    mychisq=0.0;
    for (i=0;i<NTYPE;i++) {
      for (j=0;j<NMON;j++) {
        mynij[i][j]=rowsum[i]*colsum[j]/ntotal;
        mychisq += 1.0*SQR(table[i][j]-mynij[i][j])/mynij[i][j];
      }
    }
    mycramerv=sqrt(mychisq/ntotal/(NTYPE-1));
    myconting=sqrt(mychisq/(mychisq+ntotal));
    Gamma gam = new Gamma();
    myprob=gam.gammq(mydf/2.0,mychisq/2.0);

    cntab(table,chisq,df,prob,cramrv,ccc);

  /*
    System.out.printf(fixed << setprecision(4));
    System.out.println("chi-squared       %f\n", setw(15) << mychisq << " %f\n", chisq);
    System.out.println("degrees of freedom%f\n", setw(15) << mydf << " %f\n", df);
    System.out.println("cramer-v          %f\n", setw(15) << mycramerv << " %f\n", cramrv);
    System.out.println("contingency coeff.%f\n", setw(15) << myconting << " %f\n", ccc);
    System.out.println("probability       %f\n", scientific << setw(15) << myprob << " %f\n", prob);
  */

    sbeps=1.e-15;
    localflag = abs(chisq.val/mychisq-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cntab: Returned chisq differs from test routine calculation");
      
    }

    localflag = abs(df.val/mydf-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cntab: Returned degrees-of-freedom differs from test routine calculation");
      
    }

    localflag = abs(cramrv.val/mycramerv-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cntab: Returned Cramer-V differs from test routine calculation");
      
    }

    localflag = abs(ccc.val/myconting-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cntab: Returned Contingency Coefficient differs from test routine calculation");
      
    }

    localflag = abs(prob.val) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** cntab: Returned prob should be 0.0");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
