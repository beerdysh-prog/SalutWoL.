package com.example.pccontrol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public final class PowerClient {
    private PowerClient() {}

    public static void shutdown(String ip, String secret) throws Exception {
        if (secret == null || secret.isBlank() || secret.equals("CHANGE_ME")) {
            throw new IllegalArgumentException("Сначала задайте секретный ключ");
        }
        String msg = "OFF " + secret;
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(ip);
        try (DatagramSocket socket = new DatagramSocket()) {
            for (int i = 0; i < 3; i++) {
                socket.send(new DatagramPacket(data, data.length, address, 38383));
                Thread.sleep(120);
            }
        }
    }
}
