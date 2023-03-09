package ru.smarteps.batterycontroller.service.regulator.pid;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;
import ru.smarteps.batterycontroller.model.regulator.Tag;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;

import java.util.Optional;

@Slf4j
public class ChargingPID extends GenericPid{
    private final String measTag;
    private double maxChargeValue = 95;

    public ChargingPID(DataContainer dataContainer, RegulatorSettings rs) {
        super(dataContainer, rs);
        this.measTag = rs.getTags().get(Tag.MeasICharging);
    }

    @Override
    public double calculateOutputValue(double Umeas) {
        if (workWasPaused) {
            workWasPaused = false;
            Integ = getMeasuredOutput(measTag);
            log.debug("Work was paused. Integral part was updated to {}", Integ);
        }
        double stateOfCharge = getSoC();
        if (stateOfCharge == -1.0 || stateOfCharge > maxChargeValue) {
            workWasPaused = true;
            return 0.0;
        }

        double dU = calcDeltaU(Umeas);

        if (Math.abs(dU) > 0) {
            log.debug("U measured is in regulation area, need to change Iq. prev integral value {}", Integ);
            Integ = rs.getKi() * dU + Integ;

            if (Integ > rs.getMaxOutput()) Integ = rs.getMaxOutput();
            if (Integ < rs.getMinOutput()) Integ = rs.getMinOutput();

            if (prevOutput != Integ){
                log.info("Regulation process for Iq: U setpoint = {}, U measured = {}, U delta = {},  output value = {}", rs.getUSetPoint(), Umeas, dU, Integ);
                prevOutput = Integ;
            }
        } else {
            log.debug("Iq output value does not change");
        }
        return Integ;
    }

    @Override
    public OperationMode getModeType() {
        return OperationMode.Charging;
    }

    @Override
    public void sendOutput() {

        dataContainer.setCommand(rs.getTags().get(Tag.CtrlICharging), prevOutput);
    }

    private double calcDeltaU(double uMeas){
        if (uMeas > rs.getUSetPoint()) {
            return uMeas - rs.getUSetPoint();
        } else if (uMeas < rs.getUSetPoint() - rs.getMinDeltaU() ){
            return uMeas - (rs.getUSetPoint() - rs.getMinDeltaU());
        } else {
            return 0;
        }
    }

    private double getSoC(){
        Optional<MeasurementTO> socWrap = dataContainer.findLastByTag(rs.getTags().get(Tag.MeasSOC));
        if (socWrap.isEmpty() || socWrap.get().getValue() == null) {
            log.error("State of charge if not found. regulation will be stopped");
            return -1.0;
        }
        if (socWrap.get().getValue() > maxChargeValue) {
            log.info("State of charge is bigger than {}%, stopping charging...",maxChargeValue);
        }
        return socWrap.get().getValue();
    }

}
