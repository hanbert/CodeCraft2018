package com.elasticcloudservice.predict;

import com.elasticcloudservice.linearregression.LinearRegression;
import com.elasticcloudservice.timeserise.ARIMAModel;
import com.filetool.util.FileUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* @file Common.java
* @CopyRight (C)
* @brief  通用方法集合
* @author HXH
* @date 2018-03-18
*/

public class Common {

    public static Map<String, List<String> > parseInput(String[] inputContent, Flavor flavorList){
        /**
         * @method: parse
         * @param: inputContent
         * @return: java.util.Map<java.lang.String,java.util.List<java.lang.String>>
         * @description: 解析输入文件,返回一个Map
         */

        Map<String, List<String> > input = new HashMap<>();
        String[] temp = inputContent[0].split(" ");

        //解析第1行信息
        if (temp.length > 0){
            List<String> value1 = new ArrayList<>();
            value1.add(temp[0]);
            input.put("cpu", value1);

            List<String> value2 = new ArrayList<>();
            value2.add(temp[1]);
            input.put("memory", value2);

            List<String> value3 = new ArrayList<>();
            value3.add(temp[2]);
            input.put("disk", value3);
        }

        //解析第3行信息, 第2行为空行
        List<String> value4 = new ArrayList<>();
        value4.add(inputContent[2]);
        input.put("numberOfFlavor", value4);

        //解析4 ~ 2+numberOfFlavor的信息
        List<String> value5 = new ArrayList<>();
        int numberOfFlavor = Integer.valueOf(inputContent[2]);
        for (int index = 0; index < numberOfFlavor; index++) {
            temp = inputContent[3 + index].split(" ");
            value5.add(temp[0]);
            flavorList.setFlavor(temp[0], Integer.valueOf(temp[1]), Integer.valueOf(temp[2]));
        }
        input.put("flavors", value5);

        //解析第numberOfFlavor+4行信息,numberOfFlavor+3行为空
        List<String> value6 = new ArrayList<>();
        value6.add(inputContent[numberOfFlavor + 4]);
        input.put("type", value6);

        //解析 numberOfFlavor+6 和 numberOfFlavor+7 行,
        //即预测的开始时间和终止时间
        if (inputContent[numberOfFlavor + 6].split(" ").length > 0){
            List<String> value7 = new ArrayList<>();
            value7.add(inputContent[numberOfFlavor + 6].split(" ")[0]);
            input.put("start", value7);
        }

        if (inputContent[numberOfFlavor + 7].split(" ").length > 0){
            List<String> value8 = new ArrayList<>();
            value8.add(inputContent[numberOfFlavor + 7].split(" ")[0]);
            input.put("end", value8);
        }

        return input;
    }

    public static ArrayList<Integer> rollingPredict(List<Integer> dataList, int days){
        /**
         * @method: rollingPredict
         * @param: dataList
         * @param: days 需要预测的天数
         * @return: java.util.ArrayList<java.lang.Integer>
         * @description: 根据需要预测的天数,进行滚动预测
         *               滚动预测时,每次也要加入预测值作为历史数据,用来预测下一天的值
         */

        //将预测的flavor类型及其最终的预测值
        ArrayList<Integer> predictResult = new ArrayList<>();
        //创建数据的数值数组,加入历史数据
        double[] data = new double[dataList.size() + days];
        //将List转换为double数组
        for (int index = 0; index < dataList.size(); index++) {
            data[index] = dataList.get(index);
        }

        //滚动预测, 把每次的预测值当做历史数据,预测下一天的值
        for (int index = 0; index < days; index++) {
            int predictValue = singlePointPredict(data, days);
            //当预测结果小于0的时候,修正到0
            if(predictValue < 0) predictValue = 0;
            predictResult.add(predictValue);
            data[dataList.size() + index] = predictValue;
        }

        return predictResult;
    }

