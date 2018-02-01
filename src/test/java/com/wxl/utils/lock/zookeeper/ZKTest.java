package com.wxl.utils.lock.zookeeper;

import org.apache.zookeeper.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wuxingle on 2018/1/26.
 * zk test
 */
public class ZKTest {

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
                    CreateMode.PERSISTENT);
            System.out.println("create1:" + cPath);

            Thread.currentThread().interrupt();

            cPath = zooKeeper.create("/test2", "haha".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("create2:" + cPath);

        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        } finally {
            if (zooKeeper != null) {
                try {
                    Thread.sleep(10000);
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
