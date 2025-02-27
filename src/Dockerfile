FROM openjdk:20

WORKDIR /app

# Copy compiled .class files from the root 'out' folder
COPY out/ .

# Copy the 'lib' folder from inside 'src'
COPY src/lib/ lib/

# Expose WebSocket port
EXPOSE 8080

# Run the WebSocket server
CMD ["java", "-cp", ".:lib/*", "ChatServer"]
