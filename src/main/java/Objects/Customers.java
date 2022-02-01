package Objects;

import Annotations.Column;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "Customers")
public class Customers {
    @PrimaryKey(name = "customerId")
    private int customerId;
    @Column(name = "firstName")
    private int firstName;
    @Column(name= "lastName")
    private int lastName;





    public class kids {
        private int age;
        kids(){

        }
    }
}
