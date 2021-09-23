package br.ucsal;

import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;

/*Execute o server informando nome, estado (ativado/desativado) + tempo de att
 * ao conectar responde: Nome + Estado + tempo de att
 * manda atualizações a cada tempo de att
 - Ativado: com a temperatura atual
 - Desativado; "Desativado"
 */
public class Server {
    private ServerSocket server;
    private String name;
    private Boolean isActive;
    private int refreshTime;

    public Server(String name, Boolean isActive, int refreshTime) throws Exception {
        this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        this.name = name;
        this.isActive = isActive;
        this.refreshTime = refreshTime;
    }

    public static void main(String[] args) throws Exception {
        Server app = new Server(args[0], (args[1].equals("1")), Integer.parseInt(args[2]));
        System.out.println(app.getName() + " executando em " + app.getIpAddress() + ":" + app.getPort());
        app.start();
    }

    private void start() throws Exception {
        System.out.println("Aguardando conexao...");
        Socket client = this.server.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        int clientPort = client.getPort();
        System.out.println("Nova conexao de: " + clientAddress + " " + clientPort);

//		Boolean isKnowClient = false;
//		for (Dispositivo dispositivo : availableClientList) {
//			if (clientAddress.equals(dispositivo.getIpAddress()) && clientPort == dispositivo.getPort()) {
//				System.out.println("Dispositivo permitido!");
//				dispositivo.setSocket(client);
//				Server.connectedClientList.add(dispositivo);
//				isKnowClient = true;
//				break;
//			}
//		}
//		if (!isKnowClient) {
//			System.out.println("Dispositivo não permitido");
//			client.close();
//		}

        Thread messagerThread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sendMessage(client.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        messagerThread.start();

        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    listen(client.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
        listenerThread.start();
    }

    private void sendMessage(OutputStream out) {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                String mensagem = "DESATIVADO";
                PrintWriter pw = new PrintWriter(out, true);
                if (Server.this.isActive) {
                    mensagem = "Temperatura atual: 30°";
                }
                pw.println(mensagem);
                pw.flush();
            }
        }, 0, this.getRefreshTime(), TimeUnit.SECONDS);
    }

    private void listen(InputStream inputStream) throws Exception {
        String mensagem = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        while ((mensagem = in.readLine()) != null) {
            System.out.println("Nova mensagem: " + mensagem);
        }
    }

    public String getIpAddress() {
        return this.server.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return this.server.getLocalPort();
    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

}