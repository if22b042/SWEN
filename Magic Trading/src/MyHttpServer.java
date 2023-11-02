import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MyHttpServer {

    public static void startServer() throws IOException {
        int port = 10001    ; // Define the port for your server

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/users", new UsersHandler());
        server.createContext("/", new RootHandler());
        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Server started on port " + port);
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals(exchange.getRequestMethod())) {

                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String requestBody = br.readLine();

                String[] parts = requestBody.split("&");
                String username = parts[0].split("=")[1];
                String password = parts[1].split("=")[1];

                boolean registrationSuccess = Main.Registration(username, password);

                String response = registrationSuccess ? "User registered successfully!" : "Error registering user.";

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
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
