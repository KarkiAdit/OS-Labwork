import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TeachingAssistant extends Thread {
    private Semaphore students; // Student signals to get the TA help
    private Lock chairLock; // Lock for managing the chairs in the hallway
    private boolean isHelping; // Tracks if the TA is helping a student
    private int waitingStudents; // Number of students currently waiting in chairs
    private int noOfChairs; // Total number of chairs in the hallway
    private Queue<Student> studentQueue; // Queue of waiting students

    public TeachingAssistant(Semaphore students, Lock chairLock, int noOfChairs) {
        this.students = students;
        this.chairLock = chairLock;
        this.noOfChairs = noOfChairs;
        this.isHelping = false;
        this.waitingStudents = 0;
        this.studentQueue = new LinkedList<>(); // Initialize the student queue
    }

    @Override
    public void run() {
        // After a student arrives, TA wakes up and helps all waiting students
        // Goes back to sleep if no one is in the hallway
        while (true) {
            try {
                // Wait for a student to arrive
                this.students.acquire();
                // Help the first waiting student
                this.helpStudent();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void helpStudent() throws InterruptedException {
        // Lock the chair to modify the waitingStudents count
        this.chairLock.lock();
        Student studentToHelp = null;
        try {
            // Get the first waiting student from the queue
            studentToHelp = studentQueue.poll(); // Dequeue the student
            if (studentToHelp != null) {
                this.waitingStudents--;
                this.isHelping = true;
                System.out.println("TA started helping Student " + studentToHelp.getStudentId() + ". "
                        + this.waitingStudents + " students still waiting.");
            }
        } finally {
            this.chairLock.unlock();
        }

        if (studentToHelp != null) {
            // Simulate helping the student
            Thread.sleep((int) (Math.random() * 10000));
            System.out.println("TA finished helping Student " + studentToHelp.getStudentId());

            // Notify the next waiting student
            studentToHelp.signalHelped();
        }

        // TA is ready to help another student
        this.isHelping = false;
    }

    public boolean isHelping() {
        return this.isHelping;
    }

    public void increaseWaitingStudents(Student student) {
        this.chairLock.lock();
        try {
            studentQueue.add(student); // Add student to the waiting queue
            this.waitingStudents++;
        } finally {
            this.chairLock.unlock();
        }
    }

    public boolean isFull() {
        return this.waitingStudents >= this.noOfChairs;
    }
}

class Student extends Thread {
    private int id;
    private Semaphore students;
    private Semaphore helped;
    private Lock chairLock;
    private TeachingAssistant ta;
    private boolean isWaiting;

    public Student(int id, Semaphore students, Lock chairLock, TeachingAssistant ta) {
        this.id = id;
        this.students = students;
        this.chairLock = chairLock;
        this.ta = ta;
        this.helped = new Semaphore(0);
        this.isWaiting = false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simulate the student is programming
                this.program();
                // Wait for the TA's help, if they are available or there is abundant seats in
                // the hallway to wait
                if (this.askForHelp()) {
                    this.waitUntilHelped();
                }
                // Return the student to programming
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

    private boolean askForHelp() throws InterruptedException {
        this.chairLock.lock();
        try {
            // Check if the hallway is full
            if (this.ta.isFull()) {
                System.out.println("Student " + id + " found no chairs available. Going back to programming.");
                return false;
            }

            // Student takes a chair and waits for help
            this.ta.increaseWaitingStudents(this); // Add the current student to the TA's queue
            this.isWaiting = true;
            System.out.println("Student " + id + " is waiting in the hallway.");
        } finally {
            this.chairLock.unlock();
        }

        // Notify the TA that a student needs help
        this.students.release();

        // Notify student will receive TA's help
        return true;
    }

    private void waitUntilHelped() throws InterruptedException {
        this.helped.acquire();
    }

    public void signalHelped() {
        this.helped.release();
        this.isWaiting = false;
    }

    public int getStudentId() {
        return this.id;
    }
}

public class SleepingTAManager {
    public static void main(String[] args) {
        int numStudents = 5; // Total number of students
        int numChairs = 3; // Total number of chairs available in the hallway

        // Semaphore to signal the TA for help (initial permits 0)
        Semaphore studentsSemaphore = new Semaphore(0);

        // Lock to manage the chairs in the hallway
        Lock chairLock = new ReentrantLock();

        // Create a TA object with the number of chairs
        TeachingAssistant ta = new TeachingAssistant(studentsSemaphore, chairLock, numChairs);

        // Start the TA thread
        ta.start();

        // Create and start student threads
        for (int i = 1; i <= numStudents; i++) {
            new Student(i, studentsSemaphore, chairLock, ta).start();
        }
    }
}
