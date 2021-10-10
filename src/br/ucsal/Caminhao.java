package br.ucsal;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Ao iniciar informa que está livre
 * - "LIVRE" [ID_CAMINHAO]
 * <p>
 * Escuta por chamados da central
 * - "COLETAR" [ID_CONTAINER]
 * - gerar tempo para chegar ao container (5 a 20 segs)
 * <p>
 * Ao chegar no container
 * - "CHEGUEI_CONTAINER" [ID_CAMINHAO]
 * - gerar tempo para voltar a central (5 a 20 segs)
 * <p>
 * Ao chegar comunicar central
 * - "COLETA_FINALIZADA" [ID_CAMINHAO]
 */
public class Caminhao {
    private final int id;
    private STATUS_CAMINHAO status;
    private int idContainer;
    private Socket centralSocket;
    private Socket containerSocket;
    private final Scanner scanner = new Scanner(System.in);
    private ServerSocket socket;
    private List<Dispositivo> availableClientList;
    private List<Dispositivo> containerList = new ArrayList<Dispositivo>();
    private List<Dispositivo> centralList = new ArrayList<Dispositivo>();

    public Caminhao(int id) {
        this.id = id;
    }

    public static void main(String[] args) {
        int id;
        if (args.length > 0) {
            id = Integer.parseInt(args[0]);
        } else {
            id = 3;
        }

        Caminhao caminhao = new Caminhao(id);
        caminhao.start();
    }

    public void start() {
        // Conecta a central

        try {
            this.socket = new ServerSocket(64200, 1);
            System.out.println("Caminhão executando em: " + this.getIpAddress() + ":" + this.getPort());
            System.out.println("Dispositivos disponiveis");
            availableClientList = Dispositivo.listFromFile(readFileContent());
            for (int i = 0; i < availableClientList.size(); i++) {
                Dispositivo d = availableClientList.get(i);
                if (d.getType() == DispositivoType.CENTRAL) {
                    centralSocket = new Socket(d.getIpAddress(), d.getPort());
                    centralList.add(d);
                } else if (d.getType() == DispositivoType.CONTAINER) {
                    containerList.add(d);
                }
                System.out.println(i + " - " + d);
            }

//            System.out.println("Informe o endereço e porta da central");
//            String centralAddress = scanner.next();
//            String centralIp = centralAddress.split(":")[0];
//            int centralPort = Integer.parseInt(centralAddress.split(":")[1]);
//            centralSocket = new Socket(centralIp, centralPort);

            if (centralSocket == null) {
                System.out.println("Aguardando conexão da central");
                centralSocket = this.socket.accept();
            }



            sendIsFreeToCentral();
        } catch (Exception e) {
            System.out.println("Erro ao conectar com a central");
            e.printStackTrace();
        }
    }

