package com.nr.test.test_chapter22;

import static com.nr.NRUtil.DBL_EPSILON;
import static com.nr.NRUtil.DBL_MANT_DIG;
import static com.nr.NRUtil.FLT_RADIX;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.lna.Machar;

public class Test_Machar {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    boolean localflag=false, globalflag=false;

    

    // Test Machar
    System.out.println("Testing Machar");

    Machar mch=new Machar();
//    mch.report();

    localflag = mch.ibeta != FLT_RADIX;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated radix does not agree with numeric_limits");
      
    }

    localflag = mch.it != DBL_MANT_DIG;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated number of mantissa digits does not agree with numeric_limits");
      
    }

    /*
    localflag = (mch.irnd==5); // && (numeric_limits<double>::round_style != 1);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated rounding method does not agree with numeric_limits");
    }
    */

    localflag = mch.eps != DBL_EPSILON;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated smallest step around 1.0 does not agree with numeric_limits");
      
    }

    localflag = mch.minexp != Double.MIN_EXPONENT; //numeric_limits<double>::min_exponent; // WHP added +1
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated minimum exponent does not agree with numeric_limits");
      
    }

    localflag = mch.maxexp != Double.MAX_EXPONENT+1; //numeric_limits<double>::max_exponent;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated maximum exponent does not agree with numeric_limits");
      
    }

    localflag = mch.xmin != Double.MIN_NORMAL;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated minimum normalized value does not agree with numeric_limits");
      
    }

    localflag = mch.xmax != Double.MAX_VALUE;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Machar: Calculated maximum finite value does not agree with numeric_limits");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
