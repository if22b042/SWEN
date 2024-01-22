import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
public class MyHttpServer {

    public static void startServer() throws IOException {
        int port = 10001    ; // Define the port for your server

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/users", (HttpHandler) new RegistrationHandler());
        server.createContext("/sessions", new LoginHandler());
        server.createContext("/packages", new PackageHandler());
        server.createContext("/transactions/packages", new openPackages());
        server.createContext("/cards", new SeeCards());
        server.createContext("/deck", new Decks());
        server.createContext("/battle", new Battle());
        server.createContext("/stats", new Stats());
        server.createContext("/scoreboard", new Scoreboard());
        server.createContext("/", new RootHandler());
        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Server started on port " + port);
    }
    static class Stats implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Stats been envoked");

            String username = GetUsername(exchange);
            String response = Main.GetStats(username);
            exchange.sendResponseHeaders(200, response.getBytes().length);

            // Write the response
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class Scoreboard implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<String> topPlayers = Main.Scoreboard();
            String response = String.join("\n", topPlayers);

            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        }


    static class Battle implements HttpHandler {


        @Override
        public void handle(HttpExchange exchange) throws IOException {

                String username = GetUsername(exchange);

            String waitingPlayer = BattleHandler.Waitingplayercheck();
                // Check if there is another player waiting
                if (waitingPlayer!="") {


                    // Proceed with the battle between 'username' and 'opponent'
                    BattleData playerData = BattleHandler.PrepareBattle(username);
                    BattleData opponentData = BattleHandler.PrepareBattle(waitingPlayer);

                    String battle = BattleHandler.Battle(playerData, opponentData);
                    System.out.println(battle);

                    try {
                        exchange.getResponseHeaders().add("Content-Type", "text/plain"); // Set Content-Type if needed
                        exchange.sendResponseHeaders(200, battle.getBytes(StandardCharsets.UTF_8).length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(battle.getBytes(StandardCharsets.UTF_8));
                            os.flush(); // Explicitly flush the stream
                        }
                    } catch (IOException e) {
                        e.printStackTrace(); // Log the exception
                        // Handle exception (e.g., send a 500 Internal Server Error response)
                    }

                } else {
                    // No opponent available, add player to the waiting list
                    BattleHandler.addWaitingPlayer(username);
                    // Send response to player to wait
                    String response = "Waiting for an opponent...";
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }


    }




    static class Decks implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String Username = GetUsername(exchange);

            // Check the request method
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Handle GET request, e.g., returning unconfigured deck
                String response = Main.DeckConfiguration(Username, "");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                // Write the response
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Handle PUT request with deck configuration
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);

                String requestBody = br.readLine();
                JSONArray DeckArray = new JSONArray(requestBody);
                String response = Main.DeckConfiguration(Username, DeckArray.toString());
                System.out.println(response);
                exchange.sendResponseHeaders(200, response.getBytes().length);

                // Write the response
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Handle unsupported request methods (e.g., POST, DELETE)
                String response = "Unsupported request method";
                exchange.sendResponseHeaders(405, response.getBytes().length);

                // Write the response
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    static class SeeCards implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            String Username=GetUsername(exchange);
            String ownedCards=Main.showCards(Username);

            exchange.sendResponseHeaders(200, ownedCards.getBytes().length);

            // Write the response
            OutputStream os = exchange.getResponseBody();
            os.write(ownedCards.getBytes());
            os.close();



        }
    }
    static class openPackages implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String Username=GetUsername(exchange);
            boolean success= Packages.openPackages(Username);
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");


            String response ="";

        if (success){
            response="Package Opened Sucessfully";
        }
        else{
            response="Failed";
        }
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();


        }
    }
    private static String GetUsername (HttpExchange exchange){
        //needs to be adjusted: How to get Username
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String Username="";
        if ("Bearer kienboec-mtcgToken".equals(authHeader)){
            Username = "kienboec";
        }
        if ("Bearer altenhof-mtcgToken".equals((authHeader))){
            Username="altenhof";
        }
        return Username;


    }
    static class PackageHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            boolean isAdmin=isValidAdminRequest(exchange);
            if (isAdmin) {

                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);

                    String requestBody = br.readLine();
                JSONArray packagesArray = new JSONArray(requestBody);
                    boolean success= Packages.PackageCreator(requestBody);

                    String response ="";
                    if (success){

                        response = "Packages added successfully";

                    }
                    else {
                        response = "Packages not added sucessfully";
                    }

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();




            } else {
                // Respond with unauthorized status
                String response = "Unauthorized access";
                exchange.sendResponseHeaders(401, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }
    }
    private static boolean isValidAdminRequest(HttpExchange exchange) {
        // Extract the Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        // Check if the header is valid for admin
        return "Bearer admin-mtcgToken".equals(authHeader);
    }

    static class LoginHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals(exchange.getRequestMethod())) {

                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);



                String requestBody = br.readLine();
                JSONObject json = new JSONObject(requestBody);

                String username = json.getString("Username");
                String password = json.getString("Password");

                boolean LoginSuccess = Main.Login(username, password);


                String response = LoginSuccess ? "User Logged in successfully!" : "Error registering user.";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream as = exchange.getResponseBody();
                as.write(response.getBytes());
                as.close();
            }
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Welcome to the Monster Trading Cards Game!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Add a method to handle user registration based on the JSON data

}
