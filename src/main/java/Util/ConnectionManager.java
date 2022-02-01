package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class ConnectionManager {

    private static Connection connection;

    private ConnectionManager() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                File file = new File("C:\\Users\\majaw\\IdeaProjects\\repos\\project-one-props\\props.txt");
                Scanner reader = new Scanner(file);
                while (reader.hasNextLine()) {
                    String hostname = reader.nextLine();
                    String port = reader.nextLine();
                    String DBName = reader.nextLine();
                    String userName = reader.nextLine();
                    String password = reader.nextLine();
                    connection = connect(hostname, port, DBName, userName, password);
                    System.out.println("Connection successful");
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }




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