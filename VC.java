import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class VC {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java VC <file.java>");
            System.exit(1);
        }
        String filePath = args[0];

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            boolean foundVulnerabilities = false;

            foundVulnerabilities |= checkForSystemExit(lines);
            foundVulnerabilities |= checkForSQLVulnerabilities(lines);
            foundVulnerabilities |= runPMDAnalysis(filePath);

            if (!foundVulnerabilities) {
                System.out.println("No vulnerabilities detected.");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static boolean checkForSystemExit(List<String> lines) {
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.contains("System.exit")) {
                System.out.printf("Warning: Use of System.exit detected on line %d. Potential vulnerability.\n", i + 1);
                found = true;
            }
        }
        return found;
    }

    private static boolean checkForSQLVulnerabilities(List<String> lines) {
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Check for SQL Injection (Unparameterized Queries)
            if (line.matches(".*Statement.*executeQuery\\(.*\\+.*\\).*")) {
                System.out.printf("Vulnerability: SQL Injection risk on line %d. Unparameterized query detected.\n", i + 1);
                found = true;
            }

            // Check for Hardcoded Credentials
            if (line.matches(".*(Connection|DriverManager)\\.getConnection\\(.*\"jdbc:.*;user=.*;password=.*\".*\\).*")) {
                System.out.printf("Vulnerability: Hardcoded database credentials on line %d.\n", i + 1);
                found = true;
            }

            // Check for Detailed SQL Error Messages
            if (line.contains("SQLException") && line.contains("printStackTrace")) {
                System.out.printf("Vulnerability: Detailed SQL error messages could leak sensitive information on line %d.\n", i + 1);
                found = true;
            }

            // Check for Excessive Privileges
            if (line.matches(".*GRANT ALL PRIVILEGES.*")) {
                System.out.printf("Vulnerability: Excessive database privileges granted on line %d.\n", i + 1);
                found = true;
            }

            // Check for Insecure Data Transmission (looking for connections without SSL)
            if (line.matches(".*DriverManager.getConnection\\(.*jdbc:mysql://.*") && !line.contains("useSSL=true")) {
                System.out.printf("Vulnerability: Insecure data transmission (no SSL) on line %d.\n", i + 1);
                found = true;
            }

        }
        return found;
    }

    private static boolean runPMDAnalysis(String filePath) {
        boolean foundIssues = false;
        String pmdCommand = "E:\\SEMESTER_4\\INTERNS\\Eviden\\PROJECT\\final\\tool2\\pmd-bin-7.4.0\\bin\\pmd.bat"; // Use the full path to pmd executable
    
        // Command to run PMD
        String[] cmd = {
            pmdCommand,
            "check",
            "-d", filePath,
            "-R", "rulesets/java/quickstart.xml",
            "-f", "text"
        };
    
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process process = processBuilder.redirectErrorStream(true).start();
    
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (!line.contains("No problems found")) {
                        foundIssues = true;
                    }
                }
            }
    
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running PMD analysis: " + e.getMessage());
        }
    
        return foundIssues;
    }
    
}
