package com.elasticcloudservice.boxing;

import java.util.Map;
import java.util.TreeMap;
/**
* @file PhysicalMachine.java
* @CopyRight (C)
* @brief  物理机类,用来表示物理机,以及一些必要的操作
* @author HXH
* @date 2018-04-06
*/
public class PhysicalMachine implements Comparable<PhysicalMachine> {

    private int serialNumber;
    private int cpu;
    private int memory;
    private String tag;
    private Map<String, Integer> flavors = new TreeMap<>();

    public PhysicalMachine(int serialNumber, String tag, int cpu, int memory) {
        /**
         * @method: PhysicalMachine
         * @param: serialNumber
         * @param: cpu
         * @param: memory
         * @return:
         * @description: 初始化物理机
         */

        this.serialNumber = serialNumber;
        this.tag = tag;
        this.cpu = cpu;
        this.memory = memory;

    }

    public void putVM(VirtualMachine VM){
        /**
         * @method: putVM
         * @param: VM
         * @return: void
         * @description: 放置VM,并更新当前物理机的容量
         */

        if (this.cpu >= VM.getCpu() && this.memory >= VM.getMemory()){
            //放置虚拟机
            if (flavors.containsKey(VM.getName())){ //已经含有这种类型的VM
                int count = flavors.get(VM.getName());
                flavors.put(VM.getName(), ++count);
            } else { //还没有这种类型的VM
                flavors.put(VM.getName(), 1);
            }
            //放置成功, 修改物理机剩余容量
            this.cpu -= VM.getCpu();
            this.memory -= VM.getMemory();
        } else {
            System.out.println("第 " + this.serialNumber + " 号前物理机容量不足!");
        }
    }

    @Override
    public int compareTo(PhysicalMachine PM) {
        /**
         * @method: compareTo
         * @param: PM
         * @return: int
         * @description: 重写比较器,用于Collections.sort(),升序
         */
        if ("CPU".equals(this.tag)) { //按cpu排序
            if (this.cpu > PM.cpu)
                return 1;
            else if (this.cpu == PM.cpu)
                return 0;
            else return -1;
        } else { //按内存排序
            if (this.memory > PM.memory)
                return 1;
            else if (this.memory == PM.memory)
                return 0;
            else return -1;
        }
    }

    public void show(){
        /**
         * @method: showVM
         * @param:
         * @return: void
         * @description: 显示当前物理机中放置的虚拟机
         */

        System.out.println("物理机编号: " + this.serialNumber);
        for (Map.Entry<String, Integer> entry : flavors.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }


    /** getter & setter 方法 **/
    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, Integer> getFlavors() {
        return flavors;
    }

    public void setFlavors(Map<String, Integer> flavors) {
        this.flavors = flavors;
    }
}
