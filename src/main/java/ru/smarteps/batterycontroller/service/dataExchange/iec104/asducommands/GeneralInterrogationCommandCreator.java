package ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands;

import org.openscada.protocol.iec60870.asdu.ASDUHeader;
import org.openscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.springframework.stereotype.Component;
import ru.smarteps.batterycontroller.model.dataExchange.CommandType;

@Component("GeneralInterrogation")
public class GeneralInterrogationCommandCreator implements  ICommandCreator{
    @Override
    public boolean createAndSend(AutoConnectClient client, Object cmdValue, int asduAddr, int ioAddr, boolean useTimeTag, boolean invertSignal) {
        InterrogationCommand command = new InterrogationCommand(
                new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(asduAddr)),
                InformationObjectAddress.valueOf(ioAddr), (short) 20);

         return client.writeCommand(command);
    }

    @Override
    public String getMyName() {
        return CommandType.GeneralInterrogation.toString();
    }
}
