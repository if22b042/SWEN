import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.net.HttpURLConnection;

import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;

class BattleData {
    String name;
    int[] element;
    int[] type;
    int[] monster;
    int[] damage;
    int elo;
    JSONArray activeDeck;

    public BattleData(String name, int elo, int[] element, int[] type, int[] monster, int[] damage, JSONArray activeDeck) {
        this.name= name;
        this.elo=elo;
        this.element = element;
        this.type = type;
        this.monster = monster;
        this.damage = damage;
        this.activeDeck = activeDeck;
    }

    // Add getters or other methods as needed
}
public class Main {


    static final String BASE_URL = "http://localhost:10001";
    static final String USERS_ENDPOINT = BASE_URL + "/users";
    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "postgres";
    static Connection conn = null;
    static PreparedStatement stmt = null;
    static void AcessProcedure(){
        System.out.println("Welcome to Monster Trading Cards");
        Scanner myObj = new Scanner(System.in);
        boolean success=false;
        while (!success) {
            System.out.print("Press 1 if you are already registered and press 2 if you are new");
            String decision = myObj.nextLine();
            System.out.println(decision);

            if (decision.equals("1")){

                if (success){
                    break;
                }
            }
            if (decision.equals("2")){
                success=RegistrationHandler.Registration("a","b");
                if (success){
                    break;
                }
            }


        }
        System.out.println("Access was Successful");
    }
    public static String GetStats(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT coins, elo FROM users WHERE username= ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            int elo=0;
            int coins=0;

            System.out.println("Get Stats");
            if (rs.next()) {
                elo = rs.getInt("elo");
                coins= rs.getInt("coins");
            }
            return "Username: "+ username + "  elo: "+ elo+"  coins: "+ coins;


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
        return "failure";
    }
    public static List<String> Scoreboard() {
        List<String> topPlayers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT username, elo FROM users ORDER BY elo DESC LIMIT 10";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int elo = rs.getInt("elo");
                topPlayers.add(username + ": " + elo);
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
        return topPlayers;
    }
    static boolean Login(String username, String password){
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {


                conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                String sql = "SELECT * FROM \"users\" WHERE username = ? AND password = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password);

                rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Login successful!");
                    return true;
                } else {
                    System.out.println("Invalid username or password.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }




    static String DeckConfiguration(String username, String jsonData) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder response = new StringBuilder();

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);


            if (jsonData.isEmpty()) {
                // jsonData is empty, fetch and return the user's active deck
                String fetchDeckSql = "SELECT ActiveDeck FROM users WHERE username = ?";
                stmt = conn.prepareStatement(fetchDeckSql);
                stmt.setString(1, username);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String activeDeck = rs.getString("ActiveDeck");
                    if (activeDeck != null && !activeDeck.isEmpty()) {
                        response.append(ShowActiveDeck(username));
                    } else {
                        // Active deck is empty, return all cards
                        response.append(showCards(username));
                    }
                }
            }else {
                // jsonData is not empty, update the active deck if it meets the conditions
                JSONArray deckArray = new JSONArray(jsonData);

                if (deckArray.length() == 4 && validateDeck(deckArray, username, conn)) {
                    String updateDeckSql = "UPDATE users SET ActiveDeck = ? WHERE username = ?";
                    stmt = conn.prepareStatement(updateDeckSql);
                    stmt.setString(1, jsonData);
                    stmt.setString(2, username);
                    stmt.executeUpdate();

                    // Run the ShowActiveDeck function here after the active deck is changed
                    String deck = ShowActiveDeck(username);
                    response.append(deck);
                } else {
                    response.append("Invalid deck configuration. Please make sure you have exactly 4 cards in your stack and they are valid.");
                }
            }

        } catch (SQLException | JSONException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println(response.toString());
        return response.toString();
    }
    private static boolean validateDeck(JSONArray deckArray, String username, Connection conn) throws SQLException {
        if (deckArray.length() != 4) {
            return false;
        }

        String fetchUserStackSql = "SELECT Stack FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(fetchUserStackSql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String userStack = rs.getString("Stack");

            // Check if each card ID in the deckArray is a substring within the user's stack string
            for (int i = 0; i < deckArray.length(); i++) {
                String cardId = deckArray.getString(i);

                // Check if the cardId is a substring within the user's stack
                if (!userStack.contains(cardId)) {
                    return false;
                }
            }
        } else {
            // Handle the case where the user does not exist or has no stack
            return false;
        }

