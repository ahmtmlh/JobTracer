package edu.deu.seniorproject.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    private boolean receiveError = false;
    private boolean tcpConnectionError = false;
    private boolean initError = false;

    private DataOutputStream out;
    private DataInputStream in;

    private final String ip;
    private final int port;

    private String receivedMessage;

    private Client(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void startConnection(){
        try {
            Socket clientSocket = new Socket();
            clientSocket.setSoTimeout(500);
            clientSocket.connect(new InetSocketAddress(ip, port), 200);

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            initError = true;
        }
    }

    public void sendMessage(String msg){
        try{
            out.flush();
            out.write(msg.getBytes(), 0, msg.getBytes().length);
        } catch (IOException e){
            e.printStackTrace();
            tcpConnectionError = true;
        }
    }

    public String getResponse(){
        try{
            byte[] recBuff = new byte[2048];
            int readBytes = in.read(recBuff);
            if(readBytes != -1 && !(recBuff[0] == 1 && recBuff[1] == -1)){
                receivedMessage = new String(recBuff, 0, readBytes, StandardCharsets.UTF_8);
                receiveError = false;
            } else {
                System.out.println("ERR: Receive Error");
                receiveError = true;
            }
            tcpConnectionError = false;
        } catch (IOException e){
            e.printStackTrace();
            tcpConnectionError = true;
            receivedMessage = "";
        }
        return receivedMessage;
    }

    public boolean hasError(){
        return initError || tcpConnectionError || receiveError;
    }

    public boolean hasReceiveError(){
        return receiveError;
    }

    public boolean isTcpConnectionError(){
        return tcpConnectionError;
    }

    public String getReceivedMessage(){
        return receivedMessage;
    }

    public String getErrorCause(){
        if(hasReceiveError()){
            return "No correct response received";
        } else if (isTcpConnectionError()){
            return "Connection Error";
        } else {
            return "Initialization Error. Check IP and Port";
        }
    }

    public static Client create(String ip, int port){
        return new Client(ip, port);
    }

}
