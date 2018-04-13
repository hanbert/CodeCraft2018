package com.elasticcloudservice.predict;

import java.util.*;
import com.filetool.util.FileUtil;

/**
* @file Flavor.java
* @CopyRight (C)
* @brief  存放flavor的规格
* @author HXH
* @date 2018-03-18
*/

public class Flavor {

    private List<String> flavorName = new ArrayList<>();
    private List<Integer> cpu = new ArrayList<>();
    private List<Integer> memory = new ArrayList<>();

    public Flavor() { }

    public Flavor(String flavorName, Integer cpu, Integer memory) {
            this.flavorName.add(flavorName);
            this.cpu.add(cpu);
            this.memory.add(memory);
    }

    public List<Object> getFlavor(String name) {
        /**
         * @method: getFlavor
         * @param: name flavor的名字
         * @return: java.util.List<java.lang.Object>
         * @description: 根据flavor的名字获取相应的flavor的信息
         */

        List<Object> flavor = new ArrayList<>();

        int index = this.flavorName.indexOf(name);
        if (index == -1) {
            System.out.println("This flavor is not in flavor list.");
            flavor.add(name + "is not in flavor list");
        } else {
            flavor.add(name);
            flavor.add(this.cpu.get(index));
            flavor.add(this.memory.get(index));
        }

        return flavor;
    }

    public void setFlavor(String flavorName, Integer cpu, Integer memory){
        /**
         * @method: setFlavor
         * @param: flavorName
         * @param: cpu
         * @param: memory
         * @return: void
         * @description: 根据给定的值,如果当前flavor不在列表中,则将其加入列表
         */

        if (this.flavorName.indexOf(flavorName) == -1) {
            this.flavorName.add(flavorName);
            this.cpu.add(cpu);
            this.memory.add(memory);
        }
    }

    public void fromConfig() { //在华为的判分平台上会报错,初步估计,不支持从文件读配置
        /**
         * @method: fromConfig
         * @param: configPath
         * @return: com.elasticcloudservice.predict.Flavor
         * @description: 通过配置文件生成Flavor规格列表
         */
        System.out.println(System.getProperty("user.dir"));
        //此处在判分平台可能报错,改成解析输入的时候,调用setFlavor来保存配置信息
        String configPath = System.getProperty("user.dir") + "/code/ecs/src/com/elasticcloudservice/predict/config.cfg";
        System.out.println(configPath);
        String[] config = FileUtil.read(configPath, null);

        for (int index = 0; index < config.length; index++) {
            if (config[index].contains(" ") && config[index].split(" ").length == 3){
                String[] configArray = config[index].split(" ");
                setFlavor(configArray[0], Integer.valueOf(configArray[1]), Integer.valueOf(configArray[2]));
            }
        }
    }

}
