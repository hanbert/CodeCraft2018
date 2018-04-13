package com.elasticcloudservice.timeserise;

import java.util.Random;
import java.util.Vector;

/**
* @file GeneralMethod.java
* @CopyRight (C)
* @brief  求解过程中调用到的一些方法
* @author HXH
* @date 2018-03-14
*/

public class GeneralMethod {

    public GeneralMethod(){}

    public double averageValue(double[] data){
        /**
         * @method: averageValue
         * @param: data
         * @return: double
         * @description: 求给定数据集的平均值
         */

        double summation = 0.0;

        for (int index = 0; index < data.length; index++){
            summation += data[index];
        }

        return summation / data.length;
    }

    public double varianceValue(double[] data){
        /**
         * @method: varianceValue
         * @param: data
         * @return: double
         * @description: 求数据集的方差, 最终返回的是方差的无偏估计
         */

        double variance = 0.0;
        double average = this.averageValue(data);

        if (data.length <= 1) return 0.0;
        for (int index = 0; index < data.length; index++){
            variance += (data[index] - average) * (data[index] - average);
        }

        return variance / (data.length - 1);
    }

    public double standardDeviationValue(double[] data){
        /**
         * @method: standardDeviationValue
         * @param: data
         * @return: double
         * @description: 求数据集标准差
         */

        return Math.sqrt(this.varianceValue(data));
    }

    public double[] autocorrelationCoefficient(double[] data, int order){
        /**
         * @method: autocorrelationCoefficient
         * @param: data
         * @param: order
         * @return: double[] 自协方差序列,也就是自协方差函数,也可以认为是自相关系数
         * @description: 计算自协方差, C(k) = sum((x(t) - average)*(x(t-k) - average))/(N - k)
         */

        double[] autocorrelationCoefficient = new double[order + 1];
        double average = this.averageValue(data);

        for (int interval = 0; interval <= order; interval++){
            autocorrelationCoefficient[interval] = 0.0;
            for (int index = 0; index < data.length - interval; index++){
                autocorrelationCoefficient[interval] += (data[index + interval] - average) * (data[index] - average);
            }
            autocorrelationCoefficient[interval] /= (data.length - interval);
        }

        return autocorrelationCoefficient;
    }

    public double[][] LevinsonSolve(double[] covariance){
        /**
         * @method: LevinsonSolve
         * @param: covariance 数据集的协方差
         * @return: double[][] 结果是个二维数组,第一行元素代表的是在迭代过程中的方差
         *                     其余元素代表的是迭代过程中存储的系数
         * @description: Levinson递推公式,用来求解Yule-Walker方程
         */

        int order = covariance.length - 1;
        double[][] result = new double[order + 1][order + 1];
        double[] sigmaSq = new double[order + 1];
        double sumTop, sumSub;

        sigmaSq[0] = covariance[0];
        result[1][1] = covariance[1] / sigmaSq[0];
        sigmaSq[1] = sigmaSq[0] * (1.0 - result[1][1] * result[1][1]);
        for (int index_i = 1; index_i < order; index_i++){
            sumTop = 0.0;
            sumSub = 0.0;
            for (int index_j = 1; index_j <= index_i; index_j++){
                sumTop += covariance[index_i + 1 - index_j] * result[index_i][index_j];
                sumSub += covariance[index_j] * result[index_i][index_j];
            }
            result[index_i + 1][index_i + 1] = (covariance[index_i + 1] - sumTop) / (covariance[0] - sumSub);
            for (int index_k = 1; index_k <= index_i; index_k++){
                result[index_i + 1][index_k] = result[index_i][index_k] - result[index_i + 1][index_i + 1]
                        * result[index_i][index_i + 1 - index_k];
            }
            sigmaSq[index_i + 1] = sigmaSq[index_i] * (1.0 - result[index_i + 1][index_i + 1]
                    * result[index_i + 1][index_i + 1]);
        }
        result[0] = sigmaSq;

        return result;
    }

    public double getModelAIC (Vector<double[]> vector, double[] data, int type){
        /**
         * @method: getModelAIC
         * @param: vector 模型的系数
         * @param: data 数据集
         * @param: type 选定的模型的类型,1:MA,2:AR,3:ARMA
         * @return: double 返回AIC值
         * @description: 求解AIC值
         */

        int length = data.length;
        int p = 0, q = 0;
        double tempAR, tempMA;
        double summationError = 0.0;
        Random random = new Random();

        if (type == 1){ //MA模型
            double[] coefficientMA = vector.get(0);
            q = coefficientMA.length;
            double[] errorData = new double[q];

            for (int index_i = q - 1; index_i < length; index_i++){
                tempMA = 0.0;
                for (int index_j = 1; index_j < q; index_j++){
                    tempMA += coefficientMA[index_j] * errorData[index_j];
                }
                for (int index_j = q - 1; index_j > 0; index_j--){
                    errorData[index_j] = errorData[index_j - 1];
                }
                errorData[0] = random.nextGaussian() * Math.sqrt(coefficientMA[0]);
                summationError += (data[index_i] - tempMA) * (data[index_i] - tempMA);
            }
            return (length - (q - 1)) * Math.log(summationError / (length - (q - 1))) + (q + 1) * 2;
        } else if (type == 2) { //AR模型
            double[] coefficientAR = vector.get(0);
            p = coefficientAR.length;
            for (int index_i = p - 1; index_i < length; index_i++){
                tempAR = 0.0;
                for (int index_j = 0; index_j < p - 1; index_j++){
                    tempAR += coefficientAR[index_j] * data[index_i - index_j - 1];
                }
                summationError += (data[index_i] - tempAR) * (data[index_i] - tempAR);
            }
            return (length - (p - 1)) * Math.log(summationError / (length - (p - 1))) + (p + 1) * 2;
        } else { //ARMA模型
            double[] coefficientAR = vector.get(0);
            double[] coefficientMA = vector.get(1);
            p = coefficientAR.length;
            q = coefficientMA.length;
            double[] errorData = new double[q];

            for (int index_i = p - 1; index_i < length; index_i++){
                tempAR = 0.0;
                for (int index_j = 0; index_j < p - 1; index_j++){
                    tempAR += coefficientAR[index_j] * data[index_i - index_j - 1];
                }
                tempMA = 0.0;
                for (int index_j = 1; index_j < q; index_j++){
                    tempMA += coefficientMA[index_j] * errorData[index_j];
                }
                //产生各个时期的噪声
                for (int index_j = q - 1; index_j > 0; index_j--){
                    errorData[index_j] = errorData[index_j - 1];
                }
                errorData[0] = random.nextGaussian() * Math.sqrt(coefficientMA[0]);
                //估计的方差之和
                summationError += (data[index_i] - tempAR - tempMA) * (data[index_i] - tempAR - tempMA);
            }
            return (length - (q + p - 1)) * Math.log(summationError / (length - (q + p - 1))) + (p + q) * 2;
        }
    }
}
