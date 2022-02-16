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
/*
    basic CRUD functions for the CustomerServlet
    using the custom ORM, added into Project Structure
    all exceptions are logged into a .txt file
 */

public class CustomerServlet extends HttpServlet {

    // doGet - cRud - function for Customer Servlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Customers payload = mapper.readValue(req.getInputStream(), Customers.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());
            payload = (Customers) orm.ormEntry(payload, "search");
            String JSON = mapper.writeValueAsString(payload);
            resp.getWriter().print(JSON);
            resp.setStatus(200);
        } catch (Exception e) {
            //any thrown exception is written to a text file
            FileLogger.getFileLogger().log(e);
        }
    }

    //doPost - Crud -
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Customers payload = mapper.readValue(req.getInputStream(), Customers.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());

            orm.ormEntry(payload, "insert");
            resp.setStatus(200);
        } catch (Exception e) {
            //FileLogger.getFileLogger().log(e);
            e.printStackTrace();
        }
    }

    // doPut - crUd -
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Customers payload = mapper.readValue(req.getInputStream(), Customers.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());

            orm.ormEntry(payload, "update");
            resp.setStatus(200);
        } catch (Exception e) {
            FileLogger.getFileLogger().log(e);
        }
    }

    //doDelete - cruD -
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Customers payload = mapper.readValue(req.getInputStream(), Customers.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());

            orm.ormEntry(payload, "delete");
            resp.setStatus(200);
        } catch (Exception e) {
            FileLogger.getFileLogger().log(e);
        }
    }
}