    public static int singlePointPredict(double[] data, int days){
        /**
         * @method: singlePointPredict
         * @param: data 数据集
         * @return: int
         * @description: 用ARIMA每次预测一个点的值
         */

        //周期性,用于差分处理,可调节
        int period = 17;
        //控制每个点的预测次数,通过求多次预测的平均值作为预测值,可调节
        int predictTimes= 5000;
        //统计预测次数
        int count = 0;
        //存储每次预测的值
        int[] tempPredictValue = new int[predictTimes];

        //实例化ARIMA模型
        ARIMAModel ARIMA = new ARIMAModel(data);
        ArrayList<int[] > list = new ArrayList<>();

        //进行预测,预测制定次数,每次的值放到数组中
        for (int index = 0; index < predictTimes; index++) {
            //获取模型的参数, p: bestModel[0], q: bestModel[1], AIC: bestModel[2]
            int[] bestModel = ARIMA.getARIMAModel(period, list, (index == 0)? false : true);

            if (bestModel.length == 0){ //当无法获取到合适的参数的时候,用与预测值当天对应的日期的作为预测值
                tempPredictValue[index] = (int)data[data.length - period];
                count++;
                break;
            } else { //获取到合适的参数时,用参数进行预测
                //进行预测,获取预测值
                int differencePredictValue = ARIMA.predictValue(bestModel[0], bestModel[1], period);
                tempPredictValue[index] = ARIMA.afterDealDifference(differencePredictValue, period);
                count++;
            }

            //System.out.println("BestModel: " + bestModel[0] + " " + bestModel[1]);
            //将本次获取的bestModel储存起来,
            list.add(bestModel);
        }

        //求平均,确定预测值
        double summationPredictValue = 0.0;
        for (int index = 0; index < count; index++) {
            summationPredictValue += (double)tempPredictValue[index];
        }

        return (int)Math.round(summationPredictValue / count);
    }

    public static int predictPeriod(String startTime, String endTime){
        /**
         * @method: predictPeriod
         * @param: startDate 开始日期
         * @param: endDate 结束日期
         * @return: int
         * @description: 通过开始日期和结束日期,确定需要预测的周期,以天为单位
         */

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null, endDate = null;

        try {
            startDate = dateFormat.parse(startTime);
            endDate = dateFormat.parse(endTime);
        } catch (ParseException e){
            e.printStackTrace();
        }

        //获取时间间隔,以天为单位
        //向上取整,不足1天,按1天算
        int days = (int)Math.ceil( (endDate.getTime() - startDate.getTime() ) / (1000 * 60 * 60 * 24) );

        //间隔天数,加上当天,等于预测周期数
        //比如:2015-01-01 到 2015-01-07,间隔6天,共需要预测7个点
        return days;
    }

    public static void testData(String testFilePath){
        /**
         * @method: testData
         * @param: testFilePath 测试集文件路径
         * @return: void
         * @description: 统计测试集数据
         */

        String[] testContent = FileUtil.read(testFilePath, null);
        History testData = new History(testContent);
        testData.show();
    }

    public static String[] parseResults(TreeMap<String, List<Integer> > predictResult, Integer length){
        /**
         * @method: parseResult
         * @param: length
         * @return: java.lang.String[]
         * @description: 将结果解析成字符串数组
         */

        String[] results = new String[length];

        //解析预测结果
        int total = 0; //记录预测VM的总数
        int count = 1;
        System.out.println("\n========= 预测结果 ==========");
        for (Map.Entry<String, List<Integer>> entry : predictResult.entrySet()){
            System.out.println(entry.getKey());
            //每一种flavor的总数
            int summation = 0;
            for (Integer value : entry.getValue()){
                summation += value;
                System.out.print(value + " ");
            }
            results[count++] = entry.getKey() + " " + summation;
            total += summation;
            System.out.println("\n===========================");
        }
        results[count] = "";

        //记录总数到结果集
        results[0] = String.valueOf(total);

        return results;
    }

    public static ArrayList<Integer> LRPredict(List<Integer> dataList, int days){
        /**
         * @method: LRPredict
         * @param: dataList
         * @param: days
         * @return: java.util.ArrayList<java.lang.Integer>
         * @description: 利用线性回归进行预测
         */

        //预测的分组,可以调整
        int period = (int)Math.round(days * 2);
        //int period = 10;
        //参数:训练数据集, 分组周期, 迭代次数, 迭代步长, 每次迭代使用的样本数量, 损失阈值
        LinearRegression LR = new LinearRegression(dataList, period, 100000,
                                                    0.01, 2, 0.0001);
        LR.trainModel();
        ArrayList<Integer> predictList = LR.predict(dataList, days);

        return predictList;
    }

    public static ArrayList<Integer> weightedAverage(ArrayList<Integer> listARIMA, ArrayList<Integer> listLR){
        /**
         * @method: weightedAverage
         * @param: listARIMA
         * @param: listLR
         * @return: java.util.ArrayList<java.lang.Integer>
         * @description: 求ARIMA预测和LR预测的结果的加权
         */
        //权值,可以调整=
        double weight = 0.8;
        ArrayList<Integer> average = new ArrayList<>();

        if(listARIMA.size() == listLR.size()){
            for (int index = 0; index < listARIMA.size(); index++) {
                int temp = (int)Math.round(weight * listARIMA.get(index) + (1 - weight) * listLR.get(index));
                average.add(temp);
            }
        }

        return average;
    }

}
