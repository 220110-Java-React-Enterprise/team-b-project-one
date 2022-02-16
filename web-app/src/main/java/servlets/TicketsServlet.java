package servlets;

import ORM.ORM;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.Tickets;
import utils.DBcredentials;
import utils.FileLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    basic CRUD functionality for TicketServlet
    using custom ORM added in Project Structure
    all exceptions are logged into a .txt file
 */

public class TicketsServlet extends HttpServlet {

    // doGet - cRud - function for Ticket Servlet
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Tickets payload = mapper.readValue(req.getInputStream(), Tickets.class);
            ORM orm = new ORM();
            DBcredentials cred = new DBcredentials();
            orm.connect(cred.getHostname(),
                    cred.getPort(),
                    cred.getDbname(),
                    cred.getUsername(),
                    cred.getPassword());


            orm.ormEntry(payload, "search");
            payload = (Tickets) orm.ormEntry(payload, "search");
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
            Tickets payload = mapper.readValue(req.getInputStream(), Tickets.class);
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
            Tickets payload = mapper.readValue(req.getInputStream(), Tickets.class);
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
            Tickets payload = mapper.readValue(req.getInputStream(), Tickets.class);
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
