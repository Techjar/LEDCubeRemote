package com.techjar.ledcr.network;

import com.techjar.ledcr.network.packet.Packet;
import com.techjar.ledcr.network.packet.PacketClientCapabilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TCPClient {
    private Thread sendThread;
    private Thread recvThread;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();
    private Queue<Packet> recvQueue = new ConcurrentLinkedQueue<>();

    public TCPClient(InetAddress address, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), 2000);
        socket.setTcpNoDelay(true);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        sendQueue.add(new PacketClientCapabilities(Packet.Capabilities.CONTROL_DATA));
        sendThread = new Thread("Client Send Thread") {
            @Override
            public void run() {
                Packet packet;
                DataOutputStream out = new DataOutputStream(TCPClient.this.outputStream);
                while (!TCPClient.this.socket.isClosed()) {
                    while ((packet = sendQueue.poll()) != null) {
                        try {
                            Packet.writePacket(out, packet);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            try {
                                close();
                            } catch (IOException ex2) {
                                ex2.printStackTrace();
                            }
                            return;
                        }
                    }
                    try { Thread.sleep(10); }
                    catch (InterruptedException ex) {} // I hate checked exceptions
                }
            }
        };
        sendThread.start();
        recvThread = new Thread("Client Recv Thread") {
            @Override
            public void run() {
                Packet packet;
                DataInputStream in = new DataInputStream(TCPClient.this.inputStream);
                try {
                    while (!TCPClient.this.socket.isClosed()) {
                        packet = Packet.readPacket(in);
                        recvQueue.add(packet);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        };
        recvThread.setDaemon(true);
        recvThread.start();
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void close() throws IOException {
        socket.close();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Queue<Packet> getReceiveQueue() {
        return recvQueue;
    }

    public void queuePacket(Packet packet) {
        sendQueue.add(packet);
    }
}
