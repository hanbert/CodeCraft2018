package com.elasticcloudservice.predict;

import com.elasticcloudservice.boxing.PutVMToPM;

import java.util.*;

/**
* @file Predict.java
* @CopyRight (C)
* @brief  预测算法入口
* @author HXH
* @date 2018-03-15
*/

public class Predict {

	public static String[] predictVm(String[] ecsContent, String[] inputContent) {
		/**
		 * @method: predictVm
		 * @param: ecsContent 训练数据
		 * @param: inputContent 输入文件
		 * @return: java.lang.String[]
		 * @description: 业务逻辑: 预测 & 放置
		 */

		//解析输入文件
        Flavor flavorList = new Flavor(); //存放flavor的配置信息
		Map<String, List<String> > input = Common.parseInput(inputContent, flavorList);

		//解析训练数据集
		History trainData = new History(ecsContent);
		trainData.show();

		//获取预测的时间间隔
		int days = Common.predictPeriod(input.get("start").get(0), input.get("end").get(0));

		//保存预测结果
		TreeMap<String, List<Integer> > predictResult = new TreeMap<>();

		//获取需要预测的flavor类型列表,并根据获取此flavor在过去每天的申请次数
		List<String> predictFlavor = input.get("flavors");
		for (String flavor : predictFlavor){
			//获取制定flavor类型在训练集中每一天的数目
			List<Integer> dataList = trainData.countOfFlavor(flavor);

			/** 滚动预测制定时间间隔的每种需要预测的flavor的值 **/

			//利用ARIMA模型预测
            //ArrayList<Integer> predictList = Common.rollingPredict(dataList, days);

			//利用线性回归预测
            //ArrayList<Integer> predictList = Common.LRPredict(dataList, days);

            //求加权平均
            ArrayList<Integer> predictListARIMA = Common.rollingPredict(dataList, days);
            ArrayList<Integer> predictListLR = Common.LRPredict(dataList, days);
            List<Integer> predictList = Common.weightedAverage(predictListARIMA, predictListLR);

            //保存预测结果
            predictResult.put(flavor, predictList);
        }

		//解析输出结果到字符串数组
		String[] results = Common.parseResults(predictResult, ecsContent.length);
		//Common.testData("./data/data_2015_5.txt");

        //将预测的虚拟机放置到物理机
        PutVMToPM place = new PutVMToPM(results, input, flavorList);
        place.putVM();
        place.parseResults(results);
        place.show();

		return results;
	}

}
