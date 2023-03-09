package ru.smarteps.batterycontroller.service.regulator;

import ru.smarteps.batterycontroller.model.regulator.SetpointsControl;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

public interface PID {

    boolean start();

    boolean stop();

    boolean changeSetpoint(double setpoint, SetpointsControl mode);

    boolean resetAdapter();

}
