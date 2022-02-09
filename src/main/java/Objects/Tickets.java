package Objects;

import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name="tickets")
public class Tickets {

    @PrimaryKey(name = "tickerId", type="int")
    private int tickerId;
    @ForeignKey(columnName = "customerId", type = "int", referenceTableName = "customers", referenceTableColumn = "customerId")
    private int customerId;
    @ForeignKey(columnName = "flightId", type = "int", referenceTableName = "flights", referenceTableColumn = "flightId")
    private int flightId;


}
