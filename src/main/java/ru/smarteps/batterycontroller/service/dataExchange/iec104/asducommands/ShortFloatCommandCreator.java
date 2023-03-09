package ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands;


import org.openscada.protocol.iec60870.asdu.ASDUHeader;
import org.openscada.protocol.iec60870.asdu.message.SetPointCommandShortFloatingPoint;
import org.openscada.protocol.iec60870.asdu.message.SetPointCommandShortFloatingPointTime;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.openscada.protocol.iec60870.asdu.types.CommandValue;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.smarteps.batterycontroller.model.dataExchange.CommandType;

@Component("ShortFloatingSetPoint")
public class ShortFloatCommandCreator implements ICommandCreator {
    private static final Logger log = LoggerFactory.getLogger(ShortFloatCommandCreator.class);

    @Override
    public boolean createAndSend(AutoConnectClient client, Object objVal, int asduAddr, int ioAddr, boolean useTimeTag, boolean invertSignal) {

        if (!(objVal.getClass().equals(Double.class) || objVal.getClass().equals(Float.class)|| objVal.getClass().equals(Integer.class))) {
            log.error("Short float command must be of Double or of Float class");
            return false;
        }
        float cmdVal;
        if (objVal.getClass().equals(Double.class)) {
            Double commandValue = (Double) objVal;
            if (commandValue > Float.MAX_VALUE || commandValue < -1 * Float.MAX_VALUE) {
                log.error("Short float command must be less than {} and more than {}. Command will not be executed. Got value {}", Float.MIN_VALUE, Float.MAX_VALUE, commandValue);
                return false;
            }
            cmdVal = (float) commandValue.doubleValue();
        } else if (objVal.getClass().equals(Integer.class)) {

            cmdVal = ((Integer) objVal).floatValue();

        } else {
            cmdVal = (float) objVal;
        }


        Object command;
        if (useTimeTag) {
            /* case of C_SE_TC_1 */
            command = new SetPointCommandShortFloatingPointTime(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr), new CommandValue<>(cmdVal, System.currentTimeMillis()));
        } else {
            /* case of C_SE_NC_1 */
            command = new SetPointCommandShortFloatingPoint(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr), cmdVal);
        }

        return client.writeCommand(command);
    }


    @Override
    public String getMyName() {
        return CommandType.ShortFloatingSetPoint.toString();
    }
}
