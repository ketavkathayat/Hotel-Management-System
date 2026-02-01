import java.util.Scanner;

public class HotelManagementSystem {
// DECLARATION OF VARIABLES AND IDENTIFIERS AND THEIR SIZES
    // --- Data Storage Arrays ---
    public static final int NUM_ROOMS = 30;
    public static final int MAX_RECORDS = 100;
    public static final int MAX_BOOK_PER_PERSON = 6;

    // Room Rate Constants
    public static final double NON_AC_RATE = 1000.0;
    public static final double AC_STANDARD_RATE = 1300.0;
    public static final double AC_DELUXE_RATE = 1900.0;

    //  Arrays for Room Data
    public static int[] roomNumbers = new int[NUM_ROOMS];
    public static String[] roomTypes = new String[NUM_ROOMS];
    public static boolean[] isOccupied = new boolean[NUM_ROOMS];
    public static String[] guestNames = new String[NUM_ROOMS];
    public static String[] phoneNumbers = new String[NUM_ROOMS];
    public static int[] personsCount = new int[NUM_ROOMS];

    // Day-based tracking

    public static int[] startDay = new int[NUM_ROOMS];
    public static int[] daysStayed = new int[NUM_ROOMS];
    public static double[] advancePayment = new double[NUM_ROOMS];

    // Arrays for Billing and Admin
    public static String[] billingRecords = new String[MAX_RECORDS];
    public static int recordCount = 0;
    public static double totalProfit = 0.0;

    //Parallel Arrays for Admin Data
    public static String[] adminNames = {"Anurag Singh", "Ketav Kathayat"};
    public static String[] adminPasswords = {"25817", "25084"};
    public static double[] adminBalances = {0.0, 0.0, 0.0};


    // --- Initialization Method ---
    public static void initializeRooms() {
        int idx = 0;
        // Non-AC (101-110)
        for (int i = 101; i <= 110; i++) {
            roomNumbers[idx] = i;
            roomTypes[idx] = "Non-AC";
            guestNames[idx] = "Empty";
            idx++;
        }
        // AC Standard (201-212)
        for (int i = 201; i <= 212; i++) {
            roomNumbers[idx] = i;
            roomTypes[idx] = "AC Standard";
            guestNames[idx] = "Empty";
            idx++;
        }
        // AC Deluxe (301-308)
        for (int i = 301; i <= 308; i++) {
            roomNumbers[idx] = i;
            roomTypes[idx] = "AC Deluxe";
            guestNames[idx] = "Empty";
            idx++;
        }
    }

    // ---Method to find array index by Room Number ---
    public static int findRoomIndex(int roomNo) {
        for (int i = 0; i < NUM_ROOMS; i++) {
            if (roomNumbers[i] == roomNo) {
                return i;
            }
        }
        return -1;
    }

    // --- Display Available Rooms ---
    public static void displayAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");
        System.out.printf("%-15s %-15s %-15s %-15s %-20s\n", "Room No.", "Type", "Status", "Start Day", "Estimated Last Day");
        System.out.println("----------------------------------------------------------------------");

