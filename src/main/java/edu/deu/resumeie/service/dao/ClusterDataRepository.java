package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.shared.SharedObjects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClusterDataRepository {

    /**
     * Pre Matching SQL:
         SELECT * FROM DATA WHERE exp <= ? AND max_exp >= ? (indexed)
         AND ed_status <= ?     (indexed)
         AND position = ?       (indexed)
         AND cities LIKE %?%    (can't use index on wildcard search. Table search required)
     */

    private static final String SQL_WITH_TEXT = "SELECT CLUSTER_DATA.*, JOB_DATA.text, JOB_DATA.text_clear " +
            "FROM CLUSTER_DATA INNER JOIN JOB_DATA ON CLUSTER_DATA.id=JOB_DATA.id " +
            "WHERE exp <= ? AND max_exp >= ? AND ed_status <= ? AND position = ?";

    private static final String SQL_WITHOUT_TEXT = "SELECT * FROM CLUSTER_DATA WHERE exp <= ? AND max_exp >= ? AND ed_status <= ? AND position = ?";


    private final ConnectionPool connectionPool;

    public ClusterDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl("jdbc:sqlite:./data/cluster_data.db");
    }

    private void prepareStatementForPreMatch(PreparedStatement preparedStatement, int exp, int ed_status,
                                             String position, String cities, boolean cityValid) throws SQLException{
        preparedStatement.setInt(1, exp);
        preparedStatement.setInt(2, exp);
        preparedStatement.setInt(3, ed_status);
        preparedStatement.setString(4, position);
        if(cityValid){
            preparedStatement.setString(5, "%" + cities + "%");
        }
    }

    private Job getJobFromResult(ResultSet rs, boolean getText) throws SQLException{
        String id = String.valueOf(rs.getInt(1));
        int exp = rs.getInt(2);
        int max_exp = rs.getInt(3);
        String position = rs.getString(4);
        String cities = rs.getString(5);
        int ed_status = rs.getInt(6);
        String clusters = rs.getString(SharedObjects.serviceParams.vectorizer.equals("tfidf") ? 7 : 8);
        Job ret = new Job(id, exp, max_exp, position, cities, ed_status, clusters);

        if (getText){
            String htmlText = rs.getString(8);
            String textClear = rs.getString(9);
            ret.setText(textClear);
            ret.setHtmlText(htmlText);
        }

        return ret;
    }


    public boolean doesJobPositionExist(String position){
        String sql = "SELECT position FROM CLUSTER_DATA WHERE position=? LIMIT 1";
        boolean ret = false;
        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setString(1, position);
                ResultSet rs = preparedStatement.executeQuery();
                if(rs.next()){
                    ret = true;
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

        return ret;
    }

    public List<Job> getPreMatchedJobAds(int exp, int ed_status, String position, String cities, boolean getText){
        List<Job> retList = new ArrayList<>();
        boolean cityValid = false;
        String sql = getText ? SQL_WITH_TEXT : SQL_WITHOUT_TEXT;
        if (!("*".equals(cities) || "tüm türkiye".equalsIgnoreCase(cities))){
            sql += " AND cities LIKE ?";
            cityValid = true;
        }

        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                prepareStatementForPreMatch(preparedStatement, exp, ed_status, position, cities, cityValid);
                ResultSet rs = preparedStatement.executeQuery();
                while(rs.next()){
                    retList.add(getJobFromResult(rs, getText));
                }
                rs.close();
            } finally {
                connectionPool.releaseConnection(conn);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ConnectionPoolException e){
            System.err.println(e.getLocalizedMessage());
        }

        return retList;
    }


}
