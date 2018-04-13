package com.elasticcloudservice.timeserise;

import java.util.Vector;

/**
* @file ARModel.java
* @CopyRight (C)
* @brief AR模型类
* @author HXH
* @date 2018-03-14
*/

public class ARModel {
    //原始数据集
    private double[] data;
    //AR模型的阶数p
    private int p;

    public ARModel(double[] data, int p){
        this.data = data;
        this.p = p;
    }

    public Vector<double[]> coefficientAR(){
        /**
         * @method: coefficientAR
         * @param:
         * @return: java.util.Vector<double[]>
         * @description: 求解AR模型的系数
         */

        Vector<double[]> vector = new Vector<>();
        double[] coefficientAR = coefficientOfAR(data, p);

        vector.add(coefficientAR);

        return vector;
    }

    private double[] coefficientOfAR(double[] data, int p){
        /**
         * @method: coefficientOfAR
         * @param: data 数据集
         * @param: p 模型的阶数
         * @return: double[] AR模型的系数
         * @description: 得到AR模型的系数
         */

        double[] coefficient = new double[p + 1];
        GeneralMethod method = new GeneralMethod();
        double[] covariance = method.autocorrelationCoefficient(data, p);
        //Levinson递推公式求解模型参数
        double[][] result = method.LevinsonSolve(covariance);

        for (int index = 0; index < p; index++){
            coefficient[index] = result[p][index + 1];
        }
        //噪声参数
        coefficient[p] = result[0][p];

        return coefficient;
    }
}
