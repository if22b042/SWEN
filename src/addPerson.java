import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class addPerson {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "pwd123456")) {
            String createUserSQL = "CREATE USER user1 WITH PASSWORD 'user1pw';";

            String query = "INSERT INTO person (id, name, age, description) VALUES (?, ?, ?, ?)";
            int id = 2;
            String name = "John Doe";
            int age = 30;
            String description = "Some description";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, age);
            preparedStatement.setString(4, description);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new person was inserted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}