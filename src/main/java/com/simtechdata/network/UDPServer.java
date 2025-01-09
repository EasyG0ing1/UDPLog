package com.simtechdata.network;

import com.simtechdata.App;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer implements Runnable {

    public UDPServer(String logFilePath, int udpPort, int bufferSize) {
        this.logFilePath = logFilePath;
        this.udpPort = udpPort;
        this.bufferSize = bufferSize;
    }

    private final String logFilePath;
    private final int udpPort;
    private final int bufferSize;
    private DatagramSocket socket;
    private boolean running = true;
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(udpPort);
            socket.setReuseAddress(true);
            while (running) {
                DatagramPacket inPacket = getInPacket();
                if (socket != null && !socket.isClosed()) {
                    socket.receive(inPacket);
                    new Thread(logData(inPacket)).start();
                }
                else {
                    running = false;
                    return;
                }
            }
        }
        catch(SocketException ignored) {
            System.out.println("UDP Socket Closed");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }


    private DatagramPacket getInPacket() {
        byte[] buffer = new byte[bufferSize];
        return new DatagramPacket(buffer, buffer.length);
    }


    private Runnable logData(DatagramPacket inPacket) {
        return () -> {
            String data = new String(inPacket.getData(), 0, inPacket.getLength());
            File file = new File(logFilePath);
            if(file.exists()) {
                try {
                    String fileContents = FileUtils.readFileToString(file);
                    fileContents += data + "\n";
                    FileUtils.writeStringToFile(file, fileContents);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    FileUtils.createParentDirectories(file);
                    FileUtils.writeStringToFile(file, data + "\n");
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(App.test) {
                System.out.println("Wrote:\n" + data + "\n\nTo log file: " + file.getAbsolutePath());
            }
            if(App.graal) {
                System.out.println("Data received and logged into file: " + file.getAbsolutePath());
                System.exit(0);
            }
        };
    }

    public boolean isRunning() {
        return running;
    }
}
