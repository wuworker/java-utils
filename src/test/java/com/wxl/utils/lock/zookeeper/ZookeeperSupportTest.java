package com.wxl.utils.lock.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.junit.Test;

/**
 * Created by wuxingle on 2018/1/31.
 * test zk
 */
public class ZookeeperSupportTest {

    @Test
    public void retryUntilConnected() throws Exception {
        ZookeeperSupport zs = new ZookeeperSupport("127.0.0.1:2181");
        System.out.println("connect success");

        String path = zs.retryUntilConnected(()-> zs.getZooKeeper().create(
                "/testttt",null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
        System.out.println(path);
    }

    @Test
    public void retryOnlySessionTimeout() throws Exception {

    }


}


