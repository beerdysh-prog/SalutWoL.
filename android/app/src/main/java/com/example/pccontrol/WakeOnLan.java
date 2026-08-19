package com.example.pccontrol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class WakeOnLan {
    private WakeOnLan() {}

    public static void send(String mac, String broadcast) throws Exception {
        byte[] macBytes = parseMac(mac);
        byte[] packet = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) packet[i] = (byte) 0xFF;
        for (int i = 6; i < packet.length; i++) packet[i] = macBytes[(i - 6) % macBytes.length];

        InetAddress address = InetAddress.getByName(broadcast);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            for (int i = 0; i < 3; i++) {
                socket.send(new DatagramPacket(packet, packet.length, address, 9));
                Thread.sleep(150);
            }
        }
    }

    private static byte[] parseMac(String value) {
        String clean = value.replace(":", "").replace("-", "").trim();
        if (clean.length() != 12) throw new IllegalArgumentException("Неверный MAC-адрес");
        byte[] result = new byte[6];
        for (int i = 0; i < 6; i++) result[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        return result;
    }
}
