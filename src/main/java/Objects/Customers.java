package Objects;

import Annotations.Column;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "Customers")
public class Customers {
    @PrimaryKey(name = "customerId", type="int")
    private int customerId;
    @Column(name = "firstName", type= "string")
    private String firstName = "David";
    @Column(name= "lastName", type="string")
    private String lastName = "Alvarado";

}
