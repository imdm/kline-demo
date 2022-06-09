package com.milo.kline.demo.dubbo.provider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;

public class MultiThreadTests {
    private static volatile int signal = 1;
    private final static Object lock = new Object();
    @Test
    public void testMultiThread() throws InterruptedException {
        ExecutorService pool = new ThreadPoolExecutor(
                4,
                10,
                3L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        pool.execute(this::func4);
        pool.execute(this::func2);
        pool.execute(this::func3);
        pool.execute(this::func1);
        Thread.sleep(6000);
    }

    public void func1() {
        synchronized (lock) {
            while (signal != 1) {
                System.out.println("func1 wait");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("func1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            signal = 2;
            lock.notifyAll();
        }
    }

    public void func2() {
        synchronized (lock) {
            while (signal != 2) {
                System.out.println("func2 wait");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("func2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            signal = 3;
            lock.notifyAll();
        }
    }
    public void func3() {
        synchronized (lock) {
            while (signal != 3) {
                System.out.println("func3 wait");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("func3");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            signal = 4;
            lock.notifyAll();
        }
    }
    public void func4() {
        synchronized (lock) {
            while (signal != 4) {
                System.out.println("func4 wait");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("func4");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            signal = 1;
            lock.notifyAll();
        }
    }
}
