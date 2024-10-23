import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TeachingAssistant extends Thread {
    private Semaphore students; // Semaphore for students signaling TA for help
    private Lock chairLock; // Lock for managing chairs in the hallway
    private boolean isHelping; // Is the TA currently helping a student?
    private int waitingStudents; // Number of students currently waiting in chairs

    public TeachingAssistant(Semaphore students, Lock chairLock) {
        this.students = students;
        this.chairLock = chairLock;
        this.isHelping = false;
        this.waitingStudents = 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Wait for a student to arrive (semaphore acquire)
                students.acquire();
                // TA wakes up and checks for students
                helpStudent();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void helpStudent() throws InterruptedException {
        // TA is helping a student
        isHelping = true;
        System.out.println("TA is helping a student.");

        // Simulate helping the student by sleeping for a random period
        Thread.sleep((int) (Math.random() * 1000));

        // After helping, check for other students
        chairLock.lock();
        try {
            waitingStudents--;
            System.out.println("TA finished helping a student. " + waitingStudents + " students waiting.");
        } finally {
            chairLock.unlock();
        }

        isHelping = false;
    }

    public boolean isHelping() {
        return isHelping;
    }

    public void increaseWaitingStudents() {
        waitingStudents++;
    }

    public boolean isFull() {
        return waitingStudents >= 3;
    }
}

class Student extends Thread {
    private int id;
    private Semaphore students;
    private Lock chairLock;
    private TeachingAssistant ta;

    public Student(int id, Semaphore students, Lock chairLock, TeachingAssistant ta) {
        this.id = id;
        this.students = students;
        this.chairLock = chairLock;
        this.ta = ta;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simulate the student programming
                program();

                // Check if the TA is available
                askForHelp();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void program() throws InterruptedException {
        System.out.println("Student " + id + " is programming.");
        // Simulate programming for a random period
        Thread.sleep((int) (Math.random() * 5000));
    }

    private void askForHelp() throws InterruptedException {
        chairLock.lock();
        try {
            // Check if the hallway is full
            if (ta.isFull()) {
                System.out.println("Student " + id + " found no chairs available. Going back to programming.");
                return;
            }

            // Student takes a chair and waits for help
            ta.increaseWaitingStudents();
            System.out.println("Student " + id + " is waiting in the hallway.");

        } finally {
            chairLock.unlock();
        }

        // Notify the TA that a student needs help
        students.release();
    }
}

public class SleepingTAManager {
    public static void main(String[] args) {
        int numStudents = 5; // Total number of students

        // Semaphore to signal the TA for help (initial permits 0)
        Semaphore studentsSemaphore = new Semaphore(0);

        // Lock to manage the chairs in the hallway
        Lock chairLock = new ReentrantLock();

        // Create a TA object
        TeachingAssistant ta = new TeachingAssistant(studentsSemaphore, chairLock);

        // Start the TA thread
        ta.start();

        // Create and start student threads
        for (int i = 1; i <= numStudents; i++) {
            new Student(i, studentsSemaphore, chairLock, ta).start();
        }
    }
}
