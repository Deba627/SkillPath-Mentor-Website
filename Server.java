import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final Map<String, Integer> progressData = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Home route
        server.createContext("/", new HomeHandler());

        // Progress Tracker routes
        server.createContext("/progress/status", new ProgressStatusHandler());
        server.createContext("/progress/update", new ProgressUpdateHandler());

        server.setExecutor(null); // Default executor
        System.out.println("🚀 Server started at http://localhost:8080/");
        server.start();
    }

    // Home Page Handler
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Welcome to SkillPath Mentor Backend!";
            sendResponse(exchange, response);
        }
    }

    // Progress Tracker Status Handler (GET)
    static class ProgressStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, "Only GET requests allowed", 405);
                return;
            }

            StringBuilder response = new StringBuilder("{");
            for (Map.Entry<String, Integer> entry : progressData.entrySet()) {
                response.append("\"").append(entry.getKey()).append("\":").append(entry.getValue()).append(",");
            }
            if (response.length() > 1) response.setLength(response.length() - 1); // Remove last comma
            response.append("}");

            sendResponse(exchange, response.toString());
        }
    }

    // Progress Tracker Update Handler (POST)
    static class ProgressUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, "Only POST requests allowed", 405);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.contains("user=") || !query.contains("percentage=")) {
                sendResponse(exchange, "Invalid request parameters", 400);
                return;
            }

            try {
                Map<String, String> params = parseQuery(query);
                String user = URLDecoder.decode(params.get("user"), StandardCharsets.UTF_8);
                int percentage = Integer.parseInt(params.get("percentage"));

                if (percentage < 0 || percentage > 100) {
                    sendResponse(exchange, "Invalid percentage value (0-100 allowed)", 400);
                    return;
                }

                progressData.put(user, percentage);
                sendResponse(exchange, "✅ Progress updated for " + user);
            } catch (Exception e) {
                sendResponse(exchange, "Error processing request: " + e.getMessage(), 400);
            }
        }
    }

    // Utility method to send responses with CORS headers
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // Method to parse query parameters
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    // Add CORS headers to allow frontend communication
    private static void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
