package pl.testy.kurswspolbieznosci.week_2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class _08_Homework {
    public static void main(String[] args) throws InterruptedException {
        RandomNumberStore numberStore = new RandomNumberStore();

        RandomWriter randomWriterTask_1 = new RandomWriter(numberStore, 1, 50);
        RandomWriter randomWriterTask_2 = new RandomWriter(numberStore, 100, 500);
        SumOfNumber sumOfNumber = new SumOfNumber(numberStore);
        ThreadsMonitor threadsMonitor = new ThreadsMonitor();

        Thread thread1 = new Thread(randomWriterTask_1, "randomWriterTask_1");
        Thread thread2 = new Thread(randomWriterTask_2, "randomWriterTask_2");
        Thread thread3 = new Thread(sumOfNumber, "sumOfNumber");
        Thread monitor = new Thread(threadsMonitor, "threadsMonitor");
        monitor.setDaemon(true);
        monitor.start();

        thread1.start();
        thread2.start();
        thread3.start();
        threadsMonitor.refreshListOfThreads();

        Thread.sleep(10_000);
        randomWriterTask_1.stopTask();
        randomWriterTask_2.stopTask();
        sumOfNumber.stopTask();

        thread1.join();
        thread2.join();
        thread3.join();
    }

    static class ThreadsMonitor implements Runnable {
        private List<Thread> threadList;

        public ThreadsMonitor() {
            refreshListOfThreads();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("###");
                    for (Thread thread : threadList) {
                        System.out.println(getLogAboutThread(thread));
                    }
                    System.out.println("###");
                    Thread.sleep(3_000);
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        void refreshListOfThreads() {
            threadList = new ArrayList<>(Thread.getAllStackTraces().keySet());
            threadList.sort(Comparator.comparing(Thread::getName));
        }

        private String getLogAboutThread(Thread thread) {
            return String.format("Thread[name=%s, state=%s, isAlive=%s]",
                    thread.getName(), thread.getState(), Boolean.toString(thread.isAlive()));
        }
    }

    static class RandomNumberStore {
        private Integer number;

        synchronized void setNumber(Integer number) throws InterruptedException {
            if (this.number != null) {
                wait();
            }
            this.number = number;
            notify();
        }

        synchronized Integer getNumber() throws InterruptedException {
            if (this.number == null) {
                wait();
            }
            Integer tmpNumber = number;
            number = null;
            notify();
            return tmpNumber;
        }
    }

    static class SumOfNumber implements Runnable {
        private RandomNumberStore randomNumberStore;
        private volatile boolean isRunning = false;

        SumOfNumber(RandomNumberStore randomNumberStore) {
            this.randomNumberStore = randomNumberStore;
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                while (isRunning) {
                    int number = randomNumberStore.getNumber();
                    System.out.println("Sum of " + number + " is: " + calculateSumOfNumber(number));
                    Thread.sleep(2_500);
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        void stopTask() {
            isRunning = false;
        }

        private int calculateSumOfNumber(int number) throws InterruptedException {
            return (int) (((1 + number) / 2.0) * number);
        }
    }

    static class RandomWriter implements Runnable {
        private RandomNumberStore randomNumberStore;
        private Random random;
        private int minNumberIncluded;
        private int maxNumberIncluded;
        private volatile boolean isRunning = false;

        RandomWriter(RandomNumberStore randomNumberStore, int minNumberIncluded, int maxNumberIncluded) {
            this.randomNumberStore = randomNumberStore;
            random = new Random();
            this.minNumberIncluded = minNumberIncluded;
            this.maxNumberIncluded = maxNumberIncluded;
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                while (isRunning) {
                    randomNumberStore.setNumber(getRandomInt());
                    Thread.sleep(2_500);
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        void stopTask() {
            isRunning = false;
        }

        private int getRandomInt() {
            return random.nextInt((maxNumberIncluded - minNumberIncluded) + 1) + minNumberIncluded;
        }
    }
}
