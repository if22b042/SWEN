import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.UUID;

public class Packages {
    static PreparedStatement stmt = null;
    static final String BASE_URL = "http://localhost:10001";
    static final String USERS_ENDPOINT = BASE_URL + "/users";
    static final String JDBC_URL = "jdbc:postgresql://localhost:5432/";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "postgres";
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
}
