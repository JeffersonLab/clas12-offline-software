package com.nr.test.test_chapter10;

import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.min.Dynpro;
import com.nr.ran.Ran;

public class Test_dynpro {
  Ran myran=new Ran(17);
  Penalty pen = new Penalty();
  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,m,n,NSTAGE=6,NTRIAL=10;
    double test0,test1,testmin,sbeps;
    int[] nstate=new int[NSTAGE],path=new int[NSTAGE];
    boolean localflag, globalflag=false;

    

    // Test dynpro
    System.out.println("Testing dynpro");

    int[] v;
    for (i=0;i<NTRIAL;i++) {
      nstate[0]=1;
      nstate[NSTAGE-1]=1;
      for (j=1;j<NSTAGE-1;j++) {
        nstate[j] = 2+myran.int32p()%8;
//        System.out.printf(nstate[j] << " ";
      }
//      System.out.printf(endl;

      pen.fillmatrix();
      Dynpro dynpro = new Dynpro(){
        public  double cost(int j, int k, int i){
          return pen.c[j][k][i];
        }
      };
      v=dynpro.dynpro(nstate);
      test0=cost(v[0],v[1],0)+cost(v[1],v[2],1)+cost(v[2],v[3],2)
        +cost(v[3],v[4],3)+cost(v[4],v[5],4);

//      for (j=0;j<NSTAGE;j++)
//        System.out.printf(v[j] << " ";
//      System.out.printf(test0);

      // Test result by exhaustive search
      testmin=1.e99;
      for (j=0;j<nstate[1];j++) {
        for (k=0;k<nstate[2];k++) {   
          for (m=0;m<nstate[3];m++) {   
            for (n=0;n<nstate[4];n++) {
              test1=cost(0,j,0)+cost(j,k,1)+cost(k,m,2)+cost(m,n,3)+cost(n,0,4);
              if (test1 < testmin) {
                testmin=test1;
                path[0]=0; 
                path[1]=j; 
                path[2]=k; 
                path[3]=m; 
                path[4]=n; 
                path[5]=0;
              }
            }
          }
        }
      }

//      for (j=0;j<NSTAGE;j++)
//        System.out.printf(path[j] << " ";
//      System.out.printf(testmin);

      localflag=false;
      for (j=0;j<NSTAGE;j++)
        localflag = localflag || (path[j] != v[j]);
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** dynpro: Exhaustive search found a better path");
        
      }

      sbeps=1.e-15;
      localflag = abs(testmin-test0) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** dynpro: Calculated cost in exhaustive search was lower");
        
      }   

    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  public  double cost(int j, int k, int i){
    return pen.c[j][k][i];
  }
  
  class Penalty {
    double[][][] c = new double[10][10][6];

    Penalty() {
      fillmatrix();
    }

    void fillmatrix() {
      int i,j,k;  
      for (i=0;i<10;i++)
        for (j=0;j<10;j++)
          for (k=0;k<6;k++)
            c[i][j][k]=myran.doub();
    }

  }
}
