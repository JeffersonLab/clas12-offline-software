package cnuphys.magfield;

import java.util.Random;

public class FastMath {
	
	
	private static final int           SIZE                 = 1024;
    private static final float        STRETCH            = (float)Math.PI;
    // Output will swing from -STRETCH to STRETCH (default: Math.PI)
    // Useful to change to 1 if you would normally do "atan2(y, x) / Math.PI"

    // Inverse of SIZE
    private static final int        EZIS            = -SIZE;
    private static final float[]    ATAN2_TABLE_PPY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PPX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PNY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_PNX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NPY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NPX    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NNY    = new float[SIZE + 1];
    private static final float[]    ATAN2_TABLE_NNX    = new float[SIZE + 1];

    static
    {
        for (int i = 0; i <= SIZE; i++)
        {
            float f = (float)i / SIZE;
            ATAN2_TABLE_PPY[i] = (float)(StrictMath.atan(f) * STRETCH / StrictMath.PI);
            ATAN2_TABLE_PPX[i] = STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNY[i] = -ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNX[i] = ATAN2_TABLE_PPY[i] - STRETCH * 0.5f;
            ATAN2_TABLE_NPY[i] = STRETCH - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_NPX[i] = ATAN2_TABLE_PPY[i] + STRETCH * 0.5f;
            ATAN2_TABLE_NNY[i] = ATAN2_TABLE_PPY[i] - STRETCH;
            ATAN2_TABLE_NNX[i] = -STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
        }
    }

    /**
     * ATAN2
     */

    public static final float atan2(float y, float x)
    {
        if (x >= 0)
        {
            if (y >= 0)
            {
                if (x >= y)
                    return ATAN2_TABLE_PPY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PPX[(int)(SIZE * x / y + 0.5)];
            }
            else
            {
                if (x >= -y)
                    return ATAN2_TABLE_PNY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PNX[(int)(EZIS * x / y + 0.5)];
            }
        }
        else
        {
            if (y >= 0)
            {
                if (-x >= y)
                    return ATAN2_TABLE_NPY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NPX[(int)(EZIS * x / y + 0.5)];
            }
            else
            {
                if (x <= y) // (-x >= -y)
                    return ATAN2_TABLE_NNY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NNX[(int)(SIZE * x / y + 0.5)];
            }
        }
    }	
	
	
	
	public static final float atan2Deg(float y, float x) {
		return (float) Math.toDegrees(FastMath.atan2(y, x));
	}

	public static final float atan2DegStrict(float y, float x) {
		return (float) Math.toDegrees(Math.atan2(y, x));
	}

//	public static final float atan2(float y, float x) {
//		float add, mul;
//
//		if (x < 0.0f) {
//			if (y < 0.0f) {
//				x = -x;
//				y = -y;
//
//				mul = 1.0f;
//			} else {
//				x = -x;
//				mul = -1.0f;
//			}
//
//			add = -3.141592653f;
//		} else {
//			if (y < 0.0f) {
//				y = -y;
//				mul = -1.0f;
//			} else {
//				mul = 1.0f;
//			}
//
//			add = 0.0f;
//		}
//
//		float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);
//
//		int xi = (int) (x * invDiv);
//		int yi = (int) (y * invDiv);
//
//		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
//	}
//
//	private static final int ATAN2_BITS = 7;
//
//	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
//	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
//	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
//	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
//
//	private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);
//
//	private static final float[] atan2 = new float[ATAN2_COUNT];
//
//	static {
//		for (int i = 0; i < ATAN2_DIM; i++) {
//			for (int j = 0; j < ATAN2_DIM; j++) {
//				float x0 = (float) i / ATAN2_DIM;
//				float y0 = (float) j / ATAN2_DIM;
//
//				atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
//			}
//		}
//	}
	
	
	public static void main(String arg[]) {
		int dim = 633 * 2; // random number times 2

	      float maxDiff = 0.0f;
	      float sumDiff = 0.0f;

	      for (int i = 0; i < dim * dim; i++)
	      {
	         float x = (float) ((i % dim) - (dim / 2)) / (dim / 2);
	         float y = (float) ((i / dim) - (dim / 2)) / (dim / 2);
	         float slow = (float) Math.atan2(y, x);
	         float fast = FastMath.atan2(y, x);
	         float diff = Math.abs(slow - fast);
	         if (diff > maxDiff)
	            maxDiff = diff;
	         sumDiff += diff;
	      }

	      float avgDiff = sumDiff / (dim * dim);
	      
	      
	      //now time
	      int n = 10000;
	      Random ran = new Random(72326263);
	      float xa[] = new float[n];
	      float ya[] = new float[n];
	      for (int i = 0; i < n; i++) {
	    	  xa[i] = 1.0f - 2*ran.nextFloat();
	    	  ya[i] = 1.0f - 2*ran.nextFloat();
	      }
	      double suma = 0;
	      double sumb = 0;
	      
	      
	      long start = System.nanoTime();
	      for (int i = 0; i < n; i++) {
	    	  suma += atan2DegStrict(ya[i], xa[i]);
	      }
	      long standTime = start = System.nanoTime() - start;
	      
	      start = System.nanoTime();
	      for (int i = 0; i < n; i++) {
	    	  sumb += atan2Deg(ya[i], xa[i]);
	      }
	      long fastTime = start = System.nanoTime() - start;
	      
	      start = System.nanoTime();
	      System.out.println("maxDiff=" + maxDiff); // 0.007858515
	      System.out.println("avgDiff=" + avgDiff); // 0.002910751
	      
	      double ratio = ((double)standTime)/((double)fastTime);
	      System.out.println("\nStandard time: " + standTime);
	      System.out.println("    Fast time: " + fastTime);
	      System.out.println("ratio: " + ratio);
	      
	      System.out.println("\nsum (standard) " + suma);
	      System.out.println("    sum (fast) " + sumb);
	      double diff = 100.*Math.abs(suma-sumb)/suma;
	      System.out.println("Percent diff: " + diff);
	}

}
