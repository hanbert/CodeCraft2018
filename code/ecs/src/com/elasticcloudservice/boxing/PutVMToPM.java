package com.elasticcloudservice.boxing;

import com.elasticcloudservice.predict.Flavor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @file PutVMToPM.java
* @CopyRight (C)
* @brief  将预测出来的flavor放置到物理机
* @author HXH
* @date 2018-04-02
*/
public class PutVMToPM {

    //物理机列表,存放已经打开的物理机
    private List<PhysicalMachine> listPM = new ArrayList<>();
    //虚拟机列表,预测得到的虚拟机
    private List<VirtualMachine> listVM = new ArrayList<>();
    //flavor列表,存放flavor配置信息
    private Flavor flavorList;
    //保存优化的资源类型:cpu or memory
    private String tag;
    //物理机的cpu总量
    private int cpu;
    //物理机的memory总量
    private int memory;

    public PutVMToPM(String[] predictResult, Map<String, List<String> > inputInformation, Flavor flavorList) {
        /**
         * @method: PutVMToPM
         * @param: predictResult
         * @param: inputInformation
         * @return:
         * @description: 构造函数, 初始化一些基本信息
         */

        int numberOfFlavors = Integer.valueOf(inputInformation.get("numberOfFlavor").get(0));
        //获取资源优化类型
        this.tag = inputInformation.get("type").get(0);
        //生成flavor列表
        this.flavorList = flavorList;

        //获取当前数组中已经装入的元素值
        int predictResultLength = 0;
        for (String temp : predictResult){
            if (temp != null) predictResultLength++;
            else break;
        }

        //设置预测的VM的列表
        if (predictResultLength > 0 && predictResultLength - 2 == numberOfFlavors){
            for (int index = 0; index < numberOfFlavors; index++) {
                String[] temp = predictResult[1 + index].split(" ");
                if (temp.length == 2){
                    String flavorName = temp[0];
                    for (int i = 0; i < Integer.valueOf(temp[1]); i++) {
                        List<Object> tempList = flavorList.getFlavor(flavorName);
                        VirtualMachine vm = new VirtualMachine(flavorName, tag, (int)tempList.get(1),
                                                                (int)tempList.get(2) / 1024);
                        listVM.add(vm);
                    }
                } else {
                    System.out.println("设置预测虚拟机列表出错!");
                }
            }
        }
        //对预测虚拟机列表按照降序排序
        Collections.sort(listVM);

        //设置物理机的信息
        this.cpu = Integer.valueOf(inputInformation.get("cpu").get(0));
        this.memory = Integer.valueOf(inputInformation.get("memory").get(0));
    }

    public void putVM(){
        /**
         * @method: putVM
         * @param:
         * @return: void
         * @description: 采用BFD(Best Fit Decreasing)降序最佳适应算法,
         *               先对物品按降序排序，再按照最佳适应算法进行装箱。
         */

        //统计开的物理机数目
        int count = 1;
        //如果以cpu作为资源优化,则以cpu排序;反之,以memory排序
        for (int indexVM = 0; indexVM < listVM.size(); indexVM++) {
            VirtualMachine tempVM = listVM.get(indexVM);
            int cpuVM = tempVM.getCpu();
            int memoryVM = tempVM.getMemory();
            boolean isPlaced = false;

            //遍历已经打开的物理机,找到第一个能放下的虚拟机的物理机
            for (int indexPM = 0; indexPM < listPM.size(); indexPM++) {
                PhysicalMachine tempPM = listPM.get(indexPM);
                if(tempPM.getCpu() >= cpuVM && tempPM.getMemory() >= memoryVM){
                    tempPM.putVM(tempVM);
                    listPM.set(indexPM, tempPM);
                    isPlaced = true;
                }
            }

            //如果当前打开的物理机都不能够成功放下该虚拟机,开一台新的物理机
            if (! isPlaced){
                PhysicalMachine newPM = new PhysicalMachine(count++, this.tag, this.cpu, this.memory);
                newPM.putVM(tempVM);
                listPM.add(newPM);
            }

            //放置完一台虚拟机后,重新排序物理机
            Collections.sort(listPM);
        }
    }

    public void parseResults(String[] results){
        /**
         * @method: parseResults
         * @param: result
         * @return: void
         * @description: 将放置结果解析到结果数组
         */
        //获取当前数组中已经装入的元素值
        int index = 0;
        for (String temp : results){
            if (temp != null) index++;
            else break;
        }

        results[index++] = String.valueOf(listPM.size());

        for (int indexPM = 0; indexPM < listPM.size(); indexPM++) {
            PhysicalMachine tempPM = listPM.get(indexPM);
            String temp = tempPM.getSerialNumber() + "";
            for (Map.Entry<String, Integer> entry : tempPM.getFlavors().entrySet()) {
                temp = temp + " " + entry.getKey() + " " + entry.getValue();
            }
            results[index++] = temp;
        }
    }

    public void show(){
        /**
         * @method: show
         * @param:
         * @return: void
         * @description: 展示放置的结果
         */

        System.out.println("共有 " + listPM.size() + " 台物理机, 放置结果如下: ");
        for (PhysicalMachine pm : listPM){
            pm.show();
        }
    }
}
