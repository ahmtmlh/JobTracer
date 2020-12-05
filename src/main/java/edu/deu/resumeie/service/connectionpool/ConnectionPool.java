package edu.deu.resumeie.service.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public interface ConnectionPool {

    Map<String, ConnectionPool> pools = new HashMap<>();

    Connection getConnection() throws SQLException, ConnectionPoolException;
    void releaseConnection(Connection connection) throws ConnectionPoolException;
    void setUrl(String connectionUrl);
    String getUrl();
    void close() throws SQLException;


}
