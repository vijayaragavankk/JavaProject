package s1;

import java.sql.*;
import java.util.Scanner;

public class ClientModule {
    private Scanner scanner = new Scanner(System.in);
    private int clientId;

    public ClientModule(int clientId) {
        this.clientId = clientId;
    }


    public void showMenu() {
        while (true) {
            System.out.println("\n===== Client Menu =====");
            System.out.println("1. Create a Service Request");
            System.out.println("2. View My Requests");
            System.out.println("3. View Vendor Clarifications & Respond");
            System.out.println("4. Logout");

            int choice = getValidIntegerInput("Enter your choice: ");

            switch (choice) {
                case 1 -> createServiceRequest();
                case 2 -> viewRequests();
                case 3 -> viewVendorClarificationsAndRespond();
                case 4 -> {
                    System.out.println("Logging out... Returning to main menu.");
                    return;
                }
                default -> System.out.println("Invalid choice! Please enter a valid option.");
            }
        }
    }

    private int getValidIntegerInput(String prompt) {
        int input;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
                return input;
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.next(); // Consume invalid input
            }
        }
    }

    private void createServiceRequest() {
        System.out.print("Enter the title for the issue (max 8 words): ");
        String title = scanner.nextLine().trim();
        title = limitWords(title, 8);

        System.out.print("Enter request description: ");
        String description = scanner.nextLine().trim();

        int urgencyChoice = 0;
        while (true) {
            System.out.println("Select the urgency of the issue:");
            System.out.println("1. Low");
            System.out.println("2. Medium");
            System.out.println("3. High");
            System.out.print("Enter your choice: ");

            if (scanner.hasNextInt()) {
                urgencyChoice = scanner.nextInt();
                scanner.nextLine();
                if (urgencyChoice >= 1 && urgencyChoice <= 3) break;
            } else {
                scanner.next();
            }
            System.out.println("Invalid input! Please enter 1, 2, or 3.");
        }

        String urgency = switch (urgencyChoice) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            default -> "Low";
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO tickets (client_id, title, description, urgency, status, created_date) VALUES (?, ?, ?, ?, 'Pending', NOW())";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, clientId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setString(4, urgency);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int ticketId = rs.getInt(1);
                System.out.println("Service request created successfully! Ticket ID: Request" + String.format("%03d", ticketId));
            }
        } catch (SQLException e) {
            System.out.println("Error creating service request. Please try again.");
        }
    }

    private String limitWords(String text, int maxWords) {
        String[] words = text.split(" ");
        if (words.length > maxWords) {
            return String.join(" ", java.util.Arrays.copyOfRange(words, 0, maxWords));
        }
        return text;
    }
    private void viewRequests() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT t.ticket_id, t.title, t.description AS client_description,
                       t.assigned_vendor_id, t.solution_description, 
                       t.steps_taken, t.status, t.resolved_date
                FROM tickets t
                WHERE t.client_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Your Service Requests =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int ticketId = rs.getInt("ticket_id");
                String formattedTicketId = String.format("Request%03d", ticketId);

                System.out.println("Request ID: " + formattedTicketId);
                //System.out.println("Vendor ID: " + rs.getInt("t.assigned_vendor_id"));
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Client Description: " + rs.getString("client_description"));
                System.out.println("Solution Description: " + rs.getString("solution_description"));
                System.out.println("Steps Taken: " + rs.getString("steps_taken"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Resolved Date: " + rs.getString("resolved_date"));
                System.out.println("----------------------------");
            }

            if (!found) System.out.println("No service requests found.");

        } catch (SQLException e) {
            System.out.println("Error viewing service requests. Please try again.");
        }
    }

    private void viewVendorClarificationsAndRespond() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
                SELECT c.clarification_id, c.ticket_id, c.question 
                FROM clarifications c
                JOIN tickets t ON c.ticket_id = t.ticket_id
                WHERE t.client_id = ? AND c.answer IS NULL
            """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Vendor Clarifications =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int ticketId = rs.getInt("ticket_id");
                String formattedTicketId = String.format("Request%03d", ticketId);

                System.out.println("Request ID: " + formattedTicketId);
                int clarificationId = rs.getInt("clarification_id");
                //int ticketId = rs.getInt("ticket_id");
                String question = rs.getString("question");

                System.out.println("Clarification ID: " + clarificationId);
                //System.out.println("Ticket ID: " + ticketId);
                System.out.println("Vendor Question: " + question);
                System.out.print("Enter your response (leave empty to skip): ");

                String answer = scanner.nextLine();
                if (!answer.isEmpty()) {
                    respondToClarification(clarificationId, answer);
                }
            }

            if (!found) System.out.println("No clarifications pending your response.");

        } catch (SQLException e) {
            System.out.println("Error viewing clarifications. Please try again.");
        }
    }

    private void respondToClarification(int clarificationId, String answer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE clarifications SET answer = ?, answered_date = NOW() WHERE clarification_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, answer);
            stmt.setInt(2, clarificationId);
            stmt.executeUpdate();
            System.out.println("Response submitted successfully!");

        } catch (SQLException e) {
            System.out.println("Error submitting response. Please try again.");
        }
    }
}