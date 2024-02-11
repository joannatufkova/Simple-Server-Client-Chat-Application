# Simple-Server-Client-Chat-Application

## Overview
This Android application is designed to facilitate real-time messaging and the transfer of large files (6GB and above) using a server-client architecture. Employing the command pattern, it ensures a clean separation of commands for sending and receiving data, while a dedicated service maintains a persistent connection.

## Features
- Real-time text messaging
- Support for transferring large files (>=6GB)
- Persistent service-driven connection between server and client
- User-friendly interface for connection setup and chat management
- Efficient background operations for network communication
- Scalable command pattern implementation for communication

## Architecture
The application follows a client-server model:

- **Client**: Handles initiating connections, sending messages/files, and displaying incoming data.
- **Server**: Manages client connections, receives messages/files, and distributes data accordingly.

Android services and threads ensure a smooth user experience by handling network operations in the background, allowing for a responsive UI.

## Components
- **Adapters**: `MessageAdapter` for displaying messages.
- **Services**: `FileService` for file operations over sockets.
- **Activities**: `MainActivity` for service initiation/connection, `ChatActivity` for messaging and file transfers, `FileOpenActivity` for file viewing
- **Threads**: Background threads for handling incoming server/client commands
- **Commands**: `SendMessageCommand`, `SendFileCommand`, `ReceiveMessageCommand`, `ReceiveFileCommand` for data transmission.
- **Streams**: `DataOutputStream` and `DataInputStream` for byte stream management over sockets.
- **Handlers/Listeners**: `ProgressListener` for file transfer progress, and `Handler` for main thread tasks from background operations.
