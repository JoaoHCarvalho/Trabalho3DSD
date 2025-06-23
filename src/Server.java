package src;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static final int PORT = 5000;
    public static final int MAX_CLIENTS = 5;

    private static final List<Integer> clientIds = new ArrayList<>();
    private static final List<Integer> listeningPorts = new ArrayList<>();
    private static final List<Socket> clientSockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println();
        System.out.println("Servidor iniciado na porta " + PORT + ". Aguardando " + MAX_CLIENTS + " clientes...");

        while (clientIds.size() < MAX_CLIENTS) {
            Socket client = serverSocket.accept();
            DataInputStream in = new DataInputStream(client.getInputStream());
            int clientId = in.readInt();            //recebe ID
            int clientListenPort = in.readInt();    //recebe porta de escuta

            clientIds.add(clientId);
            listeningPorts.add(clientListenPort);
            clientSockets.add(client);

            System.out.println("Cliente " + clientId + " conectado na porta " + clientListenPort + " (" + clientIds.size() + "/5)");
        }

        System.out.println();
        System.out.println("Anel formado:");
        for (int i = 0; i < clientIds.size(); i++) {
            int atual = clientIds.get(i);
            int proximo = clientIds.get((i + 1) % clientIds.size());
            System.out.println("Processo " + atual + " -> " + proximo);
        }

        System.out.println();
        System.out.println("Enviando portas dos próximos processos...");
        for (int i = 0; i < clientSockets.size(); i++) {
            int nextPort = listeningPorts.get((i + 1) % clientSockets.size());
            DataOutputStream out = new DataOutputStream(clientSockets.get(i).getOutputStream());
            out.writeInt(nextPort);
            clientSockets.get(i).close(); //encerra conexao com o cliente
        }

        serverSocket.close();
        System.out.println();
        System.out.println("Servidor finalizado. A eleição pode ser iniciada por qualquer cliente.");
    }
}
