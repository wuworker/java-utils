package com.wxl.utils.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wuxingle on 2018/1/15.
 * zookeeper test
 */
public class ZookeeperTest {


    @Test
    public void test1() {
        ZooKeeper zooKeeper = null;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, (e) -> {
                System.out.println("接收到事件:" + e);
                if (e.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            System.out.println("连接成功");

            String cPath = zooKeeper.create("/test", "haha".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            System.out.println("create:" + cPath);

            zooKeeper.create("/test/1", "haha".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);

            Stat exists = zooKeeper.exists("/test", System.out::print);
            System.out.println("exists:" + exists);

            Stat stat = zooKeeper.setData("/test", "lala".getBytes(), -1);
            System.out.println("setData:" + stat);

            zooKeeper.delete("/test", -1);

        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
