package Util;

import java.sql.Connection;
import java.util.Properties;
import java.sql.DriverManager;
import java.util.Scanner;

public class ConnectionManager {

    private static Connection connection;

    private ConnectionManager() {
    }

    public static Connection getConnection() {
        Scanner sc = new Scanner(System.in);
        if (connection == null) {
            System.out.println("Hostname: ");
            String hostname = sc.nextLine();
            System.out.println("Port: ");
            String port = sc.nextLine();
            System.out.println("Database Name: ");
            String DBName = sc.nextLine();
            System.out.println("UserName: ");
            String userName = sc.nextLine();
            System.out.println("Password: ");
            String password = sc.nextLine();
            connection = connect(hostname, port, DBName, userName, password);
        }
        return connection;
    }


    private static Connection connect(String hostname, String port, String DBName, String userName, String password) {
        try {
            String connectionString = "jdbc:mariadb://" + hostname + ":" + port + "/" + DBName + "?user=" +
                    userName + "&password=" + password;
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(connectionString);
            //System.out.println(connectionString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}