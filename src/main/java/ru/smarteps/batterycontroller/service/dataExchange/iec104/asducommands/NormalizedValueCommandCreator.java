package ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands;//package client104.asducommands;


import org.openscada.protocol.iec60870.asdu.ASDUHeader;
import org.openscada.protocol.iec60870.asdu.message.SetPointCommandNormalizedValue;
import org.openscada.protocol.iec60870.asdu.message.SetPointCommandNormalizedValueTime;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.openscada.protocol.iec60870.asdu.types.CommandValue;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.smarteps.batterycontroller.model.dataExchange.CommandType;

@Component("NormalizedSetPoint")
public class NormalizedValueCommandCreator implements  ICommandCreator{
    private static final Logger log = LoggerFactory.getLogger(NormalizedValueCommandCreator.class);

    @Override
    public boolean createAndSend(AutoConnectClient client, Object objVal, int asduAddr, int ioAddr, boolean useTimeTag, boolean invertSignal) {

        if (!objVal.getClass().equals(Integer.class)) {
            log.error("Normalized Value Command must be of Integer class");
            return false;
        }

        int cmdVal = (Integer) objVal;
        Object command;
        if (useTimeTag) {
            /* case of C_SE_TA_1 */
            command = new SetPointCommandNormalizedValueTime(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr), new CommandValue<>((double)cmdVal, System.currentTimeMillis()));
        } else {
            /* case of C_SE_NA_1 */
            command = new SetPointCommandNormalizedValue(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr), cmdVal);
        }
        return client.writeCommand(command);
    }

    @Override
    public String getMyName() {
        return CommandType.NormalizedSetPoint.toString();
    }
}
