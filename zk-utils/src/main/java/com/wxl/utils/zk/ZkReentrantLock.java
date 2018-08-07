package com.wxl.utils.zk;

import com.wxl.utils.base.annotation.ThreadSafe;

import com.wxl.utils.base.lock.AbstractDistributeLock;
import com.wxl.utils.base.lock.DistributeLockException;
import com.wxl.utils.base.lock.DistributeSync;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.zookeeper.KeeperException.ConnectionLossException;
import static org.apache.zookeeper.KeeperException.NoNodeException;
import static org.apache.zookeeper.Watcher.Event.EventType;

/**
 * Created by wuxingle on 2018/1/29.
 * zk分布式可重入锁
 * 可能存在的问题是一个客户端拿到锁之后,由于网络问题,
 * 服务器检测不到客户端,从而把这个节点删除,这样其他客户端会进行锁的竞争,
 * 就会出现2个客户端同时在跑任务.
 * 解决的方法是,搭建zk集群,合理设置sessionTimeout.
 * 参考:
 * zkClient实现
 * zookeeper提供的实例代码WriteLock.java
 * https://wiki.apache.org/hadoop/ZooKeeper/FAQ#A3
 * http://blog.csdn.net/zhangyuan19880606/article/details/51508250
 * http://www.cnblogs.com/wuxl360/p/5817540.html
 */
@Slf4j
@ThreadSafe
public class ZkReentrantLock extends AbstractDistributeLock {

    private static final String ROOT_PATH = "/wxl-locks";

    private static final boolean DEFAILT_FAIR_LOCK = true;

    //锁的名字
    private String lockName;

    private ZookeeperSupport zookeeperSupport;

    private DistributeSync sync;

    public ZkReentrantLock(String servers, String lockName) throws DistributeLockException {
        this(servers, lockName, DEFAILT_FAIR_LOCK);
    }

    public ZkReentrantLock(String servers, String lockName, boolean fair) throws DistributeLockException {
        this(servers, ZookeeperSupport.DEFAULT_SESSION_TIMEOUT, lockName, fair);
    }

    public ZkReentrantLock(String servers, int sessionTimeout, String lockName) throws DistributeLockException {
        this(servers, sessionTimeout, lockName, DEFAILT_FAIR_LOCK);
    }

    public ZkReentrantLock(String servers, int sessionTimeout, String lockName, boolean fair) throws DistributeLockException {
        this(new ZookeeperSupport(servers, sessionTimeout), lockName, fair);
    }

    public ZkReentrantLock(ZookeeperSupport zkSupport, String lockName) throws DistributeLockException {
        this(zkSupport, lockName, DEFAILT_FAIR_LOCK);
    }

    public ZkReentrantLock(ZookeeperSupport zkSupport, String lockName, boolean fair) throws DistributeLockException {
        Assert.isTrue(StringUtils.hasText(lockName) && !lockName.contains("-"),
                "lockName can not empty and not contains '-' !");
        this.zookeeperSupport = zkSupport;
        if (zkSupport.getZooKeeper() == null) {
            try {
                zookeeperSupport.connectBlock();
                zookeeperSupport.ensurePathExist(ROOT_PATH);
            } catch (Exception e) {
                throw new DistributeLockException(e);
            }
        }
        this.lockName = lockName;
        this.sync = fair ? new FairZkSync() : new NonfairZkSync();
    }

    @Override
    public boolean tryAcquire() throws DistributeLockException {
        return sync.tryAcquire();
    }


    @Override
    public boolean tryRelease() throws DistributeLockException {
        return sync.tryRelease();
    }

    /**
     * zNode节点(name-sessionId-sequence)
     * 包括sessionId和sequence
     */
    static class ZNode implements Comparable<ZNode> {

        String name;       //整个节点名
        String sessionId;  //会话id,由zk保持唯一
        int sequence;

        ZNode(String name) {
            this.name = name;
            String[] s = name.substring(name.lastIndexOf("/") + 1).split("-");
            sequence = Integer.parseInt(s[s.length - 1]);
            sessionId = s[s.length - 2];
        }

        public int compareTo(ZNode o) {
            return sequence - o.sequence;
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ZNode) {
                ZNode other = (ZNode) o;
                return name.equals(other.name);
            }
            return false;
        }