        for (int i = 0; i < NUM_ROOMS; i++) {
            String status = isOccupied[i] ? "Occupied" : "Available";
            String startD;
            String lastD;

            if (isOccupied[i]) {
                startD = startDay[i] == 0 ? "Today" : "Day " + startDay[i];
                lastD = "Day " + (startDay[i] + daysStayed[i] - 1);
            } else {
                startD = "---";
                lastD = "---";
            }

            System.out.printf("%-15d %-15s %-15s %-15s %-20s\n",
                    roomNumbers[i], roomTypes[i], status, startD, lastD);
        }
    }

    // ---Check-in (Book) ---
    public static void checkIn(Scanner sc) {
        System.out.print("Enter number of rooms to book (Max " + MAX_BOOK_PER_PERSON + "): ");
        if (!sc.hasNextInt()) {
            sc.nextLine();
            System.out.println("Invalid input. Booking cancelled.");
            return;
        }
        int numRoomsToBook = sc.nextInt();
        sc.nextLine();

        if (numRoomsToBook < 1 || numRoomsToBook > MAX_BOOK_PER_PERSON) {
            System.out.println("Cannot book " + numRoomsToBook + " rooms. Max " + MAX_BOOK_PER_PERSON + " allowed.");
            System.out.println("For more rooms, contact Manager - 9311314509.");
            return;
        }

        // Check total capacity
        int availableCount = 0;
        for (boolean occupied : isOccupied) {
            if (!occupied) availableCount++;
        }
        if (numRoomsToBook > availableCount) {
            System.out.println("Only " + availableCount + " rooms available. Booking cancelled.");
            return;
        }

        String primaryName = "";
        String primaryPhone = "";

        for (int i = 0; i < numRoomsToBook; i++) {
            System.out.println("\n--- Booking Room " + (i + 1) + " of " + numRoomsToBook + " ---");

            System.out.print("Enter room number to book: ");
            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("Invalid room number input. Booking stopped.");
                return;
            }
            int roomNo = sc.nextInt();
            sc.nextLine();

            int roomIndex = findRoomIndex(roomNo);

            if (roomIndex == -1 || isOccupied[roomIndex]) {
                System.out.println("Room " + roomNo + " does not exist or is occupied. Re-entering details for Room " + (i + 1) + ".");
                i--;
                continue;
            }

            // --- Collect Details ---
            String currentName = "";
            String currentPhone = "";
            int currentPersons = 0;
            int currentDays = 0;
            int currentStartDay = 0;
            double currentAdvance = 0.0;
            double rate = getRateByType(roomTypes[roomIndex]);

            if (i == 0) {
                // First room: Collect all primary details
                System.out.print("Enter guest name (Primary Booker): ");
                primaryName = sc.nextLine();
                System.out.print("Enter phone number: ");
                primaryPhone = sc.nextLine();
                currentName = primaryName;
                currentPhone = primaryPhone;
            } else {
                // Subsequent rooms: Ask for reuse option
                System.out.print("Are the name and phone same as primary guest (" + primaryName + ")? (y/n): ");
                String sameDetails = sc.nextLine();

                if (sameDetails.equalsIgnoreCase("y")) {
                    currentName = primaryName;
                    currentPhone = primaryPhone;
                } else {
                    System.out.print("Enter new guest name for this room: ");
                    currentName = sc.nextLine();
                    System.out.print("Enter phone number for this room: ");
                    currentPhone = sc.nextLine();
                }
            }

            // Collect Persons, Days, and Start Date
            System.out.print("Enter number of persons (max 3): ");
            currentPersons = sc.nextInt();
            sc.nextLine();
            if (currentPersons < 1 || currentPersons > 3) {
                System.out.println("Invalid persons. Re-attempting room entry.");
                i--;
                continue;
            }

            // UPDATED PROMPT
            System.out.print("Enter start day (0 for today, 1 for tomorrow, 2 for day after tomorrow, and so on): ");
            currentStartDay = sc.nextInt();
            sc.nextLine();
            if (currentStartDay < 0) {
                System.out.println("Invalid start day. Re-attempting room entry.");
                i--;
                continue;
            }

            System.out.print("Enter estimated days of stay (min 1): ");
            currentDays = sc.nextInt();
            sc.nextLine();
            if (currentDays < 1) {
                System.out.println("Must book for at least 1 day. Re-attempting room entry.");
                i--;
                continue;
            }

            // --- Advance Payment Logic & Confirmation ---
            if (currentStartDay > 0) {
                currentAdvance = rate * 0.20; // 20% of first day's rent
                System.out.printf("This is a future booking (Day %d). Advance payment required: Rs.%.2f\n", currentStartDay, currentAdvance);
                System.out.print("Confirm booking and pay advance? (y/n): ");
                String confirm = sc.nextLine();

                if (!confirm.equalsIgnoreCase("y")) {
                    System.out.println("Booking cancelled by user.");
                    if (numRoomsToBook > 1) {
                        continue;
                    } else {
                        return;
                    }
                }
            } else {
                System.out.print("Confirm booking? (y/n): ");
                String confirm = sc.nextLine();
                if (!confirm.equalsIgnoreCase("y")) {
                    System.out.println("Booking cancelled by user.");
                    if (numRoomsToBook > 1) {
                        continue;
                    } else {
                        return;
                    }
                }
            }

            // --- Update Room Data ---
            isOccupied[roomIndex] = true;
            guestNames[roomIndex] = currentName;
            phoneNumbers[roomIndex] = currentPhone;
            personsCount[roomIndex] = currentPersons;
            startDay[roomIndex] = currentStartDay;
            daysStayed[roomIndex] = currentDays;
            advancePayment[roomIndex] = currentAdvance;

            System.out.println("SUCCESS: Room " + roomNo + " booked for " + currentName + ".");
        }
        System.out.println("\nAll " + numRoomsToBook + " rooms have been processed.");
    }

    // --- Core Logic 3: Check-out (Bill) ---
    public static void checkOut(Scanner sc) {
        System.out.print("Enter room number to check out: ");
        if (!sc.hasNextInt()) {
            sc.nextLine();
            System.out.println("Invalid room number input.");
            return;
        }
        int roomNo = sc.nextInt();
        sc.nextLine();

        int roomIndex = findRoomIndex(roomNo);

        if (roomIndex == -1 || !isOccupied[roomIndex]) {
            System.out.println("Invalid room number or room is not occupied/available for check-out.");
            return;
        }

        double rate = getRateByType(roomTypes[roomIndex]);
        int days = daysStayed[roomIndex];
        double advance = advancePayment[roomIndex];
        double totalAmount = rate * days;
        double netPayable = totalAmount - advance;

        // Add to billing records
        if (recordCount < MAX_RECORDS) {
            String record = "Guest: " + guestNames[roomIndex] +
                    " | Room: " + roomNumbers[roomIndex] +
                    " | Days: " + days + " | Total: Rs." + totalAmount +
                    " | Advance: Rs." + advance + " | Net: Rs." + netPayable;
            billingRecords[recordCount] = record;
            recordCount++;
        }

        totalProfit += netPayable;
        updateAdminBalance(netPayable);

        System.out.println("\n--- BILL GENERATED ---");
        System.out.println("Guest: " + guestNames[roomIndex]);
        System.out.println("Room: " + roomNo + " (" + roomTypes[roomIndex] + ")");
        System.out.println("Total Stay Cost: Rs." + totalAmount);
        System.out.println("Advance Paid: Rs." + advance);
        System.out.println("**Net Amount Due: Rs." + netPayable + "**");

        // Clear room data
        isOccupied[roomIndex] = false;
        guestNames[roomIndex] = "Empty";
        advancePayment[roomIndex] = 0.0;
        startDay[roomIndex] = 0;
    }

    // --- Core Logic 4: Cancel Booking ---
    public static void cancelBooking(Scanner sc) {
        System.out.print("Enter room number to cancel: ");
        if (!sc.hasNextInt()) {
            sc.nextLine();
            System.out.println("Invalid room number input.");
            return;
        }
        int roomNo = sc.nextInt();
        sc.nextLine();

        int roomIndex = findRoomIndex(roomNo);

        if (roomIndex == -1 || !isOccupied[roomIndex]) {
            System.out.println("Invalid room number or room is not currently booked.");
            return;
        }

        double advance = advancePayment[roomIndex];
        int sDay = startDay[roomIndex];
        double refund = 0.0;
        String message = "Booking confirmed. No advance paid.";

        System.out.print("Confirm cancellation for Room " + roomNo + " (y/n): ");
        if (!sc.nextLine().equalsIgnoreCase("y")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        if (advance > 0.0) {
            // Cancellation logic: 5% refund if done at least 1 day before check-in (sDay > 1)
            if (sDay > 1) {
                refund = advance * 0.05;
                message = String.format("Advance Rs.%.2f. Refund (5%%) is Rs.%.2f. Loss: Rs.%.2f", advance, refund, advance - refund);
            } else {
                refund = 0.0;
                message = String.format("Advance Rs.%.2f. Cancellation too late (Check-in is today/tomorrow). No refund.", advance);
            }
            totalProfit -= (advance - refund);
        }

        // Clear room data
        isOccupied[roomIndex] = false;
        guestNames[roomIndex] = "Empty";
        advancePayment[roomIndex] = 0.0;
        startDay[roomIndex] = 0;

        System.out.println("\n--- CANCELLATION SUCCESSFUL (Room " + roomNo + ") ---");
        System.out.println(message);
        System.out.println("Room is now available.");
    }

    // --- Utility Methods ---
    public static double getRateByType(String type) {
        switch (type) {
            case "Non-AC":
                return NON_AC_RATE;
            case "AC Standard":
                return AC_STANDARD_RATE;
            case "AC Deluxe":
                return AC_DELUXE_RATE;
        }
        return 0;
    }

    public static void updateAdminBalance(double amount) {
        double share = amount / (double) adminNames.length;
        for (int i = 0; i < adminNames.length; i++) {
            adminBalances[i] += share;
        }
    }

    public static void showBillingRecords() {
        System.out.println("\n--- Billing Records ---");
        if (recordCount == 0) {
            System.out.println("No billing records yet.");
            return;
        }
        for (int i = 0; i < recordCount; i++) {
            System.out.println((i + 1) + ". " + billingRecords[i]);
        }
    }

    public static void adminLogin(Scanner sc) {
        System.out.print("Enter admin name: ");
        String admin = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        int adminIndex = -1;
        for (int i = 0; i < adminNames.length; i++) {
            if (admin.equals(adminNames[i]) && password.equals(adminPasswords[i])) {
                adminIndex = i;
                break;
            }
        }

        if (adminIndex != -1) {
            System.out.println("Login successful. Welcome " + adminNames[adminIndex]);
            adminMenu(sc, adminIndex);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    // --- Admin Menu ---
    public static void adminMenu(Scanner sc, int adminIndex) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Total Profit");
            System.out.println("2. Individual Balance");
            System.out.println("3. Withdraw");
            System.out.println("4. View Billing Records");
            System.out.println("5. Exit Admin Menu");
            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Total Profit: Rs." + totalProfit);
                    break;
                case 2:
                    System.out.println(adminNames[adminIndex] + " Balance: Rs." + adminBalances[adminIndex]);
                    break;
                case 3:
                    System.out.print("Enter amount to withdraw: ");
                    if (!sc.hasNextDouble()) {
                        sc.nextLine();
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }
                    double amt = sc.nextDouble();
                    sc.nextLine();

                    if (amt <= adminBalances[adminIndex]) {
                        adminBalances[adminIndex] -= amt;
                        System.out.println("Withdrawal successful. Remaining balance: Rs." + adminBalances[adminIndex]);
                    } else {
                        System.out.println("Insufficient balance.");
                    }
                    break;
                case 4:
                    showBillingRecords();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    // --- Main Method (Entry Point) ---
    public static void main(String[] args) {
        initializeRooms();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Hotel Management System ---");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Check-in (Book)");
            System.out.println("3. Check-out (Bill)");
            System.out.println("4. Cancel Booking");
            System.out.println("5. Admin Login");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            if (!sc.hasNextInt()) {
                sc.nextLine();
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    displayAvailableRooms();
                    break;
                case 2:
                    checkIn(sc);
                    break;
                case 3:
                    checkOut(sc);
                    break;
                case 4:
                    cancelBooking(sc);
                    break;
                case 5:
                    adminLogin(sc);
                    break;
                case 6:
                    System.out.println("Exiting System. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }
}