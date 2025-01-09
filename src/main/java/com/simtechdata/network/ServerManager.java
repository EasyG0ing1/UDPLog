package com.simtechdata.network;

import com.simtechdata.App;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerManager implements Runnable {


    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public ServerManager(String logFilePath, int udpPort, int bufferSize) {
        this.logFilePath = logFilePath;
        this.udpPort = udpPort;
        this.bufferSize = bufferSize;
    }

    private final String logFilePath;
    private final int udpPort;
    private final int bufferSize;
    private UDPServer udpServer;
    private Thread thread;

    @Override
    public void run() {
        if(udpPortAvailable(udpPort)) {
            SCHEDULER.scheduleAtFixedRate(checkServer(), 0, 5, TimeUnit.SECONDS);
        }
    }

    private void startServer() {
        udpServer = new UDPServer(logFilePath, udpPort, bufferSize);
        thread = new Thread(udpServer);
        thread.start();
        if(App.test) {
            System.out.println("Started server on port: " + udpPort);
        }
    }

    private Runnable checkServer() {
        return () -> {
            if (udpServer == null || !udpServer.isRunning() || thread == null || !thread.isAlive()) {
                startServer();
            }
        };
    }

    public boolean udpPortAvailable(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (SocketException e) {
            System.err.println("The selected UDP port: " + udpPort + " Is already in use on this machine.");
            System.err.println("Please set a different port and try again.");
            System.exit(0);
        }
        return false;
    }

}
