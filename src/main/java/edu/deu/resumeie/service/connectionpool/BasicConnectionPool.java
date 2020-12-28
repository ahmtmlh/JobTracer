package edu.deu.resumeie.service.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BasicConnectionPool implements ConnectionPool{


    private final List<Connection> availableConnections;
    private final List<Connection> usedConnections;

    private static final int MIN_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 100;

    private String url;
    private boolean closed;

    /**
     * This function requires the URL to contain all information like username and password
     * If JDBC driver for the certain SQL Engine doesn't support username and password
     * int JDBC URL, {@link edu.deu.resumeie.service.connectionpool.BasicConnectionPool#createInstanceForUser}
     * function first to create an single instance, then use this function to retrieve it.
     *
     * @see edu.deu.resumeie.service.connectionpool.BasicConnectionPool#createInstanceForUser
     * @param url JDBC Connection URL for SQL Engine
     * @return A connection pool that is static and shared among other threads
     */
    public static ConnectionPool getInstanceForUrl(String url){
        if (!pools.containsKey(url)){
            createInstance(url);
        }
        return pools.get(url);
    }

    public static void createInstance(String url){
        ConnectionPool pool = new BasicConnectionPool(url);
        pools.put(url, pool);
    }

    public static void createInstanceForUser(String url, String username, String password){
        ConnectionPool pool = new BasicConnectionPool(url, username, password);
        pools.put(url, pool);
    }

    private BasicConnectionPool(String url){
        this(url, "", "");
    }

    private BasicConnectionPool(String url, String username, String password){
        this.setUrl(url);
        closed = false;
        availableConnections = new ArrayList<>();
        usedConnections = new ArrayList<>();
        if ((username == null || username.isEmpty())){
            initWithoutUser();
        } else {
            initWithUser(username, password == null ? "" : password);
        }
    }

    private void initWithUser(String username, String password){
        try{
            for (int i = 0; i < MIN_POOL_SIZE; i++) {
                availableConnections.add(createConnection(this.url, username, password));
            }
        } catch (SQLException e){
            System.err.println(e.getLocalizedMessage());
        }
    }

    private void initWithoutUser(){
        try{
            for (int i = 0; i < MIN_POOL_SIZE; i++) {
                availableConnections.add(createConnection(this.url));
            }
        } catch (SQLException e){
            System.err.println(e.getLocalizedMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException, ConnectionPoolException{
        if(closed){
            throw new ConnectionPoolException("Connection pool is closed");
        }
        if (availableConnections.isEmpty()){
            if(usedConnections.size() < MAX_POOL_SIZE){
                availableConnections.add(createConnection(this.url));
            } else {
                throw new ConnectionPoolException("Max Pool Size is reached. Can't create new connection. Try again for an existing connection");
            }
        }

        if(availableConnections.isEmpty()){
            throw new ConnectionPoolException("No Connection available at the moment");
        }

        Connection conn = availableConnections.remove(availableConnections.size()-1);
        usedConnections.add(conn);
        // Check for if connection is still valid?

        return conn;

    }

    @Override
    public void releaseConnection(Connection connection) throws ConnectionPoolException {
        if(closed){
            throw new ConnectionPoolException("Connection pool is closed");
        }
        // Check if this connection is known by the pool
        if(usedConnections.remove(connection)){
            availableConnections.add(connection);
        }
    }

    @Override
    public void setUrl(String connectionUrl) {
        this.url = connectionUrl;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public void close() throws SQLException {
        for(Connection c : availableConnections){
            c.close();
        }
        for (Connection c : usedConnections){
            c.close();
        }
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        try{
            this.close();
        } finally {
            super.finalize();
        }
    }

    private Connection createConnection(String connUrl, String username, String password) throws SQLException {
        return DriverManager.getConnection(connUrl, username, password);
    }


    private Connection createConnection(String connUrl) throws SQLException {
        return DriverManager.getConnection(connUrl);
    }
}
