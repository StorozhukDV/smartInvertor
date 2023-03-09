package ru.smarteps.batterycontroller.model.dataExchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    @XmlAttribute
    private int commonAddress;
    @XmlAttribute
    private int ioAddress;
    @XmlAttribute
    private String name;
//    @XmlAttribute
//    private String modifier;
//    @XmlAttribute
//    private String modifierProp;

    public String sigName(){
        return commonAddress+":"+ioAddress;
    }

    public static String sigName(int commonAddress, int ioAddress){
        return commonAddress+":"+ioAddress;
    }
}
