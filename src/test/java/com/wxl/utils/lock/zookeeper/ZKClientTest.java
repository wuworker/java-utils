package com.wxl.utils.lock.zookeeper;

import com.wxl.utils.ThreadUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.*;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by wuxingle on 2018/1/26.
 * zk client test
 */
public class ZKClientTest {



    @Test
    public void testZKClient1(){
        ZkConnection zkConnection = new ZkConnection("127.0.0.1:2181",5000);
        ZkClient zkClient = new ZkClient(zkConnection,5000);
        System.out.println("connect success");
        System.out.println("--------------------------sessionId:"+zkConnection.getZookeeper().getSessionId());
        String ephemeralSequential = zkClient.createEphemeralSequential("/test", null);
        System.out.println("------------------------path:"+ephemeralSequential);

        ThreadUtils.sleep(5000);


        System.out.println("----------------------------sessionId:"+zkConnection.getZookeeper().getSessionId());

        ThreadUtils.sleep(100000);
    }

    @Test
    public void testZK1()throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181",5000,(e)->{
            System.out.println("接收到事件:" + e);
            if (e.getState() == Watcher.Event.KeeperState.SyncConnected) {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        System.out.println("connect success");
        System.out.println(zooKeeper.getSessionId());


        Thread.sleep(1000);

        zooKeeper.close();

        try {
            String s = zooKeeper.create("/test", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println(s);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }










}
