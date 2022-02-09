package Objects;

import Annotations.Column;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "flights")
public class Flights {

    @PrimaryKey(name="flightId", type = "int")
    private int flightId;
    @Column(name="from_location", type = "string")
    private String from;
    @Column(name="to_location", type="string")
    private String to;



}
