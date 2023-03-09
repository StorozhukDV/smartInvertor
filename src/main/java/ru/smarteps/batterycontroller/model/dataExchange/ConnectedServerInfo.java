package ru.smarteps.batterycontroller.model.dataExchange;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "root")
public class ConnectedServerInfo {
    private String serverId;
    private String address;
    private int port;
    private int timeout;

    @XmlElementWrapper(name = "measurements")
    @XmlElement(name="measurement")
    private List<Measurement> measurements = new ArrayList<>();

    @XmlElementWrapper(name="commands")
    @XmlElement(name="command")
    private List<Command> commands = new ArrayList<>();


    public String getServerInfo(){
        return "server address : "+ address +", port : "+port;
    }
}
