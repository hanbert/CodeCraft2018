package com.elasticcloudservice.linearregression;

import java.util.*;
/**
* @file LinearRegression.java
* @CopyRight (C)
* @brief  线性回归,用SGD调参
* @author HXH
* @date 2018-04-10
*/
public class LinearRegression {
    //训练数据集合,每行表示一组数据
    private double[][] trainData;
    //训练数据集合的列数
    private int columns;
    //训练数据集合的行数
    private int rows;
    //训练数据集每行对应的Y值
    private double[] Y;
    //模型的权重
    private double[] weights;
    //迭代次数, 默认100
    private int numberIterations = 100;
    //梯度下降的步数,控制收敛速度,也叫作学习率,默认为0.01
    private double stepLength = 0.01;
    //每次训练使用的数据份数,默认使用1份
    private int minBatchFraction = 1;
    //随机数据的下标,指向训练数据集的行
    private List<Integer> randomRows;
    //误差阈值
    private double lossThreshold = -1.0;

    public LinearRegression(List<Integer> trainData, int period, int numberIterations,
                            double stepLength, int minBatchFraction, double lossThreshold) {
        /**
         * @method: LinearRegression
         * @param: trainData 训练集
         * @param: period 数据周期,表示多少个点一组
         * @param: numberIterations 迭代步骤
         * @param: stepLength 迭代步长
         * @param: minBatchFraction 训练数据份数
         * @param: loss 损失阈值
         * @return:
         * @description: 解析训练集,检查数据合法性
         */

        this.trainData = parseTrainData(trainData, period);
        this.weights = initWeights();
        this.numberIterations = numberIterations;
        this.stepLength = stepLength;
        this.minBatchFraction = minBatchFraction;
        this.lossThreshold = lossThreshold;
        checkData();
    }

    public LinearRegression(List<Integer> trainData, int period, double[] weights, int numberIterations,
                            double stepLength, int minBatchFraction, double lossThreshold) {
        /**
         * @method: LinearRegression
         * @param: trainData 训练集
         * @param: period 数据周期,表示多少个点一组
         * @param: weights 权重集
         * @param: numberIterations 迭代步骤
         * @param: stepLength 迭代步长
         * @param: minBatchFraction 训练数据份数
         * @param: loss 损失阈值
         * @return:
         * @description: 解析训练集,检查数据合法性
         */

        this.trainData = parseTrainData(trainData, period);
        this.weights = weights;
        this.numberIterations = numberIterations;
        this.stepLength = stepLength;
        this.minBatchFraction = minBatchFraction;
        this.lossThreshold = lossThreshold;
        checkData();
    }

    private double[][] parseTrainData(List<Integer> inputData, int period){
        /**
         * @method: parseTrainData
         * @param: inputData 每种flavor的每天的申请量
         * @param: period 表示多少个点为一组数据
         * @return: double[][]
         * @description: 将输入数据解析成二维数组,每一行代表一组数据
         *               把每一组数据的第一个数据放入到Y中,表示待预测的真实值
         *               其余数据组成训练的trainData
         *               同时设置行和列的值
         */

        this.rows = inputData.size() / period;
        this.columns = period;
        this.Y = new double[this.rows];
        double[][] tempData = new double[this.rows][this.columns];

        //数据按日期倒排,索引越小,日期越接近预测日期
        int index = inputData.size() - 1;
        for (int row = 0; row < this.rows; row++){
            //将每组数据的真实值放入到Y中
            this.Y[row] = inputData.get(index);
            tempData[row][0] = 1.0;
            index--;
            for (int column = 1; column < this.columns; column++){
                tempData[row][column] = inputData.get(index);
                index--;
            }
        }

        return tempData;
    }

    private void checkData(){
        /**
         * @method: checkData
         * @param:
         * @return: void
         * @description: 检查数据合法性
         */

        if (this.numberIterations <= 0) {
            System.out.println("迭代次数输入必须大于0");
            System.exit(0);
        }
        if (this.stepLength <= 0) {
            System.out.println("每次迭代的步数必须大于0");
            System.exit(0);
        }
        if (this.minBatchFraction <= 0) {
            System.out.println("每次迭代使用的数据份数必须大于0");
            System.exit(0);
        }
        if (this.weights.length != this.trainData[0].length) {
            System.out.println("输入的初始化权重集合，数组大小必须与训练集合大小相同");
            System.exit(0);
        }
        if ((this.lossThreshold < 0 || this.lossThreshold > 1) && this.lossThreshold != -1) {
            System.out.println("输入的误差阀值必须在0和1之间");
            System.exit(0);
        }
    }

    private double[] initWeights(){
        /**
         * @method: initWeights
         * @param:
         * @return: double[]
         * @description: 初始化权重,都设为1.0,即权重相等
         */

        double[] temp = new double[this.columns];
        //辅助权重,初始为0
        temp[0] = 0;
        ArrayList<Double> randomList = new ArrayList<>();
        for (int index = 0; index < this.columns; index++) {
            double rd = new Random().nextDouble() * 0.01;
            while (randomList.contains(rd)) rd = new Random().nextDouble();
            randomList.add(rd);
        }

        //按照降序排序
        Collections.sort(randomList, Collections.reverseOrder());

        //越接近预测日期的点的权重越大
        for (int index = 1; index < this.columns; index++) {
            temp[index] = randomList.get(index - 1);
        }

        return temp;
    }

    public void trainModel(){
        /**
         * @method: trainModel
         * @param:
         * @return: void
         * @description: 训练模型
         */

        double loss = 10;
        int count = this.numberIterations;
        //不断迭代,直至损失降至阈值以下,或者迭代次数达到上限
        while (count > 0 && (this.lossThreshold != -1.0 && loss > this.lossThreshold)){
            //更新权重
            updateWeights();
            //获取当前的损失值
            loss = getLoss();
            count--;
        }

        System.out.println("误差: " + loss);
        System.out.println("迭代次数: " + (this.numberIterations - count));
    }