    public void sendIsFreeToCentral() {
        // TODO: Informa que está livre
        // "LIVRE" [ID_CAMINHAO]

        try {
            String message = "LIVRE " + this.id;
            System.out.println("Informando a central que está livre -> " + message);
            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);
            pw.print(message);
            pw.flush();

            centralSocket.close();
            centralSocket = null;

            listenCentral();
        } catch (Exception e) {
            System.out.println("Erro ao mandar mensagem para central");
            e.printStackTrace();
        }
    }

    public void listenCentral() {
        // TODO: escuta chamados da central
        // "COLETAR" [ID_CONTAINER]
        // gerar tempo para chegar ao container (5 a 20 segs)

        try {
            if (centralSocket == null) {
                System.out.println("Aguardando conexão da central");
                centralSocket = this.socket.accept();
                System.out.println("Central conectada: " + centralSocket);
            }

            System.out.println("Aguardando comando da central");
            String mensagem;
            BufferedReader in = new BufferedReader(new InputStreamReader(this.centralSocket.getInputStream()));
            while ((mensagem = in.readLine()) != null) {
                System.out.println("Nova mensagem da central: " + mensagem);
                if (mensagem.contains("COLETAR")) {
                    this.idContainer = Integer.parseInt(mensagem.split(" ")[1]);



                    this.centralSocket.close();
                    this.centralSocket = null;


                    //Informa ip do container
//                    System.out.println("Informe o endereço do container " + this.idContainer + ":");
//                    String containerAddress = scanner.next();
//                    String containerIp = containerAddress.split(":")[0];
//                    int containerPort = Integer.parseInt(containerAddress.split(":")[1]);
//                    containerSocket = new Socket(containerIp, containerPort);


                    //Espera conexão do container
//                    System.out.println("Aguardando conexão do container");
//                    containerSocket = socket.accept();
//                    System.out.println("Endereço do container: " + containerSocket.getInetAddress().getHostAddress() + ":" + containerSocket.getPort());


                    //Se conecta ao container pelo id
                    System.out.println("Se conectando ao container " + this.idContainer);
                    Dispositivo container = getContainerById(this.idContainer);
                    containerSocket = new Socket(container.getIpAddress(), container.getPort());
                    System.out.println("Conectado ao container " + this.containerSocket);


                    this.status = STATUS_CAMINHAO.OCUPADO;
                    System.out.println("Indo coletar container " + this.idContainer);
                    int time = Util.generateRandomTime();
                    System.out.println("chegando em: " + time/1000);
                    Thread.sleep(time);
                    sendArrivedAtContainer();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error ao escutar comandos da central");
            e.printStackTrace();
        }
    }

    public void sendArrivedAtContainer() {
        // TODO: Informa ao container (ou a central) que chegou
        // "CHEGUEI_CONTAINER" [ID_CAMINHAO]
        // gerar tempo para voltar a central (5 a 20 segs)

        try {
            // Informa a central que chegou ao container
//            String message = "CHEGUEI_CONTAINER " + this.id;
//            System.out.println("Informando a central que chegou ao container -> " + message);
//            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);


            // Informa ao container que chegou
            String message = "CHEGUEI_CONTAINER " + this.id;
            System.out.println("Informando a container que chegou -> " + message);
            PrintWriter pw = new PrintWriter(this.containerSocket.getOutputStream(), true);


            pw.print(message);
            pw.flush();


            this.containerSocket.close();
            this.containerSocket = null;

            int time = Util.generateRandomTime();
            System.out.println("chegando em: " + time/1000);
            Thread.sleep(time);
            sendBackAtCentral();
        } catch (Exception e) {
            System.out.println("Erro ao comunicar que chegou ao container");
            e.printStackTrace();
        }
    }

    public void sendBackAtCentral() {
        //TODO: Comunica a central que voltou
        // Ao chegar comunicar central
        // "COLETA_FINALIZADA" [ID_CAMINHAO]

        try {
            Dispositivo central = getCentral();
            this.centralSocket = new Socket(central.getIpAddress(), central.getPort());

            String message = "COLETA_FINALIZADA " + this.id;
            System.out.println("Informando a central que voltou do container -> " + message);
            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);
            pw.print(message);
            pw.flush();
            this.status = STATUS_CAMINHAO.LIVRE;

//            this.centralSocket.close();
//            this.centralSocket = null;

            this.centralSocket.close();
            this.centralSocket = null;

            listenCentral();
        } catch (Exception e) {
            System.out.println("Erro ao comunicar central que voltou");
            e.printStackTrace();
        }
    }

    private void sendMessageTo(OutputStream out, String message) {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println(message);
        pw.flush();
    }

    private String getIpAddress() {
        return this.socket.getInetAddress().getHostAddress();
    }

    private static String readFileContent() throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get("C:/Users/Suporte/Desktop/Nova pasta/lista_dispositivos.txt"));
//        byte[] encoded = Files.readAllBytes(Paths.get("lista_dispositivos.txt"));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private Dispositivo getContainerById(int id) {
        for (Dispositivo d: containerList) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    private Dispositivo getCentral() {
        return centralList.get(0);
    }

    public int getPort() {
        return this.socket.getLocalPort();
    }
}

enum STATUS_CAMINHAO {
    LIVRE, OCUPADO
}
