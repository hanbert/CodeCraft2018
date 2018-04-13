package com.elasticcloudservice.timeserise;

import java.util.Vector;

/**
* @file ARMAModel.java
* @CopyRight (C)
* @brief ARMA模型类
* @author HXH
* @date 2018-03-15
*/

public class ARMAModel {
    //数据集
    private double[] data;
    //AR模型的阶数
    private int p;
    //MA模型的阶数
    private int q;

    public ARMAModel(double[] data, int p, int q){
        this.data = data;
        this.p = p;
        this.q = q;
    }

    public Vector<double[]> coefficientARMA(){
        /**
         * @method: coefficientMR
         * @param:
         * @return: java.util.Vector<double[]>
         * @description: 求解ARMA模型的参数
         */

        Vector<double[]> vector = new Vector<>();

        //ARMA模型参数
        double[] coefficientARMA = coefficientOfARMA(data, p, q);
        //AR模型参数
        double[] coefficientAR = new double[p + 1];
        System.arraycopy(coefficientARMA, 0, coefficientAR, 0, coefficientAR.length);
        //MA模型参数
        double[] coefficientMA = new double[q + 1];
        System.arraycopy(coefficientARMA, p + 1, coefficientMA, 0, coefficientMA.length);

        vector.add(coefficientAR);
        vector.add(coefficientMA);

        return vector;
    }

    private double[] coefficientOfARMA(double[] data, int p, int q){
        /**
         * @method: coefficientOfARMA
         * @param: data 数据集
         * @param: p AR模型阶数
         * @param: q MA模型阶数
         * @return: double[] 返回ARMA模型参数
         * @description: 求解ARMA模型,首先根据原始数据求得AR模型的自回归系数(AR系数)
         *               利用AR系数与原始数据，求解的残差序列，根据残差序列的自协方差
         *               最终求得ARMA中MA系数
         */

        GeneralMethod method = new GeneralMethod();
        double[] allCovariance = method.autocorrelationCoefficient(data, p + q);
        double[] covariance = new double[p + 1];
        for (int index = 0; index < covariance.length; index++){
            covariance[index] = allCovariance[q + index];
        }
        double[][] ARResult = method.LevinsonSolve(covariance);

        //求解AR模型的自回归系数
        double[] coefficientAR = new double[p + 1];
        for (int index = 0; index < p; index++){
            coefficientAR[index] = ARResult[p][index + 1];
        }
        //噪声参数
        coefficientAR[p] = ARResult[0][p];

        //求阶MA模型的自回归系数
        double[] alpha = new double[p + 1];
        alpha[0] = -1;
        for (int index = 1; index <= p; index++){
            alpha[index] = coefficientAR[index - 1];
        }
        double[] paraGarma = new double[q + 1];
        for (int index_k = 0; index_k <= q; index_k++){
            double summation = 0.0;
            for (int index_i = 0; index_i <= p; index_i++){
                for (int index_j = 0; index_j <= p; index_j++){
                    summation += alpha[index_i] * alpha[index_j]
                            * allCovariance[Math.abs(index_k + index_i - index_j)];
                }
            }
            paraGarma[index_k] = summation;
        }
        double[][] MAResult = method.LevinsonSolve(paraGarma);
        double[] coefficientMA = new double[q + 1];
        for (int index = 1; index <= q; index++){
            coefficientMA[index] = MAResult[q][index];
        }
        //噪声参数
        coefficientMA[0] = MAResult[0][q];

        //求解ARMA模型的系数
        double[] coefficientARMA = new double[p + q + 2];
        for (int index = 0; index < coefficientARMA.length; index++){
            if (index < coefficientAR.length) {
                coefficientARMA[index] = coefficientAR[index];
            } else {
                coefficientARMA[index] = coefficientMA[index - coefficientAR.length];
            }
        }
        return coefficientARMA;
    }
}
