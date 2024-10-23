package ParkingLotManagementProblem;

import java.util.concurrent.Semaphore;

class ParkingLot {

    // Tracks available parking spaces
    private Semaphore parkingSpaces;

    // Ensures mutual exclusion for parking and leaving
    private Semaphore parkingController;

    public ParkingLot(int size) {
        this.parkingSpaces = new Semaphore(size);
        this.parkingController = new Semaphore(1);
    }

    public void parkCar(int carId) throws InterruptedException {
        // Find a parking spot
        this.parkingSpaces.acquire();
        // Ensure parking is exclusive
        this.parkingController.acquire();
        System.out.println("Car " + carId + " has parked.");
        this.parkingController.release();
    }

    public void exitCar(int carId) throws InterruptedException {
        // Ensure leaving is exclusive
        this.parkingController.acquire();
        System.out.println("Car " + carId + " has left the parking lot.");
        this.parkingController.release();

        // Release the the parking spot for another car to park
        this.parkingSpaces.release();
    }
}

class Car extends Thread {

    // Unique identifier for a car
    private int carId;
    private ParkingLot parkingLot;

    public Car(int carId, ParkingLot parkingLot) {
        this.carId = carId;
        this.parkingLot = parkingLot;
    }

    // Simulate parking for a car
    public void run() {
        try {
            // Try to park this car
            this.parkingLot.parkCar(this.carId);

            // Simulate time parked
            Thread.sleep((int) (Math.random() * 8000));

            // Exit parking after timer
            this.parkingLot.exitCar(this.carId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class ParkingLotManager {
    public static void main(String[] args) {
        // Create a parking lot with 10 spaces
        ParkingLot parkingLot = new ParkingLot(10);

        // Simulate 13 cars trying to park
        for (int i = 1; i <= 13; i++) {
            Car car = new Car(i, parkingLot);
            car.start();

            // Simulate arrival time between cars
            try {
                Thread.sleep((int) (Math.random() * 1500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}