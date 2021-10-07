package br.ucsal;

import java.io.*;
import java.net.Socket;
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
    private final Scanner scanner = new Scanner(System.in);

    public Caminhao(int id) {
        this.id = id;
    }

    public static void main(String[] args) {
        int id;
        if (args.length > 0) {
            id = Integer.parseInt(args[0]);
        } else {
            id = 3241;
        }

        Caminhao caminhao = new Caminhao(id);
        caminhao.start();
    }

    public void start() {
        // TODO: Conecta a central

        try {
            System.out.println("Informe o endereço e porta da central");
            String centralAddress = scanner.next();
            String centralIp = centralAddress.split(":")[0];
            int centralPort = Integer.parseInt(centralAddress.split(":")[1]);
            centralSocket = new Socket(centralAddress, centralPort);
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
            System.out.println("Informando a central que está livre");
            String message = "LIVRE " + this.id;
            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);
            pw.println(message);
            pw.flush();
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
            System.out.println("Aguardando comando da central");
            String mensagem;
            BufferedReader in = new BufferedReader(new InputStreamReader(this.centralSocket.getInputStream()));
            while ((mensagem = in.readLine()) != null) {
                System.out.println("Nova mensagem da central: " + mensagem);
                if (mensagem.contains("COLETAR")) {
                    this.idContainer = Integer.parseInt(mensagem.split(" ")[1]);
                    this.status = STATUS_CAMINHAO.OCUPADO;
                    Thread.sleep(Util.generateRandomTime());
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
            System.out.println("Informando a central que chegou ao container");
            String message = "CHEGUEI_CONTAINER " + this.idContainer;
            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);
            pw.println(message);
            pw.flush();
            Thread.sleep(Util.generateRandomTime());
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
            System.out.println("Informando a central que voltou do container");
            String message = "COLETA_FINALIZADA " + this.idContainer;
            PrintWriter pw = new PrintWriter(this.centralSocket.getOutputStream(), true);
            pw.println(message);
            pw.flush();
            this.status = STATUS_CAMINHAO.LIVRE;
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
}

enum STATUS_CAMINHAO {
    LIVRE, OCUPADO
}