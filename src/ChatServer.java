import java.io.IOException;
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
    private static Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private static Map<WebSocket, String> userNames = new HashMap<>();
    private static Map<WebSocket, String> userColors = new HashMap<>();

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

        String leaveMessage = "{ \"type\": \"leave\", \"name\": \"" + userName + "\" }";
        broadcast(leaveMessage);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("name:")) {
                String userName = message.substring(5).trim();
                userNames.put(conn, userName);
                String userColor = getRandomColor();
                userColors.put(conn, userColor);

                String joinMessage = "{ \"type\": \"join\", \"name\": \"" + userName + "\" }";
                broadcast(joinMessage);

                sendMessage(conn, "{ \"type\": \"start_chat\" }");

            } else if (message.startsWith("message:")) {
                String userName = userNames.get(conn);
                String formattedMessage = message.substring(8).trim();
                String userColor = userColors.get(conn);

                String messageData = "{ \"type\": \"message\", \"name\": \"" + userName + "\", \"message\": \"" + formattedMessage + "\", \"color\": \"" + userColor + "\", \"time\": \"" + getFormattedTime() + "\" }";
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

    private String getRandomColor() {
        String[] colors = {"#dcf8c6", "#e1f7ff", "#f0e1f7", "#e7f7e1", "#f7e1e1"};
        int randomIndex = (int) (Math.random() * colors.length);
        return colors[randomIndex];
    }

    private String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    @Override
    public void onStart() {
        System.out.println("Server started successfully on port " + getPort());
    }

    private void sendMessage(WebSocket conn, String message) {
        conn.send(message);
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("PORT"); // Get PORT from Render
        if (portEnv != null) {
            port = Integer.parseInt(portEnv);
        }

        ChatServer server = new ChatServer(port);
        server.start();
        System.out.println("Server is running on port " + port);
    }
}
