package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 5000;
    private static final int TOTAL_CLIENTS = 5;
    private static final int BASE_PORT = 6000;

    private static class ClientInfo {
        Socket socket;
        String ip;
        int id;

        ClientInfo(Socket socket, String ip, int id) {
            this.socket = socket;
            this.ip = ip;
            this.id = id;
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor esperando conexoes...");

        List<ClientInfo> clients = new ArrayList<>();

        for (int i = 0; i < TOTAL_CLIENTS; i++)
        {
            Socket clientSocket = serverSocket.accept();
            String clientIP = clientSocket.getInetAddress().getHostAddress();

            clients.add(new ClientInfo(clientSocket, clientIP, i));
            System.out.println("Cliente " + (i+1) + " conectado: " + clientIP);
        }

        // Envia para cada cliente: seu ID, IP e porta do próximo
        for (int i = 0; i < TOTAL_CLIENTS; i++)
        {
            ClientInfo current = clients.get(i);
            ClientInfo next = clients.get((i + 1) % TOTAL_CLIENTS);
            System.out.println("Cliente " + (current.id+1) + ": proximo cliente= " + next.ip + ":" + (BASE_PORT + next.id));

            PrintWriter out = new PrintWriter(current.socket.getOutputStream(), true);
            out.println(current.id);
            out.println(next.ip);
            out.println(BASE_PORT + next.id);
        }

        // Envia token inicial ao cliente 1
        PrintWriter out = new PrintWriter(clients.get(0).socket.getOutputStream(), true);
        out.println("TOKEN");
        System.out.println("Token enviado ao cliente 1.");

        // Fecha conexões iniciais
        for (ClientInfo c : clients)
        {
            c.socket.close();
        }
        serverSocket.close();
    }
}
