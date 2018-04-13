package com.elasticcloudservice.timeserise;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

/**
* @file ARIMAModel.java
* @CopyRight (C)
* @brief
* @author HXH
* @date 2018-03-15
*/

public class ARIMAModel {
    //原始数据集
    private double[] data = {};
    //ARIMA参数
    private Vector<double[]> coefficientARIMA = new Vector<>();

    public ARIMAModel(){}

    public ARIMAModel(double[] data){
        this.data = data;
    }

    public double[] preFirstDifference(double[] preData){
        /**
         * @method: preFirstDifference
         * @param: preData
         * @return: double[]
         * @description: 处理一阶差分, period=1
         */

        double[] tempData = new double[preData.length - 1];
        for (int index = 0; index < preData.length - 1; index++){
            tempData[index] = preData[index + 1] - preData[index];
        }
        return tempData;
    }

    public double[] preSeasonDifference(double[] preData, int period){
        /**
         * @method: preSeasonDifference
         * @param: preData 原始数据集
         * @param: period 差分的周期
         * @return: double[]
         * @description: 用后period数据减去当前数据,消除季节性差异
         */

        //用后period个数据减去当前数据,有period数据不可用
        double[] tempData = new double[preData.length - period];
        for (int index = 0; index < preData.length - period; index++){
            tempData[index] = preData[index + period] - preData[index];
        }
        return tempData;
    }

    public double[] preDealdifference(int period){
        /**
         * @method: preDealdifference
         * @param: period
         * @return: double[]
         * @description: 根据period值,进行不同差分处理
         *               分一阶差分和季节性差分,或者不做处理
         */

        if (period >= data.length - 1) period = 0;

        switch (period){
            case 0: //不做处理
                return data;
            case 1: //一阶差分
                return preFirstDifference(data);
            default: //季节性差分
                return preSeasonDifference(data, period);
        }
    }

    public int[] getARIMAModel(int period, ArrayList<int[]> notModel, boolean needNot){
        /**
         * @method: getARIMAModel
         * @param: period 用来确定用怎么样的差分处理
         * @param: notModel
         * @param: needNot
         * @return: int[]
         * @description: 求解p,q参数,确定了p,q的值,便确定了ARIMA模型
         *               通过遍历模型参数列表的方式由AIC准则或者BIC准则确定最佳p、q阶数
         *               赤池信息准则（Akaike Information Criterion，AIC)。
         *               贝叶斯信息准则（Bayesian Information Criterion，BIC）
         *               本方法是选择AIC最小的模型,主要是防止过度拟合
         */

        double[] dataAfterDifference = preDealdifference(period);
        double minAIC = Double.MAX_VALUE;
        int[] bestModel = new int[3];
        int type = 0; //记录模型类型
        Vector<double[]> coefficient;
        int length = dataAfterDifference.length;
        if (length > 5) length = 5;
        int size = ((length + 2) * (length + 1)) / 2 - 1;
        int[][] model = new int[size][2];
        int count = 0;

        //初始化model列表,也就是p,q参数的组合
        for (int index_i = 0; index_i <= length; index_i++){
            for (int index_j = 0; index_j <= length - index_i; index_j++){
                if (index_i == 0 && index_j == 0) continue;
                model[count][0] = index_i;
                model[count++][1] = index_j;
            }
        }
        //选择p,q参数
        for (int index_i = 0; index_i < model.length; index_i++){
            boolean token = false;
            if (needNot){
                for (int index_j = 0; index_j < notModel.size(); index_j++){
                    if (model[index_i][0] == notModel.get(index_j)[0] &&
                            model[index_i][1] == notModel.get(index_j)[1]){
                        token = true;
                        break;
                    }
                }
            }
            if (token) continue;

            //选择模型
            if (model[index_i][0] == 0){
                MAModel MA = new MAModel(dataAfterDifference, model[index_i][1]);
                coefficient = MA.coefficientMR();
                type = 1;
            } else if (model[index_i][1] == 0){
                ARModel AR = new ARModel(dataAfterDifference, model[index_i][0]);
                coefficient = AR.coefficientAR();
                type = 2;
            } else {
                ARMAModel ARMA = new ARMAModel(dataAfterDifference, model[index_i][0], model[index_i][1]);
                coefficient = ARMA.coefficientARMA();
                type = 3;
            }

            double aic = new GeneralMethod().getModelAIC(coefficient, dataAfterDifference, type);
            // 在求解过程中如果阶数选取过长，可能会出现NAN或者无穷大的情况
            if (Double.isFinite(aic) && !Double.isNaN(aic) && aic < minAIC){
                minAIC = aic;
                bestModel[0] = model[index_i][0];
                bestModel[1] = model[index_i][1];
                bestModel[2] = (int)Math.round(minAIC);
                this.coefficientARIMA = coefficient;
            }
        }
        return bestModel;
    }

