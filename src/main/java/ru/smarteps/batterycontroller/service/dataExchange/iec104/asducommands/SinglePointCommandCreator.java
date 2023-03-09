package ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands;

import lombok.extern.slf4j.Slf4j;
import org.openscada.protocol.iec60870.asdu.ASDUHeader;
import org.openscada.protocol.iec60870.asdu.message.SingleCommand;
import org.openscada.protocol.iec60870.asdu.message.SingleCommandTime;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.openscada.protocol.iec60870.asdu.types.CommandValue;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.smarteps.batterycontroller.model.dataExchange.CommandType;

@Component("SinglePointSetPoint")
@Slf4j
public class SinglePointCommandCreator implements ICommandCreator {

    @Override
    public boolean createAndSend(AutoConnectClient client, Object val, int asduAddr, int ioAddr, boolean useTimeTag, boolean invertSignal) {

        if (!val.getClass().equals(Boolean.class)){
            log.error("Single command must be equal boolean. Got value {}", val);
            return false;
        }
        Boolean value = (Boolean) val;
        Object command;
        if (useTimeTag) {
            /* case of C_SC_TA_1 */
            command = new SingleCommandTime(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr),new CommandValue<>(value, System.currentTimeMillis()));
        } else {
            /* case of C_SC_NA_1 */
            command = new SingleCommand(
                    new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                    InformationObjectAddress.valueOf(ioAddr), value);
        }

        return client.writeCommand(command);
    }

    @Override
    public String getMyName() {
        return CommandType.SinglePointSetPoint.toString();
    }
}
