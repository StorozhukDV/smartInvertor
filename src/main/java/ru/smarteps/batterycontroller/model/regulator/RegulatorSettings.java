package ru.smarteps.batterycontroller.model.regulator;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.Map;

@XmlRootElement(name = "RSettings")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class RegulatorSettings {
    private double Unom;
    private double uSetPoint;
    /** Min diff between measured value and setpoint to start regulation */
    private double minDeltaU = 0.5;
    /** time of calculation step in ms */
    private long regulatorStepMs = 500;
    private long initialStartDelay = 2500;

    private double minOutput;
    private double maxOutput;

    private SetpointsControl setpointsControl = SetpointsControl.Automatic;
    private OperationMode operationMode = OperationMode.Adapter;

    private long timeInModeSetpoint = 10_000;

    private double sAngle = -1;


    private Map<Tag, String> tags = Map.of(
            Tag.MeasVoltage, "U",
            Tag.MeasPOutput, "Pgen",
            Tag.MeasQOutput, "Qgen",
            Tag.MeasICharging, "ICharge",
            Tag.MeasSOC, "SOC",

            Tag.CtrlPOutput, "PgenSet",
            Tag.CtrlQOutput, "QgenSet",
            Tag.CtrlICharging, "IChargeSet"
    );

    private double Kp, Ki, Kd;
}
