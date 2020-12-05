package edu.deu.resumeie.service.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Client {

    private boolean receiveError = false;
    private boolean tcpConnectionError = false;
    private boolean initError = false;
    private boolean timeOutError = false;

    private Socket clientSocket;

    private final String ip;
    private final int port;

    private String receivedMessage;
    private int lastSendMessageLength;

    private Client(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public boolean startConnection(){
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ip, port), 200);
            clientSocket.setSoTimeout(500);
            clientSocket.setKeepAlive(true);

        } catch (IOException e) {
            e.printStackTrace();
            initError = true;
        }
        return hasError();
    }

    public void stopConnection(){
        try{
        	clientSocket.close();
        } catch(IOException ignore){ /*Ignore the exception*/ }
    }

    public void sendMessage(String msg){
        try{
            lastSendMessageLength = msg.getBytes().length;
            clientSocket.getOutputStream().write(msg.getBytes(), 0, msg.getBytes().length);
            clientSocket.getOutputStream().flush();
        } catch (IOException e){
            e.printStackTrace();
            tcpConnectionError = true;
        }
    }

    public String receiveMessage(){
        try{
            byte[] recBuff = new byte[lastSendMessageLength+1];
            int readBytes = clientSocket.getInputStream().read(recBuff);
            if(readBytes != -1){
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
            if(e instanceof SocketTimeoutException){
                timeOutError = true;
            }
            receivedMessage = "";
        }
        return receivedMessage;
    }

    /**
     * This function is the equivalent to calling sendMessage and receiveMessage
     * @param msg   Message to be send
     * @return      Message received from the server. Empty String if no message is received.
     */
    public String sendAndReceive(String msg){
        this.sendMessage(msg);
        return this.receiveMessage();
    }

    public boolean hasError(){
        return initError || tcpConnectionError || receiveError || timeOutError;
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
            return "Initialization Error. Check the service status";
        }
    }

    public static Client create(String ip, int port){
        return new Client(ip, port);
    }
}