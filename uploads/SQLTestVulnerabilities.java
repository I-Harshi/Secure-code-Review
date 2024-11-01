import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLTestVulnerabilities {

    public static void main(String[] args) {
        // Example : System.exit usage
        if (args.length == 0) {
            System.exit(1);  // This should be detected by the VulnerabilityChecker
        }

        // Example : SQL Injection (Unparameterized Query)
        String userInput = "test'; DROP TABLE users; --";
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb", "user", "pass");
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
            stmt.executeQuery(query);  // This should be detected by the VulnerabilityChecker
        } catch (SQLException e) {
            e.printStackTrace();  // This should be detected by the VulnerabilityChecker
        }

        // Example : Hardcoded Database Credentials
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb;user=root;password=secret");
        } catch (SQLException e) {
            e.printStackTrace();  // This should be detected by the VulnerabilityChecker
        }


        // Example : Excessive Privileges
        // This pattern is unlikely to be in code, more often in SQL scripts or configurations.
        // Just for demonstration purposes
        String sql = "GRANT ALL PRIVILEGES ON *.* TO 'user'@'localhost';";
        
        // Example : Insecure Data Transmission
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb");
            // This connection lacks SSL configuration and should be detected by the VulnerabilityChecker
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