        return true;
    }



    static String ShowActiveDeck(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            // Query the database to retrieve the active deck for the user
            String fetchActiveDeckSql = "SELECT ActiveDeck FROM users WHERE username = ?";
            stmt = conn.prepareStatement(fetchActiveDeckSql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String activeDeck = rs.getString("ActiveDeck");
                if (activeDeck != null && !activeDeck.isEmpty()) {
                    // Parse the active deck as a JSON array
                    JSONArray deckArray = new JSONArray(activeDeck);

                    // Create a list to store card information
                    List<String> cardInfoList = new ArrayList<>();

                    // Query the database for card information based on card IDs in the active deck
                    String fetchCardInfoSql = "SELECT * FROM cards WHERE id = ?";
                    stmt = conn.prepareStatement(fetchCardInfoSql);
                    //System.out.println(deckArray);

                    for (int i = 0; i < deckArray.length(); i++) {
                        String cardId = deckArray.getString(i);
                        stmt.setString(1, cardId);
                        rs = stmt.executeQuery();

                        if (rs.next()) {
                            String cardInfo = "Card ID: " + rs.getString("id") +
                                    ", Name: " + rs.getString("name") +
                                    ", Damage: " + rs.getString("damage"); // Adjust as needed

                            cardInfoList.add(cardInfo);
                        }
                    }

                    // Join the card information into a single string and return it
                    return String.join("\n\n\n\n", cardInfoList);
                } else {
                    // Handle the case where the active deck is empty
                    System.out.println("Active Deck for user " + username + " is empty.");
                    return "";
                }
            } else {
                // Handle the case where the user does not exist or has no active deck
                System.out.println("User " + username + " not found or has no active deck.");
                return "";
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
            // Handle any database-related errors or JSON parsing errors here
            return "Error: " + e.getMessage();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static String showCards(String username){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        StringBuilder cardDetails = new StringBuilder();

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            // Step 1: Fetch the stack from the users table
            String sql = "SELECT Stack FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("User not found or stack is empty.");
                return "false1";
            }

            String stack = rs.getString("Stack");
            if (stack == null || stack.trim().isEmpty()) {
                System.out.println("User's stack is empty.");
                return "false2";
            }

            // Step 2: Parse the stack to get card IDs
            List<String> cardIds = Arrays.asList(stack.split(","));

            // Step 3: Fetch and display each card's details
            sql = "SELECT * FROM cards WHERE id = ?";
            stmt = conn.prepareStatement(sql);

            for (String cardId : cardIds) {
                stmt.setString(1, cardId.trim());
                rs = stmt.executeQuery();

                while (rs.next()) {
                    // Format the output for each card and append to StringBuilder
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    double damage = rs.getDouble("damage");
                    cardDetails.append("ID: ").append(id).append(", Name: ").append(name).append(", Damage: ").append(damage).append("\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage(); // Return error message
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return cardDetails.toString(); // Return the card details
    }










    static void Menu(){
        System.out.println("Do you want to start a Battle?(1), Do you want to   ");
        Scanner myObj = new Scanner(System.in);
        String decision = myObj.nextLine();
        if (decision=="1"){
            Battle();
        }

    }
    static void Battle(){
         
    }
    /*static void GetRequest() {
        try {
            URL url = new URL(USERS_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                System.out.println("Response from server:");
                System.out.println(content.toString());
            } else {
                System.out.println("Error: Unable to connect to the server. Response code: " + responseCode);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/



    public static void main(String[] args) {


        try {
            System.out.println("hii");
            MyHttpServer.startServer();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //GetRequest();
        AcessProcedure();
        Menu();





    }
}
