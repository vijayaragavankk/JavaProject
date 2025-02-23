package s1;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Scanner;

public class VendorModule {
    private final Scanner scanner = new Scanner(System.in);
    private final int vendorId;

    public VendorModule(int vendorId) {
        this.vendorId = vendorId;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n===== Vendor Menu =====");
            System.out.println("1. View Available Tickets");
            System.out.println("2. View Tickets Assigned To Me");
            System.out.println("3. Assign Ticket to Yourself");
            System.out.println("4. Ask for Clarification");
            System.out.println("5. View Client Responses");
            System.out.println("6. Resolve a Ticket");
            System.out.println("7. View Monthly Resolved Requests");
            System.out.println("8. Logout");

            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number between 1 and 8.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAvailableTickets();
                case 2 -> viewTicketsAssignedToMe();
                case 3 -> assignTicketToVendor();
                case 4 -> askForClarification();
                case 5 -> viewClientResponses();
                case 6 -> resolveTicket();
                case 7 -> viewMonthlyResolvedRequests();
                case 8 -> {
                    System.out.println("Logging out... Returning to main menu.");
                    return;
                }
                default -> System.out.println("Invalid choice! Please try again.");
            }
        }
    }


private void viewAvailableTickets() {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String query = """
            SELECT ticket_id, client_id, title, description, status, urgency
            FROM tickets
            WHERE (assigned_vendor_id IS NULL OR assigned_vendor_id = ?) AND status = 'Pending'
        """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, vendorId); // Replace with the vendor ID
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n===== Available Tickets =====");
        boolean found = false;
        while (rs.next()) {
            found = true;
            int ticketId = rs.getInt("ticket_id");
            System.out.println("Ticket ID: " + String.format("Request%03d", ticketId));
            System.out.println("Client ID: " + String.format("Client%04d", rs.getInt("client_id")));
            System.out.println("Title: " + rs.getString("title"));
            System.out.println("Urgency: " + rs.getString("urgency"));
            System.out.println("Description: ");
            formatAndPrintText(rs.getString("description"), 35);
            System.out.println("Status: " + rs.getString("status"));
            System.out.println("----------------------------");
        }

        if (!found) {
            System.out.println("No tickets available.");
        }

    } catch (SQLException e) {
        System.out.println("Error viewing tickets. Please try again.");
    }
}

