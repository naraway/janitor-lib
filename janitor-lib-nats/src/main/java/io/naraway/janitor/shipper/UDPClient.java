/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.janitor.shipper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPClient {
    //
    private DatagramSocket socket;
    private InetAddress address;
    private int port;

    public UDPClient(String address, int port) throws Exception{
        //
        socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
        this.port = port;
    }

    public void sendLog(byte[] log) throws Exception{
        //
        DatagramPacket packet = new DatagramPacket(log, log.length, address, port);
        socket.send(packet);
    }

    public void close() {
        //
        socket.close();
    }
}
