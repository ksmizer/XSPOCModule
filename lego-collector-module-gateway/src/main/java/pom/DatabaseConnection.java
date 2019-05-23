package pom;

import org.apache.log4j.Logger;
import java.sql.*;

public class DatabaseConnection {
    Logger logger;
    Connection conn;

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

    public int executeQuery(String query, String[] params) {
        try {
            CallableStatement  ps = conn.prepareCall(query);
            ps.setEscapeProcessing(true);
            ps.setQueryTimeout(30);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i+1, params[i]);
            }
            ps.execute();
            return ps.getInt(1);

        } catch (SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return -1;
    }

    public int executeSProc(String sproc, String[] params) {
        String[] parameters = new String[params.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = "?";
        }
        
        StringBuilder proc = new StringBuilder(255);
        proc.append("EXEC ");
        proc.append(sproc);
        proc.append(" ");
        proc.append(String.join(",", parameters));
        String query = proc.toString();

        return executeQuery(query, params);
    }
}