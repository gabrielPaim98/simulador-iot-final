package br.ucsal;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Dispositivo {
    private String serviceName;
    private String ipAddress;
    private Boolean isActive;
    private int refreshTime;
    private int port;
    private Socket socket;

    static List<Dispositivo> listFromFile(String fileContent) {
        List<Dispositivo> dispositivoList = new ArrayList<Dispositivo>();
        String[] dispositvosString = fileContent.split("\\r?\\n");
        for (int i = 0; i < dispositvosString.length; i++) {
            dispositivoList.add(new Dispositivo(dispositvosString[i]));
        }
        return dispositivoList;
    }

    public Dispositivo(String fullName) {
        String[] nameSplit = fullName.split(" ");
        serviceName = nameSplit[0];
        ipAddress = nameSplit[1];
        port = Integer.parseInt(nameSplit[2]);
    }

    public Dispositivo(String serviceName, String ipAddress, int port) {
        this.serviceName = serviceName;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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

    @Override
    public String toString() {
        return serviceName + " " + ipAddress + " " + port;
    }

}