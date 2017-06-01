package com.nr.sp;

import static com.nr.NRUtil.*;

public class WelchWin implements WindowFun {

    @Override
    public double window(final int j, final int n) {
	return 1. - SQR(2. * j / (n - 1.) - 1.);
    }

}
