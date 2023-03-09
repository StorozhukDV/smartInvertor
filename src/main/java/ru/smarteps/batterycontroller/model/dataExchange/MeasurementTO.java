package ru.smarteps.batterycontroller.model.dataExchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeasurementTO {
    private String time;
    private Double value;
    private String name;

    public MeasurementTO(String name) {
        this.name = name;
    }

    public static MeasurementTO getEmpty(){
        MeasurementTO m = new MeasurementTO();
        m.setValue(-1.0);
        return  m;
    }

}
