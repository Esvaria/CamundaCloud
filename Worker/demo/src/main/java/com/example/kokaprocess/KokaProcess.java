package com.example.kokaprocess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.nio.file.Files;

@Component
public class KokaProcess {

    private static String apiKey;
    @Value("${api-ninjas.key}")
    public void setApiKey(String key) {
        apiKey = key;
    }
    public KokaProcess() {
    }

    public static boolean getPaymentInformation(String cardholderName, String cardNumber, String expiryDate) {
        // Check if the cardholder name is not null or empty
        if (cardholderName == null || cardholderName.trim().isEmpty()) {
            return false;
        }
        // Check if the card number is 16 digits long
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        // Check if the expiry date is in the future
        if (expiryDate == null || expiryDate.length() != 5) {
            return false;
        }
        return true;
    }
    public static void createDatabase() {
        String relativePath = "CamundaCloud/Worker/demo/src/main/resources/db.sqlite";
        File dbFile = new File(relativePath);
        boolean dbExists = dbFile.exists();

        if (!dbExists) {
            try {
                File parentDir = dbFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                String url = "jdbc:sqlite:" + relativePath;

                try (Connection conn = DriverManager.getConnection(url);
                     Statement stmt = conn.createStatement()) {

                    String createTableSQL = "CREATE TABLE IF NOT EXISTS products (\n"
                            + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + "    name TEXT NOT NULL,\n"
                            + "    quantity INTEGER\n"
                            + ");";
                    stmt.execute(createTableSQL);
                    System.out.println("A new database has been created along with the 'products' table.");

                    String insertSQL = "INSERT INTO products(name, quantity) VALUES(?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                        pstmt.setString(1, "selfLamp");
                        pstmt.setInt(2, 99);
                        pstmt.executeUpdate();

                        pstmt.setString(1, "standingDesk");
                        pstmt.setInt(2, 99);
                        pstmt.executeUpdate();

                        pstmt.setString(1, "secretLabChair");
                        pstmt.setInt(2, 99);
                        pstmt.executeUpdate();
                    }
                    System.out.println("Initial data has been inserted into the 'products' table.");

                } catch (SQLException e) {
                    System.out.println("Error executing SQL queries: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("Error creating database: " + e.getMessage());
            }
        } else {
            System.out.println("Database already exists.");
        }
    }
    // Method to fetch current quantity of a product from database
    public static int getProductQuantity(String productName) throws SQLException {
        String url = "jdbc:sqlite:CamundaCloud/Worker/demo/src/main/resources/db.sqlite";
        String selectSQL = "SELECT quantity FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, productName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            } else {
                throw new SQLException("Product not found: " + productName);
            }
        }
    }
    // Method to create JSON file from variables and return file name
    public static void createJsonFile(Map<String, Object> variables) throws IOException {
        // Create directory if it doesn't exist
        String directoryPath = "CamundaCloud/Worker/demo/src/main/resources/jsonReceipts";
        createDirectoryIfNotExists(directoryPath);

        // Generate file name based on current time
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String jsonFileName = "receipt_" + timeStamp + ".json";
        String filePath = directoryPath + "/" + jsonFileName;

        // Serialize variables to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath), variables);
    }

    // Method to create directory if it doesn't exist
    public static void createDirectoryIfNotExists(String directoryPath) {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Created directory: " + directoryPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + directoryPath, e);
            }
        }
    }
    // Method to format datetime from API response
    public static String formatDateTime(String datetime) {
        try {
            // Parse the datetime string assuming it is in ISO_OFFSET_DATE_TIME format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetime, inputFormatter);

            // Format the datetime into the desired format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return zonedDateTime.format(outputFormatter);
        } catch (Exception e) {
            System.err.println("Failed to parse and format datetime: " + datetime);
            throw new RuntimeException("Invalid datetime format", e);
        }
    }

    // Method to update product quantity in database
    public static void updateProductQuantity(String productName, int newQuantity) throws SQLException {
        String url = "jdbc:sqlite:CamundaCloud/Worker/demo/src/main/resources/db.sqlite";
        String updateSQL = "UPDATE products SET quantity = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, productName);
            pstmt.executeUpdate();
            System.out.println("Updated quantity for product " + productName + " to " + newQuantity);
        }
    }

    public static void main(String[] args) {
        createDatabase();
    }

    private HttpResponse<String> get(String uri) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("X-Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .uri(URI.create(uri))
                .build();

        System.out.println("Fetching URI: " + uri);
        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Got status code: " + response.statusCode());
        System.out.println("=== Body:");
        System.out.println(response.body());
        System.out.println("=========");
        return response;
    }
}
