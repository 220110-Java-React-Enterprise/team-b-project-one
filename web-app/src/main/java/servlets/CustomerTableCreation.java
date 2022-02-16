package servlets;

import ORM.ORM;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.Customers;
import utils.DBcredentials;
import utils.FileLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomerTableCreation extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Customers payload = new Customers();
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());

            orm.ormEntry(payload, "create");
            resp.setStatus(200);
        } catch (Exception e) {
            FileLogger.getFileLogger().log(e);
        }
    }
}

