package ru.smarteps.batterycontroller.service.regulator.pid;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;

import java.util.Optional;

@Slf4j
public abstract class GenericPid {

    protected double prevOutput = Double.MAX_VALUE;
    protected boolean workWasPaused = true;
    protected double Integ;

    protected final DataContainer dataContainer;
    protected final RegulatorSettings rs;

    protected GenericPid(DataContainer dataContainer, RegulatorSettings rs) {
        this.dataContainer = dataContainer;
        this.rs = rs;
    }

    /**
     * calculates output value of regulator
     *
     * @param Umeas - measured value if voltage
     * @return outValue - output value of regulator to achieve U setpoint
     */
    public abstract double calculateOutputValue(double Umeas);

    /**
     * returns type of regulator
     * @return Operation mode which this regulator is supposed to be used
     */
    public abstract OperationMode getModeType();

    /**
     * sends last generated value (prevOutput) to object via Data container
     */
    public abstract void sendOutput();

    /**
     * returns preveous calculated value of regulator
     * @return previous value
     */
    public double getPreviousOutputValue(){
        return prevOutput;
    }

    /**
     * sets @param workWasPause property to true value, which makes regulator measure output value and set it as Integral part
     */
    public void stopRegulation(){
        workWasPaused = true;
    }

    /**
     * sets pTest and qTest parameters to true value which provides adapter mode.
     */
    public void resetAdapter(){}

    protected double getMeasuredOutput(String measTag) {
        Optional<MeasurementTO> measP = dataContainer.findLastByTag(measTag);
        if (measP.isEmpty()) {
            log.warn("Regulated value was not found in measurements. Has to set it to 0 ");
            return 0;
        } else {
            return measP.get().getValue();
        }
    }

}
