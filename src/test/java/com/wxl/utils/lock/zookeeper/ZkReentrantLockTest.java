package com.wxl.utils.lock.zookeeper;

import com.wxl.utils.ThreadUtils;
import com.wxl.utils.lock.DistributeLockException;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuxingle on 2018/1/30.
 * zk reentrant localLock 测试
 */
public class ZkReentrantLockTest {


    @Test
    public void testFair() throws Exception {
        ZkReentrantLock[] locks = new ZkReentrantLock[10];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ZkReentrantLock("127.0.0.1:2181", "wxl",true);
        }
        testLockSequence(locks);
    }

    @Test
    public void testNonFair() throws Exception {
        ZkReentrantLock[] locks = new ZkReentrantLock[10];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ZkReentrantLock("127.0.0.1:2181", "wxl",false);
        }
        testLockSequence(locks);
    }

    private void testLockSequence(ZkReentrantLock[] locks) throws Exception {
        class Task implements Runnable {
            private CountDownLatch latch;
            private ZkReentrantLock lock;
            Task(CountDownLatch latch, ZkReentrantLock lock) {
                this.lock = lock;
                this.latch = latch;
            }
            public void run() {
                try {
                    lock.lock();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    latch.countDown();
                }
            }
        }

        locks[0].lock();
        CountDownLatch latch = new CountDownLatch(locks.length - 1);
        for (int i = 1; i < locks.length; i++) {
            Thread thread = new Thread(new Task(latch, locks[i]));
            thread.setName("zk-localLock-t" + i);
            thread.start();
        }
        locks[0].unlock();
        latch.await();
    }


    @Test
    public void testLock() throws Exception {

        class Num {
            int num;
        }

        class LockTask implements Runnable {
            private ZkReentrantLock lock;
            private CountDownLatch startLatch;
            private CountDownLatch endLatch;
            private Num safeNum;
            private Num unsafeNum;
            private Random random = new Random();

            public LockTask(ZkReentrantLock lock, CountDownLatch startLatch, CountDownLatch endLatch, Num safeNum, Num unsafeNum) {
                this.lock = lock;
                this.startLatch = startLatch;
                this.endLatch = endLatch;
                this.safeNum = safeNum;
                this.unsafeNum = unsafeNum;
            }

            public void run() {
                try {
                    startLatch.await();

                    addNum(unsafeNum);

                    lock.lock();
                    addNum(safeNum);
                    lock.unlock();

                    endLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void addNum(Num num) {
                int n = num.num;
                n++;
                ThreadUtils.sleep(random.nextInt(1));
                num.num = n;
            }
        }

        Num safe = new Num();
        Num unsafe = new Num();
        int taskNum = 100;
        int zkLockNum = 10;
        ZkReentrantLock[] zkReentrantLocks = new ZkReentrantLock[zkLockNum];
        LockTask[] lockTasks = new LockTask[taskNum];
        CountDownLatch startLatch = new CountDownLatch(taskNum);
        CountDownLatch endLatch = new CountDownLatch(taskNum);

        for (int i = 0; i < lockTasks.length; i++) {
            ZkReentrantLock lock;
            if ((lock = zkReentrantLocks[i % zkLockNum]) == null) {
                lock = zkReentrantLocks[i] = new ZkReentrantLock("127.0.0.1:2181", "wxl");
            }
            lockTasks[i] = new LockTask(lock, startLatch, endLatch, safe, unsafe);
        }

        ExecutorService service = Executors.newFixedThreadPool(taskNum);
        for (LockTask t1 : lockTasks) {
            service.execute(t1);
            startLatch.countDown();
        }

        endLatch.await();
        service.shutdown();

        System.out.println(safe.num);
        System.out.println(unsafe.num);
    }


    @Test
    public void testLockInterrupted() throws Exception {
        ZkReentrantLock lock = new ZkReentrantLock("127.0.0.1:2181", "wxl");
        Thread t1 = new Thread() {
            @Override
            public void run() {
                boolean suc = false;
                try {
                    System.out.println("t1 start");
                    lock.lockInterruptibly();
                    suc = true;
                } catch (InterruptedException | DistributeLockException e) {
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
                    System.out.println("localLock success");
                    sleep(5000);
                } catch (DistributeLockException | InterruptedException e) {
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
        ZkReentrantLock lock1 = new ZkReentrantLock("127.0.0.1:2181", "wxl");
        ZkReentrantLock lock2 = new ZkReentrantLock("127.0.0.1:2181", "wxl");

        class Task implements Runnable {
            ZkReentrantLock lock;

            Task(ZkReentrantLock lock) {
                this.lock = lock;
            }

            public void run() {
                boolean suc = false;
                try {
                    if (suc = lock.tryLock()) {
                        System.out.println(Thread.currentThread().getName() + " try localLock success");
                        Thread.sleep(4000);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " try localLock fail");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (suc) {
                        lock.unlock();
                    }
                }
            }
        }

        Thread t1 = new Thread(new Task(lock1));
        Thread t2 = new Thread(new Task(lock2));
        Thread t3 = new Thread(new Task(lock1));
        Thread t4 = new Thread(new Task(lock2));
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        Thread.sleep(5000);
    }

    @Test
    public void testTryLock2()throws Exception{
        ZkReentrantLock lock1 = new ZkReentrantLock("127.0.0.1:2181", "wxl");
        ZkReentrantLock lock2 = new ZkReentrantLock("127.0.0.1:2181", "wxl");

        Thread t1 = new Thread(){
          public void run(){
              try {
                  lock1.lock();
                  System.out.println("localLock 1 localLock success");
                  Thread.sleep(3000);
              }catch (Exception e){
                  e.printStackTrace();
              }finally {
                  System.out.println("localLock 1 unlock success");
                  lock1.unlock();
              }
          }
        };

        Thread t2 = new Thread(){
          public void run(){
              try {
                  boolean suc = lock2.tryLock(3, TimeUnit.SECONDS);
                  System.out.println("lock2 try localLock :" + suc);
              }catch (Exception e){
                  e.printStackTrace();
              }finally {
                  lock2.unlock();
              }
          }
        };

        t1.start();
        Thread.sleep(1000);
        t2.start();

        Thread.sleep(5000);
    }




}










