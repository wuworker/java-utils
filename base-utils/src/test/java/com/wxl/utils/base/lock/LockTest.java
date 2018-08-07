package com.wxl.utils.base.lock;

import com.wxl.utils.base.ThreadUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wuxingle on 2018/1/25.
 * zkLock test
 */
public class LockTest {


    private static class SimpleLock implements Lock {

        private Sync sync = new Sync();

        static class Sync extends AbstractQueuedSynchronizer {

            protected boolean tryAcquire(int arg) {
                if (compareAndSetState(0, 1)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }

            protected boolean tryRelease(int arg) {
                if (getState() == 0) {
                    return false;
                }
                setState(0);
                setExclusiveOwnerThread(null);
                return true;
            }

            protected boolean isHeldExclusively() {
                return getState() == 1;
            }

            final ConditionObject newCondition() {
                return new ConditionObject();
            }
        }

        public void lock() {
            sync.acquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(time));
        }

        public void unlock() {
            sync.release(1);
        }

        public Condition newCondition() {
            return sync.newCondition();
        }
    }

    @Test
    public void test() {
        SimpleLock lock = new SimpleLock();
        try {
            lock.lock();
            System.out.println("zkLock--------------------1");
            lock.lock();
        } finally {
            lock.unlock();
        }
    }


    @Test(expected = IllegalMonitorStateException.class)
    public void test2() {
        Lock lock = new ReentrantLock();
        lock.unlock();
    }


    @Test
    public void testCondition() throws Exception {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        Runnable r1 = () -> {
            try {
                lock.lock();
                System.out.println("localLock suc, await start");
                condition.awaitUninterruptibly();
                System.out.println("localLock suc, await end");
            } finally {
                lock.unlock();
            }
        };
        Runnable t2 = () -> {
            try {
                System.out.println("localLock start");
                lock.lock();
                System.out.println("localLock suc");
            } finally {
                lock.unlock();
            }
        };

        new Thread(r1).start();
        Thread.sleep(1000);

        System.out.println("now start 2");
        new Thread(t2).start();
        Thread.sleep(10000);

        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }

        Thread.sleep(2000);
        System.out.println("end");
    }


    @Test
    public void testLockInterrupted() {
        Lock lock = new ReentrantLock();

        Thread t1 = new Thread() {
            public void run() {
                boolean suc = false;
                try {
                    System.out.println("t1 start");
                    lock.lockInterruptibly();
                    System.out.println("lockInterruptibly success");
                    suc = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (suc)
                        lock.unlock();
                }
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {
                    lock.lock();
                    System.out.println("localLock success ");
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        };

        t2.start();
        ThreadUtils.sleep(1000);

        t1.start();
        ThreadUtils.sleep(1000);
        t1.interrupt();

        ThreadUtils.sleep(5000);
    }


    @Test
    public void testTryLock() throws Exception {
        Lock lock = new ReentrantLock();
        Runnable r = () -> {
            boolean suc = false;
            try {
                if (suc = lock.tryLock()) {
                    System.out.println(Thread.currentThread().getName() + "try localLock true");
                    Thread.sleep(4000);
                } else {
                    System.out.println(Thread.currentThread().getName() + "try localLock false");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (suc) {
                    lock.unlock();
                }
            }
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);

        t1.start();
        t2.start();

        Thread.sleep(5000);
    }

    @Test
    public void testLockInterrupted2() {
        Lock lock = new ReentrantLock();
        try {
            lock.lockInterruptibly();

            Thread.currentThread().interrupt();

            lock.unlock();

            System.out.println("success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConditionUnit() throws Exception {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        Thread t1 = new Thread() {
            public void run() {
                try {
                    lock.lock();
                    System.out.println("localLock success");
                    boolean await = condition.await(-5, TimeUnit.SECONDS);
                    System.out.println("localLock 1 await:" + await);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("unlock success");
                    lock.unlock();
                }
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                try {
                    lock.lock();
                    condition.signalAll();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        };

        t1.start();
        Thread.sleep(2000);
        t2.start();

        Thread.sleep(100000);
    }


}
