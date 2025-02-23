//package s1;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.Scanner;
//
//public class UserRegistration {
//    public static void registerUser() {
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("===== USER REGISTRATION =====");
//        System.out.print("Enter Username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter Password: ");
//        String password = scanner.nextLine();
//        System.out.print("Enter Role (client/vendor): ");
//        String role = scanner.nextLine().toLowerCase();
//
//
//        if (!role.equals("client") && !role.equals("vendor")) {
//            System.out.println("Invalid role! Please enter either 'client' or 'vendor'.");
//            return;
//        }
//
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
//            PreparedStatement stmt = conn.prepareStatement(query);
//            stmt.setString(1, username);
//            stmt.setString(2, password);
//            stmt.setString(3, role);
//            int rowsInserted = stmt.executeUpdate();
//
//            if (rowsInserted > 0) {
//                System.out.println("Registration successful! You can now log in.");
//            } else {
//                System.out.println("Registration failed. Please try again.");
//            }
//        } catch (SQLException e) {
//            System.out.println("Error registering user. Please check connection....");
//        }
//    }
//}
//
