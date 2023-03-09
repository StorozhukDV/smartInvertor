package ru.smarteps.batterycontroller.service.regulator;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.SetpointsControl;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;
import ru.smarteps.batterycontroller.model.regulator.Tag;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;
import ru.smarteps.batterycontroller.service.regulator.mode.OperationModeSelector;
import ru.smarteps.batterycontroller.service.regulator.pid.AdapterPID;
import ru.smarteps.batterycontroller.service.regulator.pid.ChargingPID;
import ru.smarteps.batterycontroller.service.regulator.pid.GenerationPID;
import ru.smarteps.batterycontroller.service.regulator.pid.GenericPid;
import ru.smarteps.batterycontroller.utils.WorkWithCfg;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class SmartInverterController implements PID{

    @Autowired
    private DataContainer dataContainer;

    @Value("${regulator.client-cfg-path}")
    private String pathToCfg;

    private RegulatorSettings rs;

    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> currentTask;
    private final Map<OperationMode, GenericPid> pidsByType = new HashMap<>();
    private OperationModeSelector modeSelector;
    private double prevOutputValue;
    GenericPid pid;


    @Override
    public boolean start() {
        /* perform general interrogation*/
        if (currentTask == null || currentTask.isCancelled()) {
            log.info("starting regulation");
            currentTask = taskScheduler.scheduleAtFixedRate(this::operationCycle, new Date(System.currentTimeMillis() + rs.getInitialStartDelay()), rs.getRegulatorStepMs());
            return true;
        } else {
            log.warn("Can not start regulation as process was already started");
            return false;
        }
    }

    @Override
    public boolean stop() {
        if (currentTask != null && !currentTask.isCancelled()) {
            log.info("stopped regulation");
            currentTask.cancel(true);
            pidsByType.values().forEach(GenericPid::stopRegulation);
            return true;
        } else {
            log.warn("regulation can not be stopped as it was not started");
            return false;
        }
    }

    @Override
    public boolean changeSetpoint(double setpoint, SetpointsControl mode) {
        //TODO: add checks of received Setpoint
        if (mode == SetpointsControl.Automatic) {
            rs.setUSetPoint(setpoint);
            log.warn("new set point was applied to SKRM regulator. Uset = {}", setpoint);
        } else {
            //TODO: add manual setpoints setting
        }
        return true;
    }

    @Override
    public boolean resetAdapter() {
        pid = pidsByType.get(OperationMode.Adapter);
        pid.resetAdapter();
        return true;
    }

    //TODO: add direct control mode
    /**
     * method check voltage measurements, determines regulator operation mode, calculate output value and send it to equipment
     */
    private void operationCycle(){
        Optional<MeasurementTO> uBusWrap = dataContainer.findLastByTag(rs.getTags().get(Tag.MeasVoltage));
        if (uBusWrap.isEmpty() || uBusWrap.get().getValue() == null) {
            log.error("Smart inverter control can not be performed: Can not find voltage measurement");
            return;
        }

        Double ctrlVoltage = uBusWrap.orElse(new MeasurementTO()).getValue();
        if (!checkVoltage(ctrlVoltage)){
            log.warn("Voltage level is out of available range: Umeas = {} ; Unom = {}. Regulation process is blocked", ctrlVoltage, rs.getUnom());
            return;
        }
        modeSelector.checkModeSwitchingNeed(prevOutputValue, ctrlVoltage);
        pid = pidsByType.get(rs.getOperationMode());
        double calcOutput = pid.calculateOutputValue(ctrlVoltage);
        if (prevOutputValue != calcOutput) {
            pid.sendOutput();
            prevOutputValue = calcOutput;
        }
    }


    /**
     * Checks measured voltage level in accordance to max given voltage deviation
     *
     * @param Umeas - measured voltage value
     * @return if measurements in a given range
     */
    private boolean checkVoltage(double Umeas) {
        return Math.abs(Umeas - rs.getUnom()) < 0.2 * rs.getUnom();
    }

    @PostConstruct
    private void readConfig() {
        rs = WorkWithCfg.unMarshalAny(RegulatorSettings.class, pathToCfg);
        pidsByType.put(OperationMode.Charging, new ChargingPID(dataContainer, rs));
        pidsByType.put(OperationMode.PowerOutput, new GenerationPID(dataContainer, rs));
        pidsByType.put(OperationMode.Adapter, new AdapterPID(dataContainer, rs));
        modeSelector = new OperationModeSelector(rs);
    }



}
