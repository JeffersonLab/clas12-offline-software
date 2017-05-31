package com.nr.test.test_chapter16;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ci.Svm;
import com.nr.ci.Svmgausskernel;
import com.nr.ci.Svmlinkernel;
import com.nr.ci.Svmpolykernel;
import com.nr.ran.Normaldev;
import com.nr.ran.Ran;

public class Test_Svm {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,M=1000,N=2;
    double a,b,lambda,omega=1.3,test,yy;
    double[]x=new double[2];
    double[] y=new double[M];
    double[][] data=new double[M][N];
    boolean localflag, globalflag=false;

    

    // Test Svm
    System.out.println("Testing Svm");

    // Create two disjoint sets of points
    Ran myran=new Ran(17);
    for (i=0;i<M/2;i++) {
      y[i]=1.0;
      a=myran.doub();
      b=2.0*myran.doub()-1.0;
      data[i][0]=1.0+(a-b);
      data[i][1]=1.0+(a+b);
    }

    for (i=M/2;i<M;i++) {
      y[i]=-1.0;
      a=myran.doub();
      b=2.0*myran.doub()-1.0;
      data[i][0]=-1.0-(a-b);
      data[i][1]=-1.0-(a+b);
    }
    
    // Linear kernel
    Svmlinkernel linkernel=new Svmlinkernel(data,y);
    Svm linsvm=new Svm(linkernel);
    lambda=10;
    k=0;
    do {
      test=linsvm.relax(lambda,omega);
//      System.out.printf(test);
      k++;
    } while (test > 1.e-3 && k < 100);
    int nerror=0;
    for (i=0;i<M;i++) {
//      if (i%10 == 0) System.out.printf((y[i]==1.0) << " %f\n", (linsvm.predict(i) >= 1.0));
//      if ((y[i] == 1.0) != (linsvm.predict(i) >= 1.0))
//        System.out.printf(data[i][0] << " %f\n", data[i][1] << " %f\n", y[i] << " %f\n", linsvm.predict(i));
      nerror += ((y[i]==1.0) != (linsvm.predict(i) >= 0.0) ? 1 : 0);
    }
    System.out.printf("Errors: %d\n", nerror);

    // Polynomial kernel
    Svmpolykernel polykernel=new Svmpolykernel(data,y,1.0,1.0,2.0);
    Svm polysvm=new Svm(polykernel);
    lambda=10;
    k=0;
    do {
      test=polysvm.relax(lambda,omega);
//      System.out.printf(test);
      k++;
    } while (test > 1.e-3 && k < 100);
    nerror=0;
    for (i=0;i<M;i++) {
      nerror += ((y[i]==1.0) != (polysvm.predict(i) >= 0.0) ? 1 : 0);
    }
    System.out.printf("Errors: %d\n", nerror);

    // Gaussian kernel
    Svmgausskernel gausskernel=new Svmgausskernel(data,y,1.0);
    Svm gausssvm=new Svm(gausskernel);
    lambda=10;
    k=0;
    do {
      test=gausssvm.relax(lambda,omega);
//      System.out.printf(test);
      k++;
    } while (test > 1.e-3 && k < 100);
    nerror=0;
    for (i=0;i<M;i++) {
      nerror += ((y[i]==1.0) != (gausssvm.predict(i) >= 0.0) ? 1 : 0);
    }
    System.out.printf("Errors: %d\n", nerror);

    
    // Need to add tests for harder test case and resolve issue that the two
    // support vectors give an erroneous indication for two of the kernels above

    // Example similar to the book
    Normaldev ndev=new Normaldev(0.0,0.5,17);
    for (j=0;j<4;j++) {   // Four quadrants
      for (i=0;i<M/4;i++) {
        k=(M/4)*j+i;
        if (j == 0) {
          y[k]=1.0;
          data[k][0]=1.0+ndev.dev();
          data[k][1]=1.0+ndev.dev();
        } else if (j == 1) {
          y[k]=-1.0;
          data[k][0]=-1.0+ndev.dev();
          data[k][1]=1.0+ndev.dev();
        } else if (j == 2) {
          y[k]=1.0;
          data[k][0]=-1.0+ndev.dev();
          data[k][1]=-1.0+ndev.dev();
        } else {
          y[k]=-1.0;
          data[k][0]=1.0+ndev.dev();
          data[k][1]=-1.0+ndev.dev();
        }
      }
    }
        
