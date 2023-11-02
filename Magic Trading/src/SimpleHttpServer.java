import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHttpServer {

    public static void main(String[] args) throws IOException {
        int port = 10001; // Define the port for your server

        // Create a new HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create a context for the "/users" endpoint
        server.createContext("/users", new UsersHandler());

        // Start the server
        server.setExecutor(null); // Use default executor
        server.start();

        System.out.println("Server started on port " + port);
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello from the server!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
