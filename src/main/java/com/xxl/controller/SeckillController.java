package com.xxl.controller;

import com.xxl.config.ZookeeperWatcher;
import com.xxl.model.Product;
import com.xxl.service.OrderService;
import com.xxl.service.ProductService;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SeckillController {
    //定义jvm内存变量，存储商品是否卖完，卖完就不在请求redis了
    private static ConcurrentHashMap<Integer, Boolean> productSoldOutMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Boolean> getProductSoldOutMap() {
        return productSoldOutMap;
    }
    private final static String PRODUCT_STOCK = "product_stock_";
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ZooKeeper zooKeeper;

    @PostConstruct
    public void init(){
        List<Product> products = productService.listProduct();
        for (Product product : products) {
            stringRedisTemplate.opsForValue().set(PRODUCT_STOCK+product.getId(), product.getStock()+"");
        }
    }

    @PostMapping("/seckill/{productId}")
    public String seckill(@PathVariable()Integer productId) throws KeeperException, InterruptedException {
        //先判断内存标记
        if (productSoldOutMap.get(productId) != null){
            return "商品库存已经售完";
        }
        Long stock = stringRedisTemplate.opsForValue().decrement(PRODUCT_STOCK + productId);
        if (stock < 0){
            //设置内存变量
            productSoldOutMap.put(productId, true);
            //避免stock小于0后还被减了多次，导致创建订单失败后的还原失效。
            stock = stringRedisTemplate.opsForValue().increment(PRODUCT_STOCK+productId);

            // zookeeper 中设置售完标记， zookeeper 节点数据格式 product/1 true
            String productPath = "/" + ZookeeperWatcher.PRODUCT_STOCK_PREFIX + "/" + productId;
            if (zooKeeper.exists(productPath, true) == null) {
                zooKeeper.create(productPath, "true".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // 监听 zookeeper 售完节点
            zooKeeper.exists(productPath, true);

            System.out.println("===================stock=="+stock);
            return "商品已经售完";
        }

        try{
            orderService.secondKill(productId);
        }catch (Exception e){
            //创建订单失败，还原redis库存
            stringRedisTemplate.opsForValue().increment(PRODUCT_STOCK+productId);
            //清楚内存标记
            if (productSoldOutMap.get(productId) != null){
                productSoldOutMap.remove(productId);
            }

            // 通过 zookeeper 回滚其他服务器的 JVM 缓存中的商品售完标记
            String path = "/" + ZookeeperWatcher.PRODUCT_STOCK_PREFIX + "/" + productId;
            if (zooKeeper.exists(path, true) != null){
                zooKeeper.setData(path, "false".getBytes(), -1);
            }

            e.printStackTrace();
            return "fail";
        }
        return "success";
    }


}
