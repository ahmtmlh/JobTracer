package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.service.model.City;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CityDataRepository {


    private final ConnectionPool connectionPool;

    public CityDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl("jdbc:sqlite:./data/data.db");
    }

    public List<City> getCities() {
        String sql = "SELECT * FROM CITIES";
        List<City> list = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(Statement stmt = conn.createStatement()){
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String zipCode = rs.getString("id");
                    String cityName = rs.getString("name");
                    list.add(new City(cityName,zipCode));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException e){
            e.printStackTrace();
        } catch(ConnectionPoolException e){
            System.err.println(e.getLocalizedMessage());
        }

        return list;
    }

    public List<City> getCitiesStartingWith(String prefix){
        String sql = "SELECT * FROM CITIES WHERE name LIKE ?";
        List<City> list = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setString(1, prefix + "%");
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String zipCode = rs.getString(1);
                    String cityName = rs.getString(2);
                    list.add(new City(cityName,zipCode));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException e){
            e.printStackTrace();
        } catch(ConnectionPoolException e){
            System.err.println(e.getLocalizedMessage());
        }

        return list;
    }

}
