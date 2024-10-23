import java.util.concurrent.Semaphore;

class Philosopher extends Thread {
    private int id;
    private Semaphore leftStick;
    private Semaphore rightStick;
    private Semaphore maxTableSize;

    public Philosopher(int id, Semaphore leftChopStick, Semaphore rigthChopstick, Semaphore capacity) {
        this.id = id;
        this.leftStick = leftChopStick;
        this.rightStick = rigthChopstick;
        this.maxTableSize = capacity;
    }

    private void think() throws InterruptedException {
        System.out.println("Philosopher " + this.id + " is thinking.");
        Thread.sleep((int) (Math.random() * 1500)); // Mock thinking time for this philosopher
    }

    private void eat() throws InterruptedException {
        System.out.println("Philosopher " + this.id + " is eating.");
        Thread.sleep((int) (Math.random() * 1500)); // Mock eating time for this philosopher
    }

    public void run() {
        try {
            // Simulate eating (eating, picking or dropping chopstick) and thinking for this
            // philosopher infinitely
            while (true) {
                // Start with this philosopher thinking
                this.think();

                // Allow this philosopher to sit if table not already has four people
                this.maxTableSize.acquire();

                if (this.id % 2 == 0) {
                    // Even id philosopher should pick right and then left
                    this.rightStick.acquire();
                    System.out.println("Philosopher " + id + " picked up right chopstick.");
                    this.leftStick.acquire();
                    System.out.println("Philosopher " + id + " picked up left chopstick.");
                } else {
                    // Odd id philosopher should pick left and then right
                    this.leftStick.acquire();
                    System.out.println("Philosopher " + id + " picked up left chopstick.");
                    this.rightStick.acquire();
                    System.out.println("Philosopher " + id + " picked up right chopstick.");
                }

                // Allow the Philospher to eat
                this.eat();

                this.leftStick.release();
                System.out.println("Philosopher " + id + " put down left chopstick.");
                this.rightStick.release();
                System.out.println("Philosopher " + id + " put down right chopstick.");

                // Release spot at the table
                this.maxTableSize.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class DiningPhilosophersManager {
    public static void main(String[] args) {
        int numOfPhilosophers = 5;
        Semaphore[] chopsticks = new Semaphore[numOfPhilosophers];
        Semaphore capacity = new Semaphore(numOfPhilosophers - 1); // Allow at most 4 philosophers in the table

        for (int i = 0; i < numOfPhilosophers; i++) {
            chopsticks[i] = new Semaphore(1);
        }

        // Create and start philosopher threads
        Philosopher[] philosophers = new Philosopher[numOfPhilosophers];
        for (int i = 0; i < numOfPhilosophers; i++) {
            philosophers[i] = new Philosopher(i + 1, chopsticks[i], chopsticks[(i + 1) % numOfPhilosophers], capacity);
            philosophers[i].start();
        }

    }
}