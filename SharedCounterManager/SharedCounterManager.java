import java.util.concurrent.Semaphore;

class SharedCounter {
    private int counter = 0;
    private Semaphore semaphore;

    public SharedCounter() {
        this.semaphore = new Semaphore(1);
    }

    public void increment() {
        try {
            // Acquire semaphore to enter critical section
            this.semaphore.acquire();
            // Increment the counter
            this.counter++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Release semaphore after one of the thread increments it
            this.semaphore.release();
        }
    }

    public int getCounter() {
        return this.counter;
    }
}

class WorkThread extends Thread {
    private SharedCounter sharedCounter;

    public WorkThread(SharedCounter sharedCounter) {
        this.sharedCounter = sharedCounter;
    }

    public void run() {
        for (int i = 0; i < 100; i++) {
            this.sharedCounter.increment();
        }
    }
}

public class SharedCounterManager {
    public static void main(String[] args) {
        SharedCounter sharedCounter = new SharedCounter();
        // Create and start 5 threads
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new WorkThread(sharedCounter);
            threads[i].start();
        }
        // Wait for all threads to finish
        for (int i = 0; i < 5; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Display the shared counter
        System.out.println("Shared counter after all 5 threads execution: " + sharedCounter.getCounter());
    }
}
