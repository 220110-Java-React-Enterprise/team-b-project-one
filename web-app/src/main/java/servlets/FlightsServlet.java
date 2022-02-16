package servlets;

import ORM.ORM;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.Flights;
import utils.DBcredentials;
import utils.FileLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    basic CRUD functions for the FlightsServlet
    using customer ORM, added through Project Structure
    all exceptions are logged into a .txt file
 */

public class FlightsServlet extends HttpServlet {

    // doGet - cRud - function for Flight Servlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Flights payload = mapper.readValue(req.getInputStream(), Flights.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());

            orm.ormEntry(payload, "search");
            payload = (Flights) orm.ormEntry(payload, "search");
            String json = mapper.writeValueAsString(payload);
            resp.getWriter().print(json);
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
            Flights payload = mapper.readValue(req.getInputStream(), Flights.class);
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
            FileLogger.getFileLogger().log(e);
        }
    }

    // doPut - crUd -
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Flights payload = mapper.readValue(req.getInputStream(), Flights.class);
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
            Flights payload = mapper.readValue(req.getInputStream(), Flights.class);
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
