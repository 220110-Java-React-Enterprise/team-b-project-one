import ORM.ORM;
import Util.ConnectionManager;
import Objects.Customers;
import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        try {
//            Connection connection = ConnectionManager.getConnection();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Scanner kb = new Scanner(System.in);
//        int input = kb.nextInt();
//        kb.nextLine();
//    }

        ORM orm = new ORM();
        Customers customer = new Customers();
        //orm.ORM(customer, "create");
        orm.ORM(customer, "insert");

    }
}