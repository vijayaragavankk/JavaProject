package s1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
//    private static final String URL = "jdbc:mysql://localhost:3306/CustomerServiceDB4";
    private static final String URL = "jdbc:mysql://3MhHg5E1e8GAsfu.root:bVj4WjF5LtVaSe27@gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/CustomerServiceDB4?sslMode=VERIFY_IDENTITY";
    private static final String USER = "root";
    private static final String PASSWORD = "bVj4WjF5LtVaSe27";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

