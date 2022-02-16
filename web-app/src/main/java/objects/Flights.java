package objects;

import Annotations.Column;
import Annotations.ForeignKey;
import Annotations.PrimaryKey;
import Annotations.Table;

@Table(name = "flights")
public class Flights {

    @PrimaryKey(name = "flight_id", type = "int")
    private Integer flight_id;


    @Column(name = "from_location", type = "string")
    private String from_location;

    @Column(name = "to_location", type = "string")
    private String to_location;

    public Flights() {

    }

    public Integer getFlight_id() {
        return flight_id;
    }

    public void setFlight_id(Integer flight_id) {
        this.flight_id = flight_id;
    }

    public String getFrom_location() {
        return from_location;
    }

    public void setFrom_location(String from_location) {
        this.from_location = from_location;
    }

    public String getTo_location() {
        return to_location;
    }

    public void setTo_location(String to_location) {
        this.to_location = to_location;
    }
}
