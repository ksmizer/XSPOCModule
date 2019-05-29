package com.avadine.lego.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CollectorDatabaseConnection {
    Logger logger = LoggerFactory.getLogger(getClass());
    Connection conn;
    CallableStatement sProc;

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
            // logger.info("Connection is Null, Creating connection");
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
                sProc = null;
            } catch (SQLException ex){
                logger.error(ex.getMessage());
            }
        }
    }

    public Integer insertPoint(PointToInsert point) {
        if (sProc == null) {
            prepareInsertPoint();
        }

        try {
            // sProc.registerOutParameter("ReturnCode", Types.INTEGER);
            sProc.setInt("CollectionPointId", point.Id);
            sProc.setDate("EffectiveDateTime", point.EffectiveDate);
            sProc.setString("CollectionPointValueText", point.PointValue);
            sProc.setInt("DurationSeconds", point.Duration);
            sProc.execute();
            // return sProc.getInt(1);

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return -1;
    }

    public List<Point> getPoints(Integer collectionSourceId) {
        ResultSet rs;
        List<Point> points = new ArrayList<Point>();
        
        if (sProc == null) {
            prepareGetPoints();
        }

        try {
            sProc.setInt("CollectionSourceId", collectionSourceId);
            sProc.execute();
            rs = sProc.getResultSet();
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
        
        if (sProc == null) {
            prepareGetCollectionSourceIds();
        }

        try {
            sProc.execute();
            rs = sProc.getResultSet();
            while (rs.next()) {
                Integer id = rs.getInt("COLL_SRCE_ID");
                // logger.info("Collector Source: " + rs.getString("COLL_SRCE_DESC"));
                ids.add(id);
            }

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return ids;
    }

    public void prepareGetPoints() {
        prepareCall("EXEC GET_COLL_PT_TO_CLCT ?");
    }

    public void prepareInsertPoint() {
        prepareCall("EXEC INS_COLL_PT_VALU ?,?,?,?");
    }
    
    public void prepareGetCollectionSourceIds() {
        // prepareCall("SELECT [COLL_SRCE_ID],[COLL_SRCE_DESC] FROM [COLLECTOR].[dbo].[COLL_SRCE] WHERE COLL_SRCE_DESC LIKE '%Ignition%'");
        prepareCall("SELECT [COLL_SRCE_ID],[COLL_SRCE_DESC] FROM [COLLECTOR].[dbo].[COLL_SRCE] WHERE COLL_SRCE_DESC = 'Ignition Test'");
    }

    public void prepareCall(String query) {
        try {
            sProc = conn.prepareCall(query);
            sProc.setEscapeProcessing(true);
            sProc.setQueryTimeout(30);
        } catch (SQLException e) {
            logger.error("Error preparing call - No Connection Available", e);
        }
    }

    public void resetCall() {
        sProc = null;
    }
}