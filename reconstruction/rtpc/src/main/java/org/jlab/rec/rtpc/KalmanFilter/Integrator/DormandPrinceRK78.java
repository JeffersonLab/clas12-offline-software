package org.jlab.rec.rtpc.KalmanFilter.Integrator;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.BetheBlochModel;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Particle;

public class DormandPrinceRK78 extends Integrator {

    public DormandPrinceRK78(Particle particle, BetheBlochModel betheBlochModel, int nvar) {

        this.numberOfVariables = nvar;
        this.particle = particle;
        this.model = betheBlochModel;
    }

    @Override
    public void ForwardStepper(double[] yInput, double h, double[] Field, Material material, double[] EnergyLoss) {

        double b21 = 1.0 / 18,
                b31 = 1.0 / 48.0,
                b32 = 1.0 / 16.0,

                b41 = 1.0 / 32.0,
                b42 = 0.0,
                b43 = 3.0 / 32.0,

                b51 = 5.0 / 16.0,
                b52 = 0.0,
                b53 = -75.0 / 64.0,
                b54 = 75.0 / 64.0,

                b61 = 3.0 / 80.0,
                b62 = 0.0,
                b63 = 0.0,
                b64 = 3.0 / 16.0,
                b65 = 3.0 / 20.0,

                b71 = 29443841.0 / 614563906.0,
                b72 = 0.0,
                b73 = 0.0,
                b74 = 77736538.0 / 692538347.0,
                b75 = -28693883.0 / 1125000000.0,
                b76 = 23124283.0 / 1800000000.0,

                b81 = 16016141.0 / 946692911.0,
                b82 = 0.0,
                b83 = 0.0,
                b84 = 61564180.0 / 158732637.0,
                b85 = 22789713.0 / 633445777.0,
                b86 = 545815736.0 / 2771057229.0,
                b87 = -180193667.0 / 1043307555.0,

                b91 = 39632708.0 / 573591083.0,
                b92 = 0.0,
                b93 = 0.0,
                b94 = -433636366.0 / 683701615.0,
                b95 = -421739975.0 / 2616292301.0,
                b96 = 100302831.0 / 723423059.0,
                b97 = 790204164.0 / 839813087.0,
                b98 = 800635310.0 / 3783071287.0,

                b101 = 246121993.0 / 1340847787.0,
                b102 = 0.0,
                b103 = 0.0,
                b104 = -37695042795.0 / 15268766246.0,
                b105 = -309121744.0 / 1061227803.0,
                b106 = -12992083.0 / 490766935.0,
                b107 = 6005943493.0 / 2108947869.0,
                b108 = 393006217.0 / 1396673457.0,
                b109 = 123872331.0 / 1001029789.0,

                b111 = -1028468189.0 / 846180014.0,
                b112 = 0.0,
                b113 = 0.0,
                b114 = 8478235783.0 / 508512852.0,
                b115 = 1311729495.0 / 1432422823.0,
                b116 = -10304129995.0 / 1701304382.0,
                b117 = -48777925059.0 / 3047939560.0,
                b118 = 15336726248.0 / 1032824649.0,
                b119 = -45442868181.0 / 3398467696.0,
                b1110 = 3065993473.0 / 597172653.0,

                b121 = 185892177.0 / 718116043.0,
                b122 = 0.0,
                b123 = 0.0,
                b124 = -3185094517.0 / 667107341.0,
                b125 = -477755414.0 / 1098053517.0,
                b126 = -703635378.0 / 230739211.0,
                b127 = 5731566787.0 / 1027545527.0,
                b128 = 5232866602.0 / 850066563.0,
                b129 = -4093664535.0 / 808688257.0,
                b1210 = 3962137247.0 / 1805957418.0,
                b1211 = 65686358.0 / 487910083.0,

                b131 = 403863854.0 / 491063109.0,
                b132 = 0.0,
                b133 = 0.0,
                b134 = -5068492393.0 / 434740067.0,
                b135 = -411421997.0 / 543043805.0,
                b136 = 652783627.0 / 914296604.0,
                b137 = 11173962825.0 / 925320556.0,
                b138 = -13158990841.0 / 6184727034.0,
                b139 = 3936647629.0 / 1978049680.0,
                b1310 = -160528059.0 / 685178525.0,
                b1311 = 248638103.0 / 1413531060.0,
                b1312 = 0.0,

                c1 = 14005451.0 / 335480064.0,
                // c2 = 0.0 ,
                // c3 = 0.0 ,
                // c4 = 0.0 ,
                // c5 = 0.0 ,
                c6 = -59238493.0 / 1068277825.0,
                c7 = 181606767.0 / 758867731.0,
                c8 = 561292985.0 / 797845732.0,
                c9 = -1041891430.0 / 1371343529.0,
                c10 = 760417239.0 / 1151165299.0,
                c11 = 118820643.0 / 751138087.0,
                c12 = -528747749.0 / 2220607170.0,
                c13 = 1.0 / 4.0;


        //
        // end of declaration !

        double[] ak2 = new double[numberOfVariables];
        double[] ak3 = new double[numberOfVariables];
        double[] ak4 = new double[numberOfVariables];
        double[] ak5 = new double[numberOfVariables];
        double[] ak6 = new double[numberOfVariables];
        double[] ak7 = new double[numberOfVariables];
        double[] ak8 = new double[numberOfVariables];
        double[] ak9 = new double[numberOfVariables];
        double[] ak10 = new double[numberOfVariables];
        double[] ak11 = new double[numberOfVariables];
        double[] ak12 = new double[numberOfVariables];
        double[] ak13 = new double[numberOfVariables];

        double[] yTemp = new double[numberOfVariables];
        double[] yIn = new double[numberOfVariables];
        double[] dydx = new double[numberOfVariables];

        int i;

        for (i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInput[i];
        }
        EquationOfMotion.ForwardRightHandSide(yIn, dydx, Field);   // 1st Stage - Not doing, getting passed

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + b21 * h * dydx[i];
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak2, Field);              // 2nd Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b31 * dydx[i] + b32 * ak2[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak3, Field);              // 3rd Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b41 * dydx[i] + b42 * ak2[i] + b43 * ak3[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak4, Field);              // 4th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b51 * dydx[i] + b52 * ak2[i] + b53 * ak3[i] +
                    b54 * ak4[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak5, Field);              // 5th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b61 * dydx[i] + b62 * ak2[i] + b63 * ak3[i] +
                    b64 * ak4[i] + b65 * ak5[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak6, Field);              // 6th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b71 * dydx[i] + b72 * ak2[i] + b73 * ak3[i] +
                    b74 * ak4[i] + b75 * ak5[i] + b76 * ak6[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak7, Field);               // 7th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b81 * dydx[i] + b82 * ak2[i] + b83 * ak3[i] +
                    b84 * ak4[i] + b85 * ak5[i] + b86 * ak6[i] +
                    b87 * ak7[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak8, Field);               // 8th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b91 * dydx[i] + b92 * ak2[i] + b93 * ak3[i] +
                    b94 * ak4[i] + b95 * ak5[i] + b96 * ak6[i] +
                    b97 * ak7[i] + b98 * ak8[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak9, Field);               // 9th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b101 * dydx[i] + b102 * ak2[i] + b103 * ak3[i] +
                    b104 * ak4[i] + b105 * ak5[i] + b106 * ak6[i] +
                    b107 * ak7[i] + b108 * ak8[i] + b109 * ak9[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak10, Field);              // 10th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b111 * dydx[i] + b112 * ak2[i] + b113 * ak3[i] +
                    b114 * ak4[i] + b115 * ak5[i] + b116 * ak6[i] +
                    b117 * ak7[i] + b118 * ak8[i] + b119 * ak9[i] +
                    b1110 * ak10[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak11, Field);              // 11th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b121 * dydx[i] + b122 * ak2[i] + b123 * ak3[i] +
                    b124 * ak4[i] + b125 * ak5[i] + b126 * ak6[i] +
                    b127 * ak7[i] + b128 * ak8[i] + b129 * ak9[i] +
                    b1210 * ak10[i] + b1211 * ak11[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak12, Field);              // 12th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b131 * dydx[i] + b132 * ak2[i] + b133 * ak3[i] +
                    b134 * ak4[i] + b135 * ak5[i] + b136 * ak6[i] +
                    b137 * ak7[i] + b138 * ak8[i] + b139 * ak9[i] +
                    b1310 * ak10[i] + b1311 * ak11[i] + b1312 * ak12[i]);
        }
        EquationOfMotion.ForwardRightHandSide(yTemp, ak13, Field);              // 13th and final Stage

        for (i = 0; i < numberOfVariables; ++i) {
            // Accumulate increments with proper weights

            yInput[i] = yIn[i] + h * (c1 * dydx[i] + c6 * ak6[i] +
                    c7 * ak7[i] + c8 * ak8[i] + c9 * ak9[i] + c10 * ak10[i]
                    + c11 * ak11[i] + c12 * ak12[i] + c13 * ak13[i]);

        }

        ForwardEnergyLoss(yInput, h, material, EnergyLoss);
    }

    @Override
    public void BackwardStepper(double[] yInput, double h, double[] Field, Material material, double[] EnergyLoss) {

        double b21 = 1.0 / 18,
                b31 = 1.0 / 48.0,
                b32 = 1.0 / 16.0,

                b41 = 1.0 / 32.0,
                b42 = 0.0,
                b43 = 3.0 / 32.0,

                b51 = 5.0 / 16.0,
                b52 = 0.0,
                b53 = -75.0 / 64.0,
                b54 = 75.0 / 64.0,

                b61 = 3.0 / 80.0,
                b62 = 0.0,
                b63 = 0.0,
                b64 = 3.0 / 16.0,
                b65 = 3.0 / 20.0,

                b71 = 29443841.0 / 614563906.0,
                b72 = 0.0,
                b73 = 0.0,
                b74 = 77736538.0 / 692538347.0,
                b75 = -28693883.0 / 1125000000.0,
                b76 = 23124283.0 / 1800000000.0,

                b81 = 16016141.0 / 946692911.0,
                b82 = 0.0,
                b83 = 0.0,
                b84 = 61564180.0 / 158732637.0,
                b85 = 22789713.0 / 633445777.0,
                b86 = 545815736.0 / 2771057229.0,
                b87 = -180193667.0 / 1043307555.0,

                b91 = 39632708.0 / 573591083.0,
                b92 = 0.0,
                b93 = 0.0,
                b94 = -433636366.0 / 683701615.0,
                b95 = -421739975.0 / 2616292301.0,
                b96 = 100302831.0 / 723423059.0,
                b97 = 790204164.0 / 839813087.0,
                b98 = 800635310.0 / 3783071287.0,

                b101 = 246121993.0 / 1340847787.0,
                b102 = 0.0,
                b103 = 0.0,
                b104 = -37695042795.0 / 15268766246.0,
                b105 = -309121744.0 / 1061227803.0,
                b106 = -12992083.0 / 490766935.0,
                b107 = 6005943493.0 / 2108947869.0,
                b108 = 393006217.0 / 1396673457.0,
                b109 = 123872331.0 / 1001029789.0,

                b111 = -1028468189.0 / 846180014.0,
                b112 = 0.0,
                b113 = 0.0,
                b114 = 8478235783.0 / 508512852.0,
                b115 = 1311729495.0 / 1432422823.0,
                b116 = -10304129995.0 / 1701304382.0,
                b117 = -48777925059.0 / 3047939560.0,
                b118 = 15336726248.0 / 1032824649.0,
                b119 = -45442868181.0 / 3398467696.0,
                b1110 = 3065993473.0 / 597172653.0,

                b121 = 185892177.0 / 718116043.0,
                b122 = 0.0,
                b123 = 0.0,
                b124 = -3185094517.0 / 667107341.0,
                b125 = -477755414.0 / 1098053517.0,
                b126 = -703635378.0 / 230739211.0,
                b127 = 5731566787.0 / 1027545527.0,
                b128 = 5232866602.0 / 850066563.0,
                b129 = -4093664535.0 / 808688257.0,
                b1210 = 3962137247.0 / 1805957418.0,
                b1211 = 65686358.0 / 487910083.0,

                b131 = 403863854.0 / 491063109.0,
                b132 = 0.0,
                b133 = 0.0,
                b134 = -5068492393.0 / 434740067.0,
                b135 = -411421997.0 / 543043805.0,
                b136 = 652783627.0 / 914296604.0,
                b137 = 11173962825.0 / 925320556.0,
                b138 = -13158990841.0 / 6184727034.0,
                b139 = 3936647629.0 / 1978049680.0,
                b1310 = -160528059.0 / 685178525.0,
                b1311 = 248638103.0 / 1413531060.0,
                b1312 = 0.0,

                c1 = 14005451.0 / 335480064.0,
                // c2 = 0.0 ,
                // c3 = 0.0 ,
                // c4 = 0.0 ,
                // c5 = 0.0 ,
                c6 = -59238493.0 / 1068277825.0,
                c7 = 181606767.0 / 758867731.0,
                c8 = 561292985.0 / 797845732.0,
                c9 = -1041891430.0 / 1371343529.0,
                c10 = 760417239.0 / 1151165299.0,
                c11 = 118820643.0 / 751138087.0,
                c12 = -528747749.0 / 2220607170.0,
                c13 = 1.0 / 4.0;


        //
        // end of declaration !

        double[] ak2 = new double[numberOfVariables];
        double[] ak3 = new double[numberOfVariables];
        double[] ak4 = new double[numberOfVariables];
        double[] ak5 = new double[numberOfVariables];
        double[] ak6 = new double[numberOfVariables];
        double[] ak7 = new double[numberOfVariables];
        double[] ak8 = new double[numberOfVariables];
        double[] ak9 = new double[numberOfVariables];
        double[] ak10 = new double[numberOfVariables];
        double[] ak11 = new double[numberOfVariables];
        double[] ak12 = new double[numberOfVariables];
        double[] ak13 = new double[numberOfVariables];

        double[] yTemp = new double[numberOfVariables];
        double[] yIn = new double[numberOfVariables];
        double[] dydx = new double[numberOfVariables];

        int i;

        for (i = 0; i < numberOfVariables; ++i) {
            yIn[i] = yInput[i];
        }
        EquationOfMotion.BackwardRightHandSide(yIn, dydx, Field);   // 1st Stage - Not doing, getting passed

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + b21 * h * dydx[i];
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak2, Field);              // 2nd Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b31 * dydx[i] + b32 * ak2[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak3, Field);              // 3rd Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b41 * dydx[i] + b42 * ak2[i] + b43 * ak3[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak4, Field);              // 4th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b51 * dydx[i] + b52 * ak2[i] + b53 * ak3[i] +
                    b54 * ak4[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak5, Field);              // 5th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b61 * dydx[i] + b62 * ak2[i] + b63 * ak3[i] +
                    b64 * ak4[i] + b65 * ak5[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak6, Field);              // 6th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b71 * dydx[i] + b72 * ak2[i] + b73 * ak3[i] +
                    b74 * ak4[i] + b75 * ak5[i] + b76 * ak6[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak7, Field);               // 7th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b81 * dydx[i] + b82 * ak2[i] + b83 * ak3[i] +
                    b84 * ak4[i] + b85 * ak5[i] + b86 * ak6[i] +
                    b87 * ak7[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak8, Field);               // 8th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b91 * dydx[i] + b92 * ak2[i] + b93 * ak3[i] +
                    b94 * ak4[i] + b95 * ak5[i] + b96 * ak6[i] +
                    b97 * ak7[i] + b98 * ak8[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak9, Field);               // 9th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b101 * dydx[i] + b102 * ak2[i] + b103 * ak3[i] +
                    b104 * ak4[i] + b105 * ak5[i] + b106 * ak6[i] +
                    b107 * ak7[i] + b108 * ak8[i] + b109 * ak9[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak10, Field);              // 10th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b111 * dydx[i] + b112 * ak2[i] + b113 * ak3[i] +
                    b114 * ak4[i] + b115 * ak5[i] + b116 * ak6[i] +
                    b117 * ak7[i] + b118 * ak8[i] + b119 * ak9[i] +
                    b1110 * ak10[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak11, Field);              // 11th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b121 * dydx[i] + b122 * ak2[i] + b123 * ak3[i] +
                    b124 * ak4[i] + b125 * ak5[i] + b126 * ak6[i] +
                    b127 * ak7[i] + b128 * ak8[i] + b129 * ak9[i] +
                    b1210 * ak10[i] + b1211 * ak11[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak12, Field);              // 12th Stage

        for (i = 0; i < numberOfVariables; ++i) {
            yTemp[i] = yIn[i] + h * (b131 * dydx[i] + b132 * ak2[i] + b133 * ak3[i] +
                    b134 * ak4[i] + b135 * ak5[i] + b136 * ak6[i] +
                    b137 * ak7[i] + b138 * ak8[i] + b139 * ak9[i] +
                    b1310 * ak10[i] + b1311 * ak11[i] + b1312 * ak12[i]);
        }
        EquationOfMotion.BackwardRightHandSide(yTemp, ak13, Field);              // 13th and final Stage

        for (i = 0; i < numberOfVariables; ++i) {
            // Accumulate increments with proper weights

            yInput[i] = yIn[i] + h * (c1 * dydx[i] + c6 * ak6[i] +
                    c7 * ak7[i] + c8 * ak8[i] + c9 * ak9[i] + c10 * ak10[i]
                    + c11 * ak11[i] + c12 * ak12[i] + c13 * ak13[i]);

        }

        BackwardEnergyLoss(yInput, h, material, EnergyLoss);

    }



}
