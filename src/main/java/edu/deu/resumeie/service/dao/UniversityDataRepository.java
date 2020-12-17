package edu.deu.resumeie.service.dao;

import edu.deu.resumeie.service.connectionpool.BasicConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPool;
import edu.deu.resumeie.service.connectionpool.ConnectionPoolException;
import edu.deu.resumeie.service.model.dto.DepartmentDTO;
import edu.deu.resumeie.service.model.dto.FacultyDTO;
import edu.deu.resumeie.service.model.dto.UniversityDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UniversityDataRepository {

    private final ConnectionPool connectionPool;

    public UniversityDataRepository(){
        connectionPool = BasicConnectionPool.getInstanceForUrl("jdbc:sqlite:./data/data.db");
    }

    public List<UniversityDTO> getUniversities(){
        String sql = "SELECT * FROM UNIVERSITIES";
        List<UniversityDTO> list = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(Statement stmt = conn.createStatement()){
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);
                    list.add(new UniversityDTO(id, name));
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


    public List<FacultyDTO> getFacultyOfUniversity(int uniId){
        String sql = "SELECT FACULTIES.id, FACULTIES.name FROM FACULTIES " +
                "INNER JOIN UNIVERSITIES ON FACULTIES.uni_id = UNIVERSITIES.id WHERE UNIVERSITIES.id = ?";
        List<FacultyDTO> faculties = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setInt(1, uniId);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);

                    faculties.add(new FacultyDTO(id, uniId, name));
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

        return faculties;
    }


    public List<DepartmentDTO> getDepartmentOfFacultyAndUniversity(int facId, int uniId){
        String sql = "SELECT DEPARTMENTS.id, DEPARTMENTS.name FROM DEPARTMENTS " +
                     "INNER JOIN FACULTIES ON DEPARTMENTS.fac_id = FACULTIES.id " +
                     "INNER JOIN UNIVERSITIES ON FACULTIES.uni_id = UNIVERSITIES.id " +
                     "WHERE UNIVERSITIES.id = ? AND FACULTIES.id = ?";

        List<DepartmentDTO> departments = new ArrayList<>();
        try{
            Connection conn = connectionPool.getConnection();
            try(PreparedStatement preparedStatement = conn.prepareStatement(sql)){
                preparedStatement.setInt(1, uniId);
                preparedStatement.setInt(2, facId);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);

                    departments.add(new DepartmentDTO(id, facId, name));
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

        return departments;
    }

}
