package ru.smarteps.batterycontroller.service.regulator.mode;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OperationModeSelector {
    private final RegulatorSettings rs;
    private final List<ModeSwitcher> switchers = new ArrayList<>();

    public OperationModeSelector(RegulatorSettings rs) {
        switchers.add(new FromChargingToOutputting());
        switchers.add(new FromOutputtingToChargingSwitch());
        switchers.add(new FromAdapterToOutputting());
        switchers.add(new FromOutputtingToAdapterMode());
        this.rs = rs;
    }

    public void checkModeSwitchingNeed(double currentOutput, double uMeas){
        for (ModeSwitcher switcher : switchers) {
            OperationMode newMode = switcher.execute(currentOutput, uMeas, rs);
            if (newMode != rs.getOperationMode()){
                log.info("New mode applied to equipment {}", newMode.name());
                rs.setOperationMode(newMode);
            }
        }
    }
}
