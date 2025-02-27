FROM openjdk:20

WORKDIR /app

# Copy compiled .class files (relative to root 'src/')
COPY out/ .

# Copy the lib folder (relative to root 'src/')
COPY lib/ lib/

# Expose WebSocket port
EXPOSE 8080

# Run the WebSocket server
CMD ["java", "-cp", ".:lib/*", "ChatServer"]
