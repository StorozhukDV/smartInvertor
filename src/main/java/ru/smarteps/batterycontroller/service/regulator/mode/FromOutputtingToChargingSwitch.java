package ru.smarteps.batterycontroller.service.regulator.mode;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

@Slf4j
public class FromOutputtingToChargingSwitch implements ModeSwitcher{
    private long timeInSwitchableMode = 0;

    @Override
    public OperationMode execute (double currentOutput, double Umeas, RegulatorSettings rs) {
        if (rs.getOperationMode() == OperationMode.PowerOutput) {
            boolean PQOutputZero = currentOutput == 0;
            boolean UmeasHigh = Umeas > rs.getUSetPoint() + rs.getMinDeltaU();
            if (PQOutputZero && UmeasHigh) {
                if (timeInSwitchableMode == 0) {
                    timeInSwitchableMode = System.currentTimeMillis();
                }
                boolean timePassed = System.currentTimeMillis() - timeInSwitchableMode > rs.getTimeInModeSetpoint();
                if (timePassed) {
                    log.info("Changing mode from power output to power consuming (charging)");
                    timeInSwitchableMode = 0;
                    return OperationMode.Charging;
                }
            } else {
                timeInSwitchableMode = 0;
            }
            log.debug("No need to switch mode to Charging");
        } else {
            timeInSwitchableMode = 0;
        }
        /* remain the previous value*/
        return rs.getOperationMode();
    }
}
