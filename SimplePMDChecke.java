import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimplePMDChecke {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java SimplePMDChecker <file.java>");
            System.exit(1);
        }

        String filePath = args[0];
        try {
            String code = readFile(filePath);
            checkForUnusedVariables(code);
            checkForEmptyCatchBlocks(code);
            checkForDuplicateCode(code);
            checkForOvercomplicatedExpressions(code);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void checkForUnusedVariables(String code) {
        // Regular expressions to find variable declarations and usages
        Pattern declarePattern = Pattern.compile("\\b(?:int|double|float|long|short|byte|boolean|char|String)\\s+([a-zA-Z_]\\w*)\\s*(?:=\\s*[^;]*)?\\s*;"); // Variable declarations
        Pattern usagePattern = Pattern.compile("\\b([a-zA-Z_]\\w*)\\b"); // Variable usages

        Matcher declareMatcher = declarePattern.matcher(code);
        Set<String> declaredVariables = new HashSet<>();
        Set<String> usedVariables = new HashSet<>();

        // Find all declared variables
        while (declareMatcher.find()) {
            declaredVariables.add(declareMatcher.group(1));
        }

        Matcher usageMatcher = usagePattern.matcher(code);
        // Find all variable usages
        while (usageMatcher.find()) {
            String variable = usageMatcher.group(1);
            if (declaredVariables.contains(variable) && !isJavaKeyword(variable)) {
                usedVariables.add(variable);
            }
        }

        // Identify unused variables
        declaredVariables.removeAll(usedVariables);
        if (!declaredVariables.isEmpty()) {
            System.out.println("Warning: Unused variables detected: " + declaredVariables);
        }
    }

    private static boolean isJavaKeyword(String word) {
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while"
        };
        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private static void checkForEmptyCatchBlocks(String code) {
        Pattern pattern = Pattern.compile("catch\\s*\\(.*\\)\\s*\\{\\s*\\}");
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            System.out.println("Warning: Empty catch block detected at line " + getLineNumber(code, matcher.start()));
        }
    }

    private static void checkForDuplicateCode(String code) {
        String[] lines = code.split("\n");
        Set<String> seenLines = new HashSet<>();
        Set<String> duplicateLines = new HashSet<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && seenLines.contains(line)) {
                duplicateLines.add(line);
            } else {
                seenLines.add(line);
            }
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (duplicateLines.contains(line)) {
                System.out.println("Warning: Duplicate code detected on line " + (i + 1));
            }
        }
    }

    private static void checkForOvercomplicatedExpressions(String code) {
        Pattern pattern = Pattern.compile("\\b(?:if|while|for)\\s*\\(.*\\)\\s*\\{");
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            int start = matcher.start();
            int end = code.indexOf('}', start) + 1;
            if (end == 0) {
                continue; // Skip if closing brace is not found
            }
            String block = code.substring(start, end);
            String[] lines = block.split("\n");
            for (String line : lines) {
                if (line.length() > 100) { // Arbitrary length limit for complexity
                    System.out.println("Warning: Overcomplicated expression detected around line " + getLineNumber(code, start));
                    break;
                }
            }
        }
    }

    private static int getLineNumber(String code, int position) {
        return code.substring(0, position).split("\n").length;
    }
}
