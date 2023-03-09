package ru.smarteps.batterycontroller.service.regulator.pid;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;
import ru.smarteps.batterycontroller.model.regulator.Tag;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;
import ru.smarteps.batterycontroller.service.regulator.adapter.TestAdapter;

@Slf4j
public class GenerationPID extends GenericPid{
    private double dUprev = 0;
    private final String measTag;
    private double fAngle = 0;

    public GenerationPID(DataContainer dataContainer, RegulatorSettings rs) {
        super(dataContainer, rs);
        this.measTag = rs.getTags().get(Tag.MeasPOutput);
    }

    @Override
    public double calculateOutputValue(double Umeas) {

        if (workWasPaused) {
            workWasPaused = false;
            Integ = getMeasuredOutput(measTag);
            dUprev = 0;
            log.debug("Work was paused. Integral part was updated to {}", Integ);
        }
        double outValue = Integ;
        double dU = rs.getUSetPoint() - Umeas;
        if (Math.abs(dU) > rs.getMinDeltaU()) {
            log.debug("delta U exceeds eps. Integral part (Q) = {}. start impact calculation", Integ);
            double prop = rs.getKp() * dU;
            double diff = rs.getKd() * (dU - dUprev) / (rs.getRegulatorStepMs() / 1000.0);
            Integ = rs.getKi() * dU + Integ;

            if (Integ > rs.getMaxOutput()) Integ = rs.getMaxOutput();
            if (Integ < rs.getMinOutput()) Integ = rs.getMinOutput();

            outValue = prop + Integ + diff;
            if (outValue > rs.getMaxOutput()) outValue = rs.getMaxOutput();
            if (outValue < rs.getMinOutput()) outValue = rs.getMinOutput();

            dUprev = dU;

            if (prevOutput != outValue){
                log.info("Regulation process: U setpoint = {}, U measured = {}, U delta = {},  output value = {}", rs.getUSetPoint(), Umeas, dU, outValue);
                prevOutput = outValue;
            }
        } else {
            log.debug("Q output value does not change");
        }

        return outValue;
    }

    @Override
    public OperationMode getModeType() {
        return OperationMode.PowerOutput;
    }

    @Override
    public void sendOutput() {
        fAngle = rs.getSAngle();
        double P = Math.cos(Math.toRadians(fAngle))*prevOutput;
        double Q = Math.sin(Math.toRadians(fAngle))*prevOutput;
        dataContainer.setCommand(rs.getTags().get(Tag.CtrlPOutput), P);
        dataContainer.setCommand(rs.getTags().get(Tag.CtrlQOutput), Q);
    }


}
