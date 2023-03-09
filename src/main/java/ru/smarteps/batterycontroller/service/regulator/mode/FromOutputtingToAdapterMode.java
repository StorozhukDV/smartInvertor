package ru.smarteps.batterycontroller.service.regulator.mode;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

@Slf4j
public class FromOutputtingToAdapterMode implements ModeSwitcher{

    private long timeInSwitchableMode = 0;

    @Override
    public OperationMode execute (double currentOutput, double Umeas, RegulatorSettings rs) {
        if (rs.getOperationMode() == OperationMode.PowerOutput) {
            if (rs.getSAngle()==-1) {
                if (timeInSwitchableMode == 0) {
                    timeInSwitchableMode = System.currentTimeMillis();
                }
                boolean timePassed = System.currentTimeMillis() - timeInSwitchableMode > rs.getTimeInModeSetpoint();
                if (timePassed) {
                    log.info("Changing mode from power output to adapter mode.");
                    timeInSwitchableMode = 0;
                    return OperationMode.Adapter;
                }
            } else {
                timeInSwitchableMode = 0;
            }
            log.debug("No need to switch mode to Adapter");
        } else {
            timeInSwitchableMode = 0;
        }
        /* remain the previous value*/
        return rs.getOperationMode();
    }
}
