package com.nr.sp;

import static java.lang.Math.*;

public class BartlettWin implements WindowFun {

    @Override
    public double window(final int j, final int n) {
	return 1. - abs(2. * j / (n - 1.) - 1.);
    }

}