    private void updateWeights(){
        /**
         * @method: updateWeights
         * @param:
         * @return: void
         * @description: 利用SGD,更新权重
         */

        this.randomRows = getRandomRows(this.minBatchFraction);
        for (int column = 0; column < this.columns; column++) {
            double dev = 0.0;
            for (Integer tempRow : this.randomRows) {
                double summation = 0.0;
                for (int index = 0; index < this.columns; index++) {
                    summation += this.weights[index] * this.trainData[tempRow][index];
                }
                dev += (summation - this.Y[tempRow]) * this.trainData[tempRow][column];
            }
            this.weights[column] -= this.stepLength * (dev / this.randomRows.size());
        }
    }

    private List<Integer> getRandomRows(int number){
        /**
         * @method: getRandom
         * @param: number 获取的随机数的个数,即对应训练时,使用的样本数量
         * @return: void
         * @description: 获取[0, this.rows)范围的内的随机数
         */

        List<Integer> temp = new ArrayList<>();
        //循环获取number个随机数,代表训练集的行值
        for (int index = 0; index < number; index++) {
            int random = new Random().nextInt(this.rows);
            //若当前这个随机数已经获取过,便重新获取一个,直到没有获取过为止
            while (temp.contains(random)) random = new Random().nextInt(this.rows);
            temp.add(random);
        }
        return temp;
    }

    private double getLoss(){
        /**
         * @method: getLoss
         * @param:
         * @return: double
         * @description: 计算损失
         */

        double loss = 0.0;

        for (int row = 0; row < this.rows; row++) {
            double summation = 0.0;
            for (int column = 0; column < this.columns; column++) {
                summation += this.weights[column] * this.trainData[row][column];
            }
            loss += Math.pow((summation - this.Y[row]), 2);
        }

        return loss;
    }

    public double[][] testModel(List<Integer> testData){
        /**
         * @method: predict
         * @param: testData
         * @return: double[][] 返回值为测试结果和实际结果组成的集合
         * @description: 测试模型
         */

        double[][] data = parseData(testData);
        double[][] result = new double[data.length][2];
        for (int row = 0; row < this.rows; row++) {
            //真实值
            result[row][0] = data[row][0];
            result[row][1] = this.weights[0];
            for (int column = 1; column < this.columns; column++) {
                //预测值
                result[row][1] += this.weights[column] * data[row][column - 1];
            }
        }

        System.out.println("============ 测试模型 ================");
        for (int index = 0; index < result.length; index++) {
            System.out.println("真实值: " + result[index][0] + "预测值: " + result[index][1]);
        }

        return result;
    }

    public double[][] parseData(List<Integer> inputData){
        /**
         * @method: parseData
         * @param: inputData
         * @param: period
         * @return: double[][]
         * @description: 将输入数据按分组解析成输入数据的二维数组
         */

        double[][] tempData = new double[this.rows][this.columns];

        //数据按日期倒排,索引越小,日期越接近预测日期
        int index = inputData.size() - 1;
        for (int row = 0; row < this.rows; row++){
            for (int column = 0; column < this.columns; column++){
                tempData[row][column] = inputData.get(index);
                index--;
            }
        }

        return tempData;
    }

    public ArrayList<Integer> predict(List<Integer> inputData, int days){
        double[][] data = parseData(inputData);
        double[] temp = new double[this.columns];
        ArrayList<Integer> result = new ArrayList<>();

        for (int index = 0; index < data[0].length; index++) {
            temp[index] = data[0][index];
        }

        for (int day = 0; day < days; day++) {
            double predictValue = weights[0];
            for (int column = 1; column < this.columns; column++) {
                predictValue += weights[column] * temp[column];
            }
            //对预测值小于0的,进行归0
            if (predictValue < 0.0) predictValue = 0.0;
            if (predictValue > 30.0) predictValue = 30.0;
            //四舍五入
            predictValue = Math.round(predictValue);
            //将预测结果加入结果集合
            result.add((int)predictValue);
            //将预测值加入输入序列,以便预测下一个值
            for (int index = 0; index < temp.length - 1; index++) {
                temp[index] = temp[index + 1];
            }
            temp[temp.length - 1] = predictValue;
        }
        return result;
    }

    public void showModel(){
        System.out.println("The weights list: ");
        for (int index = 0; index < weights.length; index++) {
            System.out.println(weights[index] + " ");
        }
    }

    /** getter & setter 方法 **/
    public double[][] getTrainData() {
        return trainData;
    }

    public void setTrainData(double[][] trainData) {
        this.trainData = trainData;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public double[] getY() {
        return Y;
    }

    public void setY(double[] y) {
        Y = y;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public int getNumberIterations() {
        return numberIterations;
    }

    public void setNumberIterations(int numberIterations) {
        this.numberIterations = numberIterations;
    }

    public double getStepLength() {
        return stepLength;
    }

    public void setStepLength(double stepLength) {
        this.stepLength = stepLength;
    }

    public int getMinBatchFraction() {
        return minBatchFraction;
    }

    public void setMinBatchFraction(int minBatchFraction) {
        this.minBatchFraction = minBatchFraction;
    }

    public List<Integer> getRandomRows() {
        return randomRows;
    }

    public void setRandomRows(List<Integer> randomRows) {
        this.randomRows = randomRows;
    }

    public double getLossThreshold() {
        return lossThreshold;
    }

    public void setLossThreshold(double lossThreshold) {
        this.lossThreshold = lossThreshold;
    }
}
