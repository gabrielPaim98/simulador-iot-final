package br.ucsal;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*lê o txt de dispositivos
cli para conectar aos dispositivos
nomeD1 CONECTAR nomeD2
escuta atualizações dos dispositivos conectados*/
public class Client {
    private final Scanner scanner = new Scanner(System.in);
    private static final String name = "Computador";
    private static List<Dispositivo> availableClientList;
    private static final List<Dispositivo> connectedClientList = new ArrayList<Dispositivo>();

    public Client() throws Exception {
        this.start();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Dispositivos disponiveis");
        availableClientList = Dispositivo.listFromFile(readFileContent());
        for (int i = 0; i < availableClientList.size(); i++) {
            System.out.println(i + " - " + availableClientList.get(i));
        }

        new Client();
    }

    private void start() {
        while (true) {
            System.out.println("Informe o numero do dispositivo que deseja conectar");
            int selectedDevice = scanner.nextInt();
            try {
                connectToSelectedClient(selectedDevice);
            } catch (Exception e) {
                System.out.println("Não foi possível se conectar com o dispositivo selecionado");
                e.printStackTrace();
            }
        }
    }

    private void connectToSelectedClient(int selectedDevice) throws UnknownHostException, Exception {
        Dispositivo selectedClient = availableClientList.get(selectedDevice);
        System.out.println("Conectando a " + selectedClient);
        Socket socket = connectToServer(selectedClient.getIpAddress(), selectedClient.getPort());
        selectedClient.setSocket(socket);
        sendConnectMessage(selectedClient);
        connectedClientList.add(selectedClient);

        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    listen(selectedClient.getSocket().getInputStream(), selectedClient.getServiceName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
        listenerThread.start();
    }

    private Socket connectToServer(String serverAdderss, int serverPort) throws UnknownHostException, Exception {
        Socket socket = new Socket(serverAdderss, serverPort);
        System.out.println("Dispositivo conectado!");
        return socket;
    }

    private void sendConnectMessage(Dispositivo connectedDevice) throws IOException {
        String connectMessage = name + " CONECTAR " + connectedDevice.getServiceName();
        PrintWriter pw = new PrintWriter(connectedDevice.getSocket().getOutputStream(), true);
        pw.println(connectMessage);
        pw.flush();
    }

    private void listen(InputStream inputStream, String clientAddress) throws Exception {
        String mensagem = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        while ((mensagem = in.readLine()) != null) {
            System.out.println("Nova mensagem de " + clientAddress + ": " + mensagem);
        }
    }

    private static String readFileContent() throws IOException {
//		byte[] encoded = Files.readAllBytes(Paths.get("C:/Users/Gabriel/Desktop/Nova pasta/lista_dispositivos.txt"));
        byte[] encoded = Files.readAllBytes(Paths.get("lista_dispositivos.txt"));
        return new String(encoded, StandardCharsets.UTF_8);
    }

//	public void start() throws Exception {
//		Thread listenerThread = new Thread() {
//			@Override
//			public void run() {
//				try {
//					super.run();
//					listen();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			};
//		};
//
//		Thread messagerThread = new Thread() {
//			@Override
//			public void run() {
//				super.run();
//				try {
//					sendMessage();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//
//		listenerThread.start();
//		messagerThread.start();
//	}
//	public void listen() throws Exception {
//		String mensagem;
//		BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
//		while ((mensagem = in.readLine()) != null) {
//			System.out.println("Nova mensagem do servidor: " + mensagem);
//		}
//	}
//
//	public void sendMessage() throws Exception {
//		String input;
//		System.out.println("Escreva uma mensagem:");
//		while (true) {
//			input = scanner.nextLine();
//			PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
//			out.println(input);
//			out.flush();
//		}
//	}
}