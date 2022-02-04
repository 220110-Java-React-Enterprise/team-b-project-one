package Objects;

import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "Customers")
public class Customers {
    @PrimaryKey(name = "customerId", type="int")
    private int customerId;
    @Column(name = "firstName", type= "string")
    private String firstName;
    @Column(name= "lastName", type="string")
    private String lastName;
    private boolean test;
    @ForeignKey(columnName = "fk", type = "int", referenceTableName = "tickets", referenceTableColumn = "ticket_id")
    public int fk;

}
