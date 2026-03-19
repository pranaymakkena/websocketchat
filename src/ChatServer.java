import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class ChatServer extends WebSocketServer {

    private static final int DEFAULT_PORT = 8080;

    // Thread-safe collections
    private static final Set<WebSocket> clients =
            Collections.synchronizedSet(new HashSet<>());

    private static final Map<WebSocket, String> userNames =
            Collections.synchronizedMap(new HashMap<>());

    private static final Map<WebSocket, String> userColors =
            Collections.synchronizedMap(new HashMap<>());

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        sendMessage(conn, "{ \"type\": \"ask_name\" }");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String userName = userNames.get(conn);

        clients.remove(conn);
        userNames.remove(conn);
        userColors.remove(conn);

        // Only broadcast if user actually joined
        if (userName != null && !userName.isEmpty()) {
            String leaveMessage = "{ \"type\": \"leave\", \"name\": \"" + escape(userName) + "\" }";
            broadcast(leaveMessage);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("name:")) {
                String userName = message.substring(5).trim();
                userName = escape(userName);

                userNames.put(conn, userName);

                String userColor = getRandomColor();
                userColors.put(conn, userColor);

                String joinMessage = "{ \"type\": \"join\", \"name\": \"" + userName + "\" }";
                broadcast(joinMessage);

                sendMessage(conn, "{ \"type\": \"start_chat\" }");

            } else if (message.startsWith("message:")) {
                String userName = userNames.get(conn);

                if (userName == null) return; // ignore invalid users

                String formattedMessage = message.substring(8).trim();
                formattedMessage = escape(formattedMessage);

                String userColor = userColors.get(conn);

                String messageData = "{ \"type\": \"message\", " +
                        "\"name\": \"" + userName + "\", " +
                        "\"message\": \"" + formattedMessage + "\", " +
                        "\"color\": \"" + userColor + "\", " +
                        "\"time\": \"" + getFormattedTime() + "\" }";

                broadcast(messageData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully on port " + getPort());
    }

    private void sendMessage(WebSocket conn, String message) {
        if (conn != null && conn.isOpen()) {
            conn.send(message);
        }
    }

    private String getRandomColor() {
        String[] colors = {"#dcf8c6", "#e1f7ff", "#f0e1f7", "#e7f7e1", "#f7e1e1"};
        int randomIndex = (int) (Math.random() * colors.length);
        return colors[randomIndex];
    }

    private String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    // Basic JSON escape to prevent breaking messages
    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        String portEnv = System.getenv("PORT"); // Render dynamic port
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException ignored) {}
        }

        ChatServer server = new ChatServer(port);
        server.start();

        System.out.println("Server is running on port " + port);
    }
}