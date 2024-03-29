package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.service.model.City;
import edu.deu.resumeie.shared.SharedObjects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CityDataRepository {

    private static final Logger logger = LogManager.getLogger(CityDataRepository.class);

    private final ConnectionPool connectionPool;

    public CityDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl(SharedObjects.DB_CONN_STR);
    }

    public List<City> getCities() {
        String sql = "SELECT * FROM CITIES";
        List<City> list = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(Statement stmt = conn.createStatement()){
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String zipCode = rs.getString(1);
                    String cityName = rs.getString(2);
                    list.add(new City(cityName,zipCode));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException | ConnectionPoolException e){
            logger.fatal(e.getLocalizedMessage(), e);
        }

        return list.stream()
                .sorted(Comparator.comparing(City::getCityName, Collator.getInstance()))
                .collect(Collectors.toList());
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
        } catch(SQLException | ConnectionPoolException e){
            logger.fatal(e.getLocalizedMessage(), e);
        }

        return list;
    }

}
