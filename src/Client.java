package src;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static int myId = new Random().nextInt(1000);
    private static int serverPort = 5000;
    private static int myListenPort;
    private static boolean liderReconhecido = false;

    public static void main(String[] args) throws Exception {
        ServerSocket listenerSocket = new ServerSocket(0);
        myListenPort = listenerSocket.getLocalPort();


        Socket socket = new Socket("localhost", serverPort);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(myId);          //ID do processo
        out.writeInt(myListenPort);  //porta de escuta
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int nextPort = in.readInt(); //porta do próximo no anel
        socket.close();

        System.out.println("[Processo " + myId + "] Porta própria: " + myListenPort + ", próximo: " + nextPort);

        //inicia thread para escutar mensagens
        new Thread(() -> {
            try (ServerSocket listener = listenerSocket) {
                while (true) {
                    Socket s = listener.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String msg = reader.readLine();
                    handleMessage(msg, nextPort);
                    s.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //aguarda os outros clientes
        Thread.sleep(3000);

        if (args.length > 0 && args[0].equals("start")) {
            System.out.println("[Processo " + myId + "] Iniciando eleicao");
            sendToNext(nextPort, "ELEICAO:" + myId);
        }
    }

    private static void handleMessage(String msg, int nextPort) {
        if (msg.startsWith("ELEICAO:")) {
            String[] parts = msg.replace("ELEICAO:", "").split(",");
            Set<Integer> idSet = new HashSet<>();
            for (String s : parts) idSet.add(Integer.parseInt(s));

            System.out.println("[Processo " + myId + "] Recebeu ELEICAO com IDs: " + idSet);

            if (idSet.contains(myId)) {
                int elected = Collections.max(idSet);
                System.out.println("[Processo " + myId + "] ELEICAO CONCLUIDA Lider: " + elected);
                sendToNext(nextPort, "LIDER:" + elected);
            } else {
                idSet.add(myId);
                StringBuilder newMsg = new StringBuilder("ELEICAO:");
                for (int id : idSet) newMsg.append(id).append(",");
                sendToNext(nextPort, newMsg.toString());
            }

        } else if (msg.startsWith("LIDER:")) {
            int liderId = Integer.parseInt(msg.replace("LIDER:", ""));
            if (!liderReconhecido) {
                liderReconhecido = true;
                System.out.println("[Processo " + myId + "] Reconheceu o líder: " + liderId);
                if (liderId != myId) {
                    sendToNext(nextPort, msg); //propaga para frente
                }
            }
        }
    }

    private static void sendToNext(int port, String msg) {
        try {
            Socket s = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(msg);
            s.close();
        } catch (IOException e) {
            System.out.println("[Processo " + myId + "] Erro ao enviar para o próximo: " + e.getMessage());
        }
    }
}
