import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class RegistrationHandler implements HttpHandler {
    static PreparedStatement stmt = null;
    static final String BASE_URL = "http://localhost:10001";
    static final String USERS_ENDPOINT = BASE_URL + "/users";
    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "postgres";
    static Connection conn = null;
        @Override
        public  void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            // Check if the path is for a specific user
            if (pathParts.length == 3) {
                // Delegate to UserDataHandler for specific user requests
                UsersDataHandler(exchange, pathParts[2]);
            }

            if ("POST".equals(exchange.getRequestMethod())) {



                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);



                String requestBody = br.readLine();
                JSONObject json = new JSONObject(requestBody);

                String username = json.getString("Username");
                String password = json.getString("Password");


                boolean registrationSuccess = Registration(username, password);

                String response = registrationSuccess ? "User registered successfully!" : "Error registering user.";

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream as = exchange.getResponseBody();
                as.write(response.getBytes());
                as.close();
            }
        }



    private void UsersDataHandler(HttpExchange exchange, String username) throws IOException {
        String method = exchange.getRequestMethod();
        String authToken = exchange.getRequestHeaders().getFirst("Authorization");

        if (!isTokenValid(authToken, username)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String response;
        switch (method) {
            case "GET":
                response = GetUserData(username);
                sendResponse(exchange, 200, response);
                break;
            case "PUT":
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject data = new JSONObject(body);

                boolean success = ChangeUserData(data, username);


                response = success ? "User data updated successfully" : "Error updating user data";
                sendResponse(exchange, success ? 200 : 500, response);
                break;
            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    static String GetUserData(String username) {
        String userData = "User data not found.";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT username, bio, image, coins FROM users WHERE username = ?";


            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("username");
                String bio = rs.getString("bio");
                String image = rs.getString("image");
                int coins = rs.getInt("coins");

                userData = String.format("Name: %s\nBio: %s\nImage: %s\nCoins: %d\n\n\n", name, bio, image, coins);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace(); // Log the exception
            }
        }
        return userData;
    }
    static boolean ChangeUserData(JSONObject data, String username) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "UPDATE users SET  bio = ?, image = ? WHERE username = ?";
            stmt = conn.prepareStatement(sql);

            System.out.println(data);
            stmt.setString(1, data.optString("Bio", "defaultBio"));
            stmt.setString(2, data.optString("Image", "defaultImage"));
            stmt.setString(3, username);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) { // Catch SQLException
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private boolean isTokenValid(String authToken, String username) {

        return true; // Placeholder
    }
    static boolean Registration(String username, String password){
        try {
            System.out.println(username +"   "+ password);
            int coins=20;
            int [] stack= {};
            int elo=100;
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO \"users\" (username, password, coins, elo) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, coins);
            stmt.setInt(4, elo);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User registered successfully!");
            } else {
                System.out.println("Error registering user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


}
