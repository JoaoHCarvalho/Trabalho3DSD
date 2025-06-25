package src;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    private static int id;
    private static String nextIP;
    private static int nextPort;
    private static final int BASE_PORT = 6000;

    public static void main(String[] args) {
        try {
            Socket serverConnection = new Socket(args[0], 5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));

            id = Integer.parseInt(in.readLine());
            nextIP = in.readLine();
            nextPort = Integer.parseInt(in.readLine());

            String possibleToken = in.readLine();
            if ("TOKEN".equals(possibleToken))
            {
                enterCriticalSection();
                passToken();
            }
            int myPort = BASE_PORT + id;
            System.out.println("Cliente " + (id+1) + " ouvindo na porta " + myPort);

            ServerSocket listenSocket = new ServerSocket(myPort);

            // Thread para escutar o token
            new Thread(() -> {
                while (true)
                {
                    try (Socket incoming = listenSocket.accept()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
                        String msg = reader.readLine();
                        if ("TOKEN".equals(msg))
                        {
                            enterCriticalSection();
                            passToken();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor " + args[0]);
            e.printStackTrace();
        }
    }

    private static void enterCriticalSection() {
        System.out.println("Cliente " + (id+1) + " entrou na seção crítica.");
        try {
            System.out.println("Abacate");
            Thread.sleep(new Random().nextInt(3000) + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Cliente " + (id+1) + " saiu da seção crítica.");
    }

    private static void passToken() {
        try (Socket next = new Socket(nextIP, nextPort)) {
            PrintWriter writer = new PrintWriter(next.getOutputStream(), true);
            writer.println("TOKEN");
            System.out.println("Cliente " + (id+1) + " passou o token para o próximo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