private void viewTicketsAssignedToMe() {
    try (Connection conn = DatabaseConnection.getConnection()) {

        String countQuery = """
            SELECT COUNT(*) AS ticket_count
            FROM tickets
            WHERE assigned_vendor_id = ? AND status != 'Resolved'
        """;
        PreparedStatement countStmt = conn.prepareStatement(countQuery);
        countStmt.setInt(1, vendorId);
        ResultSet countRs = countStmt.executeQuery();

        if (countRs.next()) {
            int ticketCount = countRs.getInt("ticket_count");
            System.out.println("\n===== Tickets Assigned to Me =====");
            System.out.println("You currently have " + ticketCount + " ticket(s) assigned to you.");

            if (ticketCount > 0) {
                String detailsQuery = """
                    SELECT ticket_id, client_id, title, description, urgency, status, created_date
                    FROM tickets
                    WHERE assigned_vendor_id = ? AND status != 'Resolved'
                """;
                PreparedStatement detailsStmt = conn.prepareStatement(detailsQuery);
                detailsStmt.setInt(1, vendorId);
                ResultSet detailsRs = detailsStmt.executeQuery();

                System.out.println("\n===== Detailed Information of Assigned Tickets =====");
                while (detailsRs.next()) {
                    int ticketId = detailsRs.getInt("ticket_id");
                    System.out.println("Ticket ID: " + String.format("Request%03d", ticketId));
                    System.out.println("Client ID: " + String.format("Client%03d", detailsRs.getInt("client_id")));
                    System.out.println("Title: " + detailsRs.getString("title"));
                    System.out.println("Urgency: " + detailsRs.getString("urgency"));
                    System.out.println("Description: ");
                    formatAndPrintText(detailsRs.getString("description"), 20);
                    System.out.println("Status: " + detailsRs.getString("status"));
                    System.out.println("Created Date: " + detailsRs.getDate("created_date"));
                    System.out.println("----------------------------");
                }
            } else {
                System.out.println("No tickets are currently assigned to you.");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error retrieving assigned tickets. Please try again.");
    }
}


    private void assignTicketToVendor() {
        System.out.print("Enter Ticket ID to assign to yourself: ");
        String ticketIdInput = scanner.nextLine();

        try {
            int ticketId = Integer.parseInt(ticketIdInput.replaceAll("[^0-9]", ""));

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkQuery = "SELECT status, assigned_vendor_id FROM tickets WHERE ticket_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, ticketId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    Integer assignedVendorId = rs.getObject("assigned_vendor_id", Integer.class);

                    if ("Resolved".equalsIgnoreCase(status)) {
                        System.out.println("This ticket is already resolved.");
                        return;
                    }

                    if (assignedVendorId != null && !assignedVendorId.equals(vendorId)) {
                        System.out.println("This ticket is already assigned to another vendor.");
                        return;
                    }

                    String updateQuery = "UPDATE tickets SET assigned_vendor_id = ?, status = 'In Progress' WHERE ticket_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, vendorId);
                    updateStmt.setInt(2, ticketId);

                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Ticket assigned to you and marked as 'In Progress'.");
                    } else {
                        System.out.println("Failed to assign ticket. Please ensure the Ticket ID is valid and available.");
                    }
                } else {
                    System.out.println("Invalid Ticket ID. Please try again.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Ticket ID format! Please enter a valid Ticket ID (e.g., Request001).");
        } catch (SQLException e) {
            System.out.println("Error assigning ticket. Please try again.");
        }
    }

    private void askForClarification() {
        System.out.print("Enter Ticket ID to request clarification: ");
        String ticketIdInput = scanner.nextLine();
        try {
            int ticketId = Integer.parseInt(ticketIdInput.replaceAll("[^0-9]", ""));

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkQuery = "SELECT status FROM tickets WHERE ticket_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, ticketId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getString("status").equalsIgnoreCase("Resolved")) {
                    System.out.println("This request is already resolved. You cannot ask for clarification.");
                    return;
                }
            }

            System.out.print("Enter your clarification question: ");
            String question = scanner.nextLine();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO clarifications (ticket_id, question, vendor_id) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, ticketId);
                stmt.setString(2, question);
                stmt.setInt(3, vendorId);
                stmt.executeUpdate();
                System.out.println("Clarification request sent to the client.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Ticket ID format! Please enter a valid Ticket ID (e.g., Request001).");
        } catch (SQLException e) {
            System.out.println("Error sending clarification request. Please try again.");
        }
    }



    private void viewClientResponses() {
        System.out.print("Enter Ticket ID to view client responses: ");
        String ticketIdInput = scanner.nextLine();
        try {
            int ticketId = Integer.parseInt(ticketIdInput.replaceAll("[^0-9]", ""));

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = """
                    SELECT 
                        c.clarification_id, 
                        c.ticket_id, 
                        c.question, 
                        c.answer, 
                        t.client_id, 
                        u.name AS client_name, 
                        t.status 
                    FROM clarifications c
                    JOIN tickets t ON c.ticket_id = t.ticket_id
                    JOIN users u ON t.client_id = u.user_id
                    WHERE c.answer IS NOT NULL AND c.ticket_id = ?
                """;
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, ticketId);
                ResultSet rs = stmt.executeQuery();

                System.out.println("\n===== Client Responses for Ticket ID: " + ticketId + " =====");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("Clarification ID: " + rs.getInt("clarification_id"));
                    ticketId = rs.getInt("ticket_id");
                    System.out.println("Ticket ID: " + String.format("Request%03d", ticketId));
                    System.out.println("Client ID: " + String.format("Client%04d", rs.getInt("client_id")));
                    //System.out.println("Ticket ID: " + rs.getInt("ticket_id"));
                    //System.out.println("Client ID: " + rs.getInt("client_id"));
                    System.out.println("Client Name: " + rs.getString("client_name"));
                    System.out.println("Status: " + rs.getString("status"));
                    System.out.println("Question: " + rs.getString("question"));
                    System.out.println("Client Response: " + rs.getString("answer"));
                    System.out.println("----------------------");
                }

                if (!found) System.out.println("No responses from the client for this ticket.");

            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Ticket ID format! Please enter a valid Ticket ID (e.g., Request001).");
        } catch (SQLException e) {
            System.out.println("Error viewing client responses. Please try again.");
        }
    }



    private void resolveTicket() {
        System.out.print("Enter Ticket ID to resolve: ");
        String ticketIdInput = scanner.nextLine();
        try {
            int ticketId = Integer.parseInt(ticketIdInput.replaceAll("[^0-9]", ""));

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkQuery = "SELECT assigned_vendor_id FROM tickets WHERE ticket_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, ticketId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int assignedVendorId = rs.getInt("assigned_vendor_id");
                    if (assignedVendorId != vendorId) {
                        System.out.println("This ticket is not assigned to you.");
                        return;
                    }

                    System.out.print("Enter your solution description: ");
                    String solutionDescription = scanner.nextLine();

                    System.out.print("Enter the steps taken to resolve the issue: ");
                    String stepsTaken = scanner.nextLine();

                    String query = """
                        UPDATE tickets 
                        SET status = 'Resolved', solution_description = ?, steps_taken = ?, resolved_date = NOW(), assigned_vendor_id = NULL 
                        WHERE ticket_id = ?
                    """;
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, solutionDescription);
                    stmt.setString(2, stepsTaken);
                    stmt.setInt(3, ticketId);
                    stmt.executeUpdate();
                    System.out.println("Service request resolved successfully!");
                } else {
                    System.out.println("Invalid Ticket ID. Please try again.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Ticket ID format! Please enter a valid Ticket ID (e.g., Request002).");
        } catch (SQLException e) {
            System.out.println("Error resolving the ticket. Please try again.");
        }
    }


    private void viewMonthlyResolvedRequests() {
        int year = 0;
        int month = 0;
        boolean validInput = false;


        while (!validInput) {
            System.out.print("Enter year (YYYY): ");
            if (scanner.hasNextInt()) {
                year = scanner.nextInt();
                if (year >= 1900 && year <= 2100) {
                    validInput = true;
                } else {
                    System.out.println("Invalid year! Please enter a year between 1900 and 2100.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid year (e.g., 2023).");
                scanner.next();
            }
        }

        validInput = false;

        while (!validInput) {
            System.out.print("Enter month (MM): ");
            if (scanner.hasNextInt()) {
                month = scanner.nextInt();
                if (month >= 1 && month <= 12) {
                    validInput = true;
                } else {
                    System.out.println("Invalid month! Please enter a month between 1 and 12.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid month (e.g., 10).");
                scanner.next();
            }
        }
        scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = """
            SELECT
                t.ticket_id,
                t.client_id,
                u.name AS client_name,
                t.title,
                t.vendor_id,
                t.assigned_vendor_id,
                t.solution_description,
                t.created_date,
                t.resolved_date
            FROM tickets t
            JOIN users u ON t.client_id = u.user_id
            WHERE t.status = 'Resolved'
            AND YEAR(t.resolved_date) = ?
            AND MONTH(t.resolved_date) = ?
        """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n======== Resolved Tickets for " + year + "-" + String.format("%02d", month) + " ========");
            System.out.printf("%-12s %-12s %-20s %-60s %-70s %-15s %-15s%n",
                    "Ticket ID", "Vendor ID", "Client Name", "Title", "Solution", "Created Date", "Resolved Date");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;
                String formattedTicketId = String.format("Request%05d", rs.getInt("ticket_id"));
                String formattedClientId = String.format("Vendor%04d", rs.getInt("client_id"));
                System.out.printf("%-12s %-12s %-20s %-60s %-70s %-15s %-15s%n",
                        formattedTicketId,
                        formattedClientId,
                        rs.getString("client_name"),
                        rs.getString("title"),
                        rs.getString("solution_description"),
                        rs.getDate("created_date"),
                        rs.getDate("resolved_date")
                );
            }

            if (!found) {
                System.out.println("No resolved tickets found for this month.");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing monthly resolved requests. Please try again.");
        }
    }



    private void formatAndPrintText(@NotNull String text, int lineLength) {
        int length = text.length();
        for (int i = 0; i < length; i += lineLength) {
            System.out.println(text.substring(i, Math.min(length, i + lineLength)));
        }
    }
}