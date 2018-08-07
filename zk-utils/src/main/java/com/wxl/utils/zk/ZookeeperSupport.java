package com.wxl.utils.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.zookeeper.KeeperException.ConnectionLossException;
import static org.apache.zookeeper.KeeperException.SessionExpiredException;
import static org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * Created by wuxingle on 2018/1/17.
 * 实现zk的超时重连以及重试
 */
@Slf4j
public class ZookeeperSupport implements Watcher {

    protected static final int DEFAULT_SESSION_TIMEOUT = 30000;

    private String servers;

    private int sessionTimeout;

    private Lock zkLock = new ReentrantLock();

    private Condition stateChangedCondition = zkLock.newCondition();

    private KeeperState currentState;

    private ZooKeeper zooKeeper;

    public ZookeeperSupport(String servers) {
        this(servers, DEFAULT_SESSION_TIMEOUT);
    }

    public ZookeeperSupport(String servers, int sessionTimeout) {
        this.servers = servers;
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * 阻塞式连接
     */
    public void connectBlock() throws IOException {
        boolean start = false;
        try {
            zkLock.lock();
            if (zooKeeper != null) {
                throw new IllegalStateException("zk has already started!");
            }
            zooKeeper = new ZooKeeper(servers, sessionTimeout, this);
            waitUntilConnected();
            start = true;
        } finally {
            zkLock.unlock();
            if (!start) {
                close();
            }
        }
    }

    /**
     * 断开
     */
    public void close() {
        try {
            zkLock.lock();
            if (zooKeeper != null) {
                zooKeeper.close();
                zooKeeper = null;
            }
        } catch (InterruptedException e) {
            log.error("zookeeper close error, because was interrupted");
            throw new IllegalStateException(e);
        } finally {
            zkLock.unlock();
        }
    }

    public ZooKeeper getZooKeeper() throws IllegalStateException {
        try {
            zkLock.lock();
            if (zooKeeper == null) {
                throw new IllegalStateException("zookeeper is close");
            }
            return zooKeeper;
        } finally {
            zkLock.unlock();
        }
    }

    /**
     * 这个watcher只监听状态的变化
     * 以进行重连
     */
    @Override
    public void process(WatchedEvent event) {
        if (event.getPath() != null) {
            return;
        }
        try {
            zkLock.lock();
            currentState = event.getState();
            //如果是session过期,进行重连
            if (currentState == KeeperState.Expired) {
                log.warn("zookeeper session is expired, try reconnect");
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
                zooKeeper = new ZooKeeper(servers, sessionTimeout, this);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            stateChangedCondition.signalAll();
            zkLock.unlock();
        }
    }

    /**
     * @param callable zk操作
     *                 对连接丢失,session过期进行无限重试
     */
    public <T> T retryUntilConnected(Callable<T> callable) throws KeeperException, InterruptedException, Exception {
        while (true) {
            try {
                return callable.call();
            } catch (ConnectionLossException | SessionExpiredException e) {
                Thread.yield();
                waitUntilConnected();
            }
        }
    }

    /**
     * @param callable zk操作
     *                 只对session过期进行无限重试
     */
    public <T> T retryOnlySessionTimeout(Callable<T> callable) throws ConnectionLossException, KeeperException, InterruptedException, Exception {
        while (true) {
            try {
                return callable.call();
            } catch (SessionExpiredException e) {
                Thread.yield();
                waitUntilConnected();
            } catch (ConnectionLossException e) {
                Thread.yield();
                waitUntilConnected();
                throw e;
            }
        }
    }

    /**
     * 确保路径存在
     */
    public void ensurePathExist(String path) throws KeeperException, InterruptedException, Exception {
        boolean exist = retryUntilConnected(() -> getZooKeeper().exists(path, false) != null);
        if (!exist) {
            retryUntilConnected(() -> {
                try {
                    return getZooKeeper().create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                //node已存在,忽略这个异常
                catch (KeeperException.NodeExistsException e) {
                }
                return null;
            });
        }
    }


    /**
     * 等待直到连接成功
     */
    private void waitUntilConnected() {
        try {
            zkLock.lock();
            while (currentState != KeeperState.SyncConnected) {
                stateChangedCondition.awaitUninterruptibly();
            }
        } finally {
            zkLock.unlock();
        }
    }

}


