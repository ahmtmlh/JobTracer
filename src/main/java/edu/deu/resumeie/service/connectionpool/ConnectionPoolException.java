package edu.deu.resumeie.service.connectionpool;

public class ConnectionPoolException extends Exception{


    public ConnectionPoolException(String msg){
        super(msg);
    }

    public ConnectionPoolException(Throwable t){
        super(t);
    }


}
