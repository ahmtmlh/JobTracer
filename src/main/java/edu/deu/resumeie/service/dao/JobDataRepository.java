package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.service.model.City;
import edu.deu.resumeie.service.model.Job;
import edu.deu.resumeie.shared.SharedObjects;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobDataRepository {

    /**
     * Pre Matching SQL:
         SELECT * FROM DATA WHERE exp <= ? AND max_exp >= ? (indexed)
         AND ed_status <= ?     (indexed)
         AND position = ?       (indexed)
         AND cities LIKE %?%    (can't use index on wildcard search. Table search required)
     */

    private static final String SQL_QUERY =
            "SELECT JOB_DATA.*, CITIES.name, CLUSTER_DATA.clusters_tfidf, CLUSTER_DATA.clusters_cnt " +
            "FROM JOB_DATA " +
            "INNER JOIN JOB_CITIES ON JOB_DATA.id = JOB_CITIES.job_id " +
            "INNER JOIN CITIES ON JOB_CITIES.city_id = CITIES.id " +
            "INNER JOIN CLUSTER_DATA ON JOB_DATA.id = CLUSTER_DATA.job_id "+
            "WHERE exp <= ? AND max_exp >= ? AND ed_status <= ? AND position = ?";

    private final ConnectionPool connectionPool;

    public JobDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl(SharedObjects.DB_CONN_STR);
    }

    private void prepareStatementForPreMatch(PreparedStatement preparedStatement, int exp, int ed_status,
                                             String position, String[] cities, int cityLen) throws SQLException{
        preparedStatement.setInt(1, exp);
        preparedStatement.setInt(2, exp);
        preparedStatement.setInt(3, ed_status);
        preparedStatement.setString(4, position);
        for (int i = 0; i < cityLen; i++) {
            preparedStatement.setString(i+5, cities[i].trim());
        }
    }

    private Job getJobFromResult(ResultSet rs, boolean getText) throws SQLException{
        String id = String.valueOf(rs.getInt(1));
        int exp = rs.getInt(2);
        int max_exp = rs.getInt(3);
        String position = rs.getString(4);
        int ed_status = rs.getInt(5);
        String cities = rs.getString(8);
        String clusters = rs.getString(SharedObjects.serviceParams.vectorizer.equals("tfidf") ? 9 : 10);
        Job ret = new Job(id, exp, max_exp, position, cities, ed_status, clusters);

        if (getText){
            String htmlText = rs.getString(6);
            String textClear = rs.getString(7);
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
        // Map is required since cities will
        Map<String, Job> retMap = new HashMap<>();
        String sql = SQL_QUERY;
        String[] citiesSplit = cities.split(",");
        int cityLen = 0;
        if (!(cities.startsWith("*") || "tüm türkiye".equalsIgnoreCase(cities))){
            StringBuilder sb = new StringBuilder(sql).append(" AND (");
            cityLen = citiesSplit.length;
            for (int i = 0; i < cityLen; i++) {
                sb.append("CITIES.name=?");
                if (i != cityLen-1){
                    sb.append(" OR ");
                }
            }
            sb.append(")");
            sql = sb.toString();
        }

        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                prepareStatementForPreMatch(preparedStatement, exp, ed_status, position, citiesSplit, cityLen);
                ResultSet rs = preparedStatement.executeQuery();
                while(rs.next()){
                    Job temp = getJobFromResult(rs, getText);
                    if (retMap.containsKey(temp.getId())){
                        temp.addCities(retMap.get(temp.getId()).getCities());
                    }
                    retMap.put(temp.getId(), temp);
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

        return new ArrayList<>(retMap.values());
    }

}
