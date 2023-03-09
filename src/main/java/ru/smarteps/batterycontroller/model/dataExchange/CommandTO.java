package ru.smarteps.batterycontroller.model.dataExchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandTO {
    private String name;
    private Double value;


}
