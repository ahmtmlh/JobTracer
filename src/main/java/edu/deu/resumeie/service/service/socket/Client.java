package edu.deu.resumeie.service.service.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class Client {

    private static final Logger logger = LogManager.getLogger(Client.class);

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

    public void startConnection(){
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ip, port), 200);
            clientSocket.setSoTimeout(500);
            clientSocket.setKeepAlive(true);

        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            initError = true;
        }
    }

    public void stopConnection(){
        try{
        	clientSocket.close();
        } catch(IOException ignore){ /*Ignore the exception*/ }
    }

    public void sendMessage(String msg) throws IOException{
        lastSendMessageLength = msg.getBytes().length;
        clientSocket.getOutputStream().write(msg.getBytes(), 0, msg.getBytes().length);
        clientSocket.getOutputStream().flush();
    }

    public String receiveMessage(){
        try{
            byte[] recBuff = new byte[lastSendMessageLength+1];
            int readBytes = clientSocket.getInputStream().read(recBuff);
            if(readBytes != -1){
                receivedMessage = new String(recBuff, 0, readBytes, StandardCharsets.UTF_8);
                receiveError = false;
            } else {
                logger.error("Receive Error");
                receiveError = true;
            }
            tcpConnectionError = false;
        } catch (IOException e){
            logger.error(e.getLocalizedMessage(), e);
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
    public String sendAndReceive(String msg) throws IOException{
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