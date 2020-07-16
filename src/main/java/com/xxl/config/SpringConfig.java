package com.xxl.config;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Value("${zookeeper.address}")
    private String zkAddr;

    @Bean
    public ZooKeeper initZookeeper() throws Exception {
        // 创建观察者
        ZookeeperWatcher watcher = new ZookeeperWatcher();
        // 创建 Zookeeper 客户端
        ZooKeeper zooKeeper = new ZooKeeper(zkAddr, 30000, watcher);
        // 将客户端注册给观察者
        watcher.setZooKeeper(zooKeeper);
        // 将配置好的 zookeeper 返回
        return zooKeeper;
    }

}
