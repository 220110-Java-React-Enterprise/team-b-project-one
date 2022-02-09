package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class ConnectionManager {

    private static Connection connection;
    private static String dbName;

    private ConnectionManager() {
    }

    public static Connection getConnection(String hostname, String port, String DBname, String userName, String password) {
        if (connection == null) {
                    connection = connect(hostname, port, DBname, userName, password);
                    dbName = DBname;
                    System.out.println("Connection successful");

        }
        return connection;
    }

    public static String getDbName(){
        return dbName;
    }

    private static Connection connect(String hostname, String port, String DBName, String userName, String password) {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String connectionString = "jdbc:mariadb://" + hostname + ":" + port + "/" + DBName + "?user=" +
                    userName + "&password=" + password;
            connection = DriverManager.getConnection(connectionString);
            //System.out.println(connectionString);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}