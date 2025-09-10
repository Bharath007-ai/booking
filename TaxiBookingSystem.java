import java.util.*;

public class TaxiBookingSystem {
    static List<Taxi> taxis = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static int customerCounter = 1; // Auto-incrementing Customer ID

    public static void main(String[] args) {
        System.out.print("Enter number of taxis: ");
        int numTaxis = readInt("Enter number of taxis: ");
        initializeTaxis(numTaxis);

        while (true) {
            System.out.println("\n1. Book Taxi\n2. Display Taxi Details\n3. Exit");
            System.out.print("Enter your choice: ");
            int choice = readInt("");

            switch (choice) {
                case 1:
                    bookTaxi();
                    break;
                case 2:
                    displayTaxiDetails();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Safe integer reader (handles non-integer input)
    private static int readInt(String prompt) {
        while (true) {
            try {
                if (!prompt.isEmpty()) System.out.print(prompt);
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid number. Please re-enter.");
                sc.next(); // discard bad token
            }
        }
    }

    public static void initializeTaxis(int n) {
        for (int i = 1; i <= n; i++) {
            taxis.add(new Taxi(i));
        }
    }

    // Reads a single A-F point. Rejects anything else (including "F." or multi-char strings).
    private static char readPoint(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.next().trim().toUpperCase();
            if (input.length() != 1) {
                System.out.println("Invalid input. Please enter a single character A-F. Re-enter:");
                continue;
            }
            char c = input.charAt(0);
            if (c < 'A' || c > 'F') {
                System.out.println("Invalid input. Please enter a point between A and F. Re-enter:");
                continue;
            }
            return c;
        }
    }

    public static void bookTaxi() {
        int customerId = customerCounter++; // Automatically increment Customer ID 
        char pickup = readPoint("Enter Pickup Point (A-F): ");
        char drop = readPoint("Enter Drop Point (A-F): ");

        // Optional: ensure drop != pickup (uncomment if you want)
        // while (drop == pickup) {
        //     System.out.println("Drop point cannot be same as pickup. Re-enter drop point:");
        //     drop = readPoint("Enter Drop Point (A-F): ");
        // }

        System.out.print("Enter Pickup Time (in hours, integer): ");
        int pickupTime = readInt("");

        Taxi selectedTaxi = null;
        int minDistance = Integer.MAX_VALUE;

        for (Taxi taxi : taxis) {
            if (taxi.isAvailable(pickupTime)) {
                int distance = Math.abs(taxi.currentPoint - pickup);
                if (selectedTaxi == null ||
                        distance < minDistance ||
                        (distance == minDistance && taxi.totalEarnings < selectedTaxi.totalEarnings)) {
                    selectedTaxi = taxi;
                    minDistance = distance;
                }
            }
        }

        if (selectedTaxi == null) {
            System.out.println("Booking rejected. No taxis available.");
            return;
        }

        int dropTime = pickupTime + Math.abs(drop - pickup);
        int amount = selectedTaxi.calculateEarnings(pickup, drop);
        int bookingId = selectedTaxi.bookings.size() + 1;

        Booking booking = new Booking(bookingId, customerId, pickup, drop, pickupTime, dropTime, amount);
        selectedTaxi.addBooking(booking);
        System.out.println("Taxi-" + selectedTaxi.id + " is allocated.");
    }

    public static void displayTaxiDetails() {
        for (Taxi taxi : taxis) {
            System.out.println("Taxi-" + taxi.id + " Total Earnings: Rs." + taxi.totalEarnings);
            System.out.printf("%-10s %-10s %-5s %-5s %-12s %-9s %-6s%n",
                    "BookingID", "CustomerID", "From", "To", "PickupTime", "DropTime", "Amount");
            for (Booking booking : taxi.bookings) {
                System.out.printf("%-10d %-10d %-5c %-5c %-12d %-9d %-6d%n",
                        booking.bookingId, booking.customerId, booking.from, booking.to,
                        booking.pickupTime, booking.dropTime, booking.amount);
            }
            System.out.println();
        }
    }
}

// Simple Booking class
class Booking {
    int bookingId;
    int customerId;
    char from;
    char to;
    int pickupTime;
    int dropTime;
    int amount;

    public Booking(int bookingId, int customerId, char from, char to, int pickupTime, int dropTime, int amount) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.from = from;
        this.to = to;
        this.pickupTime = pickupTime;
        this.dropTime = dropTime;
        this.amount = amount;
    }
}

// Simple Taxi class
class Taxi {
    int id;
    char currentPoint;
    int totalEarnings;
    List<Booking> bookings;
    int freeAt; // hour when taxi becomes free

    public Taxi(int id) {
        this.id = id;
        this.currentPoint = 'A'; // starting location
        this.totalEarnings = 0;
        this.bookings = new ArrayList<>();
        this.freeAt = 0;
    }

    // Taxi is free if requested pickupTime is >= its freeAt time
    public boolean isAvailable(int pickupTime) {
        return pickupTime >= freeAt;
    }

    // Basic fare calculation: base + per unit distance
    public int calculateEarnings(char pickup, char drop) {
        int distance = Math.abs(drop - pickup);
        int baseFare = 100;
        int perUnit = 10;
        return baseFare + distance * perUnit;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        totalEarnings += booking.amount;
        currentPoint = booking.to;
        freeAt = booking.dropTime; // taxi becomes free at dropTime
    }
}
