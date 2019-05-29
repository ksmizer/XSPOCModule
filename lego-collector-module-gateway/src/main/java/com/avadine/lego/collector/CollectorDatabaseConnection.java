package com.avadine.lego.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CollectorDatabaseConnection {
    Logger logger = LoggerFactory.getLogger(getClass());
    Connection conn;

    public CollectorDatabaseConnection() {

    }

    public CollectorDatabaseConnection(Logger thisLogger, Connection thisConn) {
        logger = thisLogger;
        conn = thisConn;
    }

    public CollectorDatabaseConnection(Logger thisLogger, String userName, String password, String serverName, String port, String databaseName) {
        logger = thisLogger;
        conn = getConnection(userName, password, serverName, port, databaseName);
    }

    public CollectorDatabaseConnection(Logger thisLogger, String userName, String password, String connectionString) {
        logger = thisLogger;
        conn = getConnection(userName, password, connectionString);
    }

    public CollectorDatabaseConnection(Connection thisConn) {
        conn = thisConn;
    }

    public CollectorDatabaseConnection(String userName, String password, String serverName, String port, String databaseName) {
        conn = getConnection(userName, password, serverName, port, databaseName);
    }

    public CollectorDatabaseConnection(String userName, String password, String connectionString) {
        conn = getConnection(userName, password, connectionString);
    }
    
    public Connection getConnection(String userName, String password, String serverName, String port, String databaseName){
        if (conn == null) {    
            StringBuilder str = new StringBuilder(255);
            str.append("jdbc:sqlserver://");
            str.append(serverName);
            str.append("\\MSSQLSERVER:");
            str.append(port);
            str.append(";databaseName=");
            str.append(databaseName);
            String url = str.toString();

            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection connection = DriverManager.getConnection(url, userName, password);
                conn = connection;

            } catch (ClassNotFoundException ex){
                logger.error(ex.getMessage());
            } catch (SQLException ex){
                logger.error(ex.getMessage());
            }
        }
        return conn;
    }
    
    public Connection getConnection(String userName, String password, String connectionString){
        if (conn == null) {    
            String url = connectionString;
            logger.info("Connection is Null, Creating connection");
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection connection = DriverManager.getConnection(url, userName, password);
                conn = connection;
            } catch (ClassNotFoundException ex){
                logger.error(ex.getMessage());
            } catch (SQLException ex){
                logger.error("SQL Exception:",ex);
            }
        }
        logger.debug(conn.toString());
        return conn;
    }

    public void closeConnection(){
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException ex){
                logger.error(ex.getMessage());
            }
        }
    }

    public Integer insertPoint(PointToInsert point) {
        String query = "EXEC INS_COLL_PT_VALU ?,?,?,?";

        try {
            CallableStatement  ps = conn.prepareCall(query);
            ps.setEscapeProcessing(true);
            ps.setQueryTimeout(30);
            ps.setInt("CollectionPointId", point.Id);
            ps.setDate("EffectiveDateTime", point.EffectiveDate);
            ps.setString("CollectionPointValueText", point.PointValue);
            ps.setInt("DurationSeconds", point.Duration);
            ps.execute();
            return ps.getInt(1);

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return -1;
    }

    public List<Point> getPoints(Integer collectionSourceId) {
        ResultSet rs;
        List<Point> points = new ArrayList<Point>();
        String query = "EXEC GET_COLL_PT_TO_CLCT ?";
        
        try {
            CallableStatement  ps = conn.prepareCall(query);
            ps.setEscapeProcessing(true);
            ps.setQueryTimeout(30);
            ps.setInt("CollectionSourceId", collectionSourceId);
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                Point point = new Point();
                point.Id = rs.getInt("CollectionPointId");
                point.TagPath = rs.getString("TagName");
                point.ValidateTagPath = rs.getString("ValidateTagName");
                point.EffectiveDate = rs.getDate("EffectiveDate");
                points.add(point);
            }

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return points;
    }

    public List<Integer> getCollectionSourceIds() {
        ResultSet rs;
        List<Integer> ids = new ArrayList<Integer>();
        String query = "SELECT [COLL_SRCE_ID],[COLL_SRCE_DESC] FROM [COLLECTOR].[dbo].[COLL_SRCE] WHERE COLL_SRCE_DESC LIKE '%Ignition%'";
        
        try {
            CallableStatement  ps = conn.prepareCall(query);
            ps.setEscapeProcessing(true);
            ps.setQueryTimeout(30);
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                Integer id = rs.getInt("COLL_SRCE_ID");
                ids.add(id);
            }

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return ids;
    }
}