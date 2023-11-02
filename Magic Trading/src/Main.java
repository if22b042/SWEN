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
import java.util.Scanner;
import java.net.HttpURLConnection;

import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
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


    static boolean Registration(String username, String password){
        try {
            System.out.println(username +"   "+ password);
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO \"users\" (username, password) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
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
