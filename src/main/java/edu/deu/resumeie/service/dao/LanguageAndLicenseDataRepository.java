package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.shared.SharedObjects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LanguageAndLicenseDataRepository {

    private static final Logger logger = LogManager.getLogger(LanguageAndLicenseDataRepository.class);

    private final ConnectionPool connectionPool;

    public LanguageAndLicenseDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl(SharedObjects.DB_CONN_STR);
    }


    public List<String> getLanguages() {
        String sql = "SELECT name FROM LANGUAGES";
        List<String> languages = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(Statement stmt = conn.createStatement()){
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    languages.add(rs.getString(1));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException | ConnectionPoolException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return languages;
    }


    public List<String> getLanguages(String prefix) {
        String sql = "SELECT name FROM LANGUAGES WHERE name LIKE ?";
        List<String> languages = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setString(1, prefix + "%");
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    languages.add(rs.getString(1));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException | ConnectionPoolException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return languages;
    }



    public List<String> getDriverLicenceTypes() {
        String sql = "SELECT type FROM DRIVING_LICENSES";
        List<String> drivingLicenses = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(Statement stmt = conn.createStatement()){
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    drivingLicenses.add(rs.getString(1));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch(SQLException | ConnectionPoolException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return drivingLicenses;
    }


}
