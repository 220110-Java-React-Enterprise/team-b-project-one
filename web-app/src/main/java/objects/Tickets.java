package objects;

import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "tickets")
public class Tickets {

    @PrimaryKey(name = "ticket_id", type="int")
    private int ticket_id;
    @ForeignKey(columnName = "customer_id", type = "int", referenceTableName = "customers", referenceTableColumn = "customer_id")
    private int customer_id;
    @ForeignKey(columnName = "flight_id", type = "int", referenceTableName = "flights", referenceTableColumn = "flight_id")
    private int flight_id;


    public Tickets() {
    }

    public int getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(int ticket_id) {
        this.ticket_id = ticket_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public int getFlight_id() {
        return flight_id;
    }

    public void setFlight_id(int flight_id) {
        this.flight_id = flight_id;
    }
}
