package com.wxl.utils.lock.zookeeper;

import com.wxl.utils.annotation.ThreadSafe;
import com.wxl.utils.lock.DistributeLock;
import com.wxl.utils.lock.DistributeLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.zookeeper.KeeperException.ConnectionLossException;
import static org.apache.zookeeper.KeeperException.NoNodeException;
import static org.apache.zookeeper.Watcher.Event.EventType;

/**
 * Created by wuxingle on 2018/1/29.
 * zk锁
 *
 */
@Slf4j
@ThreadSafe
public class ZkReentrantLock extends ZookeeperSupport implements DistributeLock {

    private static final String ROOT_PATH = "/locks";

    private final Sync sync;

    //锁的名字
    private String lockName;

    private Lock zkLock = new ReentrantLock();

    private Condition dataChangedCondition = zkLock.newCondition();

    public ZkReentrantLock(String servers, String lockName) throws IOException, DistributeLockException {
        this(true, servers, lockName);
    }

    public ZkReentrantLock(boolean fair, String servers, String lockName) throws IOException, DistributeLockException {
        this(fair, servers, DEFAULT_SESSION_TIMEOUT, lockName);
    }

    public ZkReentrantLock(String servers, int sessionTimeout, String lockName) throws IOException, DistributeLockException {
        this(true, servers, sessionTimeout, lockName);
    }

    public ZkReentrantLock(boolean fair, String servers, int sessionTimeout, String lockName) throws IOException, DistributeLockException {
        super(servers, sessionTimeout);
        this.lockName = lockName;
        try {
            ensurePathExist(ROOT_PATH);
        } catch (Exception e) {
            throw new DistributeLockException(e);
        }
        sync = fair ? new FairSync() : new NonfairSync();
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

    /**
     * 同步接口
     */
    abstract class Sync {
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
                zkLock.lock();
                dataChangedCondition.signalAll();
            } finally {
                zkLock.unlock();
            }
        };

        /**
         * 创建节点
         */
        protected abstract ZNode createLockNode() throws KeeperException, InterruptedException, Exception;

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
                        SortedSet<ZNode> children = sortChildrenNode();
                        if (children.isEmpty()) {
                            myNode = null;
                            continue Label;
                        }
                        ownerNode = children.first();
                        log.debug("current lock node is {}", ownerNode);
                        SortedSet<ZNode> lessThanMe = children.headSet(myNode);
                        if (lessThanMe.isEmpty()) {
                            if (isOwner()) {
                                lockCount++;
                                log.info("zk lock success :{}",myNode);
                                return true;
                            }
                        } else {
                            waitNode = lessThanMe.last();
                            //注册监听
                            Stat stat = retryUntilConnected(() -> getZooKeeper().exists(waitNode.name, nodeDeletedWatcher));
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

        public boolean tryRelease() throws DistributeLockException {
            if (!isOwner()) {
                throw new IllegalMonitorStateException();
            }
            if (--lockCount == 0) {
                try {
                    retryUntilConnected(() -> {
                        getZooKeeper().delete(myNode.name, -1);
                        return null;
                    });
                    log.info("release lock success, node is {}, reentrant count is 0", myNode);
                    myNode = null;
                }
                //可能连接丢失的时候进行了重试,但其实执行成功了
                catch (NoNodeException e) {
                    myNode = null;
                } catch (KeeperException | InterruptedException e) {
                    throw new DistributeLockException(e);
                } catch (Exception e) {
                    throw new DistributeLockException(e);
                }
            } else {
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
            List<String> children = retryUntilConnected(() -> getZooKeeper().getChildren(ROOT_PATH, false));
            SortedSet<ZNode> sortedSet = new TreeSet<>();
            for (String child : children) {
                sortedSet.add(new ZNode(ROOT_PATH + "/" + child));
            }
            return sortedSet;
        }
    }

    /**
     * 公平锁
     * 利用zk的临时顺序节点,
     * 进行节点的排序,序号小的优先获取锁
     */
    final class FairSync extends Sync {
        /**
         * 创建锁节点
         * name-sessionId-sequence
         * 节点创建时的sessionId不一定等于真实的sessionId,
         * 有可能创建过程中进行了重连,但是只要保持唯一性即可
         */
        protected ZNode createLockNode() throws KeeperException, InterruptedException, Exception {
            while (true) {
                String sessionId = String.valueOf(getZooKeeper().getSessionId());
                final String pathString = ROOT_PATH + "/" + lockName + "-" + sessionId + "-";
                try {
                    String path = retryOnlySessionTimeout(() -> getZooKeeper().create(
                            pathString, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
                    return new ZNode(path);
                }
            /*
             * 连接丢失,此时节点可能已经创建,
             * 需要重新获取子节点,判断是否已经创建
             */ catch (ConnectionLossException e) {
                    List<String> children = retryUntilConnected(() -> getZooKeeper().getChildren(ROOT_PATH, false));
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
    final class NonfairSync extends Sync {

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
                String sessionId = String.valueOf(getZooKeeper().getSessionId());
                int randomId = random.nextInt(Integer.MAX_VALUE);
                final String pathString = ROOT_PATH + "/" + lockName + "-" + sessionId + "-" + randomId;
                try {
                    String path = retryOnlySessionTimeout(() -> getZooKeeper().create(
                            pathString, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
                    return new ZNode(path);
                }
            /*
             * 连接丢失,此时节点可能已经创建,
             * 需要重新获取子节点,判断是否已经创建
             */ catch (ConnectionLossException e) {
                    List<String> children = retryUntilConnected(() -> getZooKeeper().getChildren(ROOT_PATH, false));
                    for (String child : children) {
                        if (child.contains(sessionId)) {
                            return new ZNode(ROOT_PATH + "/" + child);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void lock() throws DistributeLockException {
        boolean suc = false;
        try {
            zkLock.lock();
            while (!sync.tryAcquire()) {
                dataChangedCondition.awaitUninterruptibly();
            }
            suc = true;
        } finally {
            if (!suc) {
                zkLock.unlock();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException, DistributeLockException {
        boolean suc = false;
        try {
            zkLock.lock();
            while (!sync.tryAcquire()) {
                dataChangedCondition.await();
            }
            suc = true;
        } finally {
            if (!suc) {
                zkLock.unlock();
            }
        }
    }

    @Override
    public boolean tryLock() throws DistributeLockException {
        boolean tryLock = false, tryAcquire = false;
        try {
            if (tryLock = zkLock.tryLock()) {
                return tryAcquire = sync.tryAcquire();
            }
        } finally {
            //本地锁成功,远程锁失败,算失败
            if (tryLock && !tryAcquire) {
                zkLock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException, DistributeLockException {
        boolean tryLock = false, tryAcquire = false;
        long start = System.nanoTime();
        try {
            if (tryLock = zkLock.tryLock(time, unit)) {
                long use = System.nanoTime() - start;
                long need = unit.toNanos(time) - use;
                while (!sync.tryAcquire()) {
                    long s = System.nanoTime();
                    if (!dataChangedCondition.await(need, TimeUnit.NANOSECONDS)) {
                        return false;
                    }
                    need = need - (System.nanoTime() - s);
                    if (need <= 0) {
                        return false;
                    }
                }
                return tryAcquire = true;
            }
        } finally {
            if (tryLock && !tryAcquire) {
                zkLock.unlock();
            }
        }
        return false;
    }

    @Override
    public void unlock() throws DistributeLockException {
        try {
            sync.tryRelease();
        } finally {
            zkLock.unlock();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}


