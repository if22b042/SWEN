import org.json.JSONArray;

import java.sql.*;

public class BattleHandler {

    static final String BASE_URL = "http://localhost:10001";
    static final String USERS_ENDPOINT = BASE_URL + "/users";
    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "postgres";
    static Connection conn = null;
    static PreparedStatement stmt = null;

    static String Battle (BattleData user1, BattleData user2){
        System.out.println("Less go");
        Connection conn = null;
        Statement stmt = null;


        try {
            // Your loop or critical code section

        StringBuilder response = new StringBuilder();
        int round = 0;
        String result = null;
        clearWaitingPlayers();
        int user1wins=0;
        int user2wins=0;

        int draws=0;
        while (round < 100){

            int rand1 = (int) Math.floor(Math.random() * (user1.activeDeck.length()-1)); // Random index from user1's deck
            int rand2 = (int) Math.floor(Math.random() * (user2.activeDeck.length()-1)); // Random index from user2's deck


            System.out.println("Till here "+round+"   "+user1.activeDeck.length()+"     "+ user2.activeDeck.length()+"  rand1: "+ rand1+ " rand2: "+ rand2+"   ");
            if (user2.activeDeck.length()<1){
                System.out.println("user1 won");
                response.append("\n The game has finished and ").append(user1.name).append(" Has won.\n The final score was: ").append(user1wins).append(" - ").append(user2wins);

                updateEloInDatabase(user1.name, user2.name);
                break;
            }
            else if (user1.activeDeck.length()<1){
                System.out.println("user2 won");
                response.append("\n The game has finished and ").append(user2.name).append(" Has won.\n The final score was:   ").append(user1wins).append(" - ").append(user2wins);

                updateEloInDatabase(user2.name, user1.name);
                break;
            }


            // Assuming activeDeck is a JSONArray, you would retrieve the card ID like so:
            String user1CardId = user1.activeDeck.getString(rand1);
            String user2CardId = user2.activeDeck.getString(rand2);
            System.out.println("user1: " +user1CardId+"  user2: "+ user2CardId+ "type: "+ user1.type[rand1]+ "   "+  user2.type[rand2]);
            if(user1.type[rand1] == 0 && user1.type[rand1] == user2.type[rand2]) { // Pure monster fight
                if (user1.damage[rand1] > user2.damage[rand2]){
                    response.append(user1.name).append(" has won the ").append(round).append(". round in a Pure monster fight.\n");
                    // Transfer card from user2 to user1

                    transferCard(user1, user2, rand1);
                    user1wins++;
                }
                else if (user1.damage[rand1] < user2.damage[rand2]){
                    response.append(user2.name).append(" has won the ").append(round).append(". round in a Pure monster fight.\n");
                    // Transfer card from user1 to user2
                    transferCard(user2, user1, rand1);
                    user2wins++;
                }
                else {
                    response.append("The ").append(round).append(". round ended in a draw.\n");
                    draws++;
                }
            }
            else  {//Pure spell fight or mixed
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

                    transferCard(user1, user2, rand1);
                    user1wins++;
                }
                else if (user1.damage[rand1] < user2.damage[rand2]){
                    response.append(user2.name).append(" has won the ").append(round).append(". round in a Pure Spell fight.\n");
                    // Transfer card from user1 to user2

                    transferCard(user2, user1, rand1);

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
        catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace to the console
        }
        return "Failed";
    }
    public static void updateEloInDatabase(String winnerUsername, String loserUsername) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            // Update winner's Elo
            stmt = conn.prepareStatement("UPDATE users SET elo = elo + 3 WHERE username = ?");
            stmt.setString(1, winnerUsername);
            stmt.executeUpdate();

            // Update loser's Elo
            stmt = conn.prepareStatement("UPDATE users SET elo = elo - 5 WHERE username = ?");
            stmt.setString(1, loserUsername);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(); // Log this exception
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



    public static void transferCard(BattleData fromUser, BattleData toUser, int cardIndex) {
        if (cardIndex < 0 || cardIndex >= fromUser.activeDeck.length()) {
            // Invalid card index
            return;
        }

        // Transfer card ID
        String cardId = fromUser.activeDeck.getString(cardIndex);
        toUser.activeDeck.put(cardId);

        // Transfer associated data
        toUser.element = appendToArray(toUser.element, fromUser.element[cardIndex]);
        toUser.type = appendToArray(toUser.type, fromUser.type[cardIndex]);
        toUser.monster = appendToArray(toUser.monster, fromUser.monster[cardIndex]);
        toUser.damage = appendToArray(toUser.damage, fromUser.damage[cardIndex]);

        // Remove card and associated data from fromUser
        removeFromJSONArray(fromUser.activeDeck, cardIndex);
        fromUser.element = removeFromArray(fromUser.element, cardIndex);
        fromUser.type = removeFromArray(fromUser.type, cardIndex);
        fromUser.monster = removeFromArray(fromUser.monster, cardIndex);
        fromUser.damage = removeFromArray(fromUser.damage, cardIndex);
    }

    private static int[] appendToArray(int[] array, int element) {
        int[] newArray = new int[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

    private static int[] removeFromArray(int[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return array;
        }
        int[] newArray = new int[array.length - 1];
        System.arraycopy(array, 0, newArray, 0, index);
        System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
        return newArray;
    }

    private static void removeFromJSONArray(JSONArray array, int index) {
        if (index < 0 || index >= array.length()) {
            return;
        }
        array.remove(index);
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




    public static String Waitingplayercheck() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;


        try {
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            System.out.println("Check if waiting player");
            String sql = "SELECT username FROM WaitingPlayers LIMIT 1"; // Adjust the SQL query as needed

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Waiting player found");
                return rs.getString("username");
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
            int elo=0;
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            stmt= conn.prepareStatement("SELECT elo FROM users WHERE username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                elo = rs.getInt("elo");
            }

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

            return new BattleData(username,elo ,element, type, monster, damage, deckArray);

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

}
