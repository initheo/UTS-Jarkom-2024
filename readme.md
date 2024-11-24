# Java Chat Application

This is a simple chat application implemented using Java Socket Programming. The application consists of a server that can handle multiple clients and a GUI client that allows users to send and receive messages.

## Features

- Multi-client support using multithreading
- Simple GUI interface for clients
- Real-time message broadcasting
- Stable connection handling

## Requirements

- Java Development Kit (JDK) 8 or higher
- Java Swing library (included in JDK)

## How to Run

### Running the Server

1. Compile the server:
```bash
javac ChatServer.java
```

2. Run the server:
```bash
java ChatServer
```

The server will start and listen for connections on port 12345.

### Running the Client

1. Compile the client:
```bash
javac ChatClient.java
```

2. Run the client:
```bash
java ChatClient
```

You can run multiple instances of the client to simulate multiple users.

## Usage

1. Start the server first
2. Launch one or more client instances
3. Enter a server IP address, port number and a username in the client window and click "Connect"
4. If the connection is successful, the client window will display a list of connected users
6. Type messages in the text field at the bottom of the client window
7. Press Enter or click "Send" to send messages
8. Messages will be broadcast to all connected clients

## Technical Details

- Server uses `ServerSocket` to accept client connections
- Each client connection is handled in a separate thread
- Messages are broadcast to all connected clients
- GUI is implemented using Java Swing
- Connection errors are handled gracefully

## Notes

- By default, the client connects to `localhost`. To connect to a different server, modify the `serverIP` variable in the first pop up window
- The default port is 12345. This can be modified in both server code and client first pop up window
  
