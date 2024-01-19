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
    JSONArray activeDeck;

    public BattleData(String name, int[] element, int[] type, int[] monster, int[] damage, JSONArray activeDeck) {
        this.name= name;
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
                success=Registration("a","b");
                if (success){
                    break;
                }
            }


        }
        System.out.println("Access was Successful");
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


    static String Battle (BattleData user1, BattleData user2){
        Connection conn = null;
        Statement stmt = null;



        StringBuilder response = new StringBuilder();
        int round = 0;
        String result = null;
        clearWaitingPlayers();
        int user1wins=0;
        int user2wins=0;

        int draws=0;
        while (round < 100){
            if (user2.activeDeck.length()<1){
                System.out.println("user1 won");
                response.append("\n The game has finished and ").append(user1.name).append(" Has won.\n The final score was: ").append(user1wins).append(" - ").append(user2wins);
                break;
            }
            else if (user1.activeDeck.length()<1){
                System.out.println("user2 won");
                response.append("\n The game has finished and ").append(user2.name).append(" Has won.\n The final score was:   ").append(user1wins).append(" - ").append(user2wins);
                break;
            }

            int rand1 = (int) Math.floor(Math.random() * user1.activeDeck.length()); // Random index from user1's deck
            int rand2 = (int) Math.floor(Math.random() * user2.activeDeck.length()); // Random index from user2's deck


            // Assuming activeDeck is a JSONArray, you would retrieve the card ID like so:
            String user1CardId = user1.activeDeck.getString(rand1);
            String user2CardId = user2.activeDeck.getString(rand2);
            System.out.println("Till here 1 "+user1.activeDeck.length()+"     "+ user2.activeDeck.length());
            if(user1.type[rand1] == 0 && user1.type[rand1] == user2.type[rand2]) { // Pure monster fight
                if (user1.damage[rand1] > user2.damage[rand2]){
                    response.append(user1.name).append(" has won the ").append(round).append(". round in a Pure monster fight.\n");
                    // Transfer card from user2 to user1
                    user1.activeDeck.put(user2CardId);
                    user2.activeDeck.remove(rand2);
                    user1wins++;
                }
                else if (user1.damage[rand1] < user2.damage[rand2]){
                    response.append(user2.name).append(" has won the ").append(round).append(". round in a Pure monster fight.\n");
                    // Transfer card from user1 to user2
                    user2.activeDeck.put(user1CardId);
                    user1.activeDeck.remove(rand1);
                    user2wins++;
                }
                else {
                    response.append("The ").append(round).append(". round ended in a draw.\n");
                    draws++;
                }
            }
            else if(user1.type[rand1]==1&&user1.type[rand1]==user2.type[rand2]) {//Pure spell fight
                    if (user1.element[rand1]==1 && user2.element[rand2]==2){//User 1 Water vs User 2 Fire
                        user1.damage[rand1]= user1.damage[rand1]*2;
                        user2.damage[rand2]= user2.damage[rand2]/2;
                    }
                    else if (user1.element[rand1]==2 && user2.element[rand2]==1){//User 1 Fire vs User 2 Water
                        user1.damage[rand1]= user1.damage[rand1]/2;
                        user2.damage[rand2]= user2.damage[rand2]*2;
                    }
                    else if (user1.element[rand1]==0 && user2.element[rand2]==1){//User 1 normal vs User 2 Water
                        user1.damage[rand1]= user1.damage[rand1]*2;
                        user2.damage[rand2]= user2.damage[rand2]/2;
                    }
                    else if (user1.element[rand1]==1 && user2.element[rand2]==0){//User 1 Water vs User 2 normal
                        user1.damage[rand1]= user1.damage[rand1]/2;
                        user2.damage[rand2]= user2.damage[rand2]*2;
                    }
                    else if (user1.element[rand1]==2 && user2.element[rand2]==0){//User 1 Fire vs User 2 normal
                        user1.damage[rand1]= user1.damage[rand1]*2;
                        user2.damage[rand2]= user2.damage[rand2]/2;
                    }
                    else if (user1.element[rand1]==0 && user2.element[rand2]==2){//User 1 normal vs User 2 Fire
                        user1.damage[rand1]= user1.damage[rand1]/2;
                        user2.damage[rand2]= user2.damage[rand2]*2;
                    }


                    //Normal battle logic who was highest damage


                if (user1.damage[rand1] > user2.damage[rand2]){
                    response.append(user1.name).append(" has won the ").append(round).append(". round in a Pure Spell fight.\n");
                    // Transfer card from user2 to user1
                    user1.activeDeck.put(user2CardId);
                    user2.activeDeck.remove(rand2);
                    user1wins++;
                }
                else if (user1.damage[rand1] < user2.damage[rand2]){
                    response.append(user2.name).append(" has won the ").append(round).append(". round in a Pure Spell fight.\n");
                    // Transfer card from user1 to user2
                    user2.activeDeck.put(user1CardId);
                    user1.activeDeck.remove(rand1);
                    user2wins++;
                }
                else {
                    response.append("The ").append(round).append(". round ended in a draw.\n");
                    draws++;
                }
            }
            else {//Mixed fight if not anything more info is found then should be put together with just spell fight
                if (user1.element[rand1]==1 && user2.element[rand2]==2){//User 1 Water vs User 2 Fire
                    user1.damage[rand1]= user1.damage[rand1]*2;
                    user2.damage[rand2]= user2.damage[rand2]/2;
                }
                else if (user1.element[rand1]==2 && user2.element[rand2]==1){//User 1 Fire vs User 2 Water
                    user1.damage[rand1]= user1.damage[rand1]/2;
                    user2.damage[rand2]= user2.damage[rand2]*2;
                }
                else if (user1.element[rand1]==0 && user2.element[rand2]==1){//User 1 normal vs User 2 Water
                    user1.damage[rand1]= user1.damage[rand1]*2;
                    user2.damage[rand2]= user2.damage[rand2]/2;
                }
                else if (user1.element[rand1]==1 && user2.element[rand2]==0){//User 1 Water vs User 2 normal
                    user1.damage[rand1]= user1.damage[rand1]/2;
                    user2.damage[rand2]= user2.damage[rand2]*2;
                }
                else if (user1.element[rand1]==2 && user2.element[rand2]==0){//User 1 Fire vs User 2 normal
                    user1.damage[rand1]= user1.damage[rand1]*2;
                    user2.damage[rand2]= user2.damage[rand2]/2;
                }
                else if (user1.element[rand1]==0 && user2.element[rand2]==2){//User 1 normal vs User 2 Fire
                    user1.damage[rand1]= user1.damage[rand1]/2;
                    user2.damage[rand2]= user2.damage[rand2]*2;
                }


                //Normal battle logic who was highest damage


                if (user1.damage[rand1] > user2.damage[rand2]){
                    response.append(user1.name).append(" has won the ").append(round).append(". round in a Pure Spell fight.\n");
                    // Transfer card from user2 to user1
                    user1.activeDeck.put(user2CardId);
                    user2.activeDeck.remove(rand2);
                    user1wins++;
                }
                else if (user1.damage[rand1] < user2.damage[rand2]){
                    response.append(user2.name).append(" has won the ").append(round).append(". round in a Pure Spell fight.\n");
                    // Transfer card from user1 to user2
                    user2.activeDeck.put(user1CardId);
                    user1.activeDeck.remove(rand1);
                    user2wins++;
                }
                else {
                    response.append("The ").append(round).append(". round ended in a draw.\n");
                    draws++;
                }
            }

            round++;
        }
        System.out.println(response.toString());

        // Return the battle results
        return response.toString();
    }


    static void clearWaitingPlayers() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            stmt = conn.createStatement();
            String sql = "DELETE FROM WaitingPlayers"; // SQL to clear the table

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace(); // Proper error handling should be implemented
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    static void addWaitingPlayer(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO WaitingPlayers (username) VALUES (?)";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }




    static String Waitingplayercheck() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT username FROM WaitingPlayers LIMIT 1"; // Adjust the SQL query as needed

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username"); // Assumes the column name is 'username'
            } else {
                return ""; // No waiting players
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ""; // Return empty string in case of an error
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Main method or other methods...


    static BattleData PrepareBattle(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String fetchDeckSql = "SELECT ActiveDeck FROM users WHERE username = ?";
            stmt = conn.prepareStatement(fetchDeckSql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return null; // Or handle the case where the user is not found
            }

            String activeDeck = rs.getString("ActiveDeck");
            JSONArray deckArray = new JSONArray(activeDeck);

            int[] element = new int[deckArray.length()];
            int[] type = new int[deckArray.length()];
            int[] monster = new int[deckArray.length()];
            int[] damage = new int[deckArray.length()];

            String fetchCardInfoSql = "SELECT * FROM cards WHERE id = ?";
            stmt = conn.prepareStatement(fetchCardInfoSql);

            for (int i = 0; i < deckArray.length(); i++) {
                String cardId = deckArray.getString(i);
                stmt.setString(1, cardId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String cardName = rs.getString("name");
                    element[i] = Analyzeelement(cardName);
                    type[i] = Analyzetype(cardName);
                    damage[i] = rs.getInt("damage");

                    if (type[i] == 0) {
                        monster[i] = Analyzemonster(cardName);
                    }
                }
            }

            return new BattleData(username,element, type, monster, damage, deckArray);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static int Analyzetype (String input){
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (word.contains("Spell")) {
                return 1;
            }
        }
        return 0;
    }

    public static int Analyzeelement(String input) {
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (word.contains("Water")) {
                return 1;
            } else if (word.contains("Fire")) {
                return 2;
            }
        }
        return 0;
    }
    public static int Analyzemonster(String input){
        String[] words = input.split("\\s+");
        for (String word : words) {
            if (word.contains("Goblin")) {
                return 1;
            } else if (word.contains("Dragon")) {
                return 2;
            } else if (word.contains("Wizard")) {
                return 3;
            } else if (word.contains("Knight")) {
                return 4;
            } else if (word.contains("Kraken")) {
                return 5;
            } else if (word.contains("Elve")) {
                return 6;
            }
        }
        return 0;
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
                    return String.join("\n", cardInfoList);
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



    static boolean openPackages(String Username){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            //Check the user's coin balance
            String checkCoinsSql = "SELECT coins FROM users WHERE username = ?";
            stmt = conn.prepareStatement(checkCoinsSql);
            stmt.setString(1, Username);
            rs = stmt.executeQuery();
            if (!rs.next() || rs.getInt("coins") < 5) {
                System.out.println("not enough coins");
                return false; // Not enough coins or user not found

            }


            //Select a random package
            String randomPackageSql = "SELECT * FROM packages LIMIT 1";
            stmt = conn.prepareStatement(randomPackageSql);
            rs = stmt.executeQuery();
            if (!rs.next()) {

                return false; // No package found
            }
            //Deduct 5 coins
            String deductCoinsSql = "UPDATE users SET coins = coins - 5 WHERE username = ?";
            stmt = conn.prepareStatement(deductCoinsSql);
            stmt.setString(1, Username);
            stmt.executeUpdate();

            // Extract card IDs from the package
            String[] cardIds = new String[5];
            String selectedPackageId = rs.getString("id");
            for (int i = 0; i < 5; i++) {
                cardIds[i] = rs.getString("card" + (i + 1));
            }

            // Step 2: Determine top 4 damage cards from these 5 cards
            String topCardsSql = "SELECT id FROM cards WHERE id IN (?, ?, ?, ?, ?) ORDER BY damage DESC LIMIT 5";//Doesn´t need to exist but earlier Misunderstanding
            stmt = conn.prepareStatement(topCardsSql);
            for (int i = 0; i < 5; i++) {
                stmt.setString(i + 1, cardIds[i]);
            }
            rs = stmt.executeQuery();

            // Step 3: Add these cards to the user's stack
            // Assuming there's a table 'user_cards' to hold the user's cards
            String addToStackSql = "UPDATE users SET Stack = COALESCE(Stack || ',', '') || ? WHERE username = ?";
            stmt = conn.prepareStatement(addToStackSql);
            while (rs.next()) {
                String cardId = rs.getString("id");
                stmt.setString(1, cardId);
                stmt.setString(2, Username);
                stmt.executeUpdate();
            }
            String deletePackageSql = "DELETE FROM packages WHERE id = ?";
            stmt = conn.prepareStatement(deletePackageSql);
            stmt.setString(1, selectedPackageId);
            stmt.executeUpdate();


            return true; // Operation successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Operation failed
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
    static boolean PackageCreator(String jsonData) {
        JSONArray packagesArray = new JSONArray(jsonData);
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false); // Start transaction

            // Prepare statements
            pstmt = conn.prepareStatement("INSERT INTO cards (id, name, damage, number) VALUES (?, ?, ?, ?)");
            pstmt2 = conn.prepareStatement("INSERT INTO packages(id, card1, card2, card3, card4, card5) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
;
            // Assuming package ID is auto-generated, set it here
            String packageId = UUID.randomUUID().toString();
            pstmt2.setString(1, packageId);

            int cardNumber = 1; // Initialize card number counter

            for (int i = 0; i < packagesArray.length(); i++) {
                JSONObject packageObj = packagesArray.getJSONObject(i);
                String cardId = packageObj.getString("Id");
                pstmt2.setString(i + 2, cardId); // Set card references in package
                pstmt.setString(1, cardId);
                pstmt.setString(2, packageObj.getString("Name"));
                pstmt.setDouble(3, packageObj.getDouble("Damage"));
                pstmt.setInt(4, cardNumber++); // Set card number and increment
                pstmt.executeUpdate();
            }

            pstmt2.executeUpdate();
            conn.commit(); // Commit the transaction

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback in case of an error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Close resources
            try {
                if (pstmt != null) pstmt.close();
                if (pstmt2 != null) pstmt2.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    static boolean Registration(String username, String password){
        try {
            System.out.println(username +"   "+ password);
            int coins=20;
            int [] stack= {};
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO \"users\" (username, password, coins) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, coins);
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
