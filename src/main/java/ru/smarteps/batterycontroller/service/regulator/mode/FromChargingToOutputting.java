package ru.smarteps.batterycontroller.service.regulator.mode;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

@Slf4j
public class FromChargingToOutputting implements  ModeSwitcher{

    private long timeInSwitchableMode = 0;

    @Override
    public OperationMode execute(double currentOutput, double Umeas, RegulatorSettings rs) {
        if (rs.getOperationMode() == OperationMode.Charging){
            boolean IqZero = currentOutput == 0;
            boolean UmeasLow = Umeas < rs.getUSetPoint() - rs.getMinDeltaU();
            if (IqZero && UmeasLow){
                /* condition to change mode from output to consuming, last check if required time passed*/
                if (timeInSwitchableMode == 0){
                    timeInSwitchableMode = System.currentTimeMillis();
                }
                boolean timePassed = System.currentTimeMillis() - timeInSwitchableMode > rs.getTimeInModeSetpoint();
                if (timePassed){
                    log.info("Changing mode from charging to power outputting");
                    timeInSwitchableMode = 0;
                    return OperationMode.PowerOutput;
                }
            }else {
                timeInSwitchableMode = 0;
            }
            log.debug("No need to switch mode to Power output");
        } else {
            timeInSwitchableMode = 0;
        }
        /* remain the previous value*/
        return rs.getOperationMode();
    }
}
