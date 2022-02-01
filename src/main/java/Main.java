import Util.ConnectionManager;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = ConnectionManager.getConnection();
            //ViewManager.getViewManager().navigate("welcome");

        } catch (Exception e) {
            e.printStackTrace();
        }

        Scanner kb = new Scanner(System.in);
        int input = kb.nextInt();
        kb.nextLine();
    }
}
