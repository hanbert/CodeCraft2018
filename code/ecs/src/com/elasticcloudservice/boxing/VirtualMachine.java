package com.elasticcloudservice.boxing;
/**
* @file VirtualMachine.java
* @CopyRight (C)
* @brief  虚拟机类,用来表示虚拟机,以及一些常用的操作
* @author HXH
* @date 2018-04-06
*/
public class VirtualMachine implements Comparable<VirtualMachine>{
    private String name;
    private int cpu;
    private int memory;
    private String tag;

    public VirtualMachine(String name, String tag, int cpu, int memory) {
        this.name = name;
        this.cpu = cpu;
        this.memory = memory;
        this.tag = tag;
    }

    @Override
    public int compareTo(VirtualMachine VM) {
        /**
         * @method: compareTo
         * @param: VM
         * @return: int
         * @description: 重写比较方法,用于Collections.sort(),降序
         */

        if ("CPU".equals(this.tag)){
            if (this.cpu > VM.cpu)
                return -1;
            else if (this.cpu == VM.cpu)
                return 0;
            else return 1;
        } else {
            if(this.memory > VM.memory)
                return -1;
            else if (this.memory == VM.memory)
                return 0;
            else return 1;
        }
    }


    /** getter & setter 方法 **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
