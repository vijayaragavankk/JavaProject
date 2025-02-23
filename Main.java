package s1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Customer Service Request System =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid Choice! Please enter an integer or number");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> {
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void registerUser() {
        System.out.println("\n===== User Registration =====");

        String role = null;
        while (role == null) {
            System.out.print("Select role (1: Client, 2: Vendor): ");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter an integer value.");
                scanner.next();
                continue;
            }
            int roleChoice = scanner.nextInt();
            scanner.nextLine();

            if (roleChoice == 1) {
                role = "client";
            } else if (roleChoice == 2) {
                role = "vendor";
            } else {
                System.out.println("Invalid choice! Please enter 1 for Client or 2 for Vendor.");
            }
        }

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        String username;
        while (true) {
            System.out.print("Enter username (email format with only letters and numbers): ");
            username = scanner.nextLine();
            if (Pattern.matches("^[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z]{2,6}$", username)) {
                break;
            } else {
                System.out.println("Invalid email format! Please enter a valid email without special characters.");
            }
        }

        String password;
        while (true) {
            System.out.print("Enter password (must contain at least one letter, one number, and one special character): ");
            password = scanner.nextLine();
            //if (Pattern.matches("^(?=.[A-Za-z])(?=.\\d)(?=.[@$!%?&])[A-Za-z\\d@$!%*?&]{6,}$", password))
            if (Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%*?&]{6,}$", password))
                {
                break;
            } else {
                System.out.println("Invalid password! Ensure it contains at least one letter, one number, and one special character.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (name, username, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.executeUpdate();

            System.out.println("Registration successful! You can now log in.");
        } catch (SQLException e) {
            System.out.println("Error registering user. Please try again.");
        }
    }

    private static void loginUser() {
        System.out.println("\n===== User Login =====");

        int roleChoice = 0;
        while (true) {
            System.out.println("1. Client Login");
            System.out.println("2. Vendor Login");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter an integer value.");
                scanner.next();
                continue;
            }

            roleChoice = scanner.nextInt();
            scanner.nextLine();

            if (roleChoice == 1 || roleChoice == 2) {
                break;
            } else {
                System.out.println("Invalid choice! Please enter 1 for Client or 2 for Vendor.");
            }
        }

        String role = (roleChoice == 1) ? "client" : "vendor";

        System.out.println("===== " + role + " Login =====");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT user_id FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                System.out.println("Login successful! Redirecting to " + role + " module...");

                if (role.equals("client")) {
                    new ClientModule(userId).showMenu();
                } else if (role.equals("vendor")) {
                    new VendorModule(userId).showMenu();
                }
            } else {
                System.out.println("Invalid username or password for " + role + ". Try again.");
            }
        } catch (SQLException e) {
            System.out.println("Error logging in. Please try again.");
        }
    }
}