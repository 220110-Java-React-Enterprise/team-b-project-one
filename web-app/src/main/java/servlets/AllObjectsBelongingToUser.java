package servlets;

import ORM.ORM;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.Customers;
import objects.Flights;
import objects.Tickets;
import utils.DBcredentials;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AllObjectsBelongingToUser extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Object> objects = new LinkedList<>();
        objects.add(new Customers());
        objects.add(new Flights());
        objects.add(new Tickets());

        ObjectMapper mapper = new ObjectMapper();
       Customers payload = mapper.readValue(req.getInputStream(), Customers.class);
        ORM orm = new ORM();
        DBcredentials cred = new DBcredentials();
        orm.connect(cred.getHostname(),
                cred.getPort(),
                cred.getDbname(),
                cred.getUsername(),
                cred.getPassword());

            orm.objectTypes(objects);
            orm.ormEntry(payload, "getall");
            objects = orm.getAllObjects();
            String JSON = "";

            for (Object obj : objects){

                    JSON += mapper.writeValueAsString(obj);

            }

        resp.getWriter().print(JSON);
        resp.setStatus(200);
    }
}