        public String toString() {
            return name;
        }
    }

    abstract class ZkSync implements DistributeSync {
        //锁重入次数
        private int lockCount;
        //当前节点
        private ZNode myNode;
        //等待释放的节点
        private ZNode waitNode;
        //获取锁的节点
        private ZNode ownerNode;
        //节点删除监听
        private Watcher nodeDeletedWatcher = (event) -> {
            if (event.getType() != EventType.NodeDeleted) {
                return;
            }
            try {
                log.debug("node to release:{}", event.getPath());
                localLock.lock();
                localCondition.signalAll();
            } finally {
                localLock.unlock();
            }
        };

        /**
         * 创建节点
         */
        protected abstract ZNode createLockNode() throws KeeperException, InterruptedException, Exception;

        /**
         * 尝试获取锁,本身非线程安全
         * 需要调用者实现线程安全
         */
        public boolean tryAcquire() throws DistributeLockException {
            try {
                if (isOwner()) {
                    lockCount++;
                    return true;
                }
                Label:
                do {
                    if (myNode == null) {
                        myNode = createLockNode();
                        log.debug("create zk node:{}", myNode);
                    }
                    boolean waitNodeExist = false;
                    while (!waitNodeExist) {
                        //获取孩子节点
                        SortedSet<ZNode> children = sortChildrenNode();
                        if (children.isEmpty()) {
                            myNode = null;
                            continue Label;
                        }
                        //序号最小的获取锁
                        ownerNode = children.first();
                        log.debug("current localLock node is {}", ownerNode);
                        SortedSet<ZNode> lessThanMe = children.headSet(myNode);
                        if (lessThanMe.isEmpty()) {
                            if (isOwner()) {
                                lockCount++;
                                log.info("zk localLock success :{}", myNode);
                                return true;
                            }
                        } else {
                            waitNode = lessThanMe.last();
                            //注册监听
                            Stat stat = zookeeperSupport.retryUntilConnected(() -> zookeeperSupport.getZooKeeper().exists(waitNode.name, nodeDeletedWatcher));
                            waitNodeExist = stat != null;
                        }
                    }
                } while (myNode == null);
            } catch (KeeperException | InterruptedException e) {
                throw new DistributeLockException(e);
            } catch (Exception e) {
                throw new DistributeLockException(e);
            }
            return false;
        }

        /**
         * 尝试释放锁,本身非线程安全
         * 需要调用者实现线程安全
         */
        public boolean tryRelease() throws DistributeLockException {
            if (!isOwner()) {
                throw new IllegalMonitorStateException();
            }
            if (lockCount == 1) {
                try {
                    zookeeperSupport.retryUntilConnected(() -> {
                        zookeeperSupport.getZooKeeper().delete(myNode.name, -1);
                        return null;
                    });
                    log.info("release localLock success, node is {}, reentrant count is 0", myNode);
                    myNode = null;
                    lockCount--;
                }
                //可能连接丢失的时候进行了重试,但其实执行成功了
                catch (NoNodeException e) {
                    myNode = null;
                    lockCount--;
                } catch (KeeperException | InterruptedException e) {
                    throw new DistributeLockException(e);
                } catch (Exception e) {
                    throw new DistributeLockException(e);
                }
            } else {
                lockCount--;
                log.info("release success, node is {}, reentrant count is {}", myNode, lockCount);
            }
            return true;
        }

        private boolean isOwner() {
            return myNode != null && ownerNode != null && myNode.equals(ownerNode);
        }

        /**
         * 获取排好序的子节点
         */
        private SortedSet<ZNode> sortChildrenNode() throws KeeperException, InterruptedException, Exception {
            List<String> children = zookeeperSupport.retryUntilConnected(() -> zookeeperSupport.getZooKeeper().getChildren(ROOT_PATH, false));
            SortedSet<ZNode> sortedSet = new TreeSet<>();
            //获取同类的孩子节点,不至于拿到其他孩子
            String similar = lockName + "-";
            for (String child : children) {
                if (child.startsWith(similar)) {
                    sortedSet.add(new ZNode(ROOT_PATH + "/" + child));
                }
            }
            return sortedSet;
        }
    }

    /**
     * 公平锁
     * 利用zk的临时顺序节点,
     * 进行节点的排序,序号小的优先获取锁
     */
    final class FairZkSync extends ZkSync {
        /**
         * 创建锁节点
         * name-sessionId-sequence
         */
        protected ZNode createLockNode() throws KeeperException, InterruptedException, Exception {
            while (true) {
                String sessionId = String.valueOf(zookeeperSupport.getZooKeeper().getSessionId());
                final String pathString = ROOT_PATH + "/" + lockName + "-" + sessionId + "-";
                try {
                    String path = zookeeperSupport.retryOnlySessionTimeout(() -> zookeeperSupport.getZooKeeper().create(
                            pathString, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
                    return new ZNode(path);
                }
                /*
                 * 连接丢失,此时节点可能已经创建,
                 * 需要重新获取子节点,判断是否已经创建
                 */ catch (ConnectionLossException e) {
                    List<String> children = zookeeperSupport.retryUntilConnected(() -> zookeeperSupport.getZooKeeper().getChildren(ROOT_PATH, false));
                    for (String child : children) {
                        if (child.contains(sessionId)) {
                            return new ZNode(ROOT_PATH + "/" + child);
                        }
                    }
                }
            }
        }
    }

    /**
     * 非公平锁
     * 利用zk的临时节点加上随机数进行排序
     * 序号小的优先获取锁
     */
    final class NonfairZkSync extends ZkSync {

        private Random random = new Random();

        /**
         * 创建锁节点
         * name-sessionId-sequence
         * 节点创建时的sessionId不一定等于真实的sessionId,
         * 有可能创建过程中进行了重连,但是只要保持唯一性即可
         */
        @Override
        protected ZNode createLockNode() throws KeeperException, InterruptedException, Exception {
            while (true) {
                String sessionId = String.valueOf(zookeeperSupport.getZooKeeper().getSessionId());
                int randomId = random.nextInt(Integer.MAX_VALUE);
                final String pathString = ROOT_PATH + "/" + lockName + "-" + sessionId + "-" + randomId;
                try {
                    String path = zookeeperSupport.retryOnlySessionTimeout(() -> zookeeperSupport.getZooKeeper().create(
                            pathString, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
                    return new ZNode(path);
                }
                /*
                 * 连接丢失,此时节点可能已经创建,
                 * 需要重新获取子节点,判断是否已经创建
                 */ catch (ConnectionLossException e) {
                    List<String> children = zookeeperSupport.retryUntilConnected(() -> zookeeperSupport.getZooKeeper().getChildren(ROOT_PATH, false));
                    for (String child : children) {
                        if (child.contains(sessionId)) {
                            return new ZNode(ROOT_PATH + "/" + child);
                        }
                    }
                }
            }
        }
    }

}

