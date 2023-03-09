package ru.smarteps.batterycontroller.model.dataExchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Command{
    @XmlAttribute
    private Double value = null;
    @XmlAttribute
    private int ioAddress;
    @XmlAttribute
    private int commonAddress;
    @XmlAttribute
    private int asduAddress;
    @XmlAttribute
    private CommandType commandType;
    @XmlAttribute
    private boolean setTimeTag = false;
    @XmlAttribute
    private String name;
    @XmlTransient
    private boolean allowedToSend = true;

    public Command(int ioAddress, int commonAddress, CommandType commandType) {
        this.ioAddress = ioAddress;
        this.commonAddress = commonAddress;
        this.commandType = commandType;
    }

    public String getFullName(){
        return commonAddress + commandType.toString() + ioAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return ioAddress == command.ioAddress &&
                commonAddress == command.commonAddress &&
                commandType == command.commandType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ioAddress, commonAddress, commandType);
    }
}
