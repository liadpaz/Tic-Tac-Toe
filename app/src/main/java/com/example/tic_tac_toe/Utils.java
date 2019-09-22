package com.example.tic_tac_toe;

import java.io.*;
import java.net.*;
import java.util.*;

public class Utils {
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public static String getSocketMessage(Socket socket, String type) throws Exception{
        String rawInput = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
        String[] input = rawInput.split(" ");
        return input[0].equals(type) ? input[1] : null;
    }

    private static void sendSocketMessage(Socket socket, String type, String message) throws Exception {
        PrintWriter output = new PrintWriter(socket.getOutputStream());
        output.println(String.format("%s %s", type, message));
        output.close();
    }

    public static void sendInitHostMessage(Socket socket, String name, String ip) throws Exception{
        sendSocketMessage(socket, "Host Name", name);
        sendSocketMessage(socket, "Host IP", ip);
    }

    public static void sendHostMessage(Socket socket, String type, String param) throws Exception{
        sendSocketMessage(socket, "Host " + type, param);
    }

    public static void sendInitClientMessage(Socket socket, String name) throws Exception{
        sendSocketMessage(socket, "Client Name", name);
    }

    public static void sendClientMessage(Socket socket, String type, String param) throws Exception{
        sendSocketMessage(socket, "Client " + type, param);
    }
}