package s1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAuthentication {
    public static int authenticateUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // Return user ID
            }
        } catch (SQLException e) {
            System.out.println("Server not Connected");
        }
        return -1;
    }

    public static String getUserRole(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT role FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role"); // Return user role
            }
        } catch (SQLException e) {
            System.out.println("Role not found");
        }
        return null;
    }
}


