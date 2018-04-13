package com.elasticcloudservice.predict;

import java.util.*;

/**
* @file History.java
* @CopyRight (C)
* @brief  训练数据集,以及针对训练数据集的操作
* @author HXH
* @date 2018-03-18
*/
public class History {
    //存放解析完的历史数据
    private Map<String, Map<String, Integer> > history = new TreeMap<>();

    public History(){ }

    public History(String[] historyData) {
        /**
         * @method: History
         * @param: historyData
         * @return:
         * @description: 构造函数,根据已有的历史数据,初始化Histoy实例
         */
        //标记正在处理的日期
        String dealDate = "";

        //初始化history,只统计flavor1~flavor15
        for (int index = 1; index <= 15; index++) {
            Map<String, Integer> temp = new TreeMap<>();
            history.put("flavor" + index, temp);
        }

        for (int index = 0; index < historyData.length; index++) {
            if (historyData[index].contains("\t") && historyData[index].split("\t").length == 3) {
                String[] dataArray = historyData[index].split("\t");
                String flavor = dataArray[1];
                String date = dataArray[2].split(" ")[0];

                //把历史数据中的每一天都统计,这一天没有申请的flavor统计为0
                if (dealDate == null || !dealDate.equals(date)){
                    for (Map.Entry<String, Map<String, Integer> > entry : history.entrySet()){
                        if (entry.getValue() == null || !entry.getValue().containsKey(date)){
                            Map<String, Integer> temp = entry.getValue();
                            temp.put(date, 0);
                            entry.setValue(temp);
                        }
                    }
                    dealDate = date;
                }

                //只统计flavor1~flavor15
                if (history.containsKey(flavor)){
                    Integer temp = history.get(flavor).get(date);
                    temp++;
                    history.get(flavor).put(date,temp);
                }
            }
        }
    }

    public void show(){
        /**
         * @method: show
         * @param:
         * @return: void
         * @description: 输出data set的数据统计
         */

        if (history.size() > 0){
            Set<Map.Entry<String, Map<String, Integer> > > entrySet = history.entrySet();
            for (Map.Entry<String, Map<String,Integer> > entry : entrySet){
                int count = 0;
                System.out.println(entry.getKey());
                for (Map.Entry<String, Integer> countEntry : entry.getValue().entrySet()){
                    System.out.println(countEntry.getKey() + " " + countEntry.getValue());
                    count++;
                }
                System.out.println("Total: " + count);
            }
        }
    }

    public List<Integer> countOfFlavor(String flavor){
        /**
         * @method: countOfFlavor
         * @param: flavor
         * @return: java.lang.Integer[]
         * @description: 根据提供的flavor,统计每个flavor在数据集中出现的次数,以天为单位
         *               默认按时间升序排列,如:2015-01-01索引为0,则2015-01-02索引为2,以此类推
         */
        List<Integer> count = new ArrayList<>();

        if (history.size() > 0 && history.get(flavor).size() > 0){
            Set<Map.Entry<String, Integer> > countSet = history.get(flavor).entrySet();

            for (Map.Entry<String, Integer> entry : countSet){
                count.add(entry.getValue());
            }
        } else {
            System.out.println("Can't find the flavor.");
        }

        return count;
    }



}
