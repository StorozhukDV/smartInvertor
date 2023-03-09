package ru.smarteps.batterycontroller.service.regulator.mode;

import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

public interface ModeSwitcher {

    OperationMode execute (double currentOutput, double Umeas, RegulatorSettings rs);

}