    // Linear kernel
    Svmlinkernel linkernel2=new Svmlinkernel(data,y);
    Svm linsvm2=new Svm(linkernel2);
    System.out.printf("Errors: ");
    for (lambda=0.001;lambda<10000;lambda *= 10) {
      k=0;
      do {
        test=linsvm2.relax(lambda,omega);
//        System.out.printf(test);
        k++;
      } while (test > 1.e-3 && k < 100);
      nerror=0;
      for (i=0;i<M;i++) {
        nerror += ((y[i]==1.0) != (linsvm2.predict(i) >= 0.0) ? 1 : 0);
      }
      System.out.printf("%d ",nerror);
      // Test new data
      nerror=0;
      for (j=0;j<4;j++) {   // Four quadrants
        for (i=0;i<M/4;i++) {
          if (j == 0) {
            yy=1.0;
            x[0]=1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 1) {
            yy=-1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 2) {
            yy=1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          } else {
            yy=-1.0;
            x[0]=1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          }
          nerror += ((yy==1.0) != (linsvm2.predict(x) >= 0.0) ? 1 : 0);
        }
      }
      System.out.printf("%d    ",nerror);
    }
    System.out.println();

    // Polynomial kernel
    Svmpolykernel polykernel2 = new Svmpolykernel(data,y,1.0,1.0,4.0);
    Svm polysvm2=new Svm(polykernel2);
    System.out.printf("Errors: ");
    for (lambda=0.001;lambda<10000;lambda *= 10) {
      k=0;
      do {
        test=polysvm2.relax(lambda,omega);
//        System.out.printf(test);
        k++;
      } while (test > 1.e-3 && k < 100);
      // Test training set
      nerror=0;
      for (i=0;i<M;i++) {
        nerror += ((y[i]==1.0) != (polysvm2.predict(i) >= 0.0) ? 1 : 0);
      }
      System.out.printf("%d ",nerror);
      // Test new data
      nerror=0;
      for (j=0;j<4;j++) {   // Four quadrants
        for (i=0;i<M/4;i++) {
          if (j == 0) {
            yy=1.0;
            x[0]=1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 1) {
            yy=-1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 2) {
            yy=1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          } else {
            yy=-1.0;
            x[0]=1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          }
          nerror += ((yy==1.0) != (polysvm2.predict(x) >= 0.0) ? 1 : 0);
        }
      }
      System.out.printf("%d    ",nerror);
    }
    System.out.println();

    // Gaussian kernel
    Svmgausskernel gausskernel2=new Svmgausskernel(data,y,1.0);
    Svm gausssvm2=new Svm(gausskernel2);
    System.out.printf("Errors: ");
    for (lambda=0.001;lambda<10000;lambda *= 10) {
      k=0;
      do {
        test=gausssvm2.relax(lambda,omega);
//        System.out.printf(test);
        k++;
      } while (test > 1.e-3 && k < 100);
      nerror=0;
      for (i=0;i<M;i++) {
        nerror += ((y[i]==1.0) != (gausssvm2.predict(i) >= 0.0) ? 1 : 0);
      }
      System.out.printf("%d ",nerror);
      // Test new data
      nerror=0;
      for (j=0;j<4;j++) {   // Four quadrants
        for (i=0;i<M/4;i++) {
          if (j == 0) {
            yy=1.0;
            x[0]=1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 1) {
            yy=-1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=1.0+ndev.dev();
          } else if (j == 2) {
            yy=1.0;
            x[0]=-1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          } else {
            yy=-1.0;
            x[0]=1.0+ndev.dev();
            x[1]=-1.0+ndev.dev();
          }
          nerror += ((yy==1.0) != (gausssvm2.predict(x) >= 0.0) ? 1 : 0);
        }
      }
      System.out.printf("%d    ",nerror);
    }
    System.out.println();

  // Test the algorithm on test data after learning
  // Do a scan over lambda to find best value

    localflag = false;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Svm: *************************");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
