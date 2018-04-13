package com.elasticcloudservice.timeserise;

import java.util.Vector;

/**
* @file MAModel.java
* @CopyRight (C)
* @brief MA模型类
* @author HXH
* @date 2018-03-15
*/

public class MAModel {
    //数据集
    private double[] data;
    //MA模型的阶数
    private int q;

    public MAModel(double[] data, int q){
        this.data = data;
        this.q = q;
    }

    public Vector<double[]> coefficientMR(){
        /**
         * @method: coefficientMR
         * @param:
         * @return: java.util.Vector<double[]>
         * @description: 得到MR模型的参数
         */

        Vector<double[]> vector = new Vector<>();
        double[] coefficientMR = coefficientOfMR(data, q);

        vector.add(coefficientMR);

        return vector;
    }

    private double[] coefficientOfMR(double[] data, int q) {
        /**
         * @method: coefficientOfMR
         * @param: data 数据集
         * @param: q MR模型阶数
         * @return: double[] MR模型参数
         * @description: 求解MR模型的参数
         */

        //指定AR的阶层p, K*log(N),其中K为一整数,N为数据集长度
        int p = (int) Math.log(data.length);
        //求AR模型的协方差,并用Levinson递推公式计算模型参数
        GeneralMethod method = new GeneralMethod();
        double[] covariance = method.autocorrelationCoefficient(data, p);
        double[][] result = method.LevinsonSolve(covariance);
        double[] alpha = new double[p + 1];
        for (int index = 1; index <= p; index++) {
            alpha[index] = result[p][index];
        }

        double[] paraGrama = new double[q + 1];
        for (int index_i = 0; index_i <= q; index_i++){
            double summation = 0.0;
            for (int index_j = 0; index_j <= p - index_i; index_j++){
                summation += alpha[index_j] * alpha[index_i + index_j];
            }
            paraGrama[index_i] = summation / result[0][p];
        }

        double[][] temp = method.LevinsonSolve(paraGrama);
        double[] coefficientMR = new double[q + 1];
        for (int index = 1; index < coefficientMR.length; index++){
            coefficientMR[index] = -temp[q][index];
        }
        // 噪声参数
        coefficientMR[0] = 1 / temp[0][q];

        return coefficientMR;
    }
}