    public int afterDealDifference(int predictValue, int period){
        /**
         * @method: afterDealDifference
         * @param: predictValue 预测值
         * @param: period 采用的差分处理类型
         * @return: int 反差分后的预测值
         * @description: 对预测值进行反差分处理
         */

        if (period >= data.length) period = 0;
        switch (period){
            case 0:
                return predictValue;
            case 1:
                return (int)(predictValue + data[data.length - 1]);
            default:
                return (int)(predictValue + data[data.length - period]);
        }
    }

    public int predictValue(int p, int q, int period){
        /**
         * @method: predictValue
         * @param: p AR模型的阶数
         * @param: q MA模型的阶数
         * @param: period 差分处理类型
         * @return: int 返回预测值
         * @description: 进行预测
         */

        double[] dataAfterDifference = preDealdifference(period);
        int length = dataAfterDifference.length;
        int predict = 0;
        double tempAR = 0.0, tempMA = 0.0;
        double[] errorData = new double[q + 1];
        Random random = new Random();

        if (p == 0){ //用MA去预测
            double[] coefficientMA = coefficientARIMA.get(0);

            for (int index_i = q; index_i < length; index_i++){
                tempMA = 0;
                for (int index_j = 1; index_j <= q; index_j++){
                    tempMA += coefficientMA[index_j] * errorData[index_j];
                }
                //差生各个时刻的噪声
                for (int index_j = q; index_j > 0; index_j--){
                    errorData[index_j] = errorData[index_j - 1];
                }
                errorData[0] = random.nextGaussian() * Math.sqrt(coefficientMA[0]);
            }
            //产生预测
            predict = (int)tempMA;
        } else if (q == 0){ //用AR去预测
            double[] coefficientAR = coefficientARIMA.get(0);

            for (int index_i = p; index_i < length; index_i++){
                tempAR = 0;
                for (int index_j = 0; index_j < p; index_j++) {
                    tempAR += coefficientAR[index_j] * dataAfterDifference[index_i - index_j - 1];
                }
            }
            //产生预测
            predict = (int)tempAR;
        } else { //用ARMA去预测
            double[] coefficientAR = coefficientARIMA.get(0);
            double[] coefficientMA = coefficientARIMA.get(1);

            for (int index_i = p; index_i < length; index_i++){
                tempAR = 0;
                tempMA = 0;
                for (int index_j = 0; index_j < p; index_j++){
                    tempAR += coefficientAR[index_j] * dataAfterDifference[index_i - index_j - 1];
                }
                for (int index_j = 1; index_j <= q; index_j++){
                    tempMA += coefficientMA[index_j] * errorData[index_j];
                }
                //产生各个时刻的噪声
                for (int index_j = q; index_j > 0; index_j--){
                    errorData[index_j] = errorData[index_j - 1];
                }
                errorData[0] = random.nextGaussian() * Math.sqrt(coefficientMA[0]);
            }
            //产生预测值
            predict = (int)(tempAR + tempMA);
        }
        return predict;
    }
}
